# Estado real — maestro de40 decisiones

Baseline auditado: `115acc47d7e15193e9d3274a7499ae2f5f26a11f`, rama `feat/raycast-sidebar-permissions-20261020`.

Fecha:22/10/2026UTC (21/10 Argentina). C0 es inventario de código y evidencia recuperada, no nueva ejecución de pruebas ni aceptación física. Las40decisiones de este maestro reemplazan la numeración histórica de MATRIZ-40-INTEGRAL-20261016.md.

Recuperación: entorno local limpio091bb3d restaurado a115acc4;82blobs SHA1 y árbolae1cf40 verificados. Referencia local091 preservada. Fetch/status porCLI fallan autenticación; GitHub autenticado confirma HEAD, PR18abierto/borrador y reserva3cde303 propia `work-recovery-20261018-cd7b8fe1285c`. Sin otra ejecución Gradle previa recuperable.

|ID|Decisión|Estado|Evidencia y pendiente|
|---|---|---|---|
|D01|Gris translúcido Raycast|parcial|ui/GlassTokens.kt, ActionPanelView.kt, SettingsNavigation.kt; selección nativa cubierta en Preview34; falta cotejo físico y uniformidad completa.|
|D02|Glass equilibrado|parcial|GlassTokens.kt / GlassAppearance.kt / Preferences.kt; Preview34 usa80% y opacidad ActionPanel derivada, Ajustes compartida. Falta84/92/94 independientes e instantaneidad del tema.|
|D03|Altura adaptativa|parcial|search/CompactResults.kt; home3, consulta3; expansión reversible/identidad probadas en CompactResultsTest. Falta consulta6 y geometría C1.|
|D04|Adaptativo inteligente|parcial|overlay/ActionPopupHost.kt + ui/ContextActionSurface.kt; pruebas ActionPopupHostTest/FloatingSearchFlowTest en Preview34. Falta IME/InputDispatcher/rotación físicos.|
|D05|Preferencias Raycast|parcial|ui/SettingsNavigation.kt + MainActivity.kt; 8áreas y controles reales con DarkGlassSettingsTest. Falta auditoría de exclusividad de permisos/filas planas C3.|
|D06|Minimalista Raycast|pendiente|ui/SpotlightWidgets.kt ResultRow sigue dos líneas en115acc4; maestro exige una y metadato derecho.|
|D07|Originales + monocromáticos|parcial|ResultIcon usa PackageManager/icon real; PDF rojo y utilidades con círculo contradicen monocromo sin cajas.|
|D08|Barra contextual minimalista|parcial|ui/GlassContextFooter.kt; Enter/CtrlK/Tab contextuales probados en FloatingSearchFlowTest; verificar versiones táctiles/acciones disponibles.|
|D09|Tres sugerencias inteligentes|parcial|HomeSuggestions.kt máximo4 internamente, CompactResults home3; una app y señales locales en HomeSuggestionsTest. Unificar límite3 sin relleno.|
|D10|Controles contextuales|parcial|SpotlightBar.kt limpia texto y no botón fijoIA; mic siemprevisible y faltan⋯; VoiceActivity valida resolver al ejecutar.|
|D11|Centro superior adaptativo|parcial|overlay/OverlayService.kt, OverlayLayoutPolicy.kt y AdaptiveLayout.kt; pruebas geometría/IME existentes; no dispositivo OneUI.|
|D12|Microanimaciones Raycast|parcial|OverlayService visibleprimerframe/foco inmediato; cierre55/90/115ms, no entrada140ms. Falta validar movimiento/reducción sin retraso.|
|D13|Dark Glass refinado|implementada y probada|GlassTokens.glassSurface: radio16dp/borde1dp/sombra20dp; capturas nativas Preview34. Aceptación física pendiente, no afirmada.|
|D14|Raycast equilibrada|parcial|ui/Theme.kt Inter incluida; ResultRow15sp/12sp/56dp contradice14/11 y fila44 con hit48.|
|D15|Contextual inteligente|parcial|ui/SpotlightPanel.kt + FilePreview.kt; selección documental local y recursos; ResultTouchScrollTest/QuickLookTouchTargetTest. Física pendiente.|
|D16|Reproductor en inspector|parcial|control/MediaControl.kt + SpotlightPanel detalleapp; estados/controles autorizados probados con fixtures. No reproductor completo unificado/progreso real validado.|
|D17|IA en inspector contextual|parcial|SpotlightPanel.kt, AiPanel.kt, DirectAi.kt: streaming/cancelación/copia existentes. No todo integrado en único inspector.|
|D18|Contextual con opción de fijar|pendiente|ContextPanel.kt fija CONTEXTO persistido, no el inspector visual que siga selección. No confundir ambas capacidades.|
|D19|Esencial Raycast|parcial|GlassQuickMenu.kt anterior: fuentes/tema/privacidad +General/Apariencia/IA/Permisos; falta menú exacto4 yAtajos.|
|D20|Centro inteligente|parcial|CapabilitySettings.kt/SettingsNavigation.kt/Capabilities.kt: estados Android y rutas. Falta certificar todos los permisos y revocaciones reales.|
|D21|En el inspector lateral|parcial|SpotlightPanel.kt contiene formularios existentes; falta reunirlos en único inspector adaptativo y verificar argumentos.|
|D22|Confirmación inteligente|parcial|ai/ToolCapability.kt, ToolExecutor.kt, AiRequestPolicy.kt, agent/PhoneUsePolicy.kt; ToolProtocolTest/PlanExecutionTest y políticas. No certificación appsdestino.|
|D23|Seleccionar y confirmar|parcial|ResultRow.combinedClickable arbitra scroll/cancelación;7pruebasgestos históricas. Panel selecciona siempre, falta segundo toque y restauración segura.|
|D24|Cierre contextual inteligente|parcial|SpotlightPanel.execute conserva vistas/documentos/errores según acción; falta política única de cierre y fijación visual.|
|D25|Búsqueda híbrida inteligente|parcial|search/ResultRanker.kt/CompactResults.kt/SearchEngine.kt +control/CommandCenter.kt; exact/prefix/alias/fuzzy/local/history cubiertos en Preview33; repetir afectadosC1 y medir latencia real.|
|D26|Profunda por capacidades|parcial|control/IntegrationRegistry.kt, ai/ToolRegistry.kt, MediaControl.kt yPhoneUseService.kt; Intents/MediaSession/allowlists existentes. AppFunctions y capacidades externas condicionadas a API/caller/apps.|
|D27|Encadenados editables|parcial|ui/EditablePlanPanel.kt, ai/PlanExecution.kt; edición/cancelación/stop y pruebas existentes; falta dependencias/reanudación/inspector completos.|
|D28|Comandos personalizados inteligentes|parcial|personal/PersonalCommands.kt, Preferences.aliases; expansión local existente y ranking evita desplazar exacta; falta certificar parámetros/planes/editor conflictos.|
|D29|Múltiple configurable|parcial|access/ShortcutService.kt, InvokeActivity.kt yTileService existentes; métodos reales OS; sin validación hotkeyglobalOneUI.|
|D30|Atajos Raycast configurables|parcial|SpotlightPanel.kt/ActionPanelView.kt yActionPanelKeyboardContractTest: Enter/Tab/ShiftTab/CtrlK/CtrlM/Esc existentes. Falta editor general de conflictos.|
|D31|Índice local inteligente|parcial|index/FileIndex.kt/DocumentText.kt; índicelocal incremental/OCRoptin/cotas/semánticalocal existentes, evidencia histórica docs; faltan validaciones físicas y todos los formatos.|
|D32|Integración contextual|parcial|ShareContextActivity.kt/ContextStore.kt yAndroidManifest.xml: compartir explícito/grants; falta certificar ACTION_SEND_MULTIPLE y revocación en appsreales.|
|D33|Contextuales inteligentes|parcial|data/ContextStore.kt, ui/ContextPanel.kt: conversaciones locales/cancelación/recuperación probadas; falta asociación uniforme tarea/documento en inspector.|
|D34|Motor modular|parcial|ai/DirectAi.kt/ToolAgent.kt ypreferenciasproveedor: Gemini/OpenAI/backend; clavesprotegidas yerrores. No proveedorreal/cuotas comprobados en este entorno.|
|D35|Compactos + inspector|parcial|next/NativeProviders.kt/search/SearchEngine.kt: calculadora yconsultasexternas existentes; fuentes/tiempo/offline e inspector completos por certificar, fixtures≠servicioreal.|
|D36|Microestados contextuales|parcial|SpotlightPanel.kt estadoserror/carga, ContextPanel.kt recuperación y cancelación existentes; faltan todos los microestados unificados.|
|D37|Rendimiento adaptativo inteligente|parcial|FileIndex.kt/DocumentText.kt límites e incremental; cancelación SearchEngine yLRU existentes. Cold/warm/RAM/batería/OneUI NO medidos.|
|D38|Inteligente y configurable|parcial|Preferences.privateMode, AiRequestPolicy/ContextStore/ConfigTransfer: consentimiento/local/borrado; faltan cobertura integral de borrados separados y pruebas físicas.|
|D39|Actualización segura + respaldo local|parcial|data/ConfigTransfer.kt/ThemeTransfer.kt ySettingsPersistenceTest: formatos validados/sinAPIkeys; identidadPreview34 verificada. Falta migración nuevosroles/rollback e instalación de actualización real.|
|D40|Calidad por checkpoints|parcial|docs/verification/dark-glass-20261021 contiene56casos únicos/lint0errores/15avisos/1info yAPK34+ZIPcc1c28d. Son evidenciaHISTÓRICA; C1–C5 yR8/física nuevos pendientes.|

## Siguiente bloque C1

Implementar sin segundo motor: límites home3/consulta6, filas1línea14/11 y hit48, utilidades monocromas, segundo toque deliberado/Enter con scroll seguro. Después roles84/92/94 persistentes y tema inmediato; controles/foco/geometría/motion y regresión dirigida. No declarar C1 estable ni nueva APK hasta sus gates.

La especificación/prompt/CSV originales y hashes están en docs/master-40-20261021. Imágenes03/04 material,01/02 composición; sus8hashes fueron verificados. Ningún render es capturaAndroid.
