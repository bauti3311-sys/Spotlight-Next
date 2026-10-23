package dev.bautista.spotlight.ai

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

enum class SequenceDisposition { FINISHED, AWAIT_CONFIRMATION, PENDING_VERIFICATION, FAILED }
data class SequenceGate(val disposition: SequenceDisposition, val index: Int = -1, val message: String = "")

/** One explicit advance, never an automatic restore/retry loop. Read-only local
 * steps may continue; each sensitive action consumes its own confirmation. */
class ToolSequenceExecution(private val execute: suspend (ToolCall) -> ToolExecution) {
    suspend fun advance(
        calls: List<ToolCall>, omitted: Set<Int>, bindings: ToolBindings,
        initialReceipts: Map<Int, ToolReceipt>, confirmedIndex: Int? = null,
        resolve: (Int, Map<Int, ToolReceipt>) -> ToolCall,
        started: suspend (Int) -> Unit,
        completed: suspend (Int, ToolExecution) -> Unit
    ): SequenceGate {
        require(calls.size in 1..6 && omitted.all { it in calls.indices })
        try { StructuredToolResults.validate(calls, bindings, omitted) }
        catch (e: Exception) { return SequenceGate(SequenceDisposition.FAILED, message=e.message.orEmpty()) }
        val receipts=initialReceipts.toMutableMap()
        for (i in calls.indices) {
            currentCoroutineContext().ensureActive()
            if (i in omitted) continue
            val definition=ToolRegistry.definition(calls[i].name)
                ?: return SequenceGate(SequenceDisposition.FAILED,i,"Herramienta no registrada.")
            val receipt=receipts[i]
            if (receipt != null) {
                if (receipt.pendingAction || receipt.state in setOf(ExecutionState.PREPARED,ExecutionState.REQUESTED) ||
                    (definition.risk != ToolRisk.READ && receipt.state != ExecutionState.VERIFIED))
                    return SequenceGate(SequenceDisposition.PENDING_VERIFICATION,i,"El efecto de este paso todavía no está verificado.")
                continue
            }
            val call=try { resolve(i,receipts).also { ToolRegistry.decode(it) } }
                catch(e:Exception) { return SequenceGate(SequenceDisposition.FAILED,i,e.message.orEmpty()) }
            if (definition.risk != ToolRisk.READ && confirmedIndex != i)
                return SequenceGate(SequenceDisposition.AWAIT_CONFIRMATION,i,"Revisá y confirmá este paso.")
            try {
                started(i)
                val result=execute(call)
                currentCoroutineContext().ensureActive()
                completed(i,result)
                receipts[i]=ToolReceipt(result.state,result.outputs,result.action!=null)
                if (result.action!=null || result.state in setOf(ExecutionState.PREPARED,ExecutionState.REQUESTED) ||
                    (definition.risk != ToolRisk.READ && result.state != ExecutionState.VERIFIED))
                    return SequenceGate(SequenceDisposition.PENDING_VERIFICATION,i,"Paso preparado o solicitado; no se confirma su efecto externo.")
            } catch(e:CancellationException) { throw e }
            catch(e:Exception) { return SequenceGate(SequenceDisposition.FAILED,i,e.message.orEmpty().take(200)) }
        }
        return SequenceGate(SequenceDisposition.FINISHED,message="Plan terminado; estados de cada paso conservados.")
    }
}
