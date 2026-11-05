# Gate final C2 — fuente84ab6d7

Inicio04/11, resultados05/11/2026 UTC. Fuente exacta
84ab6d7aac3a27db30b718f2921dd49a4f3a1b82. No cambios de producción ni tests.
JDK17 completo; Gradle8.10.2; SDK35/build-tools35.0.0; Compose/Robolectric API33
NATIVE. Los comandos no ejecutan assemble/package APK ni Actions.

`restoration.log` conserva el fallo inicial de infraestructura (runtime Java21
sin compilador), antes de ejecutar casos. `restoration-jdk17.log` y
`restoration-green/` contienen el resultado correcto con JDK completo. El rojo
funcional anterior sigue en ../c2-files-20261104/selection-restore-red.

Configurar JAVA_HOME al JDK17, ANDROID_HOME al SDK y, donde corresponda, proxy
HTTP/HTTPS y trustStore del entorno para Gradle y el JVM de pruebas. No se cambia
el repositorio para configurar red o herramientas. Comandos desde la raíz:

```sh
bash gradlew :app:testRaycastPreviewUnitTest \
  --tests 'dev.bautista.spotlight.ui.C2FileShareFlowTest.restoringAnEditedIncomingSelectionDoesNotReplaceItWithTheOriginalContext' \
  --tests 'dev.bautista.spotlight.ui.C2FileShareFlowTest.clearingTheSourceRemovesAnUntouchedIncomingSelection' \
  --no-daemon --max-workers=2 --console=plain \
  -I scripts/directed-tests.init.gradle \
  -PspotlightDirectedClasses=dev.bautista.spotlight.ui.C2FileShareFlowTest

bash gradlew :app:testRaycastPreviewUnitTest \
  --tests 'dev.bautista.spotlight.search.C2FileContextTest.shareFilePhraseSelectsLocalFilesInsteadOfLaunchingOrAskingAi' \
  --tests 'dev.bautista.spotlight.search.C2FileContextTest.exactApplicationNamedLikeAFileQueryStillWins' \
  --tests 'dev.bautista.spotlight.search.CompactResultsTest.explicitFileSelectionActionRemainsVisibleAndNeverOutranksAnExactApp' \
  --tests 'dev.bautista.spotlight.ui.C2FileShareFlowTest.queryReviewsOriginalFileAndRequestsOnlyOneSystemChooser' \
  --tests 'dev.bautista.spotlight.ui.C2FileShareFlowTest.restoringMetadataDoesNotRestoreConfirmationOrAuthorization' \
  --tests 'dev.bautista.spotlight.ui.C2FileShareFlowTest.changedLiveSelectionInvalidatesThePreparedProposal' \
  :app:lintRaycastPreview --no-daemon --max-workers=2 --console=plain \
  -I scripts/directed-tests.init.gradle \
  -PspotlightDirectedClasses=dev.bautista.spotlight.ui.C2FileShareFlowTest,dev.bautista.spotlight.search.C2FileContextTest,dev.bautista.spotlight.search.CompactResultsTest
```

`regression-lint.log` y `regression-green/` contienen la segunda ronda.8 identidades
de prueba distintas entre ambas, sin fallos/errores/skips. Compilar todos los
fixtures no equivale a ejecutarlos: sólo se ejecutaron los métodos indicados.
Las capturas generadas por el caso existente no se usan para reclamar una nueva
comparativa visual: no hubo cambios visuales en este cierre.

Lint:0 errores/27 advertencias/1 información, BUILD SUCCESSFUL. XML y TXT íntegros
guardados. Comparado con c2-expansion-20261104/final por id+mensaje+archivo relativo,
sólo se añade ComposableNaming en el helper de pruebas C2FileShareFlowTest.panel;
no se retiran avisos ni se agregan supresiones. Las26 advertencias/1 información
anteriores no cambian. No se confunde warning de fixture con error de producción.

`sha256.json` permite comprobar los artefactos guardados. No se suman casos a los
112 históricos ni se repiten suites enteras. No hay cuentas/receptores comerciales
ni tablet conectada. startActivity simulado no es entrega real ni grant OEM probado.

[Informe](../../C2-TECHNICAL-CLOSURE-20261105.md) ·
[Aceptación física pendiente](../../C2-PHYSICAL-ACCEPTANCE-20261105.md).
