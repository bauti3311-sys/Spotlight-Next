# V06/V08 — acciones de notificación sin ancho cero

Base73af4d61d27fabf875c90cbd75cf195ab45352b3, PR21. `before`4PNG + XML/log rojo:
1caso correcto (tablet),3fallidos (390dp100%,330dp200% ambos temas). `after`4PNG
con mismos datos/dimensiones/temas; tablet normal idéntica byte a byte. No retocar.
Componente productivo real, notificación sintética, callbacks locales; no mensaje,
IA remota, portapapeles ni aplicación externa ejecutados desde la prueba UI.
Regresiones ReplyConfirmation usan PendingIntent sintéticos sin cuentas reales.

Comando dirigido (Java17/SDK35 ya instalados):
`SPOTLIGHT_CARD_PHASE=after bash gradlew :app:testDebugUnitTest
--tests '*NotificationCardLayoutTest' --tests '*ReplyConfirmationTest'
--tests '*MessageRecipientReviewRegressionTest' :app:lintDebug
-I scripts/directed-tests.init.gradle
-PspotlightDirectedClasses=dev.bautista.spotlight.ui.NotificationCardLayoutTest,dev.bautista.spotlight.control.ReplyConfirmationTest,dev.bautista.spotlight.ui.MessageRecipientReviewRegressionTest
--no-daemon --max-workers=1 --console=plain`.
RobolectricAPI33/native, densidad1. No assemble/Actions ni dispositivo real.
`manifest.json` fija base, hashes fuente/PNG; `green` XML/log/lint.
Prueba adicional de teclado se ejecuta sola, sin repetir lint/regresiones.

Galería HTML autocontenida/PDF5páginas/ZIP:
`python3 docs/verification/visual-notification-20261106/build-gallery.py RUTA_SALIDA`.
ReportLab/DejaVuSans y Playwright existentes sólo en laboratorio; ninguna dependencia
productiva. HTMLzoom/Escape comprobados sin solicitudes externas. No medir FPS/RAM
por tiempos JVM ni afirmar OneUI/Gboard/RemoteInput reales. D5 no se adopta.

Resultado final:18casos distintos correctos (5UI+7confirmación+6homónimos),
lint0E26W1Info. `keyboard` conserva el único caso adicional; argumento
-Pkotlin.compiler.execution.strategy=in-process evita el fallo de arranque del
daemon observado en la ejecución principal (fallback automático correcto).
`test-summary.json` no suma fallos rojos ni repeticiones como casos verdes.
