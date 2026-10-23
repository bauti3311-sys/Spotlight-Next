# Spotlight Next · candidata integral40 ·22/10/2026 Argentina

**Candidata a validación física; C5 no está cerrado.** Código APK/ZIP `6c897354c0c7e5d9668966ed2d582aa268d519ac`. Rama feat/raycast-sidebar-permissions-20261020; PR18 borrador; reserva work-master-recovery-20261022-cc97a1bd conservada. Sin Actions, main, force-push, nueva clave ni APKs intermedias.

## Cambios consolidados C2–C4
- Registro Compose del overlay durable dentro de ContextStore/AtomicFile existente, sin otro inspector ni motor. Codec tipado para MutableState y datos guardables, con cotas384KB/profundidad/nodos y rechazo de recursos vivos/Intent/grants/bitmaps. Corrupción preservada hasta descarte explícito.
- Barrera durable antes de efectos externos. Propuestas, argumentos, planes y resultados parciales conservados por contexto; restaurar marca incertidumbre y exige revisión/confirmación, nunca reejecuta. Notas/apps/documentos se reconstruyen por identidad y acceso vigente; selecciones documentales no serializan texto extraído ni autorizaciones.
- IA con caché limitada por tarea/destino y protección de respuestas tardías; respuestas parciales recuperables sin solicitud automática. Cambio de proveedor/endpoint invalida consentimiento heredado y vuelve a revisar contexto. Gemini prioritario en selección visual; proveedor/modelo/credenciales existentes conservados.
- Modo privado descarta una sesión recuperable suspendida aunque el overlay esté cerrado, sin borrar conversaciones guardadas explícitamente. Confirmación de descarte de recuperación disponible en Ajustes.
- Respaldo portable de temas, preferencias admitidas, prioridades, alias, comandos y atajos con validación transaccional; revisión antes de importar y compatibilidad conservadora. No exporta credenciales, endpoint, grants SAF, consentimientos ni historial privado; permisos se revalidan.
- Alias con {args}, destino editable y conflictos; alias heredado no desbanca una aplicación exacta ni en CompactResults. Búsqueda local, sin IA por tecla.
- Integraciones verifican componente habilitado/exportado y permisos. Nueva herramienta tipada control_app_media solo para el paquete autorizado y controles soportados, sin fallback a otro reproductor; search_apps devuelve paquete real. Se conservan PhoneUse, Shizuku, QuickLook y ejecutores existentes.

## Pruebas reales y diagnóstico
**129casos únicos correctos en rondas,31clases;30pruebas Python correctas.** No se presenta una batería completa verde en una sola JVM ni se suman repeticiones como casos nuevos. Primera ronda amplia:129casos,122correctos/7fallidos. Dos expectativas antiguas suponían filtro inferior; fueron actualizadas al filtro superior aprobado con comprobaciones adicionales de clipping y48dp. Cinco bloqueos EspressoAppNotIdle aparecieron en ejecución conjunta; las3clases afectadas (17casos) pasaron con JVM separada por clase, sin aumentar timeouts ni omitir aserciones. Las28clases correctas no se relanzaron. Logs/XML y último estado por caso en tests.json y evidencias.

La investigación SAF conservó sus fallos: canarios y adaptador temporal probaron que los2314bytes sí se escribían. Esperar solo metadata File no drenaba la cola principal pausada de Robolectric. La prueba final vuelve al proveedor/suppliers original y espera confirmación real de UI; después comprueba archivo/JSON/temas/alias/grants y revisión/confirmación de importación. Un error de sintaxis y un acceso al ActivityRule desde IO en los fixtures fueron corregidos; no son bugs de producción ni controles suprimidos. El adaptador experimental fue retirado.

Cobertura nueva/regresiones: registro durable y fallo de flush bloqueante; privado con servicio cerrado; reconstrucción de Activity; argumentos y planes interrumpidos sin replay; contexto IA/destino; MediaSession por paquete y liberación; ranking/alias; backup transaccional y SAF simulado; teclado, tacto/scroll PDFfixture, ActionPanel/submenús y adaptación; seguridad documental/semántica y herramientas. No se relanzaron baterías visuales históricas sin relación.

