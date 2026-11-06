# Preview66 — evidencia de preparación, sin APK ni Actions

Base7bc8a26a1a18d461099e2e0fb816afb8fa53465d/PR21. Sólo numeración Raycast Preview
66 y test adicional, ningún cambio de app/src/main. Focusfalse y defaultlegacy,
Geist,140ms/100ms, C3–C8/capacidades/ranking/firmas/permisos preservados.

Dos casos nuevos Preview66CriticalLayoutTest (oscuro/claro) correctos:330dp exterior,
texto200%,266dp interior, metadatos largos, MediaInspector con3controles sintéticos,
PdfPageNavigation3páginas, NotificationPreview real con notificación sintética.
4PNG inicial/desplazado; no son nueva interfaz/A-B de diseño ni captura de APK66.
No efectos externos. El test prueba posiciones/callbacks locales, no audio/PDF real.

Java17/SDK35 existentes. Comando:
`bash gradlew :app:testDebugUnitTest --tests '*Preview66CriticalLayoutTest'
:app:lintDebug -I scripts/directed-tests.init.gradle
-PspotlightDirectedClasses=dev.bautista.spotlight.ui.Preview66CriticalLayoutTest
-Pkotlin.compiler.execution.strategy=in-process --no-daemon --max-workers=1
--console=plain`.
XML/log completo/lint0E26W1Info; no repetición de regresiones históricas.
`validation.json` resume sólo esta nueva ejecución, no suma las35/13/18 anteriores.

Inspección AGP **sin construir APK**:
`bash gradlew :app:inspectPreview66Identity -I
 docs/verification/native66-preparation-20261106/inspect-identity.init.gradle
 --no-daemon --max-workers=1 --console=plain`.
`identity.log/json`: code66/namev66/package/ARM64/noDebuggable/R8/min26target35.
`previous65-identity.json` y signature: apksigner/aapt2 sobre APK65 conservado,
un firmantev2/certificado histórico y SHA256. No claves ni preflight real.
No verifica una firma de66 todavía inexistente.

`build-review.py RUTA_SALIDA`: HTMLoffline/PDF8páginas/ZIPoriginales con3pares
históricos aprobados (footer/PDF/notificación) y4capturas nuevas de auditoría.
`visual-manifest.json` fija base/árbol y hashes. PNG históricos sin modificación,
comparativas explícitamente históricas; no venderlas como captura física de66.
ReportLab/DejaVuSans/Playwright ya instalados sólo en laboratorio. Zoom/Escape sinred.
No métricas físicas OneUI/Gboard/WhatsApp/TalkBack/FPS/RAM. D5 no adoptado.

Las pruebas offline del manifiesto66 se registran después de congelar source_sha;
no invocar policy.main, signing preflight real, bundle APK ni workflow_dispatch.
