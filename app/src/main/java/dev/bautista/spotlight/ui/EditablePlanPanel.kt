package dev.bautista.spotlight.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.automation.*
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*

private fun text(step:PlanStep):String=when(step){is PlanStep.Brightness->"brillo al ${step.percent}%";is PlanStep.Volume->"volumen al ${step.percent}%";is PlanStep.Open->"abrir ${step.name}";is PlanStep.RelativeBrightness->"${if(step.delta<0)"bajar" else "subir"} brillo en ${kotlin.math.abs(step.delta)}%";is PlanStep.RelativeVolume->"${if(step.delta<0)"bajar" else "subir"} volumen en ${kotlin.math.abs(step.delta)}%";is PlanStep.Media->when(step.command){"pause"->"pausar música";"play"->"reanudar música";"next"->"siguiente tema";else->"tema anterior"}}
@Composable fun EditablePlanPanel(app:SpotlightApp,c:Config,raw:String,initial:LocalPlan,onAction:(Action)->Unit) {
    val scope=rememberCoroutineScope();var steps by remember(raw){mutableStateOf(initial.steps.map(::text))};var rows by remember(raw){mutableStateOf<List<ReviewedStep>>(emptyList())};var selected by remember(raw){mutableStateOf(setOf<Int>())};var status by remember(raw){mutableStateOf(mapOf<Int,String>())};var busy by remember{mutableStateOf(false)};var job by remember{mutableStateOf<Job?>(null)};var editing by remember{mutableIntStateOf(-1)};var error by remember{mutableStateOf("")};var save by remember{mutableStateOf(false)};var name by remember{mutableStateOf("")};var alias by remember{mutableStateOf("")};var icon by remember{mutableStateOf("layers")}
    val state by app.shizuku.state.collectAsState();val write by app.shizuku.writeCapability.collectAsState()
    LaunchedEffect(steps,c,state,write){if(!busy){status=emptyMap();error="";val parsed=steps.map{LocalPlan.step(it)};if(parsed.any{it==null}){rows=emptyList();selected=emptySet();error="Revisá el paso editado: brillo, volumen, abrir una app o controlar música."}else try{rows=resolvePlan(app,c,LocalPlan(parsed.filterNotNull()));selected=rows.indices.filter{rows[it].call!=null}.toSet()}catch(e:CancellationException){throw e}catch(e:Exception){error="No se pudieron comprobar los requisitos."}}}
    Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Revisar plan",style=MaterialTheme.typography.titleMedium);Text("Editá, ordená u omití pasos. Solo se ejecutan después de confirmar.",style=MaterialTheme.typography.bodySmall)
        steps.forEachIndexed {i,value->
            Row{Checkbox(i in selected,{checked->selected=if(checked)selected+i else selected-i},enabled=!busy && rows.getOrNull(i)?.call!=null);Column(Modifier.weight(1f)){Text("${i+1}. ${rows.getOrNull(i)?.title ?: value}");Text(status[i] ?: rows.getOrNull(i)?.requirement.orEmpty(),style=MaterialTheme.typography.bodySmall)
                if(editing==i)OutlinedTextField(value,{new->steps=steps.toMutableList().also{it[i]=new.take(150)}},modifier=Modifier.fillMaxWidth(),label={Text("Paso")},enabled=!busy,singleLine=true)
                Row{TextButton(enabled=!busy,onClick={editing=if(editing==i)-1 else i}){Text(if(editing==i)"Listo" else "Editar")};TextButton(enabled=!busy && i>0,onClick={steps=steps.toMutableList().also{val previous=it[i-1];it[i-1]=it[i];it[i]=previous};editing=-1}){Text("Subir")};TextButton(enabled=!busy && steps.size>1,onClick={steps=steps.filterIndexed{index,_->index!=i};editing=-1}){Text("Quitar")}}
            }}
        }
        if(rows.any{it.call==null})TextButton(onClick={onAction(Action.SystemPanel("shizuku"))}){Text("Revisar permisos y servicios")}
        Button(enabled=!busy && selected.isNotEmpty() && rows.size==steps.size && status.isEmpty(),onClick={busy=true;editing=-1;val chosen=selected;val review=rows.toList();job=scope.launch{try{review.forEachIndexed{i,row->if(i !in chosen)status=status+(i to "Omitido") else {status=status+(i to "Ejecutando…");try{val result=ToolExecutor(app).execute(row.call!!,c,true);(result.action as? Action.Launch)?.let{app.startActivity(Intent(it.intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))};status=status+(i to "Completado · ${result.text}")}catch(e:CancellationException){status=status+(i to "Interrumpido · revisá el estado del dispositivo");throw e}catch(e:Exception){status=status+(i to "Falló · ${e.message.orEmpty().take(200)}")}}}}finally{busy=false}}}){Text(if(busy)"Ejecutando…" else if(status.isNotEmpty())"Plan revisado" else "Confirmar pasos seleccionados")}
        if(busy)TextButton(onClick={job?.cancel()}){Text("Detener pasos pendientes")}
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        TextButton(enabled=!busy && rows.size==steps.size && selected.isNotEmpty(),onClick={save=!save}){Text("Guardar como comando")}
        if(save){Row{listOf("layers" to CommandIcons.Layers,"bolt" to CommandIcons.Bolt,"sun" to CommandIcons.Sun,"music" to CommandIcons.MusicNote).forEach{(key,symbol)->FilterChip(icon==key,{icon=key},label={Icon(symbol,key,Modifier.size(18.dp))})}};OutlinedTextField(name,{name=it.take(60)},label={Text("Nombre")},singleLine=true,modifier=Modifier.fillMaxWidth());OutlinedTextField(alias,{alias=it.take(50)},label={Text("Alias opcional")},singleLine=true,modifier=Modifier.fillMaxWidth());Text("Los valores relativos se guardan como los porcentajes revisados arriba.",style=MaterialTheme.typography.bodySmall);TextButton(enabled=name.isNotBlank(),onClick={val workflow=Workflow(java.util.UUID.randomUUID().toString(),name,alias,rows.filterIndexed{i,_->i in selected}.map{WorkflowStep("tool_action",Workflows.encodeTool(it.call!!))},icon);val validation=Workflows.validate(workflow);if(validation!=null)error=validation else scope.launch{try{app.preferences.update(c.copy(routines=Workflows.encode(Workflows.parse(c.routines)+workflow)));save=false;error="Comando guardado."}catch(e:Exception){error="No se pudo guardar el comando."}}}){Text("Guardar comando")}}
    }
}
