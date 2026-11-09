# C5.3 — evaluación local: base léxica y límite semántico

Checkpoint de tests/corpus publicado: bfe72bd9fb72abadbda8b14b717734c1efa526fc.

Base remota comprobada: 16ef8bfddb334ec2a7dc84615923348ec6826070 / PR21 abierto y en borrador. C5.1/C5.2 y Preview67 conservados. **C5 sigue abierto; no hay integración en la barra, nueva Preview ni APK.**

## Decisión

**No conectar todavía recuperación semántica a DocumentCitationSession.** En este entorno no hay activos verificados del modelo ni índice vectorial real. La base léxica funciona sin modelo y la evaluación muestra huecos concretos en sinónimos/paráfrasis, pero no se puede demostrar una mejora semántica actual, ausencia de falsos positivos, coste ni seguridad de una unión que no se ejecutó. No se descarga ningún modelo, no se simulan vectores y no se cambia producción para aparentar progreso.

## Auditoría de capacidades existentes

| Componente | Capacidad comprobada en código | Límite relevante para C5 |
|---|---|---|
| FileIndex.semanticSearch | Consulta local; preferencia, archivos/contenido, modo privado y recursos; 1000 vectores como máximo, selección por coseno mínimo .28 y margen .08 del mejor, hasta ocho documentos, léxico publicado antes en SearchEngine | Comprueba fingerprint del índice y substring del texto indexado; **no reabre y calcula SHA de los bytes originales**. Un Result semántico no es una cita |
| buildSemantic | Preparación manual explícita; mismo SQLite semantic_vectors, modelo/huella versionados, hasta 200 archivos y ocho fragmentos por archivo; límite de 60 s y reanudación | No se reconstruye ni activa en C5.3. Admite texto/Office/PDF; no prometer cobertura completa de documentos largos o imágenes OCR |
| DocumentEmbedder / LocalDocumentEmbedder | WordPiece cased, ventanas de 96 piezas con solapamiento 16; consulta hasta 128 tokens incluidos especiales; encoder local y timeout 1500 ms | La disponibilidad depende de SemanticModelStore; no instala ni extrae documentos |
| SemanticModelStore | Instalación optativa explícita desde Ajustes, tamaños/SHA fijados; ready() sólo verifica archivos; remove borra modelo/índice derivado por recorrido existente | ready=false en el laboratorio. No se conoce si está instalado en la tablet/teléfono del propietario |
| SemanticPolicy | Aplaza con memoria baja o disponible <max(768 MiB, 2×umbral), térmica ≥2, ahorro, batería ≤20% sin carga o datos de recursos desconocidos | Las entradas saludables/de memoria baja de estos tests son fixtures de política, no mediciones de RAM del Galaxy |
| SemanticWorker / encoder existente | Un dueño nativo activo y una solicitud en cola; terminación/cancelación/generación, cierre tras inactividad; dos hilos intra-op/uno inter-op, sin arena CPU | Carga JNI, latencia/RAM y cancelación nativa no se evaluaron de nuevo |
| Citas C5.1/C5.2 | Reabre fuente autorizada, SHA real, fragmento literal, offsets de extracción, página respaldada y estado efímero con invalidación | Se conserva sin modificaciones. Ninguna salida semántica salta estas verificaciones |

Modelo fijado: DistilUSE multilingual cased v2, revisión bfe45d0732ca50787611c0fe107ba278c7f3f889. Activos esperados: ONNX 135336307 B +vocabulario 995526 B +Dense 1575104 B =137906937 B. Es tamaño de almacenamiento, **no RAM medida**. Se inspeccionaron archivos del workspace y ubicaciones de laboratorio; no hay .onnx/vocab disponibles. El contexto Robolectric se inicia sin modelo; no es una inspección del almacenamiento privado de un dispositivo real.

La evaluación histórica auténtica de 17/10 (docs/SEMANTIC-MODEL-EVAL-20261017.md y verification/semantic-evaluation-20261017/host-evaluation.json) conserva 8/8 primeros resultados sobre otro corpus y otro recorrido de laboratorio Linux. No se repitió, no se mezcla con las métricas actuales y no prueba el puente de citas, falsos positivos nuevos ni rendimiento Android. SemanticIndexTest usa vectores simulados: sirve como evidencia de contratos, no de calidad del modelo.

La preparación de **embeddings documentales** es manual. El SearchEngine general existente sí llama semanticSearch después del léxico; con preferencia/modelo disponibles puede codificar la consulta durante una búsqueda. C5.3 no modifica ni usa ese recorrido de la barra, no añade invocaciones por tecla y no debe confundirse preparación manual con ausencia global de codificación de consultas.

## Corpus y comparación reproducible

Seis documentos ficticios: tres TXT, un PDF auténtico de tres páginas reutilizado de C5.2, un TXT con contenido repetido y un PDF raster generado localmente con texto OCR controlado. Nombres neutrales doc-01…doc-06. Doce consultas: literales, sinónimos, paráfrasis, irrelevantes, repetidos y OCR incierto. Relevancia fijada manualmente antes de ejecutar; no se ajusta según el resultado. El caso de horario exige el documento con hora; el de grupo exige equipo explícito. Son criterios de este corpus pequeño, no una evaluación general.

