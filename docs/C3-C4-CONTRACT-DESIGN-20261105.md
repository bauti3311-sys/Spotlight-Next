> Actualización posterior del propietario: Focus B ya integrado y Preview65 entregada.
> Spotify App Remote/Web API fallan físicamente según su prueba y quedan pausados.
> Se autoriza avanzar C3a en contratos, fixtures y correcciones pequeñas existentes,
> sin nuevos permisos/capacidades sensibles/envíos reales. Ver
> [avance C3a](C3-PROGRESS-20261105.md). El diseño y las puertas inferiores conservan
> su valor histórico; esta autorización no equivale a aceptación física integral.

# C3 y C4 · diseño de contratos desde Preview64

**Estado: diseño preparado; capacidades productivas nuevas NO autorizadas ni implementadas.**
Base ejecutable64:808bda2c25887d14b42665dea4c254181d72b837;
HEAD administrativo comprobado0fa67813f7ea8f461f1d8f21502bfe8f8f008c66, PR21 borrador.
Se mantiene el [plan maestro de23mejoras](SPOTLIGHT-NEXT-PLAN-MAESTRO-20261103.md).
Este documento no habilita cuentas, envíos, SDK, permisos nuevos, Actions ni APK.

## C3 · WhatsApp: elegir bien antes de confirmar

### Lo comprobado en el código actual

- `Action.ComposeMessage` conserva canal, destinatario, texto y replyKey.
- `People.find` usa contactos autorizados; `People.uniqueExact` no escoge si hay
  varias coincidencias exactas. `Person` distingue id y número. El mismo nombre
  no es identidad suficiente; una persona puede tener varios números.
- `People.messageIntent` fija `com.whatsapp` o `com.whatsapp.w4b`, normaliza número
  y prepara enlacewa.me. No demuestra que el destinatario recibió el mensaje.
- `MessageEditor` ya presenta borrador y revisión; nuevos mensajes se completan
  en WhatsApp. `Replies` sólo observa notificaciones autorizadas actuales, en RAM,
  no el historial íntegro de chats.
- `Replies.send(expected)` revalida paquete, conversación y PendingIntent vigente;
  elimina la entrada tras una solicitud y rechaza repetición con esa entrada.
  Los contratos de `ReplyConfirmationTest` no prueban entrega ni un exactly-once
  global frente a cualquier ruta o reinicio.
- `NativeLaunchSession` continúa protegiendo los lanzamientos Android: destino
  fijado, revisión de alternativas, permisos revalidados y resultado incierto sin retry.
  RemoteInput sigue por `Replies.send`; no convertirlo en Intent ni crear otro bus.

### Defecto hallado y corrección independiente

La nueva regresión usa dos Alex ficticios y callbacks interceptados. Con un número
manual y revisión abierta, pulsar otro homónimo conservaba la confirmación.
El fallo rojo está en `contact-red/`; no se envió nada. La selección de contacto
ahora limpia `confirming`, `approvedReply` y `sent`. Corrección aislada: df5bd778019e1dc16a56c41ca8f8ad9ea4525652.
La prueba verde exige otra
revisión y comprueba que el único Intent preparado corresponde al segundo número.
Es una corrección de la capacidad existente, no la implementación ni aceptación deC3.
No está en la APK64 ya entregada; sólo en la fuente posterior de esta revisión.

### Recorrido propuesto

| Paso | Interfaz que necesita el usuario | Contrato y propietario existente |
|---|---|---|
| Entrar | Elegir WhatsApp personal o Business sin cambiar de destino en silencio | `ActionCatalog`, `IntegrationRegistry`; preservar app exacta primero |
| Elegir destinatario | Nombre + número normalizado y país; homónimos requieren elección | `People.find`/`Person`; identidad id+número+paquete, no sólo texto de búsqueda |
| Escribir | Un borrador editable con origen explícito del contenido incorporado | `MessageEditor`, `ContextStore`; sin adjuntar/extractar automáticamente |
| Elegir mecanismo | «Preparar en WhatsApp» o «Responder a esta notificación» con disponibilidad real | enlace público o RemoteInput auténtico; no selector de canal decorativo ni API privada |
| Revisar | Destinatario completo, app, texto y efecto esperable | snapshot efímero de revisión; ningún efecto aquí |
| Confirmar | Botón con acción concreta, sin doble despacho | consumir aprobación antes del efecto; lanzamientos por NativeLaunchSession, respuestas por Replies.send(expected) |
| Resultado | «Solicitud enviada a WhatsApp; entrega sin comprobar» | no VERIFICADO por abrir una app o aceptar PendingIntent |
| Volver/cancelar | Retorno con consulta/cursor/selección/scroll y borrador acordado | propietarios existentes de Focus; cancelar no deshace una solicitud ya enviada |

### Snapshot de revisión: especificación, no nueva clase implementada

Identidad conjunta de canal/paquete, id+número normalizado seleccionado, texto
exacto y revisión del borrador. Si es respuesta: key de notificación, conversationId,
paquete, grupo/participante y acción/PendingIntent esperados. No serializar una
aprobación, un Intent, PendingIntent ni grants en recuperación/backup.

Cambiar persona, número, texto, app, mecanismo o contenido incorporado invalida la
revisión. Si desaparece una notificación o cambia su acción, detenerse y revisar.
Restaurar conserva sólo borradores permitidos y vuelve a comprobar disponibilidad.
No elegir otra app, otro contacto o el enlacewa.me como fallback automático.
No reutilizar una aprobación tras desconexión, restauración o resultado incierto.

Los adjuntos ya autorizados deC2 mantienen URI/MIME/grants temporales mínimos y
revisión separada; C3 no añade adjuntos RemoteInput ni lectura privada de chats.
No se añade servicio/permisos de accesibilidad, Tasker o envío oculto.

