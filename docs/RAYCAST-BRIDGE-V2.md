# Contrato local Raycast ↔ Kotlin, v2

Transporte único: MessagePort transferido por Android al documento principal
`https://spotlight.local/`. Recursos empaquetados, navegación/red/file/content
bloqueados, sin addJavascriptInterface ni claves en JavaScript. Cada carga del
documento rota la sesión, cancela búsqueda, cierra el puerto anterior e invalida
tokens. Su callback comprueba puerto y sesión actuales. Sin caché HTTP del puente.

El plugin acepta un hello con tres campos (`v:2`, `type:"bridge.ready"`,
`sessionId`) y un puerto, sólo en el documento local y desde el origen/fuente
permitidos. Android puede entregar un evento nativo sin Window/origen de una
página; no se aceptan mensajes de una Window/iframe ajena. La política CSP también
prohíbe frames y conexiones remotas. No se trata el identificador de sesión como
un secreto o mecanismo de autenticación frente a scripts locales comprometidos.

## Envelope

```json
{"v":2,"sessionId":"session-from-hello","requestId":"1","type":"search.query","payload":{"text":"spotify","limit":100}}
```

Son exactamente cinco campos: versión entera2, sesión vigente, ID decimal positivo
canónico en string (`1..2147483647`), tipo de hasta40 caracteres y objeto payload.
Máximo8192 bytes UTF-8 y8192 unidades UTF-16 por petición; texto de búsqueda hasta
256 unidades UTF-16. No se convierten booleanos, strings o decimales a enteros.
Campos desconocidos o payloads con claves diferentes de las de la tabla se rechazan.

```json
{"v":2,"sessionId":"session-from-hello","requestId":"1","type":"search.query","status":"ok","data":{"items":[{"id":"opaque-native-token","label":"Spotify","suffix":"","type":"Application","icon":""}]}}
```

```json
{"v":2,"sessionId":"session-from-hello","requestId":"4","type":"action.execute","status":"error","error":{"code":"STALE_RESULT","message":"Search again to refresh this result"}}
```

Respuestas: exactamente seis campos. `status` es `ok` con objeto `data`, o `error`
con sólo `code/message`. Se conserva requestId y tipo. Si el envelope no permite
identificar una petición válida, se emite `bridge.error`; el ID vacío no se usa
para completar ninguna promesa del frontend. El cliente ignora JSON malformado,
IDs desconocidos, otra sesión, tipo incompatible o datos inválidos: no alteran
el resultado vigente. Errores de búsquedas antiguas tampoco pisan el estado actual.

El cliente limita respuestas a2MiB UTF-8/UTF-16,100 filas, IDs ASCII opacos de
hasta64 caracteres sin duplicados, label/suffix hasta256 y sólo iconos PNG
data-URI de hasta16384 caracteres (o string vacío). No carga iconos HTTP/SVG del
mensaje. Vue escapa las etiquetas; ningún resultado se interpreta como código.

## Operaciones reales

| type | payload exacto | data de respuesta / efecto |
| --- | --- | --- |
| search.query | text:string, limit:int1..100 | items: filas de SearchEngine en scope apps; sólo launcher activities explícitas |
| search.cancel | {} | {}; cancela trabajo, invalida selección y capacidades |
| result.inspect | target:string hasta64 | selected:token; no inicia ninguna Activity |
| action.execute | target:string, action:"open" o "info" | state:"requested"; sólo la selección nativa vigente |
| overlay.close | {} | {}; callback de cierre existente |
| overlay.recover | {} | {}; callback de recuperación Compose existente |

`action.prepare`, `action.confirm`, formularios, documentos y cualquier operación
no implementada devuelven UNSUPPORTED; no se simulan capacidades futuras.

## Orden, repetición y ciclo de vida

- Cada sesión usa IDs crecientes. La caché acotada de128 peticiones retiene la
  respuesta pequeña de un efecto; repetir ID/payload no vuelve a despacharlo.
  ID reutilizado con contenido diferente se rechaza. El máximo ID visto impide
  reejecución incluso después de expulsar una entrada. Un duplicado aún pendiente
  espera la respuesta original, sin comenzar otra operación.
- Las respuestas grandes de búsqueda no se retienen en la caché; repetirlas
  exige una búsqueda nueva y no resucita capacidades antiguas.
- Una búsqueda nueva cancela la anterior y usa una generación para impedir que
  trabajo de iconos antiguo publique resultados. El cliente invalida filas desde
  el cambio de texto, antes del debounce de45ms, y envía cancelación nativa.
- Inspect confirma sólo un token de la búsqueda nativa actual. Execute vuelve a
  validar selección y consume el token antes de solicitar el efecto. Se comprueban
  paquete instalado, actividad enabled/exported y launcher MAIN/CATEGORY_LAUNCHER.
  El frontend nunca proporciona paquete, componente, Intent o URI arbitraria.
- `requested` significa que startActivity aceptó la solicitud, no que la app
  externa terminó una acción. Permisos/indisponibilidad tienen errores explícitos.
  No se reintenta automáticamente una ejecución ni se restaura un token consumido
  tras fallo, timeout o resultado desconocido.
- Timeout nativo de consulta:5s; espera del cliente:8s; máximo64 peticiones cliente
  pendientes. Reconexion/unmount resuelve y retira las pendientes, sin replay.
  Respuestas de otra sesión o completadas tras dispose se descartan.

## Errores

INVALID_INPUT, UNSUPPORTED, PERMISSION_REQUIRED, STALE_RESULT, CANCELLED, TIMEOUT,
UNAVAILABLE e INTERNAL. Mensajes constantes, sin stacks, credenciales ni mensajes
privados de excepciones. Los errores se correlacionan antes de modificar la UI.

## Compatibilidad y prueba

v2 es una migración atómica del host, el cliente y los14 assets. v1 falla cerrado;
no se mantiene un segundo executor legacy. Cambios futuros deben conservar estos
límites y rechazar capacidades desconocidas; un cambio incompatible requiere
otra versión y prueba de migración. Ninguna operación futura se habilita por el
mero hecho de aparecer en OpenUI o una respuesta de RAGFlow.

Prueba vertical por capas: Vue/Chromium emite contratos reales sobre MessagePort
de laboratorio; Kotlin/Robolectric consulta el motor real con un paquete instalado
en el sistema de prueba y solicita su Activity exacta una sola vez. Un puerto de
registro exclusivo de tests observa paquetes nativos y errores de permiso.
Esto no sustituye una prueba end-to-end en WebView/WindowManager del dispositivo.
IME, rotación, teclado físico, latencia y memoria físicos quedan pendientes.
