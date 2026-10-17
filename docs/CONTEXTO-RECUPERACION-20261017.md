# Conservación ante lectura fallida ·17/10/2026

Continúa2de8b52 (código previo4f429c0), misma rama y reserva propia. Cuatro regresiones contra producción sin modificar reproducen sobrescritura al fijar con JSON ilegible, guardar con versión2, fijar con un registro inválido y borrar una conversación desconocida con JSON ilegible. Los cuatro fallaron como se esperaba; los hashes y XML se conservan para el cierre de evidencia.

ContextStore distingue ausencia de archivo de error de lectura. AtomicFile puede recuperar su respaldo .bak; un archivo existente ilegible no se transforma en vacío. Valida versión1 (o legado sin versión), arrays, capacidad, identidad única y campos necesarios sin recortar silenciosamente textos cargados ni descartar registros. Primero construye ambos conjuntos y sus metadatos; solo después publica la lectura. Una lectura cancelada o fallida nunca habilita save. Se conservan bytes y contexto temporal; no se borran ni reparan automáticamente registros corruptos. No es recuperación general de JSON ni un mecanismo de backups adicional.

La IO de carga se realiza en Dispatchers.IO. ContextStorageStatus, conectado a ContextPanel tanto para contextos como conversaciones, muestra un mensaje genérico sin contenido privado ni rutas y Reintentar lectura. Reintentar lee localmente; no guarda, no fija contexto, no ejecuta acciones ni envía al proveedor. Una lectura válida conserva metadatos desconocidos y permite volver a guardar explícitamente. No hay cambios en barra, Quick Look, Ajustes, preferencias, invocación o estilos normales.

Verificación dirigida actual:40pruebas/7suites, cero fallos/errores/omisiones, compileDebugAndroidTestKotlin correcto en50s. Cinco archivos modificados compilados coinciden byte por byte con el árbol a publicar. Se incluyen nueve casos de almacenamiento, un flujo Compose del componente de estado con un ContextStore/archivo reales del sandbox y regresiones de capacidad, conversaciones, cancelación y Gemini de fixtures. No es ejecución nativa o prueba de inicio físico de la app.

La primera ronda dirigida se interrumpió (exit130), sin resultado de suite: un dispatcher pausado del fixture de propiedad asumía carga sin cambio de hilo. El fixture ahora bombea continuaciones hasta completar con límite5000ms; mantiene todas sus aserciones. El flujo UI usa AtomicBoolean y no confunde waitForIdle con finalización de IO. No se desactivaron pruebas ni aumentaron timeouts para ocultar regresiones. Se mantiene el ajuste local C1 documentado por el crash previo del JVM; no modifica código de la app ni workflows.

Estado inicial al publicar las fuentes (cerrado posteriormente abajo): suite completa, tres lint, R8/Release sin firma, compilación instrumentada y comparación estricta21. El estado integral verificado anterior sigue siendo2de8b52 hasta cerrar estos gates. Sin Actions ni clave de firma nueva, APK parcial, main/PR16/merge/force o cambios de presupuesto. Matriz40 parcial; sin memoria por entidades o embeddings añadidos.


## Cierre del código publicado

Código Android `f987a0e`, sobre `2de8b52`: lectura validada de ContextStore, conservación de bytes y bloqueo de escritura ante error/formato desconocido; reintento local explícito en el componente de estado de ContextPanel. Sin reset de datos, envío remoto o cambio visual normal.

Verificación nueva: 572 pruebas / 121 suites, cero fallos, tres lint sin errores, Release/R8 y compilación instrumentada correctos (BUILD SUCCESSFUL in 11m 19s); 21 comparaciones estrictas intactas. Las 328 entradas compiladas coinciden con Git. Los 40 dirigidos no se suman. Evidencia: `docs/verification/context-recovery-20261017/summary.json`.

Instrumentación nativa no ejecutada por ausencia de dispositivo/KVM. Release interno sin firma, no instalable ni entregado; ninguna clave de firma nueva, APK parcial, Actions, main/PR16 o presupuesto modificado. Matriz 40 aún parcial. Detalle, regresiones y próximo bloque semántico local: `docs/HANDOFF-INTEGRAL-20261017.md` y `docs/CONTEXTO-RECUPERACION-20261017.md`.
