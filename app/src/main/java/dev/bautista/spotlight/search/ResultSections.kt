package dev.bautista.spotlight.search

/** Presentation policy shared by touch and keyboard navigation. Provider scores
 * are only compared inside a section; a file provider cannot bury an app. */
object ResultSections {
    val order = listOf("Sugerencias", "Mensajes", "Historial", "Fijados", "Recientes", "Aplicaciones", "Sistema", "Acciones y comandos", "Contactos y comunicación", "Ajustes del dispositivo", "Herramientas y conversiones", "Notas", "Fragmentos de texto", "Portapapeles", "Archivos y carpetas", "Búsqueda web", "IA", "Clima", "Deportes en vivo", "Información")
    fun category(item: Result): String = when {
        item.action is Action.Lookup -> when(item.action.type) {
            "weather" -> "Clima"
            "sports" -> "Deportes en vivo"
            "currency", "translate" -> "Herramientas y conversiones"
            else -> "Información"
        }
        item.action is Action.ComposeMessage || item.category == "Contactos" -> "Contactos y comunicación"
        item.action is Action.Launch && item.action.intent.action?.startsWith("android.settings.") == true -> "Ajustes del dispositivo"
        item.category == "Archivos" -> "Archivos y carpetas"
        item.category == "Web" -> "Búsqueda web"
        item.category in setOf("Calculadora", "Conversión", "Herramientas") -> "Herramientas y conversiones"
        item.category == "Consultas" -> "Fijados"
        item.id.startsWith("snippet:") -> "Fragmentos de texto"
        item.category in setOf("Acciones", "Comandos") -> "Acciones y comandos"
        else -> item.category
    }
    fun grouped(items: List<Result>, limit: Int = 3, expanded: Boolean = false, query:String=""): List<Result> =
        items.groupBy(::category).entries.sortedBy { (ResultRanker.priority(query)+order).distinct().indexOf(it.key).let { n -> if(n < 0) order.size else n } }.flatMap { (name, group) ->
            val sorted = group.sortedByDescending { it.score }
            if(expanded || sorted.size <= limit) sorted else sorted.take(limit) + Result(
                "section:$name", "Ver más · ${sorted.size - limit}", name, name, 0,
                Action.Navigate(dev.bautista.spotlight.control.SearchScope(name, "results"))
            )
        }
}