D4 usa DocumentText existente y el PDF SHA2efb85528330d78458d9f427466fd2ae7c1990b698879bbca12a28b204f89784. No se reimplementa extracción/paginación. D6 tiene letras de «1O mg» (O, no cero); el PDF es auténtico y la transcripción está marcada **fixture de metadatos OCR**, no resultado de reconocimiento. No se normaliza a diez miligramos ni se afirma verificación OCR visual automática.

| Caso | Consulta | Documentos relevantes declarados | Orden léxico observado |
|---|---|---|---|
| Q01 · literal | presupuesto aprobado | D1 | D1 |
| Q02 · literal_pdf | presupuesto café | D4 | D4 |
| Q03 · literal_multiple | reunión martes | D2, D4 | D2 → D4 |
| Q04 · synonym | dinero asignado a la iniciativa | D1 | ninguno |
| Q05 · synonym | encuentro del grupo el martes | D2 | ninguno |
| Q06 · paraphrase | viaje ferroviario después del anochecer | D3 | ninguno |
| Q07 · paraphrase | a qué hora nos juntamos los compañeros | D2 | ninguno |
| Q08 · irrelevant | receta de sopa de calabaza | ninguno | ninguno |
| Q09 · irrelevant_shared_word | presupuesto de una nave espacial | ninguno | ninguno |
| Q10 · repeated | cláusula común | D4, D5 | D4 → D5 |
| Q11 · ocr_uncertain_literal | dosis aparente | D6 | D6 |
| Q12 · ocr_uncertain_paraphrase | información de la dosis escaneada | D6 | ninguno |

| Método actual | Aciertos documento-consulta | Omisiones | Falsos positivos | Precisión micro | Recall micro | MRR consultas positivas |
|---|---:|---:|---:|---:|---:|---:|
| Léxico existente | 7 | 5 | 0 | 100% | 58,33% | 0,50 |
| Semántico con modelo auténtico | **No evaluado** | — | — | — | — | — |

Son doce pares relevantes en diez consultas positivas y dos consultas negativas. Cinco consultas positivas obtienen algún relevante. Se conserva el orden completo por consulta; no se altera el ranking general de aplicaciones. La ausencia de falsos positivos en dos negativos no certifica precisión general.

La llamada semántica sin vectores/modelo devuelve vacío y el léxico sigue funcionando. Esto es un **fallback correcto, no recall semántico cero**. En evaluation.json la calidad y ranking semánticos son null. Las siete citas de las consultas con resultados pasan la sesión existente: quote=substring exacto por offsets, SHA actual, página en fragmento respaldado y bandera uncertain en D6. Para D6 esto prueba el contrato del fixture, no reconocimiento real.

## Costes y validación

Cuatro casos nuevos correctos en una clase dirigida: corpus/atribución; ready sin red e instalación incompleta; rechazo de preparación sin modelo y con poca memoria; estado efímero con preferencia semántica activa y modelo ausente. No backend de embeddings inyectado. Primera ejecución correcta; segunda sólo tras hacer que el raster OCR del fixture corresponda al texto declarado. Ambas evidencias se conservan, sin sumar rondas.

Los nanosegundos observados por llamada están en final/evaluation.json; resumen en final/host-costs.json. Son tiempos de esta JVM, corpus pequeño y cachés/scheduling del host. La puerta semántica vacía **no mide inferencia**. Sin datos nuevos de RAM, batería, temperatura, latencia JNI ni Galaxy/Motorola. No hay comparación de costes léxico/modelo ni adopción por esos tiempos.

No se repiten suites históricas ni lint: app/src/main, Gradle, permisos y dependencias son idénticos a la base. Se reutiliza explícitamente el lint C5.2 (0E/26W/1Info), sin declararlo una nueva ejecución.

## Condiciones del puente futuro, antes de integrarlo

1. Modelo existente instalado/verificado y recursos reales aptos; generar índice sólo mediante preparación explícita. Repetir este corpus con inferencia auténtica, registrar orden/omisiones/FP y ampliar negativos. Mejorar omisiones sin degradar resultados literales ni aceptar referencias inválidas; medir coste en dispositivo.
2. Resolver coordenadas: semantic_vectors apunta a SemanticFile.text = searchableText, que excluye encabezados y concatena páginas. Las citas apuntan a body de extracción. Un fragmento WordPiece puede abarcar varias páginas; no reutilizar sus offsets como offsets de body ni deducir página desde Result.preview.
3. La validación literal actual exige solapamiento léxico con la consulta. Concatenar listas de resultados no basta para sinónimos. Será necesario un pequeño contrato estructurado de fragmento candidato con mapeo verificable al documento, después de demostrar calidad; nunca parsear el preview formateado ni inventar páginas.
4. Toda coincidencia complementaria debe pasar la puerta C5.2: permisos actuales, SHA de bytes, literal/offsets/página probada, generación de consulta y estado reactivo. No publicar resultados tardíos. Conservar base léxica, cancelación, preferencia y fallback; sin activación implícita.
5. SAF/OCR/lifecycle físicos siguen pendientes, como condición para llevar C5 a la experiencia real. Esta evaluación no los resuelve. Sin síntesis de respuestas, backend, subidas ni RAGFlow.

Studio/Geist/Obsidian/140ms/Quick Look, firma/paquete/versionCode67/manifiesto/preferencias y main intactos. C3/C4/C6–C8/Spotify/Square siguen pausados. Sólo tests, corpus y documentación; **ninguna mejora semántica productiva integrada por falta de evidencia auténtica actual**.
