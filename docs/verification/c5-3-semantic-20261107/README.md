# C5.3 — evaluación local reproducible

Base16ef8bfddb334ec2a7dc84615923348ec6826070. No integración semántica productiva: modelo ausente, calidad semántica actual no evaluada. C5 no completo.

- `final/corpus.json`: seis documentos ficticios y doce consultas/juicios fijados. D4 reutiliza el PDF auténtico C5.2; D6 raster auténtico generado con glifos de texto OCR controlado, sin ejecutar OCR.
- `final/evaluation.json`: orden léxico real, citas/offsets/hash, resultados por consulta y métricas. Semántica con ranking/calidad null; puerta vacía no significa un modelo con recall0.
- `final/host-costs.json`: observaciones de tiempos JVM de la base léxica y de la puerta semántica vacía. No inferencia, RAM física, Android ni comparativa de costes del modelo.
- `final/ocr-raster-fixture.pdf`, `.png`: original del fixture OCR; «1O» usa O, no cero. La transcripción es un fixture declarado, no reconocimiento óptico real.
- `final/TEST-*.xml`, `final.log`: cuatro casos nuevos correctos, sin backend de embeddings simulado.
- `initial/`: primera ejecución de cuatro casos correcta, antes de alinear el raster OCR con la transcripción declarada. Conserva corpus/hash/salida; no se mezclan sus medidas con final.

Comando (Java17/SDK35, proxy/truststore existentes):

```sh
bash gradlew :app:testDebugUnitTest --tests '*C5SemanticEvaluationTest' -I scripts/directed-tests.init.gradle -PspotlightDirectedClasses=dev.bautista.spotlight.semantic.C5SemanticEvaluationTest -Pkotlin.compiler.execution.strategy=in-process --no-daemon --max-workers=1 --console=plain
```

Una sola clase, cuatro casos distintos. La segunda ronda repitió únicamente esa clase por mejora del fixture OCR, no suites históricas. No lint nuevo: producción/Gradle/deps/permisos sin cambios, lint C5.2 reutilizado (0E26W1Info). No APK, Actions, descargas de modelo ni instalación de dependencias.

Resultados: 7 aciertos documento-consulta, 5 omisiones, 0 falsos positivos; precisión micro1, recall7/12, MRR0.50 sobre10positivas. Hay12consultas en total/2negativas. Cinco consultas positivas con algún resultado. Los juicios son manuales para este corpus, no hechos universales de relevancia. Semántica auténtica NO evaluada; no se promete mejora ni se integra.

Los PDF y extracción previos en ../c5-2-provenance-20261107 se preservan. OCR/SAF/lifecycle físicos y disponibilidad del modelo en los dispositivos del propietario siguen pendientes. La evidencia histórica real 8/8 de ../semantic-evaluation-20261017 se referencia separadamente en el informe; no se reejecutó ni se presenta como resultado actual.
