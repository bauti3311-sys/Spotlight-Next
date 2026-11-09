package dev.bautista.spotlight.ai

import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.toList
import org.junit.Test
import org.junit.Assert.*

/** Executor fixtures only; no real Android dispatch, account or device action. */
class C6PendingGateTest {
 private val calls=listOf(ToolCall("open_app",mapOf("package" to "fixture.app")),ToolCall("set_volume",mapOf("percent" to "40")))
 @Test fun preparedOrRequestedResultBlocksLaterEffects()=runBlocking {
  for(state in listOf(ExecutionState.PREPARED,ExecutionState.REQUESTED)){
   val executed=mutableListOf<String>()
   val runner=PlanExecution({executed+=it.name;ToolExecution("fixture",state=state)},{error("No Android action expected")})
   val events=runner.run(calls,setOf(0,1),true).toList()
   assertEquals("A pending effect must not authorize the next change",listOf("open_app"),executed)
   assertEquals(StepState.BLOCKED,events.last().state)
   assertTrue(events.last().message.contains("verific",ignoreCase=true))
  }
 }
 @Test fun dispatchAcceptedIsNotAnObservedExternalEffect()=runBlocking {
  val executed=mutableListOf<String>();var dispatches=0
  val runner=PlanExecution({executed+=it.name;ToolExecution("fixture prepared",Action.SystemPanel("device"))},{dispatches++})
  val events=runner.run(calls,setOf(0,1),true).toList()
  assertEquals(1,dispatches);assertEquals(listOf("open_app"),executed)
  assertEquals(StepState.REQUESTED,events[1].state);assertEquals(StepState.BLOCKED,events.last().state)
 }
 @Test fun verifiedLocalChangesMayContinue()=runBlocking {
  val executed=mutableListOf<String>();val runner=PlanExecution({executed+=it.name;ToolExecution("observed fixture",state=ExecutionState.VERIFIED)},{error("No dispatch")})
  val events=runner.run(calls,setOf(0,1),true).toList()
  assertEquals(listOf("open_app","set_volume"),executed);assertEquals(StepState.VERIFIED,events.last().state)
 }
}
