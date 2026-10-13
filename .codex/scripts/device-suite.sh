#!/usr/bin/env bash
set -uo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
cd "$root"
maestro_result=0
native_result=0
capture_result=0
driver_result=0
bash .codex/scripts/device-checks.sh || maestro_result=$?
# Retaining the APK avoids installation during overlay checks. Stop its test
# processes afterward so Android 11 releases the exclusive UiAutomation client.
adb shell am force-stop dev.mobile.maestro || driver_result=$?
adb shell am force-stop dev.mobile.maestro.test || driver_result=$?
bash gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=dev.bautista.spotlight.OverlayGeometryDeviceTest --no-daemon --max-workers=2 --console=plain || native_result=$?
mkdir -p .maestro/output
adb pull /sdcard/Download/spotlight-geometry .maestro/output/f13-device || capture_result=$?
if [[ $maestro_result != 0 || $native_result != 0 || $capture_result != 0 || $driver_result != 0 ]]; then
  printf 'Device suite failed: Maestro=%s native=%s capture=%s driver=%s\n' "$maestro_result" "$native_result" "$capture_result" "$driver_result" >&2
  exit 1
fi