## Lint, R8 y verificación de artefactos
Lint:0errores,13advertencias,1informativa; avisos conservados, no baseline nuevo ni controles desactivados. Gate local lint+assemble con **R8, shrinkResources y no debuggable** correcto4m45s. Verificación posterior de empaquetado13s,49tareas up-to-date/2lintVital; sin repetir unitarias ni R8. No es una compilación o firma de la app principal.

Paquete `dev.bautista.spotlight.next.raycastpreview`; versionCode40; min26/target35; ARM64. Certificado histórico `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba`; firmaV2 y zipalign16KB correctos. Ocho librerías nativas coinciden con AARs originales; no temporales ni ABIs inesperadas en APK. Los temporales detectados en intermediarios recuperados NO llegaron al paquete, por lo que no se ocultaron con exclusiones ni se eliminaron dependencias. ZIP con1358blobs Git cotejados,206inputs productivos registrados; APK/ZIP del mismo SHA. mapping R8 completo se conserva en evidencias privadas.

- APK: `Spotlight-Next-Raycast-Candidate-40-6c89735.apk` ·SHA256 `99bd8c6de9bfc0ff78a1ea2e5abb6244592b3f58bc390a6362eb35d2c55882c7`.
- Fuente: `Spotlight-Next-source-6c89735.zip` ·SHA256 `1a2753962e7ac92704fcc3cc41a3a40001b8c45d6d60a6941e14590ca9a39c0b`.

## Auditoría visual frente a01–04
Las4referencias fueron vistas.03/04mandan para material;01/02para composición. Comparación sobre las capturas actuales incluidas, no sobre mockups ni una captura de otra Preview.

|Región|Resultado observado y límite|
|---|---|
|Paleta|Borde fino/radio16dp, superficie grafito, selección gris y filas compactas. Home muestra solo una favorita disponible; no rellena tres slots. Textos de resultado1línea/14sp/metadato11sp y objetivos48dp prevalecen sobre filas de2líneas del render.|
|ActionPanel|Popup sobre resultados, filtro superior, primaria y grupos separados, keycaps Android y submenú. Ancho/altura permiten acciones habituales simultáneas en tablet y scroll interno excepcional. La captura anterior solo de raíz omitía el popup; ahora se captura pantalla+raíz del menú.|
|Teléfono|Mismo lenguaje y acciones dentro de viewport390×844, sin columna vacía; controles y formularios alcanzables. El pie puede truncar etiquetas largas para conservar acciones/atajos. IME y toque fuera físicos siguen pendientes.|
|Inspector|Formulario/media en el espacio existente; metadatos, progreso y acciones reales del fixture. Portada vacía no se sustituye por una imagen inventada.|
|Ajustes|Ocho destinos, sidebar y contenido comparten material; estados y permisos centralizados. Se conserva organización aprobada y no se vuelve a rediseñar.|
|Glass|Roles84/92/94 y fallback legible. La prueba tiene fondo gris/negro de host, no wallpaper ni blur entre ventanas. No se afirma identidad pixel a pixel ni certificación de transparencia OneUI.|
|Integraciones|No se copian acciones ficticias del render como guardar canciones en biblioteca sin API/autorización. Iconos Android genéricos pertenecen a apps fixture; en dispositivo se carga PackageManager real.|

## Límites y siguientes validaciones
No hay dispositivo ADB conectado. Sin instalación física, OneUI/Motorola, Spotify/Gemini reales, proveedor SAF de terceros, OCR/modelo semántico real, TalkBack ni medidas físicas cold/warm/RAM/batería. API33/NATIVE es Robolectric, no emulador completo ni hardware. Restaurar un registro desde disco/nuevas instancias no certifica muerte física de proceso. El muestreo periódico puede perder las últimas ediciones no guardadas; los efectos externos tienen fence durable previo. La continuidad deliberadamente cerrada o privada no es historial automático.

AppFunctions se limita a prerrequisitos compatibles, no se declara ejecutor universal. Capacidades dependientes de apps/permisos/proveedores permanecen parciales, sin permiso nuevo ni migración disruptiva. C2/C3/C4 tienen cobertura automatizada ampliada; sus límites físicos/externos siguen en la matriz. C5 permanece parcial hasta validación física. No hay trabajo anunciado en segundo plano.

# C5 · candidata integral · validación física pendiente

Esta matriz no certifica dispositivos. ADB no detectó equipos durante la continuación. Robolectric/GraphicsMode.NATIVE compone Android real en JVM con aplicaciones, permisos, MediaSession y proveedores de prueba; no equivale a One UI, Motorola, emulador completo ni servicios externos reales.

