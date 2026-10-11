package dev.bautista.spotlight.search

import android.content.Context
import dev.bautista.spotlight.ai.DirectAi
import dev.bautista.spotlight.control.Replies
import dev.bautista.spotlight.control.SearchScope
import dev.bautista.spotlight.data.Config

/** Local contextual shortcuts, bounded to four; learning preferences gate usage history. */
object HomeSuggestions {
    fun results(context:Context,c:Config,filesAllowed:Boolean,hasClips:Boolean):List<Result> {
        if(!c.suggestions)return emptyList()
        val items=mutableListOf<Result>()
        fun row(id:String,title:String,subtitle:String,action:Action){items+=Result("suggest:$id",title,subtitle,"Sugerencias",2000-items.size,action)}
        val learned=if(c.learning)c.recent else emptyList()
        val packages=(c.favorites.sorted()+learned).map { it.substringBefore('/') }.distinct().take(12)
        packages.forEach { pkg ->
            val intent=context.packageManager.getLaunchIntentForPackage(pkg) ?: return@forEach
            val info=runCatching{context.packageManager.getApplicationInfo(pkg,0)}.getOrNull() ?: return@forEach
            items+=Result("suggest:app:$pkg",info.loadLabel(context.packageManager).toString(),if(pkg in c.favorites.map{it.substringBefore('/')})"Aplicación favorita" else "Aplicación reciente · aprendizaje local","Sugerencias",2200-items.size,Action.Launch(intent),info.loadIcon(context.packageManager))
        }
        if(c.learning)c.actionRecent.take(8).forEach { raw ->
            val action=when(val intent=UniversalIntent.recognize(raw)) {
                is UniversalIntent.Volume->Action.Device("volume:${intent.percent}")
                else->dev.bautista.spotlight.next.IntentRouter.route(raw,c.messageApp)?.takeIf{it is Action.SystemPanel && it.page=="display-review"}
            } ?: return@forEach
            row("action:${normalize(raw)}",raw,if(action is Action.Device)"Acción reciente · aplicar al seleccionar" else "Acción reciente · abrir revisión",action)
        }
        if(Replies.connected.value && Replies.targets.value.any{it.packageName=="com.whatsapp"})row("replies","Leer mensajes de WhatsApp","Notificaciones actuales autorizadas",Action.Navigate(SearchScope("whatsapp","replies")))
        if(DirectAi(context).configured(c))row("ai","Preguntar a la IA","Respuesta dentro de Spotlight",Action.RestoreQuery("buscar información sobre "))
        if(c.files && filesAllowed)row("files","Buscar un archivo","PDF, imágenes, audio y documentos",Action.SelectMode("Archivos"))
        row("note","Crear una nota","Guardar texto localmente",Action.NewNote())
        row("apps","Buscar una aplicación","Apps instaladas y acciones públicas",Action.SelectMode("Apps"))
        if(context.packageManager.getLaunchIntentForPackage("com.whatsapp")!=null)row("message","Enviar mensaje","Elegir contacto y revisar",Action.ComposeMessage("whatsapp"))
        if(c.clipboard && hasClips)row("clipboard","Consultar portapapeles","Textos capturados con Spotlight en foco",Action.SelectMode("Portapapeles"))
        row("commands","Explorar comandos","Sistema y herramientas locales",Action.SelectMode("Acciones"))
        return items.distinctBy{it.id}.filter{item ->
            val module=when {
                item.id.startsWith("suggest:app:") || item.id=="suggest:apps"->"apps"
                item.action is Action.Device->"media"
                else->dev.bautista.spotlight.extensions.ExtensionStore.module(item)
            }
            item.id !in c.hiddenSuggestions && (module==null || dev.bautista.spotlight.extensions.ExtensionStore.enabled(context,module))
        }.sortedByDescending{it.id in c.pinnedSuggestions}.take(4).mapIndexed { i,item->item.copy(score=2200-i) }
    }
}
