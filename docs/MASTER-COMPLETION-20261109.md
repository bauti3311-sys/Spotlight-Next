# Retomar el plan completo de 23 mejoras

Nueva instrucción del propietario recibida durante la entrega71: «termina de
incorporar por completo todas las c, v y lo otro ... implementaciones totales».
Se interpreta como el plan maestro aprobado de **10F +9V +4S**, organizado en C0–C9;
no las36 propuestas del catálogo, que el propietario rechazó en gran parte.
La pausa general C3–C8 deja de ser prioridad vigente para investigación/desarrollo;
no autoriza datos privados, envíos reales, instalación sobre apps personales ni
permisos de sistema por defecto. No habilita un segundo intento71 ni otras APK.

## Separación de la entrega en curso

La Preview71 autorizada sigue congelada exclusivamente en ancho y Ajustes:
sourceb8f8e268dd59e6410ec57a102a0e35231db01b18,
workflow604544872a70a609e2307ce0c61af6edb99b2ccb,
único run42635574367. Se conserva la fuente de70 como base, C5 fuera del APK.
Este inventario se inspeccionó sobre el PR preparado6045448 y NO modifica esa fuente.
No se escribe ni publica producto durante los jobs que validan la reserva.

## Hechos nuevos comprobados

- ADB de Work no tiene dispositivos conectados (consulta real en esta tarea).
  No se instaló nada. No se conoce el estado global de paquetes/perfiles privados.
- Los cinco tests C5.4 están preparados; el protocolo sigue sin un target seguro.
  El historial aceptado C5.3 es7/12 aciertos léxicos,5omisiones,0FP en su corpus;
  recuperación semántica auténtica no evaluada. No atribuir estos datos a71.
- Cast Sender SDK/SessionManager/RemoteMediaClient no aparecen en producto ni
  dependencias actuales; LocalMediaRequestPolicy es una política parcial, no Cast.
- AppFunctionsSupport sólo describe SDK/servicio/autorización; incluso su estado
  favorable dice explícitamente que NO descubre ni ejecuta funciones ajenas.
- Existen dos recorridos de planes: AiToolPanel usa ToolSequenceExecution y el
  editor de planes locales/comandos guardados usa PlanExecution. El segundo tiene
  una diferencia de gating que requiere una regresión específica (ver siguiente paso).
- SpotlightPanel tiene1696 líneas; la extracción modular es parcial. No se usa el
  conteo como argumento para una reescritura ni se declara S02 completo por separar UI.

## Estado por checkpoint

| Checkpoint | Implementación real y evidencia disponible | Para cerrarlo de verdad |
|---|---|---|
| C0 | Referencias, diagnósticos y evidencia física parcial histórica | Línea base fría/caliente/p95/RAM en dispositivos, IME/rotación y matriz completa |
| C1 | UI nativa, componentes/tokens, Geist, Puente aprobado; modularización parcial | Completar separación sólo donde ayude, pruebas de comportamiento y aceptación física |
| C2 | Ranking y selección estabilizados28casos; capacidades y recorrido de apertura con recibos; Share/Process Text | IME/rotación/ejecución y retorno reales; no confundir Intent recibido con efecto completo |
| C3 | Identidad/números, personal/Business, revisión efímera, homónimos y RemoteInput vigentes implementados con fixtures | Mensajería en equipos/cuentas de prueba y notificación real; envío sólo autorizado específicamente |
| C4 | Dos rutas Spotify existentes | Fallo físico reportado en65 sigue sin resolver. Necesita reproducir con autorización/cuenta/dispositivo; no prometer playback por JVM |
| C5 | Citas y estado reactivo enPR; PDF real enJVM; evaluación léxica; instrumentación preparada | SAF/OCR/lifecycle en target seguro, modelo auténtico opcional y evaluación antes de semántica; luego integración productiva mínima |
| C6 | ToolRegistry/ToolExecutor, planes editables, receipts y cancelación existentes | Unir citas verificables sin inventar fuentes; igualar gates de ejecución y probar el recorrido compuesto completo |
| C7 | MediaSession y controles Android/Shizuku existentes; política local de medios | Integración Sender real aún no implementada, revisión de dependencia y receptor Cast/dispositivos autorizados |
| C8 | Diagnóstico de requisitos únicamente | Viabilidad oficial actual + entorno/privilegios + prueba real o cierre explícito «no viable»; no bypass |
| C9 | QA dirigida y diagnósticos parciales | Regresión integrada, TalkBack y medidas reales comparables, lifecycle/fugas y consolidación de rutas aprobadas |

Ningún checkpoint se marca cerrado integralmente por producir su APK o su formulario.
No reiniciar C1/VisualFirst ni reparar repetidamente componentes ya aprobados.

## Seguimiento de las23 mejoras

