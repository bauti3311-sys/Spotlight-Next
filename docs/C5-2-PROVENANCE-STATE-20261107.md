# C5.2 — procedencia PDF real y estado efímero de citas

Fuente técnica publicada: `84c0e1a668c16becc0cc5098d20da22dc4644716`.

Continuación de `b80d9762eefef2e06bff32a7b6d97f51c2d7f94c` y del contrato `844e23d25c3c8ba8c41c2c5ed33f6ddfc11fadde`. PR #21 abierto/en borrador, remoto y ausencia de reserva ajena comprobados antes de editar. **Checkpoint parcial: C5 no está completo.** C5.3 semántica/síntesis no se integra aquí.

## PDF auténtico: qué se ejecutó

`CitationRealPdfTest` genera un PDF real de tres páginas con la dependencia PDFBox existente, sin datos personales ni metadatos de extracción simulados. Ejecuta `DocumentText.extractDetailed → PdfTextSource → PDFTextStripper → PdfTextPipeline → FileIndex.cacheExtraction → DocumentCitationSession`. No se modifica el extractor.

| Consulta | Página obtenida | Offsets UTF-16 del texto extraído | Contenido comprobado |
|---|---:|---|---|
| presupuesto café | 1 | 23–143 | 2400 €, raya, acentos y cláusula común |
| reunión martes | 2 | 168–286 | José y María, sección 2 y cláusula común |
| conclusión técnica | 3 | 311–441 | piñata, acción, ¿cuándo? y © |
| cláusula común | 1 | 23–143 | Frase repetida también en páginas 2 y 3; atribución al primer fragmento real encontrado |

El contrato conserva una cita por documento, no todas las coincidencias por página. Repetir una frase en páginas distintas no inventa otra referencia: la página elegida tiene un fragmento real que la contiene. Una cadena repetida y ambigua dentro de la misma página (extracto largo con más de una posición posible) se rechaza. Se prueba además rechazo ante metadatos de extracción incompletos. El test compara cada cita con `result.text.substring(start,end)` y su rango con el fragmento de página real. Estos offsets pertenecen al texto extraído, **no a posiciones de bytes del PDF**.

El PDF de texto tiene 1987 bytes y SHA-256 `2efb85528330d78458d9f427466fd2ae7c1990b698879bbca12a28b204f89784`. Se conserva junto con la extracción JSON y los resultados. Una comprobación independiente con Poppler (`pdfinfo`, `pdftotext -layout`) confirma tres páginas, los mismos textos y los separadores de página.

Otro PDF auténtico contiene una imagen raster incrustada y ninguna capa de texto. La ruta de extracción existente, con presupuesto OCR explícitamente cero, registra `ocr_page_limit`, sin fragmentos ni citas fabricadas. Poppler también confirma ausencia de capa textual. **No se ejecutó reconocimiento OCR real ni se demostró legibilidad/rendimiento de PdfRenderer en dispositivo.** El test distingue formatos; no sustituye la prueba OCR pendiente.

## SAF: evidencia y límite del entorno

`adb devices` no mostró dispositivos. No hay emulador, imagen Android ni KVM disponibles. No se creó APK de aplicación o pruebas, ni se ejecutó instrumentación. Por ello no se presenta esta evidencia como un proveedor Android físico/instrumentado.

`CitationDocumentsProvider` es una implementación Android `DocumentsProvider` alojada en Robolectric API33, registrada sólo en tests. Utiliza URI de árbol/documento, permisos persistidos mediante ContentResolver, lectura real de bytes a través de `openDocument`/ParcelFileDescriptor, y extracción TXT existente. La fila de índice se siembra con la API existente y se le asigna propiedad SAF en el fixture; no se prueba el recorrido completo del selector/indexador SAF.

Se cubren permiso válido; revocación sin cambio de configuración; archivo eliminado; reemplazo de igual longitud; proveedor inaccesible; cancelación; cambio de permiso durante apertura; y proveedor bloqueado. El bloqueo controlado está en `openDocument`, con latches y máximo de 3 s en el fixture. La invalidación vacía el estado antes de liberar el proveedor; resultados tardíos no vuelven a publicarse. El hash/permisos se vuelven a comprobar después de acceder a los bytes, aun sin un aviso de revocación.

SAF **no emite una señal universal de revocación**. No es posible prometer eliminación espontánea en el mismo instante de un cambio externo no notificado. El contrato exige invalidar al conocer un cambio de acceso/proveedor, al volver de Ajustes y al abandonar el contexto. La puerta de acción vuelve a validar incluso cuando no llegó señal alguna. La entrega futura debe comprobar estos adaptadores con DocumentsUI/proveedores reales, Binder, lectura de stream bloqueante y revocación física.

