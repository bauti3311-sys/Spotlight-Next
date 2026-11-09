# Dirección vigente — perfeccionar Preview 57 nativa

Decisión expresa del propietario, 2026-11-03. Esta decisión sustituye la dirección
experimental Nuxt/WebView y las prioridades históricas incompatibles; no borra su
historial ni declara inválida la evidencia de aquellas ramas.

- Repositorio: `bauti3311-sys/Spotlight-Next`.
- Referencia: `feat/antigravity-tablet-20261026`.
- Base **exacta**: `4fc33f2c918d3fe53a8bad7ac6571f2757630066`, Preview57.
- Desarrollo independiente: `feat/preview57-native-return-20261103`.
- Una barra flotante Kotlin/Jetpack Compose; identidad Obsidian Glass/Raycast,
  resultados claros, inspector contextual y navegación táctil/teclado existentes.
- Conservar búsqueda universal, acciones Android, Spotify, WhatsApp, documentos,
  IA, ajustes, permisos, datos e identidades de instalación/firma.
- No desarrollar desde Preview58/59 ni PR20. No reutilizar Nuxt, WebView, assets
  web ni su implementación de puente como capa visual.
- PR19 permanece intacto: revisar sus correcciones compatibles antes de una
  incorporación selectiva. En este primer entregable no se modifica la UI.
- De PR20 se recuperan **sólo tres skills y sus referencias**, adaptadas a nativo:
  API & Interface Design, OpenUI y RAGFlow. No es un cherry-pick de sus commits.
- API Design se aplica a tipos/validación/contratos/errores Kotlin existentes.
  OpenUI aporta principios de formularios restringidos, revisión y resultados
  estructurados en Compose; no componentes React/Vue ni nuevos motores visuales.
  RAGFlow es una investigación posterior: primero FileIndex/OCR/SAF/semántica
  local; proveedor remoto sólo ante necesidad probada y consentimiento explícito.
- Primer entregable: auditoría funcional y propuesta ordenada por impacto,
  antes de implementar formularios adicionales o RAGFlow. Sin nuevos servidores,
  dependencias, permisos, compilaciones, APK, firma o ejecuciones de Actions.
- Respetar `docs/AUTONOMIA-RESERVA.md`; no modificar main, checkpoints, PRs
  anteriores ni otros worktrees. El checkpoint integral no se promueve por esta
  auditoría. No afirmar presupuesto de Actions sin una fuente confiable.

## Lecturas y siguiente paso

[Auditoría y propuesta](AUDITORIA-NATIVA-PREVIEW57-20261103.md),
[evidencia de procedencia y límites](verification/native57-return-20261103/README.md).
Antes de la siguiente modificación productiva, volver a comprobar remoto y reserva,
elegir un bloque pequeño de esa propuesta y probar el flujo concreto. No repetir
un ensamblado de Preview57 sólo para recuperar una base que ya está en Git.


## Continuación autorizada — recuperación selectiva del PR19

La auditoría inicial se publicó en1d58294. El propietario autorizó después este
primer checkpoint de código: tres archivos del menú/barra de PR19, pruebas nativas
dirigidas, Lint y capturas comparables, sin APK/Actions. Resultado y límites en
[el informe](verification/native57-menu-20261103/README.md). El
[segundo checkpoint de contratos Android](CHECKPOINT-2-NATIVE-LAUNCH-CONTRACTS-20261103.md)
se prepara sin implementar; OpenUI y RAGFlow siguen fuera de este alcance.
