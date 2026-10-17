# Cobertura documental conservada · 17/10/2026

Continuación de 5bade7a: el OCR y su cancelación permanecen conectados; no cambia Obsidian, Ajustes, Quick Look, conversaciones, firma ni permisos.

Se reprodujo con SQLite real que cacheExtraction reemplazaba un índice largo por el extracto manual limitado del mismo PDF. Sobre producción 5bade7a más DocumentCacheCoverageTest: 5 casos, un fallo esperado en shorterManualExcerptOfSameBytesDoesNotEraseIndexedPages (esperaba una coincidencia, obtenía cero). Se conservaron entradas y XML de esa ejecución antes de modificar la copia compilada.

Ahora se conserva el texto anterior únicamente si hay huella SHA256 idéntica, versión de metadatos reconocida, mismas páginas, al menos igual cobertura examinada, y cada fragmento entrante es prefijo real de un fragmento almacenado de la misma página/método. No se decide solo por longitud. Un archivo con bytes distintos, OCR corregido o cobertura nueva sigue actualizando el índice. Metadatos inválidos o legados sin huella no bloquean la actualización autorizada. No es una fusión semántica de resultados incomparables.

La comprobación y escritura son una transacción SQLite. Se consultan exclusivamente filas de fuentes todavía autorizadas; modo privado y preferencias se verifican antes. Conservar una cobertura equivalente no incrementa la revisión ni reinicia la búsqueda. No hay importaciones automáticas ni envíos de contenido.

Después de corregir: 35 pruebas dirigidas correctas, cero fallos/errores/omisiones (caché5, OCR9, propietario de extracción6, producción universal10, índice2, recuperación documental3). La compilación instrumentada del mismo pipeline no llegó a compilar: resolución de dependencias recibió HTTP403 del repositorio. Una comprobación posterior del mismo URL oficial respondió200; se hará un reintento acotado sin sustituir versiones ni repositorios. No presentar ese pipeline como íntegramente verde. Los 516 resultados completos de 5bade7a sobrevivieron al corte anterior, pero su pipeline lint/R8 no terminó de forma verificable; no se atribuyen automáticamente a este código posterior.

El entorno temporal anterior se perdió. Se restauraron JDK17/Gradle8.10.2/SDK35 con el procedimiento ya documentado. Los nuevos controles guardan logs desde su inicio y copian resultados al terminar cada tarea mediante un init script de observación, sin cambiar acciones ni aserciones del build. OCR instrumentado real y One UI siguen pendientes por falta de dispositivo/KVM. No se creó firma, APK parcial ni ejecución de Actions. Gate integral y visual de este código pendiente en este checkpoint.


## Cierre actual · cobertura y cancelación documental

Código Android 70e6f69b8a7660ae5eac987cc54450f88a3df4d3: 521 pruebas nuevas completas en110suites, cero fallos/errores/omisiones, tres lint sin errores (10advertencias+1informativa heredadas cada uno), Release/R8 y compilación instrumentada correctos en11min45s. Verificación estricta posterior:21referencias sin cambios en21s; ninguna añadida/grabada/modificada. Se cotejaron313archivos de entrada contraGit. Evidencia: docs/verification/ocr-coverage-20261017/summary.json.

La pérdida de cobertura por extracto corto se reprodujo antes (5casos/1fallo) y se corrigió con conservación por huella/página/método/texto, sin impedir cambios de fuente o correcciones OCR. Los35dirigidos pasaron; la descarga HTTP403 de su compilación instrumentada se resolvió en el reintento completo, sin cambiar dependencias. El primer pipeline fallido no se declara verde.

OCR nativo instrumentado y mediciones físicas siguen pendientes: no hay dispositivo/KVM. Los516XML recuperados son históricos, no se suman a521; Python30+5 tampoco se repitió. Sin APK parcial, clave nueva, Actions, main, PR16 o cambios de presupuesto. Próximo bloque exacto: presupuesto OCR adaptativo del índice; ver HANDOFF-INTEGRAL-20261017.md. Las40decisiones permanecen parciales.
