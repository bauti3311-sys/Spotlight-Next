package dev.bautista.spotlight.next

import dev.bautista.spotlight.search.*
import dev.bautista.spotlight.control.SearchScope

/** Pure local routing: no network, no execution and no mutation while typing. */
object IntentRouter {
    fun title(raw:String,action:Action)=when(action){is Action.TimerForm->"Preparar temporizador";is Action.ComposeMessage->"Preparar mensaje a ${action.target}";is Action.EditNote->"Crear nota con este texto";is Action.SystemPanel->when(action.page){"weather-native"->"Consultar clima";"sports-native"->"Consultar agenda deportiva";"display-review"->"Revisar cambio de pantalla";"tasker"->"Abrir Tasker";else->raw.trim()};else->raw.trim()}

    fun route(raw:String,messageApp:String="whatsapp"):Action? {
        val text=raw.trim().take(8000);val q=normalize(text)
        Regex("^(?:m[aá]ndale|mandar|mand[aá]|enviar|env[ií]a|enviarle)(?: un)? (?:mensaje(?: por)? |)(?:whatsapp|mensaje)(?: a)? (.+?)(?: que | diciendo |: ?)(.+)$",RegexOption.IGNORE_CASE).matchEntire(text)?.let {
            return Action.ComposeMessage(if(normalize(text.substring(0,it.groups[1]!!.range.first)).contains("whatsapp"))"whatsapp" else messageApp,it.groupValues[1].trim(),it.groupValues[2].trim())
        }
        Regex("^(?:m[aá]ndale|mandar|mand[aá]|enviar|env[ií]a)(?: un)? (?:whatsapp|mensaje)(?: a)? (.+)$",RegexOption.IGNORE_CASE).matchEntire(text)?.let {return Action.ComposeMessage(if(normalize(text.substring(0,it.groups[1]!!.range.first)).contains("whatsapp"))"whatsapp" else messageApp,it.groupValues[1].trim())}
        Regex("^(?:baja|baja el|baja brillo|baja el brillo|baja brillo al|baja el brillo al|subi el brillo al|pone el brillo al|brillo al|brillo) (\\d{1,3})%$").matchEntire(q)?.let { val n=it.groupValues[1].toInt();if(n in 0..100)return Action.SystemPanel("display-review","screen_brightness:${(n*255/100).coerceAtLeast(20)}") }
        if(q in setOf("mostrame apps","mostrar aplicaciones","mis aplicaciones","mostrame las aplicaciones"))return Action.SelectMode("Apps",true)
        Regex("^(?:activa|activar|prepara|preparar) (?:el )?modo (estudio|lectura|concentracion|multimedia)$").matchEntire(q)?.let{return Action.SystemPanel("profiles",it.groupValues[1])}
        Regex("^(?:cre[aá]|crear|cr[eé]ame) (?:una )?nota (?:con(?: este texto)?|que diga|:)(?: ?)(.+)$",RegexOption.IGNORE_CASE).matchEntire(text)?.let{return Action.EditNote("",it.groupValues[1].trim())}
        Regex("^(?:quiero escuchar|escuchar|reproducir|busca musica de) (.+)$",RegexOption.IGNORE_CASE).matchEntire(text)?.let{return Action.ComposeSearch("spotify","com.spotify.music",it.groupValues[1].trim())}
        if(q in setOf("prepara spotify","preparar spotify"))return Action.ComposeSearch("spotify","com.spotify.music")
        Regex("^(?:inicia|iniciar|pone|poner|crea|crear)? ?(?:un )?temporizador(?: de)? (\\d{1,4})(?: minutos?| min)?$").matchEntire(q)?.let {val n=it.groupValues[1].toInt();if(n in 1..1440)return Action.TimerForm(n)}
        if(q in setOf("clima","tiempo","pronostico","clima nativo"))return Action.SystemPanel("weather-native")
        if(q.startsWith("clima en "))return Action.SystemPanel("weather-native",text.substring(9))
        Regex("^(?:va a llover|llovera|pronostico)(?: manana| hoy)?(?: en (.+))?$").matchEntire(q)?.let {return Action.SystemPanel("weather-native",it.groupValues[1])}
        if(q in setOf("deportes","deportes en vivo","resultados deportivos","partidos de hoy"))return Action.SystemPanel("sports-native")
        Regex("^(?:como va|como salio|resultado de|cuando juega) (.+)$").matchEntire(q)?.let{return Action.SystemPanel("sports-native",it.groupValues[1])}
        if(q in setOf("tasker","tareas de tasker","automatizaciones tasker"))return Action.SystemPanel("tasker")
        if(q in setOf("catalogo","catalogo de acciones","automatizaciones","packs","perfiles"))return Action.SystemPanel("catalog")
        Regex("^(?:busca|buscar|buscame|mostrame)(?: los| un| el)? (?:pdf|pdfs)(?: de)? (.+)$").matchEntire(q)?.let {return Action.Navigate(SearchScope(it.groupValues[1],"files","pdf"))}
        return null
    }
}
