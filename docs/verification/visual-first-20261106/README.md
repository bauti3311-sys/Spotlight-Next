# Evidencia Visual First — base f27c867

- 8 métodos VisualFirstGalleryTest, uno por perfil; 120 PNG de 9 escenas.
- VisualFirstMotionTest: 12 fotogramas/estados de reducción y contraste de tokens.
- VisualFirstIconReviewTest: 2 métodos, 4 láminas y objetivos48dp.
- FocusBIntegrationTest.phoneDarkText200: regresión de la base aprobada.
- Total12 casos distintos, todos correctos. Los24pares conservados comparten
  originalA; no representan cambios ni se vuelven a generar como B.

Java17/SDK35 existentes, API33 Robolectric/native graphics, densidad1. La matriz
se ejecutó con :app:testDebugUnitTest --tests '*VisualFirstGalleryTest'
--tests '*VisualFirstMotionTest' --tests '*FocusBIntegrationTest.phoneDarkText200',
-I scripts/directed-tests.init.gradle y -PspotlightDirectedClasses limitado a
esas tres clases, --no-daemon --max-workers=1 --console=plain. Iconos se ejecutan
después con filtro de su única clase. No assemble ni Actions/lint repetidos.

manifest.json fija base, perfiles, escenas, hashes originales, tipo de render y
casos ejecutados. Los pilotos corrigieron sólo el fixture: argumento icon omitido,
altura/contexto de grupo de Ajustes y espera de número/foco al alternar B.
No se actualizó una baseline de producto ni se eliminaron aserciones.

Generación de artefactos de revisión:
`python3 docs/verification/visual-first-20261106/build-gallery.py RUTA_SALIDA`.
Usa Python/ReportLab y DejaVuSans ya disponibles en el laboratorio; no dependencias
de la app. HTML autocontenido con filtros/zoom, PDF16páginas con12pares elegidos,
iconos y movimiento. Los PNG se embeben sin edición ni recorte. La imagen ampliada
siempre es el original. El HTML muestra fotogramas reales4x más lentos con pausa
final; no es un benchmark ni video físico.

Galería verificada con Playwright existente vía set_content (file:// está bloqueado
por la política del navegador administrado): filtros/zoom/72tarjetas correctos y
cero solicitudes remotas de la página. No servicio permanente ni servidor nuevo.

No APK/cuentas/audio/dispositivos, ni OneUI/Gboard probados. PM/monogramas y media
son fixtures; el TXT es un archivo real de laboratorio. Los periféricos son una
escena de componentes, no una captura íntegra de la Activity/APK. Base y propuesta
mantienen la misma escena/datos/perfil; scroll automático refleja el mismo campo
enfocado cuando cambia el layout. Ver auditoría para decisiones y límites.
