# Visual First — acciones visibles en tarjeta de notificación

Base remota `73af4d61d27fabf875c90cbd75cf195ab45352b3`, PR21 abierto/borrador,
reserva libre antes de adquirir una nueva propia. Continuación de V06/V08, sin
reanudar funcionalidades C3–C8. FocusB, D1 y decisiones D2–D4 conservados.

## Defecto reproducido

`NotificationPreview` colocaba sus cuatro acciones secundarias en un Row fijo.
Con390dp/texto100%, «Volver» tenía ancho0dp; «Sugerir respuesta con IA» se partía
en una columna estrecha. Con330dp/texto200%, «Sugerir respuesta con IA» también
medía0dp y su nodo se estiraba verticalmente. No bastaba con scroll vertical:
las acciones no tenían espacio horizontal asignado. Antes:4pruebas,1correcta
(tablet) y3fallidas (teléfono y texto ampliado). PNG/XML/log originales conservados.

## Corrección mínima

Las dos filas de botones existentes ahora usan FlowRow, sin separación horizontal
nueva y con4dp entre líneas cuando deben envolver. Se conserva el orden: Responder,
Abrir; luego Copiar, Resumir, Sugerir y Volver. Ningún botón, acción, etiqueta,
capacidad, color, tipografía, permiso, estado habilitado o callback se modifica.
La acción larga puede ocupar más de una línea legible dentro de su botón. La
captura de tablet1000dp/texto100% permanece **idéntica byte a byte**.

Sólo se cambia presentación de `NotificationPreview.kt`. No se modifica el acceso
a notificaciones, RemoteInput, identidad de destinatario/conversación, confirmación,
consumo único, políticas de IA, guardado de datos ni ejecución de WhatsApp. No se
agrega una capacidad simulada: la notificación es fixture explícito de prueba.

## Evidencia y límites

Cuatro pares A/B reales Compose/API33/Robolectric nativegraphics/densidad1, mismos
textos, fecha, escala y dimensiones:330dp/200% oscuro y claro,390dp/100% claro y
1000dp/100% oscuro. Se usa `NotificationPreview` productivo con notificación sintética.
El contenedor con scroll pertenece al fixture; no es una nueva navegación productiva.

Los callbacks de UI se recogen localmente: Responder prepara el mismo ComposeMessage
con replyKey; Copiar conserva el texto; las dos acciones de IA conservan Ask; Volver
se invoca una vez. Abrir está deshabilitado cuando falta PendingIntent. No se
abre WhatsApp, escribe portapapeles, llama IA externa o envía un mensaje real.
Las regresiones de confirmación usan PendingIntent sintéticos, no cuentas reales.

Se mantienen los tests de confirmación RemoteInput y destinatario homónimo.
La prueba roja, los XML finales y hashes se conservan en
`verification/visual-notification-20261106`. Galería offline con ampliación,
PDF5páginas y ZIP de originales, sin retoque de PNG. No se actualizan baselines
históricas para ocultar el defecto.

No hay nueva evidencia física OneUI/Gboard/WhatsApp/servicio NotificationListener,
TalkBack o rendimiento. Componentes JVM y callbacks contados no prueban envío ni
compatibilidad física. No APK, Actions, cambios de main, paquete, firma, versión,
dependencias o permisos. Preview65/checkpoints y código funcional preservados.

V06/V08 avanzan con una corrección visible y reversible; V10/D5 sigue sólo test,
ventana140/100ms intacta. Las nueve V siguen abiertas en aceptación física. No
rehacer FocusB ni los componentes aprobados; priorizar validación física futura
cuando exista autorización específica de entrega, sin reabrir C3–C8.

## Resultado de validación

**18casos dirigidos distintos correctos:**5UI (4escenas y teclado),7confirmación
RemoteInput,6destinatario homónimo. LintDebug0errores/26advertencias/1informativo.
Tab/Shift+Tab/Enter recorren las acciones envueltas en orden, omiten Abrir
deshabilitado y emiten cada callback una vez. No se prueba Gboard con teclado JVM.
El test de teclado se ejecutó solo tras el pase17+lint; sin repetir la suite ni lint.

La primera compilación productiva usó el fallback automático sin daemon de Kotlin
tras fallar el arranque de ese proceso; terminó correctamente en la misma ejecución.
El test adicional usa estrategia in-process por argumento, sin modificar configuración
productiva, dependencias ni aumentar timeouts. Logs completos conservados.

Fuente/pruebas/capturas publicadas: `d342c9ec2e4bf19f2f669a045a2df3184bde2fef`.
La documentación posterior no modifica esa fuente productiva.
