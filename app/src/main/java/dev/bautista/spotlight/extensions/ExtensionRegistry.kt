package dev.bautista.spotlight.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.SearchScope
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*

enum class ExtensionIcon { APPS,FILES,PEOPLE,CHAT,CALENDAR,MAPS,MUSIC,WEB,WEATHER,SPORTS,AI,NOTE,SNIPPET,CLIPBOARD,WORKFLOW,TOOLS,SETTINGS,SYSTEM,SHIELD }
data class CommandArgument(val name:String,val type:String,val required:Boolean=true)
data class RegisteredCommand(val id:String,val title:String,val action:Action,val arguments:List<CommandArgument> = emptyList())
data class ExtensionRequirement(val permissions:Set<String> = emptySet(),val packageName:String?=null,val online:Boolean=false,val ai:Boolean=false,val shizuku:Boolean=false)
data class ExtensionDefinition(val id:String,val name:String,val icon:ExtensionIcon,val category:String,val commands:List<RegisteredCommand>,val requirements:ExtensionRequirement=ExtensionRequirement(),val explanation:String,val toolIds:Set<String> = emptySet(),val advancedAdapter:String?=null) {val registeredTools get()=dev.bautista.spotlight.ai.ToolRegistry.definitions.filter{it.extension==id}}
enum class ExtensionAvailability(val label:String){AVAILABLE("Disponible"),PERMISSION("Permiso pendiente"),DEPENDENCY("Aplicación no instalada"),CONFIGURATION("Configuración pendiente"),ADVANCED("Shizuku pendiente"),DISABLED("Desactivada")}
data class ExtensionSnapshot(val definition:ExtensionDefinition,val availability:ExtensionAvailability)

