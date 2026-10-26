# Preview 50 — diagnóstico Spotify, entrega verificada

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

## Entrega privada verificada

[APK, ZIP fuente, evidencia y SHA256SUMS](https://github.com/bauti3311-sys/Spotlight-Next/releases/tag/untagged-91aa708fe2bc7c721410)

Se recuperó el acceso al mismo Codespace mediante el navegador de Work. Se comprobó ausencia de publisher activo, log previo, metadatos y release50; se verificaron de nuevo los hashes locales y se ejecutó una única publicación bajo flock, sin repetir build ni pruebas. Exit0 y seis assets uploaded. Los hashes SHA256 de GitHub coinciden con APK, fuente y evidencia locales.

El bloqueo original por timeouts y Debugger unattached queda como historial resuelto. Archivos, caches, firma y evidencias permanecen en /workspaces/.spotlight-tools/delivery50 y evidence50; no se expusieron credenciales. El publisher valida repositorio privado y protege las cabeceras de autorización fuera del ZIP.

**Preview 50 es diagnóstica: el corte de audio real sigue pendiente de causa y solución.** No hubo operaciones en una cuenta Spotify real ni validación física. El siguiente intento del usuario puede mostrar NO_ACTIVE_DEVICE solo si Spotify lo informa explícitamente; un 404 sin motivo reconocido sigue sin diagnóstico.

Preview49 conservada, PR18 en borrador y matriz9/25/6 intacta. Sin Actions, nuevas funcionalidades, scopes, permisos, cambios de firma o preferencias. APK/ZIP siguen siendo del SHA965646a9; este checkpoint posterior solo actualiza evidencia.
