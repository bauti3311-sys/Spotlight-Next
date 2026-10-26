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


## Primera ronda y refinamiento
51casos/11suites:48correctos,3fallos,0errores/omisiones,1m22s. XML/logs preservados en verification/dark-glass-20261021/first. Los dos fallos QuickLook se detuvieron en un hint de footer sustituido por keycaps; se conserva la comprobación de hint visible, cierre48dp, consulta/resultado y overlay al cerrar. Se comprueba ahora Enter dentro del panel y ausencia del panel al cerrar, además de disponibilidad del botón Acciones.

Integraciones: el catálogo de módulos anterior al encabezado se expande asíncronamente y puede desplazar el objetivo después de performScrollTo. Se mantiene accesible y bajo demanda, colapsado inicialmente junto a los otros catálogos extensos. El encabezado y la comprobación de estado real siguen sin debilitarse.

Revisión visual real: sidebar y contenido ya comparten una superficie; filtro de acciones arriba y filas visibles. Se refinan el slider a pista fina/pomo circular manteniendo la semántica/gestos de Material, el acento azul para contraste blanco4.59:1 y los grupos de Apariencia. La vista previa completa de ejemplo se conserva bajo demanda, la ventana de Ajustes cambia opacidad inmediatamente. ActionPanel usa mayor opacidad (mínimo90%, superficie+16puntos hasta100%) para evitar que el texto de resultados se lea a través sin blur costoso; sigue siendo regulado por la preferencia. En tablet el panel sobresale96dp hacia la derecha cuando cabe; teléfono mantiene el anclaje anterior y siempre se limita al área segura.

Pruebas afectadas activas al guardar este checkpoint; no declarar resultado todavía.


## Cierre de pruebas sobre código2ffe81e
- Compilación Kotlin real correcta58s (incluyó reintento automático del daemon al arrancar; no fallo del código).
- Primera ronda51:48aprobados/3fallos conservados. Ronda afectada24/24aprobados1m17s: material4, ajustes3, capacidades1, ancho7, paleta3, QuickLook2, host4.
- Capturas/contratos tras tarjetas de tema e icono contextual:9/9aprobados1m16s, incluyendo un caso nuevo con siete capturas sobre fondos controlados claros/oscuros.
- Último ajuste de proporciones:3/3aprobados1m01s; Ajustes960dp máximo, margen vertical32dp en tablet (75% del ancho y92% del alto en1280×800); sidebar224dp. Panel de acciones con salida lateral18% del ancla hasta144dp, limitado a viewport; sin cambiar paleta ni scroll.
- Total56casos únicos acreditados entre rondas (31no afectados retenidos +24afectados +1caso nuevo). No hubo una ejecución fresca completa de56. Ningún test eliminado, desactivado ni aserción funcional reducida.
- Confirmación estructural adicional: mismos títulos de sección y campos config.* en MainActivity que5cb4a1. Manifest/SDK/permisos/servicios y dependencias sin cambios. Configuración guiada recupera su acceso, ocho rutas y todos los cuerpos funcionales conservados; historial, alias y servicios no se migran.
- Cada área tiene captura nativa en tablet1280×800 y teléfono390×844. La comparación con03/04 se realizó sobre capturas reales: material uniforme, bordes finos, selección neutra, filtro superior, tipografía Inter, keycaps Android, tarjetas de tema, pista fina/pomo circular, sidebar compartido. No se afirma identidad pixel a pixel: las referencias contienen render/wallpaper difuminado, seis resultados de ejemplo y logos, mientras la app conserva hasta tres resultados iniciales reales.
- Robolectric no compone el wallpaper del sistema en Activity; los fondos negros de Ajustes en capturas no acreditan la translucidez interventana en OneUI. Los siete escenarios de contraste usan fondos de prueba explícitos. Spotify es paquete fixture con icono genérico del PackageManager en laboratorio; el APK conserva los iconos de aplicaciones instaladas reales. Sin instalaciones físicas, IME real, blur, proveedor ni medición de rendimiento acreditados.

Lint/assemblePreview34, verificación final de identidad y entrega siguen pendientes al publicar este cierre de pruebas. Firma histórica885838aa comprobada antes de compilar; no se generó clave nueva.


## EntregaPreview34 verificada
Fuente APK/ZIP:cc1c28d5bb2471c9e1c78af5cfcac13b510f2f0d. Código igual al último gate2ffe81e;193entradas de producción/configuración verificadas porSHA256 y1016archivos del ZIP contrastados uno por uno con blobsGit.

`:app:lintRaycastPreview :app:assembleRaycastPreview`:BUILD SUCCESSFUL3m18s,45tareas(12ejecutadas/33al día). Lint0errores/15advertencias/1informativa. Las dos advertencias adicionales frente aPreview33 son avisos informativos de versiones de dependencias existentes (ComposeBOM/documentfile); no se actualizaron dependencias. Se mantienen los avisos previos de ScopedStorage, APIShizuku, ExifInterface, RedundantLabel, ChromeOSABI, BouncyCastle, backup y hosts programáticos/accesibilidad. Ningún aviso se ocultó.

APK ARM64, paquete dev.bautista.spotlight.next.raycastpreview, versionCode34/versionName2.0.1-raycast-preview, variante independiente derivada de debug (no Release/R8). Firma v2 y zipalign16KB correctos. Certificado histórico885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba recuperado del respaldo privado autorizado; no clave nueva ni firma principal/Review alterada. MainActivity declarada como launchable; funcionamiento básico cubierto en Compose/Activity, sin instalación del APK en dispositivo.

- APK Spotlight-Next-Raycast-Preview-34-cc1c28d.apk:86.539.684bytes;SHA256 f2384b62091eecafae6309063c2be26b9cf5c8e73ea1a5130ddc6d03f4c6f524.
- Fuente Spotlight-Next-source-cc1c28d.zip:8.068.614bytes;SHA256 b8f19d031494d19309785ea49cec187623154e20b7f49db86b504ed61cfd586c.

Se ejecutó solo el caso teléfono de FloatingSearchFlowTest después del build para renovar sus cinco PNG con el placeholder contextual final:1/1aprobado17s, sin recompilar producción ni repetir suites. El total único permanece56; union de IDs de casos acreditada en verification.json.

Comparativa HTML autocontenida y ZIP de evidencias incluyen los PNG originales, antes/después, cuatro renders de referencia identificados como conceptos y todos los XML/logs. Las imágenes no se insertan en el APK como UI. PR18 sigue abierto/borrador, basePhoneUse; rama feat/raycast-sidebar-permissions-20261020. Sin merge/main/force/Actions manuales/gastos. Sin procesos Gradle pendientes. Próxima validación útil: instalar esta actualización Preview en tablet y probar opacidad, wallpaper/IME, rotación y toque exterior con el InputDispatcher real. No continuar otro rediseño hasta feedback de esta entrega.
