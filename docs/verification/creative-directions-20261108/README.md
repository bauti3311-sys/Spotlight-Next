# Laboratorio creativo — reproducibilidad

Galeria-Spotlight.html es autocontenida. 60 PNG originales en originals/: perfiles
tablet1200×800dp y phone412×860dp; P01–P06; inicio/resultados/seleccion/acciones/contexto.
Consulta/IDs/orden estables. Datos ficticios, no capturas físicas ni ejecución externa.

Banco: app/src/test/java/dev/bautista/spotlight/ui/CreativeDirectionsGalleryTest.kt.
rendering/ conserva XML/log de3 escenarios; mobile-revision/ conserva el rerender
sólo Mesa teléfono y navegación local. Las pruebas del producto no se repitieron.
Comparaciones JPEG/PDF son hojas de revisión a partir de los PNG, no screenshots
nuevos de la app. discarded/ conserva las composiciones rechazadas, fuente textual
y captura anterior de Mesa móvil. No son opciones activas adicionales.

Reconstruir galería y PDF desde PNG versionados, sin Gradle/APK:

    SPOTLIGHT_CONCEPTS_OUT=/ruta/salida python3 docs/verification/creative-directions-20261108/catalogue.py
    SPOTLIGHT_CONCEPTS_OUT=/ruta/salida python3 docs/verification/creative-directions-20261108/build_html.py
    SPOTLIGHT_CONCEPTS_OUT=/ruta/salida python3 docs/verification/creative-directions-20261108/pdf.py

Pillow/reportlab/Playwright son herramientas ya presentes en el entorno; no deps
Android nuevas. browser.py prueba el HTML exacto vía set_content y no requiere red.

Si cambia el prototipo, capturar únicamente este banco con Java17/SDK existentes:

    bash gradlew :app:testDebugUnitTest --tests '*CreativeDirectionsGalleryTest' -I scripts/directed-tests.init.gradle -I docs/verification/c2-app-open-20261107/no-apk.init.gradle -PspotlightDirectedClasses=dev.bautista.spotlight.ui.CreativeDirectionsGalleryTest -Pkotlin.compiler.execution.strategy=in-process --offline --no-daemon --max-workers=1 --console=plain

SPOTLIGHT_CREATIVE_CONCEPTS=5 limita la matriz a Mesa. Los métodos tabletFiveStates/
phoneFiveStates permiten dirigir el perfil; no repetir capturas válidas por rutina.
