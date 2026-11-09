# Preview65 — candidata local verificada; publicación bloqueada

Base remota comprobada `87c75aa26b0eddd8da2e137fed2bae857bced409`, PR21 abierto y
borrador en feat/preview57-native-return-20261103. Preparación autorizada por el
propietario; esta autorización no permite APK, firma, preflight real ni Actions.

## Identidad congelada

Code65 > Preview64; nombre optimizado `2.0.1-raycast-preview-native57-v65`.
Paquete `dev.bautista.spotlight.next.raycastpreview`; certificado requerido
`885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba`.
release.properties principal y configuración de firma sin cambios.

Conservados: Focus B inspector/revisión contextual, homónimos df5bd77, Spotify
Web API y App Remote separados, permisos/datos/preferencias, motor/ranking,
NativeLaunchSession, geometría/footer y teclado/táctil. Focus=false por defecto.
Plan maestro23/C3–C9 intacto; sin nuevas capacidades, servicios o dependencias.

Único delta funcional de preparación: numeración de Raycast Preview. Para probar
los controles reales se extrae su bloque existente a SpotifySelectionControls:
mismos textos, nodos, orden, disponibilidad, callbacks y ausencia de contenedor
nuevo. No nuevo diseño ni ejecución. La capa padre conserva compuerta/corrutina,
cancelación y mensajes. No se incorporaron nuevas operaciones de Spotify.

## Auditoría dirigida de Spotify

| Recorrido | Evidencia y resultado |
| --- | --- |
| Selección de ruta | Compose real: local inicial, Web/local excluyentes; elegir no confirma ni consulta. |
| Confirmación | Control explícito; busy deshabilita rutas/confirmar/comprobar; compuerta existente rechaza concurrencia sin cola. HTTP: dos confirmaciones concurrentes producen GET+un PUT. |
| Cancelación | Cancelar solicitud conserva job.cancel; finally libera busy/compuerta. GET cancelado no hace PUT; PUT enviado no se repite ni se promete revertir. Mensaje conserva incertidumbre. |
| Errores | Cliente existente distingue 401/403/404/429 y limita Retry-After sin retry; texto seguro sin cuerpo remoto. UI muestra el error y permite acción explícita posterior. Sin nuevo manejador de errores. |
| Estado | Comprobar usa observeActiveDevice sólo en Web; local usa observe. Aceptación no se convierte en reproducción observada; URI/playing identifican seleccionado, pausa u otro contenido. Sin polling. |
| Restauración | Se guarda sólo la elección de ruta; resultados, selección, job y confirmación no se restauran ni reproducen. |
| Accesibilidad acotada | Controles Compose a330dp: claro100%, oscuro200%, opciones y acciones alcanzables por scroll; busy200% sin callbacks. No acredita TalkBack/IME/OneUI. |

No se añadió otra batería equivalente a las36 pruebas verdes ya publicadas en
87c75aa (scopes/renovación, errores, diagnóstico y cliente local). Evidencia histórica
identificada en docs/verification/spotify-web-active-20261105, no contada como nueva.
Las pruebas nuevas cubren el hueco de los controles Compose productivos.

## Verificación ejecutada

15 casos distintos nuevos/revalidados en esta preparación: SelectionControls3,
IntegrationFlow2, PlaybackGuard9 y MessageRecipientReviewRegression1. Todos correctos.
La regresión homónima vuelve a probar el cambio de número antes de pedir el Intent.

Comando: bash gradlew :app:testDebugUnitTest con --tests de esas cuatro clases,
-I scripts/directed-tests.init.gradle y -PspotlightDirectedClasses restringido a
ellas, seguido por :app:lintDebug; --no-daemon --max-workers=1 --console=plain.
Java17/SDK35 ya provisionados. XML/logs en verification/native65-preparation-20261105.
No assemble, firma o APK en esta preparación.

## Única ruta de entrega posterior a autorización expresa

1. Autorizar el par fuente/workflow exacto publicado, code65 y un solo intento.
2. Adquirir reserva propia NUEVA con checkpoint=workflow autorizado; verificar
   HEAD/PR, owner+lease, manifiesto y ausencia de intento anterior por fuente/workflow.
3. Despachar una sola vez build-apk.yml desde la rama del PR21, mode=native-delivery,
   source_sha y workflow_sha exactos, confirm_preview=AUTHORIZE_PREVIEW_65_ONCE,
   reservation_owner y reservation_lease_sha de esa reserva nueva. Sin retry/fallback.
4. Flujo existente: preflight aislado de certificado histórico; build unsigned
   separado sin secretos de fuente exacta; firma aislada y validación independiente.
   Presupuesto US$0/bloqueo vigentes. Si falla una guarda o cuota, detenerse.
5. Descargar APK+ZIP, verificar firma v2/único firmante, paquete/code/name, noDebuggable,
   ARM64/SDK/16KiB, payload, hash SHA256 y ZIP idéntico a git archive de la fuente.
   Entrega Preview privada, nunca estable. Liberar únicamente reserva propia con notas.

## Pendientes físicos

Instalar sobre64 sin desinstalar/borrar datos en Galaxy Tab A9+ y moto g86.
Comprobar Focus B/homónimos, persistencia, teclado/táctil/IME/OneUI, nueva ruta Web
con Premium y dispositivo controlable, aceptación frente a observación, cancelación,
y errores reales sin repetición. App Remote sigue separado; timeout sin causa física
confirmada, no se promete corregido. Ninguna prueba JVM certifica cuenta/audio real.
Después de esa aceptación se continúa C3 y los restantes checkpoints, sin rediseñar
las variantes B ya aprobadas.

## Par preparado y bloqueo de publicación

Fuente local: `ffc74d291a8a13ac1d62c5ddfb989bb90e3b6fe7`. Manifiesto65 fija esa misma fuente como
base funcional y allowed_delta_paths=[], sin diferencias productivas entre fuente
congelada y base. El commit siguiente de manifiesto/evidencia es el SHA workflow
local, todavía NO publicado ni autorizable como entrega remota.

Lint final correcto: 0 errores, 26 advertencias y 1 información preexistentes.

Dos pushes normales de la fuente fueron rechazados con `remote: fatal error in
commit_refs`. La comprobación remota posterior seguía en87c75aa. Se comprobó
protected=false y se intentó un transporte alternativo con la API Git: el primer
POST de blob devolvió HTTP503. No se llegó a crear árbol/commit ni actualizar la
referencia vía API. No force, cambio de protección ni Actions. No más reintentos
de publicación en esta sesión. El próximo operador debe recuperar transporte,
comprobar HEAD y protocolo, publicar fast-forward esta fuente y después el tooling,
verificar ambos SHA remotos y recién entonces solicitar autorización del par.
La reserva de esta preparación no se debe reutilizar para una entrega futura.

19 checks offline de entrega/identidad correctos sobre manifiesto65; fixtures
sin claves, cuentas, builds ni preflight real. Ver offline-delivery-tests.log.
