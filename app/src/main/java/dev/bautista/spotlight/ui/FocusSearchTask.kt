package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.ActionCatalog
import dev.bautista.spotlight.search.Action
import org.json.*

/** Editor values only. Never serialize an Intent, a grant, or a prepared confirmation. */
internal class FocusEditors(initial:String="") {
 var global by mutableStateOf(TextFieldValue(initial,TextRange(initial.length)))
 val drafts=mutableStateMapOf<String,TextFieldValue>()
 fun value(draft:Action.ComposeSearch)=drafts[key(draft)] ?: TextFieldValue(draft.query,TextRange(draft.query.length))
 fun edit(draft:Action.ComposeSearch,value:TextFieldValue){drafts[key(draft)]=value}
 private fun key(draft:Action.ComposeSearch)=draft.topic+"|"+draft.packageName
 companion object {
  fun saver(privateMode:Boolean)=Saver<FocusEditors,String>(save={state->if(privateMode)null else JSONObject().apply {
   fun encoded(v:TextFieldValue)=JSONArray(listOf(v.text.take(4000),v.selection.start,v.selection.end))
   put("global",encoded(state.global));put("drafts",JSONObject().apply{state.drafts.entries.toList().takeLast(8).forEach{put(it.key,encoded(it.value))}})
  }.toString()},restore={raw->runCatching {
   require(raw.length<=40000);val j=JSONObject(raw)
   fun decoded(a:JSONArray):TextFieldValue {val text=a.getString(0).take(4000);return TextFieldValue(text,TextRange(a.getInt(1).coerceIn(0,text.length),a.getInt(2).coerceIn(0,text.length)))}
   FocusEditors().apply{global=decoded(j.getJSONArray("global"));val d=j.getJSONObject("drafts");require(d.length()<=8);d.keys().forEach{require(it.length<=400);drafts[it]=decoded(d.getJSONArray(it))}}
  }.getOrDefault(FocusEditors())})
 }
}
/** A presentation review over the existing ActionCatalog and existing dispatcher. Not an action engine. */
internal class FocusSearchReviewController {
 var prepared by mutableStateOf<Action?>(null)
 var information by mutableStateOf(false)
 var error by mutableStateOf("")
 fun invalidate(){prepared=null;error=""}
 fun prepare(draft:Action.ComposeSearch){prepared=ActionCatalog.searchAction(draft.topic,draft.packageName,draft.query);error=if(prepared==null)"Escribí una búsqueda antes de revisar." else ""}
 fun take():Action?=prepared.also{prepared=null}
}
internal fun focusSearchPlaceholder(topic:String)=when(topic){
 "spotify"->"Buscar canción o artista"
 "vivaldi","chrome","google","app-web-search"->"Buscar en la web"
 "youtube"->"Buscar videos"
 "mapas"->"Buscar lugar"
 "sofascore"->"Buscar equipo o resultado"
 else->"Buscar en la aplicación"
}
internal fun focusSearchActionTitle(topic:String)=when(topic){
 "spotify"->"Buscar música"
 "vivaldi","chrome","google","app-web-search"->"Buscar en la web"
 "youtube"->"Buscar videos"
 "mapas"->"Buscar lugar"
 else->"Buscar en la aplicación"
}
@Composable internal fun FocusSearchTaskBody(app:SpotlightApp,draft:Action.ComposeSearch,controller:FocusSearchReviewController,authorizedSpotify:Boolean,spotifyTrigger:Int) {
 val colors=MaterialTheme.colorScheme
 if(authorizedSpotify) {
  SpotifySearchPanel(app,draft.query,externalQuery=draft.query,searchTrigger=spotifyTrigger)
  return
 }
 Column(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
  val plan=controller.prepared
  if(plan!=null)Row(Modifier.fillMaxWidth().testTag("focus-action-review"),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
   Column(Modifier.weight(1f)){Text("Acción",style=MaterialTheme.typography.labelMedium,color=colors.onSurfaceVariant);Text(focusSearchActionTitle(draft.topic),style=MaterialTheme.typography.titleSmall)}
   val intent=when(plan){is Action.InApp->plan.intent;is Action.Launch->plan.intent;else->null}
   Column(Modifier.weight(1f)){Text("Destino",style=MaterialTheme.typography.labelMedium,color=colors.onSurfaceVariant);Text(intent?.data?.host ?: draft.packageName,style=MaterialTheme.typography.bodySmall)}
  } else Text(if(draft.query.isBlank())if(draft.topic=="spotify")"Escribí una canción o un artista para buscar." else "Escribí lo que querés encontrar." else "Revisá la acción antes de continuar.",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
  if(controller.information)Text(if(draft.topic=="spotify")"Enlace público de Spotify. Sin consultas a tu cuenta ni resultados internos desde Spotlight." else "Búsqueda pública en la aplicación elegida. No se leen datos privados.",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
  if(controller.error.isNotBlank())Text(controller.error,color=colors.error,style=MaterialTheme.typography.bodySmall)
 }
}
@Composable internal fun FocusSearchTaskFooter(draft:Action.ComposeSearch,controller:FocusSearchReviewController,authorizedSpotify:Boolean,onSpotifySearch:()->Unit,onRun:(Action)->Unit) {
 Row(Modifier.fillMaxWidth().heightIn(min=48.dp).padding(horizontal=12.dp).testTag("context-footer"),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
  if(authorizedSpotify)Text("Conexión autorizada",Modifier.padding(horizontal=8.dp),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
  else TextButton(onClick={controller.information=!controller.information},modifier=Modifier.heightIn(min=48.dp)){Text(if(draft.topic in setOf("app","app-web-search"))"Cómo funciona" else "Enlace público",style=MaterialTheme.typography.labelMedium)}
  Spacer(Modifier.weight(1f))
  TextButton(enabled=draft.query.isNotBlank(),onClick={
   if(authorizedSpotify)onSpotifySearch() else if(controller.prepared==null)controller.prepare(draft) else controller.take()?.let(onRun)
  },modifier=Modifier.heightIn(min=48.dp).testTag("focus-prepare")) {
   Text(if(authorizedSpotify)"Buscar" else if(controller.prepared==null)"Revisar" else "Abrir búsqueda",style=MaterialTheme.typography.labelLarge)
  }
 }
}
