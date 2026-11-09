# Focus desde Preview64 · evidencia de revisión

Base administrativa0fa6781; fuente de la APK64:808bda2. Corrección de contacto
en fuente posterior df5bd778019e1dc16a56c41ca8f8ad9ea4525652, no enAPK64.
[Diagnóstico](../../FOCUS-CONSOLIDATION-REVIEW-20261105.md) ·
[ContratosC3/C4](../../C3-C4-CONTRACT-DESIGN-20261105.md).

## Qué mirar

- comparativas.html: autocontenida, con selector de tema/perfil/escena y contexto
  de capturas E1–E5 archivadas. Sin red, cuenta ni datos privados.
- Revision-Focus-Preview64.pdf: cuatro páginas representativas.
- captures/:18PNG nativos originales sin retocar. Las láminas*-AB.png sólo pegan
  originales en columnas, con rótulos externos. No son imágenes generadas por IA.
- pairs.json, metrics-final.json y captures-sha256.json: pares, tamaños medidos y
  hashes. El alto es del fragmento de prueba, no de WindowManager ni del IME físico.
- provenance.json: componentes idénticos a fuente64, configuración y único delta
  productivo de esta revisión. El manifiesto de entrega se compara con0fa6781,
  no con808bda2, que antecede al committooling que fijó el manifiesto64.

A: componentes productivos ApplicationInspector/SpotlightBar/FocusSearchTaskBody/
FocusSearchTaskFooter/GlassContextFooter. B: ensamblaje aislado en tests usando
ResultIcon, tokensFocus, AppCapabilityActions y controlesCompose existentes;
no una implementación productiva ni una nueva ruta de ejecución.
API33 NATIVE/Roborazzi, densidad1/fontScale1,360dp móvil y470dp fragmento tablet,
oscuro/claro, Vivaldi defixture instalado y resolución documentada simulada.
El icono genérico deAndroid NO es la marca Vivaldi. Ningún Intent sale aAndroid.

Las comparaciones de composición/etiquetas no pretenden demostrar integración
completa, deduplicación del catálogo, disponibilidad revocada,330dp,200%,TalkBack,
IME, popupOEM ni rendimiento. La integración posterior deberá conservar heading,
roles/estados accesibles y construir nombre de aplicación desde PackageManager,
no desde la consulta ni un literal defixture. Mantener toda acción secundaria.

## Pruebas realmente ejecutadas

11métodos distintos con XMLverde, deduplicados por clase+método en test-coverage.json:
4pares deperfil,2ActionPanel,1contraste de acentos,1regresión del contacto,
2ReplyConfirmation y1restauraciónC2. No se suman rondas ni evidencia histórica.

```sh
# Ronda verde de cambios visuales acotados + corrección de destinatario:
bash gradlew :app:testDebugUnitTest \
 --tests '*FocusConsolidationReviewTest.paired*' \
 --tests '*MessageRecipientReviewRegressionTest' \
 --tests '*ReplyConfirmationTest.changedConversationOrRecipientRequiresAnotherHumanReview' \
 --tests '*ReplyConfirmationTest.reviewedTargetDispatchesOnceAndCannotBeRepeated' \
 --tests '*C2SharedMessageFlowTest.restoringSharedDraftDoesNotRestoreItsConfirmation' \
 -I scripts/directed-tests.init.gradle \
 -PspotlightDirectedClasses=dev.bautista.spotlight.ui.FocusConsolidationReviewTest,dev.bautista.spotlight.ui.MessageRecipientReviewRegressionTest,dev.bautista.spotlight.control.ReplyConfirmationTest,dev.bautista.spotlight.ui.C2SharedMessageFlowTest \
 --no-daemon --max-workers=1 --console=plain

# Evidencia final del componente popup correcto y contrato de contraste existente:
bash gradlew :app:testDebugUnitTest \
 --tests '*FocusConsolidationReviewTest.actualActionPanel*' \
 --tests '*FocusEditorStateTest.configuredAccentIsContrastAdjustedAcrossAllFocusMaterials' \
 -I scripts/directed-tests.init.gradle \
 -PspotlightDirectedClasses=dev.bautista.spotlight.ui.FocusConsolidationReviewTest,dev.bautista.spotlight.ui.FocusEditorStateTest \
 --no-daemon --max-workers=1 --console=plain

bash gradlew :app:lintDebug --no-daemon --max-workers=1 --console=plain
python3 docs/verification/focus-consolidation-20261105/build-gallery.py
```

JDKTemurin17.0.21+9/Gradle8.10.2/SDK35 preparados en la sesión de64; proxyJava y
truststore del sistema configurados, sin actualizar dependencias. Primera ronda
verde final8casos en36s; evidenciaActionPanel/contraste3casos en14s. Lint2m18s,
**0errores/26advertencias/1información**, sin baseline/supresiones nuevas.
No assemble/APK, Actions ni suite general. No red deproveedores ni envío real.

## Fallos y decisiones preservadas

- initial/compile.log: fallos del nuevo harness (bounds, argumento del modelo e
  icono con nombre incorrecto); corregidos antes de ejecutar los casos.
- selector-round/: capturas producidas pero aserción «Vivaldi» ambigua en cuatro
  perfiles; se limitó al ancestro de revisión, sin reducir su exigencia.
- additive-trial/: primera B,438dp frente382dp. Se descartó por crecer56dp.
  La Bfinalpliega administración y mide360dp; no se presenta el intento descartado
  como candidata aprobada.
- contact-red/: fallo real de producto reproducido sobre MessageEditor actual,
  confirmación previa visible tras cambiar al segundo homónimo. Sin solicitudes.
- validated/:8casos verdes y lint final; validated-actions/:3casos verdes finales.
  La regresión del destinatario tiene las mismas aserciones antes/después.

Las capturas E1–E5 de contexto son originales archivados y visualmente equivalentes
a64; algunos rótulos deversión dicen63. No son capturas físicas. La fuente nueva
no reemplaza APK64 instalada ni autoriza otra APK. C0/C1/C2 físicos,Spotify real,
entrega de mensajes/archivos y aprobación visual deB siguen pendientes.
