# C5.2 — evidencia dirigida

Fuente técnica: `84c0e1a668c16becc0cc5098d20da22dc4644716`.

Resultado final: 14 casos nuevos correctos +1 regresión histórica; lint0E26W1Info.

Base b80d9762eefef2e06bff32a7b6d97f51c2d7f94c; C5.1 fuente844e23d intacta. Este checkpoint no termina C5.

## Procedencia del PDF

- `pdf/real-three-pages.pdf`: PDF auténtico generado por `CitationRealPdfTest.pdf()` con PDFBox ya existente, ID fijo y Helvetica/WinAnsi. Contenido ficticio. No se modificó DocumentText/PdfTextSource/DocumentExtraction para la prueba.
- `pdf/extraction.json`: salida real de DocumentText, incluido texto con encabezados y metadatos de página/offsets del extractor.
- `pdf/pdfinfo.txt`, `poppler-pages.txt`, `independent-verification.json`: comprobación independiente del mismo PDF mediante Poppler. Las tres páginas coinciden literalmente con cada fragmento real de extracción.
- `pdf/image-only.pdf`: PDF auténtico con raster incrustado, sin capa de texto. `image-only-text.txt` vacío (salvo separador de página) en Poppler. Ruta de extracción con OCR expresamente deshabilitado; no se afirma OCR real.
- La primera clase ejecutó cuatro casos correctos: tres páginas/Unicode/repetidos, extracto ambiguo, procedencia incompleta y raster sin OCR. La evidencia sigue válida: las correcciones posteriores afectan al responsable de estado y la emisión de clear(), sin modificar extractor ni DocumentCitationSession.

## SAF y estado

DocumentsProvider real como clase Android, pero alojado en Robolectric API33. Lecturas ContentResolver/openDocument/PFD reales del fixture local; permisos persistidos de Robolectric. Índice sembrado con APIs existentes y propiedad SAF asignada sólo en el fixture, sin probar selector/indexación completa. No usar este test como prueba instrumentada, del sistema de grants/Binder real ni de un proveedor instalado.

Estado efímero sin persistencia. Configuración/revisión del índice y evento explícito del anfitrión invalidan; apertura/copia/mostrar sólo por puerta de revalidación. SAF no garantiza notificar revocaciones externas: lifecycle/acceso deben conectarse antes de integrar UI. Los cambios de archivo sin notificación sólo pueden detectarse al revalidar.

`initial/`: cuatro PDF correctos; siete fallos de inicialización del fixture SAF (context de apply era el proveedor sin inicializar). No producción.

`before-clear-fix/`: fixture corregido, seis casos SAF correctos y un rojo real al borrar índice sin revisión. `FileIndex.clear` añade una sola emisión atómica.

`before-cancel-fix/`: un caso nuevo rojo de cancelación de acción mientras el proveedor sigue bloqueado. Corregido en el responsable con tarea hermana cancelable.

`final/`: última evidencia correcta de cada clase. Los logs intermedios se conservan para distinguir qué se ejecutó en cada ronda; no se suman ejecuciones repetidas como casos nuevos. Quedan 14 casos nuevos distintos (4 PDF +10 SAF/estado) y una regresión C5.1 relacionada con privacidad/borrado. Los otros diez casos históricos se conservan por referencia en ../c5-citations-20261107 sin repetirlos.

## Comandos

Java17, Android SDK35, mismo proxy/truststore del entorno anterior. No dependencias nuevas, Actions, APK ni instrumentación.

```sh
bash gradlew :app:testDebugUnitTest --tests '*CitationRealPdfTest' --tests '*CitationSafStateTest' -I scripts/directed-tests.init.gradle -PspotlightDirectedClasses=dev.bautista.spotlight.index.CitationRealPdfTest,dev.bautista.spotlight.index.CitationSafStateTest -Pkotlin.compiler.execution.strategy=in-process --no-daemon --max-workers=1 --console=plain
```

Tras corregir la inicialización del fixture y clear(), se ejecutaron sólo SAF y la regresión:

```sh
bash gradlew :app:testDebugUnitTest --tests '*CitationSafStateTest' --tests '*DocumentCitationsTest.privacyChangeAndIndexDeletionInvalidateReferences' :app:lintDebug -I scripts/directed-tests.init.gradle -PspotlightDirectedClasses=dev.bautista.spotlight.index.CitationSafStateTest,dev.bautista.spotlight.index.DocumentCitationsTest -Pkotlin.compiler.execution.strategy=in-process --no-daemon --max-workers=1 --console=plain
```

El rojo nuevo de cancelación se ejecutó solo. Después se verificó nuevamente su clase afectada y lint:

```sh
bash gradlew :app:testDebugUnitTest --tests '*CitationSafStateTest' :app:lintDebug -I scripts/directed-tests.init.gradle -PspotlightDirectedClasses=dev.bautista.spotlight.index.CitationSafStateTest -Pkotlin.compiler.execution.strategy=in-process --no-daemon --max-workers=1 --console=plain
```

```sh
pdfinfo pdf/real-three-pages.pdf
pdftotext -layout pdf/real-three-pages.pdf pdf/poppler-pages.txt
pdftotext pdf/image-only.pdf pdf/image-only-text.txt
```

Pendiente Android real: permiso/grant DocumentsUI, Binder/proveedores, lectura nativa bloqueante en stream, OCR, latencia/memoria y eventos de lifecycle/revocación. No se integra todavía la barra ni la vía semántica C5.3.
