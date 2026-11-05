package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.search.*
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Entire review is test-source-only; no production state owner or dispatcher replaced. */
internal object FocusSearchType {
 private fun style(size:Int,line:Int,weight:FontWeight=FontWeight.Normal)=TextStyle(fontFamily=Geist,fontSize=size.sp,lineHeight=line.sp,fontWeight=weight)
 val query=style(22,30)
 val contextual=style(16,24)
 val title=style(18,24,FontWeight.SemiBold)
 val result=style(16,22,FontWeight.SemiBold)
 val subtitle=style(13,18)
 val metadata=style(12,16)
 val shortcut=style(11,14)
 val section=style(11,16,FontWeight.SemiBold).copy(letterSpacing=.8.sp)
}
internal data class FocusSearchContext(val item:Result,val draft:Action.ComposeSearch,val mechanism:AppCapabilityMechanism) {
 val name get()=item.title
 val placeholder get()=if(draft.packageName=="com.spotify.music")"Buscar canción o artista" else "Buscar en la web"
}
internal class FocusSearchState {
 var global by mutableStateOf(TextFieldValue(""));var term by mutableStateOf(TextFieldValue(""))
 var context by mutableStateOf<FocusSearchContext?>(null)
 var selected by mutableIntStateOf(0)
 var lastTapped:String?=null
 val list=LazyListState()
 var rows by mutableStateOf(emptyList<Result>())
 var review by mutableStateOf<Action.InApp?>(null)
 var error by mutableStateOf("")
 val drafts=mutableMapOf<String,TextFieldValue>()
}

