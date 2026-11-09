# Reproducir Mesa y cinco alternativas

Galeria-Spotlight.html es autocontenida.60PNG en originals/:50nuevos y10Mesa/P05
reutilizados sin cambios comoM00. reference-reuse.json guarda rutas y hashes.
Los5estados y2perfiles usan el mismo fixture. La vista contextual es texto de
laboratorio, no extracción ni citas reales.

Banco: app/src/test/java/dev/bautista/spotlight/ui/MesaExplorationsGalleryTest.kt.
rendering/:3escenarios correctos; rendering/actions/:rerender sóloacciones+
navegaciónlocal tras compactar presentación. No pruebas de producto/WM físicas.

Generar la galería desde PNG versionados, sin APK:

    SPOTLIGHT_MESA_OUT=/ruta/salida python3 docs/verification/mesa-explorations-20261108/catalogue.py
    SPOTLIGHT_MESA_OUT=/ruta/salida python3 docs/verification/mesa-explorations-20261108/build_html.py
    SPOTLIGHT_MESA_OUT=/ruta/salida python3 docs/verification/mesa-explorations-20261108/pdf.py

browser.py valida el HTML exacto vía set_content, con imágenes offline y sin
servidor. Pillow/reportlab/Playwright son herramientas existentes del entorno,
no nuevas dependencias Android.

Para cambios al laboratorio, sólo este banco con Java17/SDK local:

    bash gradlew :app:testDebugUnitTest --tests '*MesaExplorationsGalleryTest' -I scripts/directed-tests.init.gradle -I docs/verification/c2-app-open-20261107/no-apk.init.gradle -PspotlightDirectedClasses=dev.bautista.spotlight.ui.MesaExplorationsGalleryTest -Pkotlin.compiler.execution.strategy=in-process --offline --no-daemon --max-workers=1 --console=plain

SPOTLIGHT_MESA_CONCEPTS=3 limita aPuente; SPOTLIGHT_MESA_STATES=acciones limita
capturas al menú. No repetir vistas válidas por rutina. No habilitar nada productivo.
