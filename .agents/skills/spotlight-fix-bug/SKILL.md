---
name: spotlight-fix-bug
description: Reproducir, diagnosticar, corregir y verificar errores de Spotlight Next en búsqueda, teclado, overlay, documentos e integraciones.
---

# Flujo

1. Leer AGENTS.md, el contrato pertinente de docs/RENOVACION-APROBADA.md y PLANS.md. Inspeccionar archivos reales, git, último SHA y logs de CI. Mantener una base recuperable en rama aislada.
2. Reproducir con el menor flujo real disponible. Guardar fallo, test/escenario, plataforma y SHA. Si no se reproduce, decirlo; no inventar una causa.
3. Diagnosticar causa en producción, estado, ciclo de vida o fixture. Respetar Enter/Tab/Shift+Tab/Escape y no ocultar errores de permisos/proveedores.
4. Corregir lo mínimo necesario. No eliminar/desactivar tests, reducir aserciones o alargar timeouts para tapar una regresión. Ampliar cobertura cuando verifique un comportamiento relevante.
5. Ejecutar el caso y checks afectados. Para un checkpoint Android: `bash gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --no-daemon --max-workers=2 --console=plain`; Python en backend/scripts con unittest discover. PdfRenderer requiere `:app:connectedDebugAndroidTest` en dispositivo/emulador, no solo Robolectric.
6. Documentar comandos, resultados, SHA, enlaces y límites; actualizar PLANS.md. Commits pequeños y push sin force, sin modificar main ni checkpoints protegidos.

No confundir compilación del APK de pruebas con ejecución instrumentada; ni una corrida verde con validación física o una medición de rendimiento.
