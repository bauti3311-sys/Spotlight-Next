# Revisión nativa Gemini — Work, 14/10/2026

Capturas de producción Jetpack Compose, rasterización Android nativa mediante Robolectric4.14.1/API33. **No son capturas de una Samsung ni de un emulador conectado**; no mockups ni React/WebView. Qualifiers1280×800, y800×1280 en el caso Light vertical; el PNG recoge el contenido de la ventana, no toda la pantalla. Fuente de cada imagen/tamaño/hash en captures.json. Las aplicaciones son fixtures de PackageManager con artwork de plataforma; no logos inventados ni apps verificadas en la tablet.

## Revisión crítica

- Barra vacía:56dp, texto16sp, tres accesos visibles en tablet, glyphs15dp según el nuevo Kotlin; objetivos táctiles48dp preservados. El área de búsqueda se mantiene nativa y enfocada antes de retirar el cursor intermitente para capturar.
- Obsidian/Graphite/Light: gradientes ARGB de Gemini, borde tenue y reflejo superior, secundarios94949E/A0A0AB/52525B. El fondo gris externo pertenece al host de prueba: no acredita transparencia/blur OneUI.
- Aplicaciones/selección: filas compactas en línea, iconos26dp y selección gris sin barra azul ni tarjetas independientes. Persisten resultados reales del motor, no el catálogo simulado de Gemini.
- Antes de la cápsula había cinco indicaciones dispersas en el pie. before-footer/ conserva esos píxeles; after/ contiene la cápsula contextual y vignette final, con Enter y Tab reales. Se reduce la saturación sin cambiar los contratos de Escape ni introducirCtrl+K en su lugar.
- Acciones: menú nativo bajo demanda; conserva búsqueda/foco y lista desplazable. Sigue siendo más ancho y alto que el popover de la referencia por sus controles táctiles48dp. No se declara fidelidad absoluta ni se elimina contenido funcional para hacer una captura más bonita.
- PDF: inspector solo al seleccionar documento, expansión/cierre por capas probados. La imagen renderer-unavailable muestra el fallo de PdfRenderer de Robolectric, NO una página ni un archivo realmente vacío. El espacio disponible para una página real y su render deben comprobarse en dispositivo; no se sustituye por un bitmap ilustrativo.
- La vista Light vertical conserva contraste y campo entero. La rotación/IME/foco frío/blur y actualización con datos conservados quedan en docs/TABLET-GEMINI-CHECKLIST.md. No afirmar rapidez o fluidez física sin medición.

Las17 baselines históricas se compararán contra la nueva presentación: registrar diferencias esperadas (barra, filas, temas, iconos, pie) y revisar imágenes antes de aceptar nuevas referencias. Un porcentaje de píxeles no prueba que la app esté bien diseñada.
