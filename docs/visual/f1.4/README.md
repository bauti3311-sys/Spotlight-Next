# F1.4 · Expansión contextual

Capturas reales de Compose/Roborazzi SDK33, tablet1280×800, región útil1200px y escala1. No emulador/OneUI ni medición de rendimiento. Consulta2+2, movimiento desactivado para capturar estado final.

- results-before.png: comportamiento previo, ancho960px; test falla porque no solicita expansión.
- results-after.png: consulta activa, ancho1080px; misma lista y acciones.
- query-cleared.png: contracción al limpiar consulta.
- results-diff.png: diferencia absoluta sobre lienzo común, fondo48/48/48; no baseline aceptada ni alterada.

La captura manual Canvas anterior omitía textos cacheados y captureToImage agotó su espera en Robolectric. Se usa Roborazzi para estos artefactos en build/091-ui, sin escribir baselines existentes. Reproducción RED conservando assertions y regeneración anterior con el mismo capturador. El primer intento de tests de animación tenía una llamada Kotlin incorrecta a setCurrentFraction; corregida sin cambiar asserts.

## Acciones y teclado: segunda unidad funcional

keyboard/ contiene capturas y los cinco comparadores nativos revisados, con hashes y regiones exactas en review.json. Cuatro cambios solo afectan el texto del footer; tablet-context-panel cambia únicamente el panel derecho: elimina Sin vista previa/Abrir en otra app para calculadora, muestra pistas junto a Acciones y libera espacio para acciones reales. Tamaños idénticos; lista izquierda preservada. Se actualizaron exactamente esas cinco referencias, dejando doce intactas, después de368 unitarias correctas y revisión visual de TODOS los diffs. Verificación posterior17/17 correcta. No máscaras ni asserts debilitados; F1.3 frío sigue abierto, sin usar baselines para esconderlo.

RED de KeyboardHintsTest: footer incorrecto; después RED adicional por botón externo presente en una acciónCopy. Corregidos sin alterar el contrato. Fixture fija720dp/espera layout; asserts reforzados: Escape conserva dos textos4 (resultado+preview), luego uno y finalmente cierraoverlay. Record diagnosticartefactos exclusivamenteenbuild, no aceptación automática de referencias.
