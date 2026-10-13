# Progreso autónomo de Spotlight Next

## 2026-10-13 — inicio manual F1.1

- Fecha/zona: 2026-10-13, America/Argentina/Buenos_Aires.
- Rama: `feat/width-policy-f1`; base `372faa781ad2e1bf32e520bba9ed53567ebc4a34` (punta PR #7).
- Hito/subhito: F1.1, ancho general automático/manual y excepción horizontal explícita.
- Estado: EN CURSO.
- Reserva compartida: `refs/heads/locks/spotlight-autonomy`; su commit contiene JSON de propietario/estado/rama. No editar si la reserva sigue activa o no se puede demostrar que su propietario terminó. Nunca borrar una reserva ajena por antigüedad.
- Diagnóstico: Apariencia muestra automático general sin avisar el override horizontal; el control horizontal es un slider sin activación explícita; restablecer posición también borra ancho. Cambiar a manual siempre vuelve a 640 dp.
- Criterios: excepción visible en ambos ajustes; ancho y posición restablecen por separado; conservar preferencias antiguas y último manual general al alternar automático; tests de persistencia, orientación/IME, interacción y capturas teléfono/tablet. No tocar cálculo de métricas/insets todavía.
- Código base comprobado: CI `83e887a`, run https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38337171465 correcto; los commits hasta 372faa7 solo añaden documentación.
- Checks de este subhito: aún no ejecutados; no marcado como validado.
- Impacto APK: ninguna dependencia nueva prevista; Compose nativo, misma identidad/firma.
- Main/merge/firma/release: NO.
- Siguiente acción exacta: implementar control compartido de anchos y memoria manual compatible, probar y actualizar este registro. La automatización debe consultar esta rama antes de comenzar otro F1.

## Checkpoint F1.1 — validación local completa

- Fecha/hora: 2026-10-13T01:21:03.616836-03:00; zona America/Argentina/Buenos_Aires.
- Rama/PR: `feat/width-policy-f1`, [PR #8 borrador](https://github.com/bauti3311-sys/Spotlight-Next/pull/8), base infraestructura PR #7.
- Hito/subhito: F1.1 — ancho general automático/manual, excepción horizontal explícita y restablecimientos separados.
- Estado: VALIDADO LOCALMENTE. CI remoto: consultar [checks del PR #8](https://github.com/bauti3311-sys/Spotlight-Next/pull/8/checks); el resultado final también queda en la descripción del PR.
- Base -> commits producto: `372faa7` -> `6a59b6b` (política/persistencia/importación), `0ced615` (controles y capturas).
- Reserva: `locks/spotlight-autonomy`, propietario `manual-d241406e-21b0-4ace-b61b-4af11fd839a6`; sigue ACTIVA hasta cerrar la verificación remota. No editar en paralelo ni borrar la reserva sin demostrar cierre del propietario. Si un turno encuentra el lock, debe omitir modificaciones y registrar el motivo en el hilo.
- Archivos principales: OverlayWidthPolicy, Preferences/ConfigTransfer, AdaptiveLayout, OverlayWidthSettings, GlassAppearance/GlassSettings y tests nuevos. Dos PNG nuevos Roborazzi versionados; los diez previos no se alteraron.
- Reproducción: 2/2 tests nuevos fallaron en la base (override invisible y botón de posición borrando ancho). Otro test mostró que el respaldo no aceptaba ancho automático 0. Se corrigieron sin reducir aserciones/plazos. Una comprobación de visibilidad expuso el banner bajo el viewport: se movió junto al control general.
- Checks ejecutados:
  - `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug --no-daemon --max-workers=2 --console=plain`: 328 tests, 0 fallos/errores/omisiones; build correcto; lint 0 errores/10 advertencias.
  - `:app:recordRoborazziDebug --tests '*OverlayWidthVisualSnapshotTest'`: solo 2 referencias nuevas; revisadas visualmente.
  - `:app:verifyRoborazziDebug --tests '*VisualSnapshotTest'`: 12 comparaciones correctas, sin cambiar referencias existentes.
  - Python unittest discover: backend 5/5 y scripts 12/12.
  - Gitleaks: historial sin hallazgos, sin allowlists para ocultar problemas.
  - 33 tests específicos de ancho/persistencia/importación/ajustes existentes: correctos antes de la suite completa.
- Capturas: `app/src/test/snapshots/width-{phone,tablet}-active-override.png`; UI funcional en `app/build/091-ui/f1-width-*.png`, incluyendo teléfono horizontal. Son renderizaciones Compose reales con Robolectric nativo SDK 33, no pruebas físicas One UI.
- Impacto APK: ninguna dependencia nueva. Comparación debug local contra infraestructura: 177721950 -> 177738334 bytes (+16384); las capturas de tests no entran en APK. Release no medido ni firmado.
- Main/merge/firma/release: NO. APK histórico 9bb9d0c permanece intacto.
- Próximo subhito: F1.2, separar visualmente densidad de controles del ancho físico y preparar una vista previa fiel. Retomar desde la punta de este PR comprobando checks y reserva; no repetir F1.1 ni partir del árbol antiguo de infraestructura perdiendo sus cambios. Conservar ramas/PRs borrador sin merges; una nueva rama puede descender del checkpoint verificado, manteniendo la infraestructura como ancestro.
- Pendiente humano: validar controles/persistencia/orientación con teclado y One UI en Galaxy Tab A9+. F1 completo aún NO está terminado; métricas/insets/IME y expansión siguen pendientes.

## 2026-10-13T06:08:28.103011+00:00 — F1.2 inicio reservado

- Estado: EN CURSO; base `2a5607e46160b708725ccc9de4611db30772a281`, rama `feat/density-preview-f1`. Zona America/Argentina/Buenos_Aires.
- Reserva: propietario `scheduled-7403530c-96d0-4e92-a468-e6a8785d3951`, commit `b207f9556b208fa18e7b0b60129ed696c818a0f4` en `locks/spotlight-autonomy`. Solo este propietario puede liberar; si aparece en otro ciclo, omitir edición concurrente.
- Base: CI 38346023892 correcto, cinco jobs; nota final de F1.1 leída. No repetir F1.1.
- Alcance: densidad describe espaciado de filas, no ancho; vista previa con ResultRow real y ancho calculado por AdaptiveLayout para la orientación actual. Sin métricas nuevas (F1.3), dependencias ni APIs remotas.
- Criterios: borrador no persiste hasta aplicar; densidad conserva ancho/override/memoria; vista previa respeta automático/manual/override y límites en teléfono/tablet, vertical/horizontal. Capturas nativas y checks pertinentes antes de declarar terminado.
- Pruebas F1.2: aún no ejecutadas. Siguiente acción: implementar y probar componentes de ajustes y vista previa; continuar esta rama si hay interrupción. Main/merge/firma/release: NO.

## 2026-10-13T03:38:06.323460-03:00 — checkpoint F1.2 probado localmente

- Fecha/zona: 2026-10-13T03:38:06.323460-03:00, America/Argentina/Buenos_Aires.
- Subhito: F1.2, densidad independiente y vista previa inicial fiel al cálculo actual de ancho. Estado: VALIDADO LOCALMENTE; CI final pendiente de cierre en [checks PR #9](https://github.com/bauti3311-sys/Spotlight-Next/pull/9/checks), descripción y nota final del SHA.
- Base -> producto: `2a5607e46160b708725ccc9de4611db30772a281` -> `50a5bf2cb79614e52fa2d920ebfba1b810b9ebdb`. El commit final con estos registros/CI se identifica en el PR y refs/notes, sin alterar el árbol probado después de CI.
- Rama/PR: `feat/density-preview-f1`, PR #9 borrador a `chore/codex-infrastructure-2.0.1`; infraestructura 372faa7 permanece como ancestro.
- Reserva propia: `scheduled-7403530c-96d0-4e92-a468-e6a8785d3951`, commit `b207f9556b208fa18e7b0b60129ed696c818a0f4`; ACTIVA hasta cerrar CI. Para recuperación consultar punta de esta rama, este registro y la nota final; no tomar F1.3 si este checkpoint aún está pendiente. Solo el propietario libera su reserva, verificando SHA remoto.
- Archivos: OverlayAppearancePreview, GlassAppearance, tests de geometría/borrador/selección y snapshots. Cinco PNG nuevos, doce anteriores sin cambios. CI conserva informe funcional completo y capturas f12.
- Comandos ejecutados:
  - `bash gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --no-daemon --max-workers=2 --console=plain`: 340 tests, 0 fallos/errores/omisiones; lint 0 errores/10 advertencias (1 información); debug correcto.
  - `bash gradlew :app:testDebugUnitTest --tests '*OverlayAppearancePreviewTest' ...`: siete tests finales correctos; además exigen visibilidad de fila y chip seleccionado.
  - `:app:recordRoborazziDebug --tests '*OverlayAppearanceVisualSnapshotTest'`: cinco referencias nuevas propias (compacta/espaciosa/tablet vertical/teléfono vertical y horizontal), revisadas visualmente.
  - `:app:verifyRoborazziDebug --tests '*VisualSnapshotTest'`: 17 comparaciones correctas; ningún PNG previo actualizado.
  - Python `unittest discover -s backend` 5/5 y `-s scripts` 12/12; sin claves reales/APIs pagas.
  - Gitleaks 8.30.1: 71 commits examinados sin hallazgos antes de los últimos checkpoints; CI vuelve a escanear el historial final.
- Investigación: test nuevo desplazaba el contenedor horizontal de chips sin traerlos al viewport; ahora desplaza la tarjeta externa y verifica selección/visibilidad. Selector de fila acotado y altura sin recorte. En suite apareció timeout de Next091NavigationTest (339 tests, 1 fallo); la prueba aislada sobre base pasó. Se drena el looper Android pausado, igual que ActionHubVisualTest, con condiciones/aserciones/plazo original de 15 s. Suite final pasó; buscador/overlay sin cambios.
- Capturas/diff: `docs/visual/f1.2/{before,after,diff}-tablet.png`, Compose nativo SDK 33, 1280×800, mismo ancho general 820/override 460/densidad espaciosa; antes ejecutado contra 2a5607e con probe temporal eliminado, árbol base limpio. Diff sin máscaras: 341885 píxeles distintos de 1024000; cambio de interfaz previsto, no medida de rendimiento. Ver README de procedencia. Teléfono/tablet y ambas orientaciones en `app/src/test/snapshots/appearance-*.png`; UI funcional `app/build/091-ui/f12-*.png`.
- Impacto/dependencias/licencias: ninguna dependencia ni recurso externo nuevo; componentes propios con Compose/ResultRow/Inter existentes. Debug local 177738334 -> 177738334 bytes; capturas fuera del APK. Release y rendimiento no medidos.
- Límites: preview utiliza configuración dp/orientación actual y ventana inicial sin expansión; no resuelve aún diferencias WindowMetrics/insets/IME de One UI. Validación física Galaxy Tab A9+ sigue pendiente. Main/merge/firma/release: NO.
- Siguiente acción exacta: comprobar CI del commit final; si verde, avanzar checkpoint/autonomous-next por fast-forward, publicar nota de cierre y liberar solo esta reserva. Próximo ciclo F1.3: reproducir cálculos de espacio usable/rotación/IME antes de cambiar OverlayService/AdaptiveLayout.

## 2026-10-13T03:56:02.827354-03:00 — CI pendiente por espera intermitente existente

- Estado: EN CURSO, misma reserva/propietario y PR #9. El puntero validado sigue en 2a5607e; no avanzar al checkpoint sin CI completo correcto.
- CI del checkpoint 042c93b: [38357821467](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38357821467). Web 6 tests correcto, secretos 73 commits sin hallazgos, Maestro Pixel 6/Pixel C correcto. Android: 340 tests, 1 fallo en ActionHubVisualTest.lightHubOpensTypesBeforeListingFiles; siete tests nuevos correctos. Roborazzi/Python remotos se omitieron por el fallo previo, aunque sus validaciones locales ya constan arriba.
- Evidencia: job 115164819073 y artefacto native-functional-visual-9 /11793303751. El conector recupera un enlace de ZIP, pero la política de red impide descargar File Service en este executor (proxy403); no se ha leído el XML remoto. Se añade salida estándar/stack completo de tests, opt-in `-Pspotlight.test.verbose=true`, para que próximos fallos sean diagnosticables desde logs CI. Sin secretos/reales ni cambios en APK.
- Hipótesis/corrección de fixture: idle solo drena callbacks listos de Android; idleFor16ms además hace progresar el reloj pausado para callbacks diferidos/frame. ActionHub/Next091 conservan condiciones, aserciones y límite real 15 s; arranque ActionHub usa el mismo helper. Buscador/overlay no modificados.
- Ejecutado tras cambio: seis tests ActionHub/Next091 correctos con verbose. Dos repeticiones explícitas de esas seis pruebas, suite completa y 17 píxeles en curso; registrar resultados finales en nota del SHA y descripción PR, sin cambiar luego el árbol validado. No dar el fallo por corregido definitivamente hasta comprobar CI.
- Siguiente acción exacta: terminar esas verificaciones y CI del nuevo checkpoint. Si falla, leer diagnóstico publicado y corregir causa sin tocar aserciones/baselines. Si verde, avanzar puntero por FF, publicar cierre y liberar solo la propia reserva; después F1.3, no repetir F1.2. Main/merge/firma/release: NO.
