# F1.4 · Expansión contextual

Capturas reales de Compose/Roborazzi SDK33, tablet1280×800, región útil1200px y escala1. No emulador/OneUI ni medición de rendimiento. Consulta2+2, movimiento desactivado para capturar estado final.

- results-before.png: comportamiento previo, ancho960px; test falla porque no solicita expansión.
- results-after.png: consulta activa, ancho1080px; misma lista y acciones.
- query-cleared.png: contracción al limpiar consulta.
- results-diff.png: diferencia absoluta sobre lienzo común, fondo48/48/48; no baseline aceptada ni alterada.

La captura manual Canvas anterior omitía textos cacheados y captureToImage agotó su espera en Robolectric. Se usa Roborazzi para estos artefactos en build/091-ui, sin escribir baselines existentes. Reproducción RED conservando assertions y regeneración anterior con el mismo capturador. El primer intento de tests de animación tenía una llamada Kotlin incorrecta a setCurrentFraction; corregida sin cambiar asserts.