### Lenguaje y composición

Reutilizar el campo Focus, los formularios actuales, footer y revisión compacta.
Mostrar «WhatsApp personal»/«WhatsApp Business», número y «notificación actual».
«RemoteInput», nombres de clases y paquetes pertenecen al diagnóstico técnico,
no al texto principal de la tarea. No reemplazar datos reales por una tarjeta
con una conversación inventada. La lista real de opciones sigue siendo accesible.

Estados mínimos: sin permiso, sin destinatario, homónimos, borrador, revisado,
solicitando, solicitado/no verificado, permiso revocado, notificación vencida,
error conocido, resultado incierto y cancelado antes de solicitar.

### Puertas de aceptación antes de implementar/activar capacidadesC3

1. Decisión visual del propietario sobre la composición Focus e integración sin
   pérdida de controles, resultados exactos ni límites de ventana.
2. C0/C1/C2 físico aún pendiente en Preview64: OneUI/Gboard, altura al abrir desde
   Ajustes, teclado/táctil, regreso, rotación, multiventana y permisos/grants reales.
   La preparación de contratos y la corrección menor no cierran esa puerta.
3. Autorización acotada para C3a: destinatario y revisión, usando mecanismos actuales.
   Sin ampliar lectura de notificaciones/contactos ni añadir transporte.
4. Fixtures: homónimos/números múltiples, personal vsBusiness, app ausente, permiso
   revocado, PendingIntent reemplazado/cancelado, doble confirmación, Escape/Back/X,
   editar y restaurar sin aprobación, ausencia de fallback y contenido privado sin logs.
5. Envíos o respuestas reales: autorización específica, cuenta/dispositivo y
   destinatario de prueba consentido. Observar aparte la entrega real; no inferirla
   del callback de Android. Una nueva APK requiere su autorización exacta propia.

## C4 · Spotify: autorización no es reproducción observada

### Base real que debe reutilizarse

`SpotifyConnection` ya contiene OAuth/PKCE, vault, búsqueda explícita y control;
`OfficialSpotifyRemote` conecta AppRemote y obtiene PlayerState;
`SpotifyLocalPlayer` aplica mutex, cancelación, timeouts y liberación/idle;
`MediaInspector` también puede mostrar una MediaSession autorizada de Android.
Dependencia actual: app-remote0.8.0, Compose BOM2024.12.01; no se cambian.

`SpotifyConnection.state.AUTHORIZED` describe autorización de cuenta, no que exista
un dispositivo conectado o que una canción esté sonando. `play()` devuelve solicitud
aceptada; `observe()` es la vía de observación. No unificar OAuth, AppRemote y
MediaSession en un booleano optimista «conectado». Las capturas existentes de
«Horizonte» declaran una sesión sintética sin audio: no validan Spotify real.

### Estados y contratos a diseñar, no nuevos servicios

| Situación observada | Presentación y acción segura |
|---|---|
| App no instalada | Explicar ausencia; enlace público sólo como alternativa elegida y revisada |
| Sin OAuth | Buscar mediante enlace público o autorizar explícitamente; no inventar resultados internos |
| OAuth autorizado, sin playback local consentido | Separar búsqueda de la autorización de reproducción local |
| Conectando | Estado de operación actual, destino fijado; cambiar canción invalida revisión pendiente |
| Resultados de búsqueda | Título/artista/URI de la respuesta vigente, sin consultas por tecla ni resultados tardíos sobre nueva consulta |
| Play solicitado | No cambiar a «reproduciendo» por el click; consumir confirmación y esperar observación |
| PlayerState observado | Título/URI/pausa/progreso con instante y origen de observación; controles disponibles reales |
| Offline/401/restricción de cuenta/sin dispositivo | Mensajes distintos y recuperación explícita; sin retry automático de playback incierto |
| Cierre/desconexión | Cancelar callbacks/trabajos y liberar sesión conforme al lifecycle; sin servicio permanente nuevo |

Snapshot operativo: URI/tipo de contenido exactos, cuenta/configuración autorizada,
capacidad disponible y revisión del usuario. Tokens privados sólo en vault/memoria,
no en UI/logs/capturas/recuperación. No cambiar contenido aprobado mientras está busy.
Una reconexión no autoriza repetir un play que pudo aceptarse.

C4a se limita a auditar estados, desconexión y mensajes de resultado sobre los
adaptadores existentes. Antes de cualquier cambio de SDK debe existir un fallo
reproducido y análisis de compatibilidad/licencia aprobado. No añadir Cast,
listas nuevas o descargas como parte deC4.

### Aceptación pendienteC4

Después deC3 y sus puertas pertinentes: fixtures de sin sesión/carga/error/offline,
401, restricción de cuenta, selección cambiada, cancelación/doble click, estado
observado coherente y liberación de callbacks. Capturas de esos estados con datos
equivalentes. Prueba física y cuenta Spotify consentida para demostrar búsqueda,
play/pause/skip, reconexión y ausencia de cortes; no declarar audio real desde JVM.

## Siguiente implementación acotada recomendada

Primero decidir las dos variantes B de [Focus](FOCUS-CONSOLIDATION-REVIEW-20261105.md).
Si se aprueban: integrar sólo jerarquía del inspector y etiquetas de revisión detrás
de Focus opt-in; reutilizar disponibilidad/prepare actuales, deduplicar la búsqueda
promovida por capacidad y probar330dp, ambos temas, texto ampliado, retorno y teclado.
No activar Focus por defecto ni iniciarC3 por esa sola aprobación visual.
Luego C3a, tras sus condiciones físicas y autorización, y después C4a.
