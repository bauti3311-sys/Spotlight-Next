package dev.bautista.spotlight.ai

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class ToolSequenceExecutionTest {
    private val calc=ToolCall("calculate",mapOf("expression" to "40+10"))
    private val volume=ToolCall("set_volume",mapOf("percent" to "0"))
    @Test fun readsRunInOrderAndEverySensitiveStepConsumesItsOwnConfirmation()=runBlocking {
        val calls=listOf(calc,volume,volume.copy(arguments=mapOf("percent" to "60")))
        val bindings=mapOf(1 to mapOf("percent" to ToolResultRef(0,"value")))
        val receipts=mutableMapOf<Int,ToolReceipt>();val performed=mutableListOf<ToolCall>();val trace=mutableListOf<Int>()
        val runner=ToolSequenceExecution{call->performed+=call;ToolExecution("local",state=ExecutionState.VERIFIED,outputs=mapOf((if(call.name=="calculate")"value" else "percent") to ToolValue(ToolValueType.NUMBER,if(call.name=="calculate")"50" else call.arguments.getValue("percent"))))}
        suspend fun advance(confirm:Int?=null)=runner.advance(calls,emptySet(),bindings,receipts,confirm,
            resolve={i,r->StructuredToolResults.resolve(i,calls,bindings,emptySet(),r)},started={trace+=it},
            completed={i,r->receipts[i]=ToolReceipt(r.state,r.outputs,r.action!=null)})
        assertEquals(SequenceDisposition.AWAIT_CONFIRMATION,advance().disposition);assertEquals(1,performed.size)
        assertEquals(2,advance(1).index);assertEquals("50",performed[1].arguments["percent"])
        assertEquals(SequenceDisposition.FINISHED,advance(2).disposition)
        assertEquals(listOf(0,1,2),trace)
        assertEquals(SequenceDisposition.FINISHED,advance().disposition);assertEquals(3,performed.size)
    }
    @Test fun intermediateFailureStopsLaterStepsWithoutRetryLoop()=runBlocking {
        var executed=0
        val runner=ToolSequenceExecution{executed++;if(executed==2)error("fixture failure");ToolExecution("done",state=ExecutionState.VERIFIED)}
        val gate=runner.advance(List(3){calc},emptySet(),emptyMap(),emptyMap(),resolve={_,_->calc},started={},completed={_,_->})
        assertEquals(SequenceDisposition.FAILED,gate.disposition);assertEquals(1,gate.index);assertEquals(2,executed)
    }
    @Test fun requestedExternalEffectBlocksContinuationAndCannotReplayFromReceipt()=runBlocking {
        var executed=0;val receipts=mutableMapOf<Int,ToolReceipt>()
        val call=ToolCall("control_media",mapOf("command" to "pause"))
        val runner=ToolSequenceExecution{executed++;ToolExecution("requested",state=ExecutionState.REQUESTED)}
        suspend fun advance()=runner.advance(listOf(call,calc),emptySet(),emptyMap(),receipts,0,resolve={i,_->if(i==0)call else calc},started={},completed={i,r->receipts[i]=ToolReceipt(r.state,r.outputs)})
        assertEquals(SequenceDisposition.PENDING_VERIFICATION,advance().disposition)
        assertEquals(SequenceDisposition.PENDING_VERIFICATION,advance().disposition)
        assertEquals(1,executed)
    }
    @Test fun cancellationEvenIfAdapterSwallowsItCannotPublishOrRunNextStep()=runBlocking {
        var started=0;var completed=0;val entered=CompletableDeferred<Unit>()
        val runner=ToolSequenceExecution{entered.complete(Unit);try{awaitCancellation()}catch(e:CancellationException){withContext(NonCancellable){ToolExecution("late",state=ExecutionState.VERIFIED)}}}
        val job=launch{runner.advance(listOf(calc,calc),emptySet(),emptyMap(),emptyMap(),resolve={_,_->calc},started={started++},completed={_,_->completed++})}
        entered.await();job.cancelAndJoin();assertEquals(1,started);assertEquals(0,completed)
    }
    @Test fun omittedStepsDoNotRunAndInvalidGraphFailsBeforeAnyExecution()=runBlocking {
        val performed=mutableListOf<Int>()
        val runner=ToolSequenceExecution{ToolExecution("done",state=ExecutionState.VERIFIED)}
        assertEquals(SequenceDisposition.FINISHED,runner.advance(List(3){calc},setOf(1),emptyMap(),emptyMap(),resolve={_,_->calc},started={performed+=it},completed={_,_->}).disposition)
        assertEquals(listOf(0,2),performed);performed.clear()
        val bad=mapOf(1 to mapOf("expression" to ToolResultRef(0,"value")))
        assertEquals(SequenceDisposition.FAILED,runner.advance(listOf(calc,calc),setOf(0),bad,emptyMap(),resolve={_,_->calc},started={performed+=it},completed={_,_->}).disposition)
        assertTrue(performed.isEmpty())
    }
    @Test fun confirmationForAnotherIndexCannotAuthorizeCurrentSensitiveAction()=runBlocking {
        var performed=0
        val runner=ToolSequenceExecution{performed++;ToolExecution("no")}
        val gate=runner.advance(listOf(volume,volume),emptySet(),emptyMap(),emptyMap(),1,resolve={_,_->volume},started={},completed={_,_->})
        assertEquals(SequenceDisposition.AWAIT_CONFIRMATION,gate.disposition);assertEquals(0,gate.index);assertEquals(0,performed)
    }
}
