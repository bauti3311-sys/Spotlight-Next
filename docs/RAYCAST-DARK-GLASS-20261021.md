# Raycast Dark Glass · inventario y continuidad21/10

Base remota/local5cb4a139, reserva propia da6503cc comprobadas. Ranking y ActionPanel verificados previos conservados. Imágenes aprobadas verificadas contra SHA256.json del ZIP original; 03/04 tienen prioridad material, 01/02 composición.

## Inventario conservado

Los cuerpos y callbacks de MainActivity no se reemplazan. Se restilizan mediante SettingsPreferenceSection/SettingsGroupSurface y Theme compartidos. Rutas canónicas/alias de navegación y claves DataStore permanecen. Tasker/rutinas/centros retirados continúan ocultos, sin borrar datos.

| Sección real | Preferencias/capacidades referenciadas |
|---|---|
| Contexto autorizado · Inteligencia | Componente existente y callbacks conservados |
| Permisos y accesos | contacts, copy, trees |
| Privacidad · Spotlight Glass | Componente existente y callbacks conservados |
| Invocación · teclado y posición | Componente existente y callbacks conservados |
| Búsqueda · prioridades | Componente existente y callbacks conservados |
| Diagnóstico de Spotlight | onboarded |
| Bienvenido a Spotlight Next | copy |
| Centro de comandos y perfiles | Componente existente y callbacks conservados |
| Tasker · automatización externa | Componente existente y callbacks conservados |
| Clima y agenda deportiva | Componente existente y callbacks conservados |
| Extensiones e integraciones | Componente existente y callbacks conservados |
| Integraciones · capacidades disponibles | Componente existente y callbacks conservados |
| Transferir configuración | first |
| General · inicio | copy, suggestions |
| WhatsApp y notificaciones | Componente existente y callbacks conservados |
| Acceso rápido | Componente existente y callbacks conservados |
| Teclado físico · acceso global optativo | clipboardShortcut, copy, shortcut |
| Apariencia | Componente existente y callbacks conservados |
| Aprendizaje local | copy, learning |
| Portapapeles temporal | clipLimit, clipRetention, clipboard, copy |
| Búsqueda y archivos | copy, files, trees |
| Búsqueda web | copy, engine |
| Texto e imágenes de otras apps | Componente existente y callbacks conservados |
| Índice y reconocimiento local | contentIndex, copy, videoIndex, visualIndex |
| Búsqueda por significado · local | Componente existente y callbacks conservados |
| Audio · transcripción optativa | Componente existente y callbacks conservados |
| Tarjetas de información | copy, liveEndpoint, online |
| Alias personales | Componente existente y callbacks conservados |
| Integraciones automáticas | Componente existente y callbacks conservados |
| Herramientas incluidas | Componente existente y callbacks conservados |
| Rutinas y automatizaciones | Componente existente y callbacks conservados |
| Inteligencia artificial · opcional | aiProvider, copy |

## Checkpoint parcial

Material compartido y rango70–100%, valor nuevo por defecto80%, sin migrar el valor ya almacenado. Opacidad utiliza la clave existente opacity; updateAppearance ahora la guarda junto a las preferencias visuales permitidas sin sobrescribir privacidad/historial. Importación/exportación amplía el rango para conservar temas nuevos. Sin permisos/dependencias nuevos.

Ajustes: ocho áreas accesibles; sidebar en tablet/selector en teléfono; todas las secciones comparten grupos translúcidos. General abre preferencias en lugar de hero; configuración guiada se conserva bajo demanda. MainActivity usa wallpaper del sistema con API pública y fondo de ventana transparente, sin leerlo ni capturarlo. Android34 desactiva el envío de toques al wallpaper.

ActionPanel: mantiene hijo acotado/token/flags; filtro arriba, subtítulos y keycaps Android. Sin cambiar motores de búsqueda, ejecución, permisos, PhoneUse, Gemini o MediaSession.

Capturas baseline: dos casos DarkGlassSettingsTest aprobados sobre producción5cb4a1 antes de editar. Capturas de paleta de Preview33 conservadas bajo las mismas dimensiones/fixture. Las pruebas de esta nueva implementación están pendientes al publicar este checkpoint.

Referencia API: https://developer.android.com/reference/android/view/WindowManager.LayoutParams#FLAG_SHOW_WALLPAPER