Los límites existentes de 8 MB, buffer de hash de 8 KiB, ocho candidatos y timeout cooperativo de 10 s permanecen intactos. Una tarea de cancelación separada retira el texto incluso si se cancela la corrutina de una acción mientras `resolve` permanece bloqueado. Cancelar no garantiza interrumpir una llamada nativa bloqueante: se puede retirar el texto y bloquear publicaciones inmediatamente mientras el recurso termina de liberarse al retornar. No hay nuevo hilo permanente, servicio, índice ni polling periódico.

## Contrato reactivo aislado

`DocumentCitationState` envuelve la sesión existente, sin reconstruirla. Expone `visible: StateFlow<List<DocumentCitation>>`, inicialmente vacío, sin SavedStateHandle, savers, Bundle, preferencias ni serialización.

- `query(text)` borra el texto visible y cancela la sesión anterior antes de consultar. Una generación impide publicar resultados tardíos de otra consulta.
- Cambios de configuración y de `FileIndex.revision` invalidan. `invalidate()` es la entrada síncrona para eventos de permiso/proveedor, cancelar y salida a segundo plano; limpia antes de esperar I/O.
- `close()` elimina el estado y cancela el ámbito hijo. Cancelar el ámbito padre también lo elimina. Volver a crear el responsable comienza vacío: navegación/recomposición no pueden restaurar citas desde este contrato.
- `withValidated(ref, action)` exige una referencia de la sesión visible actual y revalida fuente, huella y permisos antes de permitir una operación explícita de mostrar/copiar/abrir. El callback es breve, síncrono y no debe guardar texto. El responsable no abre Quick Look, copia ni envía Intents automáticamente.
- Los consumidores deben renderizar el estado vigente, descartar copias de snapshots anteriores y no persistir el contenido. Ninguna API puede borrar una copia de texto ya entregada a otro consumidor.

Antes de integrar en la barra: conectar eventos reales de lifecycle/acceso/proveedor a `invalidate()`, usar la configuración efectiva (incluido el modo privado temporal), el ámbito de navegación apropiado, volver a validar en la puerta de cada acción y reutilizar la apertura explícita autorizada existente. No conectar scroll a apertura. La galería Studio del primer checkpoint sigue siendo únicamente una propuesta de test; no se rediseña aquí.

## Corrección localizada y pruebas

La prueba de estado detectó que `FileIndex.clear()` borraba las tablas sin emitir `revision`. Por eso el consumidor reactivo conservaba una cita visible hasta otra acción. Corrección mínima: incrementar atómicamente la revisión al concluir el borrado. No se cambian resultados, ranking, filtros, esquema ni extracción. Se conservan XML/log antes y después.

Un caso adicional reprodujo que cancelar la corrutina de `withValidated` dejaba visible el texto hasta que el proveedor retornara. El nuevo responsable usa una tarea hermana de cancelación, vinculada al mismo ámbito, para limpiar sin esperar esa lectura; no hay reintentos ni publicación tardía. Se conserva el rojo y la prueba posterior exige vaciado mientras el latch del proveedor sigue cerrado. Esta corrección pertenece al responsable nuevo, todavía sin integrar en UI.

También se conserva el primer fallo de infraestructura del fixture SAF: un `apply` resolvía `context` contra el proveedor todavía sin inicializar. Se corrigió el test usando la aplicación explícita y limpieza defensiva; no fue un fallo productivo de SAF.

Resultado: **14 casos nuevos correctos (4 PDF +10 SAF/estado), una regresión C5.1 correcta; lint 0 errores, 26 advertencias y 1 información.** No se suman ejecuciones intermedias repetidas.

Resultados finales y comandos exactos en `verification/c5-2-provenance-20261107/README.md`. Los 11 casos de C5.1 se conservan en su evidencia original; sólo se repite la regresión de privacidad/borrado relacionada con `clear()`. No se ejecutan suites históricas ni se atribuye validación física a Robolectric.

## Preservado y siguiente C5.3

Focus Studio, Geist, materiales, inspector/footer/ActionPanel, Quick Look, animación 140 ms, búsqueda universal, Preview67/paquete/firma/versionCode/manifiesto/preferencias intactos. Sin Actions, APK, main, merge, dependencias ni permisos nuevos. C3/C4/C6–C8/Spotify/Square siguen pausados.

Antes de C5.3: validar SAF/OCR real y el anfitrión de lifecycle/permisos en dispositivo; medir lectura/latencia; ampliar corpus con PDF largo/Office y OCR incierto. C5.3 podrá evaluar candidatos semánticos del índice/modelo local existente contra la base léxica, preservando estos contratos de procedencia/invalidez. Síntesis de respuestas y cualquier transmisión remota siguen fuera del alcance; sin RAGFlow ni backend. No crear una Preview por este checkpoint.
