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
        val pm=context.packageManager
        val launcher=android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        val available=runCatching{pm.queryIntentActivities(launcher,0)}.getOrDefault(emptyList())
            .filter{it.activityInfo?.packageName!=context.packageName}.distinctBy{it.activityInfo.packageName}
        val learned=if(c.learning && !c.privateMode)c.recent else emptyList()
        val usage=if(c.learning && !c.privateMode)runCatching{org.json.JSONObject(c.usageData)}.getOrDefault(org.json.JSONObject()) else org.json.JSONObject()
        val frequent=usage.keys().asSequence().sortedByDescending{usage.optJSONObject(it)?.optInt("count",0) ?: 0}.toList()
        val packages=(c.favorites.sorted()+learned+frequent).map{it.removePrefix("suggest:app:").substringBefore('/')}.distinct()
        val ordered=available.sortedWith(compareBy<android.content.pm.ResolveInfo>{packages.indexOf(it.activityInfo.packageName).let{n->if(n<0)Int.MAX_VALUE else n}}.thenBy{normalize(it.loadLabel(pm).toString())})
        ordered.take(3).forEach { info ->
            val a=info.activityInfo;val pkg=a.packageName
            val intent=android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER).setComponent(android.content.ComponentName(pkg,a.name))
            val detail=when {
                c.favorites.any{it.substringBefore('/')==pkg}->"Aplicación favorita"
                learned.any{it.substringBefore('/')==pkg}->"Aplicación reciente · aprendizaje local"
                frequent.any{it.substringBefore('/')==pkg}->"Aplicación frecuente · aprendizaje local"
                else->"Aplicación instalada"
            }
            items+=Result("suggest:app:$pkg",info.loadLabel(pm).toString(),detail,"Sugerencias",2200-items.size,Action.Launch(intent),runCatching{info.loadIcon(pm)}.getOrNull())
        }
        if(c.learning && !c.privateMode)c.actionRecent.take(8).forEach { raw ->
            val action=when(val intent=UniversalIntent.recognize(raw)) {
                is UniversalIntent.Volume->Action.Device("volume:${intent.percent}")
                else->dev.bautista.spotlight.next.IntentRouter.route(raw,c.messageApp)?.takeIf{it is Action.SystemPanel && it.page=="display-review"}
            } ?: return@forEach
            row("action:${normalize(raw)}",raw,if(action is Action.Device)"Acción reciente · aplicar al seleccionar" else "Acción reciente · abrir revisión",action)
        }
        if(c.learning && !c.privateMode)runCatching{val sequences=org.json.JSONObject(c.learnedSequences);sequences.keys().asSequence().filter{sequences.getJSONObject(it).optInt("count",0)>=3 && System.currentTimeMillis()-sequences.getJSONObject(it).optLong("last",0)<30L*86400000}.take(1).forEach{raw->if(LocalPlan.parse(raw)!=null)row("sequence:${normalize(raw)}","Repetir secuencia","Hábitos locales · revisar y guardar como comando",Action.SystemPanel("interpret",raw))}}
        if(Replies.connected.value && Replies.targets.value.any{it.packageName=="com.whatsapp"})row("replies","Leer mensajes de WhatsApp","Notificaciones actuales autorizadas",Action.Navigate(SearchScope("whatsapp","replies")))
        if(c.clipboard && hasClips && !c.privateMode)row("clipboard","Consultar portapapeles","Textos capturados con Spotlight en foco",Action.SelectMode("Portapapeles"))
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
