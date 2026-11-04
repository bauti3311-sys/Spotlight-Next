package dev.bautista.spotlight.ui

import android.content.*
import android.media.AudioManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.focus.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*

@Composable fun MessageEditor(draft:Action.ComposeMessage,onLaunch:(Intent)->Unit,onAccess:(String)->Unit,onCancel:()->Unit) {
    val context=LocalContext.current
    var target by rememberSaveable(draft){mutableStateOf(draft.target)}
    var body by rememberSaveable(draft){mutableStateOf(draft.text)}
    var choices by remember(draft){mutableStateOf(emptyList<Person>())}
    var selected by remember(draft){mutableStateOf<Person?>(null)}
    var address by remember(draft){mutableStateOf("")}
    var error by remember(draft){mutableStateOf("")}
    var loading by remember(draft){mutableStateOf(false)}
    var sent by remember(draft){mutableStateOf(false)}
    var confirming by remember(draft){mutableStateOf(false)}
    var approvedReply by remember(draft){mutableStateOf<ReplyTarget?>(null)}
    var activeReplyKey by rememberSaveable(draft){mutableStateOf(draft.replyKey)}
    val permission=People.allowed(context)
    val reply=activeReplyKey!=null
    val notifications by Replies.targets.collectAsState()
    val received=notifications.firstOrNull{it.key==activeReplyKey}
    val compatibleReply=if(!reply && draft.channel in setOf("whatsapp","whatsapp_business"))Replies.findCompatibleReply(draft.channel,selected?.name ?: target) else null
    val sharing=draft.channel=="telegram" || draft.channel.startsWith("share:")
    val app=context.applicationContext as? dev.bautista.spotlight.SpotlightApp
    val shared=app?.contexts?.active?.collectAsState()?.value
    // Names describe the already selected destination; C1 adds no channel switch or new sender.
    val appName=when(draft.channel){
        "whatsapp"->"WhatsApp personal"
        "whatsapp_business"->"WhatsApp Business"
        else->if(draft.channel.startsWith("share:"))draft.target else ActionCatalog.displayName(draft.channel)
    }
    val mechanismLabel=when{
        reply->"RemoteInput de notificación activa (${received?.packageName ?: appName})"
        draft.channel in setOf("whatsapp","whatsapp_business")->"Enlace oficial wa.me · se abre en $appName para confirmar el envío"
        draft.channel=="sms"->"Intent ACTION_SENDTO (smsto:) · se abre en SMS"
        draft.channel=="email"->"Intent ACTION_SENDTO (mailto:) · se abre en correo"
        else->"Intent ACTION_SEND (text/plain) · se abre en $appName"
    }
    val focus=remember(draft){FocusRequester()}
    val focusBody=reply || draft.target.isNotBlank() || sharing
    LaunchedEffect(draft){delay(60);focus.requestFocus()}
    LaunchedEffect(target,draft.channel,permission,reply) {
        if(reply||sharing)return@LaunchedEffect
        selected=null;address="";loading=permission && target.isNotBlank()
        try {
            if(target.isBlank()){choices=emptyList();return@LaunchedEffect}
            delay(120)
            val direct=if(draft.channel=="email")target.contains('@') else target.matches(Regex("\\+?[0-9 ()-]{7,24}"))
            if(direct){address=target;choices=emptyList()}
            else {
                choices=People.find(context,target,draft.channel=="email")
                People.uniqueExact(choices,target)?.let{selected=it;address=it.address}
            }
        } catch(e:CancellationException){throw e} catch(e:Exception){error="No se pudieron leer los contactos"} finally {loading=false}
    }
    Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        if(!reply && !sharing)TextButton(onClick={onLaunch(Intent(context,dev.bautista.spotlight.VoiceActivity::class.java).putExtra("prefix","${draft.channel} a $target: ").putExtra("fallback","${draft.channel} a $target: $body"))}){Text("Dictar mensaje · revisar borrador")};Text(if(reply)"Responder en WhatsApp" else if(sharing)"Compartir · ${appName}" else "Mensaje · ${appName}",style=MaterialTheme.typography.titleMedium)
        if(compatibleReply!=null)TextButton(onClick={activeReplyKey=compatibleReply.key;confirming=false;error=""}){Text("Responder notificación activa · RemoteInput (${compatibleReply.title})")}
        if(reply && draft.replyKey==null)TextButton(onClick={activeReplyKey=null;confirming=false;error=""}){Text("Usar contacto o enlace wa.me")}
        if(reply){if(received!=null)ReceivedMessages(received) else Text(if(sent)"La respuesta se envió a la acción de WhatsApp." else "La notificación ya no está disponible.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
        if(!sharing && !reply) {
            OutlinedTextField(target,{target=it.take(100);sent=false;confirming=false},label={Text(if(reply)"Conversación de la notificación" else "Contacto o número")},singleLine=true,readOnly=reply,modifier=Modifier.fillMaxWidth().then(if(!focusBody)Modifier.focusRequester(focus) else Modifier))
            if(!reply) {
                if(!permission)TextButton(onClick={onAccess("${draft.channel} a $target: $body")}){Text("Permitir contactos · una sola vez")}
                if(loading)LinearProgressIndicator(Modifier.fillMaxWidth())
                if(selected==null && choices.isNotEmpty()) {
                    Text("Elegí el contacto y número",fontSize=12.sp)
                    choices.take(8).forEach { person -> TextButton(onClick={selected=person;address=person.address}){Text("${person.name} · ${person.address}")}}
                }
                if(selected!=null)Text("${selected!!.name} · ${selected!!.address}",fontSize=12.sp)
                if(address.isNotBlank() || selected!=null || target.isNotBlank())OutlinedTextField(address,{address=it.take(100);selected=null;confirming=false},label={Text(if(draft.channel=="email")"Email" else "Número de destino")},singleLine=true,modifier=Modifier.fillMaxWidth())
            }
        }
        OutlinedTextField(body,{body=it.take(8000);sent=false;confirming=false},label={Text("Mensaje")},minLines=2,maxLines=5,modifier=Modifier.fillMaxWidth().then(if(focusBody)Modifier.focusRequester(focus) else Modifier))
        if(sharing)shared?.takeIf{it.text.isNotBlank()}?.let { selected ->
            TextButton(onClick={body=selected.text.take(8000);sent=false;confirming=false;approvedReply=null;focus.requestFocus()},
                modifier=Modifier.heightIn(min=48.dp).testTag("message-use-context")) {
                Text("Usar contexto seleccionado")
            }
            Text(selected.title,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            if(selected.text.length>8000)Text("Se usarán los primeros 8000 caracteres; podés editarlos antes de compartir.",
                style=MaterialTheme.typography.bodySmall)
        }
        Text(if(reply)"La respuesta se envía mediante la acción de esta notificación." else if(sharing)"El contenido se prepara en $appName. Elegí allí el destinatario y confirmá el envío." else "Contacto y enlace se resuelven automáticamente. El envío final se completa en la app receptora.",fontSize=12.sp)
        Text("Mecanismo Android: $mechanismLabel",fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error,fontSize=12.sp)
        if(sent)Text("Respuesta enviada a la app. La entrega depende de WhatsApp y la conexión.",fontSize=12.sp)
        if(confirming) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text("Confirmar · ${appName}",style=MaterialTheme.typography.titleSmall)
                    Text(if(sharing)"Destino: $appName" else "Para: ${if(reply)approvedReply?.title.orEmpty() else selected?.let{"${it.name} · $address"} ?: address}")
                    Text("Mecanismo Android: $mechanismLabel",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    if(reply && received!=null){Text("Mensaje recibido",style=MaterialTheme.typography.labelSmall);androidx.compose.foundation.text.selection.SelectionContainer{Text(received.visibleText.take(800),style=MaterialTheme.typography.bodySmall)}}
                    Text(body,style=MaterialTheme.typography.bodyMedium)
                    Row {
                        Button(colors=inspectorButtonColors(),modifier=Modifier.heightIn(min=48.dp),onClick={
                            error="";confirming=false
                            if(reply)runCatching {Replies.send(context,activeReplyKey!!,body,requireNotNull(approvedReply){"Revisá la conversación nuevamente."})}.onSuccess{sent=true}.onFailure{error=it.message ?: "No se pudo responder"}
                            else {
                                val intent=People.messageIntent(context,draft.channel,address,body)
                                if(intent==null)error="Revisá el número, código de país y área." else onLaunch(intent)
                            }
                        }) {Text(if(reply)"Enviar respuesta" else "Preparar en la app")}
                        TextButton(onClick={confirming=false}) {Text("Editar")}
                    }
                }
            }
        }
        Row {
            Button(colors=inspectorButtonColors(),modifier=Modifier.heightIn(min=48.dp),enabled=body.isNotBlank()&&!sent&&(!reply || received?.action!=null)&&(reply||sharing||address.isNotBlank()),onClick={approvedReply=received;confirming=true}) {Text(if(reply)"Revisar respuesta" else "Revisar mensaje")}
            TextButton(onClick=onCancel){Text("Volver")}
        }
    }
}

