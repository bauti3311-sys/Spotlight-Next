# Estado real — maestro de40 decisiones

Baseline auditado: `115acc47d7e15193e9d3274a7499ae2f5f26a11f`, rama `feat/raycast-sidebar-permissions-20261020`.

Fecha:22/10/2026UTC (21/10 Argentina). C0 es inventario de código y evidencia recuperada, no nueva ejecución de pruebas ni aceptación física. Las40decisiones de este maestro reemplazan la numeración histórica de MATRIZ-40-INTEGRAL-20261016.md.

Recuperación: entorno local limpio091bb3d restaurado a115acc4;82blobs SHA1 y árbolae1cf40 verificados. Referencia local091 preservada. Fetch/status porCLI fallan autenticación; GitHub autenticado confirma HEAD, PR18abierto/borrador y reserva3cde303 propia `work-recovery-20261018-cd7b8fe1285c`. Sin otra ejecución Gradle previa recuperable.

|ID|Decisión|Estado|Evidencia y pendiente|
|---|---|---|---|
|D01|Gris translúcido Raycast|implementada y probada|GlassTokens.selectedColors gris neutro; SettingsNavigation/ActionPanelView coherentes. C1.1 capturas y71casos dirigidos; OneUI físico pendienteC5.|
|D02|Glass equilibrado|parcial|C1.1 Config/Preferences/GlassTokens/GlassAppearance: roles84/92/94 independientes, persistencia/preview/tema inmediato, backup antiguo/concurrencia correctos (MasterC1MaterialTest/DarkGlassSettingsTest). Blur/contraste físico pendientes.|
|D03|Altura adaptativa|parcial|C1.1 CompactResults home3/consulta6, conserva todo el índice y selección alcontraer; CompactResultsTest/MasterC1ResultsTest/MinimalistSearchFlowTest prueban6filasvisibles yaltura. IME/rotación físicos pendientes.|
|D04|Adaptativo inteligente|parcial|overlay/ActionPopupHost.kt + ui/ContextActionSurface.kt; pruebas ActionPopupHostTest/FloatingSearchFlowTest en Preview34. Falta IME/InputDispatcher/rotación físicos.|
|D05|Preferencias Raycast|parcial|ui/SettingsNavigation.kt + MainActivity.kt; 8áreas y controles reales con DarkGlassSettingsTest. Falta auditoría de exclusividad de permisos/filas planas C3.|
|D06|Minimalista Raycast|implementada y probada|C1.1 ResultRow una línea14sp ymetadato11spderecho; capturas NATIVE tablet/teléfono ypruebas de selección/expansión. Detalle fuera de la fila.|
|D07|Originales + monocromáticos|implementada y probada|C1.1 ResultIcon conserva artworkPM; utilidades/PDF monocromos sincírculos/tarjetas. Capturas de fixtures distinguidas de appsinstaladas físicas.|
|D08|Barra contextual minimalista|parcial|ui/GlassContextFooter.kt; Enter/CtrlK/Tab contextuales probados en FloatingSearchFlowTest; verificar versiones táctiles/acciones disponibles.|
|D09|Tres sugerencias inteligentes|implementada y probada|C1.1 HomeSuggestions MAX_TOTAL_SUGGESTIONS3/MAX_SUGGESTED_APPS1, sinrelleno/remoto. HomeSuggestionsTest4/4 yflujos nativos.|
|D10|Controles contextuales|implementada y probada|C1.1 SpotlightBar limpiar/⋯; dictado consultado asíncronamente por resolverAndroid, sinbotónIAfijo. MasterC1TouchFlowTest confirma disponible/ausente yconservación consulta.|
|D11|Centro superior adaptativo|parcial|overlay/OverlayService.kt, OverlayLayoutPolicy.kt y AdaptiveLayout.kt; pruebas geometría/IME existentes; no dispositivo OneUI.|
|D12|Microanimaciones Raycast|parcial|OverlayWindowMotion/OverlayService apertura140/cierre100;16dirigidas C1correctas (2motion/8startup/6IME). Foco/input antes de terminar animación y reducción cubiertos; FPS/latencia/OneUI físicos pendientes.|
|D13|Dark Glass refinado|implementada y probada|GlassTokens.glassSurface: radio16dp/borde1dp/sombra20dp; capturas nativas Preview34. Aceptación física pendiente, no afirmada.|
|D14|Raycast equilibrada|parcial|C1.1 ResultRow14sp/metadato11sp/hit48 ya verificados; C2accionesIAahora48dp explícitos con test. FontScale/TalkBack físicos pendientes; no usar descripción histórica15/12/56.|
|D15|Contextual inteligente|parcial|Preview39: AuthorizedFilePreview revalida URI antes de leer y al recuperar lifecycle/foco; dispone PDF/audio/vídeo al inactivarse. Descriptor SAF y tarjeta guardables sin grants/Intent vivo/texto documental; prueba ContentProvider local y Activity.recreate. PDF instrumentado y revocación en proveedor físico pendientes.|
|D16|Reproductor en inspector|parcial|MediaControl.inspect/MediaSnapshot/MediaInspector integrados: metadatos/bitmap/progreso/controles del paquete exacto, acciones soportadas y seek con token actual. MediaInspector6+MediaTarget9+MediaInspectorFlow3 correctos en rondas; sin Spotify/OneUI real. Preview37; ver PREVIEW37-INSPECTOR-20261022.md.|
|D17|IA en inspector contextual|parcial|Preview38: propuesta/argumentos/orden/resultados parciales guardables por tarea en inspector existente; confirmación nueva y sin replay. Conversación activa/turnos restaurados con ActivityScenario.recreate en Robolectric; se corrigió efecto query que ocultaba IA restaurada. Fixtures≠proveedor/hardware real; muerte de proceso y ramas restantes pendientes.|
|D18|Persistencia contextual + fijar|parcial|Preview39: documento/página/expansión/ámbito y tarjeta recuperados; permisos revalidados en lifecycle y foco de ventana, confirmación sensible invalidada.7casos nuevos y regresiones afectadas. Savers y ActivityScenario no equivalen a snapshot durable del OverlayService: performRestore(null), proceso muerto y variantes locales siguen pendientes.|
|D19|Esencial Raycast|implementada y probada|C1.1 GlassQuickMenu cuatro rutas reales Ajustes/Apariencia/Permisos/Atajos separadas deCtrlK; RaycastQuickMenuComposeTest yMasterC1TouchFlowTest.|
|D20|Centro inteligente|parcial|Preview38: ruta antigua de permisos redirige al centro único MainActivity. Prueba de navegación sin toggles de concesión y capturas de páginas sin solicitudes. APIs y señales reales de código existentes; certificación de permisos/revocación en dispositivos pendiente.|
|D21|En el inspector lateral|parcial|Preview38: consulta y formulario real restaurados después de ActivityScenario.recreate, conservando borrador. MessageTaskSaver conserva datos, no confirmación/contacto resuelto. Inspector/estado guardable existentes; mensajería y hardware reales pendientes.|
|D22|Confirmación inteligente|parcial|ai/ToolCapability.kt, ToolExecutor.kt, AiRequestPolicy.kt, agent/PhoneUsePolicy.kt; ToolProtocolTest/PlanExecutionTest y políticas. No certificación appsdestino.|
|D23|Seleccionar y confirmar|implementada y probada|C1.1 SpotlightPanel primer toque deliberado/segundoejecuta/Enterdirecto/longacciones; resetea confirmación al navegar/scroll/query. MasterC1TouchFlowTest5/5 yResultTouchScrollTest7/7 (PDFfixture). Validación física pendienteC5.|
|D24|Cierre contextual inteligente|parcial|Cierre contextual: Copycierra al terminar sin tarea/inspector; fijación y documentos conservan superficie; Launch/Shortcut/InApp respetanretención, Devicecierra solo completed/verified. Caso localCopy/pin probado; rutasexternas físicas pendientes.|
|D25|Búsqueda híbrida inteligente|parcial|search/ResultRanker.kt/CompactResults.kt/SearchEngine.kt +control/CommandCenter.kt; exact/prefix/alias/fuzzy/local/history cubiertos en Preview33; repetir afectadosC1 y medir latencia real.|
|D26|Profunda por capacidades|parcial|control/IntegrationRegistry.kt, ai/ToolRegistry.kt, MediaControl.kt yPhoneUseService.kt; Intents/MediaSession/allowlists existentes. AppFunctions y capacidades externas condicionadas a API/caller/apps.|
|D27|Encadenados editables|parcial|Preview38: propuestas editables mantienen argumentos y resultados al restaurar; WorkflowRunner conserva entrada/salida parcial/progreso y no continúa aprobación suspendida. PlanExecution7 regresiones y casos nuevos aprobados. Repetir explícito con confirmación; dependencias/reanudación externa avanzada aún parcial.|
|D28|Comandos personalizados inteligentes|parcial|personal/PersonalCommands.kt, Preferences.aliases; expansión local existente y ranking evita desplazar exacta; falta certificar parámetros/planes/editor conflictos.|
|D29|Múltiple configurable|parcial|access/ShortcutService.kt, InvokeActivity.kt yTileService existentes; métodos reales OS; sin validación hotkeyglobalOneUI.|
|D30|Atajos Raycast configurables|parcial|Editor KeyboardShortcutEditor para4atajos locales; detección de conflictos con comandos/edición/acceso global, persistencia DataStore y backup validado. Handlers/ActionPanel/pistas usan mismo binding. KeyboardBindings6+KeyboardShortcutEditor2+ActionPanelKeyboardContract6 correctos; global sigue3combinaciones soportadas/servicio opcional, OneUI físico pendiente.|
|D31|Índice local inteligente|parcial|index/FileIndex.kt/DocumentText.kt; índicelocal incremental/OCRoptin/cotas/semánticalocal existentes, evidencia histórica docs; faltan validaciones físicas y todos los formatos.|
|D32|Integración contextual|parcial|ShareContextActivity.kt/ContextStore.kt yAndroidManifest.xml: compartir explícito/grants; falta certificar ACTION_SEND_MULTIPLE y revocación en appsreales.|
|D33|Contextuales inteligentes|parcial|Preview38: conversación activa y turnos autorizados guardables; Activity recreada no reenvía y follow-up incluye3turnos correctos. Cache RAM por tarea y ContextStore durable existentes; muerte de proceso y todas las ramas contextuales pendientes.|
|D34|Motor modular|parcial|ai/DirectAi.kt/ToolAgent.kt ypreferenciasproveedor: Gemini/OpenAI/backend; clavesprotegidas yerrores. No proveedorreal/cuotas comprobados en este entorno.|
|D35|Compactos + inspector|parcial|next/NativeProviders.kt/search/SearchEngine.kt: calculadora yconsultasexternas existentes; fuentes/tiempo/offline e inspector completos por certificar, fixtures≠servicioreal.|
|D36|Microestados contextuales|parcial|SpotlightPanel.kt estadoserror/carga, ContextPanel.kt recuperación y cancelación existentes; faltan todos los microestados unificados.|
|D37|Rendimiento adaptativo inteligente|parcial|FileIndex.kt/DocumentText.kt límites e incremental; cancelación SearchEngine yLRU existentes. Cold/warm/RAM/batería/OneUI NO medidos.|
|D38|Inteligente y configurable|parcial|Preferences.privateMode, AiRequestPolicy/ContextStore/ConfigTransfer: consentimiento/local/borrado; faltan cobertura integral de borrados separados y pruebas físicas.|
|D39|Actualización segura + respaldo local|parcial|Preview39: colección visual de20temas validada/canónica, roundtrip de alias/quicklinks, rechazo atómico de todo el respaldo y compatibilidad de backups1parciales. ConfigTransfer/ThemeTransfer/Preferences.importPortable;4casos nuevos+9regresiones de import/config. No restaura grants/secretos/consentimientos. SAF/proveedor y actualización física pendientes.|
|D40|Calidad por checkpoints|parcial|Preview39/6b433ff:29casos únicos dirigidos, lint0errores/13avisos/1info, APK39/ZIP mismoSHA/certificado885838aa/V2/zipalign16KB;204inputs/1238blobs cotejados. Seis capturas API33/NATIVE con procedencia. SinReleaseR8/serviciosreales/proceso muerto/hardware. C2–C5 parciales según límites documentados.|

