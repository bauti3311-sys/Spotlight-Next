package dev.bautista.spotlight.search

/** Presentation policy shared by touch and keyboard navigation. Nonempty searches order sections by their best relevance;
 * large provider batches cannot accumulate votes against a precise match. */
object ResultSections {
    val order = listOf("Sugerencias", "Mensajes", "Historial", "Fijados", "Recientes", "Aplicaciones", "Sistema", "Acciones y comandos", "Contactos y comunicación", "Ajustes del dispositivo", "Herramientas y conversiones", "Notas", "Textos frecuentes", "Portapapeles", "Archivos y carpetas", "Búsqueda web", "IA", "Clima", "Deportes en vivo", "Información")
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
        item.id.startsWith("snippet:") -> "Textos frecuentes"
        item.category in setOf("Acciones", "Comandos") -> "Acciones y comandos"
        else -> item.category
    }
    fun home(items:List<Result>):List<Result> = items.sortedBy { when(it.category){"Sugerencias"->0;"Archivos"->1;"Aplicaciones"->2;else->3} }
    fun homeHeading(item:Result)=when(item.category){"Sugerencias"->"Sugerencias frecuentes";"Archivos"->"Archivos recientes";else->category(item)}
    fun unified(items:List<Result>):List<Result> {
        val counts=mutableMapOf<String,Int>()
        val visible=items.filter { val key=category(it);val n=(counts[key] ?: 0)+1;counts[key]=n;n<=if(key=="Archivos y carpetas")2 else 3 }.take(12)
        return if(visible.size==items.size)visible else visible+Result("section:all","Ver más resultados","Mantener la consulta y ampliar resultados","Información",0,Action.Navigate(dev.bautista.spotlight.control.SearchScope("Todo","results")))
    }
    fun grouped(items: List<Result>, limit: Int = 3, expanded: Boolean = false, query:String="",preserveOrder:Boolean=false): List<Result> {
        if(query.isNotBlank()) {
            val ranked=if(preserveOrder)items else ResultRanker.ordered(items)
            if(expanded)return ranked
            val counts=mutableMapOf<String,Int>()
            val visible=ranked.filter { item ->val name=category(item);val count=(counts[name] ?: 0)+1;counts[name]=count;count<=limit }
            return visible+counts.filterValues{it>limit}.map { (name,count)->Result("section:$name","Ver más · ${count-limit}",name,name,0,Action.Navigate(dev.bautista.spotlight.control.SearchScope(name,"results"))) }
        }
        return items.groupBy(::category).entries.sortedWith(
            compareByDescending<Map.Entry<String,List<Result>>> { entry ->
                if(query.isBlank()) 0 else entry.value.maxOfOrNull { it.score } ?: 0
            }.thenBy { entry -> (ResultRanker.priority(query)+order).distinct().indexOf(entry.key).let { n -> if(n<0) order.size else n } }
        ).flatMap { (name, group) ->
            val sorted = group.sortedByDescending { it.score }
            if(expanded || sorted.size <= limit) sorted else sorted.take(limit) + Result(
                "section:$name", "Ver más · ${sorted.size - limit}", name, name, 0,
                Action.Navigate(dev.bautista.spotlight.control.SearchScope(name, "results"))
            )
        }
    }
}
