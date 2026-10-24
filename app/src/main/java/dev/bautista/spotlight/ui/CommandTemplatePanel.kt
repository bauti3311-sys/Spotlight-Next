package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
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
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.*

@Composable internal fun CommandTemplateEditor(app:SpotlightApp,initial:CommandTemplate,expectedRevision:Int?,onClose:()->Unit) {
    val key="${initial.id}:${initial.revision}"
    val fields=remember(key,initial.plan){initial.plan.calls.flatMapIndexed{i,call->call.arguments.keys.map{i to it}}}
    var name by rememberSaveable(key){mutableStateOf(initial.name)}
    var alias by rememberSaveable(key){mutableStateOf(initial.alias)}
    var values by rememberSaveable(key){mutableStateOf(fields.map{(i,f)->initial.targets.singleOrNull{it.step==i && it.argument==f}?.let{target->initial.parameters.single{it.name==target.parameter}.defaultValue} ?: initial.plan.calls[i].arguments.getValue(f)})}
    var parameterNames by rememberSaveable(key){mutableStateOf(fields.map{(i,f)->initial.targets.singleOrNull{it.step==i && it.argument==f}?.parameter.orEmpty()})}
    var error by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)}
    val scope=rememberCoroutineScope()
    fun draft():CommandTemplate {
        val calls=initial.plan.calls.mapIndexed{i,call->call.copy(arguments=call.arguments.mapValues{(f,_)->values[fields.indexOf(i to f)]})}
        val targets=fields.mapIndexedNotNull{n,(i,f)->parameterNames[n].takeIf{it.isNotBlank()}?.let{CommandParameterTarget(i,f,it)}}
        val parameters=targets.groupBy{it.parameter}.map{(id,group)->
            val definitions=group.map{target->CommandParameter(id,CommandTemplates.parameterType(calls[target.step],target.argument),calls[target.step].arguments.getValue(target.argument))}.distinct()
            require(definitions.size==1){"El parámetro $id tiene tipos o valores distintos."};definitions.single()
        }
        return initial.copy(name=name.trim(),alias=alias.trim(),plan=initial.plan.copy(calls=calls),parameters=parameters,targets=targets)
    }
    Column(Modifier.fillMaxWidth().testTag("template-editor"),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Plantilla local",style=MaterialTheme.typography.titleMedium)
        Text("Guardar y editar no ejecuta acciones. Los parámetros sustituyen argumentos completos; nunca instrucciones ni nombres de herramientas.",style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(name,{name=it.take(60)},label={Text("Nombre del comando")},enabled=!busy,modifier=Modifier.fillMaxWidth())
        OutlinedTextField(alias,{alias=it.take(24)},label={Text("Alias del comando")},enabled=!busy,modifier=Modifier.fillMaxWidth())
        fields.forEachIndexed{n,(i,f)->
            val bound=f in initial.bindings[i].orEmpty()
            Text("Paso ${i+1} · $f${if(bound)" · depende de otro paso" else ""}")
            OutlinedTextField(values[n],{v->values=values.toMutableList().also{it[n]=v.take(8000)}},enabled=!busy && !bound,
                label={Text("Valor ${i+1} · $f")},modifier=Modifier.fillMaxWidth())
            if(!bound)OutlinedTextField(parameterNames[n],{v->parameterNames=parameterNames.toMutableList().also{it[n]=v.take(40)}},enabled=!busy,
                label={Text("Parámetro ${i+1} · $f (opcional)")},supportingText={Text(CommandTemplates.parameterType(initial.plan.calls[i],f).name)},modifier=Modifier.fillMaxWidth())
        }
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        Row {
            TextButton(enabled=!busy,onClick={
                val next=runCatching{draft().also(CommandTemplates::validate)}.getOrElse{error=it.message ?: "Revisá los argumentos.";return@TextButton}
                busy=true;scope.launch{try{app.preferences.saveCommandTemplate(next,expectedRevision);onClose()}
                    catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo guardar."}finally{busy=false}}
            },modifier=Modifier.testTag("template-save")){Text(if(busy)"Guardando…" else "Guardar plantilla")}
            TextButton(enabled=!busy,onClick=onClose){Text("Cancelar edición")}
        }
    }
}

@Composable internal fun SavedTemplatePanel(app:SpotlightApp,config:Config,template:CommandTemplate,onAction:(Action)->Unit) {
    val key="${template.id}:${template.revision}"
    var editing by rememberSaveable(key){mutableStateOf(false)}
    var values by rememberSaveable(key){mutableStateOf(template.parameters.map{it.defaultValue})}
    // Freeze the reviewed invocation. Subsequent parameter edits require explicitly abandoning
    // this draft, so a recomposition cannot reset execution receipts and repeat effects.
    var preparedValues by rememberSaveable(key){mutableStateOf<List<String>?>(null)}
    var error by remember{mutableStateOf("")};val scope=rememberCoroutineScope()
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(template.name,style=MaterialTheme.typography.titleMedium)
        if(editing)CommandTemplateEditor(app,template,template.revision){editing=false}
        else {
            template.parameters.forEachIndexed{i,p->OutlinedTextField(values[i],{v->values=values.toMutableList().also{it[i]=v.take(8000)}},
                enabled=preparedValues==null,label={Text(p.name)},supportingText={Text(p.type.name)},modifier=Modifier.fillMaxWidth())}
            if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
            if(preparedValues==null) {
                Button(onClick={runCatching{CommandTemplates.instantiate(template,template.parameters.mapIndexed{i,p->p.name to values[i]}.toMap())}
                    .onSuccess{preparedValues=values.toList();error=""}.onFailure{error=it.message ?: "Revisá los parámetros."}},modifier=Modifier.testTag("template-prepare")){Text("Revisar pasos del comando")}
                TextButton(onClick={editing=true}){Text("Editar plantilla")}
                ConfirmedSettingsButton("Eliminar plantilla","Elimina únicamente esta plantilla guardada.") {
                    scope.launch{try{app.preferences.deleteCommandTemplate(template.id,template.revision);onAction(Action.RestoreQuery(""))}
                        catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo eliminar."}}
                }
            }
            preparedValues?.let{frozen->
                val plan=remember(key,frozen){runCatching{CommandTemplates.instantiate(template,template.parameters.mapIndexed{i,p->p.name to frozen[i]}.toMap())}}
                plan.getOrNull()?.let{resolved->AiToolTaskPanel(app,config,"command-template:$key",onAction,initialPlan=resolved,initialBindings=template.bindings)}
                    ?: Text("El borrador no es válido. Cerrá y volvé a revisar el comando.")
            }
        }
    }
}
