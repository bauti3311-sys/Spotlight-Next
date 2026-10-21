# ActionPanel superpuesto y búsqueda inteligente — 21/10/2026

Base remota comprobada 83a9d12f815bd1236a913c515308ec775d2d2ce3, rama feat/raycast-sidebar-permissions-20261020, PR18 borrador. Nueva instrucción expresa del propietario sustituye la opción B anterior: no aumentar visualmente la paleta para mostrar Acciones. WhatsApp Business fuera de alcance. Mismos motores, firmas y datos.

Reserva propia recuperada mediante avance normal con SHA esperado: eff7d417b75c1c04b26d7966f3d90ee8791c243b, work-recovery-20261018-cd7b8fe1285c. Sin reclamar otra sesión ni force. Fetch Git privado no tiene credenciales de terminal; se recuperaron los blobs y árbol exactos por API autenticada, conservando HEAD y ancestros remotos. Snapshot fuente íntegro igual al árbol remoto; no había checkout local posterior disponible.

## Causas comprobadas

- ContextActionSurface limita el menú al alto de la paleta compacta (~174px), dejando una sola acción visible y tapando la búsqueda. Ampliar la superficie pintada no es la solución autorizada.
- CompactResults vuelve a ordenar los resultados con sus propios tiers: una acción contextual puede superar a una aplicación parcial; una app fuzzy no resulta elegible.
- Alias se expanden en searchSources, pero ranking/presentación usan el texto anterior.
- CommandCenter.complete mezcla resultados con ejemplos estáticos y elige el primer prefijo incluso con ambigüedad; puede ofrecer un texto sin resultado ejecutable.
- HomeSuggestions ya limita apps a una y exige señales locales. Conservar; endurecer contenido de notificaciones, permiso documental y descripción multimedia sin afirmar reproducción.

## Solución en evaluación

Conservar ActionPanelView, filtro, pila de submenús y selección por ID. Comparar el host Compose actual con un PopupWindow hijo unido al token de la ventana anfitriona mediante API pública. El hijo acotado evita un host transparente grande que intercepte toques y no crea otra ventana overlay independiente. Verificar foco/IME, teclado, token, clipping, rotación, toque fuera y cierre del padre. No emplear reflexión a regiones táctiles privadas. No afirmar compatibilidad One UI a partir de Robolectric.

El bloque de búsqueda conserva aprendizaje local existente, límites y ranking por clase léxica; el autocompletado requiere un candidato vigente y no ambiguo. Sin consultas IA remotas por tecla ni catálogos nuevos.

## Estado de este checkpoint

Ocho pruebas de aceptación focalizadas guardadas antes de cambiar producción. Aún NO ejecutadas: los primeros intentos se detuvieron resolviendo el plugin, no compilaron ni ejecutaron tests. Gradle heredaba GRADLE_OPTS con browser-proxy:8889 inaccesible; el mismo POM Roborazzi1.29.0 responde200 por el proxy vigente. Configuración corregida solo fuera de Git, versiones conservadas. Reintento baseline con caché en recuperación, sin gate nuevo acreditado.

Gradle8.10.2 y SDK/build-tools35 restaurados y checksums oficiales comprobados; Java17.0.20 disponible. Firma Raycast privada recuperada mediante respaldo autorizado, todavía no usada para una nueva APK. Ninguna APK nueva, Actions, merge/main ni cambio de firma. Las123verificaciones únicas y entrega Preview32 anteriores se conservan y no se repiten por comodidad.

## Reproducción real de búsqueda

Sobre producción83a9d12 (idéntica a61a6339), con las ocho pruebas nuevas:8ejecutadas,3correctas/5fallidas,0errores/omisiones. Fallan autocompletado ambiguo, comando exacto frente a prefijo de aplicación, app con typo, alias y parcial de app frente a acción contextual. XML/log en before-search. La compilación Kotlin/Java pasó;5m14s con restauración de caché,31,095s de tests. No se atribuyen estos fallos al código corregido todavía.

La restauración inicial eligió una entrada preview ext15 del repositorio SDK35; se corrigió descargando el SDK35 base r02 oficial, con su checksum, sin cambiar compileSdk. El Java disponible era JRE sin javac; se restauró el JDK completo Temurin17.0.20.1+1 y se verificó SHA256 oficial. Estos intentos anteriores no ejecutaron pruebas. Proxy Gradle corregido fuera de Git; Roborazzi1.29.0 resuelto sin modificar versión/repositorios.

