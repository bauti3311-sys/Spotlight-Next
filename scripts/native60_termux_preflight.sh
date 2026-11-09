#!/usr/bin/env bash
# Public tooling: private material never leaves Termux. No network/install/sign/build.
set -euo pipefail
set +x
archive="${1:-$HOME/storage/downloads/Spotlight-AndroidStudio-0.9.1.zip}"
if [[ ! -r "$archive" ]]; then
  echo 'No se encontró el ZIP histórico accesible. No se modificó ningún archivo.' >&2
  exit 1
fi
for tool in python java; do
  command -v "$tool" >/dev/null || { echo 'Falta Python o un JDK17+ en Termux; no se instaló nada automáticamente.' >&2; exit 1; }
done
private_home=$(python -c 'from pathlib import Path; print(Path.home().resolve())')
case "$private_home" in
  /data/data/com.termux/files/home|/data/user/[0-9]*/com.termux/files/home) ;;
  *) echo 'Esta comprobación requiere el HOME privado de Termux; no se extrae en almacenamiento compartido.' >&2; exit 1 ;;
esac
umask 077
private_dir=$(mktemp -d "$private_home/.native60-private.XXXXXX")
cleanup() { rm -rf -- "$private_dir"; }
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM
python - "$archive" "$private_dir/historical.keystore" <<'PY'
import os, stat, sys, zipfile
try:
    with zipfile.ZipFile(sys.argv[1]) as archive:
        entries=[i for i in archive.infolist() if i.filename=='SpotlightNext/signing/spotlight-debug.keystore']
        if len(entries)!=1: raise ValueError()
        entry=entries[0]
        if stat.S_ISLNK(entry.external_attr>>16) or not 0<entry.file_size<=1048576: raise ValueError()
        with archive.open(entry) as src, open(sys.argv[2],'xb') as dst:
            os.chmod(sys.argv[2],0o600)
            data=src.read(1048577)
            if len(data)!=entry.file_size: raise ValueError()
            dst.write(data)
except Exception:
    sys.exit('No se pudo recuperar la entrada histórica exacta; no se muestran datos privados.')
PY
tooling_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
java "$tooling_dir/Native60SignerPreflight.java" --interactive "$private_dir/historical.keystore"
# EXIT removes the private copy; original ZIP remains unchanged.