## Pendientes actuales C1/C2

C1: editor general de atajos/detector de conflictos y mediciones/IME/fontScale físicos, sin rehacer C1.1 ni sus71casos. C2: completar metadata/progreso MediaSession real en el inspector, conservación de todos los planes/herramientas y revocación de recursos; instalación de Preview36 en OneUI/Motorola separada. C3–C5 conservan los pendientes documentados. Ver RECOVERY-C2-20261022.md para evidencia nueva y siguiente bloque concreto.

La especificación/prompt/CSV originales y hashes están en docs/master-40-20261021. Imágenes03/04 material,01/02 composición; sus8hashes fueron verificados. Ningún render es capturaAndroid.

## C1.1 verificado —22/10Argentina

Código5c892537:71casos únicos correctos en rondas, docs/verification/master-c1-20261022/tests.json (desde docs). No se suma evidencia histórica. Implementación nueva: D01 selección neutra, D02 roles84/92/94 independientes persistentes con preview/tema inmediato y rollback; D03 home3/consulta6 y expansión sin truncar índice; D06/07/14 filas de una línea14sp/metadato11sp/hit48/iconosmonocromos; D09home3/unaapp; D10 dictado disponible asíncrono/limpiar/⋯; D19 menú4destinos; D23segundotoque/Enter/long/scroll; D25ranking existente preservado; D30teclas probadas.

