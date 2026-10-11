package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.*

@Composable fun AiToolPanel(app:SpotlightApp,config:Config,initialPrompt:String,onAction:(Action)->Unit) {
    var prompt by remember{mutableStateOf(initialPrompt.take(3000))};var plan by remember{mutableStateOf<ToolPlan?>(null)};var busy by remember{mutableStateOf(false)};var error by remember{mutableStateOf("")};var job by remember{mutableStateOf<Job?>(null)};val scope=rememberCoroutineScope()
    val results=remember{mutableStateMapOf<Int,ToolExecution>()};val executor=remember{ToolExecutor(app)};val agent=remember{ToolAgent(CredentialVault(app))}
    val chat=remember{DirectAi(app)}
    val configured=chat.configured(config)
    val state by app.shizuku.state.collectAsState()
    DisposableEffect(Unit){onDispose{job?.cancel()}}
    Column(Modifier.fillMaxWidth().heightIn(max=340.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text("IA con herramientas",style=MaterialTheme.typography.titleMedium)
        Text("Solo se envía esta solicitud al proveedor. Las propuestas se revisan aquí; los resultados locales no se envían automáticamente.",style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(prompt,{prompt=it.take(3000);plan=null;results.clear()},label={Text("Qué querés hacer")},minLines=2,maxLines=4,enabled=!busy,modifier=Modifier.fillMaxWidth())
        Button(enabled=!busy && prompt.isNotBlank() && config.aiProvider in setOf("openai","gemini") && configured,onClick={busy=true;error="";plan=null;results.clear();job=scope.launch{try{plan=agent.propose(config,prompt,executor.available(config))}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo preparar la propuesta."}finally{busy=false}}}){Text("Proponer acciones")}
        if(!configured)Text("Guardá tu API key en Inteligencia artificial para preparar acciones.",style=MaterialTheme.typography.bodySmall)
        if(config.aiProvider=="backend")Text("Este modo necesita OpenAI o Gemini. El chat con tu backend se conserva.",style=MaterialTheme.typography.bodySmall)
        if(busy){LinearProgressIndicator(Modifier.fillMaxWidth());TextButton(onClick={job?.cancel();busy=false}){Text("Cancelar")}}
        plan?.let{p->
            Text("Propuesta · no se ejecuta automáticamente",style=MaterialTheme.typography.titleSmall)
            if(p.explanation.isNotBlank())Text(p.explanation)
            p.calls.forEachIndexed{i,call->val d=ToolRegistry.definition(call.name)!!
                Surface(shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceVariant){Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                    Text(d.title,style=MaterialTheme.typography.titleSmall);Text(d.risk.label+(if(d.advanced)" · Shizuku" else ""),style=MaterialTheme.typography.labelSmall)
                    call.arguments.forEach{(name,value)->SelectionContainer{Text("$name: $value",style=MaterialTheme.typography.bodySmall)}}
                    results[i]?.let{result->SelectionContainer{Text(result.text)};result.action?.let{action->TextButton(onClick={onAction(action)}){Text("Abrir revisión")}}}
                    if(results[i]==null)TextButton(enabled=!busy && (!d.advanced || state.available),onClick={busy=true;error="";job=scope.launch{try{results[i]=executor.execute(call,config,confirmed=true)}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "La acción no se completó."}finally{busy=false}}}){Text(if(d.risk==ToolRisk.READ)"Consultar" else "Confirmar esta acción")}
                }}
            }
        }
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        TextButton(onClick={val copy=app.toolAudit.summary();onAction(Action.Copy(copy.ifBlank{"Sin operaciones registradas."}))}){Text("Copiar registro sin textos ni argumentos")}
    }
}
