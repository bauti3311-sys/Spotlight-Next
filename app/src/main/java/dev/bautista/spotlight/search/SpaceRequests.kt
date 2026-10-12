package dev.bautista.spotlight.search

data class SportsRequest(val entity:String="",val live:Boolean=false,val unsupported:String="",val today:Boolean=false) {
    companion object {
        fun parse(raw:String):SportsRequest? {
            if(raw.contains('"'))return null
            val q=normalize(raw.trim()).removePrefix("¿").removeSuffix("?")
            if(q in setOf("mis partidos","mis deportes","deportes favoritos","agenda deportiva"))return SportsRequest()
            if(q=="deportes en vivo")return SportsRequest(live=true)
            if(q=="ufc" || q.startsWith("proxima ufc") || q.startsWith("cartelera ufc") || q in setOf("proxima mma","mma hoy"))return SportsRequest("UFC",unsupported="MMA")
            if(q=="nba hoy")return SportsRequest("NBA",today=true)
            if(q in setOf("partidos de hoy","deportes hoy"))return SportsRequest(today=true)
            val suffix=Regex("^(.+?) (?:proximo partido|proximos partidos)$",RegexOption.IGNORE_CASE).matchEntire(q)
            if(suffix!=null)return SportsRequest(raw.substring(0,suffix.groupValues[1].length).trim())
            return null
        }
    }
}
object WeatherRequests {
    fun matches(raw:String):Boolean {
        val q=normalize(raw.trim()).removePrefix("¿").removeSuffix("?")
        return q.startsWith("necesito paraguas") || q.startsWith("a que hora deja de llover") || q.startsWith("va a llover")
    }
}
