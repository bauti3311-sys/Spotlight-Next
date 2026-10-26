# D27 · reproducción y corrección funcional

Base d740a112, reproducción64168b6, implementaciónba025c2. Caso real RelativePlanChainTest: esperaba50 y observó10, 1/1fallida; BUILD FAILED5m31. XML completo y resumen de tareas preservados en plan-red. No fue fallo de Gradle.

Ronda posterior:23casos,22correctos y1timeout en NativeCommandFlowTest.searchOpensStoredReviewAndExplicitConfirmationChangesVolume. Pasaron el caso original,6proyecciones,2flujos nuevos de edición/restauración,7PlanExecution,2ContextualDraftRestore y4de5NativeCommandFlow. XML íntegros en plan-round1. No se afirma ronda verde completa.

El timeout espera abrir el comando tras UN toque. Producción SpotlightPanel exige seleccionar y después confirmar para resultados no-app/documento (D23), comportamiento anterior a este fix. Se conserva la prueba y se fortalece: primer toque no abre revisión ni cambia volumen, segundo abre y confirmación explícita ejecuta. No se alarga timeout ni reduce aserciones. Se repite solamente este método en la ronda2 junto al bloque nuevo de compartir.

Proyección pura de brillo/volumen según pasos seleccionados/orden; baseline conservado al alternar selección; mismas llamadas revisadas para ejecutar y guardar comandos. Omitidos/no disponibles no se usan como dependencias. Añadir paso local validado, máximo6, borrador saveable, sin scripts ni ejecución por restauración. D27 permanece PARCIAL: falta edición completa de propuestas IA/dependencias generales, no replay/reintento automático externo.

Sin cambios de permisos, formato persistente, proveedor, firma o CI. No se repitieron suites estéticas ni baterías completas. Validación externa/física pendiente.
