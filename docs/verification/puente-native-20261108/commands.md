# Comprobaciones reproducibles, sin APK

Entorno usado: Java17 instalado, SDK/caché existentes. No descargar dependencias. Las capturas de antes se tomaron con producción todavía en928705d y el nuevo harness añadido al mismo árbol, antes de editar producción; ejecutar `before` en la fuente actual no recrea la estética anterior.

Ronda final sobre la fuente0b1441e:

```sh
PUENTE_CAPTURE_PHASE=after JAVA_HOME=/workspace/jdk17/jdk-17.0.21+9 bash gradlew :app:testDebugUnitTest --tests '*PuenteNativeIntegrationTest' --tests '*PuenteFrameTest' --tests '*C2AppOpenJourneyTest' --tests '*StudioActionKeyboardTest' --tests '*FocusBIntegrationTest' --tests '*ContextSplitHeightTest' :app:lintDebug -I scripts/directed-tests.init.gradle -I docs/verification/c2-app-open-20261107/no-apk.init.gradle -PspotlightDirectedClasses=dev.bautista.spotlight.ui.PuenteNativeIntegrationTest,dev.bautista.spotlight.ui.PuenteFrameTest,dev.bautista.spotlight.ui.C2AppOpenJourneyTest,dev.bautista.spotlight.ui.StudioActionKeyboardTest,dev.bautista.spotlight.ui.FocusBIntegrationTest,dev.bautista.spotlight.ui.ContextSplitHeightTest -Pkotlin.compiler.execution.strategy=in-process --offline --no-daemon --max-workers=1 --console=plain
```

La sesión usó también JAVA_TOOL_OPTIONS con el proxy/CA ya configurado; no contiene credenciales. Los recursos Robolectric eran locales. Guardia no-apk impide empaquetar/firmar/instalar; el task de unit tests compila clases y recursos JVM, no una APK.

Para reproducir sólo la frontera fallida enec8b14a antes de corregirla:

```sh
bash gradlew :app:testDebugUnitTest --tests '*PuenteFrameTest.detachedPanesKeepMinimumWidthsAtTheSplitThreshold' -I scripts/directed-tests.init.gradle -I docs/verification/c2-app-open-20261107/no-apk.init.gradle -PspotlightDirectedClasses=dev.bautista.spotlight.ui.PuenteFrameTest -Pkotlin.compiler.execution.strategy=in-process --offline --no-daemon --max-workers=1 --console=plain
```

El caso nuevo está en0b1441e; para un árbol anterior hace falta copiar solamente ese harness sin sobrescribir producción. Las fallas registradas en split-before-fix prueban el problema; los XML finales prueban el arreglo. Crear árboles/capturas históricas requiere verificar protocolo/reserva primero; estos comandos no cambian refs remotos ni autorizan Actions.
