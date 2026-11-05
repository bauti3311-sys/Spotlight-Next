# Preview64 · evidencia de preparación · 2026-11-05

Fuente congelada: `808bda2c25887d14b42665dea4c254181d72b837`.
JDK17.0.21+9 Temurin (checksum del proveedor verificado), SDK35/BuildTools35.0.0,
Gradle8.10.2 (checksum versionado en wrapper). Terminal y runtime running/connected.
Se usó el proxy heredado, con proxy Java explícito y truststore del sistema.
No APK, firma, keystore nuevo, preflight real, Actions ni dispositivo físico.

## Resultados

- directed-results.json: seis métodos distintos con evidencia correcta, sin sumar
  repeticiones. initial conserva5pass/1fail. validated contiene el overlay correcto.
- initial: fallo inicial del fixture sin permiso overlay. permission-fixture y
  focus-fixture conservan los timeouts posteriores del servicio en el harness
  Compose. La corrección final despacha el looper Android y modela permiso/foco;
  no toca producto, aumenta timeout ni reduce aserciones. Overlay observado
  true→false→true, sin segunda invocación onStartCommand.
- delivery-policy-tests.log:17 checks offline correctos para el manifiesto64 final.
- release-identity-tests.log:2 checks correctos de identidad principal intacta.
- source-audit.json:44 casos XML históricos E1–E5 inspeccionados y cinco hashes
  verificados. No son44 tests nuevos; lint0E/26W/1Info también es histórico.
- runtime-bootstrap-failure.log conserva la falta inicial de javac, subsanada
  instalando JDK17 aislado. Descarga Gradle inicial sin proxy Java falló antes;
  se resolvió usando el proxy existente, sin cambiar la red.

## Comandos Android realmente ejecutados

Primera configuración con runtimeJava21: testDebugUnitTest no pudo calcular el
compilador por falta de javac; no ejecutó casos. Con JDK17:

```sh
bash gradlew :app:testDebugUnitTest \
  --tests '*FocusFoundationTest' \
  --tests '*FocusSettingsSceneTest.focusSelectorPersistsAcrossRecreationAndRestoresLegacy' \
  --tests '*FocusSettingsSceneTest.openOverlayObservesFocusPreferenceWithoutReopening' \
  --tests '*FocusSettingsSceneTest.opacityIsImmediatePersistentAndSurvivesActivityRecreation' \
  -I scripts/directed-tests.init.gradle \
  -PspotlightDirectedClasses=dev.bautista.spotlight.ui.FocusFoundationTest,dev.bautista.spotlight.ui.FocusSettingsSceneTest \
  --no-daemon --max-workers=1 --console=plain
```

Tras cada corrección del fixture se seleccionó sólo
`*FocusSettingsSceneTest.openOverlayObservesFocusPreferenceWithoutReopening`
y una única clase dirigida. Última ejecución correcta17s;26 tareas reutilizadas,
2ejecutadas. No suite general ni compilacionesAPK. La prueba valida estado
observable del servicio y su ventana abierta en Robolectric; no certifica foco,
recomposición física, geometría, teclado ni efectos Android en OneUI.

La instalación y aceptación visual/física en Tab A9+/moto g86 siguen pendientes.
