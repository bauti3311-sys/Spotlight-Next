package dev.bautista.spotlight.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.automation.*
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config

@Composable fun WorkflowSettings(config:Config,update:(Config)->Unit) {
    val saved=Workflows.parse(config.routines)
    var edit by remember{mutableStateOf<Workflow?>(null)};var error by remember{mutableStateOf("")}
    val context=LocalContext.current;var fileStep by remember{mutableIntStateOf(-1)}
    val filePicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null){val w=edit;if(w!=null && fileStep in w.steps.indices){runCatching{context.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)};edit=w.copy(steps=w.steps.toMutableList().apply{set(fileStep,get(fileStep).copy(value=uri.toString()))})}}}

    Text("Rutinas locales con revisión, progreso y cancelación. {entrada} toma el resultado anterior; {variable:nombre} reutiliza una variable. Si / Si no / Fin forman bifurcaciones. Las acciones y el envío a IA se confirman en cada paso.")
    saved.forEach{w->Row(Modifier.fillMaxWidth()){TextButton(onClick={edit=w},modifier=Modifier.weight(1f)){Text(w.name+" · ${w.steps.size} pasos")};TextButton(onClick={update(config.copy(routines=Workflows.encode(saved.filter{it.id!=w.id})))}){Text("Eliminar")}}}
    Row{OutlinedButton(onClick={edit=Workflow(java.util.UUID.randomUUID().toString(),"",steps=listOf(WorkflowStep("text")))}){Text("Nueva rutina")};TextButton(onClick={edit=Workflow(java.util.UUID.randomUUID().toString(),"Modo estudio", "estudiar",listOf(WorkflowStep("volume","20"),WorkflowStep("text","Sesión de estudio {fecha} {hora}"),WorkflowStep("note"),WorkflowStep("timer","25")))}){Text("Modo estudio")}}
    TextButton(onClick={edit=Workflow(java.util.UUID.randomUUID().toString(),"Diagnóstico local",steps=listOf(WorkflowStep("tool",Workflows.encodeTool(ToolCall("device_info",emptyMap()))),WorkflowStep("setvar","equipo={entrada}"),WorkflowStep("tool",Workflows.encodeTool(ToolCall("shizuku_diagnostics",emptyMap()))),WorkflowStep("text","{variable:equipo}\n{entrada}"),WorkflowStep("copy")))}){Text("Plantilla: diagnóstico")};TextButton(onClick={edit=Workflow(java.util.UUID.randomUUID().toString(),"Limpiar y copiar","limpiar",listOf(WorkflowStep("require_text"),WorkflowStep("clean"),WorkflowStep("dedupe"),WorkflowStep("copy")))}){Text("Plantilla: limpiar y copiar")}
    edit?.let{w->
        OutlinedTextField(w.name,{edit=w.copy(name=it)},label={Text("Nombre")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(w.alias,{edit=w.copy(alias=it)},label={Text("Alias opcional")},modifier=Modifier.fillMaxWidth())
        w.steps.forEachIndexed{i,s->
            Text("Paso ${i+1}",style=MaterialTheme.typography.labelLarge);if(s.type=="extract")TextButton(onClick={fileStep=i;filePicker.launch(arrayOf("application/pdf","text/*","image/*")+dev.bautista.spotlight.index.OfficeText.types.toTypedArray())}){Text("Elegir archivo con Android")}
            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(4.dp)){Workflows.types.forEach{type->FilterChip(s.type==type,{edit=w.copy(steps=w.steps.toMutableList().apply{set(i,s.copy(type=type))})},label={Text(Workflows.labels[type] ?: type)})}}
            if(s.type in setOf("tool","tool_action")){
                val call=runCatching{Workflows.toolCall(s.value)}.getOrNull()
                val choices=ToolRegistry.definitions.filter{it.id!="run_workflow" && (it.risk==ToolRisk.READ)==(s.type=="tool")}
                Row(Modifier.horizontalScroll(rememberScrollState())){choices.forEach{d->FilterChip(call?.name==d.id,{val next=ToolCall(d.id,d.fields.associateWith{d.choices[it]?.firstOrNull().orEmpty()});edit=w.copy(steps=w.steps.toMutableList().apply{set(i,s.copy(value=Workflows.encodeTool(next)))})},label={Text(d.title)})}}
                call?.let{selected->ToolRegistry.definition(selected.name)?.fields?.forEach{field->OutlinedTextField(selected.arguments[field].orEmpty(),{text->val next=selected.copy(arguments=selected.arguments+ (field to text));edit=w.copy(steps=w.steps.toMutableList().apply{set(i,s.copy(value=Workflows.encodeTool(next)))})},label={Text(field+" · admite variables")},modifier=Modifier.fillMaxWidth())}}
            }else if(s.type !in setOf("upper","lower","clean","dedupe","require_text","if_empty","else","end"))OutlinedTextField(s.value,{value->edit=w.copy(steps=w.steps.toMutableList().apply{set(i,s.copy(value=value))})},label={Text(if(s.type in setOf("volume","wait","timer"))"Valor numérico" else "Texto · vacío usa {entrada}")},modifier=Modifier.fillMaxWidth())
            if(s.type in setOf("tool","extract","ai_text","clean","dedupe","upper","lower","require_text"))Row{Checkbox(s.onError=="continue",{enabled->edit=w.copy(steps=w.steps.toMutableList().apply{set(i,s.copy(onError=if(enabled)"continue" else "stop"))})});Text("Continuar si esta consulta falla")}
            TextButton(onClick={edit=w.copy(steps=w.steps.filterIndexed{index,_->index!=i})}){Text("Quitar paso")}
        }
        Row{TextButton(enabled=w.steps.size<12,onClick={edit=w.copy(steps=w.steps+WorkflowStep("copy"))}){Text("Añadir paso")};Button(onClick={error=Workflows.validate(w).orEmpty();if(error.isBlank()){if(saved.size>=30 && saved.none{it.id==w.id})error="Máximo 30 rutinas" else {update(config.copy(routines=Workflows.encode(saved.filter{it.id!=w.id}+w)));edit=null}}}){Text("Guardar")};TextButton(onClick={edit=null;error=""}){Text("Cancelar")}}
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
    }
}
