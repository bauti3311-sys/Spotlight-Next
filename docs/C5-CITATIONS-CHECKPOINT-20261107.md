# C5 — primer checkpoint de citas locales

Fuente técnica publicada: `844e23d25c3c8ba8c41c2c5ed33f6ddfc11fadde`.

Base remota verificada: `293290a30d739f2e4495a3560f59e56618f96d1a`, PR #21 abierto/en borrador. C5 reabierto por decisión del propietario; C3, C4 y C6–C8 siguen pausados. **C5 no está terminado.** No hay nueva APK ni Actions.

## Inventario reutilizado

| Capacidad actual | Código | Uso en este checkpoint |
|---|---|---|
| SAF persistido, imports, permisos actuales, índice SQLite, revisión y huellas | `index/FileIndex.kt` | Misma autoridad y mismo índice; pequeño acceso de lectura que verifica los bytes actuales |
| Consultas naturales, búsqueda exacta/léxica de documentos | `search/SearchQuery.kt`, `FileIndex.search` | Recuperación explícita con el orden existente, hasta ocho candidatos |
| Extracción local TXT/Office/PDF y OCR | `index/DocumentText.kt`, `index/OfficeText.kt`, `index/DocumentExtraction.kt` | Reutiliza texto, SHA-256, páginas y offsets de extracción; no crea otro extractor |
| Fragmentos acotados | `index/DocumentExcerpt.kt` | Encuentra un extracto y comprueba que sea substring literal, sin elipsis sintéticas |
| Modelo/índice semántico local opcional | `semantic/DocumentEmbedder.kt`, `SemanticModelStore`, `WordPiece`, `FileIndex.semanticSearch` | Inventariado y preservado; todavía no conectado al contrato de citas |
| Texto contextual atribuido | `data/DocumentSelectionText.kt` | Conservado; no reemplazado ni enviado a un servicio |
| Quick Look y cancelación de extracción | `ui/AuthorizedFilePreview.kt`, `DocumentExtractionRequest` | Sin cambios. La propuesta no abre Quick Look ni agrega gestos |

## Contrato implementado

`DocumentCitationSession.retrieve(query)` devuelve referencias opacas de sesión. `resolve(ref)` vuelve a comprobar permisos, presencia en el índice y hash del archivo antes de devolver un `DocumentCitation`: documento/URI/título, `sourceId`, SHA-256, fragmento literal, offsets UTF-16 `[start,end)`, método, incertidumbre OCR y página nullable.

La página sólo se admite para PDF con metadatos de extracción versión 1, fragmentos ordenados sin solapamientos, offsets válidos y página dentro del total registrado. No se deduce del nombre, la posición del resultado ni de una respuesta generada. El texto OCR conserva errores y marca `uncertain=true`; no transforma «1O mg» en «10 mg». La atribución se apoya en la extracción local existente: no se ha demostrado todavía la correspondencia física de cada página con un PDF real en este nuevo contrato.

Una consulta nueva invalida las referencias anteriores. Cerrar o cancelar la sesión cancela sus operaciones y borra sus referencias. Resolver falla ante revocación SAF, modo privado, exclusión de fuente, borrado de índice/archivo, cambios de bytes o metadatos, referencias de otra sesión y procedencia incompleta. Se revalida también el lote antes de devolverlo. Una copia de texto ya entregada a un consumidor no puede retirarse retroactivamente: el futuro dueño de interfaz debe limpiar su estado al revocar/cancelar/cambiar fuente, y resolver nuevamente antes de mostrar, copiar o abrir. Ese dueño reactivo de interfaz aún no está integrado.

Cada lectura verifica SHA-256 con buffer de 8 KiB, límite de 8 MB por archivo; hasta ocho candidatos, extractos de hasta 600 caracteres, cuerpo indexado hasta 100000 y metadatos hasta 30000 caracteres. Tiempo cooperativo de operación: 10 s. Un proveedor SAF bloqueante puede retrasar la cancelación hasta devolver la lectura: pendiente de prueba física. No hay carga completa de bytes, servicio permanente, segundo índice, modelo nuevo ni llamada de red. La comprobación de bytes repetida tiene coste de I/O que deberá medirse antes de usarla en la barra; no se ejecuta por pulsación.

