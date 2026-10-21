# SPOTLIGHT NEXT — IMPLEMENTACIÓN VISUAL DEFINITIVA (RAYCAST DARK GLASS)

## Decisión cerrada: UN SOLO LENGUAJE VISUAL

Apruebo **Raycast Dark Glass** como el lenguaje visual definitivo de Spotlight Next. Adjunto este ZIP con CUATRO imágenes y una guía. No mezclar Raycast Dark opaco con un segundo tema gráfico para Ajustes. El componente `03-ActionPanel-Glass-APROBADO.png` es la **fuente principal de color, transparencia, bordes, selección y estilo de superficies** de la paleta y el ActionPanel; `04-Ajustes-Glass-APROBADO.png` es la fuente principal de estructura de Ajustes y cómo aplicar ese MISMO lenguaje a los controles. Las imágenes `01` y `02` aportan **composición, contenidos condicionales, jerarquía y orden**, pero donde difieren en opacidad, **manda la variante Glass de 03/04**. No son screenshots reales de la APK; son renders conceptuales. No copiar textos/datos de demostración como hechos del dispositivo.

**No quiero más planes, renders nuevos ni reinterpretaciones de diseño como entrega principal. Quiero la interfaz Kotlin + Jetpack Compose real, utilizable y fiel a estas cuatro referencias.** No insertar capturas, WebView ni HTML como sustitutos de componentes nativos.

## Continuidad y seguridad

- Repositorio: `bauti3311-sys/Spotlight-Next`.
- Rama de referencia: `feat/raycast-sidebar-permissions-20261020` (verificar rama y HEAD REMOTO actuales; no asumir que son los anteriores).
- Preview anterior conocida: código `61a6339`, paquete `dev.bautista.spotlight.next.raycastpreview`, versionCode 32. No tomarla como estado actual.
- La sesión en curso ya recibió la instrucción de mejorar **ranking, sugerencias y ActionPanel superpuesto**. **Primero comprobá qué está hecho**, conservá avances y tests acreditados. Encadená este rediseño a esa implementación, no abras otra rama competidora ni reescribas el menú funcional de manera incompatible.
- Conservá datos, permisos, servicios, PR borrador, firma histórica del canal Preview y la app principal. No modificar `main`, applicationId ni la keystore.
- Preferir compilaciones locales y tests dirigidos; no consumir GitHub Actions innecesariamente.

## Interfaz obligatoria

### 1. Barra y resultados: 01 y 02
- Overlay centrado de tamaño contenido (barra compacta alrededor de 56dp donde ya está aprobada), sin fullscreen ni fondo opaco que cubra toda la pantalla.
- Búsqueda, resultados relevantes, metadatos discretos, iconos reales de `PackageManager`, selección translúcida, footer mínimo con atajos ANDROID reales.
- Inicio vacío: sugerencias realmente disponibles, ordenadas, máx. una aplicación visible; nada de placeholders persistentes ni acciones ficticias.
- Reutilizar y concluir el trabajo de ranking/autocompletado existente. Coincidencias exactas > prefijos > parciales/fuzzy relevantes > documentos/contactos > acciones contextuales > búsquedas secundarias. No modificar silenciosamente preferencias previas.
- Las acciones que aparecen en los renders (Spotify, WhatsApp, PDF, clima, etc.) son ilustrativas y deben aparecer solo si el dispositivo y el estado real lo justifican.

### 2. ActionPanel: 03 (autoridad visual)
- Panel verdaderamente **superpuesto** a los resultados, con su buscador de acciones, secciones, selección, atajos, submenús y cierre predecible.
- No ampliar la paleta principal como lista vertical alta, no desplazar ni hacer scroll de los resultados base, no perder query/foco/selección al cerrar; scroll interno del ActionPanel solamente si excede pantalla.
- Android tablet y teléfono: posición segura, insets, IME, navegación con teclado/táctil, taps afuera sin secuestro de toques, scroll sin ejecutar PDFs ni otros elementos.
- Atajos reales del proyecto (ej. Ctrl+K) y no símbolos ⌘ propios de macOS como funcionalidad Android.
- Mostrar únicamente acciones realmente disponibles; evitar afirmaciones falsas de reproducción Spotify y envío WhatsApp.

