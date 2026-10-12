# Arquitectura 2.0

SpotlightApp mantiene componentes existentes. OverlayService administra una única ventana con Lifecycle/SavedState/ViewModel; SpotlightPanel conserva búsqueda, navegación y revisión. No se reemplazan DataStore spotlight, el índice SQLite ni CredentialVault.

SearchEngine envuelve las fuentes históricas con RootActions y comandos del ExtensionRegistry existente. LocalLanguage normaliza verbos y porcentajes; preserva nombres, acentos y texto de mensajes cuando no requieren transformación. ResultRanker combina relevancia, exactitud, prioridades acotadas y aprendizaje local limitado. StableResults protege navegación y deduplica acciones. ResultSections.unified limita la presentación, no la capacidad del buscador. La normalización, ranking e indexación no se ejecutan en el hilo principal. El refresco de archivos se limita a diez minutos y conserva invalidaciones existentes.

LocalPlan produce tipos cerrados. EditablePlanPanel edita, ordena, omite, comprueba permisos, confirma y muestra estados por paso. ToolRegistry valida argumentos; ToolExecutor vuelve a comprobar disponibilidad al ejecutar. Los comandos guardados usan Workflows, sin segundo motor de automatización. MediaControl necesita una sesión real accesible mediante el NotificationListener autorizado.

ContextStore recibe selección explícita, SAF o Compartir. Contextos temporales de diez minutos, referencias/textos fijados y conversaciones guardadas manualmente. Copias de permisos transitorios están limitadas a 20 MB y se eliminan al quitar el contexto. Extracción bajo acción explícita, nunca al escribir. La IA mantiene conversación temporal, ofrece revisión del contenido sensible y permite preparar un mensaje posterior con selección de destinatario y confirmación independientes.

Preferences.privateMode bloquea nuevos recientes, hábitos, secuencias, captura y auditoría de herramientas; una revisión monotónica evita restaurar el estado de privacidad por una emisión atrasada. No borra elementos guardados. Las secuencias repetidas se proponen después de tres repeticiones, con un máximo de cincuenta y vencimiento de treinta días; se excluyen textos de mensajes de este mecanismo.

SportsFavorites guarda entidades en un registro distinto de los antiguos eventos fijados. NativeProviders consulta identificadores de equipos/ligas y valida que los encuentros correspondan al favorito. Cache local acotada, datos desactualizados identificados, errores sin claves. La clave propia opcional de TheSportsDB se guarda en CredentialVault; nunca se integra en DataStore, temas, Git ni diagnósticos.

Diagnostics espera primer dibujo y foco del campo, cualquiera sea su orden. Guarda tiempos y percentiles sin consultas, contactos o rutas. Una apertura cercana al inicio del proceso se clasifica fría; esto no mide por sí solo el rendimiento físico de la Tab A9+.
