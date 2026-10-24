# Preview 46 — entrega y validación local

APK y ZIP fuente se construyeron desde `4db071b8a2cb5de9e56c9a1c9668a0dcf36be6a6`; los 1.588 blobs del ZIP se cotejaron con ese árbol remoto. Las correcciones posteriores son exclusivamente de pruebas: no se recompiló la APK para ellas. El verificador comprobó que ningún archivo productivo cambió después del build.

- Paquete: `dev.bautista.spotlight.next.raycastpreview`; versionCode 46; principal sin cambios (base 29). Preview 45 conservada como referencia.
- Certificado SHA-256 histórico: `885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba`; firma V2 y `zipalign -c -P 16 4` correctos; APK no debuggable.
- APK SHA-256: `e6f5f86d34082e14e09b3731eaacf3353d4d172f35bce71936459a53e3297e82` (73.282.262 bytes).
- ZIP fuente SHA-256: `d204dea9a4b4319487327d3ccdfff5ab3a13fe7694181152016cd44ce7399cd7` (9.193.931 bytes).

Build local con JDK completo 17.0.20, Gradle 8.10.2 y SDK/build-tools 35: `:app:assembleRaycastPreview :app:compileDebugUnitTestKotlin :app:warmDirectedTestRuntime -I scripts/directed-tests.init.gradle -Pkotlin.compiler.execution.strategy=in-process --no-daemon --max-workers=1 --console=plain --stacktrace`. Resultado correcto en 8m21s, 71 tareas (53 ejecutadas/18 al día). Incluye lintVital implícito de la variante; no equivale a lint completo ni a instrumentación. Se preservó la clave local, sin publicarla ni incluirla en ZIP. El entorno perdió herramientas/cachés por mantenimiento; SDK/Gradle se recuperaron con sus checksums oficiales y el JDK con el SHA-256 del paquete oficial Ubuntu. No se cambiaron dependencias.

## Regresiones dirigidas

96 casos únicos JVM/Robolectric/Compose aprobados, sin errores/fallos/omisiones restantes. Conteos deduplicados por clase y método; no se suman repeticiones como casos nuevos.

| Grupo | Casos aprobados |
| --- | ---: |
| UI | 13 |
| D28 y comandos nativos | 34 |
| Capacidades | 8 |
| Spotify | 12 |
| Cast | 9 |
| Importaciones | 8 |
| Métricas | 5 |
| Fuentes | 7 |

Primer UI: 7/13 aprobados, 6 fallidos. Se corrigieron fixtures que apuntaban a controles retirados, se usaron rutas vigentes de menú/teclado, confirmación tras selección y espera de restauración asíncrona. Los fixtures de apps invalidan su caché y usan nombres ordenables; autocompletado depende de una app instalada real del fixture, no de ejemplos estáticos. Regresiones de 6, 2 y 1 casos: todas resueltas, sin repetir los ya aprobados ni reducir umbrales de geometría.

Primer D28: 32/33 aprobados. El botón dentro del plan restaurado necesitaba desplazamiento: se agregaron scroll, aserción de visibilidad y habilitación antes de pulsarlo. La única regresión fallida pasó; se conservaron las comprobaciones de no ejecución y confirmación. Un caso adicional verifica que un editor antiguo no sobrescribe una revisión nueva, mantiene el borrador y no altera volumen; también pasó.

Los demás grupos pasaron en su primera ejecución local. Los logs y XML de cada ronda, incluidos los primeros fallos y la corrección de compilación de pruebas, están conservados en `Spotlight-Next-Preview46-evidencia.zip`, entregado junto a APK/ZIP y un informe JSON. Detalle de hashes y rondas: `verification.json` en este directorio.

La ejecución CI #55 sigue registrada como fallida por jars runtime no cacheados en modo offline: https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/40051401930. No se reejecutó ni se lanzó otro workflow en esta sesión; las validaciones y entrega se hicieron localmente, sin depender de upload-artifact ni crear Release.

## Límites y continuidad

No hay validación física en Galaxy Tab A9+/Moto G86. Los casos Spotify y Cast prueban componentes y fixtures controlados; no acreditan autenticación/reproducción real de Spotify ni descubrimiento/transmisión a Chromecast. Se conservan Solid Graphite, paquetes, firma, datos y PR #18 en borrador. Matriz sin promoción: 9 completas / 25 implementadas con pruebas insuficientes / 6 parciales. La cobertura aumentó; no se declara completa otra decisión.

Siguiente aceptación: revisar inspector, Guardados y tamaño con teclado en Galaxy Tab A9+ usando esta Preview. Continuar los criterios de D28/D26 y la configuración real de Spotify/Cast desde el checkpoint publicado; no reconstruir Preview 46 por cambios exclusivos de pruebas o documentación.
