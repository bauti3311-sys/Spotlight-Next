# F2.1: cierre táctil de Quick Look y acciones adaptables

Referencia: patrón Material/Compose de48dp usado por cierre principal y acciones. `GlassTokens.hit` sustituye el layout28×28dp del cierre de vista rápida; icono17dp intacto. Material podía extender implícitamente el área de toque: no se afirma que todo toque fallara antes. Reservar48×48dp evita depender de un área fuera del layout compartida con vecinos.

Dos tests API33 Robolectric usan tablet horizontal1280×800 y teléfono vertical390×844, tema claro. RED: ancho28 frente al mínimo48. GREEN: dimensiones48, acción principal/hint visibles, scroll hasta Guardar como nota, toque lateral dentro del círculo cierra preview/acciones y conserva consulta/overlay. Corner fuera de círculo y error de compilación del fixture se corrigieron sin quitar assertions ni ampliar15s.

La revisión detectó un recorte de hint al ampliar el encabezado. `weight` solo en altura ilimitada vaciaba la lista: descartado. Token central `actionPanelMaxHeight=360dp`, lista desplazable de máximo240dp con espacio restante acotado; conserva acciones y hints. La primera corrección no se aceptó como baseline.

Capturas antes/después/diff de ambos tamaños en este directorio. Comparación versionada:17tests,1 cambio contextual intencional, revisado en `tablet-context-compare.png` y `baseline-review.json`; solo lado derecho x664–1248, mismo1280×564,16 referencias intactas. Solo esa referencia se actualizó manualmente después de corregir el recorte; nueva verificación17/17 correcta47s. Ningún mask, timeout o assertion reducido.

No dependencias/licencias nuevas ni recursos copiados. No prueba física Android16/OneUI, PDF instrumentado o medición de rendimiento. F1.3 continúa abierto. Resultado integral y CI exacta en PROGRESO y notas del SHA de cierre.
