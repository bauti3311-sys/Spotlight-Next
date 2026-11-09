# Comparación nativa Puente / Stitch

Abrir `Puente-Stitch.html` (autocontenido) o el PDF. Los originales están en
`before` / `after`, con sus SHA-256 en `SHA256.json`. HTML no renderiza la app:
sólo muestra las capturas reales Compose/Robolectric. Fixture de apps «Proyecto»,
«Proyecto Atlas», «Proyecto Notas», sin datos personales ni servicios remotos.

Base:9cb204bad281d5efa6f57da7d4e0b0e7c737d20a.
Implementación:b764afe7eef19adcea38e0926192f0fc1d821b38.

## Reproducción acotada

Runtime nativo Robolectric API33 ya disponible, JDK17 y SDK Android local. No APK,
Actions, instalación ni descarga de modelos. Usar el init de pruebas dirigidas y
`docs/verification/c2-app-open-20261107/no-apk.init.gradle` para bloquear esos tasks.

- `StitchPresentationTest`: seis perfiles, resultados/inspector y popup Android real.
  `STITCH_CAPTURE_PHASE=after` escribe en `app/build/stitch-native/after`.
- Para antes, worktree separado exactamente9cb204b, copiar **sólo** ese nuevo banco,
  usar `STITCH_CAPTURE_PHASE=before`; nunca revertir producción actual para capturar.
- `SettingsRefreshTest.*Capture`, `SETTINGS_CAPTURE_PHASE=stitch-before/stitch-after`:
  Activity real, tablet/teléfono/texto200%. No ejecutar el resto de la suite por rutina.
- Revisión contextual: `FocusBIntegrationTest.tabletDark`, comparando misma base/
  implementación. Se reutiliza el banco existente con su contrato de restauración.
- `StitchInteractionTest` y `PuenteMaterialTest`: teclado, scroll/objetivos48dp,
  contraste de texto y preservación del color personalizado/error.

Ejemplo dirigido: `:app:testDebugUnitTest --tests '*StitchPresentationTest'`, con
`-I scripts/directed-tests.init.gradle`, init no-APK y
`-PspotlightDirectedClasses=dev.bautista.spotlight.ui.StitchPresentationTest`,
`--offline --no-daemon --max-workers=1`. No lanzar suites globales ni instrumentación.

`tests/final` reúne20casos únicos correctos. `tests/initial` conserva un fallo de
foco; `tests/baseline`/`targeted` documentan el control aislado. No se modificaron
aserciones para esconderlo. Última recaptura sólo corrigió el método de screenshot
para la segunda ventana, no el comportamiento productivo.

Galería comprobada en Chromium por localhost temporal: filtros,52imágenes cargadas,
zoom/Escape, teléfono sin overflow y cero peticiones externas. El navegador administrado
bloquea file:// aquí; no se afirma haberlo probado por esa vía. Servidor ya cerrado.
No es evidencia de OneUI/Gboard, instalación, rendimiento ni éxito de acciones externas.
