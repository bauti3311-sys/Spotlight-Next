# Evidencia original — Visual First aprobado

Base7332b2e; D1real integrado, D2–D4A conservados; D5sólo test.
Informe: ../../VISUAL-POLISH-D1-V06-V08-V10-20261106.md.

`manifest.json`:75pares y25fotogramas con SHA256; árboles de producción y archivos
candidatos. Los72antes de escenas referencian imágenes inmutables de
../visual-first-20261106/images. `pdf-before` conserva3antes rojos. `after`75PNG
reales finales; `motion`25PNG de experimento. No retocar imágenes.

Validación dirigida con Java17 y SDK35 ya instalados, RobolectricAPI33/native:
`bash gradlew :app:testDebugUnitTest --tests 'FILTRO' -I scripts/directed-tests.init.gradle
-PspotlightDirectedClasses=CLASES --no-daemon --max-workers=1 --console=plain`.
Para galería final: `SPOTLIGHT_VISUAL_PHASE=after`, clases VisualFirstGalleryTest,
VisualPolishPdfNavigationTest, VisualPolishTokensTest, VisualFirstIconReviewTest.
Se ejecutaron17casos finales. `final` conserva5movimiento+3tokens+3perfiles y lint;
`d1-pdf-green` conserva regresiones homónimos6/Spotify3/FocusB1 y primera integración;
`motion-initial` conserva ventana3/iconos2 correctos y fallo histórico del fixtureD5.
`test-summary.json` diferencia35casos vigentes verdes y1fixture histórico sustituido.
`pdf-before` conserva3rojos iniciales; no falsear el historial ni sumar repeticiones.
Lint final0E26W1Info. No pruebas físicas, FPS/RAM, APK ni Actions.

Generar HTML/PDF/ZIP sin editar PNG ni acceder a red:
`python3 docs/verification/visual-polish-20261106/build-gallery.py RUTA_SALIDA`.
Usa ReportLab/DejaVuSans ya existentes en laboratorio; ninguna dependencia de app.
HTML filtros por perfil, zoom; PDF12páginas. D5A es helper160ms sin llamadas
productivas, no la ventana real140ms/100ms. IME es viewport reservado, no Gboard.
