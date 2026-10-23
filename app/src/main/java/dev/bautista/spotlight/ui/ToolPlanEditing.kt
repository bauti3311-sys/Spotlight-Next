package dev.bautista.spotlight.ui

import dev.bautista.spotlight.ai.*

/** Index remapping keeps completed results attached to their original calls.
 * Editing never invokes a tool. Structural changes stop after execution starts. */
internal data class ToolPlanDraft(
    val plan: ToolPlan,
    val outcomes: Map<Int, ToolOutcome> = emptyMap(),
    val omitted: Set<Int> = emptySet(),
    val relativeVolumes: Set<Int> = emptySet()
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
    fun relativeVolume(index: Int, value: Boolean): ToolPlanDraft {
        require(structureEditable && plan.calls.getOrNull(index)?.name == "set_volume")
        return copy(relativeVolumes = if (value) relativeVolumes + index else relativeVolumes - index)
    }
    private fun reorder(order: List<Int>): ToolPlanDraft = copy(
        plan = plan.copy(calls = order.map { plan.calls[it] }),
        outcomes = order.mapIndexedNotNull { next, old -> outcomes[old]?.let { next to it } }.toMap(),
        omitted = order.mapIndexedNotNull { next, old -> next.takeIf { old in omitted } }.toSet(),
        relativeVolumes = order.mapIndexedNotNull { next, old -> next.takeIf { old in relativeVolumes } }.toSet()
    )
}

internal fun blankToolCall(definition: ToolDefinition) = ToolCall(definition.id,
    definition.fields.associateWith { definition.choices[it]?.firstOrNull().orEmpty() })

internal val ToolOmissionsSaver = androidx.compose.runtime.saveable.Saver<Set<Int>, ArrayList<Int>>(
    save = { ArrayList(it) }, restore = { it.filter { index -> index in 0..11 }.toSet() })

internal data class ReviewedToolCall(val call: ToolCall?, val dependency: Int? = null, val volume: Int? = null) {
    fun dependencyVerified(outcomes: Map<Int, ToolOutcome>) =
        dependency == null || outcomes[dependency]?.state == ExecutionState.VERIFIED
}

/** Project from a saved baseline, never from the previous projection or result text.
 * Explicitly omitted steps do not contribute; an invalid selected predecessor blocks
 * its relatives. Only a VERIFIED volume change can satisfy a relative dependency. */
internal fun projectToolVolumes(draft: ToolPlanDraft, baseline: Int): List<ReviewedToolCall> {
    var volume: Int? = baseline.coerceIn(0, 100)
    var previous: Int? = null
    return draft.plan.calls.mapIndexed { index, call ->
        if (call.name != "set_volume") return@mapIndexed ReviewedToolCall(call)
        val relative = index in draft.relativeVolumes
        val raw = if (call.arguments.keys == setOf("percent")) call.arguments["percent"]?.toIntOrNull() else null
        val value = if (relative) raw?.takeIf { it in -100..100 }?.let { delta ->
            volume?.let { (it + delta).coerceIn(0, 100) }
        } else raw?.takeIf { it in 0..100 }
        val review = ReviewedToolCall(value?.let { call.copy(arguments = mapOf("percent" to it.toString())) },
            if (relative) previous else null, value)
        if (index !in draft.omitted) { volume = value; previous = index }
        review
    }
}
