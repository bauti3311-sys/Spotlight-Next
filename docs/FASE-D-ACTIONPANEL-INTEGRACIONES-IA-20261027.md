# Fase D — ActionPanel Contextual, Integraciones Profundas e IA Universal (2026-10-27)

## Resumen de implementación

Esta entrega completa los tres pilares de la **Fase D** sobre la base aprobada de Preview 56 (`21a60bb938e78198be2b122725b013a24c66dab1`), preservando el diseño visual **Solid Graphite / Dark Glass / macOS Light**, el escalado `GlassTokens` (85 %–115 % en `textScale` y `uiDensityScale`), los objetivos táctiles mínimos de `48.dp` y la política de **cero llamadas remotas por pulsación**.

---

## 1. Pilar 1 — ActionPanel Contextual (`ActionPanelView.kt`, `SpotlightPanel.kt`)

- **Escalado proporcional con `GlassTokens`**:
  - `ActionPanelView` consume `LocalAppearance.current` y aplica `GlassTokens.densityScale(appearance)` a los márgenes internos (`12.dp * d`, `10.dp * d`, `8.dp * d`, `6.dp * d`, `4.dp * d`) y `GlassTokens.scaledSp(..., appearance)` a toda la jerarquía tipográfica (buscador de acciones, encabezados de grupo, títulos, subtítulos de estado y mensaje vacío).
  - Todas las filas interactivas y botones del encabezado conservan `Modifier.heightIn(min = 48.dp)` y `Modifier.size(48.dp)` incluso en densidad compacta (`0.85f`).
- **Submenú «Copiar…» completo para documentos (`file.copy-uri`)**:
  - En `SpotlightPanel.kt` (`details(true)`), cuando un resultado de la categoría `"Archivos"` posee URI (`Action.Launch.intent.data`), se expone la acción `"file.copy-uri"` (`"Copiar URI"`), integrándose automáticamente en el submenú agrupado `copy.formats` (`"Copiar…"` -> `"Copiar texto"`, `"Copiar nombre"`, `"Copiar URI"`).
- **Capacidades verificadas de apps en el ActionPanel (`Ctrl+K`)**:
  - Cuando el resultado seleccionado es una aplicación, `SpotlightPanel.kt` incorpora las capacidades públicas verificadas por `AppCapabilities(context).available(appPackage)` que no estén ya presentes en `ActionCatalog.applicationActions(appPackage)` ni dupliquen `"app.info"`.
- **Acciones de IA contextual sobre texto, notas y portapapeles**:
  - Se amplían las acciones contextuales sobre texto a **Explicar con IA**, **Resumir con IA**, **Corregir con IA** y **Puntos clave con IA**, todas preparadas mediante `AiRequestPolicy.prepare(Action.Ask(..., sensitive = true, origin = AiInputOrigin.PRIVATE_CONTEXT))` con riesgo `ToolRisk.PREPARE` y estado `ConfigurationRequired` si no hay proveedor configurado.

---

## 2. Pilar 2 — Integraciones Profundas (`AppCapabilities.kt`, `ActionCatalog.kt`, `AppCapabilityActions.kt`, `SearchEngine.kt`)

- **Alineación entre `AppCapabilities` e `IntegrationRegistry`**:
  - `AppCapabilities.searches` incorpora los enlaces documentados de **Google Maps** (`com.google.android.apps.maps` -> `https://www.google.com/maps/search/?api=1&query=consulta`) y **Sofascore** (`com.sofascore.results` -> `https://www.sofascore.com/`).
  - Con ello, `IntegrationRegistry.capabilities(packageName)` y `AppCapabilities.available(packageName)` coinciden de extremo a extremo al verificar actividades exportadas y sin permisos restringidos.
- **Ruteo natural ampliado en `ActionCatalog` y `SearchEngine`**:
  - `ActionCatalog.canonical` y `SearchEngine.searchSources` admiten consultas naturales para **Chrome** y **Vivaldi** (`"buscar X en vivaldi"`, `"buscame X en chrome"`, `"busca en mapas: X"`, `"vivaldi X"`), preservando el paquete del navegador elegido (`Action.InApp`) en lugar de delegar al navegador predeterminado.
- **Inspector de capacidades (`AppCapabilityActions.kt`) con `GlassTokens`**:
  - `AppCapabilityActions` aplica `GlassTokens.densityScale` y `GlassTokens.scaledSp` manteniendo intactos los `testTag` (`app-capabilities`, `app-action-*`, `app-capability-*`) y la altura mínima de `48.dp`.

---

## 3. Pilar 3 — IA Universal (`UniversalIntentEngine.kt`, `QueryInterpretation.kt`, `RootActions.kt`, `InterpretationPanel.kt`, `SearchEngine.kt`)

- **Consultas compuestas Documento + IA ampliadas**:
  - `UniversalIntentEngine.parseDocumentAiCompound` y el guard de `QueryInterpreter.files` reconocen verbos adicionales de segundo paso: `corregir` (`corregí`, `corregime`), `extraer` (`extraé`, `extraeme`), `comparar` (`compará`, `comparame`) y `sintetizar` (`sintetizá`, `sintetizame`), además de `explicar`, `resumir`, `analizar`, `traducir`, `contame`, `decime`, `responder` y `buscar en el documento`.
