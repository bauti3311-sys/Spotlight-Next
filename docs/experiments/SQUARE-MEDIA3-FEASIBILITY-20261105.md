# Square — experimento aislado, sin integración productiva

## Resultado: viabilidad estática condicionada; conexión física no demostrada

Se consultó Lelonio/Square v2.8.0 (release03/11/2026), commit
6d3d47cfcd4d236e5fa144376f9558662ae6dba1. Paquete dev.lelonio.square.
El repositorio declara GPL-3.0. No se copió código/binarios/engine de Square a
Spotlight, no se incorporó librespot ni se sustituyó Spotify oficial.
La fuente externa sólo se leyó en scratch para esta auditoría.

Hechos revisados en la fuente exacta:

- PlaybackService es MediaLibraryService exportado, sin permiso de binding
  declarado, con acciones MediaLibraryService y MediaBrowserService legacy.
- onGetSession devuelve la sesión; MediaBrowseTree.onConnect acepta controladores
  y añade los comandos estándar de biblioteca. No se encontró una allowlist de
  Android Auto, paquete/firma del caller o trusted-only en esos dos callbacks.
- onSearch resuelve la búsqueda y notifica el tamaño; onGetSearchResult pagina
  los resultados. La búsqueda usa la Web API de Spotify del propio Square: su
  documentación interna exige aplicación registrada por el usuario. Fallos pueden
  convertirse en lista vacía. «Square autenticado» no prueba búsqueda disponible.
- onSetMediaItems resuelve el MediaItem de biblioteca y puede expandir la cola.
  El consumidor debe conservar el item/mediaId devuelto, no reconstruir una URI.
  Esa expansión pertenece a Square; Spotlight no debe replicarla.
- El servicio crea jugador/sesión y contiene el arranque del engine; el recorrido
  revisado no llama startActivity para cada reproducción. setSessionActivity
  prepara el PendingIntent del tap de la notificación; eso no lo ejecuta.
- Square declara Media3 1.11.1, AGP8.13.2/Kotlin2.3.21. No se trasladan sus versiones
  ni dependencias al proyecto. Compatibilidad binaria/protocolo con un cliente
  Media3 de Spotlight no probada.

## Ensayo pequeño ejecutado

SquareProtocolFixtureTest vive EXCLUSIVAMENTE en app/src/test. Código original
con una interfaz de laboratorio, sin Android/Binder/Media3 importados ni nueva
dependencia. Seis casos verifican el orden conceptual search/getSearchResult,
selección explícita del item original, disponibilidad de comandos, set/prepare/play
una vez, cancelación/error y liberación finally. La solicitud no cambia por sí
misma el estado observado del fixture; otro item no confirma la selección.

**No se compiló un cliente Media3 ni se conectó a Square real.** Es un fixture del
contrato, no un sustituto de MediaBrowser, una prueba Binder ni reproducción real.
No hay acción Square en la barra/registry, datos inventados en UI, cambios Gradle,
permisos, servicios o código productivo Square. Las seis pruebas no cierran la
aceptación de conexión externa.

## Siguiente prueba, sólo si se decide continuar este experimento

Cliente de laboratorio, no adaptador productivo ni otro framework:

1. Resolver paquete/versión y servicio exacto
   dev.lelonio.square.playback.PlaybackService, habilitado/exportado. Comprobar
   visibilidad Android; Spotlight ya consulta apps launcher, pero no se presume
   suficiente en un dispositivo no observado. No añadir permisos/queries ahora.
2. Crear SessionToken con ese ComponentName; MediaBrowser.Builder.buildAsync en
   hilo/lifecycle apropiados. Conservar future y usar MediaBrowser.releaseFuture
   también si se cancela antes de conectar. Sin startActivity ni servicio nuevo
   de Spotlight. Liberar el cliente no promete detener el servicio propio de Square.
3. Comprobar comandos LIBRARY_SEARCH/GET_SEARCH_RESULT y observar resultado/callback
   de search antes de pedir página0/20. Vincular respuestas a consulta y generación;
   no buscar por tecla ni mostrar resultados tardíos. Error/lista vacía no acredita
   cuenta configurada. No extraer credenciales de Square ni reutilizar las de Spotlight.
4. Revisar item elegido; comprobar otra vez comandos SET/CHANGE_MEDIA_ITEMS,
   PREPARE y PLAY_PAUSE. setMediaItem con el item exacto, prepare/play una vez.
   No fallback de interfaz, reconexión con replay ni copia de la lógica GPL.
5. Player.Listener permite consultar cambios de MediaSession. El estado inmediato
   de un MediaController puede enmascarar cambios locales pendientes: una URI
   recién asignada o un click NO demuestran reproducción remota. Contrastar eventos
   posteriores/identidad/playing/error y, en prueba física consentida, la sesión
   real y audio. No declarar «reproduciendo» por valores optimistas.
6. Liberar listener/future/browser en salida, cancelación, denegación y error.
   Si la instalación rechaza el caller o exige UI por cada reproducción, registrar
   ese resultado y cerrar el experimento, sin bypass ni ampliar permisos.

Falta dispositivo con Square instalado, autenticado y búsqueda configurada. No se
pidió acceso a cuentas ni se ejecutaron envíos/reproducciones reales. Incorporar
Media3 como dependencia productiva y la integración de UX requiere aprobación
separada. Esta investigación termina aquí y NO bloquea C3.

## Fuentes primarias consultadas

- [Release2.8.0](https://github.com/Lelonio/Square/releases/tag/v2.8.0)
- [Manifest fijado](https://github.com/Lelonio/Square/blob/6d3d47cfcd4d236e5fa144376f9558662ae6dba1/app/src/main/AndroidManifest.xml)
- [MediaBrowseTree fijado](https://github.com/Lelonio/Square/blob/6d3d47cfcd4d236e5fa144376f9558662ae6dba1/app/src/main/java/dev/lelonio/square/playback/MediaBrowseTree.kt)
- [PlaybackService fijado](https://github.com/Lelonio/Square/blob/6d3d47cfcd4d236e5fa144376f9558662ae6dba1/app/src/main/java/dev/lelonio/square/playback/PlaybackService.kt)
- [Versiones fijadas](https://github.com/Lelonio/Square/blob/6d3d47cfcd4d236e5fa144376f9558662ae6dba1/gradle/libs.versions.toml)
- [Android: conectar MediaBrowser/MediaController](https://developer.android.com/media/media3/session/connect-to-media-app)
- [Android: MediaLibraryService](https://developer.android.com/media/media3/session/serve-content)
