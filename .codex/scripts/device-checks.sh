#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
cd "$root"
adb get-state >/dev/null || { echo 'Se requiere emulador o dispositivo conectado.' >&2; exit 2; }
adb shell appops set dev.bautista.spotlight.next SYSTEM_ALERT_WINDOW allow
adb shell am start -n dev.bautista.spotlight.next/dev.bautista.spotlight.InvokeActivity --es query '2+2' --es invocation keyboard
bash .codex/scripts/maestro.sh test .maestro/flows/overlay-keyboard.yaml
adb shell am start -n dev.bautista.spotlight.next/dev.bautista.spotlight.InvokeActivity --es query 'guardados'
bash .codex/scripts/maestro.sh test .maestro/flows/saved-menu.yaml
printf 'Maestro ejecutado. PDF nativo: ejecutar además :app:connectedDebugAndroidTest.\n'
