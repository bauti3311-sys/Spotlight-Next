#!/usr/bin/env bash
# Local-only targeted validation for the Raycast inspector and unified permissions.
# Never triggers GitHub Actions; never installs to a device or touches historical keys.
set -euo pipefail
cd "$(dirname "$0")/.."

sdk="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
if [[ -z "$sdk" || ! -f "$sdk/platforms/android-35/android.jar" ]]; then
  echo "Android SDK platform 35 is required. Configure ANDROID_SDK_ROOT (or ANDROID_HOME)." >&2
  echo "Nothing was compiled or uploaded." >&2
  exit 2
fi
if [[ ! -x ./gradlew ]]; then
  echo "Gradle wrapper is not executable; run chmod +x gradlew locally." >&2
  exit 2
fi

args=(--no-daemon --max-workers=2 --console=plain)
if [[ "${SPOTLIGHT_OFFLINE:-0}" == "1" ]]; then
  args+=(--offline)
fi

echo "1/3 Compile debug Kotlin for the current branch"
./gradlew :app:compileDebugKotlin "${args[@]}"

echo "2/3 Run focused Robolectric/JUnit regressions"
./gradlew :app:testDebugUnitTest "${args[@]}" \
  --tests 'dev.bautista.spotlight.ui.RaycastSettingsNavigationTest' \
  --tests 'dev.bautista.spotlight.control.MediaTargetTest'

echo "3/3 Lint debug resources and source"
./gradlew :app:lintDebug "${args[@]}"

if [[ "${SPOTLIGHT_BUILD_PREVIEW:-0}" == "1" ]]; then
  echo "Build independent functional-preview APK (NOT an update to installed production)."
  ./gradlew :app:assembleFunctionalPreview "${args[@]}"
  echo "Find the APK under app/build/outputs/apk/functionalPreview/."
fi
echo "Targeted validation completed. Physical One UI touch/keyboard testing still required."