|Área|Procedimiento|Resultado requerido|Galaxy Tab A9+ / One UI|Motorola G86|
|---|---|---|---|---|
|Actualización|Instalar encima de la Preview existente, sin desinstalar|Mismo certificado/paquete; preferencias, temas y datos conservados|Pendiente|Pendiente|
|Búsqueda|Vacía; `spot`, `whats`, `webOS Dev Manager`, consulta sin coincidencias|≤3 sugerencias/≤1 app; exacta/prefijo relevantes; sin resultados falsos ni IA por tecla|Pendiente|Pendiente|
|Teclado|↑↓, izquierda/derecha, Tab/Shift+Tab, Enter, Ctrl+K/M, Escape|Editor conserva foco/contexto; selección estable; Enter ejecuta solo seleccionado|Pendiente|Pendiente|
|ActionPanel|Abrir/cerrar, filtrar, submenú, scroll, tocar fuera, IME|Panel sobrepuesto sin mover resultados ni interceptar superficies ajenas|Pendiente|Pendiente|
|PDF|Seleccionar, deslizar lento/rápido/cancelar, largo, recorrer PDF largo|No abre externo al scroll; Quick Look local; inercia y acciones correctas|Pendiente|Pendiente|
|Adaptación|Rotar con teclado abierto, ancho compacto/amplio, fuente130–150%|Sin clipping ni controles inaccesibles; consultas y borradores conservados|Pendiente|Pendiente|
|Recuperación|Escribir argumentos/plan; retirar proceso de laboratorio y volver a invocar|Recupera último snapshot disponible; tareas inciertas sin replay; nueva confirmación sensible|Pendiente|Pendiente|
|Privacidad|Activar privado con overlay cerrado, reabrir; revisar borrado explícito|No reaparece sesión efímera; conversaciones guardadas no se borran por accidente|Pendiente|Pendiente|
|Recursos|Revocar carpeta/contactos/notificaciones mientras inspector está abierto y volver|Revalidación auténtica; error recuperable; sin acceso/grant inventado|Pendiente|Pendiente|
|MediaSession|Spotify autorizado y otro reproductor simultáneo; cambiar sesión/app|Metadatos/portada/progreso/controles de la app seleccionada; sin fallback al otro player|Pendiente|Pendiente|
|Gemini|Clave propia autorizada, streaming/cancelar/seguimiento, cambiar destino|Revisión de contexto; no envía al escribir/restaurar; errores/cuota auténticos|Pendiente|Pendiente|
|Phone Use|Activar servicio opcional; revisar y cancelar pasos autorizados|Respeta alcance/confirmaciones; informa solicitado frente a efecto verificado|Pendiente|Pendiente|
|Apps nativas|WhatsApp normal, Gmail, Chrome y acciones de documentos|Abre borrador/destino correcto, no afirma envío/completitud externa no observada|Pendiente|Pendiente|
|Respaldo SAF|Exportar a proveedor local, modificar ajustes, importar/revisar/confirmar; cancelar y archivo inválido|Roundtrip transaccional de temas/alias/comandos/atajos; sin claves, grants ni historial en archivo; fallo preserva configuración|Pendiente|Pendiente|
|Glass/accesibilidad|Fondo claro/oscuro, blur deshabilitado, contraste/lectura/TalkBack|Roles84/92/94 ajustables, gris de selección, fallback legible y objetivos48dp|Pendiente|Pendiente|
|Rendimiento|Arranque frío/caliente, consultas e índice autorizado, desplazamiento prolongado|Medir con trazas Android; registrar p50/p95, memoria/jank y batería reales; sin porcentajes inventados|Pendiente|Pendiente|

No se recomienda desinstalar la aplicación para resolver una firma. Una operación externa interrumpida permanece incierta hasta verificarla; restaurar nunca es autorización para repetirla. La recuperación periódica puede perder las últimas ediciones no guardadas ante terminación abrupta; los efectos externos tienen una barrera durable previa específica.

AppFunctions permanece condicionado a APIs, permisos y apps compatibles; el reporte de prerrequisitos existente no constituye ejecución universal. El índice/modelo semántico opcional, SAF de terceros, OCR y formatos complejos necesitan muestras autorizadas. No se descargaron modelos ni se concedieron permisos en los dispositivos del usuario.


