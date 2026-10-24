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
    val permission=People.allowed(context)
    val reply=draft.replyKey!=null
    val notifications by Replies.targets.collectAsState()
    val received=notifications.firstOrNull{it.key==draft.replyKey}
    val sharing=draft.channel=="telegram" || draft.channel.startsWith("share:")
    val appName=if(draft.channel.startsWith("share:"))draft.target else ActionCatalog.displayName(draft.channel)
    val focus=remember(draft){FocusRequester()}
    val focusBody=reply || draft.target.isNotBlank() || sharing
    LaunchedEffect(draft){delay(60);focus.requestFocus()}
    LaunchedEffect(target,draft.channel,permission) {
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
        if(!reply && !sharing)TextButton(onClick={onLaunch(Intent(context,dev.bautista.spotlight.VoiceActivity::class.java).putExtra("prefix","${draft.channel} a $target: ").putExtra("fallback","${draft.channel} a $target: $body"))}){Text("Dictar mensaje · revisar borrador")};Text(if(reply)"Responder en WhatsApp" else "Mensaje · ${appName}",style=MaterialTheme.typography.titleMedium)
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
        Text(if(reply)"La respuesta se envía mediante la acción de esta notificación." else "Contacto y enlace se resuelven automáticamente. El envío final se completa en la app receptora.",fontSize=12.sp)
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error,fontSize=12.sp)
        if(sent)Text("Respuesta enviada a la app. La entrega depende de WhatsApp y la conexión.",fontSize=12.sp)
        if(confirming) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text("Confirmar · ${appName}",style=MaterialTheme.typography.titleSmall)
                    Text("Para: ${if(reply)approvedReply?.title.orEmpty() else selected?.let{"${it.name} · $address"} ?: address}")
                    if(reply && received!=null){Text("Mensaje recibido",style=MaterialTheme.typography.labelSmall);androidx.compose.foundation.text.selection.SelectionContainer{Text(received.visibleText.take(800),style=MaterialTheme.typography.bodySmall)}}
                    Text(body,style=MaterialTheme.typography.bodyMedium)
                    Row {
                        Button(onClick={
                            error="";confirming=false
                            if(reply)runCatching {Replies.send(context,draft.replyKey!!,body,requireNotNull(approvedReply){"Revisá la conversación nuevamente."})}.onSuccess{sent=true}.onFailure{error=it.message ?: "No se pudo responder"}
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
            Button(enabled=body.isNotBlank()&&!sent&&(!reply || received?.action!=null)&&(reply||sharing||address.isNotBlank()),onClick={approvedReply=received;confirming=true}) {Text(if(reply)"Revisar respuesta" else "Revisar mensaje")}
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
    LaunchedEffect(draft){delay(60);focus.requestFocus()}
    Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Buscar en ${ActionCatalog.displayName(draft.topic)}",style=MaterialTheme.typography.titleMedium)
        OutlinedTextField(term,{term=it.take(4000)},label={Text("Qué querés buscar")},singleLine=true,modifier=Modifier.fillMaxWidth().focusRequester(focus))
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
