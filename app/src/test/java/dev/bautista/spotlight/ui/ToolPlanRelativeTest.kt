package dev.bautista.spotlight.ui

import dev.bautista.spotlight.ai.*
import org.junit.Assert.*
import org.junit.Test

class ToolPlanRelativeTest {
    private fun volume(percent:String)=ToolCall("set_volume",mapOf("percent" to percent))
    private val draft get()=ToolPlanDraft(ToolPlan("",listOf(volume("40"),volume("10"))),relativeVolumes=setOf(1))
    @Test fun fortyPlusTenProjectsFiftyAndRequiresVerifiedPredecessor() {
        val review=projectToolVolumes(draft,20)
        assertEquals(listOf(40,50),review.map{it.volume})
        assertEquals("50",review[1].call!!.arguments["percent"])
        assertEquals(0,review[1].dependency)
        assertFalse(review[1].dependencyVerified(emptyMap()))
        for(state in listOf(ExecutionState.COMPLETED,ExecutionState.REQUESTED,ExecutionState.PREPARED))
            assertFalse(review[1].dependencyVerified(mapOf(0 to ToolOutcome("Not verified",state))))
        assertTrue(review[1].dependencyVerified(mapOf(0 to ToolOutcome("Observed",ExecutionState.VERIFIED))))
    }
    @Test fun omittedRemovedAndReorderedStepsRecalculateFromTheSavedBaseline() {
        assertEquals(30,projectToolVolumes(draft.omit(0,true),20)[1].volume)
        val removed=draft.remove(0)
        assertEquals(setOf(0),removed.relativeVolumes)
        assertEquals(30,projectToolVolumes(removed,20)[0].volume)
        val moved=draft.move(1,0)
        assertEquals(listOf(30,40),projectToolVolumes(moved,20).map{it.volume})
        assertNull(projectToolVolumes(moved,20)[0].dependency)
        assertEquals(projectToolVolumes(draft,20),projectToolVolumes(draft,20))
    }
    @Test fun invalidSelectedPredecessorBlocksDependentRatherThanBeingSilentlyOmitted() {
        val invalid=draft.copy(plan=draft.plan.copy(calls=listOf(volume(""),volume("10"))))
        assertNull(projectToolVolumes(invalid,20)[1].call)
        assertEquals(30,projectToolVolumes(invalid.omit(0,true),20)[1].volume)
    }
    @Test fun relativeValuesClampAndDoNotAccumulateAcrossRepeatedProjection() {
        val chain=ToolPlanDraft(ToolPlan("",listOf(volume("95"),volume("10"),volume("-100"))),relativeVolumes=setOf(1,2))
        assertEquals(listOf(95,100,0),projectToolVolumes(chain,20).map{it.volume})
        assertEquals(listOf(95,100,0),projectToolVolumes(chain,20).map{it.volume})
        assertNull(projectToolVolumes(chain.copy(plan=ToolPlan("",listOf(volume("101")))),20)[0].call)
    }
    @Test fun extraFieldsAreRejectedAndRelativeModeCannotApplyToOtherTools() {
        val bad=ToolPlanDraft(ToolPlan("",listOf(ToolCall("set_volume",mapOf("percent" to "40","unsafe" to "x")))))
        assertNull(projectToolVolumes(bad,20)[0].call)
        try{ToolPlanDraft(ToolPlan("",listOf(ToolCall("calculate",mapOf("expression" to "2+2"))))).relativeVolume(0,true);fail()}
        catch(expected:IllegalArgumentException){}
    }
    @Test fun editingPreviousAbsoluteValueChangesTheDependentTarget() {
        val edited=draft.copy(plan=draft.plan.copy(calls=listOf(volume("70"),volume("10"))))
        assertEquals(80,projectToolVolumes(edited,20)[1].volume)
    }
}
