# Skills, auditoría y checkpoints independientes

## Estado verificado antes de editar

Base publicada de la integración Nuxt/WebView: `dbfcf81858240964812c5977612fbca86fc0d2f6`,
`feat/nuxt-webview-recovery-20261028`. No se identifica una Preview59 por su número.
La entrega58 documentada usa otra fuente: `8c7128e75399ea4518e2df759a8a4caeab2e2073`.
Main sigue en `47259ce2d7259f0ddfa5d85b7c73f07983d8cd6d` al inspeccionar GitHub.
El PR19 y su checkout `feat/preview57-unslop-20261102` se conservan separados.

Se revisaron INSTRUCCIONES-CODEX.md, PROMPT-PARA-CODEX.md y los nueve archivos
del ZIP antes de extraer. Las tres skills y sus referencias se instalan byte por
byte en `.agents/skills/`, sin sobrescribir archivos existentes. No hay scripts
ejecutables en el ZIP. Reserva adquirida normalmente después de verificarla libre;
trabajo aislado en `feat/webview-contracts-20261102`.

La instrucción actual del propietario autoriza continuar la interfaz Nuxt/WebView
del prototipo; prevalece sobre la restricción histórica de Compose exclusivo.
El motor privilegiado sigue siendo Kotlin y se conserva la recuperación Compose.

## Auditoría conjunta

| Área | Estado real | Riesgo o decisión |
| --- | --- | --- |
| OverlayService | Aloja LocalRaycastWebView sólo en `.webviewprototype`; conserva recuperación Compose | No reemplazar la UI de las variantes anteriores |
| Transporte | Recursos locales en `https://spotlight.local`, red/file/content bloqueados, MessagePort hacia origen exacto | Mantener un solo puente; revisar ciclo de vida y paquetes malformados |
| Contrato anterior | v1 con id/op, respuestas sólo para búsqueda/error | Sin confirmación de apertura, selección nativa ni deduplicación de efectos |
| Búsqueda | SearchEngine, scope de apps; tokens opacos por consulta | Preservar proveedores locales, cancelar e invalidar tokens antiguos |
| Ejecución | Kotlin comprueba componente launcher instalado antes de startActivity | Consumir capacidades antes de solicitar el efecto, no reintentar automáticamente |
| Nuxt | Vue recibe filas reales; sin host no inventa apps | Validar respuestas, correlacionar errores, invalidar UI desde el primer cambio de consulta |
| Documentos | FileIndex/OCR/SAF, Quick Look, DirectAi y búsqueda semántica local existentes | FileIndex ya conserva vectores/fragmentos, permisos y cancelación: no duplicarlos ni transmitir documentos por cada pulsación |
| OpenUI | No instalado | La fase1 no requiere otro renderer ni gateway |
| RAGFlow | No instalado/configurado | Sólo planificación; no servidor ni ingestión implícita |

## Matriz de compatibilidad y límites

| Componente | Verificación | Resultado / siguiente comprobación |
| --- | --- | --- |
| Android | minSdk26, compile/target35, AGP8.7.3, Kotlin2.0.21 | Mantener versiones; MessagePort de plataforma está disponible desde API23 |
| AndroidX WebKit | No es dependencia del proyecto | WebViewAssetLoader/addWebMessageListener son alternativas a evaluar; no se añade una librería para duplicar el transporte ya restringido |
| Nuxt/UI/Vue | Lock existente: Nuxt4.1.3, NuxtUI4.0.1, Vue3.5.22 | No migrar ni actualizar en bloque |
| Gestor/build | npm/package-lock, generate estático y assets de variante versionados | Regenerar assets sólo al cambiar código frontend y comprobar que son el mismo protocolo |
| OpenUI Vue | Registro npm consultado: @openuidev/vue-lang0.4.2, MIT, peer Vue>=3.5.0, Zod^3.25.0 o ^4.0.0 | Vue satisface el rango; falta revisar tipos Renderer y dependencias empaquetadas en checkpoint2. No asumir paridad con React1.0 |
| Zod | Registro consultado: latest4.6.7, MIT | Información, no instalación ni selección de versión. Verificar convivencia con lock antes de añadir |
| WebView real | Motor Chromium depende del dispositivo | APIs de navegador, IME, foco, memoria y tiempos requieren dispositivo; Chromium de laboratorio no certifica Android |
| Tamaño | No se genera APK nueva en esta tarea | Medir bytes de assets antes/después; impacto de OpenUI en APK y apertura pendiente para checkpoint2 |
| RAGFlow | REST opcional, servidor sin configurar | Confirmar versión desplegada, endpoints y licencia antes de un adaptador real; no adivinar campos de página |

