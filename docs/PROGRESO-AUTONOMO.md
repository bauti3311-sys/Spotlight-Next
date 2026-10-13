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
