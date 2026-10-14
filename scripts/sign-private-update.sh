#!/usr/bin/env bash
# Re-sign an approved debug artifact. No private inputs in logs/artifacts/caches.
set -euo pipefail
set +x
for field in ANDROID_KEYSTORE_BASE64 ANDROID_STORE_PASSWORD ANDROID_KEY_ALIAS ANDROID_KEY_PASSWORD; do
  if [[ -z "${!field:-}" ]]; then echo "Historical signing unavailable: missing $field" >&2; exit 1; fi
done
umask 077
private_dir=$(mktemp -d "${RUNNER_TEMP:?}/spotlight-update-key.XXXXXX")
trap 'rm -rf "$private_dir"' EXIT
printf '%s' "$ANDROID_KEYSTORE_BASE64" | base64 --decode > "$private_dir/historical.keystore"
unset ANDROID_KEYSTORE_BASE64
build_tools="${ANDROID_HOME:?}/build-tools/35.0.0"
mkdir -p delivery/update
output=delivery/update/Spotlight-Next-update-58ac0a2.apk
if ! "$build_tools/apksigner" sign --ks "$private_dir/historical.keystore" --ks-key-alias "$ANDROID_KEY_ALIAS" --ks-pass env:ANDROID_STORE_PASSWORD --key-pass env:ANDROID_KEY_PASSWORD --out "$output" input/candidate/Spotlight-Next-debug-58ac0a2.apk > "$private_dir/sign.log" 2>&1; then
  echo 'Historical signing failed; artifact withheld. Check existing signing Secrets.' >&2; exit 1
fi
unset ANDROID_STORE_PASSWORD ANDROID_KEY_PASSWORD ANDROID_KEY_ALIAS
"$build_tools/apksigner" verify --verbose --print-certs "$output" > delivery/update/certificate.txt
"$build_tools/zipalign" -c 4 "$output"
python3 scripts/verify_update_pair.py final public "$output" delivery/update/certificate.txt | tee delivery/update/update-verification.json
(cd delivery/update && sha256sum Spotlight-Next-update-58ac0a2.apk > SHA256SUMS)
printf '%s\n' '58ac0a2d4cb4f96c9fbef46da4f4104475c251ae' > delivery/update/SOURCE_COMMIT
# Public digests are printed as well as summarized, unlike private signing data.
cat delivery/update/SHA256SUMS
cat delivery/update/update-verification.json >> "$GITHUB_STEP_SUMMARY"
