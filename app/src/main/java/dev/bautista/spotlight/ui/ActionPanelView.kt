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
@Composable fun ActionPanelView(actions:List<ContextAction>,focus:FocusRequester,onClose:(()->Unit)?=null) {
    val path=remember {mutableStateListOf<ActionMenuFrame>()}
    var filter by remember {mutableStateOf("")}
    var selectedId by remember {mutableStateOf<String?>(null)}
    var filterFocused by remember {mutableStateOf(false)}
    val filterFocus=remember {FocusRequester()}
    val level=path.fold(actions) { choices,frame -> choices.firstOrNull{it.identity==frame.id}?.children.orEmpty() }
    val all=level.distinctBy{it.identity}.let { it.take(1)+it.drop(1).sortedBy{a->a.group.ordinal} }
    fun matches(a:ContextAction):Boolean = normalize(a.title+" "+a.description+" "+a.state.label+" "+a.children.joinToString(" "){it.title}).contains(normalize(filter))
    val visible=all.filter{matches(it)}
    val currentId=selectedId?.takeIf{key->visible.any{it.identity==key}} ?: visible.firstOrNull()?.identity
    val index=visible.indexOfFirst{it.identity==currentId}.coerceAtLeast(0)
    val requests=remember(visible.map{it.identity}) { visible.mapIndexed { i,a->a.identity to if(i==0)focus else FocusRequester() }.toMap() }
    fun back() {
        if(path.isNotEmpty()) { val frame=path.removeAt(path.lastIndex);filter=frame.filter;selectedId=frame.selectedId;filterFocused=false }
        else onClose?.invoke()
    }
    fun activate(action:ContextAction?) {
        if(action==null || !action.state.executable)return
        if(action.children.isNotEmpty()) {path+=ActionMenuFrame(action.identity,filter,action.identity);filter="";selectedId=null;filterFocused=false}
        else action.perform()
    }
    fun move(delta:Int) {filterFocused=false;if(visible.isNotEmpty())selectedId=visible[Math.floorMod(index+delta,visible.size)].identity}
    LaunchedEffect(currentId,filterFocused,path.map{it.id},visible.map{it.identity}) {
        if(!filterFocused) {
            withFrameNanos { }
            if(currentId!=null)runCatching { requests[currentId]?.requestFocus() }
            else runCatching { filterFocus.requestFocus() }
        }
    }
    val colors=MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().heightIn(max=GlassTokens.actionPanelMaxHeight).padding(8.dp).testTag("context-action-panel")
        .onPreviewKeyEvent { e ->
            if(e.type!=KeyEventType.KeyDown)false
            else if((e.isCtrlPressed || e.isMetaPressed) && e.key==Key.F){filterFocus.requestFocus();true}
            else if(e.isCtrlPressed || e.isMetaPressed)false
            else when(e.key) {
                Key.Escape,Key.Back->{back();true}
                Key.Tab->{move(if(e.isShiftPressed)-1 else 1);true}
                Key.DirectionDown->{move(1);true}
                Key.DirectionUp->{move(-1);true}
                Key.Enter,Key.NumPadEnter->{activate(visible.firstOrNull{it.identity==currentId});true}
                else->false
            }
        }) {
        if(path.isNotEmpty()) Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick={back()}){Icon(CommandIcons.Reply,"Volver al menú anterior",Modifier.size(18.dp))}
            Text(path.last().let { f -> actions.firstOrNull{it.identity==f.id}?.title ?: "Submenú" },
                style=MaterialTheme.typography.labelMedium,maxLines=1,overflow=TextOverflow.Ellipsis)
        }
        Column(Modifier.fillMaxWidth().weight(1f,fill=true).verticalScroll(rememberScrollState())) {
            var previous:ContextActionGroup?=null
            visible.forEachIndexed { i,action ->
                val group=if(i==0 && action==all.firstOrNull())ContextActionGroup.Primary else action.group
                if(group!=previous) {
                    if(previous!=null)HorizontalDivider(Modifier.padding(vertical=6.dp),color=colors.onSurface.copy(alpha=.08f))
                    Text(group.label.uppercase(),Modifier.padding(start=10.dp,top=4.dp,bottom=4.dp),
                        fontSize=10.sp,color=colors.onSurfaceVariant,letterSpacing=.6.sp)
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
                    Icon(action.icon,null,Modifier.size(17.dp),tint=if(action.group==ContextActionGroup.Management)colors.error else colors.onSurfaceVariant)
                    Column(Modifier.weight(1f)) {
                        Text(action.title,fontSize=13.sp,fontWeight=if(active)FontWeight.SemiBold else FontWeight.Medium,
                            maxLines=1,overflow=TextOverflow.Ellipsis)
                        if(action.state!=ContextActionState.Available)Text(action.state.label,fontSize=11.sp,color=colors.onSurfaceVariant)
                    }
                    val key=action.shortcut.ifBlank { if(action==all.firstOrNull())"Enter" else "" }
                    if(action.children.isNotEmpty())Icon(CommandIcons.Chevron,null,Modifier.size(16.dp),tint=colors.onSurfaceVariant)
                    else if(key.isNotEmpty())Text(key,fontSize=11.sp,color=colors.onSurfaceVariant)
                }
            }
            if(visible.isEmpty())Text("No hay acciones con ese nombre.",Modifier.padding(10.dp),fontSize=12.sp,color=colors.onSurfaceVariant)
        }
        HorizontalDivider(color=colors.onSurface.copy(alpha=.08f))
        Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically) {
            Icon(CommandIcons.Search,null,Modifier.padding(start=8.dp,end=10.dp).size(17.dp),tint=colors.onSurfaceVariant)
            BasicTextField(filter,{filter=it.take(120)},singleLine=true,
                textStyle=TextStyle(fontFamily=Inter,fontSize=13.sp,color=colors.onSurface),
                modifier=Modifier.weight(1f).heightIn(min=44.dp).focusRequester(filterFocus).onFocusChanged{filterFocused=it.isFocused}.testTag("action-filter"),
                decorationBox={field->Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.CenterStart) {
                    if(filter.isBlank())Text("Buscar acciones…",fontSize=12.sp,color=colors.onSurfaceVariant)
                    field()
                }})
            IconButton(onClick={back()}){Icon(CommandIcons.Close,if(path.isEmpty())"Cerrar acciones" else "Volver al menú anterior",Modifier.size(17.dp))}
        }
    }
}
