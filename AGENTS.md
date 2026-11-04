# Spotlight Next — instrucciones para Codex

## Dirección vigente 2026-11-04 — C1 autorizado, implementación incremental

El [plan maestro](docs/SPOTLIGHT-NEXT-PLAN-MAESTRO-20261103.md) conserva los23
objetivos y10 checkpoints. Aprobación posterior expresa: implementar **sólo C1**
en la rama de PR21 desde faf84f0. Refinado en barra/listado/estructura; Adaptive
en inspectores y presentaciones existentes. Lista compacta sin aumentar altura,
hasta3 sugerencias, acento contenido al contexto, footer móvil discreto, foco
sutil también en claro y nomenclatura WhatsApp inequívoca. No añadir selector de
canal ni funciones previstas para C2–C9. No incorporar las capacidades dibujadas
para Spotify/citas como si estuvieran implementadas. Ver [alcance C1](docs/C1-IMPLEMENTATION-20261104.md).

Separar commits estructural, base visual y ajustes de inspector; conservar
búsqueda, teclado, borradores, permisos y NativeLaunchSession. Pruebas locales
nativas dirigidas/Lint y capturas comparables, sin APK, firma o Actions. C0 físico
sigue pendiente; JVM/Robolectric no lo cierra. No main/merge ni nuevas dependencias.

La APK60 ya fue entregada desde e9647c1, workflowc6d481c, run41581584988 correcto,
certificado Preview885838aa. La sección de preparación de firma inferior es
histórica: NO autoriza otro intento ni describe el estado vigente de la entrega.

## Firma Native60 — preparación, NO ejecutar Actions todavía

La APK aprobada debe compilarse desde `e9647c1329b93820383e3c8b4ec56dbceb3ce5f1`
(code60, funcionalidad f664484); el SHA de automatización se revisa aparte.
Prioridad actual: comprobar en Termux la copia histórica privada del ZIP0.9.1,
con clave original885838aa, sin enviar material privado al chat/cloud.
Ver `docs/NATIVE60-HISTORICAL-SIGNING-20261103.md` y kit público de preflight.
La ruta está en el dispositivo y aún no se comprobó su disponibilidad actual.

`mode=native60-once` está preparado y validado localmente, **sin autorización de
dispatch**. Sólo si la vía local no es viable, el propietario debe confirmar los
bindings/cuota y autorizar expresamente una ejecución del par SHA fuente/SHA
workflow revisado. Solicitudes anteriores de APK o un «seguí» no levantan esta
restricción. No usar el modo legacy56, no hacer re-run/otro dispatch ni eludir
las guardas. El operador conserva su propia reserva durante la ejecución y sólo
él la libera con notas. No copiar/exportar claves privadas o contraseñas.

## Dirección vigente — Preview 57 nativa (decisión 2026-11-03)

Leer primero [la decisión vigente](docs/NATIVE-PREVIEW57-DIRECTION-20261103.md)
y [su auditoría funcional](docs/AUDITORIA-NATIVA-PREVIEW57-20261103.md).
Base exacta `4fc33f2c918d3fe53a8bad7ac6571f2757630066`, referencia
`feat/antigravity-tablet-20261026`; rama independiente
`feat/preview57-native-return-20261103`. Esto prevalece sobre bases/prioridades
históricas incompatibles, sin reescribir checkpoints. No continuar desde
Preview58/59/PR20 ni incorporar Nuxt, assets web o WebView como interfaz.
Conservar Kotlin/Compose y las funcionalidades, datos, firma e identidad de57.
La auditoría inicial quedó publicada en1d58294. Continuación autorizada03/11:
incorporar selectivamente correcciones de PR19 con pruebas nativas dirigidas,
Lint y capturas; sin APK, Actions ni rediseño. Ver
`docs/verification/native57-menu-20261103/README.md`. El segundo checkpoint de
contratos Android fue autorizado y ejecutado; ver
`docs/CHECKPOINT-2-NATIVE-LAUNCH-CONTRACTS-20261103.md` y
`docs/verification/native-launch-20261103/README.md`. Mantener la revisión de
alternativas sin fallback automático, validación antes del efecto y autorización
no restaurable. No generalizar el fence local como garantía global de exactly-once. No implementar OpenUI/RAGFlow en este bloque.

Tres skills recuperadas de PR20 y **adaptadas a nativo**, sin su implementación:

- `.agents/skills/spotlight-api-contracts/SKILL.md`: contratos Kotlin, validación,
  acciones tipadas, capacidades, cancelación y resultados/errores honestos.
- `.agents/skills/spotlight-openui-forms/SKILL.md`: principios de formularios y
  resultados contextuales en Compose existente; no instalar React/Vue/OpenUI.
- `.agents/skills/spotlight-ragflow-documents/SKILL.md`: recuperar/citar primero
  desde FileIndex/OCR/SAF/semántica local. Servidor/proveedor/subidas remotos sólo
  con necesidad demostrada y consentimiento explícito separado.

No crear registros, motores ni frameworks paralelos por aplicar una skill.
Respetar reserva propia y presupuesto; no modificar main, ramas/PRs anteriores,
worktrees ajenos ni el checkpoint integral. Las normas siguientes conservan su
valor cuando son compatibles con la decisión vigente.

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

- Para búsqueda, inspector, Quick Look, formularios y acciones, leer `.agents/skills/spotlight-raycast-native/SKILL.md`. Foco/estado/pruebas/motion/rendimiento se cargan selectivamente; no instalan frameworks ni autorizan CI. Ver su revisión de adopción y compatibilidad antes de copiar APIs recientes.
