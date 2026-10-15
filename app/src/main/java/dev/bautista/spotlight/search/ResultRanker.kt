package dev.bautista.spotlight.search

import dev.bautista.spotlight.data.Config
import org.json.JSONObject
import kotlin.math.ln

object ResultRanker {
    fun rank(query:String,items:List<Result>,c:Config):List<Result> {
        val usage=runCatching{JSONObject(c.usageData)}.getOrDefault(JSONObject())
        val priorities=runCatching{JSONObject(c.sourcePriority)}.getOrDefault(JSONObject())
        return ordered(items.map {item->
            val lexical=if(item.category in setOf("Aplicaciones","Contactos","Notas"))relevance(query,item.title)/3 else 0
            val exact=if(query.isNotBlank() && normalize(query.trim())==normalize(item.title))2400 else 0
            val count=if(c.learning && !c.privateMode)usage.optJSONObject(item.id)?.optInt("count",0) ?: 0 else 0
            item.copy(matchClass=matchClass(query,item),score=item.score+lexical+exact+priorities.optInt(item.category,0).coerceIn(-150,150)+(if(item.id in c.favorites)80 else 0)+(ln(1.0+count)*20).toInt().coerceAtMost(100))
        })
    }
    fun ordered(items:List<Result>):List<Result> = items.sortedWith(compareByDescending<Result>{it.matchClass}.thenByDescending{it.score}.thenBy{normalize(it.title)}.thenBy{it.id}).distinctBy{it.id}

    // A personal bonus can order peers, never outrank a better lexical class.
    fun matchClass(query:String,item:Result):Int {
        val q=normalize(query.trim()).split(Regex("\\s+")).filter{it.isNotBlank()}.joinToString(" ")
        if(q.isEmpty())return 0
        val t=normalize(item.title.trim()).split(Regex("\\s+")).joinToString(" ")
        val installed=item.category=="Aplicaciones"
        val words=q.split(' ')
        return when {
            installed && t==q->6
            installed && t.startsWith(q)->5
            installed && (t.contains(q) || words.all{word->t.split(Regex("[^\\p{L}\\p{N}]+")).any{it.startsWith(word)}})->4
            item.category in setOf("Calculadora","Conversión","Herramientas") && item.action is Action.Copy->4
            item.category=="Web"->0
            (item.category in setOf("Archivos","Contactos") || item.category=="Notas" && item.action !is Action.NewNote) && relevance(query,item.title)>=500->3
            item.id.startsWith("root:") || item.id.startsWith("smart:") || item.id=="intent:local" || relevance(query,item.title)>=500->2
            else->0
        }
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
