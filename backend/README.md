# Backend privado de referencia · 0.3.0

Servidor Python de biblioteca estándar, enlazado SOLO a 127.0.0.1:8787. Requiere un proxy HTTPS y acceso autenticado privado (por ejemplo VPN privada) antes de usar desde la tablet. No incluye OAuth ni autenticación pública: no publicar directamente en internet. Las claves se definen en el entorno del servidor; no se incluyen en el ZIP/APK.

- POST /chat: {messages:[{role,content}]} → SSE {delta}; OpenAI Responses.
- POST /transcribe: {name:"audio.mp3",audio:"base64"} → {text}; audio hasta 20 MB. Requiere OPENAI_API_KEY + OPENAI_TRANSCRIBE_MODEL. La app pide confirmación para cada envío.
- GET /live?type=flight&q=AR1301: conector Aviationstack. Requiere AVIATIONSTACK_API_KEY y una cuenta/plan que permita consultas HTTPS y los campos buscados.
- GET /live?type=sports&q=Boca: fútbol mediante API-FOOTBALL. Requiere API_FOOTBALL_KEY; la cuenta debe permitir equipos, partidos, tablas y estadísticas. Otras disciplinas necesitan otro conector.

Las consultas de vuelos y deportes devuelven {title,source,updated_at,body,url,rows:[{label,value}]}. Ausencia de claves, datos o permisos se informa como error; nunca genera vuelos o marcadores ficticios. No se ha conectado una cuenta ni desplegado este servidor.

Configurar en Opciones: IA https://tu-host-privado/chat; datos en vivo https://tu-host-privado/live. El audio usa /transcribe en el mismo host. No poner claves en las URLs. La IA/API tiene facturación separada de ChatGPT Plus.

Pruebas locales sin llamadas a servicios reales:

    python3 -m unittest discover -s backend -p 'test_*.py'

Referencias oficiales:
- https://developers.openai.com/api/reference/resources/audio/subresources/transcriptions/methods/create
- https://aviationstack.com/documentation
- https://www.api-football.com/documentation-v3

El mapa abre Maps/navegador con la posición del proveedor si existe. No hay un mapa de seguimiento propio ni garantía de cobertura mundial o actualización por segundo.
