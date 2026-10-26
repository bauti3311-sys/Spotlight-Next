package dev.bautista.spotlight.ai

import org.json.*

enum class ToolValueType { NUMBER, TEXT }
data class ToolValue(val type: ToolValueType, val value: String) {
    init {
        require(value.length <= 8000 && '\u0000' !in value)
        if (type == ToolValueType.NUMBER) require(value.matches(Regex("-?\\d{1,30}(?:\\.\\d{1,12})?")))
    }
}
data class ToolResultRef(val step: Int, val field: String)
typealias ToolBindings = Map<Int, Map<String, ToolResultRef>>
data class ToolReceipt(val state: ExecutionState, val outputs: Map<String, ToolValue>, val pendingAction: Boolean = false)

/** Output fields are declared by local adapters, never extracted from display text. */
object StructuredToolResults {
    fun outputTypes(call: ToolCall): Map<String, ToolValueType> = when (call.name) {
        "calculate" -> mapOf("value" to ToolValueType.NUMBER)
        "transform_text" -> mapOf("text" to ToolValueType.TEXT)
        "set_volume" -> mapOf("percent" to ToolValueType.NUMBER)
        "read_display_setting", "write_display_setting" -> mapOf("value" to ToolValueType.NUMBER)
        else -> emptyMap()
    }
    fun inputType(call: ToolCall, field: String): ToolValueType? = when {
        call.name == "calculate" && field == "expression" -> ToolValueType.NUMBER
        call.name == "set_volume" && field == "percent" -> ToolValueType.NUMBER
        call.name == "write_display_setting" && field == "value" -> ToolValueType.NUMBER
        call.name in setOf("transform_text", "create_note", "prepare_message") && field == "text" -> ToolValueType.TEXT
        call.name in setOf("search_apps", "search_files", "search_contacts") && field == "query" -> ToolValueType.TEXT
        else -> null
    }
    fun number(value: String) = runCatching { ToolValue(ToolValueType.NUMBER, value) }.getOrNull()
    fun text(value: String) = runCatching { ToolValue(ToolValueType.TEXT, value) }.getOrNull()

    fun validate(calls: List<ToolCall>, bindings: ToolBindings, omitted: Set<Int>) {
        require(calls.size <= 6 && bindings.size <= 6)
        val visiting = mutableSetOf<Int>(); val visited = mutableSetOf<Int>()
        fun visit(index: Int) {
            check(index !in visiting) { "Hay un ciclo entre los pasos." }
            if (!visited.add(index)) return
            visiting += index
            bindings[index].orEmpty().values.forEach { if (it.step in calls.indices) visit(it.step) }
            visiting -= index
        }
        calls.indices.filter { it !in omitted }.forEach(::visit)
        bindings.forEach { (target, fields) ->
            require(target in calls.indices && fields.size <= 8) { "Paso de destino inexistente." }
            if (target !in omitted) fields.forEach { (argument, ref) ->
                require(ref.step in calls.indices && ref.step !in omitted) { "Falta una dependencia obligatoria o está omitida." }
                require(ref.step < target) { "La dependencia debe estar antes del paso que la utiliza." }
                val expected = inputType(calls[target], argument)
                require(expected != null && expected == outputTypes(calls[ref.step])[ref.field]) { "Tipos de resultado y argumento incompatibles." }
            }
        }
    }

    fun resolve(index: Int, calls: List<ToolCall>, bindings: ToolBindings, omitted: Set<Int>, receipts: Map<Int, ToolReceipt>): ToolCall {
        validate(calls, bindings, omitted)
        require(index in calls.indices && index !in omitted)
        val call = calls[index]
        val arguments = call.arguments.toMutableMap()
        bindings[index].orEmpty().forEach { (field, ref) ->
            val receipt = receipts[ref.step] ?: error("La dependencia todavía no tiene resultado.")
            check(receipt.state == ExecutionState.VERIFIED && !receipt.pendingAction) { "La dependencia no quedó verificada." }
            val value = receipt.outputs[ref.field] ?: error("El resultado estructurado no está disponible.")
            require(value.type == inputType(call, field) && value.type == outputTypes(calls[ref.step])[ref.field])
            arguments[field] = when {
                call.name == "calculate" -> "(${value.value})+0"
                value.type == ToolValueType.NUMBER -> value.value.toBigDecimal().toBigIntegerExact().toString()
                else -> value.value
            }
        }
        return call.copy(arguments = arguments)
    }

    fun encodeValues(values: Map<String, ToolValue>) = JSONObject().apply {
        require(values.size <= 8)
        values.forEach { (key, v) -> require(key.length <= 40); put(key, JSONObject().put("type", v.type.name).put("value", v.value)) }
    }
    fun decodeValues(raw: JSONObject): Map<String, ToolValue> {
        require(raw.length() <= 8)
        return raw.keys().asSequence().associateWith { key ->
            require(key.length <= 40)
            val v = raw.getJSONObject(key)
            require(v.keys().asSequence().toSet() == setOf("type", "value"))
            ToolValue(ToolValueType.valueOf(v.getString("type")), v.getString("value"))
        }
    }
    fun encodeBindings(bindings: ToolBindings) = JSONArray().apply {
        require(bindings.size <= 6)
        bindings.forEach { (target, fields) -> fields.forEach { (argument, ref) ->
            put(JSONObject().put("target", target).put("argument", argument).put("source", ref.step).put("field", ref.field))
        } }
    }.toString()
    fun decodeBindings(raw: String): ToolBindings {
        require(raw.length <= 16000)
        val a = JSONArray(raw); require(a.length() <= 48)
        val out = linkedMapOf<Int, MutableMap<String, ToolResultRef>>()
        for (i in 0 until a.length()) {
            val j = a.getJSONObject(i); val target = j.getInt("target"); val source = j.getInt("source")
            require(target in 0..5 && source in -1..5)
            val arg = j.getString("argument"); val field = j.getString("field")
            require(arg.length in 1..40 && field.length in 1..40)
            require(out.getOrPut(target) { linkedMapOf() }.put(arg, ToolResultRef(source, field)) == null)
        }
        return out
    }
}
