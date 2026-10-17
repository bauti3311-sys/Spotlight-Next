# Presupuesto adaptativo del OCR PDF · 17/10/2026

Continuación de151dfbe, misma rama propia feat/work-integral-settings-20261016 y propietario work-gemini-native-20261014-f914c617. No sustitución de arquitectura ni cambios en Obsidian, Quick Look, Ajustes, datos, conversaciones o firma. Código del bloque: f05e82b0c7a53263b2e25d381bb6ce057a75a321, precedido por0595052 y c8a02fc. El estado completo de gates se cierra en el handoff y summary de evidencia, no se deduce de este documento.

## Política y recorrido de producción

DeviceResources extrae la lectura existente de AdvancedPanel y comparte ActivityManager.MemoryInfo y batería sticky. Añade PowerManager para ahorro y estado térmico (API29+). Las lecturas fallidas son desconocidas, no valores saludables inventados; en API26 sin API térmica no se exige esa señal. AdvancedPanel mantiene composición y campos anteriores. No se solicita un permiso nuevo ni se envían lecturas por red.

FileIndex, cuando se solicita análisis, evalúa antes de abrir el PDF y antes de cada nueva página. PdfTextPipeline vuelve a comprobar después de renderizar y antes de admitir ML. El presupuesto no se aplica a etiquetas/vídeo ni cambia por defecto la extracción manual explícita. Las consultas de búsqueda no esperan OCR: ensure agenda actualización de metadatos en segundo plano, también para SAF. En modo privado o con archivos deshabilitados ni siquiera agenda esa actualización.

| Condición real | Decisión |
|---|---|
| lowMemory o memoria disponible por debajo de max(64MiB, umbral Android) | Aplazar OCR. |
| Térmico SEVERE o superior (3–6) | Aplazar. |
| Ahorro de energía activo | Aplazar. |
| Batería ≤15% sin alimentación externa | Aplazar. |
| Señal requerida ausente/inválida | Aplazar; API térmica inexistente en API26 no bloquea. |
| Térmico MODERATE, batería ≤30% sin alimentación externa, o memoria por debajo de max(128MiB, doble umbral anterior) | Reducir. |
| Resto | Presupuesto normal. |

Normal: hasta3 páginas nuevas OCR/documento, lado1600px,1.800.000píxeles,12s por tarea. Reducido:1 página nueva, lado1024px,786.432píxeles (~3MiB ARGB),8s. Se conservan los límites40páginas nativas/8.000.000bytes/45s por PDF y12intentos OCR/120s de admisión por actualización. Una sola extracción PDF admitida; no se crea un worker paralelo. La memoria consultada es del sistema, no RSS/heap de la app. Son límites de entrada y admisión verificables, no una promesa de RSS ni preempción dura de bibliotecas nativas.

## Aplazamiento y recuperación

Las páginas omitidas conservan issues ocr_deferred con motivo. Las omitidas antes de leer texto nativo no incrementan páginas examinadas; una página examinada nativamente puede seguir con OCR pendiente. No se generan fragmentos OCR ficticios y la fila queda pending. Preview y estado explican batería/ahorro/memoria/temperatura/estado desconocido y el reintento. El usuario reanuda con la acción existente de analizar índice cuando mejora la condición; no hay reanudación automática persistente ni WorkManager nuevo.

La recuperación sobrevive recrear FileIndex porque texto, procedencia y pendiente están en SQLite. Reutiliza solo páginas OCR con versión de metadatos válida, SHA256 exacto de los bytes actuales, número total de páginas coincidente, rangos válidos y sin truncamiento por character_limit. Una huella distinta o un fragmento truncado requiere reconocer de nuevo. La cuota cuenta OCR nuevo, no páginas ya reconocidas reutilizadas.

Si cambia el archivo mientras está aplazado, conserva el último texto y lo etiqueta explícitamente como versión anterior pendiente. No hace pasar datos viejos por actuales ni borra texto útil por batería baja. No marca el documento completo si queda contenido pendiente.

## Cancelación, permisos y concurrencia

Se mantienen CancellationException, lease nativo y descarte tardío. Un bitmap y la admisión no se liberan hasta terminar realmente la Task nativa, aunque el consumidor cancele. Se comprueba autorización antes de cada archivo/página y antes de guardar; SAF revocado no se convierte en importación. El índice conserva filas ya completadas y texto anterior al cancelar.

Se retiró la transacción que envolvía todo el recorrido y esperas OCR. Cada fila se confirma en una transacción corta, sin suspensión. La poda solo ocurre tras el recorrido autorizado completo. Buscar/ensure siguen disponibles con OCR suspendido.

Antes de guardar se vuelve a leer la fila: si una extracción manual modificó body/extraction/author/created durante la espera, el refresh conserva ese resultado y deja pendiente el análisis. La regresión de escritura perdida se reprodujo contra0595052 y se corrigió en c8a02fc. El guard de cobertura de151dfbe sigue vigente. El bucle ensure privado→refresh rechazado→revision→nueva búsqueda se reprodujo contra c8a y se corrigió en f05e82b; el índice frío privado/deshabilitado no agenda ni notifica un refresh inútil.

## Evidencia y límites

Las pruebas recorren política, pipeline y SQLite/búsqueda reales con fixtures de recursos y reconocedor. Incluyen pausa/recuperación tras recreación, permisos revocados, cancelación, consulta concurrente, cambio de fuente, conservación de extracción manual más nueva, recortes inválidos y API26. DeviceResourcesDeviceTest y PdfOcrDeviceTest son instrumentadas; compilar sus fuentes NO equivale a ejecutarlas. Sin dispositivo conectado/KVM no se acredita ML Kit/PdfRenderer nativo, batería/temperatura real, One UI ni mediciones de memoria/latencia.

Fuentes Android primarias: https://developer.android.com/reference/android/os/PowerManager ; https://developer.android.com/reference/android/app/ActivityManager.MemoryInfo ; https://developer.android.com/reference/android/os/BatteryManager .

No Actions, nuevas claves, entrega APK parcial, merge a main, force-push ni modificación del PR16. La matriz40 continúa parcial. La firma histórica autorizada y sus requisitos sobre el SHA exacto siguen siendo condición de la única entrega final.
