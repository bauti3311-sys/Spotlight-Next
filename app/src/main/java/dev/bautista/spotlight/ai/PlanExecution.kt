package dev.bautista.spotlight.ai

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.flow

enum class ExecutionState { COMPLETED, PREPARED, REQUESTED, VERIFIED }
enum class StepState { RUNNING, COMPLETED, PREPARED, REQUESTED, VERIFIED, SKIPPED, BLOCKED, FAILED }
data class PlanEvent(val index:Int,val state:StepState,val message:String)

/** Serial orchestration over the existing executor, never a second tool engine.
 * A failure stops later changes. Cancellation propagates and no step is replayed. */
class PlanExecution(private val execute:suspend(ToolCall)->ToolExecution,
                    private val dispatch:suspend(ToolExecution)->Unit) {
 fun run(calls:List<ToolCall?>,selected:Set<Int>,confirmed:Boolean)=flow {
  check(confirmed){"Revisá y confirmá el plan antes de ejecutarlo."}
  require(calls.size in 1..6 && selected.isNotEmpty() && selected.all{it in calls.indices}){"Plan fuera de los límites permitidos."}
  var stopped=false
  for(i in calls.indices) {
   currentCoroutineContext().ensureActive()
   if(i !in selected){emit(PlanEvent(i,StepState.SKIPPED,"Omitido"));continue}
   if(stopped){emit(PlanEvent(i,StepState.BLOCKED,"No ejecutado por el fallo anterior"));continue}
   emit(PlanEvent(i,StepState.RUNNING,"Ejecutando…"))
   val event=try {
    val result=execute(requireNotNull(calls[i]){"La capacidad dejó de estar disponible."})
    // A native/provider adapter can return after cancellation. Discard that result
    // before dispatching an Android action; cancellation is never a new authorization.
    currentCoroutineContext().ensureActive()
    if(result.action!=null)dispatch(result)
    val state=if(result.action!=null)StepState.REQUESTED else StepState.valueOf(result.state.name)
    val prefix=when(state){StepState.VERIFIED->"Verificado";StepState.REQUESTED->"Solicitud enviada";StepState.PREPARED->"Preparado";else->"Completado"}
    PlanEvent(i,state,"$prefix · ${if(result.action!=null)"Android recibió la apertura; la app receptora decide su estado." else result.text}")
   }catch(e:CancellationException){throw e}
   catch(e:Exception){stopped=true;PlanEvent(i,StepState.FAILED,"Falló · ${e.message.orEmpty().take(200)}")}
   emit(event)
  }
 }
}
