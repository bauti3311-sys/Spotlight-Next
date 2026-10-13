#!/usr/bin/env bash
set -euo pipefail
profile=${1:-android}
case "$profile" in
  android)
    java -version
    python3 --version
    sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
    [[ -n "$sdk" && -d "$sdk/platforms/android-35" && -x "$sdk/build-tools/35.0.0/apksigner" ]] || { echo 'Falta SDK 35 / Build Tools 35.0.0; conservar proxy y CA al instalar.' >&2; exit 2; }
    printf 'SDK 35 y apksigner disponibles. No se leen secretos.\n'
    ;;
  visual)
    node --version
    npm --version
    if command -v chromium >/dev/null; then chromium --version; else echo 'Navegador: instalar Chromium únicamente en el perfil visual/CI.'; fi
    ;;
  device)
    adb devices
    adb get-state >/dev/null || { echo 'No hay dispositivo. Esto no valida Maestro ni instalación física.' >&2; exit 2; }
    ;;
  *) echo 'Uso: doctor.sh android|visual|device' >&2; exit 2 ;;
esac
