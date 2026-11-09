# Ajustes renovados y ancho efectivo — 09/11/2026

Solicitud del propietario: reparar el ancho que no responde, rediseñar Ajustes y
quitar configuraciones inservibles. Esta autorización reemplaza la conservación
estética del cuerpo A de Ajustes; no autoriza otra APK, Actions ni nuevas funciones.
Base remota comprobada `42ac439936dd7e47ac906af3719eb273eef6b20f`, PR21 OPEN/DRAFT.
Reserva nueva propia; main y fuente aislada de Preview70 se conservan.

## Tres fallos reproducidos antes de editar producción

[XML anteriores](verification/settings-refresh-20261109/before/xml):

1. Cambiar `windowScale` se persistía y llegaba al servicio, pero no activaba
   `refreshWindowGeometry()`. WindowManager seguía en704px en el escenario de prueba.
2. Puente imponía560dp al buscador sin inspector aun después de elegir ancho manual.
   Cambiaban las dimensiones de la ventana Android, pero no la superficie visible.
3. En un teléfono de412dp, el mínimo380dp de WindowSizePolicy reducía TODO el slider
   al mismo ancho380dp. Era un control accesible/persistente pero sin efecto visual.

Se extiende la observación geométrica a windowScale/adaptive; se conserva el máximo
compacto de Puente sólo mientras no haya elección manual; se distribuye el slider
por el intervalo efectivo disponible, manteniendo el tope1100dp. El mínimo manual de
un teléfono pasa a300dp cuando cabe. No se migran preferencias heredadas: con
windowScale=-1 se conservan las reglas anteriores. «Restaurar ancho anterior» sólo
restaura ese selector, sin borrar width/landscapeWidth/position guardados.

La curva manual de tablet interpola sus límites sin una zona superior saturada.
Un valor normalizado guardado conserva su posición relativa; su ancho resultante
puede variar respecto de la fórmula anterior. El valor mostrado en Ajustes es una
estimación para su pantalla, no una medición independiente del WindowManager OEM.
En330dp se explica que el margen de ajuste es pequeño para conservar legibilidad.

## Diseño implementado, no otro catálogo conceptual

- **Tablet:** navegación lateral persistente, búsqueda arriba y cuerpo a todo el
  ancho útil; se retira el máximo global960dp de Ajustes. Paleta Puente reutilizada
  cuando Focus está activo, mismos temas/Geist y sin nuevo motor visual.
- **Teléfono:** entrada con secciones legibles y acceso directo. Botón «Secciones»,
  Volver y Escape permiten regresar; búsqueda cruza las secciones. Estado y scroll
  por sección conservados con el mismo SaveableStateHolder y claves de composición.
- **Apariencia:** ancho primero, valor estimado, tema, interfaz reversible, acento y
  reducción de movimiento. Texto, espaciado, tipografía y transparencias desplegables.
- **Inicio:** tamaño/apariencia, fuentes, privacidad y teclado. Se elimina la
  promoción de conectar IA como primer paso; su configuración sigue disponible.
- **Avanzado:** proveedores, conectores heredados, mensajería predeterminada,
  respaldo, diagnóstico y Acerca de Spotlight. No se borran claves ni configuraciones.
- Versión y atribución Icons8 se trasladan a Acerca; dejan de reservar altura en
  todas las secciones, especialmente costosa con texto200%.

### Qué se quitó y qué se conservó

Se ocultan sólo de Ajustes los bloques informativos «Bienvenido», «Herramientas
incluidas» y los botones de clima/deportes: no son preferencias y la barra conserva
sus capacidades. Las exclusiones previas de Tasker/rutinas permanecen iguales.
Se retira del recorrido Puente el slider de opacidad del buscador: Puente usa
respaldo legible98%, por lo que ese slider no modificaba esa superficie. Su valor
se conserva para la interfaz anterior, donde sigue disponible en detalles.

Los controles útiles de escala, densidad, menú de acciones y opacidad de Ajustes
siguen disponibles, no se eliminan por parecer técnicos. Permisos, revocación,
modo privado, limpieza de datos y confirmaciones destructivas quedan accesibles.
No se otorgan accesos al visitar Ajustes ni se activan integraciones por defecto.

## Evidencia y comprobaciones

