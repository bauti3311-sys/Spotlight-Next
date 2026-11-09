package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.*
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.search.*
import java.text.DateFormat
import java.util.Date

@Composable fun ReceivedMessages(target:ReplyTarget){
    val context=LocalContext.current
    var photo by remember(target.key,target.icon){mutableStateOf<android.graphics.Bitmap?>(null)}
    LaunchedEffect(target.key,target.icon){photo=withContext(Dispatchers.IO){runCatching{target.icon?.loadDrawable(context)?.toBitmap(80,80)}.getOrNull()}}
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(6.dp)){
        Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){photo?.let{Image(it.asImageBitmap(),"Imagen de la conversación",Modifier.size(36.dp));Spacer(Modifier.width(10.dp))};Text(target.title,style=MaterialTheme.typography.titleMedium)}
        Text((if(target.group)"Grupo · " else "")+DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(target.time)),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        SelectionContainer{Column(Modifier.fillMaxWidth().heightIn(max=180.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
            if(target.messages.isEmpty())Text("La notificación no contiene texto accesible. Android o WhatsApp pueden ocultarlo.")
            target.messages.forEach{m->Surface(color=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.5f),shape=MaterialTheme.shapes.medium){Column(Modifier.padding(10.dp)){if(target.group && m.sender.isNotBlank())Text(m.sender,style=MaterialTheme.typography.labelMedium);Text(m.text)}}}
        }}
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable fun NotificationPreview(key:String,onAction:(Action)->Unit,onCopy:(String)->Unit,onClose:()->Unit){
    val entries by Replies.targets.collectAsState();val target=entries.firstOrNull{it.key==key}
    var error by remember(key){mutableStateOf("")};val context=LocalContext.current
    Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        if(target==null){Text("La notificación ya no está disponible");TextButton(onClick=onClose){Text("Volver")}}
        else{
            ReceivedMessages(target)
            FlowRow(verticalArrangement=Arrangement.spacedBy(4.dp)){Button(enabled=target.action!=null,onClick={onAction(Action.ComposeMessage(if(target.packageName=="com.whatsapp.w4b")"whatsapp_business" else "whatsapp",target.title,replyKey=key))}){Text("Responder")};TextButton(enabled=target.open!=null,onClick={runCatching{target.open?.send(context,0,null)}.onSuccess{onClose()}.onFailure{error="La acción para abrir la conversación venció."}}){Text("Abrir")}}
            FlowRow(verticalArrangement=Arrangement.spacedBy(4.dp)){TextButton(enabled=target.messages.isNotEmpty(),onClick={onCopy(target.visibleText)}){Text("Copiar mensaje")};TextButton(enabled=target.messages.isNotEmpty(),onClick={onAction(AiRequestPolicy.prepare(Action.Ask("Resumí estos mensajes de WhatsApp en español:\n"+target.visibleText,true,origin=AiInputOrigin.PRIVATE_CONTEXT)))}){Text("Resumir con IA")};TextButton(enabled=target.messages.isNotEmpty(),onClick={onAction(AiRequestPolicy.prepare(Action.Ask("Redactá una respuesta breve y natural en español para estos mensajes de WhatsApp:\n"+target.visibleText,true,messageTarget=target.title,origin=AiInputOrigin.PRIVATE_CONTEXT)))}){Text("Sugerir respuesta con IA")};TextButton(onClick=onClose){Text("Volver")}}
            if(target.action==null)Text("WhatsApp no publicó una acción de respuesta directa.",style=MaterialTheme.typography.bodySmall)
            if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        }
    }
}
