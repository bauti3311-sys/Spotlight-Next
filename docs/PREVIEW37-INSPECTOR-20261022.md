# Spotlight Next · Preview37 · 22/10/2026

## Rama, código y coordinación
Rama `feat/raycast-sidebar-permissions-20261020`; PR18 sigue abierto como borrador sobre PR17. Base de esta sesión: `eb0a85ab`. Código exacto de APK y ZIP: `bcca55163a46e407d3899867f9c05507d479c5cb`. Propietario conservado: `work-master-recovery-20261022-cc97a1bd`; actualizaciones por hijo/CAS/force=false, sin reclamar reservas ajenas. Los commits documentales posteriores conservan los201 inputs productivos idénticos. No merge, force-push, Actions ni gastos habilitados.

## Implementación y decisiones
- D16: MediaSnapshot/MediaControl.inspect y MediaInspector muestran título/artista/álbum/portada bitmap/duración/posición suministrados por la MediaSession autorizada del paquete exacto. Callbacks inmediatos y tick de1s solo durante la composición; se liberan al salir. No sesión única o acceso perdido: no datos/controles inventados. Progreso extrapolado desde timestamp/velocidad de Android, limitado a duración; desconocido se declara. Seek requiere soporte y misma sesión viva; controles vuelven a consultar sesiones. No hay streaming propio ni descarga de portadas URI remotas.
- D18/D21/D27: hasta8 borradores de formulario/plan por contexto del overlay. SearchEditor y texto/destinatario del mensaje conservan entradas; no se restaura una confirmación sensible ni un contacto sin comprobarlo. Pasos editados, bindings, selección y estado del plan se conservan; requisitos se resuelven de nuevo. Ninguna ejecución automática al restaurar. El host movible conserva estado al cambiar ancho.
- D17/D33: hasta4 conversaciones completas por tarea/proveedor/modelo/destino, solo RAM del overlay y con límite64k caracteres por entrada. Volver a una pregunta ya completada recupera su conversación sin nueva petición; tareas distintas no mezclan historial. Seguimiento sigue explícito. Guardado duradero mediante ContextStore existente; no se inventa reanudación de streaming ni recuperación completa tras muerte de proceso.
- D30: editor real para Acciones, Expandir, Autocompletar explícitamente y Portapapeles. Captura y persiste combinaciones locales, detecta duplicados/acceso global/reservas de edición y Android. Cambian también los handlers y pistas de la UI/ActionPanel. ConfigTransfer conserva backups antiguos y rechaza overrides conflictivos. Flechas/Enter/Tab/Escape siguen fijos. Invocación global conserva sus tres combinaciones soportadas, servicio optativo y límites Android; no intercepta nuevas teclas fuera de Spotlight.
- D23: conserva selección/tacto/Quick Look; regresiones afectadas ejecutadas. La revisión nativa detectó controles multimedia fuera del viewport de teléfono; se compactó la identidad y se acotó la lista en esa vista. No rediseño de Dark Glass ni cambios de permisos/proveedores.

Las decisiones siguen parciales donde requieren hardware, servicios o el resto del maestro; este hito no cierra las40.

## Verificaciones reales
48 casos únicos correctos, deduplicados por clase/método en `tests.json`; no se suman ejecuciones solapadas ni las59 históricas de Preview36. MediaTarget9 aprobados en la primera ronda se reutilizan porque su código no cambió después. Ronda final dirigida39casos:38correctos/1fixture de sincronización; reintento de ese único caso1/1correcto. Se esperaba el registro de callback antes de la primera emisión: se corrigió el fixture esperando esa señal, sin retrasos artificiales. Las rondas anteriores conservan un fallo de geometría real en teléfono, un selector de texto de formulario y la etiqueta de tecla numérica, todos con evidencia y corrección.

Suites: MediaInspector6, MediaTarget9, MediaInspectorFlow3, ContextualDraftRestore2, KeyboardBindings6, KeyboardShortcutEditor2, UnifiedInspectorFlow5, ActionPanelKeyboardContract6, QuickLookTouchTarget2, ResultTouchScroll7.

Comando final: `:app:testRaycastPreviewUnitTest --tests '*MediaInspectorTest.observingRegistersOnlyTheTargetAndDisposalUnregistersCallbacks' :app:lintRaycastPreview :app:assembleRaycastPreview`, Gradle8.10.2/JDK17/SDK35 local,4m28s. Lint:0errores/13advertencias/1informativa. Sin suite integral nueva ni R8. XML/logs bajo `docs/verification/media-inspector-20261022`.

Se recuperaron JDK/Gradle/SDK tras pérdida de /tmp, verificando hashes de los paquetes oficiales y conservando versiones. El aviso inicial del daemon Kotlin se recuperó dentro de la misma ejecución; no se reinició esa ronda. El empaquetado advirtió de un temporal armeabi-v7a: APK final contiene solo ocho .so ARM64 normales, todas coinciden con los AAR originales, sin entradas temporales. ZIP fuente1128blobs cotejados con Git;201inputs registrados; sin keystores.

## Identidad y archivos
Paquete `dev.bautista.spotlight.next.raycastpreview`, versionCode37, min26/target35/ARM64. FirmaV2/zipalign16KB verificados. Certificado histórico `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba` idéntico a Preview36. Variante derivada de debug, no Release/R8 ni actualización del canal principal.

- `Spotlight-Next-Raycast-Preview-37-bcca551.apk` · 86605220bytes · SHA256 `42ae5e183b49866c28e7b24ab11ebc8efa3b2cd2b9959e619b59b7c9c371a229`.
- `Spotlight-Next-source-bcca551.zip` · 8285351bytes · SHA256 `93897b44c548226af41ade65fe6e5b9e615a671f49d4dc472afc8133dcce57dc`.

## Capturas y límites
Galería original API33/GraphicsMode.NATIVE: tablet1280×800 y teléfono390×844. Se inspeccionaron PNG completos y se corrigió el clipping de controles. Material Raycast Dark Glass/selección gris/Inter/contorno conservados; fondo de fixture, no blur hardware. Íconos y canciones del fixture no representan una cuenta o reproducción real de Spotify. IA usa MockWebServer; no Gemini real. No hay dispositivo conectado, instalación OneUI/Motorola, rotación/IME/InputDispatcher físicos ni medición de rendimiento acreditados.

La restauración de estado de campos y planes se verificó con StateRestorationTester; el cambio800→360 y vuelta se ejecutó en Compose. No equivale a certificación de recreación completa de todos los recursos Android/Phone Use/planes de herramientas ni a reanudar acciones externas interrumpidas. No se repiten al restaurar.

## Continuación
Validar Preview37 físicamente: sesión Spotify autorizada/pausa/progreso/revocación, actualización sin desinstalar, teclado personalizado y rotación/IME. Después ampliar persistencia de propuestas/herramientas y tareas pendientes dentro del inspector existente y auditoría C3. Preservar esta evidencia, la reserva y los certificados; no repetir48sin cambios relevantes.
