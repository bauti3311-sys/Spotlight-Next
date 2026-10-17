# Vigencia de extracción manual · 17/10/2026

Continuación de f059fa9, sin cambiar Obsidian ni Quick Look. Diagnóstico sobre el flujo de producción: Extraer texto lanzaba una coroutine sin identidad ligada a la consulta/selección, de modo que una lectura tardía podía sustituir el preview y reabrir acciones del documento anterior. No se afirma reproducción física en One UI.

DocumentExtractionRequest es el propietario de un Job por solicitud en el hilo UI. Cancela la anterior al sustituirla, compara generación y contexto vivo antes de publicar texto o errores, y comprueba cancelación incluso si una tarea nativa termina con un error tardío. SpotlightPanel lo conecta mediante DisposableEffect a consulta, sección, selección, preview, menú y generación de búsqueda; destruir el overlay también dispone el propietario. Se conserva el contrato Enter/Tab/ShiftTab/Escape. El mismo control contextual permite Cancelar extracción mientras trabaja, sin añadir controles redundantes ni cambiar geometría.

DocumentText mantiene la propiedad nativa independiente: cancelar la espera no recicla prematuramente bitmaps que ML Kit todavía usa. Se comprueba cancelación antes de guardar el texto en el índice; cacheExtraction conserva sus verificaciones actuales de permisos y preferencias. No se envía nada a IA.

Seis pruebas del propietario: reemplazo y finalización fuera de orden; cambio de selección antes de disposición; cancelación explícita y error nativo tardío; error actual y recuperación; destrucción del scope padre antes de disponer el propietario; inicio sobre un scope ya destruido. Ronda dirigida final: 27 casos correctos, cero fallos/errores/omisiones, incluidos flujo SearchEngine→SpotlightPanel→IA fixture, OCR y contratos de teclado; compilación instrumentada correcta, en 1 min 32 s. Las rondas anteriores de 23/26 casos sobre cuatro/cinco tests del propietario también pasaron, pero no sustituye esta ronda final. Suite completa/lint/R8/visuales del conjunto todavía pendientes en este checkpoint.

No se ejecutó OCR instrumentado ni una prueba física de navegación/cancelación en tablet. No hay APK parcial, firma nueva, Actions, main o PR16 modificados. El cierre OCR de f059fa9 conserva sus 510 resultados y 21 referencias anteriores; no se atribuyen automáticamente a este código posterior.


## Cierre actual · cobertura y cancelación documental

Código Android 70e6f69b8a7660ae5eac987cc54450f88a3df4d3: 521 pruebas nuevas completas en110suites, cero fallos/errores/omisiones, tres lint sin errores (10advertencias+1informativa heredadas cada uno), Release/R8 y compilación instrumentada correctos en11min45s. Verificación estricta posterior:21referencias sin cambios en21s; ninguna añadida/grabada/modificada. Se cotejaron313archivos de entrada contraGit. Evidencia: docs/verification/ocr-coverage-20261017/summary.json.

La pérdida de cobertura por extracto corto se reprodujo antes (5casos/1fallo) y se corrigió con conservación por huella/página/método/texto, sin impedir cambios de fuente o correcciones OCR. Los35dirigidos pasaron; la descarga HTTP403 de su compilación instrumentada se resolvió en el reintento completo, sin cambiar dependencias. El primer pipeline fallido no se declara verde.

OCR nativo instrumentado y mediciones físicas siguen pendientes: no hay dispositivo/KVM. Los516XML recuperados son históricos, no se suman a521; Python30+5 tampoco se repitió. Sin APK parcial, clave nueva, Actions, main, PR16 o cambios de presupuesto. Próximo bloque exacto: presupuesto OCR adaptativo del índice; ver HANDOFF-INTEGRAL-20261017.md. Las40decisiones permanecen parciales.
