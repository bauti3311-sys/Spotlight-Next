# Continuidad funcional y recuperación administrativa · 23/10/2026

Base `bf674880`, rama `feat/raycast-sidebar-permissions-20261020`, PR18 borrador.

La autorización excepcional del propietario se aplicó mediante el conector GitHub:
`create_commit(parent_sha)` y `update_ref(expected_sha, force=false)`, alternativa
expresamente autorizada en su instrucción de desbloqueo final. La garantía de rechazo
del SHA esperado se basa en el contrato publicado de esa herramienta; no se afirma
haber ejecutado una negociación Git por terminal. `gh` no está instalado, Git CLI no
está autenticado y no se guardaron ni solicitaron credenciales.

Registro anterior `b7788d6a1479727bf4bb80de8ef8d61c12277e56`; nuevo registro inicial
`9ddf781a841760621f1045f7f2aacc7c8d89a40a`, con el anterior como padre y árbol intacto.
Propietario nuevo `work-admin-20261023-58a89fd0-a03f-4918-8a41-056edbc1fa34`.
La metadata conserva identidad/SHA previos, motivo, autorización puntual, checkpoint,
notas observadas y transporte. Se verificó la referencia remota después de actualizar.
Las notas `f6cb4e3f3678a71c7a4fcb87c74f6c27ff7a3df9` se conservaron. No release con
identidad ajena, borrado, force, main ni merge. Las nueve pruebas administrativas
locales ya aprobadas no se repitieron ni se presentan como pruebas del conector.

Fixture de ContentProvider publicado por separado en `cc6b4f65d1a7b7cd5574f72c394c0a751ed22897`.
Respeta la proyección solicitada y mantiene las aserciones de extracción/deduplicación.
`SharedInputActivityTest`: 3/3 aprobadas nuevamente, gate dirigido 15s. Las dos copias
locales y evidencia previa siguen preservadas.

## D27 · editor de propuestas

Añadir herramientas disponibles del registro, quitar incluso el último paso, subir,
bajar y omitir pasos. Los argumentos siguen editables y las omisiones se restauran
con el borrador del contexto; nunca se ejecutan al editar/restaurar. Máximo seis pasos,
campos registrados y validación real del executor conservados. No proveedores nuevos
ni envío remoto al añadir. La estructura se protege después de ejecutar la primera
acción para conservar la correspondencia de resultados y evitar replay. Cada acción
mantiene su confirmación y su revisión de permisos al ejecutarse.

Pruebas nuevas: ToolPlanEditingTest 5/5 y AiPlanEditingFlowTest 2/2. Regresión de
cancelación/restauración/aislamiento ToolTaskRecoveryTest 4/4. Total 11/11 del gate,
1m49s, Kotlin/Java Debug y tests compilados. XML en verification/d27-ai-20261023/editor.
No equivalen a interacción física ni a llamadas reales a Gemini.

D27 permanece PARCIAL: falta ampliar la proyección de relativos/dependencias dentro
de propuestas IA y cubrir dependencias/resultados generales. Los cálculos relativos
del editor local previo conservan su evidencia; no se afirma haberlos repetido aquí.
La matriz sigue 8 completas,25 implementadas sin pruebas suficientes,7 parciales.

Preview44 permanece inalterada y corresponde a `bf674880`, no al editor nuevo:
APK SHA256 `faabb9a6cbcf577ee32f6e994463adb8c254869a74563cef608709c740e55949`.
No se recompiló ni se le atribuyen estas funciones nuevas. No validación física,
servicios externos, benchmark, nuevo certificado o permiso sensible.
