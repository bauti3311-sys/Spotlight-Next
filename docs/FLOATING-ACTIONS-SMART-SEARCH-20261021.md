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
