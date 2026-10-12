package dev.bautista.spotlight.search

/** Small deterministic Spanish lexicon. Never calls a model or executes a query. */
object LocalLanguage {
    private val words=mapOf("cero" to 0,"uno" to 1,"dos" to 2,"tres" to 3,"cuatro" to 4,"cinco" to 5,"seis" to 6,"siete" to 7,"ocho" to 8,"nueve" to 9,"diez" to 10,"veinte" to 20,"treinta" to 30,"cuarenta" to 40,"cincuenta" to 50,"sesenta" to 60,"setenta" to 70,"ochenta" to 80,"noventa" to 90,"cien" to 100)
    fun canonical(raw:String):String {
        var q=normalize(raw.trim()).replace(Regex("\\s+")," ")
        val verbs=mapOf("abri" to "abrir","abre" to "abrir","baja" to "bajar","bajame" to "bajar","subi" to "subir","sube" to "subir","pone" to "poner","pon" to "poner","pausa" to "pausar","pausame" to "pausar","resumi" to "resumir","resume" to "resumir","mandale" to "mandar","enviale" to "mandar")
        q=q.split(' ').joinToString(" "){verbs[it] ?: it}
        q=q.replace("por ciento","%").replace("la pantalla","brillo")
        q=Regex("\\b("+words.keys.joinToString("|")+")\\b").replace(q){words.getValue(it.value).toString()}
        q=q.replace(Regex("(\\d{1,2}0) y (\\d)(?=\\s*%)")){(it.groupValues[1].toInt()+it.groupValues[2].toInt()).toString()}
        return q
    }
    fun forSearch(raw:String):String {
        val normalized=normalize(raw.trim()).replace(Regex("\\s+")," ")
        val canonical=canonical(raw)
        return if(canonical==normalized || raw.trim().startsWith("mand",true) || raw.trim().startsWith("envia",true))raw else canonical
    }
    fun isQuestion(raw:String):Boolean=raw.trim().startsWith("¿") || Regex("^(que es|como funciona|por que|quien es|cual es|explicame)\\b").containsMatchIn(normalize(raw))
}

/** Keep the selected identity and already-visible order when late sources arrive. */
object StableResults {
    fun merge(previous:List<Result>,next:List<Result>,locked:Boolean):List<Result> {
        if(!locked)return next
        val map=next.associateBy{it.id}
        return previous.mapNotNull{map[it.id]}+next.filter{n->previous.none{it.id==n.id}}
    }
    fun unique(items:List<Result>):List<Result> = items.distinctBy { when(val a=it.action){
        is Action.Launch->"launch:"+a.intent.toUri(0)
        is Action.InApp->"launch:"+a.intent.toUri(0)
        is Action.ComposeSearch->"search:${a.topic}:${a.query}"
        is Action.ComposeMessage->"message:${a.channel}:${a.target}:${a.text}:${a.replyKey}"
        is Action.Ask->"ask:${a.prompt}:${a.messageTarget}"
        else->a.toString()
    }}
}
