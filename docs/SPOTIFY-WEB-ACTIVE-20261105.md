# Spotify: reproducción explícita en dispositivo activo

Base remota verificada: `4546c55c7e7522dac0a6bb96f4009ca2cadc0be3`, rama
`feat/preview57-native-return-20261103`, PR #21. Checkpoint Focus B conservado.

## Cambio acotado

El buscador existente permite elegir «Reproducir en dispositivo activo» (Web API)
o «Reproducción local (App Remote)». La opción local anterior sigue seleccionada
inicialmente; no hay fallback. Elegir una ruta no reproduce nada. La confirmación
explícita ejecuta la ruta elegida. Focus, motor, ranking y ejecutores generales no
se modifican. No Square/Outify, dependencias, permisos Android ni firma nuevos.

La autorización PKCE existente ya solicita user-read-playback-state y
user-modify-playback-state. Ahora se comprueban los scopes realmente almacenados
y los renovados antes de permitir el control Web. Si faltan, requiere reconectar;
no amplía permisos ni inicia autorización automáticamente.

Se reutiliza SpotifyPlaybackClient.playSelection(): una lectura del estado vivo,
exige device.id válido, is_active=true e is_restricted=false, y como máximo un PUT
al device_id observado por confirmación. No pausa, transfiere ni reintenta.
Una compuerta atómica no encola confirmaciones concurrentes. La corrutina instala su limpieza antes de suspender, incluso ante cancelación inmediata. Cancelación libera la compuerta;
no promete deshacer un PUT que Spotify pudiera haber recibido.

«Solicitud aceptada» no confirma audio. «Comprobar reproducción» hace una lectura
Web explícita en esa ruta y compara URI/playing con la selección. No polling.
Si otro contenido sigue activo o está en pausa, lo informa. Es una observación de
la cuenta en ese instante, no una prueba de causalidad ni de audio audible.

## App Remote: investigación separada

El timeout local es el límite de 20 s de SpotifyLocalPlayer.authorize mientras
espera OfficialSpotifyRemote.open. Se conserva ese límite y la limpieza del enlace
tardío. El SDK oficial 0.8.0 utiliza showAuthView(true) y redirect fijo
http://127.0.0.1/spotify/callback. El paquete com.spotify.music ya está declarado
en queries. No hay prueba suficiente para atribuir el timeout a un defecto preciso.

Spotify documenta registrar Client ID, redirect, paquete y huella de la aplicación:
https://developer.spotify.com/documentation/android/tutorials/getting-started
La siguiente comprobación física acotada es contrastar esos datos exactos en el
Dashboard con la Preview instalada y obtener la clase de error/callback del SDK,
sin tokens ni secretos. Premium reproduciendo descarta que no haya audio iniciado,
pero no demuestra que el SDK haya autorizado ese paquete/huella. No se alarga el
timeout ni se cambian certificados, redirect, SDK o permisos por una hipótesis.

Web API requiere Premium y scope de modificación, según documentación consultada:
https://developer.spotify.com/documentation/web-api/reference/start-a-users-playback

## Límites

Pruebas HTTP sobre loopback y JVM no prueban la cuenta ni la tablet real. No se
leyeron tokens del usuario, no se controló su música y no se comprobó su Dashboard.
La guardia se ejecutará contra su dispositivo real cuando instale una futura APK
autorizada. Esta corrección no está en Preview64; no hubo Actions ni APK nueva.

## Verificación técnica

Comando dirigido: `:app:testDebugUnitTest --tests '*SpotifyPlaybackGuardTest'
--tests '*SpotifyPlaybackFailureTest' --tests '*SpotifyPlaybackClientTest'
--tests '*SpotifySessionTest' --tests '*SpotifyWebScopeTest'
--tests '*SpotifyLocalPlayerTest' :app:lintDebug`, con
`-I scripts/directed-tests.init.gradle`, `-PspotlightDirectedClasses=` restringido
a esas seis clases y `--no-daemon --max-workers=1 --console=plain`. Java17/SDK35
del entorno ya provisionado. No assemble ni despacho de CI.

Se conservan logs intermedios. Una invocación mal ordenada pasó --tests a lintDebug
y falló al configurar tareas, antes de ejecutar verificaciones; se corrigió el
orden de argumentos. No se alteraron pruebas ni aserciones por ese fallo.
La evidencia final sustituye la intermedia y no suma repeticiones.

Pruebas finales: 36 casos distintos correctos en seis clases: PlaybackGuard9,
PlaybackFailure5, PlaybackClient3, Session7, WebScope3, LocalPlayer9. Cubren
dispositivo activo/pausado/ausente/restringido o malformado, rechazos401/403/404/429
en lectura y envío, cancelación antes y después del PUT, doble confirmación
concurrente con un solo PUT, permisos ausentes o perdidos al renovar, observación
verdadera frente a aceptación y compatibilidad del recorrido App Remote anterior.
No se afirma una prueba táctil/instrumentada de la opción nueva ni reproducción real.

Lint final correcto: 0 errores, 26 advertencias y 1 información preexistentes.
Ejecución final completa: BUILD SUCCESSFUL; ver final-source.log y XML adjuntos.

## Commits publicados

- Cliente/scopes/compuerta: fbff9bfd9a4845f01e2fb3826736ee1cd3be3737.
- Integración y regresiones HTTP: 212368dff77391260bf60b7e1ea0c8a8ae219dc4
  (fuente ejecutable comprobada; el commit posterior sólo agrega continuidad/evidencia).
- PR21 sigue abierto/borrador; main47259ce y checkpoint integral e38eae8 intactos.
- Reserva propia actualizada en cada publicación; cierre mediante notas atómicas
  del commit administrativo final. Ver refs/notes/spotlight-autonomy.
