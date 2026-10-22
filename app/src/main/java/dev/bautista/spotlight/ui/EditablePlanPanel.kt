package dev.bautista.spotlight.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.automation.*
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

private fun text(step:PlanStep):String=when(step){is PlanStep.Brightness->"brillo al ${step.percent}%";is PlanStep.Volume->"volumen al ${step.percent}%";is PlanStep.Open->"abrir ${step.name}";is PlanStep.RelativeBrightness->"${if(step.delta<0)"bajar" else "subir"} brillo en ${kotlin.math.abs(step.delta)}%";is PlanStep.RelativeVolume->"${if(step.delta<0)"bajar" else "subir"} volumen en ${kotlin.math.abs(step.delta)}%";is PlanStep.Media->when(step.command){"pause"->"pausar música";"play"->"reanudar música";"next"->"siguiente tema";else->"tema anterior"}}
@Composable fun EditablePlanPanel(app:SpotlightApp,c:Config,raw:String,initial:LocalPlan,onAction:(Action)->Unit) {
    EditablePlanPanel(app,c,raw,initial,emptyList(),onAction)
}
@Composable fun EditablePlanPanel(app:SpotlightApp,c:Config,raw:String,initial:LocalPlan,boundCalls:List<ToolCall?>,onAction:(Action)->Unit) {
    var bindings by rememberSaveable(raw,stateSaver=PlanBindingsSaver){mutableStateOf(initial.steps.indices.map{boundCalls.getOrNull(it)})}
    var reviewedInputs by rememberSaveable(raw){mutableStateOf("")}
    val scope=rememberCoroutineScope();var steps by rememberSaveable(raw){mutableStateOf(initial.steps.map(::text))};var rows by remember(raw){mutableStateOf<List<ReviewedStep>>(emptyList())};var selected by rememberSaveable(raw,stateSaver=PlanSelectionSaver){mutableStateOf(setOf<Int>())};var status by rememberSaveable(raw,stateSaver=PlanStatusSaver){mutableStateOf(mapOf<Int,String>())};var busy by remember{mutableStateOf(false)};var job by remember{mutableStateOf<Job?>(null)};var editing by rememberSaveable(raw){mutableIntStateOf(-1)};var error by remember{mutableStateOf("")};var save by rememberSaveable(raw){mutableStateOf(false)};var name by rememberSaveable(raw){mutableStateOf("")};var alias by rememberSaveable(raw){mutableStateOf("")};var icon by rememberSaveable(raw){mutableStateOf("layers")};var saving by remember{mutableStateOf(false)}
    val state by app.shizuku.state.collectAsState();val write by app.shizuku.writeCapability.collectAsState()
    LaunchedEffect(steps,bindings,c,state,write){if(!busy){val inputs=planDraftKey(steps,bindings);val prior=selected.toSet();val retain=reviewedInputs==inputs;if(!retain)status=emptyMap();error="";rows=emptyList();selected=emptySet();val parsed=steps.mapIndexed{i,value->bindings.getOrNull(i)?.let{NativeCommands.step(it){value.removePrefix("abrir ")}} ?: LocalPlan.step(value)};if(parsed.any{it==null}){rows=emptyList();selected=emptySet();error="Revisá el paso editado: brillo, volumen, abrir una app o controlar música."}else try{rows=resolvePlan(app,c,LocalPlan(parsed.filterNotNull()),bindings);selected=rows.indices.filter{rows[it].call!=null && (!retain || it in prior)}.toSet();reviewedInputs=inputs}catch(e:CancellationException){throw e}catch(e:Exception){error="No se pudieron comprobar los requisitos."}}}
    Column(Modifier.fillMaxWidth().heightIn(max=440.dp).verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Revisar plan",style=MaterialTheme.typography.titleMedium);Text("Editá, ordená u omití pasos. Solo se ejecutan después de confirmar.",style=MaterialTheme.typography.bodySmall)
        steps.forEachIndexed {i,value->
            Row(Modifier.testTag("plan-step-$i")){Checkbox(i in selected,{checked->selected=if(checked)selected+i else selected-i},enabled=!busy && rows.getOrNull(i)?.call!=null);Column(Modifier.weight(1f)){Text("${i+1}. ${rows.getOrNull(i)?.title ?: value}");Text(status[i] ?: rows.getOrNull(i)?.requirement.orEmpty(),style=MaterialTheme.typography.bodySmall)
                if(editing==i)OutlinedTextField(value,{new->steps=steps.toMutableList().also{it[i]=new.take(150)};bindings=bindings.toMutableList().also{it[i]=null};rows=emptyList();selected=emptySet()},modifier=Modifier.fillMaxWidth(),label={Text("Paso")},enabled=!busy,singleLine=true)
                Row{TextButton(enabled=!busy,onClick={editing=if(editing==i)-1 else i}){Text(if(editing==i)"Listo" else "Editar")};TextButton(enabled=!busy && i>0,onClick={steps=steps.toMutableList().also{val previous=it[i-1];it[i-1]=it[i];it[i]=previous};bindings=bindings.toMutableList().also{val previous=it[i-1];it[i-1]=it[i];it[i]=previous};rows=emptyList();selected=emptySet();editing=-1}){Text("Subir")};TextButton(enabled=!busy && steps.size>1,onClick={steps=steps.filterIndexed{index,_->index!=i};bindings=bindings.filterIndexed{index,_->index!=i};rows=emptyList();selected=emptySet();editing=-1}){Text("Quitar")}}
            }}
        }
        if(rows.any{it.call==null})TextButton(onClick={onAction(if(steps.mapNotNull{LocalPlan.step(it)}.any{it is PlanStep.Media})Action.Access("notifications") else Action.SystemPanel("shizuku"))}){Text("Revisar permisos y servicios")}
        Button(enabled=!busy && selected.isNotEmpty() && rows.size==steps.size && status.isEmpty(),onClick={if(reviewedInputs!=planDraftKey(steps,bindings) || rows.size!=steps.size || selected.isEmpty())return@Button;busy=true;editing=-1;val chosen=selected;val review=rows.toList();job=scope.launch{try{
            PlanExecution(execute={call->ToolExecutor(app).execute(call,app.preferences.config.first(),true)},dispatch={result->
                val launch=result.action as? Action.Launch ?: error("Esta acción necesita revisión individual antes de continuar.")
                app.startActivity(Intent(launch.intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }).run(review.map{it.call},chosen,true).collect{event->status=status+(event.index to event.message)}
        }catch(e:CancellationException){status=status.mapValues{(_,value)->if(value=="Ejecutando…")"Interrumpido · revisá el estado del dispositivo" else value};throw e}catch(e:Exception){error="No se pudo ejecutar el plan. Revisá los requisitos antes de intentarlo nuevamente."}finally{busy=false}}}){Text(if(busy)"Ejecutando…" else if(status.isNotEmpty())"Plan revisado" else "Confirmar pasos seleccionados")}
        if(busy)TextButton(onClick={job?.cancel()}){Text("Detener pasos pendientes")}
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        TextButton(enabled=!busy && rows.size==steps.size && selected.isNotEmpty(),onClick={save=!save}){Text("Guardar como comando")}
        if(save){Row{listOf("layers" to CommandIcons.Layers,"bolt" to CommandIcons.Bolt,"sun" to CommandIcons.Sun,"music" to CommandIcons.MusicNote).forEach{(key,symbol)->FilterChip(icon==key,{icon=key},label={Icon(symbol,key,Modifier.size(18.dp))})}};OutlinedTextField(name,{name=it.take(60)},label={Text("Nombre")},singleLine=true,modifier=Modifier.fillMaxWidth());OutlinedTextField(alias,{alias=it.take(50)},label={Text("Alias opcional")},singleLine=true,modifier=Modifier.fillMaxWidth());Text("Los valores relativos se guardan como los porcentajes revisados arriba.",style=MaterialTheme.typography.bodySmall);TextButton(enabled=name.isNotBlank() && !saving && !busy,onClick={val workflow=Workflow(java.util.UUID.randomUUID().toString(),name,alias,rows.filterIndexed{i,_->i in selected}.map{WorkflowStep("tool_action",Workflows.encodeTool(it.call!!))},icon,NativeCommands.KIND);val validation=Workflows.validate(workflow);if(validation!=null)error=validation else {saving=true;scope.launch{try{app.preferences.saveNativeCommand(workflow);save=false;error="Comando guardado."}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo guardar el comando."}finally{saving=false}}}}){Text(if(saving)"Guardando…" else "Guardar comando")}}
    }
}
