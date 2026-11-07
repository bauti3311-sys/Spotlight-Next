# Focus Studio — correcciones a partir de Preview 67 física

Base remota verificada: `9d652771f75be1f0fc9f1d87fa7681f386e5c05c`, PR #21 abierto/en borrador. Fuente de corrección: `2731897c80b2d87440a58d9f9b6d08ef1710723e`. Preview 67 instalada no contiene estos cambios.

## Evidencia y alcance

Las tres capturas aportadas en la conversación muestran texto de ChatGPT atravesando resultados/formulario, ActionPanel desbordando visualmente a la derecha y abajo, y controles Spotify apilados con espacio vertical innecesario. Las imágenes personales no se incorporan al repositorio. No se deducen medidas físicas ni FPS de esas fotos.

- **Legibilidad:** el vidrio exterior conserva preferencias/materiales. Sólo Focus añade respaldo interno de superficie al 98 % en resultados/formularios y footer contextual. Sin blur, dependencias ni capas de animación nuevas. La ruta anterior conserva su transparencia.
- **ActionPanel:** Focus elimina el desplazamiento lateral fuera del padre; limita la altura a `min(límite existente, área visible segura, max(altura padre, 320 dp))`. Conserva margen de 8 dp respecto al marco visible. El contenido sigue desplazable y todas las acciones permanecen disponibles. Se mantienen token, flags, comportamiento IME y navegación del host nativo; la colocación se recalcula al cambiar el marco. La ruta anterior permanece igual.
- **Formulario Spotify:** solamente presentación: padding interior y filtros/botón en una fila cuando caben; se envuelven en teléfono/texto ampliado. Mismos filtros, consultas, callbacks, autorización, cancelación y ejecución. El estado del footer puede ceder ancho para mantener legible la acción Buscar.

Sin cambios en C5, motores/ranking, configuración de app, firma, paquete, versión, manifiesto de entrega, Ajustes A, Geist, radios D1 o apertura de 140 ms. Sin Actions ni APK.

## Comparación reproducible

Abrir [galería antes/después](verification/studio-physical-polish-20261107/gallery.html). Son 24 pares PNG originales de Compose/Robolectric: tres escenas (resultados, acciones, formulario), tablet 1000×720 y teléfono 360×780, claro/oscuro y texto 100/200 %. Datos de prueba iguales, fondo tipográfico ficticio exigente y opacidad configurada de .70. Iconos de fallback del PackageManager; ninguna cuenta real ni llamada Spotify.

El host nativo se mide contra marcos visibles controlados; ActionPanelView real se captura en esa posición. Esto no es una captura de WindowManager en One UI ni evidencia física de IME. La revisión inicial detectó un footer aún translúcido y la revisión de teléfono 200 % detectó el botón partido: se conservaron ambas evidencias intermedias.

En tablet, mismo formulario autorizado vacío: **131 → 91 dp** (100 %) y **152 → 112 dp** (200 %). El menú de la escena pasa de `(640,120–1000,696)` a `(550,120–910,440)`; el padre termina en x=910. Son medidas del banco mdpi, no de la foto. Ningún recorte elimina acciones; la última se alcanza por scroll/Tab.

## Validación dirigida

- Pruebas dirigidas: 19/19 casos distintos aprobados (10 nuevos y 9 regresiones relacionadas), sin omisiones. Incluyen geometría en marcos IME/rotación controlados, scroll/Tab/Enter/Escape, filtros táctiles y teclado, reversibilidad y respaldo de contenido en ambos temas. Las variaciones de color por fondo negro/blanco son 0–1 niveles RGB en Focus y 10 en la ruta anterior; son mediciones de composición, no contraste WCAG ni fotometría física.
- **Lint global NO aprobado:** 1 error, 26 advertencias y 1 informativo. El error está en `app/src/androidTest/java/dev/bautista/spotlight/c5/C5FocusHostDeviceTest.kt:65`, `StateFlowValueCalledInComposition`, presente idéntico en HEAD base. No se modifica C5, se suprime el diagnóstico ni se crea baseline. Evidencia completa en `verification/studio-physical-polish-20261107/lint/`. No se declara verde el proyecto completo.

Comando de pruebas reproducible (Java 17/SDK configurados; no genera APK):

```sh
bash gradlew :app:testDebugUnitTest \
  --tests '*StudioPhysicalPolishTest' --tests '*StudioPopupBudgetTest' \
  --tests '*StudioPolishContractTest' --tests '*StudioActionKeyboardTest' \
  --tests '*ActionPopupHostTest' --tests '*SpotifyFilterGeometryTest' \
  -I scripts/directed-tests.init.gradle \
  -PspotlightDirectedClasses=dev.bautista.spotlight.ui.StudioPhysicalPolishTest,dev.bautista.spotlight.overlay.StudioPopupBudgetTest,dev.bautista.spotlight.ui.StudioPolishContractTest,dev.bautista.spotlight.ui.StudioActionKeyboardTest,dev.bautista.spotlight.overlay.ActionPopupHostTest,dev.bautista.spotlight.ui.SpotifyFilterGeometryTest \
  -Pkotlin.compiler.execution.strategy=in-process --no-daemon --max-workers=1 --console=plain
```

Se ejecutaron por tandas documentadas: la última sólo repite las dos clases afectadas por el arreglo posterior del footer. Los XML finales conservan el último resultado de cada clase, sin sumar reintentos como pruebas distintas. `:app:lintDebug` es el comando global que falló; `:app:lintAnalyzeDebug` se completó correctamente después del último arreglo para analizar nuevamente el código productivo, sin alterar el fallo global ya registrado.

Se conservan logs de intentos fallidos del banco (selector ambiguo, opt-in de API, PixelCopy no disponible, medición inicial de altura visual en lugar del área táctil y espera de composición tras autorización ficticia). No se presentan como defectos productivos ni como pruebas aprobadas. Cinco aserciones productivas fallaron antes del arreglo: altura de formulario, tres límites/posición Focus y botón Buscar de tres líneas. La ruta de menú anterior pasó sin cambios. Las regresiones previas sólo se repiten cuando el cambio posterior afecta su alcance.

## Pendiente físico

El fixture espera explícitamente que los cuatro filtros estén visibles tras cambiar la autorización de prueba, antes de afirmar visibilidad; conserva las mismas aserciones y no amplía límites de ejecución productivos.

En Galaxy Tab A9+ y Motorola G86, después de una entrega expresamente autorizada: comprobar legibilidad sobre otra aplicación, menú con todas sus acciones/scroll, IME abierto/cerrado, rotación y ventana flotante, texto 200 %, Tab/Enter/Escape y formularios. Confirmar fluidez y ausencia de saltos; no hay mediciones reales de RAM/FPS en este checkpoint. Las nueve V no se declaran cerradas.

C5.3 mantiene 7/12 aciertos léxicos, cinco omisiones y ninguna evaluación semántica auténtica; banco C5.4 y su NO-GO de instalación se conservan. No se reejecutan ni se integran en UI.
