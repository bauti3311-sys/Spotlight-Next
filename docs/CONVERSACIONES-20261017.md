# Conversaciones guardadas y continuación autorizada

Bloque posterior al checkpoint de gates e768a6a. No cambia la barra Obsidian Glass, el overlay, Quick Look, las preferencias ni los identificadores de aplicación. Usa ContextStore, AiRequestPolicy y DirectAi existentes; no incorpora otro motor IA.

Guardar desde la respuesta inline conserva todos los intercambios completos de la sesión en memoria, hasta seis, con roles explícitos y un máximo total de 48.000 caracteres. No guarda una respuesta interrumpida ni incorpora el contexto activo sin seleccionarlo. El botón muestra el resultado o error y bloquea guardados simultáneos. Al alcanzar 30 conversaciones se rechaza un nuevo guardado en lugar de expulsar datos anteriores.

Los registros nuevos conservan también su texto legible en el formato anterior. Los registros legados siguen legibles y copiables, pero no se intenta reconstruir roles a partir de su texto. Campos desconocidos de conversaciones válidas y del documento raíz se conservan al guardar. La escritura espera la carga previa para no reemplazar datos existentes por una lista inicialmente vacía.

Abrir una conversación desde la búsqueda permite introducir una nueva pregunta. El botón Revisar continuación congela los cinco intercambios completos más recientes y la pregunta. La revisión muestra todos sus textos y roles antes de Enviar; el contenido enviado coincide con esa revisión, sin mensajes de otras sesiones ni lectura nueva de adjuntos. Escribir o cancelar no envía nada. El historial explícito exige revisión aunque su procedencia se marque como consulta de usuario. La política anterior de Enter para una pregunta nueva se conserva.

Eliminar una conversación requiere confirmación. La conversación original no se sustituye al continuar: guardar el hilo resultante crea otro registro local. No hay resumen automático, memoria semántica ni envío diferido. La reducción a cinco intercambios se comunica en la vista y deja espacio para la pregunta sin truncamiento adicional del transporte.

## Verificación dirigida

Comando: `:app:testDebugUnitTest --tests '*ConversationPersistenceTest' --tests '*UniversalProductionFlowTest' --tests '*Glass20CoreTest' --tests '*AutomaticContextRegressionTest' --tests '*AiTransportTest'`.

48 pruebas correctas, cero fallos/errores/omisiones, BUILD SUCCESSFUL en 1 min 22 s. Incluye siete casos nuevos de persistencia/roles/límites/privacidad y tres recorridos nuevos del flujo de producción. Android es Robolectric API33; HTTP y claves son fixtures de MockWebServer. No se hicieron consultas reales al proveedor, uso de una clave del usuario ni pruebas físicas. Gitleaks del árbol sin hallazgos; diff --check correcto. Los gates completos de este bloque todavía están pendientes en su primer checkpoint.
