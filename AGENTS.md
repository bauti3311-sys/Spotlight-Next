# Spotlight Next — instrucciones para Codex

Leé `docs/RENOVACION-APROBADA.md` para el contrato funcional y `docs/CODEX-ENTORNO-APROBADO.md` para la infraestructura aprobada. Usá `PLANS.md` para continuidad.

- Kotlin y Jetpack Compose nativos. Priorizar funcionamiento verificable; no agregar pantallas/modos redundantes ni dependencias en bloque.
- Enter desde resultados ejecuta la acción principal. Tab abre y recorre acciones, Shift+Tab retrocede; Enter en acciones ejecuta la enfocada. Escape cierra acciones antes de vistas secundarias y del overlay.
- Seleccionar un PDF muestra Quick Look local automáticamente; abrirlo externamente requiere una acción explícita. Conservar selección y recursos al cambiar de documento.
- Ninguna IA remota por pulsación. Confirmar uso externo de contenido sensible. No inventar datos, permisos o integraciones.
- Conservar datos/preferencias, `dev.bautista.spotlight.next` y firma histórica. No sustituir release por debug ni generar una nueva clave.
- Ejecutar pruebas apropiadas y registrar SHA, comandos, resultados y límites. No eliminar/desactivar pruebas ni reducir aserciones para conseguir verde. No afirmar verificaciones no ejecutadas ni validación física basada en emuladores.
- Firma únicamente mediante flujo autorizado y protegido; nunca exponer secretos a PRs, logs o ZIP. APK y ZIP fuente deben corresponder al mismo SHA.
- Mantener PR #5, `checkpoint/integrations-2.0.1` y `checkpoint/documents-2.0.1`. No integrar main sin autorización explícita. Trabajar en ramas aisladas.
- Herramientas de laboratorio/CI fuera del APK; LeakCanary solo debug, MockWebServer solo tests. Evaluar versión, licencia, compatibilidad y rendimiento antes de incorporar bibliotecas visuales.

Skills locales: `.agents/skills/spotlight-fix-bug/SKILL.md`, `.agents/skills/spotlight-visual-review/SKILL.md`, `.agents/skills/spotlight-release/SKILL.md`. Leé la correspondiente antes de aplicarla.
