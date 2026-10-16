package dev.bautista.spotlight.search

/** Small deterministic Spanish lexicon. Never calls a model or executes a query. */
object LocalLanguage {
    private val words=mapOf("cero" to 0,"uno" to 1,"dos" to 2,"tres" to 3,"cuatro" to 4,"cinco" to 5,"seis" to 6,"siete" to 7,"ocho" to 8,"nueve" to 9,"diez" to 10,"once" to 11,"doce" to 12,"trece" to 13,"catorce" to 14,"quince" to 15,"dieciseis" to 16,"diecisiete" to 17,"dieciocho" to 18,"diecinueve" to 19,"veintiuno" to 21,"veintidos" to 22,"veintitres" to 23,"veinticuatro" to 24,"veinticinco" to 25,"veintiseis" to 26,"veintisiete" to 27,"veintiocho" to 28,"veintinueve" to 29,"veinte" to 20,"treinta" to 30,"cuarenta" to 40,"cincuenta" to 50,"sesenta" to 60,"setenta" to 70,"ochenta" to 80,"noventa" to 90,"cien" to 100)
    fun canonical(raw:String):String {
        var q=normalize(raw.trim()).replace(Regex("\\s+")," ")
        val verbs=mapOf("abri" to "abrir","abre" to "abrir","abrime" to "abrir","baja" to "bajar","bajame" to "bajar","subi" to "subir","sube" to "subir","subime" to "subir","pone" to "poner","pon" to "poner","poneme" to "poner","pausa" to "pausar","pausame" to "pausar","resumi" to "resumir","resume" to "resumir","resumime" to "resumir","mandale" to "mandar","enviale" to "mandar","mostrame" to "mostrar","muestrame" to "mostrar","buscame" to "buscar")
        q=q.split(' ').joinToString(" "){verbs[it] ?: it}
        q=q.replace("por ciento","%").replace("la pantalla","brillo")
        q=Regex("\\b("+words.keys.joinToString("|")+")\\b").replace(q){words.getValue(it.value).toString()}
        q=q.replace(Regex("(\\d{1,2}0) y (\\d)(?=\\s*%)")){(it.groupValues[1].toInt()+it.groupValues[2].toInt()).toString()}
        return q.replace(Regex("\\s+%"),"%")
    }
    fun forSearch(raw:String):String {
        if(raw.contains('"') || raw.trim().startsWith("mand",true) || raw.trim().startsWith("envia",true) || LocalPlan.parse(raw)!=null)return raw
        val canonical=canonical(raw)
        val control=Regex("^(?:(?:bajar|subir|poner|ajustar|cambiar) )?(?:el )?(?:brillo|volumen)\\b").containsMatchIn(canonical)
        return if(control)canonical else raw
    }
    fun isQuestion(raw:String):Boolean {
        val trimmed=raw.trim()
        if(trimmed.isEmpty() || trimmed.length>4000)return false
        val q=normalize(trimmed).replace(Regex("\\s+")," ")
        val clean=q.removePrefix("¿").removeSuffix("?").trim()
        if(clean.isEmpty())return false
        // Domain-specific local intents (sports, weather, calendar, file searches, compound plans) are not generic AI questions.
        if(WeatherRequests.matches(trimmed) || clean.startsWith("clima"))return false
        if(SportsRequest.parse(trimmed)!=null || Regex("^(?:como va|resultado de|partido de|como salio|cuando juega) (.+)$").matches(clean))return false
        if(clean in setOf("que tengo manana","mi agenda manana","agenda manana","que tengo hoy","mi agenda hoy","agenda hoy"))return false
        if(LocalPlan.parse(trimmed)!=null || UniversalIntentEngine.parseDocumentAiCompound(trimmed)!=null)return false
        val prefixes=listOf(
            "que es","que son","que significa","que significan","que paso","que hace","que diferencia hay",
            "como funciona","como funcionan","como puedo","como hacer","como se hace","como se calcula","como se dice",
            "por que","para que",
            "quien es","quien fue","quienes son","quienes fueron","quien gano","quien invento","quien escribio",
            "cual es","cuales son","cual fue","cuales fueron",
            "cuanto es","cuanto vale","cuanto mide","cuanto pesa","cuanto dura","cuanto tarda",
            "cuanta ","cuantos ","cuantas ",
            "cuando es","cuando fue","cuando se ","cuando paso","cuando nacio","cuando gano",
            "donde queda","donde esta","donde se encuentra","donde nacio","donde se jugo",
            "explicame","explica","explicar",
            "defini","define","definir",
            "resumi","resumir","resume","resumime",
            "ayudame a","redacta","redactame","compara","comparame","analiza","analizame","contame","decime","diferencia entre"
        )
        return q.startsWith("¿") || q.endsWith("?") || prefixes.any{clean==it.trim() || clean.startsWith(if(it.endsWith(" "))it else "$it ")}
    }
}

/** Keep the selected identity and already-visible order when late sources arrive. */
object StableResults {
    fun merge(previous:List<Result>,next:List<Result>,locked:Boolean):List<Result> {
        if(!locked)return next
        val map=next.associateBy{it.id}
        return previous.mapNotNull{map[it.id]}+next.filter{n->previous.none{it.id==n.id}}
    }
    fun actionSignature(a:Action):String = when(a) {
        is Action.Launch->"launch:"+a.intent.toUri(0)
        is Action.InApp->"launch:"+a.intent.toUri(0)
        is Action.ComposeSearch->"search:${normalize(a.topic)}:${normalize(a.query)}"
        is Action.ComposeMessage->"message:${normalize(a.channel)}:${normalize(a.target)}:${normalize(a.text)}:${a.replyKey.orEmpty()}"
        is Action.Ask->"ask:${normalize(a.prompt)}:${normalize(a.messageTarget)}"
        is Action.SystemPanel->"panel:${a.page}:${normalize(a.packageName)}"
        is Action.Navigate->"nav:${a.scope.section}:${normalize(a.scope.topic)}:${a.scope.kind}"
        is Action.SelectMode->"mode:${a.mode}"
        else->a.toString()
    }
    fun unique(items:List<Result>):List<Result> {
        val seenActions=mutableSetOf<String>()
        val seenVisual=mutableSetOf<String>()
        return items.filter { item ->
            val actSig=actionSignature(item.action)
            val visSig="${normalize(item.title)}|${normalize(item.subtitle)}|${normalize(item.category)}"
            val isNewAction=seenActions.add(actSig)
            val isNewVisual=if(item.category in setOf("Archivos","Contactos","Notas","Portapapeles","Guardados"))true else seenVisual.add(visSig)
            isNewAction && isNewVisual
        }
    }
}
