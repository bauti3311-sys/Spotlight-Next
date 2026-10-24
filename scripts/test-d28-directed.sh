#!/usr/bin/env bash
# Local/codespace/authorized host only. Does not dispatch GitHub Actions or install tools.
set -euo pipefail
cd "$(dirname "$0")/.."
if ! command -v java >/dev/null; then
  echo 'Falta Java 17. No se instalaron herramientas ni se iniciaron pruebas.' >&2
  exit 2
fi
if [[ ! -d "${ANDROID_HOME:-}/platforms/android-35" && ! -d "${ANDROID_SDK_ROOT:-}/platforms/android-35" && ! -f local.properties ]]; then
  echo 'Falta SDK Android 35 configurado. No se iniciaron pruebas.' >&2
  exit 2
fi
bash gradlew :app:testDebugUnitTest --offline --no-daemon --max-workers=1 --console=plain \
  --tests 'dev.bautista.spotlight.ui.ContextSplitHeightTest' \
  --tests 'dev.bautista.spotlight.ui.ProductEntryPointsTest' \
  --tests 'dev.bautista.spotlight.ui.MediaInspectorFlowTest' \
  --tests 'dev.bautista.spotlight.ui.SpotlightPanelLayoutTest' \
  --tests 'dev.bautista.spotlight.automation.CommandTemplatesTest' \
  --tests 'dev.bautista.spotlight.ui.CommandTemplateFlowTest' \
  --tests 'dev.bautista.spotlight.automation.NativeCommandsTest' \
  --tests 'dev.bautista.spotlight.ui.NativeCommandFlowTest'
