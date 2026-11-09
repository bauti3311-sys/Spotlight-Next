# Footer — V05/V06, texto ampliado

Base remota d570a8689a63caeb4e29eb4273dba34e2570317b. Producción cambia únicamente
la decisión wide/compact del footer según escala Android×preferencia existente.
No cambiar los controles, callbacks, atajos, fuente, colores o ancho del overlay.

`before`:5PNG originales + XML/log rojo;4correctos/1fallido. A700dp/200%, Acciones
ancho0, footer308dp. `after`:5PNG equivalentes con corrección; footer52dp en700dp.
Normal tablet1000/100 y teléfono330/200 son idénticos byte a byte. `extra-validation`:
casos posteriores adicionales con opciones y preferencia130%; sin pareja A/B.
Fixture del componente real/API33/nativegraphics/densidad1, no APK ni OneUI.
`manifest.json` fija fuente y hashes; no manipular ni sobrescribir PNG originales.

Java17/SDK35 existentes, comando principal:
`SPOTLIGHT_FOOTER_PHASE=after bash gradlew :app:testDebugUnitTest
--tests '*VisualFooterBudgetTest' --tests '*AdaptiveResultsGeometryTest'
--tests '*FocusBIntegrationTest.phoneDarkText200' :app:lintDebug
-I scripts/directed-tests.init.gradle
-PspotlightDirectedClasses=dev.bautista.spotlight.ui.VisualFooterBudgetTest,dev.bautista.spotlight.ui.AdaptiveResultsGeometryTest,dev.bautista.spotlight.ui.FocusBIntegrationTest
--no-daemon --max-workers=1 --console=plain`.

Se ejecutaron5footer+5geometría+1FocusB. Dos pruebas footer adicionales se
incorporaron después de ese pase y se ejecutan solas con sus nombres, mismo filtro
Class/phase y sin repetir lint ni geometría. `test-summary.json` conserva13casos
vigentes únicos, no sumar el rojo inicial ni repeticiones. Lint0E26W1Info.

HTMLoffline/PDF6páginas/ZIP con originales y manifest:
`python3 docs/verification/visual-footer-20261106/build-gallery.py RUTA_SALIDA`.
ReportLab/DejaVuSans y Playwright ya instalados en laboratorio, ninguna dependencia
de la app. HTML zoom/cierre Escape comprobados sin solicitudes externas. Sin FPS/RAM,
TalkBack, OneUI/Gboard/IME físico nuevos. D5test120ms no se adopta en producción.
