# WhatsApp personal — experimento aislado, desactivado

No está incluido en el APK, no cambia manifest/dependencias, no añade una acción disponible a la búsqueda y no conecta ninguna cuenta. La comparación y pins están en [RESEARCH.md](RESEARCH.md). Las prioridades de Preview47 se mantienen.

## Qué se implementó y verificó

- Contrato `WhatsAppMessagingProvider` y tres rutas independientes.
- `NotificationReplyProvider`: adaptador Android a `Replies.send`/RemoteInput, solo notificación actual, paquete personal exacto y conversación revisada. El callback Python es únicamente un fixture de contrato, no ejecución Android. Resultado REQUESTED, no entrega.
- `LinkedDeviceMessagingProvider`: desactivado por defecto, transporte inyectado y consentimiento independiente. Adaptador Python real de solicitudes HTTP WuzAPI (texto y logout) probado con upstream loopback controlado, **no con una instancia WuzAPI real**. ACCEPTED requiere respuesta success e Id; nunca significa DELIVERED/READ.
- `PublicIntentMessagingProvider`: devuelve o abre la ruta pública wa.me dirigida a com.whatsapp; EXTERNAL_REVIEW, envío final manual en WhatsApp. No se elige automáticamente después de fallar otro proveedor.
- Confirmación ligada a ID, proveedor, destinatario y texto inmutables; nonce de un solo uso y 120 segundos. Cancelación y nueva revisión al editar. Una API por sí sola no prueba que hubo un humano: la confirmación debe venir exclusivamente de una UI confiable, nunca de Gemini/MCP.
- Bitácora SQLite con directorio0700/archivo0600, HMAC y reserva durable **antes** del efecto. Guarda IDs/digests/estados, no sesiones, tokens, teléfonos ni texto. Tras excepción/reinicio incierto queda UNKNOWN y no se reenvía. Límite10000 sin borrar automáticamente fences. La protección es por ID; no demuestra exactamente una vez para IDs nuevos ni controla todos los reintentos internos del cliente externo.
- Un destinatario E.164 explícito, texto1..8000, sin grupos/listas/LIDs inventados; cooldown5s y máximo20 borradores. Sin adjuntos ni sincronización masiva/historial.
- Servicio fixture solo127.0.0.1, bearer fuerte, rechazo Origin/Host ajeno, JSON acotado, cuatro conexiones y timeout; no logs de mensajes/credenciales. Upstream requiere HTTPS validado, sin usuario/contraseña en URL, sin redirects ni proxy implícito. No se expone al exterior.
- Desconexión deshabilita localmente aunque logout falle; se informa si la revocación remota no quedó verificada. Si es incierta, el usuario deberá verificar/desvincular desde WhatsApp antes de otra prueba.

## Ejecutar la prueba sin cuenta

Desde la raíz:

```bash
python3 -m unittest discover -s experiments/whatsapp -p 'test_*.py' -v
```

Resultado comprobado: **17/17**, ver `verification/python-tests.log`. Incluye HTTP real contra fixtures locales, auth, rechazo de redirects, confirmación/cambios de texto, duplicados concurrentes, fence de crash, reinicio, cancelación, logout fallido, no promoción de recibos de entrega no verificados y privacidad del journal. Las cifras no se suman a los96 casos Android históricos de Preview46.

Un demo fixture opcional se inicia con `python3 experiments/whatsapp/bridge.py --fixture` y una clave local fuerte en `SPOTLIGHT_WA_TOKEN`. No publicar esa clave ni introducirla en este chat. Sin ese flag el programa no arranca; el CLI **no puede activar/vincular una cuenta real**. Devuelve SIMULATED para su proveedor fixture. No se dejó servidor ejecutándose.

API fixture autenticada: POST /draft (provider, recipient, text), /confirm (id,digest,nonce), /cancel (id), /status (id), /disconnect (confirmed=true). El token y la confirmación nunca forman parte de herramientas del modelo. No hay rutas de QR/login/registro masivo. Ningún cliente externo se instaló o ejecutó.

## Preparación Android, todavía fuera de producción

`android/WhatsAppMessagingProvider.kt` está **fuera de app/src**. Contiene revisión humana, fence privado durable, RemoteInput real reutilizable, Intent público y transporte vinculado abstracto desactivado. No contiene Node, credenciales o una sesión WhatsApp. Es un prototipo de integración: **no se compiló ni se ejecutó Android en esta sesión**; mantenimiento retiró SDK/Gradle y no se repitió su recuperación. El adaptador HTTPS Android y la pantalla completa aún no se implementaron. No copiar este archivo a producción ni anunciar la capacidad sin su gate dirigido.

Flujo futuro en Acciones de app: destinatario autorizado e inequívoco → texto → revisión Gemini explícita y autorizada → cancelar borrador anterior si cambia → mostrar texto/destinatario/proveedor exactos → confirmación humana → submit → estado real. Editar, restaurar, recibir texto de Gemini o descubrir un contacto nunca ejecuta un envío. Solicitudes naturales existentes deben preparar el formulario; no activar el puente por interpretar una frase.

Antes de activar un cliente vinculado: consentimiento específico para cuenta de prueba y riesgo de bloqueo, cliente/pin auditado, almacén de sesiones protegido/cifrado, token de usuario dedicado, transporte autenticado, Android ARM64/lifecycle/RAM medidos, revocación real y eventos de entrega autenticados/correlacionados. No se conecta la cuenta principal con esta autorización. Los recibos DELIVERED/READ permanecen sin implementar hasta esa prueba. AppFunctions/Gemini de Google no se usan como un atajo no autorizado.
