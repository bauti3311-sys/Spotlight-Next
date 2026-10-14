# Raycast: checkpoint de composición

Referencia aprobada: captura aportada el 14/10/2026. Se toma la ventana central, no los controles del visor Android. Una superficie continua grafito, contorno tenue y sombra difusa; lista plana con selección neutral, metadatos alineados y pie subordinado. No se copian snippets ni funciones de Raycast.

Tokens Android: radio 20 dp, barra 68 dp, iconos 20–22 dp/objetivo táctil 48 dp, filas 52–60 dp, margen de lista 8 dp, texto principal 15–16 sp/secundario 12–13 sp, selección 8 % blanco en oscuro/6 % negro en claro, transición 90 ms (sin movimiento cuando el sistema lo reduce). Inter y trazos existentes; iconos originales de aplicaciones.

A: contenedor/barra/lista/selección. B: cálculos/apps/comandos mantienen una lista; acciones bajo demanda sin repetir contenido; inspector solo documentos y contenido enriquecido. C: pulido del pie, tema claro y consistencia. Las capturas serán renderizados nativos reproducibles, no mockups. Comparar composición y legibilidad, no usar porcentaje de píxeles como aceptación.

Límites: Robolectric no verifica el blur entre ventanas, InputDispatcher/IME real ni One UI físico. OverlayService evita FLAG_BLUR_BEHIND para no extender blur al rectángulo transparente del overlay. No habilitarlo sin evidencia física. F1.3 continúa abierto.

Iconografía auditada: Hugeicons free Stroke Rounded (controles de barra, licencia conservada) ofrece los accesos esenciales; Lucide ISC ya cubre las acciones/utilidades; Phosphor Regular MIT identifica PDF y recursos puntuales. No se descarga ninguna familia ni se copian iconos premium de la referencia. Se unifican tamaño/tinte neutral y se eliminan azulejos de fondo; los bitmaps originales de las apps permanecen. La cobertura limitada de Hugeicons evita sustituir iconos de acciones por glifos genéricos o inventados. Unificación completa de familias pendiente de revisión visual física.

Las pruebas de Escape cambian el contrato de consultas simples de forma explícita: acciones → lista → cerrar overlay; mantienen foco, consulta, contador de cierres y ausencia de duplicación. Los documentos mantienen su capa de Quick Look. Los antiguos fixtures de cálculo prueban ahora «Cerrar acciones», con los mismos objetivos táctiles y assertions de conservación de consulta/overlay. Ningún test se desactiva ni se cambia un timeout.
