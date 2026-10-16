package dev.bautista.spotlight.ai

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import org.junit.Assert.*
import org.junit.Test

class PlanExecutionTest {
 private val calls=listOf(ToolCall("open_app",mapOf("package" to "test.app")),ToolCall("set_volume",mapOf("percent" to "40")))
 @Test fun missingConfirmationNeverExecutes()=runBlocking {
  var count=0;val runner=PlanExecution({count++;ToolExecution("ok")},{})
  assertNotNull(runCatching{runner.run(calls,setOf(0,1),false).toList()}.exceptionOrNull());assertEquals(0,count)
 }
 @Test fun failureBlocksRemainingSelectedChanges()=runBlocking {
  val executed=mutableListOf<String>();val runner=PlanExecution({executed+=it.name;error("package unavailable")},{})
  val events=runner.run(calls,setOf(0,1),true).toList()
  assertEquals(listOf("open_app"),executed)
  assertEquals(listOf(StepState.RUNNING,StepState.FAILED,StepState.BLOCKED),events.map{it.state})
 }
 @Test fun skippedStepDoesNotBlockAndObservedStateIsRetained()=runBlocking {
  val executed=mutableListOf<String>();val runner=PlanExecution({executed+=it.name;ToolExecution("observed 6/15",state=ExecutionState.VERIFIED)},{})
  val events=runner.run(calls,setOf(1),true).toList()
  assertEquals(listOf("set_volume"),executed);assertEquals(StepState.SKIPPED,events.first().state)
  assertEquals(StepState.VERIFIED,events.last().state)
 }
 @Test fun cancellationNeverStartsTheNextStep()=runBlocking {
  val executed=mutableListOf<String>();val runner=PlanExecution({executed+=it.name;throw CancellationException("fixture cancellation")},{})
  val failure=runCatching{runner.run(calls,setOf(0,1),true).toList()}.exceptionOrNull()
  assertTrue(failure is CancellationException);assertEquals(listOf("open_app"),executed)
 }
 @Test fun dispatchFailureNeverChangesVolume()=runBlocking {
  val executed=mutableListOf<String>()
  val runner=PlanExecution({executed+=it.name;ToolExecution("ready",dev.bautista.spotlight.search.Action.SystemPanel("device"))},{error("This action requires individual review")})
  val events=runner.run(calls,setOf(0,1),true).toList()
  assertEquals(listOf("open_app"),executed);assertEquals(StepState.FAILED,events[1].state);assertEquals(StepState.BLOCKED,events.last().state)
 }
 @Test fun existingSixStepLimitIsPreservedAndLargerPlansAreRejected()=runBlocking {
  var count=0;val runner=PlanExecution({count++;ToolExecution("ok")},{})
  val six=List(6){calls.last()};assertEquals(12,runner.run(six,six.indices.toSet(),true).toList().size)
  assertEquals(6,count)
  assertTrue(runCatching{runner.run(six+calls.last(),setOf(0),true).toList()}.isFailure);assertEquals(6,count)
 }
}
