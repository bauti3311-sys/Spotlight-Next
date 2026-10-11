package dev.bautista.spotlight.search

import dev.bautista.spotlight.data.Config
import org.json.JSONObject
import kotlin.math.ln

object ResultRanker {
    fun rank(query:String,items:List<Result>,c:Config):List<Result> {
        val usage=runCatching{JSONObject(c.usageData)}.getOrDefault(JSONObject())
        return items.distinctBy{it.id}.map {item->
            val lexical=if(item.category in setOf("Aplicaciones","Contactos","Notas"))relevance(query,item.title)/3 else 0
            val exact=if(query.isNotBlank() && normalize(query.trim())==normalize(item.title))400 else 0
            val count=if(c.learning)usage.optJSONObject(item.id)?.optInt("count",0) ?: 0 else 0
            item.copy(score=item.score+lexical+exact+(if(item.id in c.favorites)80 else 0)+(ln(1.0+count)*20).toInt().coerceAtMost(100))
        }.sortedWith(compareByDescending<Result>{it.score}.thenBy{normalize(it.title)}.thenBy{it.id})
    }
    fun priority(query:String):List<String> {
        val q=normalize(query)
        return when {
            q.startsWith("kind:") || q.startsWith("pdf ") || q.startsWith("archivos ") || q.startsWith("fotos ")->listOf("Archivos y carpetas")
            q.startsWith("clima")->listOf("Clima")
            q.startsWith("ia ")->listOf("IA")
            q.any{it.isDigit()} && !q.startsWith("abrir")->listOf("Herramientas y conversiones")
            else->emptyList()
        }
    }
}
