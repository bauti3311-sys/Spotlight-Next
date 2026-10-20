# Spotlight Next — Raycast Inspector + Ajustes/Permisos (2026-10-20)

## Alcance
UI **nativa en Kotlin/Jetpack Compose**. Contrato: diseño Obsidian Glass de referencia, sin WebView/React, sin pantallas rígidas para la IA, sin nuevas autorizaciones automáticas.

### Inspector contextual en el launcher
- `SpotlightPanel` revela el inspector a la derecha **solo** en un overlay adaptable con al menos 650 dp y una app real seleccionada; al cambiar la selección se actualizan los detalles.
- En pantallas menores conserva la lista y usa la hoja contextual existente. No deja columna derecha vacía.
- Mostrar icono instalado, título, subtítulo, identificador de paquete y versión local disponible; sin inventar duración, tamaño ni reproducción.
- Enter abre la app. «Ver acciones contextuales» abre el ActionPanel existente. PDF y Quick Look conservan su comportamiento anterior.
- `ContextSplitPanel`: proporciones 62/38 ajustables, borde/divisor y material discreto.

### Menú de opciones estilo Raycast
- `GlassQuickMenu` organizado en Búsqueda, tema/privacidad y rutas a General, Apariencia, Integraciones, Inteligencia artificial, Atajos, Permisos y accesos.
- Mantiene un solo contenedor overlay para evitar problemas de popups con WindowManager y teclado.
- Se inspira en las ideas de `List`, `Detail` y `ActionPanel` de https://developers.raycast.com/api-reference/user-interface/list y https://developers.raycast.com/api-reference/user-interface/action-panel . No se importan sus componentes React.

### Una sola entrada para los permisos
- `SettingsGroups` añade **Permisos y accesos** y **Atajos**. Traduce deep links heredados de «Permisos y privacidad» a la ruta nueva.
- `SettingsAccessSummary` lee estados reales: overlay, contactos, listener de notificaciones, carpetas, lectura de calendario, ubicación, atajo de teclado, servicio Phone Use, Shizuku, acceso ampliado a archivos, uso de apps y (Android 13+) notificaciones propias.
- **No usa Switch para permisos especiales**. «Configurar/Gestionar» abre la pantalla oficial de Android o el diálogo runtime; permisos concedidos en UI son reportes, nunca cambios simulados.
- `MainActivity` centraliza esas rutas y reemplaza enlaces redundantes en WhatsApp, archivos y notificaciones por accesos al centro. La opción del inicio navega al mismo centro.
- El acceso a una carpeta elegida sigue disponible desde la búsqueda porque forma parte de la configuración del índice local; el estado del permiso se muestra en el centro.

### Controles de música reales y aislados
- `MediaControl.availableForPackage` / `executeForPackage` filtran sesiones autorizadas por **paquete exacto** y requieren una sola sesión compatible.
- El inspector obtiene compatibilidad fuera del hilo principal mediante `produceState` y solo muestra comandos soportados por la MediaSession: reproducir, pausar, siguiente y anterior.
- Un toque solicita el comando y registra una auditoría, pero **no certifica su ejecución remota**.
- Jamás controlar otra app porque sea la única sesión activa.

## Pruebas / límites conocidos
- Nueva prueba `RaycastSettingsNavigationTest` para rutas, permisos y compatibilidad de deep links (escrita, **aún no ejecutada con Gradle**).
- 18/18 revisiones estructurales de archivos remotos correctas a este punto (no son tests de Android).
- Compilación sintáctica local independiente de una expresión Kotlin de spread/if: OK; **no representa compilación del proyecto**.
- **Pendiente:** `:app:compileDebugKotlin`, pruebas JUnit/Compose, lint dirigido, emulador/API 36 y tablet Samsung real. No se entrega APK ni se asegura fidelidad definitiva hasta verificarla físicamente.
- La rama `feat/work-integral-settings-20261016` y `main` permanecen sin cambios; la reserva `locks/spotlight-autonomy` sigue ajena. No disparar CI completo ni gastar minutos de GitHub Actions innecesariamente.

## Próximo ciclo
1. Ejecutar compilación Android/JUnit/lint en entorno que tenga SDK; corregir cualquier error.
2. Validar anchura, accesibilidad y scroll en Galaxy Tab A9+ y Moto G86 con teclado y tacto.
3. Entregar APK de revisión independiente y capturas comparativas; no tocar el canal release ni los secretos/keystore.

## Consolidación / control de calidad (20/10)
- **Corregido el ancho por defecto:** `Config.width=640dp` y `SpotlightPanel` resta 32dp de márgenes, por lo que `maxWidth>=650dp` impedía ver el inspector aunque la tablet fuera grande. Nuevo umbral `>=600dp`, de modo que el panel lateral es alcanzable con la configuración existente sin cambiar tus preferencias.
- Compilado y ejecutado `AdaptiveLayout.kt` junto a `OverlayWidthPolicy.kt` y `WindowSizePolicy.kt` de la copia fuente aportada, con un stub únicamente de Config, mediante `kotlinc`: **3/3 escenarios** (tablet vertical/horizontal con ventana de 640dp; teléfono vertical sin lateral) correctos. La geometría aún requiere verificación física.
- Se modificó el toque táctil de una aplicación: selección e inspector contextual en tablet; menú de acciones en teléfono. El teclado conserva Enter para abrir directamente. Así se evita abrir apps accidentalmente al explorar.
- Se centralizó también **revocar carpetas**, el acceso del teclado por Accesibilidad y la **configuración de Shizuku** en Permisos y accesos. Los paneles de búsqueda e invocación mantienen sus ajustes funcionales, pero redirigen a la sección única para conceder o revocar permisos.
- `MediaTargetTest` amplía casos para comprobar que un control multimedia no actúe sobre otra app, ni sobre varias sesiones ambiguas; **tests escritos, no ejecutados con Gradle**.
- **20/20 comprobaciones de invariantes estructurales** sobre los archivos remotos, sin errores detectados en esos invariantes. No son una suite JUnit.
- Entorno de trabajo revisado: JDK y compilador Kotlin disponibles; **Android SDK/Gradle cache ausentes** y red externa de la terminal no disponible, por lo que no puede compilarse una APK nueva en esta ejecución. `scripts/validate-raycast-sidebar.sh` permite compilar/probar sin disparar Actions cuando haya SDK.
- No se lanzó CI, no se gastaron minutos de Actions, y no se cambió `main`, la reserva de Work ni claves de firma.