Código de búsqueda guardado2ca208a y superficie hija a6c0b00, ambos parciales/no verificados. ActionPopupHost usa TYPE_APPLICATION_SUB_PANEL, token del padre, tamaño acotado y FLAG_NOT_TOUCH_MODAL, sin ampliar la paleta visual ni una segunda ventana overlay independiente. FLAG_LAYOUT_IN_SCREEN evita sumar dos veces el origen del overlay; se aplica el frame seguro explícitamente. El padre delega el toque fuera mientras está abierto el hijo; Back/Escape recorren submenús, Ctrl+K cierra, el filtro es independiente. Pruebas de host, geometría y capturas nuevas preparadas; verificación corregida todavía pendiente. Aumento exclusivo de RaycastPreview a33, principal/Review intactas.

## Primera ronda corregida

70casos/11suites:65correctos/5fallidos,0errores/omisiones,3m2s. Host3, ActionPanelKeyboard3, Home4, ResultTouchScroll7, FloatingSearch3 y NativePalette11 correctos; panel acotado, tres acciones visibles48dp,10capturas después y restauración exacta de posición/consulta/selección comprobadas en Robolectric. Sin física.

Cinco fallos analizados: CompactResults y MinimalistSearch aún esperaban completar Chro cuando hay seis apps candidatas; el nuevo contrato explícito exige no completar una ambigüedad. Se conserva el control de no completar el nombre exacto y se añade el caso único Firefox más apertura/cierre de Acciones para el ambiguo, sin quitar foco/teclado. Dos QuickLookTouchTarget fallaban capturando onRoot ante las dos raíces actuales, antes de probar cierre; se cambia exclusivamente a captura de pantalla completa y se conservan todas las aserciones de cierre/tamaño48dp/consulta/no cierre del overlay. El fixture WhatsApp se registró también para el Intent launcher acotado al paquete, como hace Android real. Además, advertencias de configuración no deben recibir clase de comando ejecutable: se protege con una regresión independiente.

Revisión visual real detectó demasiado espacio en encabezados y acciones de Sistema fuera del primer viewport. Se fija lineHeight12sp en los encabezados10sp y máximo560dp para el menú, siempre acotado al frame/IME; filas siguen48dp y paleta no cambia. Se exige también Información de la app visible en tablet. No se agrandan superficies base ni se eliminan acciones.

Los5casos fallidos y suites afectadas se repetirán; no la batería integral. Las65aprobadas iniciales se preservan como evidencia de su SHA, sin confundirlas con las posteriores. Tests y APK nuevos siguen pendientes al guardar.

## Cierre dirigido — código6af47a0

Reintento de las seis suites afectadas:48casos/0fallos/0errores/0omisiones, BUILD SUCCESSFUL in1m48s,5tareas ejecutadas/23reutilizadas. XML y log en affected-final. Gradle conserva XML de suites no seleccionadas: se separaron expresamente los23casos anteriores para no contarlos como una ejecución nueva. Consolidación71casos únicos en rondas:23de corrected-first y48de affected-final; no batería completa nueva. Producción de las cinco suites conservadas sin cambios funcionales en su contrato; fuente precisa y límites quedan explícitos.

Capturas finales reales de pantalla completa, condiciones equivalentes: tablet1280×800dp y teléfono390×844dp, API33/Robolectric, oscuro, Spotify fixture favorita, consulta spot, selección conservada. Siete acciones de la app visibles simultáneamente en tablet sin scroll; filtro, submenú y paleta base sin cambio de límites verificados. No son capturas OneUI ni iconos reales de Spotify: fixture usa icono genérico; producción conserva PackageManager.

Lint y assembleRaycastPreview son el siguiente paso inmediato; no añadir funciones. Se mantiene certificado885838aa/paquete independiente, versión prevista33. Física pendiente: token/ventanas en OneUI, encaminamiento de toque exterior por InputDispatcher real, IME/rotación reales, teclado físico y PDF renderer. Los tests de host comprueban subtipo/token/flags/posición/Back/cierre; no simulan la política real de entrada Android.
