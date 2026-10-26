---
name: spotlight-native-craft
description: Refinar y revisar la interfaz Kotlin Compose de Spotlight Next para evitar diseño genérico, mejorar jerarquía, densidad, accesibilidad y estados sin reemplazar Solid Graphite ni los flujos Raycast aprobados.
---
# Precisión antes que decoración

Adaptación local de Impeccable (Paul Bakaus, Apache-2.0), modificada para Compose y el contrato de Spotlight. Leer [fuentes y límites](references/sources.md). Esta versión no usa su ejecutable, instalador, hooks ni descargas automáticas.

1. Leer AGENTS y la skill spotlight-visual-review. Elegir un flujo concreto y una métrica observable: filas visibles, acciones inequívocas, destino, foco o recuperación. Usar el código y las capturas actuales como autoridad.
2. Tratar Spotlight como herramienta de operación. Conservar Solid Graphite, tipografía aprobada, iconos reales de aplicaciones y el sistema de iconos ya aprobado para cada superficie y una búsqueda universal. No introducir una nueva paleta, fuentes, gradientes, tarjetas anidadas, badges repetidos, efectos ni pantallas por gusto.
3. Dar prioridad visual a la acción principal. Agrupar alternativas como filas coherentes y metadatos secundarios discretos. No agregar encabezados o explicaciones que repitan el contenido.
4. Todo control debe exponer acción, rol y estado correctos para TalkBack y teclado. Mantener objetivos táctiles de 48dp aunque el icono mida menos. Los iconos decorativos no necesitan descripción. No convertir Enter/Tab/Escape en acciones distintas.
5. Comprobar carga, vacío, error, éxito verificado, permiso revocado y cancelación. Nunca anunciar éxito por haber solicitado una acción externa. Los mensajes deben indicar recuperación sin culpar al usuario ni mostrar errores privados del proveedor.
6. Medir móvil/tablet, orientación, teclado y texto ampliado. Comprobar límites de filas, footer, inspector y altura de ventana real; contar resultados no demuestra visibilidad. No fijar alturas para ocultar problemas ni añadir scroll cuando sobra espacio.
7. Aplicar el cambio mínimo. Ejecutar casos Compose de semántica, foco y geometría pertinentes; comparar capturas reales del mismo escenario cuando cambie su apariencia. Registrar por separado JVM/Robolectric y prueba física. No afirmar mejoras de rendimiento sin medición.

La skill guía al desarrollador; instalarla no cambia por sí solo el APK ni garantiza una interfaz mejor.

## Critique → simplify → audit → polish

- Critique: choose one screenshot/state, identify at most four observable defects by region and user effect. Read code to distinguish clipping from intentional truncation; do not invent defects from taste.
- Simplify: remove a redundant layer/control only if its function remains accessible; preserve labels, confirmation, recovery and permission explanations. No new palette or generic card system.
- Audit: check hierarchy, text scaling, contrast, touch target, selected vs focused semantics, keyboard traversal, footer/window/IME bounds, scroll and error recovery. Mark unobserved states pending.
- Polish: apply the smallest reversible correction, compare real before/after at identical fixture settings, run directed input/geometry/restore tests, and stop when the observed defect is fixed.
- Use spotlight-raycast-native for component mapping and the adoption review. This extends the existing Impeccable adaptation; it does not reinstall or update upstream code. Never equate more installed skills with better UI.
