package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.ai.ToolRisk
import dev.bautista.spotlight.search.normalize

enum class ContextActionGroup(val label:String) {
    Primary("Principal"), Open("Más acciones"), Communicate("Comunicar"), Share("Compartir"),
    Organize("Organizar"), Text("Texto"), System("Sistema"), Management("Gestión")
}
/** Presentation of an existing executor's capability, not a second capability registry. */
enum class ContextActionState(val label:String,val executable:Boolean=true) {
    Available(""), PermissionRequired("Requiere permiso"), ConfigurationRequired("Requiere configuración"),
    External("Intervención en Android"), Unsupported("No compatible",false), Unavailable("No disponible actualmente",false)
}
data class ContextAction(
    val title:String,val description:String,val icon:ImageVector,val perform:()->Unit,
    val shortcut:String="",val id:String="",val group:ContextActionGroup=ContextActionGroup.Open,
    val children:List<ContextAction> = emptyList(),val state:ContextActionState=ContextActionState.Available,
    val risk:ToolRisk=ToolRisk.READ,
) { val identity:String get()=id.ifBlank{title} }
private data class ActionMenuFrame(val id:String,val filter:String,val selectedId:String?)

@OptIn(ExperimentalFoundationApi::class)
@Composable fun ActionPanelView(actions:List<ContextAction>,focus:FocusRequester,onClose:(()->Unit)?=null,contextResult:dev.bautista.spotlight.search.Result?=null,actionsKey:dev.bautista.spotlight.input.KeyChord=dev.bautista.spotlight.input.KeyChord(android.view.KeyEvent.KEYCODE_K)) {
    val path=remember {mutableStateListOf<ActionMenuFrame>()}
    var filter by remember {mutableStateOf("")}
    var selectedId by remember {mutableStateOf<String?>(null)}
    var filterFocused by remember {mutableStateOf(false)}
    val filterFocus=remember {FocusRequester()}
    fun orderedLevel()=path.fold(actions) { choices,frame -> choices.firstOrNull{it.identity==frame.id}?.children.orEmpty() }
        .distinctBy{it.identity}.let { it.take(1)+it.drop(1).sortedBy{a->a.group.ordinal} }
    val all=orderedLevel()
    fun matches(a:ContextAction):Boolean = normalize(a.title+" "+a.description+" "+a.state.label+" "+a.children.joinToString(" "){it.title}).contains(normalize(filter))
    val visible=all.filter{matches(it)}
    val currentId=selectedId?.takeIf{key->visible.any{it.identity==key}} ?: visible.firstOrNull()?.identity
    val requests=remember(visible.map{it.identity}) { visible.mapIndexed { i,a->a.identity to if(i==0)focus else FocusRequester() }.toMap() }
    fun back() {
        if(path.isNotEmpty()) { val frame=path.removeAt(path.lastIndex);filter=frame.filter;selectedId=frame.selectedId;filterFocused=false }
        else onClose?.invoke()
    }
    val backNavigation=LocalActionBackNavigation.current
    SideEffect {backNavigation?.back={back()}}
    DisposableEffect(backNavigation){onDispose{backNavigation?.back=null}}
    fun activate(action:ContextAction?) {
        if(action==null || !action.state.executable)return
        if(action.children.isNotEmpty()) {path+=ActionMenuFrame(action.identity,filter,action.identity);filter="";selectedId=null;filterFocused=false}
        else action.perform()
    }
    // Key events can arrive in the same frame: resolve live filter/path/ID, not captured indices.
    fun visibleNow()=orderedLevel().filter{matches(it)}
    fun currentAction()=visibleNow().let { choices -> choices.firstOrNull{it.identity==selectedId} ?: choices.firstOrNull() }
    fun move(delta:Int) {
        filterFocused=false
        val choices=visibleNow()
        if(choices.isNotEmpty()) {
            val index=choices.indexOfFirst{it.identity==selectedId}.coerceAtLeast(0)
            selectedId=choices[Math.floorMod(index+delta,choices.size)].identity
        }
    }
    LaunchedEffect(currentId,filterFocused,path.map{it.id},visible.map{it.identity}) {
        if(!filterFocused) {
            withFrameNanos { }
            if(currentId!=null)runCatching { requests[currentId]?.requestFocus() }
            else runCatching { filterFocus.requestFocus() }
        }
    }
    val colors=MaterialTheme.colorScheme
    val commandAccent=rememberCommandAccent(contextResult)
    Column(Modifier.fillMaxWidth().heightIn(max=GlassTokens.actionPanelMaxHeight).padding(12.dp).testTag("context-action-panel")
        .onPreviewKeyEvent { e ->
            if(e.type!=KeyEventType.KeyDown)false
            else if(actionsKey.matches(e.nativeKeyEvent)){onClose?.invoke();true}
            else if((e.isCtrlPressed || e.isMetaPressed) && e.key==Key.F){filterFocus.requestFocus();true}
            else if(e.isCtrlPressed || e.isMetaPressed)false
            else when(e.key) {
                Key.Escape,Key.Back->{back();true}
                Key.Tab->{move(if(e.isShiftPressed)-1 else 1);true}
                Key.DirectionDown->{move(1);true}
                Key.DirectionUp->{move(-1);true}
                Key.Enter,Key.NumPadEnter->{activate(currentAction());true}
                else->{
                    val character=e.nativeKeyEvent.unicodeChar
                    if(!filterFocused && !e.isAltPressed && character>=32 && Character.isValidCodePoint(character)) {
                        filter=(filter+String(Character.toChars(character))).take(120)
                        filterFocused=true
                        filterFocus.requestFocus()
                        true
                    } else false
                }
            }
        }) {
        Row(Modifier.fillMaxWidth().heightIn(min=48.dp).background(colors.onSurface.copy(alpha=.035f),RoundedCornerShape(10.dp)).border(1.dp,colors.onSurface.copy(alpha=.12f),RoundedCornerShape(10.dp)),verticalAlignment=Alignment.CenterVertically) {
            if(path.isNotEmpty())IconButton(onClick={back()},modifier=Modifier.size(48.dp)) {
                IosCommandIcon(CommandIcons.Reply,"Volver al menú anterior",Modifier.size(17.dp))
            } else IosCommandIcon(CommandIcons.Search,null,Modifier.padding(start=8.dp,end=10.dp).size(17.dp),tint=colors.onSurfaceVariant)
            BasicTextField(filter,{filter=it.take(120)},singleLine=true,
                textStyle=TextStyle(fontFamily=Inter,fontSize=13.sp,color=colors.onSurface),
                modifier=Modifier.weight(1f).heightIn(min=48.dp).focusRequester(filterFocus).onFocusChanged{filterFocused=it.isFocused}.testTag("action-filter"),
                decorationBox={field->Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.CenterStart) {
                    if(filter.isBlank())Text(contextResult?.let{"Buscar acciones de ${it.title}…"} ?: "Buscar acciones…",fontSize=12.sp,color=colors.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
                    field()
                }})
            IconButton(onClick={onClose?.invoke()},modifier=Modifier.size(48.dp)){IosCommandIcon(CommandIcons.Close,"Cerrar acciones",Modifier.size(17.dp))}
        }
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth().weight(1f,fill=false).verticalScroll(rememberScrollState())) {
            var previous:ContextActionGroup?=null
            visible.forEachIndexed { i,action ->
                val appContext=contextResult?.let{PreviewPresentation.isApplication(it)}==true
                val group=if(i==0 && action==all.firstOrNull())ContextActionGroup.Primary else if(appContext)when(action.group) {
                    ContextActionGroup.Open,ContextActionGroup.Communicate->ContextActionGroup.Primary
                    ContextActionGroup.Text->ContextActionGroup.Organize
                    else->action.group
                } else action.group
                if(group!=previous && filter.isBlank()) {
                    if(previous!=null)HorizontalDivider(Modifier.padding(vertical=6.dp),color=colors.onSurface.copy(alpha=.08f))
                    Text(group.label.uppercase(java.util.Locale.ROOT),Modifier.padding(start=10.dp,top=4.dp,bottom=4.dp),
                        fontSize=10.sp,lineHeight=14.sp,color=colors.onSurfaceVariant,letterSpacing=0.sp)
                    previous=group
                }
                val active=action.identity==currentId
                val bring=remember(action.identity) { BringIntoViewRequester() }
                LaunchedEffect(active,filterFocused){if(active && !filterFocused)bring.bringIntoView()}
                Row(Modifier.fillMaxWidth().heightIn(min=48.dp)
                    .background(if(active)colors.onSurface.copy(alpha=.09f) else Color.Transparent,RoundedCornerShape(10.dp))
                    .focusRequester(requests.getValue(action.identity)).onFocusChanged{if(it.isFocused){selectedId=action.identity;filterFocused=false}}.focusable()
                    .semantics(mergeDescendants=true) { role=Role.Button;selected=active;stateDescription=action.state.label
                        if(!action.state.executable)disabled()
                        onClick(action.title){activate(action);action.state.executable} }
                    .clickable(enabled=action.state.executable){selectedId=action.identity;activate(action)}
                    .bringIntoViewRequester(bring).padding(horizontal=10.dp,vertical=6.dp),
                    verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    IosCommandIcon(if(i==0 && path.isEmpty() && action==all.firstOrNull())CommandIcons.Arrow else action.icon,
                        null,Modifier.size(20.dp),tint=if(action.group==ContextActionGroup.Management)colors.error else if(i==0 && path.isEmpty())commandAccent ?: colors.onSurfaceVariant else colors.onSurfaceVariant)
                    Column(Modifier.weight(1f)) {
                        Text(action.title,fontSize=13.sp,fontWeight=if(active)FontWeight.SemiBold else FontWeight.Medium,
                            maxLines=1,overflow=TextOverflow.Ellipsis)
                        val detail=if(action.state!=ContextActionState.Available)action.state.label else action.description.takeIf{it.isNotBlank() && it!=action.group.label && it!="Principal"}.orEmpty()
                        if(detail.isNotBlank() && (LocalAppearance.current.preset=="glass" || action.state!=ContextActionState.Available))Text(detail,fontSize=11.sp,lineHeight=14.sp,color=colors.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
                    }
                    val key=action.shortcut.ifBlank { if(action==all.firstOrNull())"Enter" else "" }
                    if(action.children.isNotEmpty())IosCommandIcon(CommandIcons.Chevron,null,Modifier.size(16.dp),tint=colors.onSurfaceVariant)
                    else if(key.isNotEmpty())GlassKeycap(key)
                }
            }
            if(visible.isEmpty())Text("No hay acciones con ese nombre.",Modifier.padding(10.dp),fontSize=12.sp,color=colors.onSurfaceVariant)
        }

    }
}
