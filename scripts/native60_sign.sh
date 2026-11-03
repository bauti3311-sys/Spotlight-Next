#!/usr/bin/env bash
# Trusted workflow tooling only; never run Gradle with signing inputs present.
set -euo pipefail
set +x
mode="${1:?preflight or sign required}"
case "$mode" in preflight|sign) ;; *) exit 2 ;; esac
for name in RAYCAST_PREVIEW_KEYSTORE_BASE64 RAYCAST_PREVIEW_STORE_PASSWORD RAYCAST_PREVIEW_KEY_ALIAS RAYCAST_PREVIEW_KEY_PASSWORD; do
  if [[ -z "${!name:-}" ]]; then
    echo 'Historical signing configuration is incomplete; no key created.' >&2
    exit 1
  fi
done
umask 077
private_dir=$(mktemp -d "${RUNNER_TEMP:?}/native60-signing.XXXXXX")
cleanup() {
  rm -rf -- "$private_dir"
  unset RAYCAST_PREVIEW_KEYSTORE_BASE64 RAYCAST_PREVIEW_STORE_PASSWORD RAYCAST_PREVIEW_KEY_ALIAS RAYCAST_PREVIEW_KEY_PASSWORD
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM
if ! printf '%s' "$RAYCAST_PREVIEW_KEYSTORE_BASE64" | base64 --decode > "$private_dir/historical.keystore"; then
  echo 'Historical signing backup is invalid.' >&2
  exit 1
fi
unset RAYCAST_PREVIEW_KEYSTORE_BASE64
chmod 600 "$private_dir/historical.keystore"
tooling_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
# Both stdout/stderr remain private on failure. No alias/password/tool exception in logs.
if ! java "$tooling_dir/Native60SignerPreflight.java" "$private_dir/historical.keystore" > "$private_dir/check.log" 2>&1; then
  echo 'Historical signer verification failed; private diagnostics withheld.' >&2
  exit 1
fi
if [[ "$mode" == preflight ]]; then
  echo 'Historical signing input and certificate verified; no APK built or signed.'
  exit 0
fi
input_dir="${NATIVE60_INPUT_DIR:?}"
output_dir="${NATIVE60_OUTPUT_DIR:?}"
tools="${ANDROID_HOME:?}/build-tools/35.0.0"
# Validate before exposing signing output; artifact/source data are never executed.
python3 "$tooling_dir/native60_verify.py" unsigned "$input_dir" "$tools"
mkdir -p "$output_dir"
apk="$output_dir/Spotlight-Next-Native57-Validation60-e9647c1.apk"
if ! "$tools/apksigner" sign --ks "$private_dir/historical.keystore" \
  --ks-key-alias "$RAYCAST_PREVIEW_KEY_ALIAS" \
  --ks-pass env:RAYCAST_PREVIEW_STORE_PASSWORD --key-pass env:RAYCAST_PREVIEW_KEY_PASSWORD \
  --min-sdk-version 26 --v1-signing-enabled false --v2-signing-enabled true \
  --v3-signing-enabled false --v4-signing-enabled false \
  --out "$apk" "$input_dir/unsigned.apk" > "$private_dir/sign.log" 2>&1; then
  echo 'Historical signing failed; private diagnostics withheld and output withheld.' >&2
  rm -f -- "$apk"
  exit 1
fi
# Remove private files before final verification/upload. EXIT trap remains idempotent.
cleanup
cp "$input_dir/source.zip" "$output_dir/Spotlight-Next-source-e9647c1.zip"
cp "$input_dir/provenance.json" "$output_dir/provenance.json"
python3 "$tooling_dir/native60_verify.py" signed "$output_dir" "$tools"
echo 'One final APK and matching source verified. No install or publication performed.'
