# Evidencia C2/F01

- `before/`: seis pruebas contra producción base bfa07e7; falta el recibo del inspector fijado (1 fallo/5 correctos).
- `after/`: cinco XML, 23 casos distintos verdes (6 nuevos + 17 existentes).
- `preservation.json`: alcance del delta y comprobación de archivos preservados.
- `initial-fixture/`: ejecución inicial con tres suposiciones incorrectas de la prueba: apertura sin fijar cierra overlay, Enter sobre Volver cancela. No son defectos productivos.
- `after-fixture-assertion/`: ejecución de 23 casos con 22 verdes; el restante ya mostraba el recibo correcto, pero assertTextContains exigía texto completo. Se corrigió sólo la aserción a assertTextEquals multilínea y se repitieron únicamente los seis nuevos casos.

Los 17 casos existentes verdes de esa ejecución se conservan sin repetirlos.
Lint se ejecuta tras la última corrección de la aserción. Los logs preservan los
intentos fallidos; no se presentan fallos de fixtures como errores de producto.
Son pruebas JVM/Compose sobre una aplicación Android ficticia. No se prueba
reproducción, integración remota ni finalización real de aplicaciones externas.

Comando final (Java 17, SDK local):

```sh
bash gradlew :app:testDebugUnitTest --tests '*C2AppOpenJourneyTest' :app:lintDebug \
  -I scripts/directed-tests.init.gradle \
  -I /workspace/scratch/c5-lint-fix/no-apk.init.gradle \
  -PspotlightDirectedClasses=dev.bautista.spotlight.ui.C2AppOpenJourneyTest \
  -Pkotlin.compiler.execution.strategy=in-process --no-daemon --max-workers=1 --console=plain
```

El init adicional impide ensamblado, empaquetado, firma, instalación e instrumentación;
su copia se conserva aquí para reproducirlo sin depender del scratch original.
