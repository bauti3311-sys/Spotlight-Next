# Continuidad de Spotlight Next

Actualizado: 2026-10-13. Este archivo registra decisiones, evidencia y siguiente paso; no convierte trabajo pendiente en validado.

## Estado recuperable

- Código de entrega 2.0.1/28: `9bb9d0ca7a97e9798ae5197c3ee32958e643dd6d`, rama `fix/recover-2.0.1-ci`, PR #6 borrador.
- Debug, unitarias, lint y Python: [run 38321571252](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38321571252), correcto.
- PdfRenderer real: [run 38321571205](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38321571205), tres pruebas correctas en Android 29.
- Local: `:app:assembleDebugAndroidTest`, `:app:lintRelease` y `:app:assembleRelease` ejecutados; R8 correcto y lint 0 errores/10 advertencias.
- Firma/ZIP exacto: rama separada `ci/deliver-2.0.1-9bb9d0c`, fuente fija anterior; [run 38329360568](https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/38329360568) iniciado. Actualizar el resultado cuando termine; no afirmar firmado todavía.
- main sigue en `47259ce2d7259f0ddfa5d85b7c73f07983d8cd6d`. PR #5 y ambos checkpoints preservados.
- No hay Galaxy Tab A9+ conectada. Comparación de manifiesto/certificado no equivale a instalación física.

## Secuencia aprobada

1. Completar entrega histórica 2.0.1, comparar con APK distribuido 2.0.0/27 y preservar evidencia. Sin merge a main.
2. Incorporar AGENTS.md, este plan y las tres skills en una rama independiente; no modificar el SHA del APK ya verificado.
3. Preparar Maestro, Roborazzi, fixtures offline, pruebas de foco/overlay/PDF y diagnóstico de memoria/rendimiento. Incorporar una herramienta por commit y ejecutar sus checks.
4. Laboratorio web separado: Node LTS, React/Vite/TypeScript, Playwright/Chromium. Investigar bibliotecas Compose y licencias antes de elegir dependencias Android.
5. Automatización general protegida: aprobaciones de versión, checks funcionales/visuales/secretos, firma histórica y APK/ZIP conjunto. Nunca auto-merge de cambios defectuosos.

## Formato para tareas extensas

Registrar objetivo y alcance, rama/base SHA, decisiones y referencias/licencias, cambios pequeños, comandos realmente ejecutados, resultados/checks, artefactos/SHA y pendientes. Separar herramientas configuradas de herramientas ejecutadas; referencias visuales de código reutilizable; validación automática de prueba física.

Cada nueva tarea continúa desde la última evidencia: no reiniciar ni revalidar indiscriminadamente si nada relevante cambió. Cuando falle una prueba, reproducir y corregir su causa conservando el contrato.
