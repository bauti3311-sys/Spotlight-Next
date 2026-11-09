# Preview 57 nativa — auditoría funcional y mejoras por impacto

Fecha: 2026-11-03 UTC. Base exacta `4fc33f2c918d3fe53a8bad7ac6571f2757630066`.
Esta auditoría aplica la [nueva dirección aprobada](NATIVE-PREVIEW57-DIRECTION-20261103.md).
El código de aplicación y sus tests permanecen byte por byte iguales a Preview57:
no se implementan cambios de producto, OpenUI ni RAGFlow en esta entrega.

## Diagnóstico para el propietario

**Conviene perfeccionar esta base.** Ya contiene el overlay Compose, el motor de
búsqueda, el inspector, formularios, ejecutores y recuperación documental. No hace
falta rehacer la interfaz ni sustituir motores para obtener mejoras funcionales.

Hay defectos acotados documentados en el menú y un problema de Lint de la barra;
el PR19 contiene sus correcciones. Hay además contratos de lanzamiento que
permiten cambiar de destino ante un fallo, y requieren revisión. Spotify y
WhatsApp tienen implementaciones nativas reales, pero su funcionamiento con la
cuenta, el teléfono y One UI **no queda certificado por una prueba con fixtures**.
No se conoce una lista completa de fallos físicos de Preview57; no se inventa.

## Cómo se interpreta la evidencia

- **Inspeccionado ahora:** código de la base exacta, historial, refs/notas remotas,
  PR19/20, XML y capturas de PR19. Se revisan rutas de ejecución, no sólo nombres.
- **Evidencia histórica revisada:** la nota de entrega de Preview57 declara 67
  pruebas dirigidas en cohortes (32 retenidas +35 afectadas), un ensamblado
  experimental sin R8/resource shrinking y lintVital correcto. Aquí se conserva
  esa nota; no se recuperó su ZIP de evidencia ni se reejecutaron esos casos.
  Por tanto es evidencia de entrega reportada, no una suite actual completa.
- **XML históricos leídos:** se cotejan los resultados del menú/barra de PR19 y
  su fallo inicial; son de otro HEAD, no pruebas de esta rama ejecutadas hoy.
- **No ejecutado hoy:** Gradle, Kotlin/JUnit/Robolectric, APK, instrumentación,
  llamadas a proveedores, OCR/embeddings nativos, envíos, audio o dispositivo.
  Los controles actuales de procedencia no se cuentan como pruebas funcionales.
- La matriz histórica 9/25/6 no se promociona ni se aplica mecánicamente al nuevo
  HEAD. Varios informes del 23/10 preceden Spotify App Remote y la fase D.

## Inventario funcional de la base

“Implementado” significa que la ruta existe y fue trazada en código; la última
columna limita qué puede afirmarse que funciona en el entorno del propietario.

