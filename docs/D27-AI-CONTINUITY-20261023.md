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

## D27 · proyección relativa y dependencias de volumen

El editor permite marcar set_volume como Cambio relativo. Es una edición explícita
local del usuario; el esquema del proveedor sigue proponiendo porcentajes absolutos.
Se conserva una base de volumen al crear el borrador y se proyectan solo los pasos
seleccionados:40 seguido de+10 se revisa50; omitir/quitar/reordenar/editar recalcula
sin acumular proyecciones. Límites0–100,delta−100–100 y rechazo de campos adicionales.
Un predecesor seleccionado inválido bloquea sus relativos; omitirlo explícitamente
usa la base o el anterior seleccionado. Cada relativo dependiente exige VERIFIED
del predecesor antes de habilitar su confirmación. REQUESTED/PREPARED/COMPLETED no
certifican volumen. No se interpretan textos de resultados como instrucciones.

ToolPlanRelativeTest6/6 y AiPlanRelativeFlowTest3/3, junto a las11regresiones
del editor/restauración:20/20,1m55s. Incluye confirmaciones separadas40y50,
cancelación bloqueando dependiente, persistencia sin replay y argumentos inválidos
no ejecutables. Tras limpiar el indicador antiguo al descartar una propuesta,
los2casos afectados de cancelación/restauración se repitieron y aprobaron.
Son20casos únicos,no22ni31. XML de rondas relative/final preservados.

LintDebug: intento offline falló al construir debugAndroidTestCompileClasspath
por dependencias de pruebas instrumentadas ausentes en caché (androidx.test.ext:junit
1.2.1,runner1.6.2 y transitivas). No se borraron tests ni se cambiaron versiones.
LintRaycastPreview terminó correctamente en2m57s:0errores,15advertencias,1informativa. No assemble ni nueva APK.

D27 sigue PARCIAL: pendientes dependencias/resultados tipados generales, brillo
relativo dentro de propuestas IA y ejecución secuencial general. El editor local
previo conserva brillo/volumen encadenados y PlanExecution con su evidencia.
No se afirma validación física ni interacción real con Gemini en estos fixtures.

## Cierre verificado

Código relativo publicado `6639a421cc08b9724ab435842efde8ce9aee2b49`;
235entradas productivas/configuración cotejadas por GitSHA1 contra árbol remoto.
Resumen y lint XML actuales en verification/d27-ai-20261023/final.
Fixture3/3 y20casos únicos de edición/restauración/proyección correctos; dos
regresiones de cancelación repetidas después del último ajuste, sin sumar repeticiones.
PR18 sigue borrador, notas intactas, sin procesos Gradle activos al cerrar.
La reserva propia conserva el checkpoint publicado para continuidad de esta sesión.
Próxima tarea concreta: dependencias tipadas de resultados entre herramientas;
D27 sigue parcial y no se atribuye ejecución secuencial general a este editor.
