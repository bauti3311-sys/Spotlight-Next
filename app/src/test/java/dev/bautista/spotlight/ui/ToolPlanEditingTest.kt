package dev.bautista.spotlight.ui

import dev.bautista.spotlight.ai.*
import org.junit.Assert.*
import org.junit.Test

class ToolPlanEditingTest {
    private val one=ToolCall("calculate",mapOf("expression" to "1+1"))
    private val two=ToolCall("create_note",mapOf("text" to "Revisar"))
    @Test fun addRemoveAndMovePreserveDraftArgumentsAndOmissionIdentity() {
        val initial=ToolPlanDraft(ToolPlan("Propuesta",listOf(one,two))).omit(1,true)
        val added=initial.add(one.copy(arguments=mapOf("expression" to "40+10")))
        val moved=added.move(1,0)
        assertEquals(setOf(0),moved.omitted)
        assertEquals(two,moved.plan.calls[0])
        val removed=moved.remove(1)
        assertEquals(listOf(two,added.plan.calls[2]),removed.plan.calls)
        assertEquals(setOf(0),removed.omitted)
        assertTrue(removed.outcomes.isEmpty())
    }
    @Test fun completedOperationsPreventStructuralReplay() {
        val draft=ToolPlanDraft(ToolPlan("",listOf(one,two)),mapOf(0 to ToolOutcome("2",ExecutionState.COMPLETED)))
        for(operation in listOf<()->Unit>({draft.remove(0)},{draft.move(1,0)},{draft.add(one)},{draft.omit(1,true)})) {
            try {operation();fail("Executed structure must stay protected")}catch(expected:IllegalArgumentException){}
        }
    }
    @Test fun sixStepLimitAndRegisteredFieldSchemaAreEnforced() {
        val full=ToolPlanDraft(ToolPlan("",List(6){one}))
        try{full.add(one);fail()}catch(expected:IllegalArgumentException){}
        try{full.copy(plan=ToolPlan("",emptyList())).add(one.copy(arguments=mapOf("shell" to "bad")));fail()}catch(expected:IllegalArgumentException){}
    }
    @Test fun removalOfLastStepLeavesAnEditableEmptyPlan() {
        assertTrue(ToolPlanDraft(ToolPlan("",listOf(one))).remove(0).plan.calls.isEmpty())
    }
    @Test fun blankDraftUsesOnlyDeclaredFieldsAndAllowedChoices() {
        val definition=ToolRegistry.definition("calendar_agenda")!!
        assertEquals(mapOf("period" to "today"),blankToolCall(definition).arguments)
        assertEquals(mapOf("expression" to ""),blankToolCall(ToolRegistry.definition("calculate")!!).arguments)
    }
}