### 3. Ajustes completos: 04 (autoridad estructural)
- Rediseñar **todas** las pantallas y el menú de opciones, no solo General. Sidebar estilo Raycast en tablet, navegación de una columna en teléfono, grupos limpios de filas, selects, toggles y campos con los mismos tokens de la paleta.
- Secciones existentes: General, Apariencia, Búsqueda, Integraciones, Permisos (UNA sola sección consolidada), IA, Atajos y Avanzado, ajustadas al inventario real de la app.
- Auditar cada ajuste real, sus claves, permisos, almacenamiento y navegación, antes de mover controles. Cada control visible debe leer y escribir datos verdaderos: prohibidos sliders/toggles decorativos, categorías duplicadas o estados falsos de permisos y aplicaciones.
- No imitar literalmente que un render muestre Apariencia, Búsqueda y Aplicaciones predeterminadas TODAS dentro de General. El render define estilo, no una mala taxonomía: distribuir cada preferencia en la sección correspondiente, sin perder ninguna capacidad.
- Integraciones deben exponer permisos/disponibilidad real. No invertir prioridad en WhatsApp Business. No agregar permiso/servicio innecesario.
- Menú rápido del engranaje: pocas opciones contextuales; enlazar a Ajustes completos sin duplicar páginas.

### 4. Transparencia/Glass regulable
- Introducir un control real y persistente en **Apariencia**, con vista previa inmediata. Recomiendo rotular **Opacidad de superficie** para evitar la ambigüedad matemática de 'Transparencia 70%' del render (100% opacidad = superficie totalmente opaca). Valor inicial sugerido **80% de opacidad**, sujeto a contraste y pruebas; ofrecer un rango seguro y un botón Restablecer.
- Si preferís mostrar el nombre **Transparencia**, definí explícitamente el valor inverso en UI. No llamar 70% transparencia a alfa=0.7. El porcentaje del render es ilustrativo, NO un contrato matemático.
- Ajustar fondos del buscador, resultados, footer y ActionPanel de forma coherente; el ActionPanel podrá ser ligeramente más opaco por legibilidad. **No aplicar opacidad a todo el WindowManager**, porque eso atenuaría también texto e iconos.
- 'Glass' significa fondo grafito translúcido con contraste robusto y efecto de profundidad: blur de ventanas detrás es opcional si la plataforma lo admite, con fallback sin blur. No pedir captura de pantalla ni nuevos permisos para simularlo.
- No sobrecargar GPU, animaciones, sombras, ni añadir gradientes intensos.

## Verificación visual y funcional obligatoria

1. Terminá/cerrá sin duplicar los cambios de búsqueda y ActionPanel en curso y documentá el HEAD real.
2. Implementá en checkpoints visuales coherentes: tokens/base → buscador/resultados → ActionPanel → Ajustes completos/persistencia/transparencia, preservando commits y revertibilidad.
3. Compará **capturas reales** de la APK/Compose con las 4 imágenes para tablet Samsung Galaxy Tab A9+ y teléfono Motorola G86 (emulador donde proceda, identificándolo). Si no hay dispositivo físico, declaralo.
4. Probar: foco/IME, navegación teclado, scroll/tap, submenús, estabilidad de resultados, contraste sobre fondos claros y oscuros, persistencia de preferencias, accesibilidad, rotación/recorte y regresiones de permisos. No inventar estados exitosos.
5. En el primer checkpoint instalable confiable, entregar Preview APK y ZIP fuente del **mismo commit**, hashes, firma, versionCode, pruebas, screenshots reales y pendientes. Mantener el PR como borrador; no merge a main.
6. Dentro del alcance visual y ajustes aprobados, podés resolver autónomamente detalles menores. Para permisos nuevos, ruptura de compatibilidad, arquitectura radical, recursos remotos, datos migrados destructivamente o servicios persistentes, explicá impacto y pedí aprobación.

## Fuente Figma, secundaria frente al ZIP

https://www.figma.com/design/RitlIxMMIRW6HGLa86aax2

Los kits Raycast comunitarios y macOS se usan como estudio de proporciones y componentes, sin copiar branding, depender de React ni introducir librerías web. **Las cuatro imágenes de este ZIP determinan la dirección visual.**
