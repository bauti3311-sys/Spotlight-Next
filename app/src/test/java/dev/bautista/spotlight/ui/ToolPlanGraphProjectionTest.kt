package dev.bautista.spotlight.ui

import dev.bautista.spotlight.ai.*
import org.junit.Assert.*
import org.junit.Test

class ToolPlanGraphProjectionTest {
    private fun bright(value:String)=ToolCall("write_display_setting",mapOf("key" to "screen_brightness","value" to value))
    private val calc=ToolCall("calculate",mapOf("expression" to "40+10"))
    @Test fun deletingOrOmittingSourceNeverRebindsToADifferentStep() {
        val draft=ToolPlanDraft(ToolPlan("",listOf(calc,calc,bright("0")))).bind(2,"value",ToolResultRef(0,"value"))
        val removed=draft.remove(0)
        assertEquals(-1,removed.bindings[1]!!["value"]!!.step)
        assertTrue(runCatching{StructuredToolResults.validate(removed.plan.calls,removed.bindings,removed.omitted)}.isFailure)
        val omitted=draft.omit(0,true)
        assertTrue(runCatching{StructuredToolResults.validate(omitted.plan.calls,omitted.bindings,omitted.omitted)}.isFailure)
    }
    @Test fun reorderingRemapsSourceIdentityAndRejectsForwardDependency() {
        val draft=ToolPlanDraft(ToolPlan("",listOf(calc,calc,bright("0")))).bind(2,"value",ToolResultRef(0,"value"))
        val moved=draft.move(0,1)
        assertEquals(1,moved.bindings[2]!!["value"]!!.step)
        StructuredToolResults.validate(moved.plan.calls,moved.bindings,emptySet())
        val invalid=draft.move(2,0)
        assertTrue(runCatching{StructuredToolResults.validate(invalid.plan.calls,invalid.bindings,emptySet())}.isFailure)
    }
    @Test fun relativeBrightnessProjectsWithinRawBoundsAndTracksOnlySelectedPredecessors() {
        val draft=ToolPlanDraft(ToolPlan("",listOf(bright("102"),bright("10"),bright("-100"))),relativeBrightness=setOf(1,2))
        assertEquals(listOf("102","127","20"),projectToolSettings(draft,20,20).map{it.call!!.arguments["value"]})
        assertEquals("76",projectToolSettings(draft.omit(0,true),20,20)[1].call!!.arguments["value"])
        assertEquals(0,projectToolSettings(draft,20,20)[1].dependency)
        assertEquals(1,projectToolSettings(draft.move(1,2),20,20)[2].dependency)
    }
    @Test fun absentBaselineAndInvalidBrightnessPredecessorBlockRelativeExecution() {
        val noBase=ToolPlanDraft(ToolPlan("",listOf(bright("10"))),relativeBrightness=setOf(0))
        assertNull(projectToolSettings(noBase,20,null)[0].call)
        val invalid=ToolPlanDraft(ToolPlan("",listOf(bright("bad"),bright("10"))),relativeBrightness=setOf(1))
        assertNull(projectToolSettings(invalid,20,20)[1].call)
    }
    @Test fun observedHardwareValuesReplaceProjectedBaseWithoutParsingResultText() {
        val volume=ToolPlanDraft(ToolPlan("",listOf(ToolCall("set_volume",mapOf("percent" to "40")),ToolCall("set_volume",mapOf("percent" to "10")))),
            outcomes=mapOf(0 to ToolOutcome("not parsed 999",ExecutionState.VERIFIED,outputs=mapOf("percent" to ToolValue(ToolValueType.NUMBER,"33")))),relativeVolumes=setOf(1))
        assertEquals(43,projectToolSettings(volume,20,20)[1].volume)
        val brightness=ToolPlanDraft(ToolPlan("",listOf(bright("102"),bright("10"))),outcomes=mapOf(0 to ToolOutcome("untrusted text",ExecutionState.VERIFIED,outputs=mapOf("value" to ToolValue(ToolValueType.NUMBER,"76")))),relativeBrightness=setOf(1))
        assertEquals("102",projectToolSettings(brightness,20,20)[1].call!!.arguments["value"])
    }
    @Test fun typedBrightnessInputResolvesBeforeRelativeProjectionAndUnresolvedSourceBlocks() {
        val draft=ToolPlanDraft(ToolPlan("",listOf(calc,bright("0"),bright("10"))),relativeBrightness=setOf(2)).bind(1,"value",ToolResultRef(0,"value"))
        assertNull(projectToolSettings(draft,20,20)[2].call)
        val completed=draft.copy(outcomes=mapOf(0 to ToolOutcome("not used",ExecutionState.VERIFIED,outputs=mapOf("value" to ToolValue(ToolValueType.NUMBER,"102")))))
        assertEquals("127",projectToolSettings(completed,20,20)[2].call!!.arguments["value"])
    }
}