| Área | Qué existe y se conserva | Evidencia concreta | Qué falta o puede fallar |
| --- | --- | --- | --- |
| Barra/overlay nativo | ComposeView en WindowManager, foco, insets, cierre y captura de estado; no necesita WebView | OverlayService.kt:217/251; OverlayLayoutPolicy; OverlayStartupTest; nota Preview57 | One UI, foco frío, IME, rotación/multiventana y tamaño real aún necesitan dispositivo. Incidencia histórica F1.3 no se declara resuelta por volver de rama |
| Búsqueda universal | SearchEngine agrega apps, archivos, contactos y acciones; ranking exacto/prefijo y resultados estables. UI usa debounce120ms, collectLatest y guard de generación | Models.kt:8/44; SearchEngine.kt:63/132; SpotlightPanel.kt:660; SmartSearchAcceptanceTest; UniversalIntelligenceArchitectureTest | Latencia/ranking con datos reales por medir; catálogo no equivale a permiso/capacidad disponible. Hay una ruta opcional de consulta online de texto; no afirmar “cero red de cualquier tipo por pulsación”. La política de cero IA remota por tecla se conserva |
| Resultados/inspector/acciones | Selección, Quick Look, panel contextual, fijado, acciones con identidad y navegación existentes | SpotlightPanel.kt:438/949/1062; ActionPanelView; ContextSplitPanel; ResultTouchScrollTest; ActionPanelKeyboardContractTest | Recorte y foco del menú en la base: ver PR19. Scroll, TalkBack y teclado físico/IME requieren comprobación real |
| Integraciones Android | Action sellada, Intent/shortcuts, detección de actividades exportadas/habilitadas y permisos; AppCapabilities revalida al preparar | Models.kt; IntegrationRegistry.kt:17; AppCapabilities.kt:55; ActionCatalogTest; AppCapabilitiesTest | El lanzamiento final conserva fallback automático; propuesta P1. Un enlace o actividad pública no ofrece control total ni acceso a datos internos. AppFunctions no es un ejecutor universal |
| Spotify | Búsqueda OAuth PKCE, tokens, SDK oficial App Remote0.8.0 y sesión local con ventana de inactividad; MediaSession para controles compatibles | SpotifySession/SpotifySearchClient/SpotifyLocalPlayer; docs/integrations/SPOTIFY-APP-REMOTE.md; SpotifyLocalPlayerTest y SpotifyOverlayRecoveryRegressionTest | Requiere app/cuenta/configuración/autorización. play devuelve “Solicitud aceptada”, no audio verificado. Reportes previos de cortes no están certificados resueltos en57; reproducir con cuenta real antes de tocar el controlador |
| WhatsApp/Business | Componer por contacto/número y enlace wa.me; responder una notificación vigente mediante RemoteInput auténtico; revisión explícita y cotejo de destinatario/acción. Datos visibles en RAM, no historial completo | CommandForms.kt:17/84; People.kt; ReplyService.kt:27/57/64; ReplyConfirmationTest; HybridIntelligenceTest:151 | Sólo notificaciones accesibles con respuesta publicada; sin notificación se prepara en la app. El motor vinculado experimental no está incluido, y la UI lo dice. No se certifica entrega ni lectura de todos los chats |
| Documentos/Quick Look | FileIndex, SAF/almacenamiento autorizado/importaciones, filtros/metadatos, extracción/OCR acotados, renderer PDF nativo con recursos limitados | FileIndex.kt:116/194/218; DocumentExtraction.kt:27; PdfQuickLook.kt:19; DocumentTextSafetyTest; ContextExtractionFlowTest | Concesiones reales, PDF escaneado/grande, motor OCR y consumo deben medirse; selección/scroll no deben abrir otra app. No se promete soporte ilimitado de formatos |
| Semántica local | semantic_vectors, fragmentos/offsets, huellas, modelo ONNX opcional verificado, cancelación y comprobaciones de fuente vigentes | FileIndex.kt:579/612/670; DocumentEmbedder; SemanticModelStore; SemanticIndexTest/SemanticPolicyTest | Sin modelo válido o datos preparados no hay resultados semánticos; conservar búsqueda léxica. Result.preview aplana cita/procedencia a texto; falta transportar cita estructurada/página comprobada, no un nuevo índice |
| IA contextual | DirectAi, streaming/cancelación, clave nativa, revisión de texto privado, herramientas/planes existentes | DirectAi.kt:51/94; AiRequestPolicy.kt; aiReview en SpotlightPanel.kt:644; HybridIntelligenceTest:92; AiRecoveryPrivacyTest | Sin configuración no hay inferencia. Proveedores/cuota/cancelación real y exactitud no se certifican. Una respuesta generada no demuestra efecto Android; no generar datos ni acciones fingidas |
| Formularios/planes | MessageEditor, formularios locales, CommandTemplatePanel, borradores por contexto, edición/confirmación, estados PREPARED/REQUESTED/COMPLETED/VERIFIED, límites y orquestación existentes | CommandForms; CommandTemplatePanel; ContextualDraftHost; ToolRegistry/ToolExecutor; PlanExecution; ToolSequenceExecution; NativeCommandFlowTest | Contratos aún mezclan tipos sellados con strings y errores de Exception. Mejorar un flujo específico antes de sumar un renderer; restaurar un borrador no autoriza ejecutar |
| Ajustes/permisos/datos | Áreas nativas, preferencias y backups, lectura de permisos reales, rutas centralizadas, AtomicFile/ContextStore y recuperación | SettingsNavigation; SettingsAccessSummary.kt:33; PermissionActivity; ContextStore; SettingsPersistenceTest; ContextRecoveryTest | Conceder/revocar y regresar de Ajustes del sistema en One UI pendientes. Shizuku/accesibilidad son opcionales; no sustituir grants por toggles ni migrar/borrar datos para recuperar UI |

## Qué está defectuoso, qué es riesgo y qué sigue sin comprobar

