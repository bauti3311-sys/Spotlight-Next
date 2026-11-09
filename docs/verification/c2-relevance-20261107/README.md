# C2 — corpus local y evidencia

Fuente f9fcd5d8745ed45127f44c7616da2ec9a82ef2c3, base71500e3.

- before/:13casos nuevos contra producción sin modificar,4fallos reales/9correctos.
- after/:28casos distintos aprobados,0omisiones;13nuevos+15regresiones seleccionadas; lint0E/26W/1Info.
- corpus-orders.txt:orden real emitido por SearchEngine/ResultRanker/CompactResults en fixtures, incluidos resultados iniciales/expandidos y antes/después.
- initial-fixture/:primera iteración con expectativa inválida documento/contacto y cast que ocultaba el diagnóstico de alias; se corrigen sólo fixtures antes de registrar baseline definitiva. No confundir con defectos productivos nuevos ni ocultar fallos.
- preservation.json:únicos4archivos productivos modificados y resto main idéntico, UI/ejecutores/C5/Preview68 protegidos.
- directed-invocation.json:clases/métodos exactos ejecutados. Usar --tests por cada filtro y la lista spotlightDirectedClasses; SDK/Java17 configurados. no-apk.init.gradle impide tareas de APK/firma/install/connected. No ejecutar instrumentación.

Los datos de aplicaciones/contactos/documentos son ficticios. Apps/engine usan Robolectric/PackageManager real bajo shadow; archivos/contactos de la comparación de orden son filas sintéticas autorizadas, no SAF u OCR real. No hay modelo, HTTP, integración productiva nueva, APK o Actions. No se mide rendimiento en teléfono ni se afirma prueba física.
