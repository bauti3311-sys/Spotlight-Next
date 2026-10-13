---
name: spotlight-visual-review
description: Investigar referencias, implementar patrones nativos en Compose y comparar capturas verificables de Spotlight Next.
---

# Flujo

1. Leer AGENTS.md y los documentos aprobados. Identificar flujo, orientación, tamaño, tema y estados necesarios antes de cambiar el diseño.
2. Investigar referencia original, versión y licencia. Material web premium/sin licencia sirve como referencia de comportamiento, no como autorización para copiar código o recursos. Hugeicons/Phosphor solo con recursos compatibles y atribución conservada.
3. Ejecutar la referencia en el laboratorio web separado cuando haya acceso legítimo. Inspeccionar animación/interacción; registrar fuente y limitaciones. No agregar React/Vite/WebView al APK.
4. Implementar el patrón con Compose nativo. Evaluar bibliotecas una por una con la matriz de compatibilidad; no actualizar Kotlin/Compose/Gradle en bloque para hacerlas encajar. Mantener fallback de efectos gráficos y accesibilidad.
5. Generar capturas reales de barra, resultados/selección, panel lateral, PDF, menú, Guardados, Ajustes y carga/error en teléfono/tablet, vertical/horizontal. Desactivar animaciones variables únicamente en el entorno de captura, sin eliminar pruebas de interacción.
6. Comparar con baseline versionada y revisar diferencias/áreas enmascaradas. Nunca actualizar automáticamente baselines para ocultar un fallo. Ejecutar teclado/foco/overlay además de comparación visual.
7. Corregir, repetir checks pertinentes y registrar capturas, diferencias, dispositivo/API, SHA y límites en PLANS.md. One UI, transparencia entre ventanas y teclado físico requieren validación en Galaxy Tab A9+.

Roborazzi/Maestro se incorporan gradualmente como herramientas de tests; su presencia en configuración no significa que las pruebas hayan sido ejecutadas. No afirmar mejoras de fluidez sin trazas/mediciones.
