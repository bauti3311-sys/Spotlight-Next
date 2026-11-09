# Evidencia · integración B aprobada

Base677d9d8; fuenteintegrada8f6a5b470310b59c4f5950870ce81327a546e947.
[Informe](../../FOCUS-B-INTEGRATION-20261105.md).

## Capturas

before/:4casos antes de editar producción,8PNG con SpotlightPanel real, presupuesto330dp.
after/:14PNG de producción integrada:8de perfiles360/960dp oscuro/claro y6con fuente200%.
full-fixture/:antes/después700dp del mismo fixturephone-light deFocusIntegratedSceneTest,
con CSVmedidos426/407dp. AntesarchivadoE1–E5 cuyo código visual es equivalente a677d9d8;
no retocar rótulos ni fabricar laAPKinstalada. Los demás8pares son capturas frescas
anteriores a los cambios de fuente, conservadas en before/.

pairs.json y captures-sha256.json fijan9pares y24originales completos. Las láminas
usan recortes iguales de354/470px para quitar sólo canvas vacío; contenido y fuente
no se retocan. build-gallery.py reproduce HTML/PDF/láminas con Pillow/ReportLab
existentes; no agrega una dependenciaAndroid. HTMLautocontenido sin red.
FixtureAPI33 NATIVE/Roborazzi, densidad1, fuentesAndroid1/2. No dispositivo real,
Gboard, OneUI, translucidezOEM, rendimiento ni cuentas/efectos externos.

## Resultado y comandos

test-coverage.json:15métodos distintos, última evidencia por método. directed/ conserva
15casos con un fallo de sincronización de ventana; tablet-settled/ pasa sólo ese caso.
access-round/ conserva los3casos de reversión y capturas200%, uno con falta de foco/
resultados listos antes del atajo; font-settled/ pasa sólo ese método. Los otros14
no se repitieron sin necesidad. La deduplicación final deja15pass,0fail/error/skip.
Antesbaseline no suma casos nuevos. oldFocusConsolidationReviewTest conserva las
aserciones pero accede al nuevo desplegable; su antiguo compositor aislado suprime
la promoción adicional para no duplicar su propio botón depropuesta. Los PNG de
la revisión anterior permanecen inmutables y reproducibles desde su commit.

```sh
# Baseline, ejecutado antes de tocar producción en677d9d8:
FOCUS_B_CAPTURE_PHASE=before bash gradlew :app:testDebugUnitTest \
 --tests '*FocusBIntegrationTest.phoneDark' --tests '*FocusBIntegrationTest.phoneLight' \
 --tests '*FocusBIntegrationTest.tabletDark' --tests '*FocusBIntegrationTest.tabletLight' \
 -I scripts/directed-tests.init.gradle \
 -PspotlightDirectedClasses=dev.bautista.spotlight.ui.FocusBIntegrationTest \
 --no-daemon --max-workers=1 --console=plain

# Ronda dirigida15casos; después sólo métodos fallidos/ampliados, ver logs:
bash gradlew :app:testDebugUnitTest \
 --tests '*FocusBIntegrationTest' --tests '*FocusCapabilityPresentationTest' \
 --tests '*MessageRecipientReviewRegressionTest' \
 --tests '*FocusIntegratedSceneTest.phoneLight' \
 --tests '*FocusConsolidationReviewTest.paired*' \
 -I scripts/directed-tests.init.gradle \
 -PspotlightDirectedClasses=dev.bautista.spotlight.ui.FocusBIntegrationTest,dev.bautista.spotlight.ui.FocusCapabilityPresentationTest,dev.bautista.spotlight.ui.MessageRecipientReviewRegressionTest,dev.bautista.spotlight.ui.FocusIntegratedSceneTest,dev.bautista.spotlight.ui.FocusConsolidationReviewTest \
 --no-daemon --max-workers=1 --console=plain

bash gradlew :app:lintDebug --no-daemon --max-workers=1 --console=plain
python3 docs/verification/focus-b-integration-20261105/build-gallery.py
```

JDKTemurin17.0.21+9, Gradle8.10.2 ySDK35 ya disponibles; proxyJava y truststore del
sistema, sin actualización de bibliotecas. Lint0E26W1Info,2m7s,8tareas ejecutadas/
20reutilizadas. No assemble, Actions ni nuevaAPK. La ejecución de tests registra
requests sólo mediante Robolectric/callbacks, nunca mensajes a personas/cuentas reales.

## Fallos útiles preservados

first-round/ y restore-round/: metadata ausente después de restaurar. La traza de
recovery-red/ prueba que resultados regresaban, pero el estado vacío/parcial había
borrado la identidad del inspector. GuardUIcorregido: no reconciliar selecciónFocus
con lotes provisionales. No modificar SearchEngine ni restaurar autorización.
La aserción del metadata/desplegable sigue presente y verde. Espere explícitamente
resultados/metadata asíncronos en el harness; no aumentar el límite15s.

directed/,access-round/: el harness también debe esperar cierre del popup, resultado
seleccionado disponible y foco real en búsqueda antes de Ctrl+K. Se conservaron sus
fallos y sólo se repitieron casos afectados. No se «soluciona» alterando teclado ni WM.

provenance.json verifica los límites byte a byte desde677d9d8, incluida la corrección
homónimo y su prueba, NativeLaunchSession, motor/ranking, WM, tema/barra/footer,
identidad y tooling. Focus=false permanece; la nueva fuente no está enAPK64.