/** Internal extensions only: actions remain typed and cannot download code or acquire privileges. */
object ExtensionRegistry {
    private val genericVerbStopwords = setOf("buscar","busca","buscame","abrir","abri","abre","ver","mostrar","mostrame","consultar","explorar","preparar","iniciar")
    private fun command(id:String,title:String,action:Action,args:List<CommandArgument> = emptyList())=RegisteredCommand(id,title,action,args)
    private fun topic(id:String,name:String,icon:ExtensionIcon,pkg:String)=ExtensionDefinition(id,name,icon,"Aplicaciones",listOf(command("$id.search","Buscar contenido",Action.ComposeSearch(id,pkg),listOf(CommandArgument("consulta","texto")))),explanation="Búsqueda documentada o respaldo web; sin controlar datos privados de la app.")
    val extensions:List<ExtensionDefinition> = listOf(
        ExtensionDefinition("catalog","Centro de comandos",ExtensionIcon.WORKFLOW,"Productividad",listOf(command("catalog.open","Explorar packs y perfiles",Action.SystemPanel("catalog"))),explanation="30 acciones reales y cuatro perfiles manuales; sin eventos automáticos ocultos."),
        ExtensionDefinition("tasker","Tasker",ExtensionIcon.WORKFLOW,"Automatización",listOf(command("tasker.open","Descubrir tareas",Action.SystemPanel("tasker"))),ExtensionRequirement(packageName="net.dinglisch.android.taskerm"),"Proveedor oficial, parámetros, revisión y confirmación de finalización; requiere acceso externo en Tasker."),
        ExtensionDefinition("weather-direct","Clima nativo",ExtensionIcon.WEATHER,"Información",listOf(command("weather-direct.open","Pronóstico de Open-Meteo",Action.SystemPanel("weather-native"))),explanation="Sin clave ni backend. Localidad seleccionable, pronóstico horario y diario, caché sin conexión."),
        ExtensionDefinition("sports-direct","Agenda deportiva nativa",ExtensionIcon.SPORTS,"Información",listOf(command("sports-direct.open","Consultar agenda",Action.SystemPanel("sports-native"))),explanation="TheSportsDB: equipos y competiciones favoritos, consultas dirigidas y caché. Cobertura limitada según plan; no garantiza datos en vivo."),
        ExtensionDefinition("apps","Aplicaciones",ExtensionIcon.APPS,"Aplicaciones",listOf(command("apps.open","Explorar aplicaciones",Action.SelectMode("Apps"))),explanation="Iconos reales, cinco recientes y catálogo alfabético. Acciones públicas mediante Android.",advancedAdapter="app-inspector"),
        ExtensionDefinition("files","Archivos",ExtensionIcon.FILES,"Archivos",listOf(command("files.search","Buscar archivos",Action.SelectMode("Archivos"))),explanation="Almacenamiento compartido autorizado, filtros y vistas previas. No lee datos privados de otras apps."),
        ExtensionDefinition("contacts","Contactos",ExtensionIcon.PEOPLE,"Comunicación",listOf(command("contacts.search","Buscar contactos",Action.Navigate(SearchScope("","contacts")))),ExtensionRequirement(permissions=setOf(Manifest.permission.READ_CONTACTS)),"Selector de coincidencias; los nombres repetidos no se fusionan."),
        ExtensionDefinition("whatsapp","WhatsApp",ExtensionIcon.CHAT,"Comunicación",listOf(command("whatsapp.message","Preparar mensaje",Action.ComposeMessage("whatsapp"),listOf(CommandArgument("destinatario","contacto"),CommandArgument("texto","texto"))),command("whatsapp.notifications","Mensajes de notificaciones",Action.Navigate(SearchScope("whatsapp","replies")))),ExtensionRequirement(packageName="com.whatsapp"),"Borrador por enlace documentado; lectura y respuesta de notificaciones requieren su permiso y RemoteInput compatible."),
        ExtensionDefinition("calendar","Calendario",ExtensionIcon.CALENDAR,"Comunicación",listOf(command("calendar.agenda","Agenda",Action.Navigate(SearchScope("agenda","calendar","week"))),command("calendar.create","Preparar evento",Action.RestoreQuery("evento "))),ExtensionRequirement(permissions=setOf(Manifest.permission.READ_CALENDAR)),"Consulta local autorizada; crear eventos abre la revisión de Calendario."),
        topic("mapas","Mapas",ExtensionIcon.MAPS,"com.google.android.apps.maps"),
        ExtensionDefinition("media","Multimedia",ExtensionIcon.MUSIC,"Sistema",listOf(command("media.volume","Volumen multimedia",Action.AudioPanel())),explanation="Controles públicos de Android; disponibilidad multimedia depende de la sesión activa."),
        topic("spotify","Spotify",ExtensionIcon.MUSIC,"com.spotify.music"),
        topic("youtube","YouTube",ExtensionIcon.WEB,"com.google.android.youtube"),
        ExtensionDefinition("web","Web",ExtensionIcon.WEB,"Web",listOf(command("web.search","Buscar en Google",Action.ComposeSearch("google",""))),explanation="Consulta en navegador predeterminado, sin configurar URLs."),
        ExtensionDefinition("weather","Clima",ExtensionIcon.WEATHER,"Información",listOf(command("weather.query","Consultar clima",Action.RestoreQuery("clima en "))),ExtensionRequirement(online=true),"Proveedor meteorológico real; necesita conexión y tarjetas online activadas."),
        ExtensionDefinition("sports","Deportes",ExtensionIcon.SPORTS,"Información",listOf(command("sports.search","Buscar equipo en Sofascore",Action.ComposeSearch("sofascore","com.sofascore.results"))),explanation="Búsqueda de contenido en app o web. Las tarjetas en vivo requieren proveedor adicional configurado."),
        ExtensionDefinition("ai","Inteligencia artificial",ExtensionIcon.AI,"IA",listOf(command("ai.ask","Preguntar a IA",Action.SelectMode("IA"))),ExtensionRequirement(ai=true),"OpenAI/Gemini con clave privada local; el chat se conserva en memoria."),
        ExtensionDefinition("notes","Notas",ExtensionIcon.NOTE,"Productividad",listOf(command("notes.create","Crear nota",Action.NewNote()),command("notes.search","Buscar notas",Action.RestoreQuery("notas"))),explanation="Notas locales editables; no necesita conexión."),
        ExtensionDefinition("snippets","Textos frecuentes",ExtensionIcon.SNIPPET,"Productividad",listOf(command("snippets.search","Buscar fragmentos",Action.RestoreQuery("snippets"))),explanation="Plantillas con fecha, hora y campos. Copia mediante el portapapeles normal."),
        ExtensionDefinition("clipboard","Portapapeles",ExtensionIcon.CLIPBOARD,"Productividad",listOf(command("clipboard.search","Ver textos capturados",Action.SelectMode("Portapapeles"))),explanation="Captura con Spotlight enfocado; historial temporal con caducidad. No observa otras apps."),
        ExtensionDefinition("workflows","Rutinas",ExtensionIcon.WORKFLOW,"Productividad",listOf(command("workflows.search","Explorar rutinas",Action.RestoreQuery("rutinas"))),explanation="Pasos locales registrados, revisión y cancelación; sin ejecución de terminal.",advancedAdapter="workflow-advanced"),
        ExtensionDefinition("focus","Concentración",ExtensionIcon.WORKFLOW,"Productividad",listOf(command("focus.start","Concentración y Pomodoro",Action.SystemPanel("focus"))),explanation="Sesiones locales con pausas; Reloj para alarma fuera de la barra. No modifica notificaciones."),
        ExtensionDefinition("tools","Herramientas",ExtensionIcon.TOOLS,"Herramientas",listOf(command("tools.text","Herramientas de texto",Action.RestoreQuery("herramientas de texto")),command("tools.timer","Preparar temporizador",Action.TimerForm())),explanation="Calculadora, unidades, transformaciones de texto y Reloj; funciones locales sin conexión."),
        ExtensionDefinition("settings","Ajustes",ExtensionIcon.SETTINGS,"Sistema",listOf(command("settings.public","Ajustes de Android",Action.Navigate(SearchScope("sistema","topic")))),explanation="Intents públicos compatibles; no requiere Shizuku."),
        ExtensionDefinition("system","Dispositivo",ExtensionIcon.SYSTEM,"Sistema",listOf(command("system.info","Información del dispositivo",Action.SystemPanel("device")),command("system.controls","Sistema y controles avanzados",Action.SystemPanel("controls"))),explanation="Diagnóstico público. Los cambios de pantalla requieren conexión ADB, validación y confirmación.",advancedAdapter="reversible-settings"),
        ExtensionDefinition("shizuku","Shizuku",ExtensionIcon.SHIELD,"Sistema",listOf(command("shizuku.setup","Configurar Shizuku",Action.SystemPanel("shizuku"))),explanation="API oficial, autorización explícita y conexión bajo demanda. La búsqueda estándar sigue funcionando.",advancedAdapter="shizuku-manager")
    )
    fun searchCommands(context:Context,query:String,c:Config):List<Result> {
        val normQuery=normalize(query.trim())
        // Bare action verbs ("buscar", "abrir", "ver") must not dump indistinguishable extension commands.
        if(normQuery in genericVerbStopwords)return emptyList()
        return extensions.filter{ExtensionStore.enabled(context,it.id) && it.id !in setOf("catalog","focus","snippets","workflows") && (it.id!="tasker" || normQuery.contains("tasker"))}.flatMap { e ->
            e.commands.mapNotNull { command ->
                val displayTitle=if(command.title=="Buscar contenido")"Buscar en ${e.name}" else command.title
                val match=maxOf(relevance(query,displayTitle),relevance(query,e.name))
                if(match<500)return@mapNotNull null
                // If the user typed "buscar <algo>", require that <algo> actually matches the extension or command target.
                if(normQuery.startsWith("buscar ") && relevance(normQuery.removePrefix("buscar ").trim(),e.name)<500 && relevance(normQuery,displayTitle)<620)return@mapNotNull null
                val missing=e.requirements.packageName?.let{context.packageManager.getLaunchIntentForPackage(it)==null} ?: false
                val permission=e.requirements.permissions.firstOrNull{ContextCompat.checkSelfPermission(context,it)!=PackageManager.PERMISSION_GRANTED}
                val action=when{missing->Action.SystemPanel("extensions");permission==Manifest.permission.READ_CONTACTS->Action.Access("contacts");permission==Manifest.permission.READ_CALENDAR->Action.Access("calendar");else->command.action}
                val subtitle=when{missing->"Aplicación no instalada · revisar alternativas";permission!=null->"Permiso pendiente · autorizar al seleccionar";command.title=="Buscar contenido"->"${e.name} · ${e.explanation}";else->e.explanation}
                Result("registered:"+command.id,displayTitle,subtitle,e.category,match+300,action)
            }
        }
    }
    fun resolve(id:String):RegisteredCommand?=extensions.flatMap{it.commands}.singleOrNull{it.id==id}
    suspend fun snapshot(context:Context,config:Config):List<ExtensionSnapshot> = withContext(Dispatchers.IO){
        val tasker=dev.bautista.spotlight.next.TaskerAdapter(context).discover()
        extensions.map{e->val r=e.requirements;val availability=when{
            !ExtensionStore.enabled(context,e.id)->ExtensionAvailability.DISABLED
            e.id=="tasker" && !tasker.available->if(tasker.packageName==null)ExtensionAvailability.DEPENDENCY else ExtensionAvailability.CONFIGURATION
            e.id=="contacts" && !config.contacts->ExtensionAvailability.CONFIGURATION
            e.id=="files" && !config.files->ExtensionAvailability.CONFIGURATION
            e.id!="tasker" && r.packageName!=null && runCatching{context.packageManager.getApplicationInfo(r.packageName,0)}.isFailure->ExtensionAvailability.DEPENDENCY
            r.permissions.any{ContextCompat.checkSelfPermission(context,it)!=PackageManager.PERMISSION_GRANTED}->ExtensionAvailability.PERMISSION
            r.online && !config.online->ExtensionAvailability.CONFIGURATION
            r.ai && !dev.bautista.spotlight.ai.DirectAi(context).configured(config)->ExtensionAvailability.CONFIGURATION
            r.shizuku && (context.applicationContext as? SpotlightApp)?.shizuku?.state?.value?.available!=true->ExtensionAvailability.ADVANCED
            else->ExtensionAvailability.AVAILABLE
        };ExtensionSnapshot(e,availability)}
    }
    fun results(query:String):List<Result> = if(normalize(query.trim()) in setOf("extensiones","integraciones","administrar extensiones","administrar integraciones"))listOf(Result("extensions:registry","Extensiones e integraciones","Comandos, permisos y compatibilidad real","Comandos",2450,Action.SystemPanel("extensions"))) else emptyList()
}
