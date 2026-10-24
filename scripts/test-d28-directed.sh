#!/usr/bin/env bash
# Local/provisioned host only. No downloads, APK or GitHub Actions dispatch.
set -euo pipefail
cd "$(dirname "$0")/.."
group="${1:-ui}"
ui=(dev.bautista.spotlight.ui.ContextSplitHeightTest dev.bautista.spotlight.ui.ProductEntryPointsTest dev.bautista.spotlight.ui.MediaInspectorFlowTest dev.bautista.spotlight.ui.SpotlightPanelLayoutTest)
d28=(dev.bautista.spotlight.automation.CommandTemplatesTest dev.bautista.spotlight.ui.CommandTemplateFlowTest dev.bautista.spotlight.automation.NativeCommandsTest dev.bautista.spotlight.ui.NativeCommandFlowTest)
capabilities=(dev.bautista.spotlight.control.AppCapabilitiesTest dev.bautista.spotlight.control.IntegrationRegistryTest)
spotify=(dev.bautista.spotlight.integrations.spotify.SpotifyPkceTest dev.bautista.spotlight.integrations.spotify.SpotifySearchClientTest dev.bautista.spotlight.integrations.spotify.SpotifySessionTest dev.bautista.spotlight.integrations.spotify.SpotifyLoopbackTest dev.bautista.spotlight.integrations.spotify.SpotifyPlaybackClientTest)
cast=(dev.bautista.spotlight.integrations.cast.LocalMediaRequestPolicyTest)
imports=(dev.bautista.spotlight.sharing.BoundedSharedCopyTest dev.bautista.spotlight.sharing.SharedInputActivityTest dev.bautista.spotlight.ui.SharedImportPanelTest)
metrics=(dev.bautista.spotlight.overlay.DiagnosticsMeasurementTest)
sources=(dev.bautista.spotlight.next.NativeProviderTest dev.bautista.spotlight.next.SourceFreshnessTest dev.bautista.spotlight.ui.SportsAgendaMergeTest)
case "$group" in
 ui) tests=("${ui[@]}");; d28) tests=("${d28[@]}");; capabilities) tests=("${capabilities[@]}");;
 spotify) tests=("${spotify[@]}");; cast) tests=("${cast[@]}");; imports) tests=("${imports[@]}");;
 metrics) tests=("${metrics[@]}");; sources) tests=("${sources[@]}");;
 all) tests=("${ui[@]}" "${d28[@]}" "${capabilities[@]}" "${spotify[@]}" "${cast[@]}" "${imports[@]}" "${metrics[@]}" "${sources[@]}");;
 *) echo 'Grupo válido: ui (primero), d28, capabilities, spotify, cast, imports, metrics, sources, all.' >&2;exit 2;;
esac
if ! command -v java >/dev/null; then echo 'Falta Java 17. No se iniciaron pruebas.' >&2;exit 2;fi
if [[ ! -d "${ANDROID_HOME:-}/platforms/android-35" && ! -d "${ANDROID_SDK_ROOT:-}/platforms/android-35" && ! -f local.properties ]]; then
 echo 'Falta SDK Android 35 configurado. No se iniciaron pruebas.' >&2;exit 2
fi
if [[ -n "${SPOTLIGHT_GRADLE_BIN:-}" ]]; then runner=("$SPOTLIGHT_GRADLE_BIN");else
 shopt -s nullglob
 distributions=("${GRADLE_USER_HOME:-$HOME/.gradle}"/wrapper/dists/gradle-8.10.2-bin/*/gradle-8.10.2/bin/gradle)
 if (( ${#distributions[@]} == 0 ));then echo 'Falta Gradle 8.10.2 local/cacheado. No se descargará.' >&2;exit 2;fi
 runner=(bash gradlew)
fi
filters=();for test_class in "${tests[@]}";do filters+=(--tests "$test_class");done
printf -v selected_classes '%s,' "${tests[@]}"
selected_classes="${selected_classes%,}"
"${runner[@]}" :app:testDebugUnitTest -I scripts/directed-tests.init.gradle "-PspotlightDirectedClasses=$selected_classes" --offline --no-daemon --max-workers=1 --console=plain "${filters[@]}"
