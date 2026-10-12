#!/usr/bin/env bash
# No tracing: credentials are consumed via environment and a runner-temp keystore.
set -euo pipefail
set +x
for name in ANDROID_KEYSTORE_BASE64 ANDROID_STORE_PASSWORD ANDROID_KEY_ALIAS ANDROID_KEY_PASSWORD; do
  if [[ -z "${!name:-}" ]]; then echo "Missing required signing input: $name" >&2; exit 1; fi
done
# Installed 1.3.1 baseline; historical certificate unchanged.
installed_version_code=26
umask 077
scratch=$(mktemp -d "${RUNNER_TEMP:?}/spotlight-signing.XXXXXX")
trap 'rm -rf "$scratch"' EXIT
printf '%s' "$ANDROID_KEYSTORE_BASE64" | base64 --decode > "$scratch/historical.keystore"
unset ANDROID_KEYSTORE_BASE64
build_tools="${ANDROID_HOME:?}/build-tools/35.0.0"
unsigned=app/build/outputs/apk/release/app-release-unsigned.apk
"$build_tools/zipalign" -p -f 4 "$unsigned" "$scratch/aligned.apk"
# Tool diagnostics may contain alias/keystore details: keep them temporary and
# report only a generic failure; never publish diagnostic files.
if ! "$build_tools/apksigner" sign --ks "$scratch/historical.keystore" --ks-key-alias "$ANDROID_KEY_ALIAS" --ks-pass env:ANDROID_STORE_PASSWORD --key-pass env:ANDROID_KEY_PASSWORD --out "$scratch/release.apk" "$scratch/aligned.apk" > "$scratch/sign.log" 2>&1; then
  echo 'Historical signing failed; check the four GitHub Secrets.' >&2; exit 1
fi
unset ANDROID_STORE_PASSWORD ANDROID_KEY_PASSWORD ANDROID_KEY_ALIAS
"$build_tools/apksigner" verify --verbose --print-certs "$scratch/release.apk" > "$scratch/verification.txt"
"$build_tools/zipalign" -c -p 4 "$scratch/release.apk"
"$build_tools/aapt" dump badging "$scratch/release.apk" > "$scratch/badging.txt"
python3 scripts/verify_release.py "$scratch/badging.txt" "$installed_version_code" "$scratch/verification.txt" > "$scratch/update-verification.txt"
mkdir -p app/build/outputs/verified-release
cp "$scratch/release.apk" app/build/outputs/verified-release/spotlight-next-release.apk
cp "$scratch/update-verification.txt" app/build/outputs/verified-release/update-verification.txt
cat "$scratch/update-verification.txt" >> "${GITHUB_STEP_SUMMARY:?}"