- **IA con contexto autorizado (`PrimaryIntentKind.CONTEXT_AI`)**:
  - `UniversalIntentEngine.resolve`, `RootActions.results` y `SearchEngine.interpret` reconocen expresiones como `"corregí este texto"`, `"corregí esto"`, `"analizá este documento"`, `"resumí este PDF"`, `"este portapapeles"`, exigiendo siempre `RemoteAiEscalation.EXPLICIT_WITH_CONTEXT_CONFIRMATION` y `RequiredPermission.AI_CONTEXT_CONSENT`.
  - `InterpretationPanel.kt` ofrece guía local sin lectura automática de archivos para `"resumí este PDF"` / `"analizá este PDF"`, y formulario local sin lectura automática del portapapeles para `"corregí este texto"` (preparando `Action.Ask` con `AiInputOrigin.PRIVATE_CONTEXT`).

---

## 4. Pruebas añadidas

- `AppCapabilitiesTest.mapsAndSofascoreDocumentedSearchCapabilitiesMatchIntegrationRegistry`
- `ActionCatalogTest.publicSearchUsesEncodedQueryAndPreferredApp` (casos `vivaldi` y `chrome` en lenguaje natural)
- `UniversalIntelligenceArchitectureTest.phase7_expandedCompoundAndContextualAiPreserveExplicitConsentAndPrivateOrigin`
- `ActionPanelKeyboardContractTest.densityAndTextScalePreserveMinimum48dpTouchTargetsInActionPanel`
- `HybridIntelligenceTest.correctionAndPdfSummaryRequireExplicitReviewWithoutAutomaticRead`
- `ReplyConfirmationTest.naturalReplyQueryExtractsDraftAndMatchesCompatibleRemoteInputTarget`
- `ContextExtractionFlowTest.contextQuickActionsPrepareBoundedSummaryAndKeyPointsWithPrivateOrigin`
- `HybridIntelligenceTest.messageEditorSwitchesToCompatibleRemoteInputAndRequiresExplicitConfirmation`

---

## 5. Segunda iteración (recorridos completos WhatsApp, Documentos y ActionPanel)

1. **Prioridad 1 — WhatsApp (`ReplyService.kt`, `IntentRouter.kt`, `UniversalIntentEngine.kt`, `SearchEngine.kt`, `CommandForms.kt`, `NotificationPreview.kt`)**:
   - `Replies.parseReplyQuery` extrae destinatario y borrador en consultas naturales (`"responderle a Juan diciendo: Ya salgo para allá"`, `"contestar por whatsapp a Ana"`) preservando mayúsculas y signos del borrador.
   - `Replies.results` admite búsqueda unificada entre `com.whatsapp` y `com.whatsapp.w4b` cuando `packageName` es vacío, precargando `draftText` en `Action.ComposeMessage` con `replyKey` sin enviar automáticamente; si no hay notificación activa del contacto, `SearchEngine` ofrece un fallback directo a `Action.ComposeMessage(c.messageApp, term, draftBody)`.
   - `Replies.findCompatibleReply` e integración en `MessageEditor`: cuando se redacta un mensaje normal a un contacto que tiene una notificación activa con `RemoteInput`, `MessageEditor` ofrece cambiar explícitamente a `RemoteInput` (`"Responder notificación activa · RemoteInput (...)"`) y muestra el mecanismo Android concreto (`RemoteInput` vs. `wa.me` / `ACTION_SENDTO` / `ACTION_SEND`) tanto en el formulario como en la tarjeta de confirmación.
   - `NotificationPreview` protege `"Resumir con IA"` y `"Sugerir respuesta con IA"` mediante `AiRequestPolicy.prepare(..., origin = AiInputOrigin.PRIVATE_CONTEXT)`, vinculando `messageTarget = target.title`.
2. **Prioridad 2 — Documentos (`SpotlightPanel.kt`, `ContextPanel.kt`)**:
   - El inspector Quick Look (`details(false)`) incorpora acciones directas para `"Extraer texto local"` (con cancelación cooperativa y caché en `FileIndex`), `"Usar como contexto IA"` (`app.contexts.setDocument`) y, cuando el documento tiene texto extraído o vista previa, `"Resumir con IA"` y `"Explicar con IA"` mediante `AiRequestPolicy.prepare(..., origin = AiInputOrigin.PRIVATE_CONTEXT)`.
   - El `ActionPanel` (`Ctrl+K`) suma `"Usar como contexto IA"` (`file.context`) para archivos con URI Android, y `ContextPanel` añade accesos directos `"Resumir documento"` y `"Puntos clave"` pasando por el mismo flujo de extracción local y revisión previa al envío.
3. **Prioridad 3 — IA y ActionPanel (`SpotlightPanel.kt`, `LocalPlan.kt`)**:
   - Se elimina la duplicación de `"Información de la app"` en el `ActionPanel` filtrando `ACTION_APPLICATION_DETAILS_SETTINGS` de `ActionCatalog.applicationActions` (conservando la entrada canónica `app.info` en `ContextActionGroup.System`) y asignando iconos/grupos específicos para `Action.Navigate` (`ContextActionGroup.Open`, `CommandIcons.Folder`) y `Action.ComposeSearch` (`CommandIcons.Search`).
   - `LocalPlan.step` reconoce variaciones naturales con `"un"` en pasos relativos (`"bajar el volumen un 20% y abrir Spotify"`).
