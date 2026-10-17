# Conversaciones guardadas y continuación autorizada

Bloque posterior al checkpoint de gates e768a6a. No cambia la barra Obsidian Glass, el overlay, Quick Look, las preferencias ni los identificadores de aplicación. Usa ContextStore, AiRequestPolicy y DirectAi existentes; no incorpora otro motor IA.

Guardar desde la respuesta inline conserva todos los intercambios completos de la sesión en memoria, hasta seis, con roles explícitos y un máximo total de 48.000 caracteres. No guarda una respuesta interrumpida ni incorpora el contexto activo sin seleccionarlo. El botón muestra el resultado o error y bloquea guardados simultáneos. Al alcanzar 30 conversaciones se rechaza un nuevo guardado en lugar de expulsar datos anteriores.

Los registros nuevos conservan también su texto legible en el formato anterior. Los registros legados siguen legibles y copiables, pero no se intenta reconstruir roles a partir de su texto. Campos desconocidos de conversaciones válidas y del documento raíz se conservan al guardar. La escritura espera la carga previa para no reemplazar datos existentes por una lista inicialmente vacía.

Abrir una conversación desde la búsqueda permite introducir una nueva pregunta. El botón Revisar continuación congela los cinco intercambios completos más recientes y la pregunta. La revisión muestra todos sus textos y roles antes de Enviar; el contenido enviado coincide con esa revisión, sin mensajes de otras sesiones ni lectura nueva de adjuntos. Escribir o cancelar no envía nada. El historial explícito exige revisión aunque su procedencia se marque como consulta de usuario. La política anterior de Enter para una pregunta nueva se conserva.

Eliminar una conversación requiere confirmación. La conversación original no se sustituye al continuar: guardar el hilo resultante crea otro registro local. No hay resumen automático, memoria semántica ni envío diferido. La reducción a cinco intercambios se comunica en la vista y deja espacio para la pregunta sin truncamiento adicional del transporte.

## Verificación dirigida

Comando: `:app:testDebugUnitTest --tests '*ConversationPersistenceTest' --tests '*UniversalProductionFlowTest' --tests '*Glass20CoreTest' --tests '*AutomaticContextRegressionTest' --tests '*AiTransportTest'`.

48 pruebas correctas, cero fallos/errores/omisiones, BUILD SUCCESSFUL en 1 min 22 s. Incluye siete casos nuevos de persistencia/roles/límites/privacidad y tres recorridos nuevos del flujo de producción. Android es Robolectric API33; HTTP y claves son fixtures de MockWebServer. No se hicieron consultas reales al proveedor, uso de una clave del usuario ni pruebas físicas. Gitleaks del árbol sin hallazgos; diff --check correcto. Estos fueron los resultados dirigidos del primer checkpoint 0c2f74f.

## Gates completos de 0c2f74f

493 pruebas Android/JVM correctas en 104 suites, cero fallos/errores/omisiones. Las 48 dirigidas están incluidas, no se suman de nuevo. Lint Debug, Release y functionalPreview sin errores, cada variante con las 10 advertencias y una informativa heredadas. Release/R8 sin firma correcta; pipeline completo en 10 min 33 s. Verificación posterior de 21 referencias visuales correcta en 21 s, sin grabar ni cambiar ninguna. Los 303 archivos de entradas compiladas coinciden con Git.

El artefacto interno de R8 conserva paquete principal, versión 2.0.1/código29, ARM64/min26/target35 y ausencia de debuggable; zipalign16KB correcto. Las cadenas de la continuación nueva están presentes en DEX minificado. Está deliberadamente sin firmar y no se entrega para instalar. No se generó una clave nueva de P1 ni se utilizaron secretos históricos.

Evidencia: [verification/conversations-20261017/summary.json](verification/conversations-20261017/summary.json), con comandos, logs, JUnit consolidado, diagnósticos lint, huellas de entradas y resultados visuales. Gitleaks sin hallazgos en árbol y 12 commits locales disponibles. Python no cambió: se conservan las 30+5 pruebas anteriores, sin volver a contarlas como ejecutadas ahora. Las 40 decisiones no están completas; no hubo entrega de APK parcial, Actions ni validación física/de proveedor.
