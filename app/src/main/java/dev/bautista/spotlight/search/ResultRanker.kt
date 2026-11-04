package dev.bautista.spotlight.search

import dev.bautista.spotlight.data.Config
import org.json.JSONObject
import kotlin.math.exp
import kotlin.math.ln

object ResultRanker {
    private const val HALF_LIFE_MS = 14.0 * 86_400_000.0
    fun rank(query:String,items:List<Result>,c:Config):List<Result> {
        val usage=runCatching{JSONObject(c.usageData)}.getOrDefault(JSONObject())
        val priorities=runCatching{JSONObject(c.sourcePriority)}.getOrDefault(JSONObject())
        val now=System.currentTimeMillis()
        return ordered(items.map {item->
            val lexical=if(item.category in setOf("Aplicaciones","Contactos","Notas"))relevance(query,item.title)/3 else 0
            val exact=if(query.isNotBlank() && normalize(query.trim())==normalize(item.title))2400 else 0
            val entry=if(c.learning && !c.privateMode)usage.optJSONObject(item.id) else null
            val count=entry?.optInt("count",0) ?: 0
            val lastTime=entry?.optLong("time",0L) ?: 0L
            val decay=if(lastTime>0L && now>lastTime)exp(-(now-lastTime)/HALF_LIFE_MS) else 1.0
            val habitBonus=(ln(1.0+count)*20.0*decay).toInt().coerceIn(0,100)
            item.copy(
                matchClass=matchClass(query,item),
                score=item.score+lexical+exact+priorities.optInt(item.category,0).coerceIn(-150,150)+(if(item.id in c.favorites)80 else 0)+habitBonus
            )
        })
    }
    fun ordered(items:List<Result>):List<Result> =
        items.sortedWith(
            compareByDescending<Result>{it.matchClass}
                .thenByDescending{if(it.matchClass==6 && it.category=="Aplicaciones")1 else 0}
                .thenByDescending{it.score}
                .thenBy{normalize(it.title)}
                .thenBy{it.id}
        ).distinctBy{it.id}

    // A personal bonus can order peers, never outrank a better lexical class.
    fun matchClass(query:String,item:Result):Int {
        val q=normalize(query.trim()).split(Regex("\\s+")).filter{it.isNotBlank()}.joinToString(" ")
        if(q.isEmpty())return 0
        val t=normalize(item.title.trim()).split(Regex("\\s+")).joinToString(" ")
        val installed=item.category=="Aplicaciones"
        val command=item.category in setOf("Comandos","Acciones") && item.action !is Action.Ask &&
            item.action !is Action.Access && item.action !is Action.RestoreQuery &&
            (item.action as? Action.SystemPanel)?.page!="extensions"
        return when {
            (installed || command) && t==q->6
            (installed || command) && t.startsWith(q)->5
            (installed || command) && relevance(q,t)>0->4
            item.category in setOf("Calculadora","Conversión","Herramientas") && item.action is Action.Copy->4
            item.category=="Archivos" && item.semanticMatch->3
            item.category=="Web"->0
            (item.category in setOf("Archivos","Contactos") || item.category=="Notas" && item.action !is Action.NewNote) && relevance(query,item.title)>=500->3
            item.id.startsWith("root:") || item.id.startsWith("smart:") || item.id.startsWith("context:app-search:") || item.id=="intent:local" || relevance(query,item.title)>=500->2
            else->0
        }
    }
    fun priority(query:String):List<String> {
        val q=normalize(query)
        return when {
            q.startsWith("kind:") || q.startsWith("pdf ") || q.startsWith("archivos ") || q.startsWith("fotos ")->listOf("Archivos y carpetas")
            q.startsWith("clima") || WeatherRequests.matches(query)->listOf("Clima")
            q.startsWith("ia ") || LocalLanguage.isQuestion(query)->listOf("IA")
            q.any{it.isDigit()} && !q.startsWith("abrir")->listOf("Herramientas y conversiones")
            else->emptyList()
        }
    }
}
