package dev.bautista.spotlight.ui

import dev.bautista.spotlight.ai.*

/** Index remapping keeps completed results attached to their original calls.
 * Editing never invokes a tool. Structural changes stop after execution starts. */
internal data class ToolPlanDraft(
    val plan: ToolPlan,
    val outcomes: Map<Int, ToolOutcome> = emptyMap(),
    val omitted: Set<Int> = emptySet()
) {
    val structureEditable get() = outcomes.isEmpty()
    fun add(call: ToolCall): ToolPlanDraft {
        require(structureEditable && plan.calls.size < 6)
        val definition = ToolRegistry.definition(call.name) ?: error("Herramienta no registrada")
        require(call.arguments.keys == definition.fields.toSet())
        return copy(plan = plan.copy(calls = plan.calls + call))
    }
    fun remove(index: Int): ToolPlanDraft {
        require(structureEditable && index in plan.calls.indices)
        return reorder(plan.calls.indices.filter { it != index })
    }
    fun move(index: Int, destination: Int): ToolPlanDraft {
        require(structureEditable && index in plan.calls.indices && destination in plan.calls.indices)
        val order = plan.calls.indices.toMutableList()
        order.add(destination, order.removeAt(index))
        return reorder(order)
    }
    fun omit(index: Int, value: Boolean): ToolPlanDraft {
        require(structureEditable && index in plan.calls.indices)
        return copy(omitted = if (value) omitted + index else omitted - index)
    }
    private fun reorder(order: List<Int>): ToolPlanDraft = copy(
        plan = plan.copy(calls = order.map { plan.calls[it] }),
        outcomes = order.mapIndexedNotNull { next, old -> outcomes[old]?.let { next to it } }.toMap(),
        omitted = order.mapIndexedNotNull { next, old -> next.takeIf { old in omitted } }.toSet()
    )
}

internal fun blankToolCall(definition: ToolDefinition) = ToolCall(definition.id,
    definition.fields.associateWith { definition.choices[it]?.firstOrNull().orEmpty() })

internal val ToolOmissionsSaver = androidx.compose.runtime.saveable.Saver<Set<Int>, ArrayList<Int>>(
    save = { ArrayList(it) }, restore = { it.filter { index -> index in 0..11 }.toSet() })
