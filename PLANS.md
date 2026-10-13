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

## Checkpoint F1.2 — densidad y vista previa

Continuación aislada desde `checkpoint/autonomous-next` = `2a5607e`, rama `feat/density-preview-f1`, [PR #9 borrador](https://github.com/bauti3311-sys/Spotlight-Next/pull/9) dirigido a infraestructura. Producto hasta `50a5bf2`: espaciado explícitamente independiente del ancho, automático antiguo explicado, vista previa con filas reales y geometría compartida. Cambiar/restaurar el borrador no persiste; aplicar conserva los anchos y datos.

Local: 340 unitarias sin fallos/errores/omisiones, siete tests específicos finales correctos, 17 comparaciones nativas (12 referencias anteriores intactas), Python 5+12, lint 0 errores/10 advertencias y assembleDebug correctos. Antes/después/diff reales en `docs/visual/f1.2`; cinco PNG nuevos de densidad/preview en snapshots. Sin dependencias nuevas; debug local conserva 177738334 bytes (release/rendimiento no medidos). CI conserva ahora el informe unitario completo antes de que Roborazzi lo reemplace.

La verificación remota final se consulta en checks/descripción PR #9 y `git notes --ref=spotlight-autonomy show <SHA>`; el puntero duradero solo avanzará tras los checks verdes. La reserva propia permanece activa hasta ese cierre; leer su JSON y no editar mientras exista. Si hay interrupción, retomar esta rama/checkpoint pendiente antes de otro subhito.

Siguiente F1.3: revisar métricas actuales/insets/IME en OverlayService/AdaptiveLayout, definir casos pantalla usable/rotación y límites, reproducir antes de corregir. Prueba física Galaxy Tab A9+ separada; no afirmar F1 completo ni One UI validado. Sin main/merges/firma/releases; preservar PRs históricos.

### Cierre remoto F1.2 pendiente

CI 38357821467 falló en una espera ActionHub existente (339/340 correctos; resto de jobs correcto). Ver último evento de PROGRESO; reserva aún activa. Fixtures hacen progresar el reloj Android pausado conservando plazo/aserciones; seis específicas pasaron inicialmente. Logs verbose solo tests/CI, sin dependencias/runtime nuevo. Resultado definitivo de repeticiones/suite/píxeles/CI en notas del SHA y descripción PR #9; no retomar F1.3 si ese cierre falta.

### Cierre local definitivo F1.2

Producto 14fc5e9925f1300d6373a5b6660ec43ee40eac71: una reproducción determinista mostró pérdida de resultados al editar y restaurar texto en un mismo frame; invalidaciones con generación y observación atómica de consulta/contexto la corrigen. No se añadió flowOn ni se cambiaron teclas/Quick Look. RED->GREEN y 25 ciclos de navegación, 341 unitarias, 17 píxeles, lint y debug correctos. Sin nuevas dependencias; las trazas temporales no están en fuente ni APK.

CI de 45ac6c7 quedó verde, pero no resuelve por sí solo la repetición local posterior; validar el SHA final con esta corrección, consultar nota/descripcion PR #9 y mantener reserva hasta cerrar. El puntero solo avanza por FF tras CI correcto. Siguiente F1.3, salvo fallo que obligue a retomar este checkpoint.

## Prioridad de reanudación — checkpoint F1.2 pendiente

CI final del producto e0639d8 / run38366740355 falló en menú ActionHub: 340/341 correctos, otros cuatro jobs correctos. Local pasó341 y17 píxeles, pero no declarar resuelto el fallo intermitente de contexto. F1.2 permanece pendiente de cierre integral; retomar punta feat/density-preview-f1 / PR #9 y su nota antes de F1.3. No volver al árbol viejo ni repetir UI/densidad ya implementadas. Puntero validado permanece2a5607e; reserva propia liberada al cerrar (comprobar remoto/nota). Diagnosticar por qué el menú queda sin filas con consulta vacía; mantener todas las condiciones, 25 ciclos, reproducción de texto y 15 s.

## Reanudación F1.2 — ActionHub
Rama fix/action-hub-f12-continuation desde 424757d; reserva scheduled-f9a4844c-6b36-431f-a6cf-9cc0f3a5403c. Retomar pérdida de filas contextual confirmada en CI 38368556541 antes de F1.3. Mantener 25 ciclos y todas las aserciones; estado/evidencia final en progreso y notas.

## Checkpoint diagnóstico F1.2 — PR #10
Rama fix/action-hub-f12-continuation, base 424757d. Diario opt-in acotado a 160 eventos, sin consultas/tópicos/URIs/claves; informe de fallo con fases, identidad de estado, snapshots, recomposer y lifecycle. Default desactivado; no se evalúan trazas detalladas en uso normal.
Se reprodujo pérdida de presentación después de recibir/asignar filas. Misma instancia de estado, Activity RESUMED/visible, recomposer Idle sin trabajo pendiente. Notificación explícita, snapshot atómico y cambiar el reloj del fixture NO resolvieron el fallo; cambios experimentales retirados.
341 pruebas locales finales: 339 correctas, 2 timeouts (ActionHub y Next110), sin omisiones/aserciones reducidas. CI diagnóstico 490f3d8 /38389843466 tuvo cinco jobs correctos, pero no demuestra causa resuelta. F1.2 continúa abierto y checkpoint/autonomous-next conserva2a5607e. Calidad/píxeles y CI final se registran en nota del SHA y descripción PR. Retomar PR #10 antes de F1.3: aislar publicación y observación/invalidation de resultados, conservando25 ciclos/15s.

## F1.2 — corrección del panel bajo verificación final
Producto5ba1e5d: BoxWithConstraints observa resultados directamente; filas/selección/acción principal/Quick Look se calculan en la subcomposición que los presenta. Reproducción RED antes y341/341 en dos suites locales después (la final mantiene25 ciclos/15s y refuerza ausencia de permisos en menú). No se conservan prototipos anteriores. Lint/debug final correctos, repetición final forzada341/341 correcta; 17/17 píxeles finales correctos; CI final todavía pendiente.
Native PDF workflow d4528c1 permite PRs dirigidos a infraestructura y cambiosSpotlightPanel, con permisos de lectura; ejecutar renderer real víaCI sin main/merge/release. Captura después y diff en docs/diagnostics. Actualizar puntero por FF únicamente si todos los checks finales son correctos; resultado final y siguiente subhito en nota del SHA/PR #10. Si falla, retomar este checkpoint primero.

## F1.2 verificado — cierre documental
Producto c6b357cf1aeb6cb82d3e3b6890b8c1df172f2ff1 validado integralmente: CI38396241075 cinco jobs correctos; PDF38396240951 tres instrumentadas Android10/API29 correctas. Local341/341 final y repetición341/341,17 píxeles,17 Python,lint0/10 y debug. El enlace local de datos/selección/Quick Look resuelve la reproducción del menú; condiciones/plazos y25 ciclos conservados/reforzados.
Este checkpoint de documentación cierra F1.2; comprobar sus checks y nota final. Mantener código probado sin cambios. Puntero avanza solo por FF tras cierre seguro; liberar únicamente reserva propia. Siguiente subhito F1.3: reproducir métricas actuales/insets/IME en OverlayService/AdaptiveLayout, conservar ancho/manual/horizontal/densidad y evaluar teléfono/tablet/orientaciones. One UI físico sigue separado. No empezar otra función en este ciclo.

## 2026-10-13T13:10:20.312752-03:00 — F1.3, retención de inset en actualización de geometría

Reserva exclusiva scheduled-500ee0a9-63fc-434a-a85c-017941618de1, fda4e47; rama fix/ime-inset-retention-f13 desde d95401b verificado (CI38398146928 y PDF38398146856 correctos). Estado EN CURSO: reproducir pérdida de altura restringida cuando ancho/preferencias/configuración refrescan sin nuevo evento de insets. Test nuevo OverlayImeHeightTest conserva assertions y simula teclado visible, actualización y ocultación explícita. Criterio: recordar último inset hasta el próximo evento real; no cambiar ancho/densidad/package/firma. WindowMetrics, barras/áreas seguras y validación física siguen pendientes; no marcar F1.3 completo. No dependencias nuevas, APIs pagas, main/merges/releases. Resultado definitivo y cierre de reserva en nota/PR.

Reproducción RED ejecutada: :app:testDebugUnitTest --tests '*OverlayImeHeightTest', 1/1 falló, expected229.0/actual409.0 tras refresh sin inset. Corrección: lastBottomInset persiste entre eventos; cero explícito restaura altura. Cobertura adicional tablet800x1280 vertical/1280x800 horizontal Robolectric (no emulador ni One UI físico). Python backend5/5 y scripts12/12 correctos offline. Unitarias completas/lint/debug en curso; no afirmar verde todavía. No baselines modificados; comparación nativa final pendiente.

### Checkpoint local F1.3 parcial — PR #11
Producto5942483: regresión RED->GREEN (229dp se conservan frente a409dp anteriores); tres casos teléfono/tablet vertical/horizontal pasan. Suite :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --no-daemon --max-workers=2 --console=plain:344/344,0errores/omisiones,lint0errores/10advertencias,debug correcto. :app:verifyRoborazziDebug --tests '*VisualSnapshotTest' ...:17/17 comparaciones nativas correctas, ningún baseline cambiado. Python unittest discover backend5/scripts12 correctos, offline. XML funcional conservado antes de Roborazzi; logs locales f13-red/quality/visual en scratch y evidencia remota en CI/artefactos. Sin dependencias nuevas ni licencias añadidas. No nuevas capturas específicas de IME/One UI: la cobertura de geometría usa Robolectric, comparaciones existentes y Maestro remoto; no confundirla con validación física.
CI producto38428647104 y PDF38428646699 en curso al escribir; resultado final del checkpoint documental en git notes --ref=spotlight-autonomy y descripción PR #11. Puntero avanza únicamente tras verificar checks; si pendientes/fallan, retomar esta rama propia antes de otro trabajo. Reserva propia se libera al cerrar, comprobar remoto/nota. F1.3 permanece sin marcar completo: siguiente acción revisar WindowMetrics con fallback/API, métricas y áreas seguras con IME/rotación, conservar anchos manual/horizontal/densidad y preview. Main/PRs históricos/checkpoints preservados; sin merge, firma ni release.
