# C5.4 — preparación comprobada, hardware no ejecutado

Base d712a14cb8e924f958fdf2526b1defd7e925e61d. Los datos C5.3 aceptados se mantienen: 7 aciertos/12 pares relevantes, 5 omisiones, 0FP. Sin evaluación semántica nueva.

**Ejecutado:** dry-run de tareas, compilación Kotlin/Java de AndroidTest, revisión del manifiesto fusionado, integridad de fixtures, grafo sin empaquetado/firma/instalación/instrumentación, inventario APK antes/después y comprobación de producción idéntica a base.

**No ejecutado:** ninguno de los cinco métodos Android instrumentados. adb-devices.txt no contiene dispositivos. No hay APK/test APK, instalación, firma, Actions, descarga de modelos, métricas físicas ni corpus semántico auténtico nuevo.

El provider Java sólo vive en test APK; utiliza APIs SDK para funcionar en su propio proceso sin depender del runtime Kotlin deduplicado del target. Sus controles requieren grant al árbol ficticio. No concede permisos nuevos a la aplicación.

## Compilación sin APK (ejecutada)

~~~sh
bash gradlew :app:compileDebugAndroidTestKotlin :app:compileDebugAndroidTestJavaWithJavac --dry-run --no-daemon --max-workers=1 --console=plain
# Después de verificar el grafo:
bash gradlew :app:compileDebugAndroidTestKotlin :app:compileDebugAndroidTestJavaWithJavac --no-daemon --max-workers=1 --console=plain
~~~

Java17, SDK35 y configuración proxy/truststore existente. packageDebugResources sólo prepara recursos intermedios, no un APK instalable. No se ejecutaron packageDebug, packageDebugAndroidTest, assemble, validateSigning, install o connected.

- compile-dry-run.log: grafo revisado.
- initial/compile.log: error inicial de test por use aplicado a un renderer sin Closeable; corregido sólo en test.
- compile-first-success.log / compile-sealed.log: compilaciones correctas intermedias del banco mientras se reforzaban controles.
- compile-final-guarded.log: código final compilado, BUILD SUCCESSFUL en13s.
- apks-before.json: inventario previo vacío. Sigue vacío después.
- preparation-checks.json: controles estáticos correctos; NO una ejecución de tests físicos.
- verify-preparation.py: verificador de sólo lectura, sin Gradle, adb/instrumentación, instalaciones o red.

Reproducir los controles desde el repo después de la compilación, utilizando el inventario previo de esa preparación:

~~~sh
python3 docs/verification/c5-4-physical-preparation-20261107/verify-preparation.py --compile-log docs/verification/c5-4-physical-preparation-20261107/compile-final-guarded.log --apk-inventory-before docs/verification/c5-4-physical-preparation-20261107/apks-before.json
~~~

La compilación no garantiza carga de JNI, comportamiento de DocumentsUI/proveedor, OCR, gestos o lifecycle real. No se repiten C5.1/C5.2/C5.3 ni suites históricas. Lint C5.2 se conserva por referencia para producción idéntica; no es un lint nuevo de los tests.

Protocolo y guardas de target/firma/consentimiento en ../../C5-4-PHYSICAL-PROTOCOL-20261107.md. Ficha vacía en ../../C5-4-PHYSICAL-ACCEPTANCE-TEMPLATE.md. Preview67 no contiene C5; este banco compilado es DebugAndroidTest y requiere target compatible autorizado. No ejecutar contra una aplicación personal ni reemplazarla.
