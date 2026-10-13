#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
cd "$root"
diagnose_failure() {
  result=$?
  if [[ $result != 0 && "${CI:-}" == true ]]; then
    # CI emulators contain only the synthetic fixtures from these flows.
    adb shell uiautomator dump /data/local/tmp/spotlight-ui.xml || true
    adb shell cat /data/local/tmp/spotlight-ui.xml || true
    adb logcat -d -s SpotlightOverlay AndroidRuntime WindowManager || true
  fi
  exit "$result"
}
trap diagnose_failure EXIT
adb get-state >/dev/null || { echo 'Se requiere emulador o dispositivo conectado.' >&2; exit 2; }
adb shell appops set dev.bautista.spotlight.next SYSTEM_ALERT_WINDOW allow
adb shell am start -n dev.bautista.spotlight.next/dev.bautista.spotlight.InvokeActivity --es query '2+2' --es invocation keyboard
bash .codex/scripts/maestro.sh test .maestro/flows/overlay-focus.yaml
# Maestro 2.11.0 AndroidDriver maps TAB to 62 (SPACE), not 61 (TAB).
# Inject the real Android key; keep the same visibility assertions in Maestro.
adb shell input keyevent 61
bash .codex/scripts/maestro.sh test .maestro/flows/overlay-actions.yaml
adb shell input keyevent 61
bash .codex/scripts/maestro.sh test .maestro/flows/overlay-keyboard.yaml
adb shell am start -n dev.bautista.spotlight.next/dev.bautista.spotlight.InvokeActivity --es query 'guardados'
bash .codex/scripts/maestro.sh test .maestro/flows/saved-menu.yaml
printf 'Maestro ejecutado. PDF nativo: ejecutar además :app:connectedDebugAndroidTest.\n'