El inventario superior parte de115acc4 y se actualizó porID con C1.1. C1.1 implementa y prueba automáticamente D01,D06,D07,D09,D10,D19,D23; D02/D03/D14 tienen cobertura automatizada nueva pero siguen condicionados a contraste/IME/fontScale físico. D11/D12/D25/D30 continúan parciales: faltan anclaje/IME físico, microanimaciones140/100, medición de latencia y editor general de conflictos. D04/C2 conserva ActionPanel flotante probado, no se declara C2 cerrado. No nuevo permiso, proveedor, motor ni dependencia. C3–C5 y las demás capacidades parciales siguen abiertas.

Lint/assembleRaycastPreview activos al redactar; APK35/firma/ZIP/hashes no se declaran entregados hasta informe final. Capturas API33/Robolectric NATIVE con apps/proveedores fixture; sin wallpaper ni InputDispatcher físicos. Comparativa contra03/04 respeta material y01/02composición; prevalecen filas1línea del maestro. No afirmar100%de decisiones terminadas.

C1.1 gatescerrados:producciónce46279,71casosúnicosdirigidos,últimos2dedictadocorrectos,lint0errores/13advertencias/1infoyassembleRaycastPreviewcorrectos3m6. Preview35debug-derived/ARM64;NOReleaseR8. Fuente finalAPK/ZIPyhashesseañadenalmanifiestodeentrega, sinaplicarcompletitudfísicaalosIDsparciales. Automatizaciónsiguepausada.

