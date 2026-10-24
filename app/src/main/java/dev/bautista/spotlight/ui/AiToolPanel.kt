package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.*

@Composable internal fun AiToolTaskPanel(app:SpotlightApp,config:Config,initialPrompt:String,onAction:(Action)->Unit,initialPlan:ToolPlan?=null,executeOverride:(suspend (ToolCall)->ToolExecution)?=null,initialBindings:ToolBindings=emptyMap()) {
    var prompt by rememberSaveable(initialPrompt){mutableStateOf(initialPrompt.take(3000))};var plan by rememberSaveable(initialPrompt,stateSaver=ToolProposalSaver){mutableStateOf(initialPlan)};var busy by remember{mutableStateOf(false)};var error by remember{mutableStateOf("")};var job by remember{mutableStateOf<Job?>(null)};val scope=rememberCoroutineScope()
    var results by rememberSaveable(initialPrompt,stateSaver=ToolOutcomesSaver){mutableStateOf(emptyMap<Int,ToolOutcome>())};var interrupted by rememberSaveable(initialPrompt){mutableIntStateOf(-1)};val executor=remember{ToolExecutor(app)};val agent=remember{ToolAgent(CredentialVault(app))}
    var omitted by rememberSaveable(initialPrompt,stateSaver=ToolOmissionsSaver){mutableStateOf(emptySet<Int>())}
    var relativeVolumes by rememberSaveable(initialPrompt,stateSaver=ToolOmissionsSaver){mutableStateOf(emptySet<Int>())}
    var bindings by rememberSaveable(initialPrompt,stateSaver=ToolBindingsSaver){mutableStateOf(initialBindings)}
    var relativeBrightness by rememberSaveable(initialPrompt,stateSaver=ToolOmissionsSaver){mutableStateOf(emptySet<Int>())}
    var brightnessBaseline by rememberSaveable(initialPrompt){mutableStateOf<Int?>(null)}
    var sequencing by rememberSaveable(initialPrompt){mutableStateOf(false)}
    var sequenceNext by rememberSaveable(initialPrompt){mutableIntStateOf(-1)}
    var sequenceStopped by rememberSaveable(initialPrompt){mutableStateOf(false)}
    var sequenceMessage by rememberSaveable(initialPrompt){mutableStateOf("")}
    var trace by rememberSaveable(initialPrompt){mutableStateOf(emptyList<String>())}
    val volumeBaseline=rememberSaveable(initialPrompt){
        val audio=app.getSystemService(android.media.AudioManager::class.java)
        val max=audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        val min=if(android.os.Build.VERSION.SDK_INT>=28)audio.getStreamMinVolume(android.media.AudioManager.STREAM_MUSIC) else 0
        if(max>min)(audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)-min)*100/(max-min) else 0
    }
    var adding by rememberSaveable(initialPrompt){mutableStateOf(false)}
    var savingTemplate by rememberSaveable(initialPrompt){mutableStateOf(false)}
    val templateId=rememberSaveable(initialPrompt){java.util.UUID.randomUUID().toString()}
    var available by remember { mutableStateOf(emptyList<ToolDefinition>()) }
    LaunchedEffect(config){available=executor.available(config)}
    fun draft(p:ToolPlan)=ToolPlanDraft(p,results,omitted,relativeVolumes,bindings,relativeBrightness)
    fun change(draft:ToolPlanDraft){plan=draft.plan;results=draft.outcomes;omitted=draft.omitted;relativeVolumes=draft.relativeVolumes;bindings=draft.bindings;relativeBrightness=draft.relativeBrightness}
    fun record(message:String){trace=(trace+message).takeLast(24)}
    fun label(value:ExecutionState)=when(value){ExecutionState.PREPARED->"Preparado";ExecutionState.REQUESTED->"Solicitud enviada";ExecutionState.COMPLETED->"Completado";ExecutionState.VERIFIED->"Verificado"}
    val checkpoint=LocalTaskCheckpoint.current
    val chat=remember{DirectAi(app)}
    val configured=chat.configured(config)
    val state by app.shizuku.state.collectAsState()
    DisposableEffect(Unit){onDispose{job?.cancel()}}
    suspend fun runCall(call:ToolCall):ToolExecution {
        if(ToolRegistry.definition(call.name)?.risk!=ToolRisk.READ)checkpoint()
        return executeOverride?.invoke(call) ?: executor.execute(call,app.preferences.config.first(),confirmed=true)
    }
    suspend fun advanceSequence(p:ToolPlan,confirmed:Int?=null) {
        busy=true;error="";sequenceNext=-1
        try {
            val gate=ToolSequenceExecution(::runCall).advance(p.calls,omitted,bindings,draft(p).receipts(),confirmed,
                resolve={i,_->
                    val row=projectToolSettings(draft(p),volumeBaseline,brightnessBaseline)[i]
                    check(row.dependencyVerified(results)){"La dependencia todavía no está verificada."}
                    row.call ?: error("Revisá argumentos y dependencias.")
                },started={i->interrupted=i;record("Paso ${i+1}: en curso");checkpoint()},
                completed={i,result->results=results+(i to ToolOutcome(result.text,result.state,result.action!=null,result.outputs));interrupted=-1;record("Paso ${i+1}: ${label(result.state)}");checkpoint()})
            sequenceMessage=gate.message
            sequenceNext=if(gate.disposition==SequenceDisposition.AWAIT_CONFIRMATION)gate.index else -1
            sequenceStopped=gate.disposition in setOf(SequenceDisposition.FAILED,SequenceDisposition.PENDING_VERIFICATION)
            record(when(gate.disposition){SequenceDisposition.FINISHED->"Plan finalizado";SequenceDisposition.AWAIT_CONFIRMATION->"Paso ${gate.index+1}: esperando confirmación";SequenceDisposition.PENDING_VERIFICATION->"Paso ${gate.index+1}: pendiente de verificación";SequenceDisposition.FAILED->"Plan detenido"});checkpoint()
        } catch(e:CancellationException){sequenceStopped=true;sequenceMessage="Secuencia cancelada; no se reanudará automáticamente.";record("Cancelado");throw e}
        catch(e:Exception){sequenceStopped=true;error=e.message ?: "No se pudo guardar el estado; secuencia detenida.";record("Detenido")}
        finally{busy=false}
    }
    Column(Modifier.testTag("tool-task").fillMaxWidth().heightIn(max=340.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text("IA con herramientas",style=MaterialTheme.typography.titleMedium)
        Text("Solo se envía esta solicitud al proveedor. Las propuestas se revisan aquí; los resultados locales no se envían automáticamente.",style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(prompt,{prompt=it.take(3000);plan=null;results=emptyMap();omitted=emptySet();relativeVolumes=emptySet();adding=false;interrupted=-1;bindings=emptyMap();relativeBrightness=emptySet();brightnessBaseline=null;sequencing=false;sequenceNext=-1;sequenceStopped=false;sequenceMessage="";trace=emptyList()},label={Text("Qué querés hacer")},minLines=2,maxLines=4,enabled=!busy,modifier=Modifier.fillMaxWidth())
        Button(enabled=!busy && prompt.isNotBlank() && config.aiProvider in setOf("openai","gemini") && configured,onClick={busy=true;error="";plan=null;results=emptyMap();omitted=emptySet();relativeVolumes=emptySet();adding=false;interrupted=-1;bindings=emptyMap();relativeBrightness=emptySet();brightnessBaseline=null;sequencing=false;sequenceNext=-1;sequenceStopped=false;sequenceMessage="";trace=emptyList();job=scope.launch{try{plan=agent.propose(config,prompt,executor.available(config))}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo preparar la propuesta."}finally{busy=false}}}){Text("Proponer acciones")}
        if(!configured)Text("Guardá tu API key en Inteligencia artificial para preparar acciones.",style=MaterialTheme.typography.bodySmall)
        if(config.aiProvider=="backend")Text("Este modo necesita OpenAI o Gemini. El chat con tu backend se conserva.",style=MaterialTheme.typography.bodySmall)
        if(busy){LinearProgressIndicator(Modifier.fillMaxWidth());TextButton(onClick={job?.cancel();busy=false}){Text("Cancelar")}}
        plan?.let{p->
            Text("Propuesta · no se ejecuta automáticamente",style=MaterialTheme.typography.titleSmall)
            if(p.explanation.isNotBlank())Text(p.explanation)
            val editable=!busy && interrupted<0 && results.isEmpty() && !sequencing
            Text("Añadí, quitá, ordená u omití pasos antes de ejecutar. Cada acción se confirma por separado.",style=MaterialTheme.typography.bodySmall)
            if(results.isNotEmpty())Text("La estructura queda protegida después de ejecutar para evitar repeticiones.",style=MaterialTheme.typography.bodySmall)
            TextButton(enabled=editable && omitted.isEmpty() && relativeVolumes.isEmpty() && relativeBrightness.isEmpty(),onClick={savingTemplate=!savingTemplate},modifier=Modifier.testTag("tool-save-template")){Text("Guardar como plantilla")}
            if(savingTemplate && editable)key(p,bindings){CommandTemplateEditor(app,
                dev.bautista.spotlight.automation.CommandTemplate(templateId,"","",p,bindings=bindings),null){savingTemplate=false}}
            TextButton(enabled=editable && p.calls.size<6,onClick={adding=!adding},modifier=Modifier.testTag("tool-add")){Text(if(adding)"Cerrar acciones disponibles" else "Añadir paso")}
            if(adding && editable) available.forEach { definition ->
                TextButton(modifier=Modifier.testTag("tool-add-${definition.id}"),onClick={change(draft(p).add(blankToolCall(definition)));adding=false}){Text(definition.title)}
            }
            val graphError=runCatching{StructuredToolResults.validate(p.calls,bindings,omitted)}.exceptionOrNull()?.message.orEmpty()
            if(graphError.isNotEmpty())Text(graphError,color=MaterialTheme.colorScheme.error)
            if(-1 in results)Text(results.getValue(-1).text,color=MaterialTheme.colorScheme.error)
            Button(enabled=!busy && !sequencing && interrupted<0 && -1 !in results && graphError.isEmpty() && p.calls.indices.any{it !in omitted && it !in results},onClick={if(busy)return@Button;busy=true;sequencing=true;sequenceStopped=false;job=scope.launch{advanceSequence(p)}},modifier=Modifier.testTag("tool-sequence-start")){Text("Ejecutar en orden")}
            if(sequencing && results.isEmpty() && !busy && interrupted<0)TextButton(onClick={sequencing=false;sequenceNext=-1;sequenceStopped=false}){Text("Volver a editar el plan")}
            if(sequencing && !busy && interrupted<0 && !sequenceStopped && sequenceNext<0 && p.calls.indices.any{it !in omitted && it !in results})TextButton(onClick={if(busy)return@TextButton;busy=true;job=scope.launch{advanceSequence(p)}},modifier=Modifier.testTag("tool-sequence-resume")){Text("Revisar continuación")}
            if(sequenceMessage.isNotBlank())Text(sequenceMessage,modifier=Modifier.testTag("tool-sequence-status"))
            if(trace.isNotEmpty())Text(trace.joinToString(" · "),style=MaterialTheme.typography.labelSmall,modifier=Modifier.testTag("tool-sequence-trace"))
            val projected=projectToolSettings(draft(p),volumeBaseline,brightnessBaseline)
            p.calls.forEachIndexed{i,call->val d=ToolRegistry.definition(call.name) ?: return@forEachIndexed
                val reviewed=projected[i]
                val validated=reviewed.call?.let { runCatching { ToolRegistry.decode(it) }.isSuccess } == true
                val dependencyReady=reviewed.dependencyVerified(results)
                Surface(shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceVariant){Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                    Text(d.title,style=MaterialTheme.typography.titleSmall);Text(d.risk.label+(if(d.advanced)" · Shizuku" else ""),style=MaterialTheme.typography.labelSmall)
                    Row {
                        Checkbox(i !in omitted,{checked->change(draft(p).omit(i,!checked))},enabled=editable,modifier=Modifier.testTag("tool-include-$i"))
                        Text(if(i in omitted)"Paso omitido" else "Paso ${i+1}",modifier=Modifier.padding(top=12.dp))
                    }
                    if(call.name=="set_volume") {
                        FilterChip(i in relativeVolumes,{change(draft(p).relativeVolume(i,i !in relativeVolumes))},enabled=editable,label={Text("Cambio relativo")},modifier=Modifier.testTag("tool-relative-$i"))
                        if(i in relativeVolumes) {
                            Text(reviewed.volume?.let{"Objetivo revisado: $it%"} ?: "Revisá el cambio relativo (−100 a 100).",modifier=Modifier.testTag("tool-target-$i"))
                            Text(reviewed.dependency?.let{"Depende del paso ${it+1}: debe quedar verificado antes de confirmar."} ?: "Base revisada: $volumeBaseline%.",style=MaterialTheme.typography.bodySmall)
                        }
                    }
                    if(call.name=="write_display_setting" && call.arguments["key"]=="screen_brightness") {
                        FilterChip(i in relativeBrightness,{
                            change(draft(p).relativeBrightness(i,i !in relativeBrightness))
                            if(i in relativeBrightness && brightnessBaseline==null && state.available)scope.launch{try{
                                val raw=app.advancedBackend.read("screen_brightness").toInt()
                                require(raw in 20..255);if(plan===p && i in relativeBrightness && brightnessBaseline==null)brightnessBaseline=(raw*100+254)/255
                            }catch(e:CancellationException){throw e}catch(e:Exception){error="No se pudo leer el brillo actual; revisá Shizuku."}}
                        },enabled=editable,label={Text("Brillo relativo")},modifier=Modifier.testTag("tool-brightness-relative-$i"))
                        if(i in relativeBrightness)Text(reviewed.volume?.let{"Objetivo revisado: $it%"} ?: "Falta una base de brillo válida o un paso previo.",modifier=Modifier.testTag("tool-brightness-target-$i"))
                    }
                    call.arguments.forEach{(name,value)->
                        if(results[i]==null && !busy && interrupted!=i && !sequencing && bindings[i]?.containsKey(name)!=true)OutlinedTextField(value,{next->
                            plan=p.copy(calls=p.calls.toMutableList().also{it[i]=call.copy(arguments=call.arguments+(name to next.take(8000)))})
                        },label={Text(if((name=="percent" && i in relativeVolumes) || (name=="value" && i in relativeBrightness))"Cambio (%)" else name)},singleLine=false,maxLines=4,modifier=Modifier.fillMaxWidth().testTag("tool-argument-$i-$name"))
                        else SelectionContainer{Text("$name: $value",style=MaterialTheme.typography.bodySmall)}
                        bindings[i]?.get(name)?.let{ref->
                            Text(if(ref.step<0)"$name: origen eliminado" else "$name ← paso ${ref.step+1} · ${ref.field}",modifier=Modifier.testTag("tool-binding-$i-$name"))
                            SelectionContainer{Text(reviewed.call?.arguments?.get(name)?.let{"$name revisado: $it"} ?: "$name: resultado pendiente de verificación",modifier=Modifier.testTag("tool-resolved-$i-$name"))}
                            TextButton(enabled=editable,onClick={change(draft(p).unbind(i,name))},modifier=Modifier.testTag("tool-unbind-$i-$name")){Text("Usar argumento escrito")}
                        }
                        if(editable && StructuredToolResults.inputType(call,name)!=null) {
                            for(source in 0 until i)StructuredToolResults.outputTypes(p.calls[source]).forEach{(field,type)->
                                if(source !in omitted && type==StructuredToolResults.inputType(call,name))TextButton(onClick={change(draft(p).bind(i,name,ToolResultRef(source,field)))},modifier=Modifier.testTag("tool-bind-$i-$name-$source-$field")){Text("Usar $field del paso ${source+1}")}
                            }
                        }

                    }
                    Row {
                        TextButton(enabled=editable && i>0,onClick={change(draft(p).move(i,i-1))},modifier=Modifier.testTag("tool-up-$i")){Text("Subir paso")}
                        TextButton(enabled=editable && i<p.calls.lastIndex,onClick={change(draft(p).move(i,i+1))},modifier=Modifier.testTag("tool-down-$i")){Text("Bajar paso")}
                        TextButton(enabled=editable,onClick={change(draft(p).remove(i))},modifier=Modifier.testTag("tool-remove-$i")){Text("Quitar")}
                    }
                    results[i]?.let{result->
                        Text(label(result.state),style=MaterialTheme.typography.labelSmall);SelectionContainer{Text(result.text)}
                        if(result.review)TextButton(enabled=!busy,onClick={
                            if(d.risk==ToolRisk.CHANGE){error="La operación ya fue solicitada. Revisá su estado antes de repetirla.";return@TextButton}
                            busy=true;job=scope.launch{try{runCall(call).action?.let(onAction) ?: run{error="La revisión ya no está disponible."}}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo volver a preparar la revisión."}finally{busy=false}}
                        }){Text("Volver a preparar revisión")}
                    }
                    if(interrupted==i) {
                        Text("Operación interrumpida. Su efecto externo no se puede deducir al restaurar.",color=MaterialTheme.colorScheme.error)
                        TextButton(enabled=!busy,onClick={interrupted=-1;sequencing=false;sequenceStopped=false;sequenceNext=-1}){Text("Revisé el estado; permitir otro intento")}
                    }
                    if(i !in omitted && results[i]==null && interrupted!=i)TextButton(modifier=Modifier.heightIn(min=48.dp).testTag("tool-execute-$i"),enabled=!busy && -1 !in results && graphError.isEmpty() && (!sequencing || (!sequenceStopped && sequenceNext==i)) && validated && dependencyReady && (!d.advanced || state.available),onClick={if(busy)return@TextButton;if(sequencing){busy=true;job=scope.launch{advanceSequence(p,i)};return@TextButton};busy=true;error="";interrupted=i;job=scope.launch{try{
                        val current=projectToolSettings(draft(p),volumeBaseline,brightnessBaseline)[i]
                        val effective=current.call ?: error("Revisá los argumentos del paso.")
                        check(reviewed.dependencyVerified(results)){"El paso previo todavía no está verificado."}
                        ToolRegistry.decode(effective)
                        val result=runCall(effective)
                        currentCoroutineContext().ensureActive()
                        results=results+(i to ToolOutcome(result.text,result.state,result.action!=null,result.outputs));interrupted=-1;record("Paso ${i+1}: ${label(result.state)}");checkpoint()
                    }catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "La acción no se completó."}finally{busy=false}}}){Text(if(d.risk==ToolRisk.READ)"Consultar" else "Confirmar esta acción")}

                }}
            }
        }
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        TextButton(onClick={val copy=app.toolAudit.summary();onAction(Action.Copy(copy.ifBlank{"Sin operaciones registradas."}))}){Text("Copiar registro sin textos ni argumentos")}
    }
}

@Composable fun AiToolPanel(app:SpotlightApp,config:Config,initialPrompt:String,onAction:(Action)->Unit) {
    AiToolTaskPanel(app,config,initialPrompt,onAction)
}
