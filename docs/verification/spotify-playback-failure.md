# Spotify Playback — diagnóstico pendiente del corte físico

Reporte del usuario: al confirmar una canción desde Spotlight se corta la música que ya estaba sonando en Spotify. La causa física NO está reproducida ni resuelta.

Defecto independiente confirmado: el cliente descartaba el campo de error del servidor. Una regresión contra c3347229 falló 1/1 al esperar el motivo explícito NO_ACTIVE_DEVICE. El fix d4045154 conserva solamente ese identificador reconocido, con lectura limitada a 4096 bytes; no expone texto privado ni interpreta un 404 desconocido como dispositivo inactivo.

Validación en Codespace persistente: 20/20 casos, cero fallos, errores u omisiones; BUILD SUCCESSFUL en 6m7s. Grupos: SpotifyPlaybackFailureTest 5, SpotifyPlaybackClientTest 3, SpotifySearchClientTest 6, SpotifyIntegrationFlowTest 2, SpotifyOverlayRecoveryRegressionTest 4. Se comprueba una única petición PUT play con URI tipada, sin pausa ni recuperación/reintento automático adicional en el fixture. Esto no valida reproducción en una cuenta real.

Evidencia conservada en /workspaces/.spotlight-tools/spotify-playback-failure: red.log/red-results, green.log/green-results y fixture original. La primera preparación del green se detuvo antes de ejecutar Gradle por una diferencia de salto de línea; se preservó el archivo local y se comprobó la equivalencia antes del fast-forward. El control del navegador tuvo timeouts intermitentes; la ejecución fue recuperada, sin duplicarla.

Preview 50 se prepara como diagnóstico, con el mismo paquete y certificado; no debe presentarse como solución comprobada del corte. Sin cambios de reproducción, scopes, permisos, tokens, firma, diseño o preferencias. No se usaron GitHub Actions. Preview 49 y matriz 9/25/6 conservadas. Compilación/lint Raycast y entrega de Preview 50 pendientes.