EntregaC1.1:fuenteAPK/ZIPf497f8b,Preview35/certificadohistórico/193inputs/1067blobsverificados. GatesyhashesenMASTER-C1-20261022.md,71casosúnicos/lint0errores. D40continúaparcialhastaC5/R8/física;ningunadecisióndependiendodelhardwaresepromuevedebidoaestaAPK.


## Continuidad recuperada C1/C2 ·22/10
Reserva transferida con autorización explícita, CAS/FF, respaldo y propietario nuevo work-master-recovery-20261022-cc97a1bd; ver RECOVERY-C2-20261022.md. Código C1motion671e262:16/16casos dirigidos aprobados (OverlayWindowMotion2,OverlayStartup8,OverlayImeHeight6), sin repetir71dePreview35. D12 apertura140/cierre100 con foco disponible antes de finalizar animación y reducción inmediata; OneUI/performance física pendientes. D14ya implementa14/11/hit48segúnC1.1; el texto histórico superior de15/12/56no describeelHEADactual.

C2tests33134ec:formulario sustituye lista/inspector ausente reproducido en prueba nativa; test de nota encontró fixture que leía StateFlow antes de publicación y se corrigió esperando el registro real. No se afirma segunda reproducción funcional por ese error. Corrección local integra formularios/IA/planes al ContextSplitPanel, inspector fijable explícito, retención de contenido/consulta y movableContent para ancho tablet/teléfono; aún NO verificada. D17/D18/D21/D24siguen parciales hasta pruebas nuevas. No servicios externos ni física acreditados; sin CI/main/merge/firma nueva.


