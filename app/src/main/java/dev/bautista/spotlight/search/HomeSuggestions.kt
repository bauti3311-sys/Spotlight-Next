package dev.bautista.spotlight.search

import android.content.Context
import dev.bautista.spotlight.ai.DirectAi
import dev.bautista.spotlight.control.Replies
import dev.bautista.spotlight.control.SearchScope
import dev.bautista.spotlight.data.Config

/** Contextual commands, never an activity/search history projection. */
object HomeSuggestions {
    fun results(context:Context,c:Config,filesAllowed:Boolean,hasClips:Boolean):List<Result> {
        if(!c.suggestions)return emptyList()
        val items=mutableListOf<Result>()
        fun row(id:String,title:String,subtitle:String,action:Action){items+=Result("suggest:$id",title,subtitle,"Sugerencias",2000-items.size,action)}
        if(Replies.connected.value && Replies.targets.value.any{it.packageName=="com.whatsapp"})row("replies","Leer mensajes de WhatsApp","Notificaciones actuales autorizadas",Action.Navigate(SearchScope("whatsapp","replies")))
        if(DirectAi(context).configured(c))row("ai","Preguntar a la IA","Respuesta dentro de Spotlight",Action.SelectMode("IA"))
        if(c.files && filesAllowed)row("files","Buscar un archivo","PDF, imágenes, audio y documentos",Action.SelectMode("Archivos"))
        row("note","Crear una nota","Guardar texto localmente",Action.NewNote())
        row("apps","Buscar una aplicación","Apps instaladas y acciones públicas",Action.SelectMode("Apps"))
        if(context.packageManager.getLaunchIntentForPackage("com.whatsapp")!=null)row("message","Enviar mensaje","Elegir contacto y revisar",Action.ComposeMessage("whatsapp"))
        if(c.clipboard && hasClips)row("clipboard","Consultar portapapeles","Textos capturados con Spotlight en foco",Action.SelectMode("Portapapeles"))
        row("commands","Explorar comandos","Sistema, herramientas y rutinas",Action.SelectMode("Acciones"))
        return items.filter{it.id !in c.hiddenSuggestions}.sortedByDescending{it.id in c.pinnedSuggestions}.take(5)
    }
}