## Checkpoint 1 — contrato Kotlin ↔ WebView (implementar ahora)

Envelope v2 con `v`, `sessionId`, `requestId`, `type`, `payload`; respuestas
correlacionadas con `status` y `data` o `error.code/message`. Migración atómica de
Kotlin, cliente y assets: v1 queda rechazado, sin canal paralelo de privilegios.
Operaciones reales previstas: search.query/cancel, result.inspect, action.execute
(open/info), overlay.close/recover. Prepare/confirm y documentos no se simulan.

IDs monotónicos por sesión, validación de forma/tamaño, caché acotada y rechazo
de IDs antiguos incluso tras expulsión de caché. Una repetición no vuelve a
ejecutar. Cada apertura consume el token nativo de resultado; una respuesta
`requested` no afirma que otra app terminó una tarea. Cambiar consulta, reconectar
o cerrar invalida selecciones y trabajo anterior; no hay reintento de efectos.

Aceptación: app registrada como instalada en Android de prueba → búsqueda por
motor real → token nativo → selección sin apertura → Enter/apertura explícita
una sola vez. Pruebas Kotlin de mensajes y permisos/targets, más navegador real
para Vue, teclado, selección, scroll y respuestas fuera de orden. Ambos niveles
se informan separados; falta de prueba física no se presenta como validación móvil.

## Checkpoint 2 — formulario contextual (sólo planificado)

Revisar docs y tipos fijados de Renderer Vue0.4.2, Zod, licencias y peso. Primera
superficie determinista y optativa con destinatario, texto editable, Cancelar y
Confirmar; sin IA ni envío. Reusar los tokens Raycast existentes y eventos del
checkpoint1. Kotlin resolverá permisos/destino y prepare→review→confirm antes
de habilitar una acción externa. Si Vue renderer no conviene, usar formulario
Vue fijo con la misma estética, explicando la evidencia. Flag reversible.

Aceptación independiente: cancelación no envía; destino ambiguo no confirma;
payload inválido no ejecuta; capturas equivalentes teléfono/tablet, 85–115%,
teclado y scroll; tamaño de assets/APK y apertura medidos, sin resultados ficticios.

## Checkpoint 3 — recuperación documental (sólo planificado)

Auditar FileIndex antes de definir un proveedor para no duplicar registros.
La inspección confirmó `semantic_vectors`, `DocumentEmbedder` local,
`semanticSource`, epoch de cancelación y extracción estructurada: reutilizar
esos fragmentos y su validación de fuentes como primera alternativa local.
Mantener búsqueda local y Quick Look sin Internet. Proveedor de prueba con PDF
no privado: fragmento exacto, título/ID, origen local/remoto y página nullable;
una página desconocida no se inventa. URI/grants permanecen en Kotlin.

Matriz de privacidad: archivo seleccionado y grant SAF → extracción local;
transmisión sólo con consentimiento explícito por documento → endpoint/dataset
aislado, retención/borrado documentados y revocación efectiva. Sin configuración
remota no sale contenido. Credentials fuera de Nuxt/localStorage/fuentes.
Un servidor o endpoint real requiere autorización/configuración separada.

Aceptación independiente: fixture→fragmento→cita comprobable; sin coincidencias
lo declara; cancelación/revocación/borrado descartan resultados; no consultas
remotas por pulsación ni reintentos infinitos. No Docker/Elasticsearch/modelos
de servidor en Android ni instalación de RAGFlow en este entorno.

Las fases2/3 permanecen planificadas hasta la prueba vertical del checkpoint1.
No se cambia estética, Spotify, WhatsApp o PDF por introducir estas skills.
