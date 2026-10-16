# Matriz funcional y pendientes — Spotlight Universal Preview

La interfaz Obsidian/Graphite/macOS Light se conserva. Las verificaciones automatizadas usan Android/Robolectric y fixtures explícitos de apps, archivos y HTTP; no son instalación física ni una consulta real con clave del usuario. No se usó ninguna clave real ni se contactó a Gemini durante el desarrollo.

| Consulta / flujo | Comportamiento real implementado | Evidencia local | Requisito / límite |
| --- | --- | --- | --- |
| cuántas veces ganó Boca la Libertadores | Resultado principal Responder con Gemini si ese proveedor está seleccionado; Enter inicia streaming/Markdown en la misma ventana | UniversalProductionFlowTest + UniversalIntelligenceArchitectureTest | Clave/modelo Gemini configurados por el usuario; respuesta real del proveedor no verificada aquí |
| Explicame el reflejo vestibuloocular; compará estas opciones | Reconocimiento local y pregunta explícita; sin envío por escribir | UniversalSearchTest + flujo UI→HTTP de fixture | No adjunta automáticamente contexto local |
| Continuar conversación | Botón Enviar explícito conserva el diálogo autorizado; una pregunta nueva comienza sin historial anterior | Política de continuación tipada; transporte existente y pruebas de IA | Sin herramientas automáticas a partir de una respuesta |
| Abrí Spotify | Busca la app instalada y prepara su Intent real | UniversalSearchTest / UnifiedRootRegressionTest | Requiere esa app instalada; no promete reproducción automática |
| webOS Dev Manager | La coincidencia exacta supera comandos genéricos y bonos de hábitos | Regresiones léxicas UniversalSearchTest | PackageManager del dispositivo determina los resultados |
| Abrí Spotify y bajá el volumen al 40% | Plan editable; comprobar app y herramientas; confirmar pasos seleccionados; abrir app y ajustar volumen multimedia | UniversalProductionFlowTest.spotifyAndVolumeUseReviewedNativeToolsNotRemoteAi | Comportamiento al cambiar de app bajo One UI pendiente de prueba física |
| Buscá el PDF de rehabilitación vestibular y explicame el capítulo 3 | Índice autorizado → extracción local → sección/fragmento → prompt completo visible → consentimiento → IA inline | Prueba completa SearchEngine→SpotlightPanel→DirectAi; payload igual al prompt revisado | Extrae hasta40 páginas/24.000 caracteres; envío de fragmento acotado. No OCR universal ni interpretación estructural infalible de capítulos |
| Explicame este documento | Elegir contexto o documento autorizado y revisar antes de enviar | ContextPanel existente + política de consentimiento + pruebas de contexto | Sin documento/contexto elegido no se inventa contenido |
| Traducí este texto | Formulario local; usuario pega/escribe; revisión del envío privado | HybridIntelligenceTest | No lee el portapapeles automáticamente |
| Buscá el PDF de anatomía que abrí ayer | Informa que no existe historial de apertura; ofrece búsqueda por nombre/tipo sin ese filtro | UniversalSafetyTest en raíz y ámbito Archivos | **Pendiente:** registrar/consultar aperturas reales. Nunca sustituye apertura por modificación |
| PDF de anatomía modificados ayer | Rango local de modificación; filtros del índice | QueryInterpretationTest / FileSqlFilter | Depende de metadatos y autorización de la fuente |
| Inicio sin hábitos/favoritos | Sin relleno alfabético; puede quedar vacío | HomeSuggestionsTest y tests de arquitectura | Hasta una app con señal local; acciones/contexto tienen prioridad |
| Notas / comandos / temporizador | Adaptadores nativos preservados, ranking previo a deduplicar | Regresiones de creación/guardado, modos y Pomodoro | No se reintroducen snippets, Tasker ni rutinas como entradas de producto |
| Clima / Cómo va River | Se conservan rutas y proveedores existentes; no se añadieron datos ficticios | Clasificación/ruteo local | Sin validación nueva de datos en vivo; no se afirma cobertura, resultados ni horarios |

## Pendientes explícitos

- Probar físicamente esta integración en Samsung: apertura/foco y teclado, transición Spotify/volumen, Quick Look de documentos reales, rotación, permisos de la nueva Preview y fluidez. La estética del checkpoint anterior fue aprobada por el usuario; eso no acredita estos cambios funcionales.
- Configurar Gemini en la nueva Preview mediante Ajustes, guardar clave local y elegir un modelo devuelto por Consultar modelos disponibles. La aplicación previa conserva sus credenciales y datos: Android no los comparte con otro paquete.
- Validar respuesta real, cancelación/errores de cuota y continuación con el proveedor configurado por el usuario. Pruebas HTTP de fixture no demuestran disponibilidad de un modelo real ni consumo gratuito.
- Historial de archivos abiertos por fecha, índices semánticos y localización de capítulos fuera del límite de extracción: no implementados. Si no hay encabezado coincidente, se rotula claramente extracto inicial.
- Proveedores deportivos/clima, OCR extenso y nuevas herramientas de IA no se ampliaron en esta entrega.
- Actualización con certificado histórico continúa reservada al proceso protegido con CI verde del SHA exacto; esta entrega independiente no accede a esos secretos.

## Instalación segura

La variante adicional universalPreview usa dev.bautista.spotlight.next.preview.universal (código29). Puede coexistir con dev.bautista.spotlight.next y con dev.bautista.spotlight.next.preview. Abrir Spotlight Universal Preview desde su icono, conceder únicamente sus permisos y configurar IA allí. No desinstalar ninguna aplicación anterior ni reasignar la invocación principal ya validada para probar esta copia.
