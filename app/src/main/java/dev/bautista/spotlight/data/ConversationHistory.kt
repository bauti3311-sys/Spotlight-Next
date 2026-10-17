package dev.bautista.spotlight.data

import dev.bautista.spotlight.search.AiRequestPolicy
import org.json.JSONArray
import org.json.JSONObject

/** Completed text exchanges only. Legacy display text is never parsed into provider roles. */
object ConversationHistory {
    const val MAX_STORED_CHARS = 48000
    fun render(turns: List<Pair<String, String>>) = turns.joinToString("\n\n") { (role, text) ->
        "${if (role == "assistant") "Spotlight" else "Usuario"}: $text"
    }
    fun valid(turns: List<Pair<String, String>>) = turns.size in 2..12 && turns.size % 2 == 0 &&
        turns.withIndex().all { (i, turn) ->
            turn.first == (if (i % 2 == 0) "user" else "assistant") && turn.second.isNotBlank() &&
                turn.second.length <= AiRequestPolicy.MAX_PROMPT_CHARS
        } && render(turns).length <= MAX_STORED_CHARS

    fun encode(turns: List<Pair<String, String>>) = JSONArray().apply {
        turns.forEach { (role, text) -> put(JSONObject().put("role", role).put("text", text)) }
    }
    fun decode(value: JSONArray?, displayedText: String): List<Pair<String, String>> {
        if (value == null || value.length() !in 2..12) return emptyList()
        val turns = runCatching { (0 until value.length()).map { i ->
            val item = value.getJSONObject(i)
            item.getString("role") to item.getString("text")
        } }.getOrNull() ?: return emptyList()
        return turns.takeIf { valid(it) && render(it) == displayedText } ?: emptyList()
    }
    // Five full exchanges leave room for the new question without transport truncation.
    fun forResume(turns: List<Pair<String, String>>) = if (valid(turns)) turns.takeLast(10).toList() else emptyList()
}
