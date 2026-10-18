package dev.bautista.spotlight.search

import android.provider.DocumentsContract
import dev.bautista.spotlight.data.Config

/** Local presentation of the existing ranked search, never another provider or executor.
 * Secondary matches stay available in [all]; only useful candidates enter the initial view. */
object CompactResults {
    const val limit = 3
    data class View(val initial: List<Result>, val all: List<Result>) {
        fun visible(expanded: Boolean, retainedId: String? = null): List<Result> {
            if (expanded) return all
            val retained = all.firstOrNull { it.id == retainedId } ?: return initial
            if (initial.any { it.id == retained.id }) return initial
            // Collapsing must not execute/select a different result under the user's hands.
            return initial.take(limit - 1) + retained
        }
    }

    fun select(query: String, items: List<Result>, config: Config, preserveOrder: Boolean = false): View {
        val all = StableResults.unique(items.filterNot { it.id.startsWith("section:") })
        if (query.isBlank()) return View(ResultSections.home(all).take(limit), all)
        val intent = UniversalIntentEngine.resolve(query, config)
        val interpreted=QueryInterpreter.interpret(query)
        val appQuery = intent.entities.firstOrNull { it.type == EntityType.APP_NAME }?.value ?: interpreted.text
        val normalizedQuery=normalize(query.trim())
        val requestedFolders=Regex("^(?:buscar )?carpetas?(?: |$)").containsMatchIn(normalizedQuery) ||
            !query.contains('"') && Regex("(?:^| )kind:(?:folders?|carpetas?)(?: |$)").containsMatchIn(normalizedQuery)
        fun tier(item: Result): Int {
            val lexical = ResultRanker.matchClass(appQuery, item)
            if (item.category == "Aplicaciones") return when (lexical) { 6 -> 8; 5 -> 7; 4 -> 5; else -> 0 }
            val action = item.action
            // Generic web and dictionary fallbacks are exploratory, not inferred intent.
            if (item.id == "url" || item.id.startsWith("search:natural:") || item.id.startsWith("integration:")) return 6
            if (item.id == "concept-definition" || item.category == "Web") return 0
            // These adapters only emit after an explicit local command/scope match.
            // Eligibility never substitutes their permission or confirmation contracts.
            if (item.id.startsWith("lookup:") || item.id.startsWith("text:") || item.id.startsWith("ai:") ||
                item.id.startsWith("reply:") || item.id.startsWith("action:") || item.id == "power-settings" ||
                item.id == "ai" && normalizedQuery.startsWith("ia ") ||
                action is Action.TimerForm || action is Action.NewNote) return 6
            if (item.mime == DocumentsContract.Document.MIME_TYPE_DIR && !requestedFolders) return 0
            if (action is Action.ComposeMessage && (intent.primaryIntent == PrimaryIntentKind.COMMUNICATION ||
                    normalize(query).trim() == normalize(action.channel.replace("_business", "")))) return 6
            if (item.id.startsWith("root:") || item.id.startsWith("universal:") || item.id == "intent:local" ||
                item.id.startsWith("smart:") || item.id == "settings:natural" ||
                item.id == "message:compose" || item.id.startsWith("search:natural:")) return 6
            if (action is Action.Device || action is Action.Copy && item.category in setOf("Calculadora", "Conversión", "Herramientas")) return 6
            if (action is Action.Access || item.id.startsWith("files:") || item.id == "extensions:disabled") return 4
            // These providers already require a real lexical/content/semantic match, not a
            // filename-only heuristic: OCR hits must remain visible when the title differs.
            if (item.category in setOf("Archivos", "Notas", "Portapapeles", "Guardados")) return 4
            if (item.category == "Contactos" && (relevance(interpreted.text, item.title) >= 500 || interpreted.scope?.section == "contacts" || intent.primaryIntent == PrimaryIntentKind.COMMUNICATION)) return 4
            if (item.id.startsWith("recent:") || item.id.startsWith("conversation:") && normalize(query).contains("conversacion") ||
                item.id.startsWith("pin:") && normalize(query).trim() in setOf("fijados", "favoritos")) return 4
            return if (relevance(query, item.title) >= 500) 3 else 0
        }
        // Compute lexical/intent eligibility once per candidate, not in every comparator call.
        val eligible = all.map { it to tier(it) }.filter { it.second > 0 }
        val best = if (preserveOrder) eligible else eligible.sortedWith(
            compareByDescending<Pair<Result, Int>> { it.second }.thenByDescending { it.first.score })
        return View(best.take(limit).map { it.first }, all)
    }

    fun subtitle(item: Result): String = item.subtitle.takeUnless {
        normalize(it.trim()) in setOf("abrir aplicación", "abrir aplicacion", "aplicacion", "aplicación")
    }.orEmpty()
}
