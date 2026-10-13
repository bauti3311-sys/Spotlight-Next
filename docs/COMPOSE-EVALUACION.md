# Evaluación de componentes nativos — 2026-10-13

Base conservada: Kotlin 2.0.21, AGP 8.7.3, Compose BOM 2024.12.01, JDK 17, SDK 35/minSdk 26. Consultas reales a repositorios, releases y catálogos en los tags indicados. Las versiones de build upstream son evidencia de riesgo, no una prueba de mínimos del consumidor; falta resolver POM/AAR y hacer un spike antes de aprobar dependencias.

| Candidato | Versión observada / licencia | Build observado upstream | Decisión inicial |
| --- | --- | --- | --- |
| [Compose Unstyled](https://github.com/composablehorizons/compose-unstyled/releases/tag/2.10.0) | 2.10.0, MIT, publicado 2026-09-18 | Kotlin 2.4.0, Compose 1.12.0, AGP 9.1.1, compileSdk 37 | Primitivas útiles para paleta/menús; no agregar último release a la base actual sin spike. Portals/hojas deben permanecer dentro del overlay. |
| [Haze](https://github.com/chrisbanes/haze/releases/tag/2.0.1) | 2.0.1, Apache-2.0, 2026-09-29 | Kotlin 2.4.20, Compose/Jetpack 1.12.1, AGP 9.4.1 | Evaluar blur de contenido propio y fallback opaco. No confundirlo con capturar/desenfocar otras apps debajo del overlay; probar One UI. |
| [AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass/releases/tag/2.0.1) | 2.0.1, Apache-2.0, 2026-08-26 | Kotlin 2.4.10, Compose 1.12.0, AGP 9.3.2 | Spike de GPU/memoria/legibilidad antes de distorsión. No se incorporó ni se afirma rendimiento en Tab A9+. |
| [MaterialKolor](https://github.com/jordond/MaterialKolor/releases/tag/5.0.1) | 5.0.1, MIT, 2026-08-27 | Kotlin 2.4.10, Compose 1.12.0, compileSdk 37/min biblioteca 21 | Generación de paletas prometedora; evaluar una versión compatible y dependencias transitivas, sin forzar actualización del toolchain. |
| [Lumo UI](https://github.com/nomanr/lumo-ui/releases/tag/v1.2.5) | v1.2.5, Apache-2.0, 2025-04-15 | AGP 8.7.3, BOM 2024.12.01, Kotlin 2.1.0, minSdk 24 | Referencias cercanas a la base; release más antiguo y Kotlin diferente: revisar mantenimiento/metadata antes de reutilizar componentes. |
| [Vico](https://github.com/patrykandpatrick/vico/releases/tag/v3.3.1) | v3.3.1, Apache-2.0, 2026-08-28 | Kotlin 2.4.10, Compose 1.12.0, BOM 2026.08.00, AGP 9.3.2 | Elegir versión compatible para clima/estadísticas. Probar Android 10 por fix upstream de Canvas.restore y consumo durante streaming/redimensionado. |
| [Kizitonwose Calendar](https://github.com/kizitonwose/Calendar/releases/tag/2.10.1) | 2.10.1, MIT, 2026-03-28 | Kotlin 2.3.0, Compose Android 1.10.1, AGP 8.13.2 | Probar versión compatible/scroll mensual-semanal; mantener CalendarContract y permisos, no reemplazar el almacenamiento. |
| [Markdown Compose](https://github.com/mikepenz/multiplatform-markdown-renderer/releases/tag/v0.45.0) | v0.45.0, Apache-2.0, 2026-08-28 | Build con Kotlin 2.2/Java 21 declarado; módulos Compose | Candidato nativo para listas/tablas/enlaces/código. Verificar todos los formatos, streaming, accesibilidad y memoria. Sin HTML/WebView ni descarga automática de imágenes remotas. |

Repositorios no archivados al consultar. `pushed_at` reciente no garantiza soporte ni calidad; las licencias anteriores son SPDX informado por GitHub y deben conservarse junto con las licencias transitivas si se integra una versión. No se añadieron estos candidatos al APK, ni se midió todavía su rendimiento en el hardware del usuario.

Fuentes de compatibilidad: `gradle/libs.versions.toml` en cada tag de la tabla; para Markdown también `gradle.properties`. No utilizar la palabra “compatible” por similitud de nombres/SDK: ejecutar resolución, compilación y tests del proyecto.

## Experimentos de incorporación

Una biblioteca por rama/commit: resolver dependencias y licencia; medir tamaño APK y clases/transitivas; validar Enter/Tab/foco, vertical/horizontal y accesibilidad; capturar baselines; medir jank, memoria/PDF y tiempos de apertura con Perfetto/Macrobenchmark. Mantener fallback y rechazar regresiones. No afirmar porcentajes sin comparar muestras/fríos-calientes con el mismo dispositivo/API.

El laboratorio web contiene enlaces a Arc UI, EasyUI, Motion Primitives, MicroKit, Aceternity y SmoothUI; ejecuta un prototipo propio para estudiar interacción. Las implementaciones externas premium/sin licencia aún no se ejecutaron ni se copiaron. Iconos existentes Hugeicons/Phosphor conservan las licencias de los subconjuntos incluidos en assets; ampliar solo con recursos gratuitos compatibles y atribución.
