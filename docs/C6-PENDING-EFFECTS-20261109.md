# C6/S03 — no continuar después de un efecto pendiente

Nueva instrucción del propietario: completar el plan maestro23. Se retoma trabajo
funcional después de finalizar la entrega71, con reserva nueva. Base de desarrollo
`8f234e2498c207a43c492fbca5de2507e268bf88`; PR21 abierto/en borrador.

## Defecto reproducido

El recorrido `EditablePlanPanel`/comandos guardados usa `PlanExecution`. Éste
bloqueaba pasos posteriores después de un fallo, pero continuaba después de un
resultado PREPARED/REQUESTED o de despachar un Action a Android. Eso confundía
solicitud aceptada con efecto observado. La otra ruta, AiToolPanel con
ToolSequenceExecution, ya aplicaba esa espera.

Nuevo banco JVM con ejecutores/dispatch de fixture: tres casos, dos fallaban antes
(en un caso se comprueban PREPARED y REQUESTED; el primer assert fallaba ya en
PREPARED). Se reproducen continuación de un cambio posterior y dos dispatches
cuando sólo debía haber uno. No se abrió una app ni se modificó volumen real.

## Corrección mínima

Se conserva el motor existente. `PlanExecution` guarda el motivo de bloqueo y
no ejecuta pasos posteriores si el resultado anterior sólo quedó preparado o
solicitado. El texto explica que falta verificar el efecto, sin etiquetarlo como
fallo confirmado. Un error conserva su motivo anterior; cancelación se propaga.
Pasos omitidos y resultados locales COMPLETED/VERIFIED conservan su comportamiento.

No se añade mecanismo para afirmar verificación pulsando un botón ni se reintenta
un efecto incierto. La reanudación con observación real sigue siendo parte del
recorrido completo C6 pendiente; este cambio sólo cierra el hueco de seguridad.

## Verificación

- 3 casos nuevos y7 existentes de PlanExecution, más6 de ToolSequenceExecution:
  **16 casos correctos**, sin sumar las rondas rojas como éxitos.
- LintDebug final0E/23W/1Info; sin supresiones/baselines.
- Se mantienen confirmación, rechazo de fallo, omisión, seis pasos máximos,
  cancelación y retorno tardío sin dispatch; nueva cobertura de efectos pendientes.
- Sólo cambia PlanExecution.kt (6 líneas añadidas/4 retiradas) y se añade el banco.
  Sin cambios de UI, ranking, preferencias, permisos, dependencias o ejecutores de apps.
- [XML/resumen/logs](verification/c6-pending-gate-20261109/summary.json).

Java17 y SDK/cachés existentes. `:app:testDebugUnitTest` sólo estas tres clases,
-I scripts/directed-tests.init.gradle y la guardia no-apk de c2-app-open-20261107,
--offline --no-daemon --max-workers=1; después :app:lintDebug. No APK/testAPK, firma,
instalación ni Actions nuevos.

**No es C6 completo.** Faltan el recorrido compuesto con citas C5 verificadas,
observación externa/reanudación y aceptación física. [Matriz de los23](MASTER-COMPLETION-20261109.md).
Esta corrección es POSTERIOR a Preview71 y NO está en su APK: la fuente71 permanece
b8f8e268dd59e6410ec57a102a0e35231db01b18, run42635574367 único correcto/consumido.
