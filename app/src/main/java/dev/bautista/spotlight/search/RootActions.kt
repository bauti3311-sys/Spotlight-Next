package dev.bautista.spotlight.search

import android.content.*
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.control.ActionCatalog

/** Local suggestions only. All network/content processing starts after explicit selection. */
object RootActions {
    fun results(context:Context,raw:String,c:Config):List<Result> {
        val q=LocalLanguage.canonical(raw);val rows=mutableListOf<Result>()
        fun add(id:String,title:String,detail:String,category:String,action:Action,score:Int=3400){rows+=Result("root:$id",title,detail,category,score,action)}
        val message=Regex("^(?:mandale|enviale|mandar|enviar)(?: un mensaje)?(?: a)? (.+?) que (.+)$",RegexOption.IGNORE_CASE).matchEntire(raw.trim())
        if(message!=null){val target=message.groupValues[1];val text=message.groupValues[2]
            val pkg=if(c.messageApp=="whatsapp_business")"com.whatsapp.w4b" else "com.whatsapp"
            if(c.messageApp in setOf("whatsapp","whatsapp_business") && context.packageManager.getLaunchIntentForPackage(pkg)==null)
                add("message:missing","WhatsApp no está instalado","Revisá la integración o instalá WhatsApp; no se cambia el canal sin elegirlo","Comunicación",Action.SystemPanel("extension","whatsapp"))
            else add("message","Preparar mensaje para $target","Elegir contacto y revisar el texto; no se envía automáticamente","Comunicación",Action.ComposeMessage(c.messageApp,target,text))
        }
        Regex("^(?:poner |ajustar |cambiar )?(?:el )?(?:brillo|volumen)(?: al| a)? (-?\\d{1,9})\\s*%?$").matchEntire(q)?.let{m->
            val n=m.groupValues[1].toIntOrNull()
            if(n==null || n !in 0..100)add("invalid","Porcentaje inválido","Usá un valor entre 0 y 100; no se modifica el dispositivo","Información",Action.RestoreQuery(raw),4000)
        }
        LocalPlan.step(raw)?.takeIf{it is PlanStep.RelativeBrightness || it is PlanStep.RelativeVolume || it is PlanStep.Media}?.let{add("plan-single","Revisar acción","Comprobar permisos y confirmar antes de ejecutar","Acciones",Action.SystemPanel("interpret",raw))}
        if(q.startsWith("clima"))add("weather",if(q.contains("manana"))"Pronóstico de mañana" else "Consultar clima","Open-Meteo · conexión solo al seleccionar","Clima",Action.SystemPanel("weather-native",if(q.contains("manana"))"tomorrow:" else q.removePrefix("clima").removePrefix(" en ").trim()))
        Regex("^(?:como va|resultado de|partido de) (.+)$").matchEntire(q)?.let{add("sports","Consultar ${it.groupValues[1]}","Elegir equipo o competición real · datos del proveedor","Deportes",Action.SystemPanel("sports-native",it.groupValues[1]))}
        Regex("^(?:buscar |escuchar )?musica(?: de)? (.+?)(?: en (spotify|youtube))?$").matchEntire(q)?.let{m->
            val topic=m.groupValues[2].ifBlank{if(context.packageManager.getLaunchIntentForPackage("com.spotify.music")!=null)"spotify" else "youtube"}
            val term=m.groupValues[1]
            add("music:$topic","Buscar música en ${ActionCatalog.displayName(topic)}",term+" · búsqueda, sin iniciar reproducción","Acciones",Action.ComposeSearch(topic,ActionCatalog.packages[topic].orEmpty(),term))
        }
        if(q in setOf("buscar musica en spotify","buscar musica en youtube")){val topic=q.substringAfterLast(' ');add("music-form","Buscar en ${ActionCatalog.displayName(topic)}","Elegir el contenido a buscar","Acciones",Action.ComposeSearch(topic,ActionCatalog.packages[topic].orEmpty()))}
        if(q.contains("este archivo") || q.contains("este documento") || q.contains("este pdf") || q.contains("este texto") || q in setOf("explicame esto","resumir esto","traducir esto"))
            add("context","Trabajar con contexto autorizado","Elegir o revisar el contenido antes de usar IA","IA",Action.SystemPanel("context-ai",raw),4000)
        if(q in setOf("contexto","contextos","contextos fijados","elegir documento"))add("context-list","Contexto y documentos","Elegir, fijar o quitar contenido autorizado","Contexto",Action.SystemPanel("context"))
        if(q in setOf("diagnostico","diagnostico de spotlight","diagnosticos"))add("diagnostics","Diagnóstico de Spotlight","Permisos, servicios y tiempos observados sin contenido privado","Sistema",Action.SystemPanel("diagnostics"))
        if(q in setOf("modo privado","privacidad"))add("privacy","Modo privado y datos","Suspender captura y aprendizaje sin borrar lo guardado","Ajustes",Action.Launch(Intent(context,dev.bautista.spotlight.MainActivity::class.java).putExtra("group","Privacidad y sistema")))
        if(LocalLanguage.isQuestion(raw) && rows.none{it.id=="root:context"})add("ai","Responder con IA","Requiere conexión · consulta únicamente al seleccionar","IA",Action.Ask(raw,true),1900)
        dev.bautista.spotlight.ui.SettingsGroups.names.filter{relevance(q,it)>=650}.forEach{group->add("settings:$group",group,"Abrir esta sección de ajustes de Spotlight","Ajustes",Action.Launch(Intent(context,dev.bautista.spotlight.MainActivity::class.java).putExtra("group",group)),2100)}
        return rows
    }
}
