package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.semantics.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.search.normalize

data class ContextAction(val title:String,val description:String,val icon:ImageVector,val perform:()->Unit,val shortcut:String="") {
    val group:String get()=when{title.startsWith("Eliminar")->"Gestión";title.contains("Permisos")||title.contains("Shizuku")||title.contains("componentes")->"Sistema";title.contains("favorita")||title.contains("consulta")||title.contains("sugerencia")->"Organizar";title.startsWith("Copiar")||title.startsWith("Guardar")||title.contains("IA")||title.startsWith("Traducir")->"Texto";else->"Más acciones"}
}
@OptIn(ExperimentalFoundationApi::class)
@Composable fun ActionPanelView(actions:List<ContextAction>,focus:FocusRequester) {
    var filter by remember {mutableStateOf("")};var selected by remember{mutableIntStateOf(0)};var filterFocused by remember{mutableStateOf(false)}
    val distinct=actions.distinctBy{it.title};val all=distinct.take(1)+distinct.drop(1).sortedBy{listOf("Texto","Organizar","Más acciones","Sistema","Gestión").indexOf(it.group)};val visible=all.filter{normalize(it.title+" "+it.description).contains(normalize(filter))}
    val requests=remember(visible.map{it.title}){List(visible.size){if(it==0)focus else FocusRequester()}}
    LaunchedEffect(filter){selected=0}
    LaunchedEffect(selected,filterFocused,visible.map{it.title}){if(!filterFocused){withFrameNanos{ };runCatching{requests.getOrNull(selected)?.requestFocus()}}}
    Column(Modifier.fillMaxWidth().heightIn(max=GlassTokens.actionPanelMaxHeight).onPreviewKeyEvent{e->
        if(e.type!=KeyEventType.KeyDown || e.isCtrlPressed || e.isMetaPressed)false else when(e.key){
            Key.Tab->{filterFocused=false;selected=if(visible.isEmpty())0 else Math.floorMod(selected+if(e.isShiftPressed)-1 else 1,visible.size);true}
            Key.DirectionDown->{filterFocused=false;selected=(selected+1).coerceAtMost((visible.size-1).coerceAtLeast(0));true}
            Key.DirectionUp->{filterFocused=false;selected=(selected-1).coerceAtLeast(0);true}
            Key.Enter,Key.NumPadEnter->{visible.getOrNull(selected)?.perform?.invoke();true}
            else->false
        }
    },verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text("Acciones",style=MaterialTheme.typography.titleSmall)
        Text("Tab siguiente · Shift+Tab anterior", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(filter,{filter=it.take(120)},label={Text("Filtrar acciones")},singleLine=true,modifier=Modifier.fillMaxWidth().onFocusChanged{filterFocused=it.isFocused},leadingIcon={Icon(CommandIcons.Search,null,Modifier.size(18.dp))})
        Column(Modifier.fillMaxWidth().weight(1f, fill=false).heightIn(max=240.dp).verticalScroll(rememberScrollState())){
            var previous=""
            visible.forEachIndexed{i,action->
                val group=if(all.firstOrNull()==action)"Principal" else action.group
                if(group!=previous){Text(group.uppercase(),Modifier.padding(start=10.dp,top=10.dp,bottom=4.dp),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);previous=group}
                val bring=remember(action.title){BringIntoViewRequester()}
                LaunchedEffect(selected,filterFocused){if(!filterFocused && selected==i)bring.bringIntoView()}
                Surface(color=if(!filterFocused && i==selected)MaterialTheme.colorScheme.primary.copy(alpha=.12f) else MaterialTheme.colorScheme.surface.copy(alpha=0f),shape=RoundedCornerShape(10.dp)){
                    val perform by rememberUpdatedState(action.perform)
                    val active=!filterFocused && i==selected
                    Row(Modifier.fillMaxWidth().heightIn(min=48.dp)
                        .focusRequester(requests[i]).onFocusChanged{if(it.isFocused){selected=i;filterFocused=false}}.focusable()
                        .semantics(mergeDescendants=true){role=Role.Button;this.selected=active;onClick(action.title){perform();true}}
                        .pointerInput(action.title,i){detectTapGestures(onTap={selected=i;filterFocused=false;perform()})}
                        .bringIntoViewRequester(bring).padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){
                        Icon(action.icon,null,Modifier.size(19.dp));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(action.title,modifier=Modifier.fillMaxWidth());Text(action.description,modifier=Modifier.fillMaxWidth(),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                        val key=action.shortcut.ifBlank{if(all.firstOrNull()==action)"Enter" else ""};if(key.isNotBlank())Text(key,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if(visible.isEmpty())Text("No hay acciones con ese nombre.",style=MaterialTheme.typography.bodySmall)
        }
        Text("Tab / Shift+Tab · ↑↓ elegir · Enter ejecutar · Esc volver",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