| ID | Estado técnico | Pendiente material |
|---|---|---|
| F01 | Integraciones públicas e inspector implementados | Cobertura por apps reales y efectos/retorno observados |
| F02 | Base WhatsApp C3a implementada | Aceptación física de disponibilidad, revisión y respuesta autorizada |
| F03 | Rutas Spotify implementadas pero fallidas físicamente según propietario | Reproducción efectiva y estado observado no resueltos |
| F04 | Sin Sender Cast completo | Dependencia/API/receptor y ciclo de sesión real |
| F05 | Contratos de citas C5.1–C5.4, no barra productiva | Validación segura e integración; modelo no evaluado |
| F06 | Base de planes y ejecutores presente | Flujo compuesto completo con citas y gating uniforme |
| F07 | Ranking y contexto autorizado con regresiones | Validación física y presupuestos locales de latencia |
| F12 | Controles compatibles y observación parcial presentes | Matriz física de permisos/restricciones/dispositivos |
| F14 | Share/ProcessText y formularios reales | Recorrido físico de URI/permisos/cancelación/retorno |
| F15 | Sólo comprobador AppFunctions | Estudio/prueba real; puede cerrarse no viable conforme al plan |
| V01 | Materiales nativos aprobados, ahora Puente/Hielo | Contraste y transparencia físicos; no rediseñar |
| V02 | Inspector contextual implementado | Recorridos reales de cada integración |
| V03 | Acentos por app conservados | Contraste con iconos/apps reales |
| V04 | Jerarquía/título2:1/selección implementados | Legibilidad física y texto ampliado |
| V05 | Footer adaptable y acciones conservados | Teclado y estados reales por integración |
| V06 | Adaptación/IME/scroll/ancho reparado y probado JVM | WindowManager/OneUI/Gboard/rotación físicos |
| V07 | Geist e iconos por rol/licencias conservados | Accesibilidad y revisión física de recursos |
| V08 | Tarjetas existentes implementadas | No confundir presentación con capacidades C4/C5/C7 completas |
| V10 | Apertura140ms y reducción de movimiento implementadas | Jank/latencia/RAM reales; D5no se adopta sin evidencia |
| S01 | Protocolo y evidencias parciales | Acceso autorizado al hardware; Work no lo tiene |
| S02 | Componentes separados, raíz todavía1696líneas | Extracciones justificadas y verificadas; no otro framework |
| S03 | Estados tipados en motores presentes | Consistencia en rutas heredadas y observación externa |
| S04 | Instrumentos y pruebas de host presentes | Baseline/p95/memoria/fugas en el mismo hardware |

## Primer avance real publicado, sin bloquearse en Spotify

C6/S03: dos fallos reproducidos en tres casos nuevos; PlanExecution ahora bloquea
pasos posteriores si el efecto anterior sólo está preparado/solicitado. Conserva
la ruta cuyo resultado local está completado/verificado.16casos de planes correctos
con regresiones y lint0E/23W/1I. Ver [checkpoint C6](C6-PENDING-EFFECTS-20261109.md).
No es síntesis con citas, reanudación automática ni C6 completo; no está enAPK71.

Después: contratos faltantes C3/C5/F14 y, de forma independiente, investigación
C7/C8 con sus requisitos concretos. No descargar modelos, pedir documentos
personales, activar accesibilidad o enviar mensajes para fingir una aceptación.
Si faltan dispositivo/cuenta/receptor/privilegios, registrar bloqueo y continuar
sólo trabajo independiente verificable. No afirmar ejecución futura en segundo plano.


## Investigación oficial C7/C8 realizada en esta tarea

[URLs, hashes y POM originales](verification/master-completion-20261109/README.md).
Cast23.0.0 aparece en GoogleMaven/notas oficiales del19/10/2026; incluye
MediaRouter1.9.0-beta01.22.3.1 también arrastra una versión beta y Kotlin2.1.0.
No se adopta automáticamente ninguna ni se inventa una medición de RAM/tamaño.

La guía AppFunctions confirma Android16 y permiso de invocación. AOSP Android16
clasifica EXECUTE_APP_FUNCTIONS como internal|privileged; no es un permiso común.
El dictamen productivo actual es NO-GO para anunciar control de otras apps desde
la APK ordinaria. No requiere fabricar un invocador ni eludir privilegios para
«completar»C8. Los equipos/OEM siguen sin comprobar; no se declara prueba física.

## Bloqueos para una implementación/aceptación TOTAL

1. S01/C0/C5/C9: no hay Android conectado ni target limpio/certificado para el banco
   de instrumentación. No puede cerrarse físicamente con pruebas JVM.
2. F03/C4: resultado Spotify reportado fallido, sin cuenta/sesión/dispositivo
   accesibles aquí para reproducir y observar la corrección. No se prometió reparación.
3. F05/C5: SAF/OCR/lifecycle pendientes; modelo real no evaluado. No se integra
   recuperación semántica ni respuestas remotas para sustituir esa evidencia.
4. F04/C7: SDK Sender real no incorporado; revisión de grafo/host/recursos y receptor
   necesarios. No se instalan nuevas dependencias por una apariencia de avance.
5. F15/C8: privilegios de plataforma ajenos a esta APK ordinaria. El plan admite
   un cierre de viabilidad negativa, no control universal fingido.

No se declaran terminadas las23 ni se promete seguir ejecutando en segundo plano.
Se completó la entrega71 y el primer arreglo funcional posterior; la matriz conserva
las piezas reales faltantes para continuar cuando se resuelvan sus puertas.
