package dev.bautista.spotlight.agent

import org.json.JSONArray
import org.json.JSONObject

/** One explicitly approved, bounded program; no shell, coordinates or arbitrary scripts. */
internal data class PhoneUseStep(
    val operation: String,
    val selector: String,
    val text: String? = null,
    val expected: String? = null
)

/** Treat generated JSON as untrusted until the user approves the whole sequence. */
internal object PhoneUseSequencePolicy {
    private val tapFields = setOf("op", "selector", "expect")
    private val typeFields = setOf("op", "selector", "text")

    fun parse(raw: String): List<PhoneUseStep> {
        require(raw.length in 2..2500) { "La secuencia excede el límite." }
        val array = JSONArray(raw)
        require(array.length() in 1..4) { "Se permiten de 1 a 4 pasos por confirmación." }
        return (0 until array.length()).map { i ->
            val entry = array.getJSONObject(i)
            val op = entry.getString("op")
            val fields = entry.keys().asSequence().toSet()
            val required = when (op) {
                "tap" -> tapFields
                "type" -> typeFields
                else -> error("Operación no permitida.")
            }
            require(fields == required) { "Campos inesperados o incompletos." }
            val selector = entry.getString("selector")
            PhoneUsePolicy.parseSelector(selector)
            if (op == "tap") {
                val expected = entry.getString("expect")
                PhoneUsePolicy.parseSelector(expected)
                require(expected != selector) { "No alcanza con comprobar el mismo control." }
                PhoneUseStep(op, selector, expected = expected)
            } else {
                val value = entry.getString("text")
                require(PhoneUsePolicy.allowText(value)) { "El texto no es válido." }
                PhoneUseStep(op, selector, text = value)
            }
        }
    }

    /** Assertion on an exact selector, never substring search within a control. */
    fun matches(step: PhoneUseStep, observed: Set<String>, after: Set<String>): Boolean =
        if (step.operation == "tap") {
            step.selector in observed && step.expected !in observed && step.expected in after
        } else false
}
