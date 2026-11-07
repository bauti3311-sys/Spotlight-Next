# Comparativas A/B de Focus — continuación07/11

Informe ../../FOCUS-DIRECTIONS-20261107.md. Basecfb8fa8,6casos nuevos verdes,
78PNG/26gruposC-A-B. Componentes productivos en fixture, NO APK/SpotlightPanel completo.
Geist, D1, jerarquía/acciones y motores actuales; fixtures PM/contexto/acción reciente.
Producción, versión, workflow y manifesto de entrega sin cambios.

Java17/SDK35 existentes:
`bash gradlew :app:testDebugUnitTest --tests '*FocusDirectionsGalleryTest'
-I scripts/directed-tests.init.gradle
-PspotlightDirectedClasses=dev.bautista.spotlight.ui.FocusDirectionsGalleryTest
-Pkotlin.compiler.execution.strategy=in-process --no-daemon --max-workers=1 --console=plain`.
No repetir lint/assemble/Actions. `pilot` conserva fallos de fixture, `final` pase6verde.
`manifest.json` fija base ySHA256 de cadaPNG. No retocar imágenes ni fingir hardware.
`build-gallery.py SALIDA` generaHTML/PDF8páginas/ZIPoriginales; ReportLab/DejaVuSans
existentes sólo en laboratorio. Filtros/zoom/Escape comprobados conPlaywright existente.
A/B pendientes de decisión del propietario. RecomendaciónB con cuerpoA enAjustes.
