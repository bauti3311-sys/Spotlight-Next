# Reproducibilidad — catálogo de elección

348 PNG originales en `screenshots/`: 312 estilos/perfiles/escenas +36 funciones.
`catalogo.json` conserva fichas/códigos. Fuente del prototipo: ChoiceCatalogueGalleryTest,
FunctionChoiceGalleryTest y app/src/test/resources/choice-catalogue/functions.json.
No es UI productiva ni ejecución real de las capacidades propuestas.

`rendering/`: XML/logs de7 escenarios correctos. `rendering/structural/`: rerender
focalizado de E05/E07/E10 en seis perfiles, sin repetir el escenario36funciones.
`browser-check.json`: controles de la galería comprobados en Chromium local.
`preservation.json`: delta productivo cero respecto dd5e595.

Generar HTML desde estos PNG, sin Gradle ni APK:

    SPOTLIGHT_CATALOGUE_OUT=/ruta/salida python3 docs/verification/choice-catalogue-20261108/build_gallery.py

El PDF usa reportlab/Pillow del entorno, no dependencias Android:

    SPOTLIGHT_CATALOGUE_OUT=/ruta/salida python3 docs/verification/choice-catalogue-20261108/build_pdf.py

Capturar de nuevo sólo si se cambia el prototipo; no actualizar baselines productivas:

    bash gradlew :app:testDebugUnitTest --tests '*ChoiceCatalogueGalleryTest' --tests '*FunctionChoiceGalleryTest' -I scripts/directed-tests.init.gradle -I docs/verification/c2-app-open-20261107/no-apk.init.gradle -PspotlightDirectedClasses=dev.bautista.spotlight.ui.ChoiceCatalogueGalleryTest,dev.bautista.spotlight.ui.FunctionChoiceGalleryTest -Pkotlin.compiler.execution.strategy=in-process --offline --no-daemon --max-workers=1 --console=plain

SPOTLIGHT_CATALOGUE_STYLES=05,07,10 limita los estilos si sólo cambian esas direcciones.
Todo se ejecutó con Java17, SDKlocal y Roborazzi/Robolectric existentes.
