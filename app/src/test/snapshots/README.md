# Baselines visuales Android

Roborazzi 1.29.0 se evalúa contra Kotlin 2.0.21/Compose BOM 2024.12.01, evitando instalar la última versión construida con otro toolchain. Solo dependencias test y plugin de desarrollo; sin dependencia visual de producción.

Registrar: `bash gradlew :app:recordRoborazziDebug --tests '*VisualSnapshotTest'`. Verificar: `bash gradlew :app:verifyRoborazziDebug --tests '*VisualSnapshotTest'`. Comparar: `:app:compareRoborazziDebug`. Los diffs/reportes permanecen en app/build/outputs/roborazzi. Revisar cambios de baselines antes de incorporarlos; CI verifica, nunca registra para ocultar fallos.

Fixtures: SDK 33, gráficos nativos, tema oscuro, consultas/pins sintéticos, sin APIs remotas. Capturas neutrales sin cursor parpadeante: foco real se afirma antes de limpiar el foco únicamente para captura. Los contratos de teclado/overlay siguen en sus tests funcionales.

Matriz inicial: barra, resultados, selección, panel contextual, menú, Guardados, Apariencia y error PDF; teléfono y horizontal. Las páginas PDF reales y la carga/renderización se verifican en dispositivo: Robolectric 4.14.1 no ofrece shadow de PdfRenderer. Preparar capturas de PDF renderizado/carga en el flujo instrumentado/Maestro, sin fingir una vista nativa en JVM.

F1.1 añade dos referencias del control de ancho (`width-*-active-override.png`), revisadas en teléfono y tablet. `OverlayWidthVisualSnapshotTest` comparte el filtro `*VisualSnapshotTest`, por lo que CI verifica las doce referencias. Los diez PNG originales permanecen intactos.

F1.2 añade cinco referencias `appearance-*.png`: filas reales compactas/espaciosas con igual ancho, tablet vertical y teléfono en ambas orientaciones. Fixtures equilibradas explícitas; el automático antiguo se prueba sin migrarlo. CI verifica 17 referencias; las doce anteriores siguen intactas. Antes/después/diff del árbol base real: `docs/visual/f1.2`.

## Ajustes integral · 16/10/2026

La autorización nueva del usuario permite renovar únicamente Ajustes y sus componentes. SettingsVisualSnapshotTest agrega cuatro referencias de MainActivity: Obsidian/Graphite/macOS Light en tablet y Obsidian en teléfono. `tablet-preferences.png` se actualiza tras revisar la eliminación de temas duplicados y controles recortados. Las otras16referencias anteriores se conservan byte por byte. Total21; el filtro existente `*VisualSnapshotTest` las incluye sin cambiar CI ni tolerancias.

Registro dirigido usado: `:app:recordRoborazziDebug --tests '*SettingsVisualSnapshotTest' --tests '*VisualSnapshotTest.preferences'`. No registrar las demás para ocultar regresiones. Revisión y hashes en `docs/visual/integral-settings/review.json`; verificar posteriormente las21 con `:app:verifyRoborazziDebug --tests '*VisualSnapshotTest'`. Capturas Compose con gráficos nativos/API33 bajo Robolectric, no dispositivo físico One UI. Las nuevas referencias muestran la actividad de Ajustes, no cambian la barra o Quick Look.