@Composable internal fun FocusSearchReview(app:SpotlightApp,config:Config,state:FocusSearchState,onRequest:(Action)->Unit) {
 val c=MaterialTheme.colorScheme
 val focus=remember{FocusRequester()};val scope=rememberCoroutineScope();val list=state.list
 val registry=remember(app){AppCapabilities(app)}
 val context=state.context
 val accent=context?.let { PremiumMaterial.accent(c,if(it.draft.packageName=="com.spotify.music")Color(0xFF65D993) else Color(0xFFDE6D72)).primary } ?: c.onSurfaceVariant
 fun back() {
  context?.let{state.drafts[it.draft.packageName]=state.term}
  state.context=null;state.review=null;state.error="";focus.requestFocus()
 }
 fun prepare() {
  val active=state.context ?: return
  val term=state.term.text
  state.review=null
  scope.launch {
   try {
    val action=withContext(Dispatchers.IO) {
     val fresh=registry.prepare(active.draft.packageName,"search") as Action.ComposeSearch
     check(fresh==active.draft){"La capacidad cambió. Volvé a resultados."}
     val candidate=ActionCatalog.searchAction(fresh.topic,fresh.packageName,term) as? Action.InApp
      ?: error("Escribí una búsqueda antes de revisar.")
     check(IntegrationRegistry(app).resolves(candidate.intent)){"La aplicación ya no está disponible. Volvé a resultados."}
     candidate
    }
    if(state.context==active && state.term.text==term){state.review=action;state.error=""}
   } catch(e:kotlinx.coroutines.CancellationException){throw e} catch(e:Exception){if(state.context==active)state.error=e.message ?: "No disponible"}
  }
 }
 fun enter(item:Result) {
  val pkg=(item.action as? Action.ComposeSearch)?.packageName ?: return
  val installed=state.rows.firstOrNull{(it.action as? Action.Launch)?.intent?.component?.packageName==pkg} ?: return
  val globalSnapshot=state.global.text
  scope.launch {
   try {
    val capability=withContext(Dispatchers.IO){registry.available(pkg).singleOrNull{it.id=="search"} ?: error("Esta app no publica una búsqueda compatible.")}
    val draft=withContext(Dispatchers.IO){registry.prepare(pkg,"search") as Action.ComposeSearch}
    if(state.global.text!=globalSnapshot)return@launch
    state.review=null;state.error="";state.term=state.drafts[pkg] ?: TextFieldValue("")
    state.context=FocusSearchContext(installed,draft,capability.mechanism);focus.requestFocus()
   } catch(e:kotlinx.coroutines.CancellationException){throw e} catch(e:Exception){state.error=e.message ?: "No disponible"}
  }
 }
 LaunchedEffect(state.global.text) {
  val q=state.global.text
  app.search.search(q,config).collect { raw ->
   state.rows=CompactResults.select(q,raw.filter(ProductVisibility::allowed),config,true)
     .visible(false,adaptive=q.isNotBlank())
   state.selected=state.selected.coerceIn(0,(state.rows.size-1).coerceAtLeast(0))
  }
 }
 LaunchedEffect(Unit){focus.requestFocus()}
 Box(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=12.dp).testTag("focus-footprint")) {
 Column(Modifier.fillMaxWidth().heightIn(max=676.dp).testTag("focus-window").clip(RoundedCornerShape(22.dp))
  .background(Brush.verticalGradient(listOf(c.surface,c.surfaceContainerLow)))
  .onPreviewKeyEvent { e ->
   if(e.type!=KeyEventType.KeyDown)false else when(e.key) {
    Key.Escape,Key.Back->{if(state.review!=null)state.review=null else if(state.context!=null)back();true}
    Key.DirectionDown->if(context==null){state.selected=(state.selected+1).coerceAtMost((state.rows.size-1).coerceAtLeast(0));true}else false
    Key.DirectionUp->if(context==null){state.selected=(state.selected-1).coerceAtLeast(0);true}else false
    Key.Enter->{if(context!=null)prepare() else state.rows.getOrNull(state.selected)?.let{if(it.action is Action.ComposeSearch)enter(it) else onRequest(it.action)};true}
    else->false
   }
  }) {
  Row(Modifier.fillMaxWidth().heightIn(min=76.dp).padding(horizontal=16.dp,vertical=12.dp).testTag("focus-header"),
   verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   if(context!=null)IconButton(onClick={back()},modifier=Modifier.size(48.dp).testTag("focus-back")) {
    IosCommandIcon(CommandIcons.Reply,"Volver a búsqueda global",Modifier.size(20.dp),tint=c.onSurfaceVariant)
   }
   if(context==null)IosCommandIcon(CommandIcons.Search,null,Modifier.size(22.dp),tint=c.onSurfaceVariant)
   else ResultIcon(context.item,Modifier.size(24.dp))
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)) {
    Text(context?.name ?: "Spotlight",style=FocusSearchType.metadata,color=accent)
    BasicTextField(if(context==null)state.global else state.term,
     {value->if(context==null){state.global=value;state.selected=0;state.lastTapped=null;state.review=null;scope.launch{list.scrollToItem(0)}} else {state.term=value.copy(text=value.text.take(4000));state.review=null}},
     keyboardOptions=KeyboardOptions(imeAction=ImeAction.Search),
     keyboardActions=KeyboardActions(onSearch={if(context!=null)prepare() else state.rows.getOrNull(state.selected)?.let{if(it.action is Action.ComposeSearch)enter(it) else onRequest(it.action)}}),
     textStyle=(if(context==null)FocusSearchType.query else FocusSearchType.contextual).copy(color=c.onSurface),singleLine=true,
     modifier=Modifier.fillMaxWidth().focusRequester(focus).testTag("focus-query"),
     cursorBrush=androidx.compose.ui.graphics.SolidColor(accent),decorationBox={input ->
      if((if(context==null)state.global.text else state.term.text).isEmpty())Text(context?.placeholder ?: "Buscar o ejecutar",
       style=if(context==null)FocusSearchType.query else FocusSearchType.contextual,color=c.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
      input()
     })
   }
  }
  if(context==null) {
   if(state.rows.isNotEmpty())Text(if(state.global.text.isBlank())"PARA EMPEZAR" else "RESULTADOS",
    Modifier.padding(horizontal=20.dp,vertical=6.dp),style=FocusSearchType.section,color=c.onSurfaceVariant)
   LazyColumn(state=list,modifier=Modifier.weight(1f,fill=false).fillMaxWidth().testTag("focus-results")) {
    itemsIndexed(state.rows,key={_,r->r.id}) { i,row ->
     val search=row.action as? Action.ComposeSearch
     Row(Modifier.fillMaxWidth().heightIn(min=62.dp).background(if(i==state.selected)c.onSurface.copy(alpha=.045f) else Color.Transparent)
      .clickable(role=Role.Button){state.selected=i;if(search!=null)enter(row) else {if(state.lastTapped==row.id)onRequest(row.action);state.lastTapped=row.id}}.testTag("focus-result-$i")
      .padding(horizontal=20.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
      ResultIcon(row,Modifier.size(if(search==null)32.dp else 22.dp))
      Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {
       Text(if(search==null)row.title else if(search.packageName=="com.spotify.music")"Buscar canción o artista" else "Buscar en la web",
        style=FocusSearchType.result,color=c.onSurface,maxLines=2,overflow=TextOverflow.Ellipsis)
       Text(if(search==null)if((row.action as? Action.Launch)?.intent?.component!=null)"Aplicación instalada" else row.category else "Abrir búsqueda contextual",style=FocusSearchType.subtitle,color=c.onSurfaceVariant,maxLines=1)
      }
      Text(if(search!=null)"Entrar" else if(i==state.selected)"↗" else "",style=FocusSearchType.metadata,color=c.onSurfaceVariant)
     }
    }
   }
  } else Column(Modifier.weight(1f,fill=false).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=20.dp,vertical=14.dp),
    verticalArrangement=Arrangement.spacedBy(10.dp)) {
   Text(if(state.review==null)"Tu búsqueda continúa en ${context.name}" else "Revisá antes de continuar",style=FocusSearchType.title)
   Text(if(context.draft.packageName=="com.spotify.music")"Se abrirá su enlace público. No se consultan tu cuenta ni resultados internos desde Spotlight."
    else "Se abrirá la búsqueda web en Vivaldi. No se leen pestañas ni historial.",style=FocusSearchType.subtitle,color=c.onSurfaceVariant)
   if(state.review!=null) {
    Text(state.term.text,style=FocusSearchType.result)
    Text(state.review!!.intent.data?.host.orEmpty(),style=FocusSearchType.metadata,color=accent)
   }
  }
  if(state.error.isNotBlank())Text(state.error,Modifier.padding(horizontal=20.dp,vertical=8.dp).testTag("focus-error"),style=FocusSearchType.subtitle,color=c.error)
  Row(Modifier.fillMaxWidth().heightIn(min=48.dp).padding(horizontal=16.dp).testTag("focus-footer"),verticalAlignment=Alignment.CenterVertically) {
   Text(if(context==null)if(state.global.text.isBlank())"Búsqueda global" else "${state.rows.size} resultados" else if(state.review==null)"Sin solicitud enviada" else "Acción preparada",
    Modifier.weight(1f),style=FocusSearchType.metadata,color=c.onSurfaceVariant)
   if(context!=null)TextButton(onClick={
    if(state.review==null)prepare() else {
     val frozen=state.review!!;state.review=null
     if(IntegrationRegistry(app).resolves(frozen.intent))onRequest(frozen) else state.error="La aplicación ya no está disponible."
    }
   },enabled=state.term.text.isNotBlank(),modifier=Modifier.heightIn(min=48.dp).testTag("focus-prepare")) {
    Text(if(state.review==null)"Revisar" else "Solicitar a Android",style=FocusSearchType.subtitle,color=if(state.term.text.isBlank())c.onSurfaceVariant.copy(alpha=.45f) else accent)
   } else state.rows.getOrNull(state.selected)?.let { row ->
    TextButton(onClick={if(row.action is Action.ComposeSearch)enter(row) else onRequest(row.action)},modifier=Modifier.heightIn(min=48.dp).testTag("focus-global-primary")) {
     Text(if(row.action is Action.ComposeSearch)"Entrar" else "Abrir",style=FocusSearchType.subtitle,color=c.onSurface)
    }
   }
  }
 }
 }
}