| Hallazgo | Clasificación y evidencia | Efecto / recomendación |
| --- | --- | --- |
| Títulos y detalles del menú se recortan; nombre del filtro depende visualmente del placeholder y foco poco distinguible | Defecto documentado: ActionPanelView de57 fuerza maxLines1/ellipsis. PR19 conserva fallo inicial al200%, XML/capturas antes/después y corrección. Se inspeccionó la comparación real, no un mockup | Adoptar selectivamente correcciones de PR19 en un bloque posterior; preservan identidad y ejecutores. El texto largo ocupa más altura: comprobar scroll/última acción antes de aceptar |
| BoxWithConstraints sin usar restricciones en SpotlightBar | Defecto de calidad documentado: informe Lint inicial del primer commit de PR19; sustitución por Box equivalente | Traer ese parche y sus pruebas cuando se implemente el bloque. No afirmar que hoy se ejecutó Lint sobre57 |
| Ante fallo, Action.InApp llama launch(action.fallback) automáticamente | Comportamiento constatado en SpotlightPanel.kt:491–498; launch también prueba destino preferido y luego original:336–354 | Puede cambiar de app/destino tras la revisión. Algunas rutas ya lo describen como respaldo web; no es prueba de fallo físico. Recomiendo convertir ese cambio en alternativa explícita y revisar el destino otra vez. Caso reproducible requerido antes de parchear |
| Mensajes de fallo heterogéneos | Deuda constatada: varios catch usan Exception.message; modelos y estados de capacidades ya existen | Unificar categorías/recuperación en un flujo sensible, manteniendo la causa sólo en diagnóstico seguro. No anunciar “completado” por REQUESTED; no crear un segundo motor |
| Respuestas tardías de la tarjeta de texto | Riesgo a reproducir: la publicación de card comprueba isCurrentRequest, su catch/error y finally/cardLoading no lo hacen (SpotlightPanel.kt:681–695) | Añadir en un siguiente bloque un caso de proveedor tardío/cancelado; sólo corregir si altera el estado de la consulta nueva. No se presenta como bug reproducido hoy |
| Spotify: cortes/autorización/estado real | Incidencia reportada en continuidad; SDK y fixes anteriores no prueban audio en57 | Reutilizar la APK57 ya entregada y recoger una reproducción concreta/estado antes de investigar. No recompilar ni cambiar SDK a ciegas |
| WhatsApp completo y citas paginadas uniformes | Limitación de alcance, no regresión probada: RemoteInput sólo notificaciones y extracción no siempre sabe página | Mostrar alcance real; conservar mecanismos existentes. No vincular cuentas ni activar proveedores no oficiales para “completar” una demo |

## Revisión selectiva del PR19

