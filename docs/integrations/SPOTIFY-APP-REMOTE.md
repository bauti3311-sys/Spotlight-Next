# Reproducción local oficial

SDK App Remote 0.8.0, fuente Spotify fijada en 5aa4d62465f61a0677081ae9a3108177d0365fc3; AAR Git blob ec49aad33b631e3b1066be6ccecb2da528fcc9b9 (132749 bytes), manifiesto revisado: minSdk21, queries de Spotify, sin permisos nuevos. Licencias en licenses/Spotify-App-Remote-*.

La búsqueda conserva OAuth PKCE. Reproducción seleccionada y observación usan IPC oficial al cliente instalado, sin exigir un reproductor Web API ya activo. La autorización inicial se solicita explícitamente desde Ajustes. Registrar paquete y huella SHA-1 del certificado en Spotify Dashboard. Spotify debe estar instalado y con sesión iniciada. Android o Spotify pueden impedir conexión tras un cierre forzado; no se promete eludir restricciones del sistema.

No hay fallback automático a HTTP, transferencia, pausa previa ni reintento de reproducción. La aceptación del SDK no equivale a audio verificado físicamente. Las pruebas de controlador usan fixtures; la integración real requiere prueba en tablet/cuenta autorizada. No se conectó ninguna cuenta en desarrollo.

Interfaz: filas planas seleccionables de 48dp, selección neutra, semántica accesible y bloqueo durante solicitudes. Aplicación local de las skills Compose de Chris Banes y native craft, conservando Solid Graphite.