[Galería antes/después](verification/settings-refresh-20261109/Antes-despues-Ajustes.html),
[PDF](verification/settings-refresh-20261109/Ajustes-antes-despues.pdf), originales
PNG en `before`/`after`. Capturas reales Compose/Robolectric API33, no renders web,
APK instalada ni One UI. Mismos perfiles/datos/temas en cada comparación; vistas
1280×900,390×844 y330×800 con200% de texto. Escenas de ancho mínimo/máximo iguales.

Las rondas y sus XML se conservan sin esconder fallos. La primera compilación del
harness tuvo un import/extensión faltante, corregido sin producción; una aserción
de scroll en la escena anterior330dp/200% quedó en un bucle de idling y se detuvo
sólo ese proceso de tests. Las capturas anteriores ya obtenidas son válidas; no se
presenta aquella ronda interrumpida como exitosa. La reproducción final anterior
contiene tres fallos productivos y una captura correcta.

Se detectaron después una etiqueta «Compacta» duplicada, un selector de navegación
histórico renombrado y una aserción incorrecta que simulaba IME sin un evento real:
se corrige la etiqueta, se usan IDs de sección y la prueba de ventana verifica
ancho ida/vuelta sobre el mismo host. La retención IME se comprueba en su banco
existente de eventos/geometría. El runtime Android28 de Robolectric no estaba en caché y no pudo
resolverse; se usa explícitamente la caché API33/35, sin descargarlo ni afirmar cobertura28.

La prueba nueva de Escape y el cambio rápido de sección detectaron problemas del
nuevo contenedor: se conserva foco explícito dentro de Ajustes y se aísla cada
composición de sección sin perder su estado guardable. Evidencia de la ronda fallida
se conserva. La distribución de los tres temas evita desbordes de redondeo de FlowRow.
Una regresión adicional de deep link en teléfono se reproduce aisladamente: al
recibir una sección por Intent mientras se mostraba el índice, éste no se cerraba.
Se distingue ahora un cambio explícito de sección de la restauración del índice;
la prueba cubre Intent, Escape y recreación sin perder esa pantalla.

Consultar `verification/settings-refresh-20261109/results.json` para resultados
finales, clases y severidades lint. Se reutilizan las regresiones de ancho,
persistencia/respaldos, Puente, permisos guiados y altura con IME; no suites históricas
de búsqueda, Spotify o C5. Guardia no-apk en todos los comandos. Sin supresiones,
baselines ni actualizaciones automáticas de imágenes esperadas.

## Límites y siguiente entrega

No se generó APK/testAPK, se firmó, se instaló ni se ejecutó Actions. Preview70 ya
entregada no cambia; estos commits todavía no están en el dispositivo. Su permiso
está consumido. Una entrega posterior necesita autorización nueva y conservar el
procedimiento de fuente aislada para no incorporar C5 del PR.

Pendiente físico: ancho visible vivo con One UI/Gboard, rotación, ventana flotante,
retorno de foco/teclado y legibilidad con configuración real. Las pruebas JVM no
cierran C2, C5 ni Visual First. No ranking, ejecución de apps, Spotify, Square,
WhatsApp, documentos, permisos nuevos, dependencias, firmas, paquetes ni versiones.


## Checkpoints y reproducción

Corrección de ancho: `90285c35871450fe3c5061c7f9fadd3d81c66820`.
La renovación de Ajustes y este informe forman el checkpoint siguiente; ambos se
publican juntos por avance normal del PR, sin alterar ninguna fuente de entrega.

Los logs muestran los comandos Gradle y la guardia de tareas. Se usa Java17 en
`/workspace/jdk17/jdk-17.0.21+9`, `--offline --no-daemon --max-workers=1`, init de
`scripts/directed-tests.init.gradle` y `docs/verification/c2-app-open-20261107/no-apk.init.gradle`.
`JAVA_TOOL_OPTIONS=-Drobolectric.enabledSdks=33,35` limita a runtimes disponibles;
no es una dependencia de producto ni una supresión de pruebas fallidas.
No sumar por rutina todos los intentos: results.json selecciona el XML correcto
más reciente de cada clase y registra40 casos distintos, no rondas duplicadas.

Galería reproducible: `gallery.py`, con18 originales PNG; `check_gallery.py`
comprueba los nueve pares, decodificación de imágenes, adaptación390px y ausencia
de red. La carga de verificación usa set_content del HTML exacto; no navegación
file:// independiente. PDF4p y ZIP se generan de los originales, sin reconstruir UI.
