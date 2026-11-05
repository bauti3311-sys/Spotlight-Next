# Dirección vigente · Spotlight Focus · 2026-11-05

La solicitud de renovación E1–E5 y el acabado `bb7096c` prevalecen sobre tokens históricos incompatibles: Geist principal con fallback; Icons8 iOS para utilidades; iconos instalados por PackageManager; grafito/marfil; campo editable único global/contextual. No restaurar automáticamente Inter, Lucide, cuatro sugerencias ni tarjetas grandes.

La integración está en código productivo con preferencia persistente `focusInterface`. Su valor inicial sigue siendo falso hasta la comparación/aprobación de la composición y conserva la interfaz anterior como recuperación. No es otro motor ni una maqueta. Ver [informe de integración](docs/FOCUS-INTEGRACION-E1-E5-20261105.md).

Se mantienen las preferencias existentes de fuente anterior, opacidades, densidad, acento, ancho y movimiento. Geist/Inter conservan sus licencias OFL; Icons8 conserva atribución y su aviso específico, no se reclasifica como MIT. Sin blur obligatorio, nuevos permisos ni servicios. La aprobación conceptual no equivale a validación física de One UI.

---

## Documento histórico de diseño

# Spotlight Next 2.0 — diseño nativo

La búsqueda es la pantalla principal. Una consulta mezcla acciones locales, aplicaciones, documentos autorizados y herramientas; los filtros son opcionales. Una coincidencia precisa pesa más que el número de archivos o los hábitos. La acción principal aparece antes de las alternativas y el panel contextual conserva la consulta al volver.

## Sistema visual

Compose, Inter con licencia OFL y símbolos Lucide con licencia MIT. Los iconos de aplicaciones provienen de Android. No se incorporan SF Symbols ni componentes React de terceros.

Tres familias: macOS Glass (superficie translúcida, reflexión estática discreta y bordes graduados), Raycast Graphite (superficie opaca y compacta), Minimal Pure (mínima decoración). Tema claro/oscuro/automático, seis acentos, tonos neutro/cálido/frío, tipografía, densidad y selección. Los temas se previsualizan antes de aplicar; duplicación, importación y exportación admiten solo campos visuales. Las preferencias históricas siguen siendo legibles.

Tokens centrales en GlassTokens: 8/12/18 dp, controles táctiles de 48 dp, selección de 90 ms. No se retrasa la primera presentación para una animación de entrada. El modo reducido elimina interpolaciones. El vidrio conserva opacidad mínima de 90 % para legibilidad. El fondo real NO se desenfoca: One UI puede mostrar una huella rectangular al usar FLAG_BLUR_BEHIND. Se utiliza una alternativa nativa estática sin shaders, refracción ni APIs privadas.

## Organización

Inicio vacío o hasta cuatro sugerencias útiles, como una aplicación disponible o una acción reciente. Nunca se agregan accesos genéricos para rellenar la pantalla. Búsqueda con hasta doce resultados iniciales; máximo dos archivos y tres resultados de una misma familia. Ver más mantiene la consulta. La selección y el orden ya visible se conservan al navegar mientras llegan fuentes tardías.

Las opciones rápidas comparten el host TYPE_APPLICATION_OVERLAY con la barra. No hay PopupWindow/DropdownMenu separado: evita el token de Activity inexistente y las pérdidas transitorias de foco. Tema, privacidad y efectos preceden a los filtros. Escape cierra primero el panel; los toques exteriores o el cierre explícito cierran Spotlight.

Ajustes en seis grupos: Apariencia, Búsqueda, Inteligencia, Integraciones, Invocación, Privacidad y sistema. Búsqueda global por títulos y sinónimos; sidebar en ancho grande y selector integrado en teléfono. Tasker y los perfiles históricos quedan accesibles en un segundo nivel.

## Referencias consultadas

Las referencias aportan organización, jerarquía, materiales y transiciones; el código y los tokens son propios de Android. Se consultó contenido público de 21st.dev, uiarc.dev/components/blocks/command-palette, ui.shadcn.com/docs/components/base/command, component.gallery, kinetics.colorion.co, microkit.design, smoothui.dev, magicui.design, ui.aceternity.com, refero.design/styles, design.md, mobbin.com, minimal.gallery y kage.design. glass.samasante.com y easyui.pro mostraron contenido limitado; uselayouts.com y motion-primitives.com no pudieron inspeccionarse de forma completa. No se atribuye a esas páginas una validación visual que no se realizó ni se copian activos de licencia desconocida.

Fuentes funcionales: manual.raycast.com, developer.android.com y documentación oficial de Open-Meteo y TheSportsDB. Referencias no implican paridad con macOS, Raycast o Apple Intelligence.