GitHub: [PR19](https://github.com/bauti3311-sys/Spotlight-Next/pull/19), borrador
abierto, HEAD `a622df75208e40826955e51cc84c65d7754f9f53`, base de57 exacta.
Dos commits: `39f5c35` y `a622df7`. Tres archivos productivos: ActionPanelView,
GlassTokens y SpotlightBar. No modifica ejecutores, permisos, identidad o datos.

Recomiendo recuperar luego: texto completo/encabezados legibles y semánticos,
nombre accesible estable del filtro, anillo de foco, borde por rol, recuperación
sin coincidencias y Box equivalente. Mantener pruebas de callbacks, teclado,
320dp/200%/RTL y escala compacta. Los contrastes medidos pertenecen a sus fixtures,
no a todos los fondos/transparencias de One UI.

Se leyeron sus XML: último bloque11 casos (4 nuevos+7 previos); barra4 casos en
una ronda anterior. No sumar rondas repetidas. Las capturas comparadas muestran
el texto antes recortado y después completo; el menú puede crecer y necesita su
scroll. [Comparativa histórica](https://github.com/bauti3311-sys/Spotlight-Next/blob/a622df75208e40826955e51cc84c65d7754f9f53/docs/verification/hallmark-jakub-preview57-20261102/README.md).
No se cherry-pickea todo el PR ni se instala Hallmark/React/Vue por esta revisión.
La nueva rama conserva el producto base sin modificaciones para este entregable.

## Propuesta concreta, por impacto

| Orden | Bloque independiente | Mejora comprobable / criterio de cierre | Costo/riesgo y qué preservar |
| --- | --- | --- | --- |
| P0 | Validar el recorrido habitual en la APK57 existente | Abrir overlay → buscar app real → Enter → regresar; PDF → seleccionar/scroll/abrir explícito; IME/rotación; Spotify seleccionar/reproducir/observar; WhatsApp revisar/cancelar. Registrar esperado/observado, permiso y mecanismo; envío real sólo con autorización concreta | Sin nueva compilación inicial. No cambiar datos/configuración ni conectar cuentas automáticamente. Separar problemas reproducidos de permisos/configuración faltantes |
| P1-A | Correcciones selectivas de PR19 | Títulos completos/foco y nombre accesible; última acción alcanzable en320dp/200%; callback correcto una vez; Tab/ShiftTab/Enter/Escape/CtrlF. Pruebas dirigidas y capturas del mismo estado | Tres componentes existentes; preservar paleta, geometría general y acciones. Inspección visual de crecimiento del menú; sin rediseño |
| P1-B | Contrato nativo de lanzamiento/resultado | Una app/capacidad elegida conserva destino hasta ejecutar; permiso revocado/app ausente genera error recuperable; fallback se ofrece para revisión y nunca se dispara solo. Tipos/categorías y receipt honestos, con fixtures de SecurityException, cambios y cancelación | Extender Action/AppCapabilities/IntegrationRegistry y ejecutor actual; sin bus nuevo, puente, servidor ni permisos. Caso rojo/verde focalizado antes de cambiar |
| P1-C | Estabilidad que se reproduzca | Primero una hipótesis del corte de Spotify o foco/IME; repetir sólo el caso afectado. Si el fallo es configuración, explicar esa causa en vez de reescribir el controlador | No gastar matrices CI ni nuevos builds idénticos. Mantener sesión oficial/MediaSession, sin fallback o reintento de reproducción tras efecto incierto |
| P2 | Formularios contextuales nativos (principios OpenUI) | Mejorar MessageEditor existente: dos Juan/dos números/Business, selección explícita, payload revisado inmutable, cambio de target invalida aprobación, restauración no envía, confirmación repetida no duplica | No instalar OpenUI. Primero flujo determinista y fixtures; mismos tokens/inspector. No mensajes ficticios ni inferencia para probar UI |
| P2 | Citas documentales locales antes de RAGFlow | Corpus consentido pequeño → consulta → fragmento exacto → título/fuente/huella → Quick Look; página sólo si TextFragment permite demostrarla. Revocar/borrar/cancelar invalida hit; servidor ausente no altera búsqueda | Reusar FileIndex/ExtractedDocument/DocumentEmbedder. Medir relevancia/latencia/tamaño antes de sumar infraestructura. No asumir que offset equivale a página |
| P3, condicional | Investigar proveedor remoto | Sólo si la evaluación local demuestra una brecha concreta: comparar solución local y adaptador RAGFlow; acordar endpoint/versión, datos salientes, costo, retención/borrado y consentimiento separado | No se configura ni implementa servidor/adaptador ahora; remoto opt-in, local siempre conserva funcionamiento. Consulta, subida y uso de LLM tienen consentimientos distintos |

Los primeros cambios de código recomendados son P1-A y P1-B, en checkpoints
separados con pruebas dirigidas. La verificación física P0 puede acompañarlos sin
bloquear trabajo local independiente; no declarar resuelto el audio o One UI por
un test de laboratorio. OpenUI y RAGFlow permanecen posteriores a esta auditoría.

## Decisiones técnicas para las tres skills

- **API & Interface Design:** usar Action sellada, ToolCall/ToolExecution,
  CapabilityStatus/ExecutionState y registros existentes. JSON versionado sólo en
  persistencia/límites externos que lo necesitan; sin importar el envelope v2 del
  puente de PR20 ni forzar un protocolo de mensajes dentro de Compose.
- **OpenUI:** una adaptación de principios (datos restringidos, formulario,
  validación, revisión y resultado), no un renderer instalado. Compose ya resuelve
  la representación; no React/Vue/Zod/JS ni otro prototipo visual.
- **RAGFlow:** primero cerrar citas y evaluación local. FileIndex ya tiene el
  trabajo costoso de permisos, índice y semántica. El proveedor remoto es una
  opción justificada por resultados, nunca un requisito ni una promesa activa.

Base de compatibilidad leída: AGP8.7.3, Kotlin/compiler2.0.21, Compose BOM2024.12.01,
Gradle8.10.2, Java17, min26, compile/target35; no se cambia ninguna versión,
dependencia, permiso, firma o identificador. No se genera nueva Preview ni APK.