@Composable fun SearchEditor(draft:Action.ComposeSearch,onRun:(Action)->Unit,onCancel:()->Unit) {
    val app=LocalContext.current.applicationContext as? dev.bautista.spotlight.SpotlightApp
    if(draft.topic=="spotify" && app!=null) {
        val spotifyState by app.spotify.state.collectAsState()
        LaunchedEffect(Unit){app.spotify.inspect()}
        if(spotifyState==dev.bautista.spotlight.integrations.spotify.SpotifyConnectionState.AUTHORIZED) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                SpotifySearchPanel(app,draft.query)
                TextButton(onClick=onCancel){Text("Volver")}
            }
            return
        }
    }
    var term by rememberSaveable(draft){mutableStateOf(draft.query)}
    val focus=remember(draft){FocusRequester()}
    val context=LocalContext.current
    val destination=remember(draft.topic,draft.packageName) {
        if(draft.topic in setOf("app","app-web-search"))runCatching {
            context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(draft.packageName,0)).toString()
        }.getOrDefault(draft.packageName) else ActionCatalog.displayName(draft.topic)
    }
    val shared=app?.contexts?.active?.collectAsState()?.value
    LaunchedEffect(draft){delay(60);focus.requestFocus()}
    Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Buscar en $destination",style=MaterialTheme.typography.titleMedium)
        OutlinedTextField(term,{term=it.take(4000)},label={Text("Qué querés buscar")},singleLine=true,modifier=Modifier.fillMaxWidth().focusRequester(focus))
        shared?.takeIf{it.text.isNotBlank()}?.let { selected ->
            TextButton(onClick={term=selected.text.take(4000);focus.requestFocus()},
                modifier=Modifier.heightIn(min=48.dp).testTag("search-use-context")) {
                Text("Usar contexto seleccionado")
            }
            Text(selected.title,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            if(selected.text.length>4000)Text("Se usarán los primeros 4000 caracteres; podés editarlos antes de buscar.",
                style=MaterialTheme.typography.bodySmall)
        }
        Row {Button(enabled=term.isNotBlank(),onClick={ActionCatalog.searchAction(draft.topic,draft.packageName,term)?.let(onRun)}){Text("Buscar")};TextButton(onClick=onCancel){Text("Volver")}}
    }
}

@Composable fun AudioControl(onCancel:()->Unit) {
    val audio=LocalContext.current.getSystemService(AudioManager::class.java)
    val maximum=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    var level by remember{mutableFloatStateOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat())}
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Volumen multimedia · ${(level/maximum*100).toInt()} %",style=MaterialTheme.typography.titleMedium)
        Slider(level,{level=it;audio.setStreamVolume(AudioManager.STREAM_MUSIC,it.toInt(),0)},valueRange=0f..maximum.toFloat(),steps=(maximum-1).coerceAtLeast(0))
        TextButton(onClick=onCancel){Text("Volver a sonido")}
    }
}
