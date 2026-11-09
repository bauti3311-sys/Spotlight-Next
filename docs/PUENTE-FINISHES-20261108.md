# Puente: paletas, tipografías, espaciado y superficies

El propietario señaló interés en Puente y pidió sus acabados. No es aprobación de integración productiva. Base auditada: `4b2b9f8da6e99a1cfd71f9633e3d70d8aec92383`, PR21 abierto/en borrador, reserva propia nueva.

[Galería offline](verification/puente-finishes-20261108/Galeria-Puente.html) · [PDF](verification/puente-finishes-20261108/Puente-Acabados.pdf) · [Muestras](verification/puente-finishes-20261108/samples.json).

## Elegir piezas sin confundir sus efectos

- C01–C08: Grafito, Tinta, Niebla, Oliva, Cobre, Ciruela, Marfil y Océano. Ocho paletas, ambos temas, aplicación/acciones y PDF/contexto, tablet y teléfono: 64 capturas.
- T01–T05: Geist, Inter, Sistema (Roboto API33), Serif y Mono. Ambas dimensiones; oscuro/acciones: 10 capturas. Recursos ya existentes, sin fuentes descargadas.
- E01–E03: Compacto, Equilibrado, Aireado. Filas 48/54/64dp; márgenes12/18/24dp; separación16/28/40dp: 6 capturas.
- A01–A03: Plano, Suave, Glass legible. Sin blur; el último utiliza gradiente96–98,5%: 6 capturas.

Base común de comparaciones: C01+T01+E02+A02. Cada eje cambia por separado. La selección/exportación JSON recoge preferencias; NO aplica una mezcla mediante CSS ni muestra una combinación no renderizada. La mezcla elegida requerirá capturas Compose propias antes de aprobar integración.

Recomendación de exploración: Niebla para calma, Cobre para personalidad cálida, Marfil especialmente en claro. Geist/Inter y Equilibrado son las piezas más conservadoras; Serif y Mono cambian el carácter y pueden recortar títulos antes. No se adopta ninguna.

## Evidencia y verificaciones

86 PNG nuevos reales de Compose/Robolectric API33, 1200×800dp y412×860dp, mismos cuatro resultados/IDs, consulta `proyecto`; app seleccionada para acciones y PDF ficticio para contexto. Dos PNG Puente anteriores reutilizados exactamente, hashes en reference-reuse.json. Capturas sin retoque; sólo las hojas de contacto se reducen y rotulan fuera de la UI.

Cuatro pruebas dirigidas aprobadas: dos renderizados, contraste de tokens opacos y teclado/selección/recibo local sin ejecución externa. BUILD SUCCESSFUL58s; XML/log publicados. Cinco rásteres distintos de consulta por perfil prueban que las cinco familias se renderizaron realmente. El documento conserva Geist, no se altera su fuente para simular la tipografía de UI.

Contraste medido >=4,5 para texto principal/secundario/acento sobre superficie opaca y texto principal sobre selección. Esto NO certifica toda accesibilidad, transparencia ni contraste sobre aplicaciones reales. No se ejecutó lint global: producción sin delta. No se repiten suites históricas.

Chromium: 88 imágenes decodificadas, categorías8/5/3/3, comparación del mismo eje, zoom de referencia, controles incompatibles desactivados, exportación JSON, sin desborde a390px ni errores JS. Se cargó HTML exacto mediante set_content; navegación file:// no probada por política del navegador administrado. HTML autosuficiente sin red.

## Límites y preservación

Representación de Puente dentro de un único lienzo Compose, no dos ventanas WindowManager. Datos ficticios; botones sólo emiten recibo local de laboratorio. Sin comprobación física de IME, consumo, fluidez, overlay sobre otras apps, texto200% ni rotación. Pendientes antes de integrar; no declarar experiencia final aprobada.

Sólo app/src/test y documentación/evidencias. app/src/main, compilación, firma, paquetes, versiones, manifiestos, FocusStudio, Preview69, C2/C5, catálogo36funciones y bancos previos intactos. Sin APK/testAPK, Actions, instalaciones, dependencias, permisos ni main. Autorización69 consumida.

Reproducción dirigida:

```sh
bash gradlew :app:testDebugUnitTest --tests '*PuenteFinishesGalleryTest' -I scripts/directed-tests.init.gradle -I docs/verification/c2-app-open-20261107/no-apk.init.gradle -PspotlightDirectedClasses=dev.bautista.spotlight.ui.PuenteFinishesGalleryTest -Pkotlin.compiler.execution.strategy=in-process --offline --no-daemon --max-workers=1 --console=plain
python3 docs/verification/puente-finishes-20261108/build_html.py
python3 docs/verification/puente-finishes-20261108/browser.py
python3 docs/verification/puente-finishes-20261108/pdf.py
```

Java17/SDK y caché local requeridos; sin instalar ni descargar. Generador HTML/PDF consume los originales publicados. Reserva se actualiza al checkpoint publicado y sólo se libera la propia con nota y transacción atómica.
