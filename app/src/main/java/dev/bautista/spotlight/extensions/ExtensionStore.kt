package dev.bautista.spotlight.extensions

import android.content.Context
import dev.bautista.spotlight.search.*

object ExtensionStore {
    private fun prefs(context:Context)=context.getSharedPreferences("spotlight_extensions_v1",Context.MODE_PRIVATE)
    fun enabled(context:Context,id:String)=id !in prefs(context).getStringSet("disabled",emptySet()).orEmpty()
    fun setEnabled(context:Context,id:String,enabled:Boolean){require(ExtensionRegistry.extensions.any{it.id==id});val p=prefs(context);val current=p.getStringSet("disabled",emptySet()).orEmpty();p.edit().putStringSet("disabled",if(enabled)current-id else current+id).apply()}
    fun module(result:Result):String?=when(val a=result.action){
        is Action.SystemPanel->when(a.page){"catalog","profiles"->"catalog";"tasker"->"tasker";"weather-native"->"weather-direct";"sports-native"->"sports-direct";"agent"->"ai";"focus"->"focus";"controls","device","display-review","app"->"system";"shizuku"->"shizuku";else->null}
        is Action.ComposeMessage->if(a.channel.startsWith("whatsapp"))"whatsapp" else "contacts"
        is Action.ComposeSearch->when(a.topic){"spotify","youtube","mapas"->a.topic;"sofascore"->"sports";else->"web"}
        is Action.WorkflowRun,is Action.Routine->"workflows"
        is Action.NewNote,is Action.EditNote,is Action.SaveNote->"notes"
        is Action.Ask->"ai"
        is Action.AudioPanel->"media"
        is Action.Navigate->when(a.scope.section){"apps"->"apps";"files","files-mode","files-menu"->"files";"contacts"->"contacts";"calendar"->"calendar";"replies"->"whatsapp";else->null}
        else->when(result.category){"Aplicaciones"->"apps";"Archivos"->"files";"Contactos"->"contacts";"Notas"->"notes";"Portapapeles"->"clipboard";else->null}
    }
    fun allowed(context:Context,result:Result):Boolean {
        val action=result.action
        if(action is Action.SystemPanel && action.page=="profiles") {
            val id=mapOf("estudio" to "study","lectura" to "read","concentracion" to "focus","multimedia" to "media")[action.packageName]
            if(id in context.getSharedPreferences("catalog_v1",Context.MODE_PRIVATE).getStringSet("disabled",emptySet()).orEmpty())return false
        }
        return module(result)?.let{enabled(context,it)} ?: true
    }
}
