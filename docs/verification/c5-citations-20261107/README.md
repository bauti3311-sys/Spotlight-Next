# Evidencia C5 — 2026-11-07

Base:293290a30d739f2e4495a3560f59e56618f96d1a / PR21. Checkpoint parcial, no aceptación física de C5.

- `initial/`: primera ejecución de ocho casos y lint.
- `final/`: once casos correctos (10 dominio +1 Compose), ejecución sellada posterior al renombrado del fixture OCR a .pdf, y lint sin errores/26W/1Info.
- `images/`: originales Compose Roborazzi, ambos temas, misma escena.
- `gallery.html`: propuesta de componente, no integración productiva.
- XML de dominio contiene tres líneas CORPUS reproducibles. Bytes PDF/OCR son fixtures de contrato, no PDF/OCR reales.

Comando de validación (Java17, SDK35, proxy/truststore del entorno):

```sh
bash gradlew :app:testDebugUnitTest --tests '*DocumentCitationsTest' --tests '*C5CitationCardReviewTest' :app:lintDebug -I scripts/directed-tests.init.gradle -PspotlightDirectedClasses=dev.bautista.spotlight.index.DocumentCitationsTest,dev.bautista.spotlight.ui.C5CitationCardReviewTest -Pkotlin.compiler.execution.strategy=in-process --no-daemon --max-workers=1 --console=plain
```

El run sellado repitió sólo las dos clases dirigidas tras corregir la extensión del fixture; lint ya válido sin cambios productivos posteriores. No Actions, APK, pruebas históricas ni servicios. Offline significa configuración online=false y ruta sin llamadas de red; no simulación de proveedor físico ni modo avión instrumentado.

Ver contrato y limitaciones en ../../C5-CITATIONS-CHECKPOINT-20261107.md.
