# Spotlight Next — instrucciones para Codex

Leé `docs/RENOVACION-APROBADA.md` para el contrato funcional y `docs/CODEX-ENTORNO-APROBADO.md` para la infraestructura aprobada. Usá `PLANS.md` para continuidad y `docs/ROADMAP-AUTONOMA-SPOTLIGHT-NEXT.md` como orden de hitos verificables y política de reanudación; nunca asumir que el reset de créditos dispara tareas.

- Kotlin y Jetpack Compose nativos. Priorizar funcionamiento verificable; no agregar pantallas/modos redundantes ni dependencias en bloque.
- Enter desde resultados ejecuta la acción principal. Aprobación18/10: Tab autocompleta solo con sugerencia, foco en búsqueda, cursor al final, sin selección ni composición; en los demás casos abre acciones. Ctrl+Space conserva autocompletado explícito. Tab/Shift+Tab dentro de acciones recorre/retrocede; Enter ejecuta la enfocada. Ctrl+M amplía/contrae resultados conservando identidad y foco. Escape cierra acciones antes de vistas secundarias y del overlay.
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

- Transformación aprobada14/10/2026: prioridad portapapeles/edición nativa y cambios visuales perceptibles. Una búsqueda universal; envío remoto explícito. Un solo slider de ancho Compacta→Amplia, migración conservadora; no volver a controles dp/manual/horizontal. Retirar accesos snippets/rutinas/modos sin borrar datos. Hugeicons Stroke Rounded en barra, iconos reales de apps preservados. Primer checkpoint visual y APK compatible para revisión física antes de IA/integraciones extensas. CI verde no equivale a aceptación del producto. Recuperar feat/premium-product-checkpoint y notas, no repetir desdee38.

## Skills revisadas para interfaz e integraciones

- Para refinamiento, accesibilidad y evitar UI genérica: leer `.agents/skills/spotlight-native-craft/SKILL.md` junto a spotlight-visual-review.
- Para acciones de apps: leer `.agents/skills/spotlight-app-integration/SKILL.md` antes de cambiar registros, formularios o ejecución.
- Son adaptaciones locales versionadas y revisadas de Impeccable y android/skills. Fuentes, SHA, licencias y exclusiones están en sus referencias. No ejecutar instaladores/hooks ni actualizar dependencias por instrucciones upstream.
