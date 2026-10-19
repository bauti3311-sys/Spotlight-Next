# Gemini: propuestas completas antes de ofrecer herramientas

## Resultado

Código `df97608f97fd5721d8af1d2adb2f45ea61d36fe1`. Verificación dirigida aprobada: 21 pruebas, 3 suites, 0 fallos, errores u omisiones. ToolProtocolTest 10, PlanExecutionTest 7, ToolCapabilityTest 4. Gradle BUILD SUCCESSFUL en 1m 17s. XML y resumen en `verification/gemini-completion-20261018`. Código y test compilados coinciden byte a byte con el checkpoint.

Comando: `:app:testDebugUnitTest --tests dev.bautista.spotlight.ai.ToolProtocolTest --tests dev.bautista.spotlight.ai.PlanExecutionTest --tests dev.bautista.spotlight.ai.ToolCapabilityTest`, con el init C1 existente exclusivo del JVM de pruebas. Gradle 8.10.2, JDK 17, Robolectric SDK 33 y Roborazzi 1.29.0 sin cambios de dependencias. Lint posterior pendiente al redactar este checkpoint.

## Reproducción y corrección

Base `043ff241034977d9a2ecbbe3bae998591326f73c`: 10 pruebas, 3 fallos, 0 errores/omisiones. XML RED preservado. Fallos observados: LANGUAGE aceptado como propuesta completa; prompt SAFETY aceptado pese a bloqueo; llamada marcada thought ofrecida como acción. Los dos últimos fixtures son respuestas contradictorias/malformadas, no respuestas observadas de un proveedor real.

El parser de generateContent no streaming ahora exige STOP, rechaza bloqueos de prompt y excluye partes thought completas. STOP normal conserva herramientas, explicación y validación tipada mediante ToolRegistry. Se mantienen autorización y ejecución serial existentes, sin ejecutor paralelo. DirectAi streaming no cambia.

Referencias oficiales consultadas: https://ai.google.dev/api/generate-content (contrato de finalización y bloqueos) y https://developers.raycast.com/api-reference/tool (revisión contextual de acciones). Implementación Kotlin nativa.

## Límites y continuidad

Fixtures locales, sin solicitudes, claves ni gastos de proveedor. No acredita Gemini real, permisos físicos ni One UI. Las decisiones 4, 16, 18 y 20 siguen parciales; este bloque mejora manejo de respuestas y propuestas, no completa esas capacidades. PDF mantiene sus 17 pruebas aprobadas y lint Debug sin errores. No se generó APK, no se ejecutó Actions y no se modificaron firmas, UI o main.

19/10: XML recuperados y revisados tras el corte de cuota. El resultado del lint posterior no sobrevivió en /tmp; no se declara aprobado y se repetirá al restaurar el entorno.
