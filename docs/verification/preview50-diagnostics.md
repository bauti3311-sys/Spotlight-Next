# Preview 50 — diagnóstico Spotify, entrega pendiente

**El corte de audio reportado al confirmar reproducción NO está reproducido ni resuelto.** Activar Spotify primero no resolvió el caso del usuario. No se operó una cuenta real.

Se confirmó y corrigió un defecto independiente: el cliente descartaba el motivo del error de Spotify. Una regresión contra c3347229 falló 1/1. El fix d4045154 reconoce únicamente NO_ACTIVE_DEVICE explícito, lee como máximo 4096 bytes y no muestra texto privado. Un 404 desconocido queda expresamente sin diagnóstico. No se cambiaron órdenes de reproducción, pausas ni reintentos automáticos.

## Verificaciones efectivamente terminadas

- 20/20 casos dirigidos, sin fallos, errores u omisiones; 6m7s.
- Grupos: PlaybackFailure5, PlaybackClient3, SearchClient6, IntegrationFlow2, OverlayRecovery4.
- Compilación Raycast Preview con R8 y lint completos: BUILD SUCCESSFUL en 16m4s; lint 0 errores / 14 advertencias.
- Código Kotlin probado: d40451548b6c76df8b780d377b2359d03d58ab38.
- Fuente exacta APK/ZIP: 965646a9c53717075708c81a885556b2f02fd374; solo incrementa versión Raycast y añade evidencia después de las pruebas.
- Paquete dev.bautista.spotlight.next.raycastpreview, versionCode50, no debuggable.
- Certificado histórico SHA256: 885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba.
- apksigner y zipalign -c -P16 4 correctos; ZIP creado por git archive del mismo SHA, sin archivos de firma.
- APK SHA256: f8cef5bee6553e5d9d6e411271b189dcbea3e7d9d0fc3d40e7fb7de258efac6f
- Fuente ZIP SHA256: 956af45d7f6b33906ade76ce809f6b1428a6a13ac8f113ef64e0d777c542be29
- Evidencia ZIP SHA256: 76e85e81a490bc21b11d304e98d0c6f596606e4ec1452f6f768f0332535a25e1

## Bloqueo de entrega y continuidad

Los archivos están en `/workspaces/.spotlight-tools/delivery50`. Logs, XML y verificadores en `/workspaces/.spotlight-tools/evidence50`. La carpeta `/workspaces/.spotlight-tools/spotify-playback-failure` conserva también el fixture previo y el fallo original.

**No se creó una release ni se subieron los archivos.** El control del navegador devolvió timeouts y finalmente `Debugger unattached`, incluso al recuperar la pestaña conocida. El intento de publicación no apareció en la terminal y la API confirmó ausencia de Preview50. No afirmar entrega ni parada del Codespace: su detención no pudo verificarse. No se recompiló ni se lanzó Actions.

Al reconectar: primero inspeccionar procesos, `publish50.log`, `release-metadata.json` y releases remotas para evitar duplicados. Revisar el publisher existente (puede tener un guard preparado de flock/metadatos; no se confirmó su ejecución). Si no empezó la publicación, ejecutar el publisher existente, verificar hashes del servidor y luego detener el Codespace. No reconstruir APK/ZIP. El checkout puede seguir en 965646a9c53717075708c81a885556b2f02fd374; este commit posterior contiene solo evidencia.

Preview49 conservada, PR18 en borrador, matriz9/25/6 intacta. No se modificó firma, preferencias, scopes ni permisos. Falta prueba física y obtener el motivo del rechazo de reproducción en la cuenta del usuario.
