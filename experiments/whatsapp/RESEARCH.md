# WhatsApp personal: comparación técnica y decisión D26

Consulta: 24/10/2026. Base Spotlight: 4d868128; PR18 borrador. Snapshots exactos en upstream-snapshot.json. Se consultaron repos originales, README, LICENSE, manifests, contrato HTTP y documentación oficial Google/Android; no se ejecutó código de terceros ni se auditó todo su código.

## Decisión

Prototipar aislado. Para un servicio pequeño, preferir **whatsmeow/Go**; para esta prueba de interoperabilidad usar un adaptador al contrato de **WuzAPI**. No incorporar Node, Chromium, sesiones ni nuevos permisos al APK. El PoC Python de biblioteca estándar valida fronteras y protocolo HTTP; no es todavía un cliente WhatsApp ni una recomendación de mantener dos servicios en producción. Una implementación definitiva Go/whatsmeow podría evitar esa capa adicional después de validar el cliente.

| Proyecto | Mantenimiento observado | Licencia leída | Peso/estabilidad y Android ARM64 |
| --- | --- | --- | --- |
| [Baileys](https://github.com/WhiskeySockets/Baileys) | Repo push 21/10; rama principal commit 04/08; release v7.0.0-rc14 del 29/07/2026 | MIT | Node >=20, WebSocket sin Chromium. El README advierte cambios incompatibles de v7; el nombre RC no se convierte en estable porque GitHub marque prerelease=false. Dependencias nativas como whatsapp-rust-bridge; Android/Bionic y sus binarios no verificados. |
| [whatsmeow](https://github.com/tulir/whatsmeow) | Rama/push 17/10/2026; endpoint latest no devolvió release | MPL-2.0 | Biblioteca Go de multidispositivo con recibos y estado; hay que construir el servicio y su almacenamiento. HEAD declara Go1.26/toolchain1.27.2. La candidata con menor superficie controlable, no una cifra demostrada de RAM. Android ARM64 requiere compilación y gestión de proceso propias. |
| [WuzAPI](https://github.com/asternic/wuzapi) | Push/head 20/10; release v1.0.9 del 17/09/2026 | MIT | REST sobre whatsmeow, Go1.26 y SQLite modernc en el árbol consultado. Más superficie que la biblioteca: dashboard, medios y otras integraciones. Tokens por usuario, TLS configurable. Atención: bind por defecto 0.0.0.0; debe aislarse antes de ejecutarlo. ARM64 Android no probado. |
| [ChatRail](https://github.com/pedroantonnio/ChatRail) | Head/release v1.0.1 del 12/09/2026 | MIT, además de licencias de dependencias | Node>=20; proveedor habitual whatsapp-web.js con navegador; Baileys alternativo. API loopback/MCP y resolución LID interesante, pero no encaja como primera opción ligera. Chromium/Termux y dependencias Android sin validar. Su proveedor fake no demuestra WhatsApp real. |
| [Evolution API](https://github.com/evolution-foundation/evolution-api) | Push repo14/07; head06/05/2026; release2.3.7 publicada05/12/2025 | Texto Apache2.0 **con condiciones adicionales** de marca/aviso; detector GitHub NOASSERTION | Node+SQL y Redis recomendado, múltiples integraciones y telemetría documentada. Adecuado para plataforma externa mayor; excesivo para un único WhatsApp personal. No tratar como Apache2.0 sin modificaciones ni presumir despliegue Android ARM64 por existir imágenes Linux. |

Push del repositorio puede corresponder a otra rama o tag: no prueba una corrección en la rama principal ni estabilidad. No se verificó ausencia de vulnerabilidades. El desarrollo activo no equivale a auditoría de seguridad.

## Memoria, dispositivos vinculados y permisos

No hay benchmark comparable Android ARM64 realizado aquí. Comparación cualitativa: biblioteca Go acotada suele permitir menor superficie que plataforma Node+SQL; Chromium agrega procesos. Esta es una inferencia arquitectónica, no medición de RSS/PSS. Medir arranque, reposo con una sesión, reconexión, 10 mensajes de texto y manejo de medios por separado; tampoco extrapolar resultados Linux x86_64 a la tablet.

Los clientes Web no oficiales mantienen una identidad de dispositivo vinculado y claves de sesión propias. whatsmeow/Baileys permiten nuevos mensajes sin abrir la UI del teléfono principal después de vincular; no significa acceso oficial a la cuenta. Se debe verificar protocolo/cambios de WhatsApp, revocación y recibos por versión. No se vinculó ningún dispositivo en esta sesión.

Linux ARM64 no es Android ARM64: Bionic, objetivo GOOS, toolchain, políticas de ejecución de binarios, segundo plano y ciclo de vida requieren una prueba aparte. Docker no convierte un servicio Linux en un servicio Android nativo. Un companion/servicio externo autorizado es una ruta inicial más realista que meter Node en Spotlight. No se introdujeron permisos, servicios foreground, root, ADB ni accesibilidad nueva.

RemoteInput reutiliza únicamente una notificación actual con acción válida y acceso de notificaciones ya concedido/conectado; no permite elegir cualquier chat ni leer historial. Se revalidan paquete exacto, conversación y acción. Public Intent abre WhatsApp para revisión/envío y no acredita entrega. Los contactos necesitan autorización existente y selección inequívoca, nunca resolver un nombre ambiguo de forma silenciosa.

## Gemini y AppFunctions: límites oficiales

[Gemini/WhatsApp](https://support.google.com/gemini/answer/15574928?hl=en) permite mensajes desde la aplicación Gemini Android y sus Apps conectadas. No documenta una API que Spotlight pueda invocar con sus credenciales de Gemini API. No lee todo el historial: las respuestas a notificaciones dependen de Device assistance. Por ello, usar Gemini para redactar es distinto de obtener la capacidad de envío de Google. No se llamó a Gemini ni se transmitió texto personal.

[AppFunctions](https://developer.android.com/ai/appfunctions) está descrito como experimental, Android16+, con integración Gemini limitada a participantes seleccionados. [EXECUTE_APP_FUNCTIONS](https://developer.android.com/reference/android/Manifest.permission#EXECUTE_APP_FUNCTIONS) es normal, pero hay comprobación runtime de allowlist y requisitos adicionales; declararlo no concede acceso. No se descubrió aquí una función WhatsApp exportada y autorizada para Spotlight, ni se alteró compileSdk35. No se usarán APIs privadas de Google.

## Contrato de prueba WuzAPI

Pin: 042f96b65697c60556c086da1028cb36cfe64f5a. Se cotejó handlers.go del pin con el árbol consultado. [Middleware/send](https://github.com/asternic/wuzapi/blob/042f96b65697c60556c086da1028cb36cfe64f5a/handlers.go) y [rutas](https://github.com/asternic/wuzapi/blob/042f96b65697c60556c086da1028cb36cfe64f5a/routes.go): POST /chat/send/text, header Token, Phone/Body/Id y LinkPreview=false. Parte de API.md menciona Authorization, pero el middleware lee Token para el usuario. Se valida success y un Id en la respuesta. Ese resultado se muestra como ACCEPTED; no como DELIVERED/READ. Logout explícito usa /session/logout. Sin reintentos automáticos, ni llamadas de QR/login/contactos/grupos.

## Seguridad y aceptación pendiente

**Usar clientes no oficiales puede provocar bloqueo o suspensión de la cuenta**, incluso sin envío masivo. Respetar límites y confirmar manualmente no elimina ese riesgo. Antes de habilitar un cliente real, solicitar consentimiento específico para una cuenta de prueba; la principal sigue fuera de alcance.

No se almacenan sesiones en este repositorio. Token de usuario dedicado, sesiones/claves privadas en almacenamiento del servicio cifrado/protegido, token de administración fuera del APK, bind loopback y TLS autenticado si en otro dispositivo. La prueba no provisiona ni genera claves de WhatsApp; solo mantiene una bitácora privada de IDs/digests/estados. WhatsApp mantiene sus claves en el servicio externo elegido, cuya protección debe validarse por separado.

Pendientes: cliente real sin cuenta principal, pairing/revocación, Android ARM64, batería/RAM, eventos autenticados de entrega, adaptador Android HTTPS, compilación Kotlin y la UI completa. La revisión Gemini debe ser explícita por mensaje; todo cambio de texto/destinatario/proveedor cancela la revisión anterior. No exponer confirmación como herramienta IA/MCP. No afirmar exactamente una vez entre IDs distintos: tras UNKNOWN se reconcilia el estado antes de preparar otro envío.

Las prioridades de Preview47 siguen vigentes: expansión automática → Acciones de app en inspectores → recuperar respuestas → redacción/envío → 40 decisiones/Spotify/Cast. Este experimento no modifica la UI ni se anuncia como capacidad disponible en búsqueda. Matriz9/25/6 y Preview46 conservadas.
