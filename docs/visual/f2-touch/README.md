# F2: espacio táctil explícito al cerrar Quick Look

Referencia: patrón Material/Compose ya usado por el cierre principal y las acciones (48 dp); token existente `GlassTokens.hit`. No se copian recursos ni se agregan bibliotecas/licencias.

El botón de cierre de la vista previa asignaba 28×28 dp de layout. Compose puede ampliar implícitamente el hit target fuera de ese layout: no se afirma que todo toque fallara antes. La corrección reserva explícitamente 48×48 dp, evitando depender de un área extendida sobre controles/contenido vecinos. El icono permanece en 17 dp. No cambia ancho manual, densidad, preferencias, Quick Look PDF ni teclas.

Prueba RED con fuente876f7cb: dos tests nativos Robolectric API33 fallan con `Actual width is 28.0.dp, expected at least 48.0.dp`. Capturas `tablet-before.png` y `phone-before.png` con el mismo fixture. Pruebas nuevas comprueban área asignada48dp, toque real cerca del borde lateral izquierdo (dentro de la forma circular), cierre solo de vista/acciones, consulta preservada y overlay abierto.

Las capturas son fixtures sintéticos tablet horizontal1280×800 y teléfono vertical390×844, tema claro; no validación física en Android16/OneUI ni prueba del renderizador PDF. Comparadores antes/después y resultado integral se registran en progreso/notas al cerrar. No actualizar referencias existentes para ocultar diferencias; revisar cualquier cambio intencional.

GREEN focalizado: dos tests nuevos y KeyboardHintsTest correctos. El primer intento tocaba la esquina fuera del círculo y el siguiente tenía un error de compilación del fixture; corregidos sin eliminar assertions ni ampliar plazos. Diferencias revisadas: encabezado20dp más alto, icono17dp intacto y contenido desplazado dentro del panel, consulta/resultados izquierdos intactos. El hint superior Tab/Shift+Tab permanece visible; el hint duplicado inferior puede quedar fuera del viewport limitado del fixture. No se declara mejora de rendimiento.
