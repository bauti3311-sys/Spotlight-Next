# Continuidad integral · 17/10/2026

## Estado actual para continuar

Último commit con cambios Android: `0c2f74fbefc374ae2842ab0d14f14c40c5d3fa0a`, publicado en la misma rama aislada. El checkpoint posterior contiene únicamente cierre documental/evidencia; obtener su HEAD y la reserva remotos antes de editar, sin volver a d4 ni descartar comandos/Ajustes/conversaciones. Gitleaks sin hallazgos en el árbol y 12 commits disponibles localmente.

Este bloque pasó 493 pruebas Android/JVM, tres lint sin errores, Release/R8 sin firma y 21 comparaciones visuales sin cambios. Evidencia: `verification/conversations-20261017/summary.json`; descripción: `CONVERSACIONES-20261017.md`. La clave P1 sigue ausente; no se creó una sustituta. No ejecutar assembleFunctionalPreview para ofrecerlo como actualización P1 con otra firma. No se entrega una APK parcial y no se declara completada la matriz40.

Siguiente cambio funcional: documentos autorizados, decisión35. Punto concreto: `index/DocumentText.kt` actualmente extrae texto PDF/Office/texto e imágenes, pero un PDF escaneado devuelve un error explícito. Incorporar OCR PDF local acotado reutilizando MediaAnalysis/MLKit y las utilidades PDF existentes, con cancelación, límites de páginas/píxeles/memoria y procedencia; no enviar contenido al proveedor ni sustituir el error por texto inventado. Conservar Quick Look. Añadir pruebas dirigidas y un escenario instrumentado del OCR real; si no hay dispositivo/emulador capaz de ejecutar sus bibliotecas nativas, registrar ese límite y continuar tareas independientes sin declararlo verificado. No confundir esta mejora con embeddings o búsqueda semántica. Mantener las prioridades y pendientes restantes de la matriz.

## Recuperación comprobada

Se recuperó el árbol Git limpio de `feat/work-integral-settings-20261016` en `f49096313baa3df3b815d89b0babf80634cdd058`; coincide con el HEAD remoto. La reserva `787eaa6dea380665d68332d97e963332c0b39db3` sigue siendo de `work-gemini-native-20261014-f914c617`, con ese mismo checkpoint. No se reclamó ni liberó una reserva ajena. Se conserva el trabajo completo de Ajustes, capacidades, ejecución, documentos y comandos nativos guardados.

Sobrevivieron los informes de 461 y 470 pruebas completas y las 38 dirigidas del bloque de comandos. La última sesión informó 483 correctas, pero sus XML y el final del pipeline no sobrevivieron en `/tmp`: ese resultado no se usa como evidencia recuperada de un gate integral. Los 30 tests de scripts y 5 de backend y sus informes sí se conservaron.

## Entorno y firma

El entorno temporal, SDK, caché Gradle y clave local de P1 se perdieron. Se restauraron Gradle 8.10.2 y Microsoft JDK 17.0.16 con comprobación SHA256, SDK/Build Tools 35 y dependencias. La ejecución usa una copia de código fuera de la raíz sincronizada, como en la sesión anterior.

**No existe la clave local que firmó P1.** No generar otra clave para presentar una actualización del paquete `dev.bautista.spotlight.next.preview.p1`. Su APK anterior y su certificado público permanecen disponibles; no permiten reconstruir la clave privada. Una futura entrega necesita recuperar esa clave mediante un mecanismo autorizado o usar un paquete independiente nuevo, conservando P1 instalada. No se tocaron credenciales históricas ni la aplicación principal. No se entrega una APK parcial.

## Verificación de esta reanudación

- Gitleaks 8.30.1: sin hallazgos en el árbol actual y en los 10 commits disponibles localmente. No es auditoría del historial remoto completo.
- Comparación de archivos de referencia contra d4e3694:16 PNG anteriores idénticos; solo cambió `tablet-preferences.png`, con revisión documentada, y se añadieron 4 capturas de Ajustes. No se regrabaron barra/resultados/Quick Look para ocultar discrepancias.
- Pipeline completo correcto en 17 min 47 s: `:app:testDebugUnitTest :app:lintDebug :app:lintRelease :app:lintFunctionalPreview :app:assembleRelease`. Nuevas 483 pruebas, cero fallos/errores/omisiones; 103 suites. Tres lint sin errores, cada uno con 10 advertencias y 1 informativa heredadas. Release/R8 correcta, sin firma histórica. No se empaquetó P1 con un certificado nuevo.
- Verificación estricta posterior correcta en 22 s: `:app:verifyRoborazziDebug --tests '*VisualSnapshotTest'`. 21 referencias sin cambios; cero grabadas/añadidas/modificadas. XML completos preservados antes de filtrar los tests visuales.
- 301 entradas de código, configuración, tests y referencias de la copia compilada coinciden con el contenido Git de f490963. Release de comprobación: paquete principal conservado, 2.0.1/código 29, ARM64, min 26/target 35, no depurable,zipalign 16 KB correcto. Apksigner confirma que está sin firmar: **no es un archivo instalable ni una entrega al usuario**.

Evidencia versionada en [verification/integral-20261017/summary.json](verification/integral-20261017/summary.json): comandos, logs, manifiesto de 301 entradas, 483 casos JUnit, diagnósticos lint y 21 comparaciones visuales. El JUnit consolidado conserva suites/casos/estados; omite properties y salidas de consola duplicadas. Se registran SHA256 de los XML originales conservados fuera del árbol fuente. Los logs y resultados de la sesión anterior se distinguen de esta revalidación.

Estos resultados cierran los gates locales del código f490963; **no completan las 40 decisiones**. No declarar CI verde, instalación física ni verificación de proveedor. No hubo Actions, cambios de presupuesto, main, PR #16, force-push ni modificación de la estética aprobada de la barra.

## Bloque siguiente al primer cierre (ya realizado)

Tras publicar e768a6a se implementó la continuación estructurada de conversaciones guardadas (decisiones 15/24), reutilizando ContextStore y DirectAi. Se añadieron persistencia compatible y recorridos búsqueda→conversación guardada→revisión→HTTP fixture, comprobando cero envíos al escribir y la igualdad del historial autorizado. Código y resultados actuales están al inicio de este documento. La matriz conserva los pendientes reales; no entregar APKs parciales ni declarar completa la app por cerrar estos gates.