# Matriz de40decisiones · candidata integral40

Código APK/ZIP 6c897354c0c7e5d9668966ed2d582aa268d519ac. Estado de implementación, automatización y físico se distinguen. No se declara C5 cerrado.

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
|D15|Contextual inteligente|parcial|C2 integral: descriptores, página/expansión, notas y selección documental recuperables por identidad. Permisos revalidados y renderers/callbacks dispuestos; InspectorResourceRecoveryTest/InspectorDescriptorPersistenceTest dentro de129casos en rondas. PDF instrumentado/proveedor físico pendiente.|
|D16|Reproductor en inspector|parcial|Inspector MediaSession conserva metadatos/portada/progreso/controles soportados del paquete exacto. MediaInspectorFlowTest y TargetMediaToolTest correctos en C5; nueva herramienta tipada control_app_media sin fallback a otro player. Spotify/OneUI físicos pendientes.|
|D17|IA en inspector contextual|parcial|C2 integral: propuestas, argumentos y planes guardables con fence durable antes de efectos; streams parciales aislados por generación/tarea/destino y restauración sin solicitud remota. DurableTaskRegistryTest/ToolTaskRecoveryTest/AiActivityRecoveryTest/AiRecoveryPrivacyTest correctos en rondas. Proveedor/hardware real pendiente.|
|D18|Persistencia contextual + fijar|parcial|OverlayService sobrescribe LocalSaveableStateRegistry con snapshot durable en ContextStore/AtomicFile existente, codec MutableState, LRU8 y corrupción preservada. Durabilidad comprobada con disco+nuevas instancias/registro y fallo de flush que bloquea ejecución; no equivale a muerte física de proceso ni garantiza cada última tecla. Recreación/rotación/OneUI pendientes de matriz física.|
|D19|Esencial Raycast|implementada y probada|C1.1 GlassQuickMenu cuatro rutas reales Ajustes/Apariencia/Permisos/Atajos separadas deCtrlK; RaycastQuickMenuComposeTest yMasterC1TouchFlowTest.|
|D20|Centro inteligente|parcial|Preview38: ruta antigua de permisos redirige al centro único MainActivity. Prueba de navegación sin toggles de concesión y capturas de páginas sin solicitudes. APIs y señales reales de código existentes; certificación de permisos/revocación en dispositivos pendiente.|
|D21|En el inspector lateral|parcial|Formularios y borradores por contexto conservan argumentos editados; permisos/destinos/confirmación se revalidan al ejecutar. Recuperación en inspector unificado y no replay cubiertos en129casos dirigidos. Mensajería y hardware reales pendientes.|
|D22|Confirmación inteligente|parcial|ai/ToolCapability.kt, ToolExecutor.kt, AiRequestPolicy.kt, agent/PhoneUsePolicy.kt; ToolProtocolTest/PlanExecutionTest y políticas. No certificación appsdestino.|
|D23|Seleccionar y confirmar|implementada y probada|C1.1 SpotlightPanel primer toque deliberado/segundoejecuta/Enterdirecto/longacciones; resetea confirmación al navegar/scroll/query. MasterC1TouchFlowTest5/5 yResultTouchScrollTest7/7 (PDFfixture). Validación física pendienteC5.|
|D24|Cierre contextual inteligente|parcial|Cierre contextual: Copycierra al terminar sin tarea/inspector; fijación y documentos conservan superficie; Launch/Shortcut/InApp respetanretención, Devicecierra solo completed/verified. Caso localCopy/pin probado; rutasexternas físicas pendientes.|
|D25|Búsqueda híbrida inteligente|parcial|Exacta/prefijo/alias/fuzzy locales; CompactResults ya no deja que alias heredado desplace app exacta. AliasTemplateRankingTest/SmartSearchAcceptanceTest y captura spot→Spotify correctos. Sin IA por tecla; latencia física pendiente.|
|D26|Profunda por capacidades|parcial|IntegrationRegistry comprueba componente habilitado/exportado y permiso real. MediaSession/control_app_media tipado por paquete probado. Intents/shortcuts/PhoneUse/Shizuku existentes; AppFunctions solo reporte condicionado de prerrequisitos, no ejecución universal. Integraciones externas físicas pendientes.|
|D27|Encadenados editables|parcial|Plan/propuesta/argumentos/resultados parciales guardables; checkpoint durable antes de efectos; estado interrumpido exige revisión y nunca replay. PlanExecutionTest/ToolTaskRecoveryTest/DurableTaskRegistryTest correctos en rondas; confirmación/servicios externos físicos pendientes.|
|D28|Comandos personalizados inteligentes|parcial|Alias{args} literales/missingargs sin ejecutar, destino editable y conflictos de app/comando; exacta instalada prevalece también en CompactResults. AliasTemplateRankingTest correcto en C5; planes complejos y destinos reales conservan límites de su capacidad.|
|D29|Múltiple configurable|parcial|access/ShortcutService.kt, InvokeActivity.kt yTileService existentes; métodos reales OS; sin validación hotkeyglobalOneUI.|
|D30|Atajos Raycast configurables|parcial|Editor KeyboardShortcutEditor para4atajos locales; detección de conflictos con comandos/edición/acceso global, persistencia DataStore y backup validado. Handlers/ActionPanel/pistas usan mismo binding. KeyboardBindings6+KeyboardShortcutEditor2+ActionPanelKeyboardContract6 correctos; global sigue3combinaciones soportadas/servicio opcional, OneUI físico pendiente.|
|D31|Índice local inteligente|parcial|index/FileIndex.kt/DocumentText.kt; índicelocal incremental/OCRoptin/cotas/semánticalocal existentes, evidencia histórica docs; faltan validaciones físicas y todos los formatos.|
|D32|Integración contextual|parcial|ShareContextActivity.kt/ContextStore.kt yAndroidManifest.xml: compartir explícito/grants; falta certificar ACTION_SEND_MULTIPLE y revocación en appsreales.|
|D33|Contextuales inteligentes|parcial|Snapshot del registro y cache limitada de sesiones por tarea/destino; streaming parcial restaurado sin reenviar, cambios de proveedor reabren revisión. Guardado/borrado de conversaciones explícitas existente. JVM/disco/recreación probados; muerte abrupta física pendiente.|
|D34|Motor modular|parcial|Gemini primero en selección visual, conserva proveedor/modelo/credenciales configurados. Adaptadores existentes Gemini/OpenAI/backend; cambio de proveedor/endpoint invalida consentimiento heredado y backup no lo autoriza. AiDestinationConsentTest/ToolProtocolTest correctos; claves/cuotas/servicio real no probados.|
|D35|Compactos + inspector|parcial|next/NativeProviders.kt/search/SearchEngine.kt: calculadora yconsultasexternas existentes; fuentes/tiempo/offline e inspector completos por certificar, fixtures≠servicioreal.|
|D36|Microestados contextuales|parcial|SpotlightPanel.kt estadoserror/carga, ContextPanel.kt recuperación y cancelación existentes; faltan todos los microestados unificados.|
|D37|Rendimiento adaptativo inteligente|parcial|FileIndex.kt/DocumentText.kt límites e incremental; cancelación SearchEngine yLRU existentes. Cold/warm/RAM/batería/OneUI NO medidos.|
|D38|Inteligente y configurable|parcial|Privado descarta sesión recuperable incluso sin OverlayService; no elimina conversaciones explícitas. Corruptos preservados hasta descarte, destinos IA revalidados. PrivateRecoveryPolicyTest/DurableOverlayRecoveryTest/AiDestinationConsentTest correctos; privacidad y permisos físicos pendientes.|
|D39|Actualización segura + respaldo local|parcial|Export/import SAF por callbacks reales de MainActivity, revisión y confirmación; temas/alias/comandos/atajos y ranking portables, validación transaccional y compatibilidad parcial antigua. PortableSafFlowTest y suites de import/themes/prioridad correctos en rondas; proveedor simulado no certifica SAF ni actualización físicos.|
|D40|Calidad por checkpoints|parcial|C5:129casos únicos aprobados en rondas +30Python;7fallos iniciales conservados,17casos afectados correctos con JVM aislada. Capturas API33/NATIVE actuales incluyen popup real. Lint0errores/13avisos/1info, R8+recursos reducidos+assemble40 correctos, firma885838aa/V2/zipalign16KB y fuente1358blobs/206inputs verificados. Candidata a validación física; sin hardware/servicios acreditados.|

129casos únicos aprobados en rondas y30Python; no una batería íntegra verde en una única JVM. Los7fallos iniciales están conservados. Matriz física específica incluida en el informe.
