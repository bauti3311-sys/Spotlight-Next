package dev.bautista.spotlight.ui

import android.content.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.*
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*

@Composable fun ContextChip(app:SpotlightApp,item:SharedContext,onAction:(Action)->Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal=18.dp),verticalAlignment=Alignment.CenterVertically){Icon(CommandIcons.Description,null,Modifier.size(16.dp));TextButton(onClick={onAction(Action.SystemPanel("context"))},modifier=Modifier.weight(1f)){Text("Contexto: ${item.title}",maxLines=1)};IconButton(onClick={app.contexts.clearActive()}){Icon(CommandIcons.Close,"Quitar contexto",Modifier.size(18.dp))}}
}
@Composable fun ContextPanel(app:SpotlightApp,c:Config,page:String,payload:String,onAction:(Action)->Unit) {
    val scope=rememberCoroutineScope();val current by app.contexts.active.collectAsState();val pinned by app.contexts.pinned.collectAsState();val chats by app.contexts.conversations.collectAsState()
    var text by remember{mutableStateOf("")};var error by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)}
    LaunchedEffect(page,payload){if(page=="context" && payload.isNotBlank())app.contexts.select(payload)}
    Column(Modifier.fillMaxWidth().heightIn(max=400.dp).verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        if(page=="diagnostics") {DiagnosticsPanel(app);return@Column}
        if(page=="conversation") {
            val chat=chats.firstOrNull{it.id==payload}
            if(chat==null)Text("Esta conversación ya no está guardada.") else {
                Text(chat.title,style=MaterialTheme.typography.titleMedium);SelectionContainer{Text(chat.text)}
                TextButton(onClick={app.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Conversación",chat.text))}){Text("Copiar")}
                TextButton(onClick={scope.launch{app.contexts.deleteConversation(chat.id)}}){Text("Eliminar conversación")}
            };return@Column
        }
        Text(if(page=="context-ai")"Revisar contexto para IA" else "Contexto autorizado",style=MaterialTheme.typography.titleMedium)
        Text("Temporal durante 10 minutos; fijar guarda una referencia o el texto local. Nada se envía al escribir.",style=MaterialTheme.typography.bodySmall)
        current?.let{item->
            Text(item.title,style=MaterialTheme.typography.titleSmall)
            if(item.text.isNotBlank())SelectionContainer{Text(item.text.take(2000))}
            Row{TextButton(onClick={scope.launch{app.contexts.pinActive()}}){Text(if(item.pinned)"Contexto fijado" else "Fijar")};TextButton(onClick={app.contexts.clearActive()}){Text("Quitar")}}
            TextButton(enabled=!busy,onClick={busy=true;scope.launch{try{val content=app.contexts.text(item);onAction(Action.Ask((if(payload.isBlank())"Resumí este contenido" else payload)+"\n\n"+content,true))}catch(e:CancellationException){throw e}catch(e:Exception){error="No se pudo leer el contenido. Elegí nuevamente el archivo o pegá el texto."}finally{busy=false}}}){Text("Revisar envío a IA")}
        }
        TextButton(onClick={onAction(Action.Launch(Intent(app,dev.bautista.spotlight.ContextPickerActivity::class.java).putExtra("query",payload)))}){Text("Elegir documento con Android")}
        OutlinedTextField(text,{text=it.take(16000)},label={Text("Texto o enlace")},modifier=Modifier.fillMaxWidth(),minLines=2)
        TextButton(enabled=text.isNotBlank(),onClick={app.contexts.setText(text);text=""}){Text("Usar este texto como contexto")}
        if(!c.privateMode)TextButton(onClick={
            val manager=app.getSystemService(ClipboardManager::class.java)
            if(manager.primaryClipDescription?.extras?.getBoolean("android.content.extra.IS_SENSITIVE",false)==true)error="El portapapeles está marcado como sensible. Pegalo explícitamente si querés usarlo."
            else manager.primaryClip?.getItemAt(0)?.text?.toString()?.take(16000)?.takeIf{it.isNotBlank()}?.let{app.contexts.setText(it,"Portapapeles solicitado")}
        }){Text("Leer portapapeles ahora")}
        if(pinned.isNotEmpty())Text("Contextos fijados",style=MaterialTheme.typography.titleSmall)
        pinned.forEach{item->Row{TextButton(onClick={app.contexts.select(item.id)},modifier=Modifier.weight(1f)){Text(item.title)};TextButton(onClick={scope.launch{app.contexts.removeContext(item.id)}}){Text("Eliminar")}}}
        if(chats.isNotEmpty())Text("Conversaciones guardadas",style=MaterialTheme.typography.titleSmall)
        chats.forEach{chat->Row{TextButton(onClick={onAction(Action.SystemPanel("conversation",chat.id))},modifier=Modifier.weight(1f)){Text(chat.title)};TextButton(onClick={scope.launch{app.contexts.deleteConversation(chat.id)}}){Text("Eliminar")}}}
        if(busy)LinearProgressIndicator(Modifier.fillMaxWidth());if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
    }
}
