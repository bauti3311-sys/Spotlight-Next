package dev.bautista.spotlight.ui

import android.content.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.media.AudioManager
import android.provider.AlarmClock
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.bautista.spotlight.automation.*
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.Dispatchers
import android.net.Uri
import dev.bautista.spotlight.data.LocalStore
import kotlinx.coroutines.*

@Composable fun WorkflowRunner(w:Workflow,local:LocalStore,onAction:(Action)->Unit={},onDismiss:()->Unit) {
    val checkpoint=LocalTaskCheckpoint.current
    val context=LocalContext.current;val scope=rememberCoroutineScope()
    val app=context.applicationContext as SpotlightApp
    val executor=remember(app){ToolExecutor(app)};val chat=remember(app){DirectAi(app)}
    var pending by remember{mutableStateOf<Pair<String,CompletableDeferred<Boolean>>?>(null)}
    var prepared by rememberSaveable(w.id,stateSaver=PreparedWorkflowSaver){mutableStateOf(emptyList<Pair<String,String>>())}
    val history=remember{context.getSharedPreferences("workflow_history",Context.MODE_PRIVATE)}
    var historyEnabled by remember{mutableStateOf(history.getBoolean("enabled",false))}
    suspend fun approve(details:String){val answer=CompletableDeferred<Boolean>();pending=details to answer;try{check(answer.await()){"Acción cancelada. La rutina se detuvo."}}finally{pending=null}}

    var input by rememberSaveable(w.id){mutableStateOf("")}
    var progress by rememberSaveable(w.id,stateSaver=WorkflowProgressSaver){mutableStateOf<WorkflowProgress?>(null)};var running by remember{mutableStateOf(false)};var error by remember{mutableStateOf("")};var job by remember{mutableStateOf<Job?>(null)}
    fun timerIntent(minutes:Int)=Intent(AlarmClock.ACTION_SET_TIMER).putExtra(AlarmClock.EXTRA_LENGTH,minutes*60).putExtra(AlarmClock.EXTRA_SKIP_UI,false).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    DisposableEffect(Unit){onDispose{job?.cancel()}}
    Column(Modifier.fillMaxWidth().heightIn(max=340.dp).verticalScroll(rememberScrollState()).padding(16.dp)){
        Text(w.name,style=MaterialTheme.typography.titleMedium)
        Column{
        if(progress==null){Text("Revisá los pasos antes de ejecutar:");w.steps.forEachIndexed{i,s->Text("${i+1}. ${Workflows.labels[s.type] ?: s.type} ${s.value.take(120)}")};Text("Los cambios realizados antes de cancelar no se revierten automáticamente.",style=MaterialTheme.typography.bodySmall)}
        progress?.let{p->Text("${p.step}/${p.total} · ${p.label}");if(p.output.isNotBlank())Text(p.output.take(1000));if(p.failed)Text("La rutina se detuvo; los pasos restantes no se ejecutaron.",color=MaterialTheme.colorScheme.error)}
        if(!running && progress==null)OutlinedTextField(input,{input=it.take(8000)},label={Text("Texto de entrada opcional")},minLines=1,maxLines=3,modifier=Modifier.fillMaxWidth())
        pending?.let{(details,answer)->Surface(color=MaterialTheme.colorScheme.secondaryContainer,shape=MaterialTheme.shapes.medium){Column(Modifier.padding(12.dp)){Text("Confirmar paso",style=MaterialTheme.typography.titleSmall);Text(details.take(8200));Row{Button(onClick={answer.complete(true)}){Text("Confirmar")};TextButton(onClick={answer.complete(false)}){Text("Cancelar paso")}}}}}
        prepared.forEach{(title,encoded)->TextButton(enabled=!running,onClick={scope.launch{
            try{val call=Workflows.toolCall(encoded);check(ToolRegistry.definition(call.name)?.risk!=ToolRisk.CHANGE){"Revisá el resultado del cambio anterior antes de repetirlo."};executor.execute(call,app.preferences.config.first(),true).action?.let(onAction) ?: run{error="La revisión ya no está disponible."}}
            catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo recuperar la revisión."}
        }}){Text("Volver a preparar revisión: $title")}}
        if(progress!=null && !running)Text("Repetir comienza desde el primer paso; los efectos previos no se deshacen.",style=MaterialTheme.typography.bodySmall)
        if(progress?.warning?.isNotBlank()==true)Text(progress!!.warning,color=MaterialTheme.colorScheme.error)
        if(!running)Row{Checkbox(historyEnabled,{historyEnabled=it;history.edit().putBoolean("enabled",it).apply()});Text("Guardar estado de rutinas · sin contenidos",style=MaterialTheme.typography.bodySmall)}
        if(running && pending==null)LinearProgressIndicator(progress={((progress?.step ?: 0).toFloat()/w.steps.size.coerceAtLeast(1)).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth())
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
    }
    Row {TextButton(enabled=!running,onClick={
        if(w.steps.any{it.type=="timer"} && context.packageManager.resolveActivity(timerIntent(1),0)==null){error="No hay una app Reloj compatible. No se ejecutó ningún paso.";return@TextButton}
        error="";prepared=emptyList();progress=null;running=true;job=scope.launch{try{
            val host=object:WorkflowHost {
                override suspend fun copy(text:String){checkpoint();context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Spotlight",text))}
                override suspend fun note(text:String){checkpoint();local.saveNote(text)}
                override suspend fun volume(level:Int){approve("Cambiar volumen multimedia a $level %");checkpoint();val audio=context.getSystemService(AudioManager::class.java);audio.setStreamVolume(AudioManager.STREAM_MUSIC,audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*level/100,0)}
                override suspend fun timer(minutes:Int){checkpoint();context.startActivity(timerIntent(minutes))}
                override suspend fun tool(call:ToolCall):String {
                    val definition=ToolRegistry.definition(call.name) ?: error("Herramienta no compatible.")
                    if(definition.risk!=ToolRisk.READ)approve(definition.title+"\n"+call.arguments.entries.joinToString("\n"){"${it.key}: ${it.value}"})
                    checkpoint();val result=executor.execute(call,app.preferences.config.first(),definition.risk!=ToolRisk.READ)
                    result.action?.let{prepared=prepared+(definition.title to Workflows.encodeTool(call))};return result.text
                }
                override suspend fun ai(text:String):String {val config=app.preferences.config.first();check(chat.configured(config)){"Configurá IA antes de ejecutar."};approve("Enviar este texto a ${config.aiProvider}:\n$text");checkpoint();return withTimeout(180000){chat.answer(config,listOf("user" to text)).fold(""){all,delta->(all+delta).also{check(it.length<=8000){"La respuesta supera el límite de la rutina."}}}}}
                override suspend fun extract(uri:String):String {val parsed=Uri.parse(uri);require(parsed.scheme=="content"){"Elegí un archivo autorizado mediante Android."};return dev.bautista.spotlight.index.DocumentText.extract(context,parsed,null)}
            }
            Workflows.run(w,host,input).collect{progress=it};if(historyEnabled)history.edit().putString("last", "${w.id}|${System.currentTimeMillis()}|${if(progress?.failed==true)"failed" else "completed"}").apply()
        }catch(e:CancellationException){progress=WorkflowProgress(progress?.step ?: 0,w.steps.size,"Cancelada",progress?.output.orEmpty(),failed=true);throw e}finally{running=false}}
    }){Text(if(progress==null)"Ejecutar" else "Repetir")};TextButton(onClick={if(running){job?.cancel()}else onDismiss()}){Text(if(running)"Detener" else "Cerrar")}}
    }
}
