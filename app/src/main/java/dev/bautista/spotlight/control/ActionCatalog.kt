package dev.bautista.spotlight.control

import android.content.*
import android.net.Uri
import android.provider.Settings
import dev.bautista.spotlight.search.*

data class SearchScope(val topic:String,val section:String,val kind:String="") {
    val label: String get()=when(section){"apps"->"Aplicaciones";"commands"->"Comandos";"ai"->"Inteligencia artificial";"clipboard"->"Portapapeles";"contacts"->"Contactos";"files-mode"->"Archivos";else->ActionCatalog.displayName(topic.ifBlank { "archivos" })+" › "+when(section){"files-menu"->"Tipos de archivo";"files"->when(kind){"image"->"Imágenes";"video"->"Vídeos";"audio"->"Audio";"pdf"->"PDFs";else->"Archivos"};"calendar"->when(kind){"today"->"Hoy";"tomorrow"->"Mañana";"week"->"Próximos siete días";else->"Agenda"};"replies"->"Mensajes recibidos";else->"Acciones"}}
}

object ActionCatalog {
    fun displayName(topic:String):String=mapOf("whatsapp_business" to "WhatsApp Business","whatsapp" to "WhatsApp","youtube" to "YouTube","gmail" to "Gmail","sms" to "SMS","email" to "Correo","sofascore" to "Sofascore")[topic] ?: topic.replaceFirstChar{it.uppercase()}
    private val aliases=mapOf("whatsapp business" to "whatsapp_business","whatsapp_business" to "whatsapp_business","wa" to "whatsapp","yt" to "youtube","youtube" to "youtube","whatsapp" to "whatsapp","sonido" to "sonido","audio" to "sonido","volumen" to "sonido","ajustes de sonido" to "sonido","wi-fi" to "sistema","wifi" to "sistema","bluetooth" to "sistema","pantalla" to "sistema","sistema" to "sistema","ajustes" to "sistema","configuracion" to "sistema","agenda" to "agenda","calendario" to "agenda","archivos" to "archivos","acciones" to "acciones","sp" to "spotify","spotify" to "spotify","sofascore" to "sofascore","telegram" to "telegram","mapas" to "mapas","chrome" to "chrome","vivaldi" to "vivaldi","google" to "google","gmail" to "gmail")
    val packages=mapOf("whatsapp_business" to "com.whatsapp.w4b","whatsapp" to "com.whatsapp","youtube" to "com.google.android.youtube","spotify" to "com.spotify.music","sofascore" to "com.sofascore.results","telegram" to "org.telegram.messenger","mapas" to "com.google.android.apps.maps","chrome" to "com.android.chrome","vivaldi" to "com.vivaldi.browser","gmail" to "com.google.android.gm")
    fun topic(query:String):String?=aliases[normalize(query.trim())]
    fun canonical(query:String):String {
        Regex("^buscar videos(?: de)? (.+)$",RegexOption.IGNORE_CASE).matchEntire(query.trim())?.let{return "youtube "+it.groupValues[1]}
        val q=query.trim().replace(Regex("^abrir ajustes de ",RegexOption.IGNORE_CASE), "")
        Regex("^(?:buscar|busca|buscame)(?: en)? (youtube|spotify|sofascore|google|mapas|chrome|vivaldi)[: ]+(.*)$",RegexOption.IGNORE_CASE).matchEntire(q)?.let{return normalize(it.groupValues[1])+" "+it.groupValues[2]}
        Regex("^(?:buscar|busca|buscame) (.+) en (youtube|spotify|sofascore|google|mapas|chrome|vivaldi)$",RegexOption.IGNORE_CASE).matchEntire(q)?.let{return normalize(it.groupValues[2])+" "+it.groupValues[1]}
        return q
    }
    fun settings(query: String): Intent? {
        val match=Regex("^abrir (?:los )?ajustes(?: de)? (.+)$").matchEntire(normalize(query.trim())) ?: return null
        val action=mapOf("sonido" to Settings.ACTION_SOUND_SETTINGS,"wifi" to Settings.ACTION_WIFI_SETTINGS,"wi-fi" to Settings.ACTION_WIFI_SETTINGS,"bluetooth" to Settings.ACTION_BLUETOOTH_SETTINGS,"pantalla" to Settings.ACTION_DISPLAY_SETTINGS,"bateria" to Settings.ACTION_BATTERY_SAVER_SETTINGS)[match.groupValues[1]] ?: return null
        return Intent(action)
    }
    fun message(query:String,defaultChannel:String="whatsapp"):Action.ComposeMessage? {
        val q=query.trim()
        val expression=Regex("^(?:(?:enviar|mandar|escribir)(?:le)?(?: un)? )?(mensaje|whatsapp business|whatsapp_business|whatsapp|wa|sms|correo|email)(?: por whatsapp)? a (.+)$",RegexOption.IGNORE_CASE).matchEntire(q) ?: return null
        val raw=expression.groupValues[2]
        val colon=raw.indexOf(':'); val saying=Regex("\\s+(?:diciendo|que)\\s+",RegexOption.IGNORE_CASE).find(raw)
        val cut=if(colon>=0)colon else saying?.range?.first ?: -1
        val target=if(cut>=0)raw.take(cut).trim() else raw.trim()
        val body=if(colon>=0)raw.drop(colon+1).trim() else if(saying!=null)raw.drop(saying.range.last+1).trim() else ""
        if(target.isBlank())return null
        val channel=when(normalize(expression.groupValues[1])){"whatsapp business","whatsapp_business"->"whatsapp_business";"mensaje"->if(normalize(q).contains("por whatsapp"))"whatsapp" else defaultChannel;"sms"->"sms";"correo","email"->"email";else->"whatsapp"}
        return Action.ComposeMessage(channel,target,body)
    }
    private fun row(id:String,title:String,subtitle:String,action:Action)=Result("catalog:$id",title,subtitle,"Acciones",2200,action)
    /** Keep navigation/reply capabilities too; action class is not an eligibility filter. */
    fun applicationActions(packageName:String):List<Result> = packages.entries
        .firstOrNull { it.value == packageName }?.let { menu(it.key,packageName=packageName) }.orEmpty()
    fun menu(topic:String,launch:Intent?=null,packageName:String?=packages[topic]):List<Result> {
        val title=displayName(topic)
        val out=mutableListOf<Result>()
        if(launch!=null)out+=row("$topic:open","Abrir $title","Aplicación instalada",Action.Launch(launch))
        fun command(label:String,query:String){out+=row("$topic:$query",label,"Cargar y completar",Action.RestoreQuery(query))}
        fun setting(label:String,value:String){out+=row("$topic:$value",label,"Panel de Android",Action.Launch(Intent(value)))}
        when(topic) {
            "whatsapp","whatsapp_business","telegram","gmail" -> {
                if(topic=="telegram")out+=row("telegram:compose","Compartir mensaje","Elegir contacto dentro de Telegram",Action.ComposeMessage("telegram"))
                else out+=row("$topic:compose","Enviar mensaje","Elegir contacto y escribir aquí",Action.ComposeMessage(if(topic=="gmail")"email" else topic))
                if(topic in setOf("whatsapp","whatsapp_business"))out+=row("wa:reply","Responder mensajes recibidos","Notificaciones con respuesta directa",Action.Navigate(SearchScope(topic,"replies")))
            }
            "youtube","spotify","sofascore","google","chrome","vivaldi","mapas" -> out+=row("$topic:search",if(topic=="sofascore")"Buscar equipo o resultado" else "Buscar en $title","Ingresar búsqueda aquí",Action.ComposeSearch(topic,packageName ?: ""))
            "sonido" -> {
                out+=row("audio-panel","Controlar volumen","Ajuste multimedia dentro de Spotlight",Action.AudioPanel())
                listOf("Subir volumen" to "volume_up","Bajar volumen" to "volume_down","Silenciar" to "mute","Activar sonido" to "unmute","Reproducir" to "play","Pausar" to "pause","Siguiente canción" to "next","Canción anterior" to "previous").forEach{(label,cmd)->out+=row(cmd,label,"Control nativo",Action.Device(cmd))}
                setting("Ajustes de sonido",Settings.ACTION_SOUND_SETTINGS)
            }
            "sistema" -> listOf("Wi-Fi" to Settings.ACTION_WIFI_SETTINGS,"Bluetooth" to Settings.ACTION_BLUETOOTH_SETTINGS,"Pantalla" to Settings.ACTION_DISPLAY_SETTINGS,"Batería" to Settings.ACTION_BATTERY_SAVER_SETTINGS,"Almacenamiento" to Settings.ACTION_INTERNAL_STORAGE_SETTINGS,"Aplicaciones" to Settings.ACTION_APPLICATION_SETTINGS,"Ubicación" to Settings.ACTION_LOCATION_SOURCE_SETTINGS,"Accesibilidad" to Settings.ACTION_ACCESSIBILITY_SETTINGS).forEach{(label,action)->setting(label,action)}
            "agenda" -> {
                listOf("Hoy" to "today","Mañana" to "tomorrow","Próximos siete días" to "week").forEach{(label,period)->out+=row("agenda:$period",label,"Eventos de tus calendarios",Action.Navigate(SearchScope("agenda","calendar",period)))}
                command("Crear evento","evento ")
            }
            "acciones" -> {
                listOf("WhatsApp" to "whatsapp","Sonido" to "sonido","Sistema" to "sistema","Agenda" to "agenda").forEach{(label,value)->out+=row("mode:$value",label,"Explorar acciones",Action.Navigate(SearchScope(value,"topic")))}
                out+=row("mode:files","Archivos","Explorar tipos",Action.SelectMode("Archivos"))
                out+=row("mode:clipboard","Portapapeles","Buscar textos copiados",Action.SelectMode("Portapapeles"))
                out+=row("mode:ai","Inteligencia artificial","Consulta dentro de Spotlight",Action.SelectMode("IA"))
                out+=row("focus","Sesión de concentración","Volumen al 20 % + preparar temporizador de 25 minutos",Action.Routine(25,20))
                out+=row("note","Crear nota","Nota local editable",Action.NewNote())
                command("Temporizador","temporizador de 5 minutos")
                command("Alarma","alarma 07:00")
                command("Traducir texto","traducir en a es: ")
                command("Textos guardados","snippets")
                command("Herramientas de texto","herramientas de texto")
                command("Símbolos y emojis","simbolos")
                command("Generar UUID","generar uuid")
            }
        }
        if(topic=="archivos")return fileMenu(SearchScope("","files-menu"))
        if(topic !in setOf("sonido","sistema","agenda","acciones")) {
            if(topic !in aliases.values)out+=row("$topic:search","Buscar en $title","Actividad compatible o búsqueda web de respaldo",Action.ComposeSearch(topic,packageName ?: ""))
            out+=row("$topic:files","Buscar archivos","Elegir tipo antes de listar",Action.Navigate(SearchScope(topic,"files-menu")))
            if(packageName!=null)out+=row("$topic:info","Información de la app","Permisos y almacenamiento",Action.Launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:$packageName"))))
        }
        return out
    }
    fun fileMenu(scope:SearchScope):List<Result> = listOf("Todos los archivos" to "","Documentos PDF" to "pdf","Documentos" to "document","Imágenes" to "image","Vídeos" to "video","Audio" to "audio","Carpetas" to "folders").map{(label,kind)->row("files:${scope.topic}:$kind",label,if(scope.topic.isBlank())"Almacenamiento compartido" else "Relacionado con ${scope.topic}",Action.Navigate(SearchScope(scope.topic,"files",kind)))}
    fun fileQuery(scope:SearchScope,query:String):String = listOf(if(scope.kind.isBlank())"" else "kind:${scope.kind}",query.trim()).filter{it.isNotBlank()}.joinToString(" ")
    fun searchAction(topic:String,packageName:String,query:String):Action? {
        val q=query.trim();if(q.isBlank())return null
        val url=when(topic){"youtube"->"https://www.youtube.com/results?search_query=";"spotify"->"https://open.spotify.com/search/";"mapas"->"https://www.google.com/maps/search/?api=1&query=";"sofascore"->"https://www.google.com/search?q=";else->"https://www.google.com/search?q="}
        val term=when(topic){"sofascore"->"site:sofascore.com $q resultado";"google","chrome","vivaldi","youtube","spotify","mapas","app"->q;else->"$topic $q"}
        val web=Intent(Intent.ACTION_VIEW,Uri.parse(url+Uri.encode(term)))
        val pkg=packageName.ifBlank {packages[topic].orEmpty()}
        return if(topic=="google" || pkg.isBlank())Action.Launch(web)
        else Action.InApp(if(topic in setOf("youtube","spotify","mapas","chrome","vivaldi"))Intent(web).setPackage(pkg) else Intent(if(topic=="app-web-search")Intent.ACTION_WEB_SEARCH else Intent.ACTION_SEARCH).setPackage(pkg).putExtra(android.app.SearchManager.QUERY,q),web)
    }
}
