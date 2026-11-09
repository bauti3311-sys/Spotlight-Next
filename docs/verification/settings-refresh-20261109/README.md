# Ajustes y ancho — evidencia nativa

Abrir `Antes-despues-Ajustes.html` para comparar nueve escenas antes/después.
`Ajustes-antes-despues.pdf` contiene cuatro comparaciones representativas.
Los PNG en before/after son capturas reales Compose/Robolectric, sin retoque.
No son capturas físicas ni se generó una APK. Temas, perfiles y datos equivalentes.

`results.json` enumera40 casos correctos distintos y los XML válidos más recientes.
Se conservan también los fallos reproducidos y las rondas de corrección; no sumar
intentos como casos nuevos. Lint0 errores/23 advertencias/1 información. API33/35;
API28 no estaba disponible. One UI, Gboard y ejecución física pendientes.

El último arreglo de navegación del teléfono sólo distingue deep links explícitos
de la restauración del índice de secciones. Las capturas reutilizan el acabado
visual ya validado; no se repite render por ese cambio de estado sin delta visual.

Reproducir la galería: `python3 gallery.py`, después `python3 check_gallery.py`.
Requiere Pillow/ReportLab/Playwright ya existentes en el entorno de trabajo;
ninguna es dependencia nueva de Spotlight. `collect_results.py` comprueba XML,
archivos productivos permitidos, paths protegidos y referencias remotas.
Todos los comandos Gradle de esta tarea usan guardia no-apk y prueba dirigida.
Ver informe completo en `docs/SETTINGS-REFRESH-20261109.md` del repositorio.
