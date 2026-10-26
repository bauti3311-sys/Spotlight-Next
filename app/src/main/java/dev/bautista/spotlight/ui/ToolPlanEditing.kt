package dev.bautista.spotlight.ui

import dev.bautista.spotlight.ai.*

/** Index remapping keeps completed results attached to their original calls.
 * Editing never invokes a tool. Structural changes stop after execution starts. */
internal data class ToolPlanDraft(
    val plan: ToolPlan,
    val outcomes: Map<Int, ToolOutcome> = emptyMap(),
    val omitted: Set<Int> = emptySet(),
    val relativeVolumes: Set<Int> = emptySet(),
    val bindings: ToolBindings = emptyMap(),
    val relativeBrightness: Set<Int> = emptySet()
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
    fun relativeBrightness(index: Int, value: Boolean): ToolPlanDraft {
        require(structureEditable && plan.calls.getOrNull(index)?.let { it.name == "write_display_setting" && it.arguments["key"] == "screen_brightness" } == true)
        return copy(relativeBrightness = if (value) relativeBrightness + index else relativeBrightness - index)
    }
    fun bind(index: Int, argument: String, ref: ToolResultRef): ToolPlanDraft {
        require(structureEditable)
        val next = copy(bindings = bindings + (index to (bindings[index].orEmpty() + (argument to ref))))
        StructuredToolResults.validate(plan.calls, next.bindings, omitted)
        return next
    }
    fun unbind(index: Int, argument: String): ToolPlanDraft {
        require(structureEditable)
        return copy(bindings = bindings + (index to (bindings[index].orEmpty() - argument)))
    }
    private fun reorder(order: List<Int>): ToolPlanDraft {
        val positions = order.withIndex().associate { it.value to it.index }
        return copy(
        plan = plan.copy(calls = order.map { plan.calls[it] }),
        outcomes = order.mapIndexedNotNull { next, old -> outcomes[old]?.let { next to it } }.toMap(),
        omitted = order.mapIndexedNotNull { next, old -> next.takeIf { old in omitted } }.toSet(),
        relativeVolumes = order.mapIndexedNotNull { next, old -> next.takeIf { old in relativeVolumes } }.toSet(),
        relativeBrightness = order.mapIndexedNotNull { next, old -> next.takeIf { old in relativeBrightness } }.toSet(),
        bindings = order.mapIndexedNotNull { next, old -> bindings[old]?.let { fields -> next to fields.mapValues { (_, ref) -> ref.copy(step = positions[ref.step] ?: -1) } } }.toMap()
        )
    }
}

internal fun blankToolCall(definition: ToolDefinition) = ToolCall(definition.id,
    definition.fields.associateWith { definition.choices[it]?.firstOrNull().orEmpty() })

internal val ToolOmissionsSaver = androidx.compose.runtime.saveable.Saver<Set<Int>, ArrayList<Int>>(
    save = { ArrayList(it) }, restore = { it.filter { index -> index in 0..11 }.toSet() })

internal data class ReviewedToolCall(val call: ToolCall?, val dependency: Int? = null, val volume: Int? = null) {
    fun dependencyVerified(outcomes: Map<Int, ToolOutcome>) =
        dependency == null || (outcomes[dependency]?.state == ExecutionState.VERIFIED && outcomes[dependency]?.review == false)
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
        if (index !in draft.omitted) {
            volume = draft.outcomes[index]?.takeIf { it.state == ExecutionState.VERIFIED }?.outputs?.get("percent")?.value?.toIntOrNull()?.takeIf { it in 0..100 } ?: value
            previous = index
        }
        review
    }
}

internal fun ToolPlanDraft.receipts() = outcomes.mapValues { (_, result) -> ToolReceipt(result.state, result.outputs, result.review) }

/** Resolve typed inputs before projecting relative adjustments, keeping missing
 * dependencies as invalid drafts rather than silently using literal arguments. */
internal fun projectToolSettings(draft: ToolPlanDraft, volumeBaseline: Int, brightnessBaseline: Int?): List<ReviewedToolCall> {
    val resolved = draft.plan.calls.mapIndexed { i, call ->
        if (draft.bindings[i].isNullOrEmpty()) call else runCatching {
            StructuredToolResults.resolve(i, draft.plan.calls, draft.bindings, draft.omitted, draft.receipts())
        }.getOrNull()
    }
    val effective = draft.copy(plan = draft.plan.copy(calls = resolved.mapIndexed { i, call -> call ?: draft.plan.calls[i].copy(arguments = emptyMap()) }))
    val volumes = projectToolVolumes(effective, volumeBaseline)
    var brightness = brightnessBaseline
    var previous: Int? = null
    return volumes.mapIndexed { i, row ->
        val rawCall = effective.plan.calls[i]
        if (resolved[i] == null) {
            if(i !in draft.omitted && draft.plan.calls[i].name=="write_display_setting" && draft.plan.calls[i].arguments["key"]=="screen_brightness") {brightness=null;previous=i}
            return@mapIndexed row.copy(call = null)
        }
        if (rawCall.name != "write_display_setting" || rawCall.arguments["key"] != "screen_brightness") return@mapIndexed row
        if(rawCall.arguments.keys!=setOf("key","value")) {
            if(i !in draft.omitted){brightness=null;previous=i}
            return@mapIndexed row.copy(call=null)
        }
        val relative = i in draft.relativeBrightness
        val raw = rawCall.arguments["value"]?.toIntOrNull()
        val percent = if (relative) raw?.takeIf { it in -100..100 }?.let { delta -> brightness?.let { (it + delta).coerceIn(8,100) } }
            else raw?.takeIf { it in 20..255 }?.let { (it*100+254)/255 }
        val value = if (relative) percent?.let { (it*255/100).coerceIn(20,255) } else raw?.takeIf { it in 20..255 }
        val reviewed = ReviewedToolCall(value?.let { rawCall.copy(arguments=mapOf("key" to "screen_brightness","value" to it.toString())) },if(relative)previous else null,percent)
        if(i !in draft.omitted) {
            brightness = draft.outcomes[i]?.takeIf { it.state == ExecutionState.VERIFIED }?.outputs?.get("value")?.value?.toIntOrNull()?.takeIf { it in 20..255 }?.let { (it*100+254)/255 } ?: percent
            previous=i
        }
        reviewed
    }
}

internal val ToolBindingsSaver = androidx.compose.runtime.saveable.Saver<ToolBindings,String>(
    save = { StructuredToolResults.encodeBindings(it) },
    restore = { runCatching { StructuredToolResults.decodeBindings(it) }.getOrDefault(mapOf(-1 to emptyMap())) })
