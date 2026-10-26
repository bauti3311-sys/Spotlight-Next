# Preview 49 — hotfix de recuperación Spotify

APK y ZIP fuente generados desde `71c43207a3f2de07ccd9aa6802bb913b5de3d401`, con 32/32 casos dirigidos aprobados (0 fallos, errores u omisiones), compilación Raycast Preview y lint completos. Lint: 0 errores, 14 advertencias. No se utilizó GitHub Actions.

## Defecto reproducido y corrección

El panel productivo de Spotify con el registro durable del overlay fallaba al componer: `MutableState containing TRACK cannot be saved using the current SaveableStateRegistry`. La reproducción anterior al fix (`a8ff467e`) falló 1/1 y se conservó su log/XML. No hay logcat del dispositivo: esto demuestra un defecto del código de Preview 48, no su exclusividad como causa del crash físico.

El filtro guarda ahora su identificador API como String, conserva el codec estricto y admite un identificador desconocido mediante el filtro Tema. El cierre protege la captura del estado: ante un proveedor inválido conserva el último checkpoint y completa la limpieza, sin registrar estado privado ni la excepción que podría contenerlo. No se borraron preferencias ni conversaciones.

Las primeras rondas detectaron problemas de fixture/aserción (Client ID público ausente, comparación de mensaje abreviado y etiqueta del campo incluida en la comparación). Sus logs/XML se conservaron; la validación final mantiene comparaciones exactas y las comprobaciones de recuperación.

## Evidencia automatizada

- SpotifyOverlayRecoveryRegressionTest: 4 casos, panel real y codec durable; filtros, consulta, error sin token, cierre/reapertura, persistencia por ContextStore e identificador desconocido.
- OverlayDestroyRecoveryTest: 2 casos, limpieza tras fallo del proveedor, checkpoint/conversación conservados y ausencia de estado privado en logs.
- OverlayStartupTest: 8 casos (SDK 28/30).
- DurableOverlayRecoveryTest: 5 casos.
- MediaInspectorFlowTest: 5 casos (incluye geometría de tablet/móvil).
- SpotifyIntegrationFlowTest: 2 casos.
- SpotifySearchClientTest: 6 casos con fixtures controlados.

No hubo conexión OAuth con una cuenta real, búsqueda/reproducción real en Spotify, envío de WhatsApp ni validación física en Galaxy Tab A9+. El proveedor no oficial sigue desactivado. Matriz 9/25/6 sin cambios.

## Entrega verificada

[Release privada en borrador, APK, ZIP fuente, evidencia y SHA256SUMS](https://github.com/bauti3311-sys/Spotlight-Next/releases/tag/untagged-04efa22f975c79bd5830)

Paquete `dev.bautista.spotlight.next.raycastpreview`, versionCode **49**, no debuggable, firma v2 válida, certificado histórico SHA256 `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba` y alineación de 16 KiB verificados. El ZIP proviene de git archive del mismo SHA y no contiene archivos de firma. Los hashes del servidor coinciden con los archivos locales.

- APK SHA256: `062eba8ab2a0ef3ff3d6acdc64af8e6bec3bd966372365be720a31e864224477`
- ZIP fuente SHA256: `782742819d1ffb5af0e51ae02b06196bd58b330becb92cd3cc2bb18e0b64740c`
- ZIP evidencia SHA256: `9d104ccc7b5624f5fb0ed138d3f47f2e6d244e61571640bc76088bacc50c38b2`

Previews anteriores conservadas. Rama autorizada y PR #18 en borrador; sin merge ni force-push.
