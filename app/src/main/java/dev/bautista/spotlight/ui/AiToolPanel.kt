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

@Composable internal fun AiToolTaskPanel(app:SpotlightApp,config:Config,initialPrompt:String,onAction:(Action)->Unit,initialPlan:ToolPlan?=null,executeOverride:(suspend (ToolCall)->ToolExecution)?=null) {
    var prompt by rememberSaveable(initialPrompt){mutableStateOf(initialPrompt.take(3000))};var plan by rememberSaveable(initialPrompt,stateSaver=ToolProposalSaver){mutableStateOf(initialPlan)};var busy by remember{mutableStateOf(false)};var error by remember{mutableStateOf("")};var job by remember{mutableStateOf<Job?>(null)};val scope=rememberCoroutineScope()
    var results by rememberSaveable(initialPrompt,stateSaver=ToolOutcomesSaver){mutableStateOf(emptyMap<Int,ToolOutcome>())};var interrupted by rememberSaveable(initialPrompt){mutableIntStateOf(-1)};val executor=remember{ToolExecutor(app)};val agent=remember{ToolAgent(CredentialVault(app))}
    var omitted by rememberSaveable(initialPrompt,stateSaver=ToolOmissionsSaver){mutableStateOf(emptySet<Int>())}
    var adding by rememberSaveable(initialPrompt){mutableStateOf(false)}
    var available by remember { mutableStateOf(emptyList<ToolDefinition>()) }
    LaunchedEffect(config){available=executor.available(config)}
    fun change(draft:ToolPlanDraft){plan=draft.plan;results=draft.outcomes;omitted=draft.omitted}
    val checkpoint=LocalTaskCheckpoint.current
    val chat=remember{DirectAi(app)}
    val configured=chat.configured(config)
    val state by app.shizuku.state.collectAsState()
    DisposableEffect(Unit){onDispose{job?.cancel()}}
    suspend fun runCall(call:ToolCall):ToolExecution {
        if(ToolRegistry.definition(call.name)?.risk!=ToolRisk.READ)checkpoint()
        return executeOverride?.invoke(call) ?: executor.execute(call,app.preferences.config.first(),confirmed=true)
    }
    Column(Modifier.testTag("tool-task").fillMaxWidth().heightIn(max=340.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text("IA con herramientas",style=MaterialTheme.typography.titleMedium)
        Text("Solo se envía esta solicitud al proveedor. Las propuestas se revisan aquí; los resultados locales no se envían automáticamente.",style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(prompt,{prompt=it.take(3000);plan=null;results=emptyMap();omitted=emptySet();adding=false},label={Text("Qué querés hacer")},minLines=2,maxLines=4,enabled=!busy,modifier=Modifier.fillMaxWidth())
        Button(enabled=!busy && prompt.isNotBlank() && config.aiProvider in setOf("openai","gemini") && configured,onClick={busy=true;error="";plan=null;results=emptyMap();omitted=emptySet();adding=false;job=scope.launch{try{plan=agent.propose(config,prompt,executor.available(config))}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo preparar la propuesta."}finally{busy=false}}}){Text("Proponer acciones")}
        if(!configured)Text("Guardá tu API key en Inteligencia artificial para preparar acciones.",style=MaterialTheme.typography.bodySmall)
        if(config.aiProvider=="backend")Text("Este modo necesita OpenAI o Gemini. El chat con tu backend se conserva.",style=MaterialTheme.typography.bodySmall)
        if(busy){LinearProgressIndicator(Modifier.fillMaxWidth());TextButton(onClick={job?.cancel();busy=false}){Text("Cancelar")}}
        plan?.let{p->
            Text("Propuesta · no se ejecuta automáticamente",style=MaterialTheme.typography.titleSmall)
            if(p.explanation.isNotBlank())Text(p.explanation)
            val editable=!busy && interrupted<0 && results.isEmpty()
            Text("Añadí, quitá, ordená u omití pasos antes de ejecutar. Cada acción se confirma por separado.",style=MaterialTheme.typography.bodySmall)
            if(results.isNotEmpty())Text("La estructura queda protegida después de ejecutar para evitar repeticiones.",style=MaterialTheme.typography.bodySmall)
            TextButton(enabled=editable && p.calls.size<6,onClick={adding=!adding},modifier=Modifier.testTag("tool-add")){Text(if(adding)"Cerrar acciones disponibles" else "Añadir paso")}
            if(adding && editable) available.forEach { definition ->
                TextButton(modifier=Modifier.testTag("tool-add-${definition.id}"),onClick={change(ToolPlanDraft(p,results,omitted).add(blankToolCall(definition)));adding=false}){Text(definition.title)}
            }
            p.calls.forEachIndexed{i,call->val d=ToolRegistry.definition(call.name) ?: return@forEachIndexed
                Surface(shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceVariant){Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                    Text(d.title,style=MaterialTheme.typography.titleSmall);Text(d.risk.label+(if(d.advanced)" · Shizuku" else ""),style=MaterialTheme.typography.labelSmall)
                    Row {
                        Checkbox(i !in omitted,{checked->change(ToolPlanDraft(p,results,omitted).omit(i,!checked))},enabled=editable,modifier=Modifier.testTag("tool-include-$i"))
                        Text(if(i in omitted)"Paso omitido" else "Paso ${i+1}",modifier=Modifier.padding(top=12.dp))
                    }
                    call.arguments.forEach{(name,value)->
                        if(results[i]==null && !busy && interrupted!=i)OutlinedTextField(value,{next->
                            plan=p.copy(calls=p.calls.toMutableList().also{it[i]=call.copy(arguments=call.arguments+(name to next.take(8000)))})
                        },label={Text(name)},singleLine=false,maxLines=4,modifier=Modifier.fillMaxWidth().testTag("tool-argument-$i-$name"))
                        else SelectionContainer{Text("$name: $value",style=MaterialTheme.typography.bodySmall)}
                    }
                    Row {
                        TextButton(enabled=editable && i>0,onClick={change(ToolPlanDraft(p,results,omitted).move(i,i-1))},modifier=Modifier.testTag("tool-up-$i")){Text("Subir paso")}
                        TextButton(enabled=editable && i<p.calls.lastIndex,onClick={change(ToolPlanDraft(p,results,omitted).move(i,i+1))},modifier=Modifier.testTag("tool-down-$i")){Text("Bajar paso")}
                        TextButton(enabled=editable,onClick={change(ToolPlanDraft(p,results,omitted).remove(i))},modifier=Modifier.testTag("tool-remove-$i")){Text("Quitar")}
                    }
                    results[i]?.let{result->
                        SelectionContainer{Text(result.text)}
                        if(result.review)TextButton(enabled=!busy,onClick={
                            if(d.risk==ToolRisk.CHANGE){error="La operación ya fue solicitada. Revisá su estado antes de repetirla.";return@TextButton}
                            busy=true;job=scope.launch{try{runCall(call).action?.let(onAction) ?: run{error="La revisión ya no está disponible."}}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo volver a preparar la revisión."}finally{busy=false}}
                        }){Text("Volver a preparar revisión")}
                    }
                    if(interrupted==i) {
                        Text("Operación interrumpida. Su efecto externo no se puede deducir al restaurar.",color=MaterialTheme.colorScheme.error)
                        TextButton(onClick={interrupted=-1}){Text("Revisé el estado; permitir otro intento")}
                    }
                    if(i !in omitted && results[i]==null && interrupted!=i)TextButton(modifier=Modifier.heightIn(min=48.dp).testTag("tool-execute-$i"),enabled=!busy && (!d.advanced || state.available),onClick={busy=true;error="";interrupted=i;job=scope.launch{try{
                        ToolRegistry.decode(call)
                        val result=runCall(call)
                        results=results+(i to ToolOutcome(result.text,result.state,result.action!=null));interrupted=-1
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
