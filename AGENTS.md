# Spotlight Next — instrucciones para Codex

Leé `docs/RENOVACION-APROBADA.md` para el contrato funcional y `docs/CODEX-ENTORNO-APROBADO.md` para la infraestructura aprobada. Usá `PLANS.md` para continuidad y `docs/ROADMAP-AUTONOMA-SPOTLIGHT-NEXT.md` como orden de hitos verificables y política de reanudación; nunca asumir que el reset de créditos dispara tareas.

- Kotlin y Jetpack Compose nativos. Priorizar funcionamiento verificable; no agregar pantallas/modos redundantes ni dependencias en bloque.
- Enter desde resultados ejecuta la acción principal. Tab abre y recorre acciones, Shift+Tab retrocede; Enter en acciones ejecuta la enfocada. Escape cierra acciones antes de vistas secundarias y del overlay.
- Seleccionar un PDF muestra Quick Look local automáticamente; abrirlo externamente requiere una acción explícita. Conservar selección y recursos al cambiar de documento.
- Ninguna IA remota por pulsación. Confirmar uso externo de contenido sensible. No inventar datos, permisos o integraciones.
- Conservar datos/preferencias, `dev.bautista.spotlight.next` y firma histórica. No sustituir release por debug ni generar una nueva clave.
- Ejecutar pruebas apropiadas y registrar SHA, comandos, resultados y límites. No eliminar/desactivar pruebas ni reducir aserciones para conseguir verde. No afirmar verificaciones no ejecutadas ni validación física basada en emuladores.
- Firma únicamente mediante flujo autorizado y protegido; nunca exponer secretos a PRs, logs o ZIP. APK y ZIP fuente deben corresponder al mismo SHA.
- Mantener PR #5, `checkpoint/integrations-2.0.1` y `checkpoint/documents-2.0.1`. No integrar main sin autorización explícita. Trabajar en ramas aisladas.
- Herramientas de laboratorio/CI fuera del APK; LeakCanary solo debug, MockWebServer solo tests. Evaluar versión, licencia, compatibilidad y rendimiento antes de incorporar bibliotecas visuales.

- Autonomía: mantener dos líneas, estabilidad y desarrollo funcional. F1.3 es incidencia conocida, no bloqueo de F1.4. Máximo una investigación dirigida adicional; sin hipótesis comprobable, pasar a tareas independientes. Checkpoints frecuentes, cuota solo si verificable, ninguna automatización afirmada sin comprobarla.

- Coordinación: `python3 scripts/autonomy_lock.py status` antes de editar; adquirir propietario único, actualizar checkpoint publicado y liberar solo la propia reserva/SHA. Ver `docs/AUTONOMIA-RESERVA.md`; no reclamar por edad ni forzar pushes. Leer las notas del checkpoint parcial más reciente sin perder avances; el puntero integral solo avanza con sus pruebas completas.

Skills locales: `.agents/skills/spotlight-fix-bug/SKILL.md`, `.agents/skills/spotlight-visual-review/SKILL.md`, `.agents/skills/spotlight-release/SKILL.md`. Leé la correspondiente antes de aplicarla.
