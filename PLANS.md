# Continuidad de Spotlight Next

Actualizado: 2026-10-13. Este archivo registra decisiones, evidencia y siguiente paso; no convierte trabajo pendiente en validado.

## Estado recuperable

- Código de entrega 2.0.1/28: `9bb9d0ca7a97e9798ae5197c3ee32958e643dd6d`, rama `fix/recover-2.0.1-ci`, PR #6 borrador.
- Debug, unitarias, lint y Python: [run 38321571252](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38321571252), correcto.
- PdfRenderer real: [run 38321571205](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38321571205), tres pruebas correctas en Android 29.
- Local: `:app:assembleDebugAndroidTest`, `:app:lintRelease` y `:app:assembleRelease` ejecutados; R8 correcto y lint 0 errores/10 advertencias.
- Firma/ZIP exacto: rama separada `ci/deliver-2.0.1-9bb9d0c`, fuente fija anterior; [run 38329360568](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38329360568) correcto. [Entrega privada conjunta](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38329360568/artifacts/11782506585): APK histórico, ZIP fuente, manifiesto de hashes y verificación comparada con el APK distribuido 2.0.0/27. Orquestador `d70a85e`; fuente APK/ZIP `9bb9d0c`.
- main sigue en `47259ce2d7259f0ddfa5d85b7c73f07983d8cd6d`. PR #5 y ambos checkpoints preservados.
- La hoja de ruta registra actualización física exitosa reportada por el usuario; aún falta evaluación física sistemática de One UI. Esta instancia no tiene tablet conectada.

## Secuencia aprobada

1. Completar entrega histórica 2.0.1, comparar con APK distribuido 2.0.0/27 y preservar evidencia. Sin merge a main.
2. Incorporar AGENTS.md, este plan y las tres skills en una rama independiente; no modificar el SHA del APK ya verificado.
3. Preparar Maestro, Roborazzi, fixtures offline, pruebas de foco/overlay/PDF y diagnóstico de memoria/rendimiento. Incorporar una herramienta por commit y ejecutar sus checks.
4. Laboratorio web separado: Node LTS, React/Vite/TypeScript, Playwright/Chromium. Investigar bibliotecas Compose y licencias antes de elegir dependencias Android.
5. Automatización general protegida: aprobaciones de versión, checks funcionales/visuales/secretos, firma histórica y APK/ZIP conjunto. Nunca auto-merge de cambios defectuosos.

## Formato para tareas extensas

Registrar objetivo y alcance, rama/base SHA, decisiones y referencias/licencias, cambios pequeños, comandos realmente ejecutados, resultados/checks, artefactos/SHA y pendientes. Separar herramientas configuradas de herramientas ejecutadas; referencias visuales de código reutilizable; validación automática de prueba física.

Cada nueva tarea continúa desde la última evidencia: no reiniciar ni revalidar indiscriminadamente si nada relevante cambió. Cuando falle una prueba, reproducir y corregir su causa conservando el contrato.

## Infraestructura gradual

Rama `chore/codex-infrastructure-2.0.1`, separada del APK entregado. AGENTS/skills validados; MockWebServer añadido solo a tests, seis contratos offline y tres tests IA previos pasaron. Detectó normalización incorrecta de timeout/cancelación, corregida sin cambiar la aserción, exclusivamente en esta rama.

Laboratorio React/TypeScript/Vite con lockfile: build y seis pruebas Chromium tablet/teléfono correctas; referencias como inspector y prototipo propio. Roborazzi compatible 1.29.0: diez PNG reales versionados, registro y verificación correctos. Gitleaks escaneó 55 commits sin hallazgos. Maestro 2.11.0: dos flujos con sintaxis válida; MCP stdio inicializa y lista herramientas. Configuración ejemplo permite solo herramientas locales, sin operaciones Cloud.

Infraestructura `83e887a`: CI 38337171465 completo, cinco jobs correctos (Android/Roborazzi, web, secretos, Maestro teléfono/tablet). No hay emulador local; las capturas de PDF renderizado/carga, Macrobenchmark/Baseline Profiles y diagnóstico LeakCanary quedan como siguiente incorporación gradual. Perfetto tiene configuración/launcher preparados, sin mediciones afirmadas. Renovate está preparado, no activado como bot. ktlint/Detekt siguen en evaluación antes de reformatear legado.

## Turno autónomo actual — F1.1

Base documental actual `372faa7`, rama aislada `feat/width-policy-f1`. Leer `docs/PROGRESO-AUTONOMO.md` de su punta antes de repetir trabajo. Objetivo: coherencia de automático/manual y override horizontal, preservando preferencias. Reserva remota `locks/spotlight-autonomy`; adquirir de forma atómica y no tomar un trabajo ocupado. La automatización conserva su cadencia; si este turno sigue activo, debe omitir edición concurrente.

### Checkpoint probado de F1.1

Producto en `6a59b6b` + `0ced615`, PR #8 borrador. Ancho general/override horizontal comparten una política; memoria manual persiste y se exporta/importa; restablecer posición conserva ancho. Local: 328 unitarias, lint 0 errores/10 advertencias, debug correcto, 12 comparaciones visuales y Python 5+12 correctos. Detalles y estado de reserva/CI en `docs/PROGRESO-AUTONOMO.md`; consultar checks/descripcion PR #8 antes de retomar.

Siguiente F1.2: densidad y vista previa del ancho, desde el checkpoint de este PR para no perder trabajo sin integrar. Mantener rama aislada y PR borrador. One UI físico y F1.3 (métricas/insets/IME) siguen pendientes; no declarar F1 completo por este subhito.
