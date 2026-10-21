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

## Entrega Preview33 verificada

APK y ZIP fuente del mismo SHA560608f48fafa059c0a10df3c3772b4c247695d4, producción idéntica a6af47a0. LintRaycastPreview0errores/13advertencias/1informativa y assembleRaycastPreview correctos (BUILD SUCCESSFUL in4m33s), sin reiniciar ni repetir pruebas aprobadas. apksigner correcto, certificado885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba; paquete dev.bautista.spotlight.next.raycastpreview, versión33/2.0.1-raycast-preview, ARM64/min26/target35. Variante debug independiente; no R8 ni actualización del paquete principal.

191entradas de producción/configuración coinciden con Git; ZIP git archive con969archivos contrastados blob por blob y sin claves/credenciales/cachés. APK86523300bytes, SHA25646e9fa6afecd14e623630df9da219bf23c3e0665880e0968d2b145035d78f24c. ZIP7993553bytes, SHA25697b7cde93b33c268d170d7eb42a007e0a636469ecde7a7f4276e0e193aae3dec. Identidad, logs, XML lint y hashes en delivery. Capturas antes/después originales y reporte visual interactivo en el paquete de evidencias de la entrega.

Advertencias conservadas: incluye ViewConstructor del host programático y ClickableViewAccessibility por su manejo de ACTION_OUTSIDE (acciones accesibles delegadas a Compose). No se suprimieron controles ni advertencias para lograr verde. Los8.so precompilados siguen sin stripping; zipalign16KB correcto, funciones preservadas. ADB consultado en puerto propio5041:sin dispositivos; se cerró ese servidor. No hay validación física ni medidas de rendimiento, proveedor externo real o automatización futura.

Gradle terminó; no hay ejecución pendiente. No añadir funciones antes de entregar estos archivos y revisar OneUI/IME/toques exteriores/teclado/PDF. WhatsAppBusiness fuera del alcance y sin modificaciones específicas. Sin CI, main, merge, force-push, permisos/dependencias nuevos ni nueva clave. Próximo bloque: observaciones físicas de Preview33 y solo correcciones reproducibles.


## Continuación21/10 · contratos adicionales de ActionPanel

Partida091bb3d, árbol limpio y reserva propia b347871 confirmados mediante GitHub. Checkpoint previo a pruebas8e54047. El entorno conservaba repositorio/entregables pero había perdido JDK/SDK/Gradle/caché en /tmp. Se restauraron Temurin17.0.20.1+1, Gradle8.10.2 y SDK35 con hashes oficiales, sin modificar versiones del proyecto.

Se agregaron tres contratos Compose/Robolectric API33 en ActionPanelKeyboardContractTest:
- Puente de Back: volver del submenú restaura el filtro padre, el siguiente Back cierra y la composición libera el callback. Se invoca el controlador compartido; no se afirma validación del dispatcher nativo/OneUI.
- Ctrl+F enfoca el filtro independiente, Enter ejecuta el resultado filtrado y Ctrl+K cierra sin ejecutar otra acción.
- Reordenamiento asíncrono conserva la identidad seleccionada; si desaparece, Enter usa el primer resultado vigente.

Ejecución dirigida: `:app:testRaycastPreviewUnitTest --tests dev.bautista.spotlight.ui.ActionPanelKeyboardContractTest`. Primera ronda6casos:5correctos/1fallo de selector duplicado (acción y texto editable «Sistema»),5m04s incluyendo caché fría. Primer reintento de un caso:1fallo de aserción del fixture, porque el campo vacío expone también el placeholder. Se corrigieron solamente los selectores: excluir action-filter al buscar la fila e inspeccionar SemanticsProperties.EditableText para comprobar el vacío. Todas las aserciones funcionales se conservan.

Último reintento del único caso afectado:1/1correcto,0errores/omisiones, BUILD SUCCESSFUL38s. Total6casos únicos verificados en rondas5+1, incluidos los tres nuevos; no se repitieron las otras suites. XML/logs de las tres rondas preservados en docs/verification/action-navigation-20261021. No sumar este bloque como una ejecución completa nueva de71casos.

Sin cambios de producción/configuración respecto al SHA fuente560608f de Preview33; no se repitieron lint/assemble ni se generó otra APK. La firma v2 del APK existente se volvió a comprobar con apksigner: certificado885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba. Hash APK46e9fa6afecd14e623630df9da219bf23c3e0665880e0968d2b145035d78f24c y fuente97b7cde93b33c268d170d7eb42a007e0a636469ecde7a7f4276e0e193aae3dec coinciden. Se mantienen los archivos entregados del mismo SHA560608f.

Pendiente: validación física OneUI, dispatcher nativo Back, foco/IME/rotación y propagación del toque exterior con InputDispatcher real. Los nuevos tests no sustituyen estas verificaciones. Sin cambios de permisos, firmas, UI, funciones, Actions o main.
