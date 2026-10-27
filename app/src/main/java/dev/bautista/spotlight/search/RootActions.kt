package dev.bautista.spotlight.search

import android.content.*
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.control.ActionCatalog

/** Local suggestions only. All network/content processing starts after explicit selection. */
object RootActions {
    fun results(context:Context,raw:String,c:Config):List<Result> {
        if(raw.contains('"') && !LocalLanguage.isQuestion(raw))return emptyList()
        val q=LocalLanguage.canonical(raw).removePrefix("¿").removeSuffix("?").trim();val rows=mutableListOf<Result>()
        fun add(id:String,title:String,detail:String,category:String,action:Action,score:Int=3400){rows+=Result("root:$id",title,detail,category,score,action)}
        UniversalIntentEngine.parseDocumentAiCompound(raw)?.let { docPlan ->
            val provider=UniversalIntentEngine.aiProviderLabel(c)
            val sectionText=if(docPlan.sectionTarget.isNotBlank())"Extraer ${docPlan.sectionTarget}" else "Extraer contenido"
            add("compound-doc-ai","Preparar consulta sobre PDF con $provider","1. Buscar «${docPlan.searchTerms.ifBlank{docPlan.fileKind}}» · 2. $sectionText · 3. Confirmar envío","IA",Action.SystemPanel("interpret",raw),4150)
            return rows
        }
        val registeredMessage=dev.bautista.spotlight.next.IntentRouter.route(raw,c.messageApp) is Action.ComposeMessage || ActionCatalog.message(raw,c.messageApp)!=null
        val message=if(registeredMessage)null else Regex("^(?:mandale|enviale|mandar|enviar)(?: un mensaje)?(?: a)? (.+?) que (.+)$",RegexOption.IGNORE_CASE).matchEntire(raw.trim())
        if(message!=null){val target=message.groupValues[1];val text=message.groupValues[2]
            val pkg=if(c.messageApp=="whatsapp_business")"com.whatsapp.w4b" else "com.whatsapp"
            if(c.messageApp in setOf("whatsapp","whatsapp_business") && context.packageManager.getLaunchIntentForPackage(pkg)==null)
                add("message:missing","WhatsApp no está instalado","Revisá la integración o instalá WhatsApp; no se cambia el canal sin elegirlo","Comunicación",Action.SystemPanel("extensions"))
            else add("message","Preparar mensaje para $target","Elegir contacto y revisar el texto; no se envía automáticamente","Comunicación",Action.ComposeMessage(c.messageApp,target,text))
        }
        Regex("^(?:poner |ajustar |cambiar )?(?:el )?(?:brillo|volumen)(?: al| a)? (-?\\d{1,9})\\s*%?$").matchEntire(q)?.let{m->
            val n=m.groupValues[1].toIntOrNull()
            if(n==null || n !in 0..100)add("invalid","Porcentaje inválido · entre 0 y 100","Usá un valor entre 0 y 100; no se modifica el dispositivo","Información",Action.RestoreQuery(raw),4000)
        }
        LocalPlan.step(raw)?.takeIf{it is PlanStep.RelativeBrightness || it is PlanStep.RelativeVolume || it is PlanStep.Media}?.let{add("plan-single","Revisar acción","Comprobar permisos y confirmar antes de ejecutar","Acciones",Action.SystemPanel("interpret",raw))}
        SportsRequest.parse(raw)?.let{add("sports-agenda","Consultar agenda deportiva","Favoritos y cobertura real del proveedor","Deportes",Action.SystemPanel("sports-native",raw),3900)}
        if(WeatherRequests.matches(raw)) {
            val city=WeatherRequests.extractLocation(raw)
            val tomorrow=Regex("\\bma[nñ]ana\\b",RegexOption.IGNORE_CASE).containsMatchIn(raw) || q.contains("manana")
            val payload=if(tomorrow)"tomorrow:$city" else city
            add("weather-question",if(tomorrow && city.isNotBlank())"Pronóstico de mañana en $city" else "Consultar pronóstico","Open-Meteo · consulta solo al seleccionar","Clima",Action.SystemPanel("weather-native",payload),3900)
        }
        if(q.startsWith("clima")) {
            val tomorrow=Regex("\\bma[nñ]ana\\b",RegexOption.IGNORE_CASE).containsMatchIn(raw)
            val city=WeatherRequests.extractLocation(raw)
            add("weather",if(tomorrow)"Pronóstico de mañana" else "Consultar clima","Open-Meteo · conexión solo al seleccionar","Clima",Action.SystemPanel("weather-native",if(tomorrow)"tomorrow:$city" else city))
        }
        Regex("^¿?(?:c[oó]mo va|resultado de|partido de|c[oó]mo sali[oó]|cu[aá]ndo juega) (.+?)\\??$",RegexOption.IGNORE_CASE).matchEntire(raw.trim())?.let{add("sports","Consultar ${it.groupValues[1]}","Elegir equipo o competición real · datos del proveedor","Deportes",Action.SystemPanel("sports-native",it.groupValues[1]))}
        Regex("^(?:buscar |escuchar |pon[eé] |poner |reproducir )?m[uú]sica(?: de)? (.+?)(?: en (spotify|youtube))?$",RegexOption.IGNORE_CASE).matchEntire(raw.trim())?.let{m->
            val topic=m.groupValues[2].lowercase().ifBlank{if(context.packageManager.getLaunchIntentForPackage("com.spotify.music")!=null)"spotify" else "youtube"}
            val term=m.groupValues[1]
            add("music:$topic","Buscar música en ${ActionCatalog.displayName(topic)}",term+" · búsqueda, sin iniciar reproducción","Acciones",Action.ComposeSearch(topic,ActionCatalog.packages[topic].orEmpty(),term))
        }
        if(q in setOf("buscar musica en spotify","buscar musica en youtube")){val topic=q.substringAfterLast(' ');add("music-form","Buscar en ${ActionCatalog.displayName(topic)}","Elegir el contenido a buscar","Acciones",Action.ComposeSearch(topic,ActionCatalog.packages[topic].orEmpty()))}
        if(Regex("\\bestos(?: +\\d{1,2})? +(documentos|archivos|pdfs)\\b").containsMatchIn(q))add("selection","Trabajar con documentos seleccionados","Extracción local y revisión antes de enviar a IA","IA",Action.SystemPanel("selection-ai",raw),4200)
        if(q.contains("este archivo") || q.contains("este documento") || q.contains("este pdf") || q.contains("este texto") || q.contains("este portapapeles") || q in setOf("explicame esto","resumir esto","resumi esto","traducir esto","traduci esto","corregi esto","corregir esto","corregime esto","analiza esto","analizame esto"))
            add("context","Trabajar con contexto autorizado","Elegir o revisar el contenido antes de usar IA","IA",Action.SystemPanel("context-ai",raw),4000)
        if(q in setOf("pomodoro","concentracion","sesion de concentracion"))add("focus","Concentración","Temporizador local · abrir opciones", "Acciones",Action.SystemPanel(if(dev.bautista.spotlight.extensions.ExtensionStore.enabled(context,"focus"))"focus" else "extensions"),2900)
        if(q in setOf("calendario","mi calendario","agenda","mi agenda","que tengo manana","que tengo hoy") || dev.bautista.spotlight.control.CalendarLanguage.draft(raw)!=null || dev.bautista.spotlight.control.CalendarLanguage.free(raw)!=null)add("calendar","Calendario","Calendarios sincronizados · permisos opcionales","Agenda",Action.SystemPanel("calendar",raw),4100)
        if(q in setOf("guardados","mis guardados","textos frecuentes"))add("saved","Guardados","Notas, textos, enlaces y documentos fijados","Guardados",Action.SystemPanel("saved"),3800)
        if(q in setOf("contexto","contextos","contextos fijados","elegir documento"))add("context-list","Contexto y documentos","Elegir, fijar o quitar contenido autorizado","Contexto",Action.SystemPanel("context"))
        if(q in setOf("diagnostico","diagnostico de spotlight","diagnosticos"))add("diagnostics","Diagnóstico de Spotlight","Permisos, servicios y tiempos observados sin contenido privado","Sistema",Action.SystemPanel("diagnostics"))
        if(q in setOf("modo privado","privacidad"))add("privacy","Modo privado y datos","Suspender captura y aprendizaje sin borrar lo guardado","Ajustes",Action.Launch(Intent(context,dev.bautista.spotlight.MainActivity::class.java).putExtra("group","Privacidad y sistema")))
        if(LocalLanguage.isQuestion(raw) && UniversalIntent.recognize(raw) !is UniversalIntent.Information && rows.isEmpty()) {
            val aiTitle=UniversalIntentEngine.primaryAiTitle(c)
            add("ai",aiTitle,"Requiere conexión · consulta únicamente al seleccionar","IA",Action.Ask(raw,!c.aiRootConsent),3850)
        }
        dev.bautista.spotlight.ui.SettingsGroups.names.filter{relevance(q,it)>=650}.forEach{group->add("settings:$group",group,"Abrir esta sección de ajustes de Spotlight","Ajustes",Action.Launch(Intent(context,dev.bautista.spotlight.MainActivity::class.java).putExtra("group",group)),2100)}
        return rows
    }
}