## C1.2/C2.1 ·checkpoint instalable36
FuenteAPK/ZIP7d9994b1aca0507406e2614e01a270b81173ecbd;59casos únicos dirigidos (16C1 +39retenidos de regresión +4nuevos finales, sin contar repeticiones), última pruebaIAconobjetivo48dp1/1correcta. Lint0errores/13avisos/1info yassemble3m35s; reempaquetadosolo12s (1tarea,36up-to-date) elimina16.3MBdehuecos ZIP sin cambiar payload/código. Paqueteindependiente36/certificado885838aa/1102blobs fuente/195inputs/8libsARM64idénticasaAAR/zipalign16KBverificados. NoR8/CI/instalaciónfísica/proveedorreal. Matrizmantieneparciales.

### Preview37 ·22/10/2026 ·continuación de Preview36
Código APK/ZIP bcca55163a46e407d3899867f9c05507d479c5cb;48casos únicos correctos en rondas dirigidas (no repetir59históricos),lint0errores/13advertencias/1informativa,assemble4m28s. Paquete independiente/37/certificado885838aa/ARM64/zipalign16KB,1128blobs fuente/201inputs verificados. MediaSession contextual, borradores/planes y4atajos configurables; capturas nativas API33, no física. Fuente/documentos posteriores no cambian producción. Reserva work-master-recovery-20261022-cc97a1bd conservada, PR18borrador, sinActions/main/force. Ver docs/PREVIEW37-INSPECTOR-20261022.md.

### Preview38 ·C2 recuperación / C3 inicio ·22/10/2026 Argentina
CódigoAPK/ZIPe83b99d5fc594fd89f2e760266e1f30ad4ea9339;38casos únicoscorrectos entre nuevos yregresiones afectadas, sin sumar/repetir48históricos. Gate final3m42s:2casos afectados,lint0errores/13avisos/1informativa,assemblePreviewcorrecto. Package38/certificado885838aa/ARM64/zipalign16KB;1170blobs/202inputs verificados. CapturasAPI33conprocedencia463a35f yfixesdelógica posteriores identificados. Reserva propia yPR18borrador conservados;sinActions/main/force. C2/C3 siguenparciales donde faltan recursosdocumentales/tarjetas, muerte deproceso, SAF/temas yvalidación física; ver docs/PREVIEW38-C2-C3-20261022.md.


### Preview39 · C2 documental / C3 temas · pruebas dirigidas correctas
Restauración de descriptores SAF/página/expansión/ámbito y tarjetas recuperadas, sin serializar grants, Intent vivo ni texto extraído. Acceso revalidado al recrear, reanudar y recuperar foco de WindowManager; hijo PDF/audio/vídeo retirado mientras inactivo. La revisión sensible restaura reviewed=false y respuestas de tarjeta se atan al contexto. Colección de temas visuales portable validada dentro de la transacción existente, backups anteriores conservadores, alias/quicklinks y datos privados preservados. FlowRow/48dp para acciones de tarjeta en teléfono/fuente130%.
29casos únicos correctos en rondas dirigidas (11nuevos+18regresiones); no se suman38históricos. XML/logs y fallos de fixtures conservados en docs/verification/preview39-20261022. Gate lint/assemble39 pendiente al guardar este checkpoint. C2/C3 permanecen parciales: OverlayService todavía performRestore(null), sin persistencia durable de su registro ante muerte abrupta; faltan variantes locales y SAF/proveedores/actualización físicos. Las pruebas de reconstrucción de Activity no certifican recuperación del proceso ni OneUI.


### Preview39 · entrega instalable verificada
APK/ZIP código6b433ff328df57ed28183bcb7dc977e5e1f97ab9;29casos únicos dirigidos correctos en rondas, no batería histórica completa. Gate lint/assemble1m3s correcto:0errores/13avisos/1informativa. Preview39/certificado885838aa/ARM64/V2/zipalign16KB;204inputs/1238blobs cotejados. Seis capturas API33/NATIVE con procedencia por escena; componentes documentales diferenciados de paleta/Ajustes completos. Sin R8, OneUI/Moto, servicios externos reales, PDF instrumentado ni proceso muerto. C2/C3 siguen parciales por snapshot durable de OverlayService, variantes locales y SAF/update físico. Ver docs/PREVIEW39-RECOVERY-SETTINGS-20261022.md y verification/preview39-20261022. SinActions/main/force/nuevasfirmas; PR18borrador y reserva propia conservados.
