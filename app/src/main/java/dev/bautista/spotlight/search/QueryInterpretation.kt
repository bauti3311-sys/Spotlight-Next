package dev.bautista.spotlight.search

import dev.bautista.spotlight.control.SearchScope
import java.time.*
import java.time.temporal.TemporalAdjusters

enum class QueryIntent { SEARCH, FILES, APPLICATION, ACTION, KNOWLEDGE, CALENDAR }
data class FileConstraint(val kind:String,val terms:String,val start:Long?=null,val end:Long?=null,val dateField:String="modified",val dateLabel:String="") {
    val query:String get()=listOf("kind:$kind",terms,if(start!=null)"$dateField-after:$start" else "",if(end!=null)"$dateField-before:$end" else "").filter{it.isNotBlank()}.joinToString(" ")
}
data class QueryInterpretation(val original:String,val text:String,val intent:QueryIntent,val scope:SearchScope?=null,val files:FileConstraint?=null,val question:String?=null,val calendarPeriod:String?=null) {
    val hints:List<String> get()=files?.let{listOf(when(it.kind){"pdf"->"PDF";"image"->"Imágenes";"video"->"Vídeos";else->"Documentos"},it.dateLabel,"Archivos locales").filter{it.isNotBlank()}} ?: emptyList()
}

/** One entry point. Adapters preserve existing commands; date tokens never become file names. */
object QueryInterpreter {
    fun interpret(raw:String,scope:SearchScope?=null,today:LocalDate=LocalDate.now(),zone:ZoneId=ZoneId.systemDefault()):QueryInterpretation {
        if(scope!=null)return QueryInterpretation(raw,raw,QueryIntent.SEARCH,scope)
        SearchModifiers.parse(raw)?.let{return QueryInterpretation(raw,it.query,QueryIntent.SEARCH,it.scope)}
        files(raw,today,zone)?.let{return QueryInterpretation(raw,it.query,QueryIntent.FILES,SearchScope("","files"),it)}
        val q=normalize(raw.trim()).removePrefix("¿").removeSuffix("?")
        if(q in setOf("que tengo manana","mi agenda manana","agenda manana"))return QueryInterpretation(raw,"",QueryIntent.CALENDAR,SearchScope("","calendar","tomorrow"),calendarPeriod="tomorrow")
        if(q in setOf("que tengo hoy","mi agenda hoy","agenda hoy"))return QueryInterpretation(raw,"",QueryIntent.CALENDAR,SearchScope("","calendar","today"),calendarPeriod="today")
        if(LocalLanguage.isQuestion(raw))return QueryInterpretation(raw,raw,QueryIntent.KNOWLEDGE,question=raw)
        return QueryInterpretation(raw,LocalLanguage.forSearch(raw),if(UniversalIntent.recognize(raw) is UniversalIntent.OpenApp)QueryIntent.APPLICATION else QueryIntent.SEARCH)
    }
    fun files(raw:String,today:LocalDate=LocalDate.now(),zone:ZoneId=ZoneId.systemDefault()):FileConstraint? {
        // Literal/advanced queries retain the original grammar and operators.
        if(raw.contains('"') || raw.contains(':') || Regex("\\b(AND|OR|NOT)\\b").containsMatchIn(raw))return null
        val q=normalize(raw.trim()).replace(Regex("\\s+")," ").replace(Regex("^(buscar|busca|buscame|mostrame|mostrar)(?: los| las| un| una| el)? "),"")
        val head=q.substringBefore(' ')
        val kind=when(head){"pdf","pdfs"->"pdf";"foto","fotos","imagen","imagenes"->"image";"video","videos"->"video";"archivo","archivos","documento","documentos"->"document";else->return null}
        var terms=q.substringAfter(' ',"").removePrefix("de ")
        if(terms.isBlank() && head in setOf("archivo","archivos"))return null
        val periods=linkedMapOf("la semana pasada" to "lastWeek","semana pasada" to "lastWeek","esta semana" to "week","el mes pasado" to "lastMonth","mes pasado" to "lastMonth","este mes" to "month","anteayer" to "twoDays","ayer" to "yesterday","hoy" to "today")
        val date=periods.entries.firstOrNull{Regex("(?<!\\w)"+Regex.escape(it.key)+"(?!\\w)").containsMatchIn(terms)}
        val dateField=if(kind in setOf("image","video"))"created" else "modified"
        val interval=date?.let { (_,period)->when(period){
            "lastWeek"->today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1).let{it to it.plusWeeks(1)}
            "week"->today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).let{it to it.plusWeeks(1)}
            "lastMonth"->today.withDayOfMonth(1).minusMonths(1).let{it to it.plusMonths(1)}
            "month"->today.withDayOfMonth(1).let{it to it.plusMonths(1)}
            else->today.minusDays(when(period){"yesterday"->1;"twoDays"->2;else->0}).let{it to it.plusDays(1)}
        }}
        if(date!=null)terms=terms.replace(date.key,"").trim().removeSuffix(" de").trim()
        if(kind in setOf("image","video") && date==null)terms=terms.split(' ').filter{it !in setOf("en","la","el","las","los","una","un","con","y","de")}.joinToString(" ")
        return FileConstraint(kind,terms,interval?.first?.atStartOfDay(zone)?.toInstant()?.toEpochMilli(),interval?.second?.atStartOfDay(zone)?.toInstant()?.toEpochMilli(),dateField,date?.key.orEmpty())
    }
}

/** SQL pruning is conservative; the full parser still checks every returned document. */
object FileSqlFilter {
    data class Filter(val selection:String,val args:List<String>)
    fun from(query:String):Filter {
        if(Regex("\\b(OR|NOT)\\b").containsMatchIn(query))return Filter("",emptyList())
        val clauses=mutableListOf<String>();val args=mutableListOf<String>()
        Regex("\\b(modified|created)-(after|before):(\\d+)").findAll(query).forEach{m->clauses+="${m.groupValues[1]} ${if(m.groupValues[2]=="after")">=" else "<"} ?";args+=m.groupValues[3]}
        Regex("(?:^|\\s)kind:(pdf|image|video|audio)(?=\\s|$)").find(query)?.groupValues?.get(1)?.let{kind->
            when(kind){"pdf"->{clauses+="(mime = ? OR name LIKE ?)";args+="application/pdf";args+="%.pdf"};else->{clauses+="mime LIKE ?";args+="$kind/%"}}
        }
        return Filter(clauses.joinToString(" AND "),args)
    }
}
