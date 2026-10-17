# Selección documental y contexto Gemini · 17/10/2026

Código642b00cadcf0768f1969ad05d289ae9fdea01783, posterior al cierre OCR b1140df. Reutiliza FileBuffer, DocumentText.extractDetailed, DocumentExtractionRequest y DirectAi/AiRequestPolicy. La comparación y el resumen ya tenían botones; se consolidó su preparación y seguridad, sin sustituir motores ni agregar una pantalla. Obsidian, Quick Look, Ajustes y conversaciones no se rediseñaron.

## Alcance real

La selección sigue limitada a12archivos locales/autorizados y conserva compartir mediante Intent con read grants. Resumir/comparar prepara texto, no inicia una petición. Cada fuente tiene identificador D1…D12, nombre acotado, SHA256 de los bytes extraídos cuando existe, cobertura y cierre propios. PDF añade página y método (texto nativo/OCR local, con aviso de revisar). El hash de imagen no está disponible en el extractor actual: se comunica, no se inventa. Los avisos generados del PDF no se presentan como citas del documento.

El presupuesto total es16000caracteres de contexto documental, repartido entre fuentes incluyendo cabeceras/cierres. Cada fuente conserva espacio propio: ya no hay un take final que corte el último documento silenciosamente. Recortes adicionales se señalan. Se mantiene el límite real del extractor de8.000.000bytes, aunque el proveedor no informe tamaño. Hasta6páginas nuevas OCR/imágenes en la selección; cada PDF conserva sus límites de40páginas nativas/3OCR por defecto, acotados por el presupuesto restante. Un PDF con OCR pendiente conserva cobertura parcial, no se declara completo.

Hay un plazo cooperativo de60s para preparar el conjunto. Un timeout interno del extractor informa qué documento falló; una cancelación del usuario no se convierte en error. Un fallo de permiso/fuente aborta la preparación: no envía una comparación parcial silenciosa. No hay lectura de red, caché nueva de contenido, embeddings ni worker persistente añadidos por este bloque.

La preparación tiene propietario ligado a consulta/ámbito y al conjunto de identidades/URI seleccionadas. Cambiarlo, destruir el overlay o pulsar Cancelar preparación cancela la solicitud y descarta texto/errores tardíos. Reutiliza el propietario de extracciones manuales existente y la retención nativa de bitmaps del extractor; cancelar no recicla memoria que ML todavía usa. El botón existente Preparar resumen cambia temporalmente a Cancelar preparación mientras trabaja; su estado normal, materiales y distribución permanecen iguales.

El resultado preparado usa PRIVATE_CONTEXT. La revisión muestra el texto exacto que se enviará; solo Enviar autoriza DirectAi. No se usa ID de resultado ni salto de línea como única prueba de consentimiento. Iniciar otra preparación retira el borrador de revisión anterior. No se llama Gemini al escribir ni se envía el contenido por seleccionar archivos.

## Pruebas

Diez casos nuevos: extracción de dos TXT reales con hash exacto; doce textos grandes conservando cabecera/cierre de todas las fuentes dentro del tope; procedencia PDF/cobertura parcial/recorte sin aviso citado como contenido; metadatos inválidos y permiso revocado; cancelación sin leer el siguiente archivo; timeout anidado identificable; cuota OCR y hash de imagen no disponible; y tres recorridos Compose.

Los recorridos Compose parten de una selección inyectada en el mismo componente de producción. Dos archivos locales pasan por extracción real → revisión → DirectAi con HTTP fixture: cero peticiones antes de Enviar y payload idéntico a lo revisado; el streaming se muestra inline. Cancelar mediante el botón o quitar un archivo mediante su chip descarta una extracción fixture que termina tarde y no hace HTTP. No es una prueba de descubrimiento/selección manual desde un proveedor SAF ni una llamada real a Gemini.

32pruebas dirigidas/5suites y compileDebugAndroidTestKotlin correctos en1min31s;323inputs compilados coinciden con642b00c. Los gates completos y referencias visuales de este nuevo código se registran en el handoff/evidencia, separados de los539/21ya cerrados para OCR en f05e82b. No sumar rondas ni convertir instrumentación compilada en ejecutada.

## Pendientes

Validación en One UI con documentos reales y credenciales propias, reconocimiento ML nativo y medidas de memoria/tiempo; adjuntos multimodales remotos, comparación completa de documentos extensos y operaciones universales en lote. Esta función prepara extractos acotados, no comprensión de archivos completos. No entrega APK parcial ni firma nueva; la entrega única final conserva el mecanismo histórico protegido.


Cierre642b00c:549completos/116suites, lint3sin errores (11advertencias+1informativa cada uno), Release/R8 y Kotlin instrumentado correctos en10min47s.21referencias estrictas sin cambios en21s;323inputs Git iguales, sin fuentes adicionales. verification/document-selection-20261017/summary.json contiene casos, comandos, hashes y límites. No instrumentación ejecutada ni proveedor real.
