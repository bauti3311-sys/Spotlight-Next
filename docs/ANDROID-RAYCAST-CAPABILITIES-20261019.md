# Integraciones Android y adaptación Raycast — primer bloque

Base 0558ce4. Estado: código y siete pruebas preparados, SIN VERIFICAR. No equivale a compatibilidad física A9+.

## Auditoría AppFunctions

Android documenta Android 16+ y autorización EXECUTE_APP_FUNCTIONS para descubrir/ejecutar funciones ajenas. El ejemplo ChatApp publica funciones; el agente oficial es una herramienta de desarrollo con lanzamiento privilegiado. Publicar funciones propias no otorga permiso para controlar otras apps. No se instala ese agente ni se eleva Spotlight.

El diagnóstico nuevo consulta versión, presencia del servicio público app_function y permisos del proceso. Aun con ambos requisitos informa integración limitada: no hay descubrimiento/ejecución implementados. No se añaden permisos protegidos, reflexión ni SDK Jetpack todavía. compileSdk35 se conserva: el diagnóstico usa Context.getSystemService(String) y checkSelfPermission. Para un adaptador real futuro se deberá evaluar SDK36+/AGP/Jetpack y autorización legítima. No hay evidencia física de disponibilidad en esta Tab A9+.

## Matriz Android

| Proveedor/capacidad | API / mínimo | Estado y permisos | Intervención / alternativa | Implementación / evidencia |
|---|---|---|---|---|
| Android AppFunctions | Plataforma Android16/API36+; Jetpack | Limitada; servicio y autorización del sistema comprobados en ejecución | No se puede pedir como permiso común. Alternativa Intents públicos | Diagnóstico nuevo appfunctions_status; ejecución ajena no implementada, pruebas pendientes |
| Marcador | ACTION_DIAL tel:, API1 (app min26) | Disponible solo si resolveActivity encuentra receptor; sin CALL_PHONE ni READ_CONTACTS para número explícito | Confirmar propuesta y pulsar llamar en Teléfono. Sin marcador: copiar número en búsqueda | prepare_dial nuevo en ToolRegistry/Executor; NaturalIntents usa People.dialIntent; pruebas pendientes |
| Contactos | ContactsContract, API5 | Requiere READ_CONTACTS; consulta existente | Lista existente conserva números separados; elegir destino | People/SearchEngine existentes, sin validación física nueva |
| WhatsApp | wa.me/Intent con paquete; Android min26 del proyecto | Requiere app receptora y número compatible | Usuario revisa/envía; compartir como alternativa | People.messageIntent existente; no historial privado ni envío automático |
| Notificaciones | NotificationListener/RemoteInput API20 | Requiere acceso voluntario y acción de respuesta publicada | Respuesta solo cuando existe RemoteInput | ReplyService existente; prueba física pendiente |
| Spotify/YouTube | Intents y MediaSession API21 | Requiere app o sesión autorizada; comandos soportados | Destino ambiguo no ejecuta; abrir proveedor | Bloque multimedia 0558ce4 verificado JVM, físico pendiente |
| Apps instaladas | MAIN/LAUNCHER, PackageManager | Disponible según visibilidad Android | Abrir app/inspector; no inferir APIs privadas | Motores existentes; descubrimiento universal de funciones pendiente |
| App Shortcuts | LauncherApps API25 | Limitada por permisos de host/rol; no asumir acceso como launcher alternativo | Lanzamiento público de aplicación | Auditoría pendiente, no nuevo control anunciado |
| Calendario | CalendarContract/ACTION_INSERT API14 | Lectura/escritura con permisos; editor externo revisable | Confirmación en editor; no afirmar evento creado por abrir Intent | NaturalIntents/CalendarWorkspace existentes |
| Archivos | SAF API19, FileProvider, ACTION_SEND | Requiere URI autorizada | Quick Look y compartir con permisos acotados | Motores existentes; PDF mantiene pruebas previas |
| Alarmas/mapas | AlarmClock/Intents públicos | Depende de receptor y permisos existentes | Revisar en app receptora; no garantía de completar | Ampliación de herramientas pendiente |
| Ventanas ajenas | Multiwindow API24 | Limitada por Android/OneUI, sin privilegios de gestión global | Abrir actividad; split-screen gestionado por usuario | No mover/redimensionar ventanas ajenas |

## Matriz Raycast → Android

| API | Comportamiento | Equivalente / Compose existente | Límite / estado |
|---|---|---|---|
| List | Resultados y selección | LazyColumn, CompactResults (máximo3), expansión | Existente, sin rediseño |
| ActionPanel/Actions | Principal y secundarias contextuales | ActionPanelView y acciones por tipo | Existente; nueva llamada prepara ACTION_DIAL, no llamada automática |
| Detail | Detalle útil al resultado | Quick Look/inspector condicionado | Existente; PDF físico pendiente |
| Form | Entrada y validación | Formularios nativos/editor de planes | Parámetro número validado en ToolRegistry; no formulario paralelo |
| Grid | Colecciones visuales | Solo cuando corresponda a medios | No nueva cuadrícula principal |
| Tool | Herramientas/confirmaciones | ToolRegistry → capacidades → ToolExecutor | Reutilizado; dos herramientas nuevas sin ejecución remota automática |
| AI Extensions/AI | Contexto y respuestas | DirectAi/ToolAgent nativos | Sin SDK Raycast, integración/proveedor real pendientes |
| Keyboard | Atajos contextuales/foco | Contrato AGENTS, ActionPanelView | Conservado; teclado OneUI pendiente |
| Menu Bar | Acciones accesibles del sistema | Tile/atajo existentes | Sin notificación permanente nueva |
| Window Management | Control de ventanas | Abrir actividades/multiventana permitida | No equivalencia global Android |
| Command | Identidad, argumentos, errores, lanzamiento | ToolDefinition, ToolCall, ToolExecution y NativeCommands | No registro paralelo; PREPARED distingue preparar de completar |

## Fuentes y alcance

Consultadas oficialmente: https://developer.android.com/ai/appfunctions ; https://github.com/android/appfunctions ; https://github.com/android/appfunctions/blob/main/agent/README.md ; Context.java AOSP android-16.0.0_r1 (constante pública app_function); https://developer.android.com/guide/components/intents-common#Phone ; https://developers.raycast.com/api-reference/command ; https://developers.raycast.com/api-reference/user-interface/action-panel ; https://developers.raycast.com/api-reference/tool ; https://developers.raycast.com/api-reference/user-interface/form . Las demás filas reutilizan la investigación previa y requieren revisión detallada al tocar cada bloque, no se presentan como auditoría exhaustiva nueva.

Pruebas preparadas: rechazo de códigos/URIs en número, ausencia de marcador, confirmación/preparación sin startActivity, búsqueda numérica local sin contactos, alternativa copiar, estados de AppFunctions y diagnóstico local. No se llama a teléfonos/proveedores ni se utiliza Gemini real. Firma, APK, UI aprobada, main y dependencias intactos.