## Corpus reproducible y alcance de la prueba

`DocumentCitationsTest` usa archivos ficticios locales, `online=false`, sin modelo ni servicio. Tres consultas con la gramática natural **ya existente**:

| Consulta | Documento | Fragmento esperado | Página / método |
|---|---|---|---|
| buscar archivos de presupuesto aprobado | plan.txt | El presupuesto aprobado del proyecto es de 2400 pesos. | ninguna / local_text |
| buscar archivos de reunión martes | agenda.txt | La reunión será el martes a las 15 horas. | ninguna / local_text |
| buscar archivos de dosis aparente | escaneo.pdf | La dosis aparente es 1O mg; revisar la imagen original. | 2, fixture / ocr_local, incierto |

El caso PDF/OCR usa bytes de fixture y metadatos controlados, **no ejecuta OCR ni PdfRenderer sobre un PDF real**. Prueba propagación y validación del contrato, no reconocimiento óptico ni paginación física. Los tests comprueban substring por offsets y procedencia, permiso SAF persistido (Robolectric), revocación con configuración todavía antigua, eliminación, reemplazo de igual longitud, cancelación, sesión cerrada, cambio de consulta, modo privado, índice borrado, páginas inválidas, huella ausente y referencia ajena.

Resultado: 10 casos de dominio + 1 caso Compose correctos; lint 0 errores, 26 advertencias y 1 información. Comandos, XML y capturas originales en `verification/c5-citations-20261107/`. No se ejecutaron suites históricas completas. Esta evidencia JVM/Compose no demuestra comportamiento de proveedores SAF, Gboard, One UI ni rendimiento físico.

## Presentación propuesta, sin integración productiva

[Galería clara/oscura](verification/c5-citations-20261107/gallery.html): tarjeta de 360 dp construida con `SpotlightTheme`, `StudioSurface("inspector")`, tipografía y botón existentes. Documento, cita literal, procedencia e indicación de página cuando sea comprobable; OCR debe mostrar advertencia de incertidumbre. «Abrir fuente» es explícito y revalida la referencia antes del callback. El test comprueba cero callbacks al hacer scroll y uno al pulsar; **no abre un documento real**. No es una pantalla nueva ni una captura del buscador completo.

La propuesta conserva composición Studio; no altera barra, inspector, ActionPanel, footer, Ajustes, ranking, Focus reversible ni animación productiva de 140 ms. Antes de incorporarla, diseñar la invalidación reactiva del estado visible y conectar exclusivamente la apertura explícita autorizada existente. No presentar automáticamente citas como respuestas concluyentes.

## Límites y siguiente implementación acotada

- Recuperación léxica con consultas naturales existentes, no comprensión general de preguntas ni síntesis de respuestas. La integración de candidatos semánticos y su evaluación quedan pendientes; no se promete recuperación por sinónimos.
- Filas antiguas sin SHA verificable, OCR de imagen sin huella y archivos mayores al límite se excluyen; no se reextraen silenciosamente. Se conserva el índice existente.
- Un extracto ambiguo o metadatos incompletos se rechazan. Hay que ampliar pruebas reales PDF/Office/OCR, texto Unicode y documentos repetitivos sin debilitar la procedencia.
- Próximo checkpoint recomendado: validar procedencia/páginas con un pequeño PDF real y SAF instrumentado; evaluar recuperación semántica local sobre el mismo corpus; preparar dueño de sesión con limpieza visible por revocación/cancelación antes de integrar la tarjeta.
- Offline demostrado con archivos locales ficticios; un proveedor SAF seleccionado puede gestionar su propia red y debe evaluarse por separado.
- Sin backend, RAGFlow, subida de documentos, dependencias, permisos nuevos ni respuestas inventadas. No se modifica identidad/versionCode 67, manifiesto de entrega, preferencias, Preview 67, firma, paquete ni main. C3/C4/C6–C8 siguen pausados.
