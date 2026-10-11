package dev.bautista.spotlight.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.*
import dev.bautista.spotlight.control.SearchScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

internal data class ReviewedStep(val title:String,val call:ToolCall?,val requirement:String)
internal suspend fun resolvePlan(app:SpotlightApp,c:Config,plan:LocalPlan):List<ReviewedStep> {
    val available=ToolExecutor(app).available(c).map { it.id }.toSet()
    return plan.steps.map { step ->
        val title:String
        val call:ToolCall?
        when(step) {
            is PlanStep.Brightness->{title="Brillo al ${step.percent}%";call=if(step.percent*255/100<20)null else ToolCall("write_display_setting",mapOf("key" to "screen_brightness","value" to (step.percent*255/100).toString()))}
            is PlanStep.Volume->{title="Volumen al ${step.percent}%";call=ToolCall("set_volume",mapOf("percent" to step.percent.toString()))}
            is PlanStep.Open->{title="Abrir ${step.name}";val apps=app.search.search(step.name,c,SearchScope("","apps")).first().filter { it.action is Action.Launch };val exact=apps.filter { normalize(it.title)==normalize(step.name) };val match=exact.singleOrNull() ?: apps.singleOrNull();call=(match?.action as? Action.Launch)?.intent?.component?.packageName?.let { ToolCall("open_app",mapOf("package" to it)) }}
        }
        val ready=call!=null && call.name in available
        ReviewedStep(title,if(ready)call else null,when {
            ready->"Disponible · requiere tu confirmación"
            step is PlanStep.Brightness && step.percent*255/100<20->"El ajuste reversible admite un mínimo de 20/255; elegí un porcentaje mayor."
            step is PlanStep.Brightness->"Requiere Shizuku conectado, autorización de escritura y extensión Sistema activa."
            step is PlanStep.Open && call==null->"No hay una app única coincidente. Buscá y elegí la aplicación."
            else->"La extensión necesaria está desactivada."
        })
    }
}

@Composable fun InterpretationPanel(app:SpotlightApp,c:Config,raw:String,onAction:(Action)->Unit) {
    val scope=rememberCoroutineScope()
    val plan=remember(raw){LocalPlan.parse(raw)}
    var rows by remember(raw){mutableStateOf<List<ReviewedStep>?>(null)}
    var selected by remember(raw){mutableStateOf(setOf<Int>())}
    var statuses by remember(raw){mutableStateOf(mapOf<Int,String>())}
    var busy by remember(raw){mutableStateOf(false)}
    var reviewed by remember(raw){mutableStateOf(false)}
    var error by remember(raw){mutableStateOf("")}
    val service by app.shizuku.state.collectAsState()
    val writeCapability by app.shizuku.writeCapability.collectAsState()
    LaunchedEffect(raw,c,service,writeCapability) {if(plan!=null && !busy && !reviewed)try{rows=resolvePlan(app,c,plan);selected=rows!!.indices.filter{rows!![it].call!=null}.toSet()}catch(e:CancellationException){throw e}catch(e:Exception){error="No se pudieron comprobar las acciones. Reintentá la búsqueda."}}
    Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        if(plan!=null) {
            Text("Revisar plan",style=MaterialTheme.typography.titleMedium)
            Text("Los pasos son independientes. Se ejecutan en orden solo los que selecciones; un fallo no impide los demás.",style=MaterialTheme.typography.bodySmall)
            if(rows==null && error.isBlank())Text("Comprobando aplicaciones y permisos…")
            rows?.forEachIndexed { i,row ->
                Row(Modifier.fillMaxWidth()) {
                    Checkbox(i in selected,onCheckedChange={ checked->selected=if(checked)selected+i else selected-i },enabled=row.call!=null && !busy && !reviewed)
                    Column(Modifier.weight(1f).padding(top=8.dp)) {Text("${i+1}. ${row.title}");Text(statuses[i] ?: row.requirement,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                }
            }
            if(plan.steps.any{it is PlanStep.Brightness} && rows?.any{it.call==null}==true)
                TextButton(enabled=!busy,onClick={onAction(Action.SystemPanel("shizuku"))}){Text("Revisar Shizuku y permisos")}
            Button(enabled=selected.isNotEmpty() && !busy && !reviewed,onClick={
                busy=true;reviewed=true
                scope.launch {
                    try {
                        rows!!.forEachIndexed { i,row ->if(i in selected) {
                            statuses=statuses+(i to "Ejecutando…")
                            try {
                                val execution=ToolExecutor(app).execute(row.call!!,c,confirmed=true)
                                val launch=execution.action as? Action.Launch
                                if(launch!=null)app.startActivity(Intent(launch.intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                statuses=statuses+(i to if(launch!=null)"Solicitud de apertura enviada a Android." else execution.text)
                            }catch(e:CancellationException){statuses=statuses+(i to "Interrumpido; revisá el estado del dispositivo.");throw e}catch(e:Exception){statuses=statuses+(i to (e.message ?: "No se pudo ejecutar este paso."))}
                        }}
                    }finally{busy=false}
                }
            }) {Text(if(busy)"Ejecutando…" else if(reviewed)"Plan revisado" else "Confirmar pasos seleccionados")}
            if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        } else when(normalize(raw)) {
            "quiero estudiar sin distracciones"->{
                Text("Preparar una sesión de estudio",style=MaterialTheme.typography.titleMedium)
                Text("Elegí los ajustes que necesitás. Spotlight no activa un modo ni cambia permisos automáticamente.")
                TextButton(onClick={onAction(Action.AudioPanel())}){Text("Revisar volumen multimedia")}
                TextButton(onClick={onAction(Action.TimerForm(25))}){Text("Preparar temporizador de 25 minutos")}
                TextButton(onClick={onAction(Action.Launch(Intent(android.provider.Settings.ACTION_SOUND_SETTINGS)))}){Text("Revisar sonido en Android")}
            }
            "explicame este pdf","explica este pdf"->{
                Text("Elegí primero el PDF",style=MaterialTheme.typography.titleMedium)
                Text("Buscá un archivo autorizado, abrí sus acciones, elegí Extraer texto y después Explicar con IA. Revisá el texto antes de enviarlo. No se lee ningún documento al escribir esta consulta.")
                val allowed=c.files && (app.index.broadAllowed() || c.trees.isNotEmpty())
                TextButton(onClick={onAction(if(allowed)Action.RestoreQuery("kind:pdf ") else Action.Access("files","kind:pdf "))}){Text(if(allowed)"Buscar un PDF" else "Autorizar búsqueda de archivos")}
            }
            else->{
                var text by remember(raw){mutableStateOf("")}
                Text("Texto para traducir",style=MaterialTheme.typography.titleMedium)
                Text("Pegá el texto que querés compartir. No se lee el portapapeles automáticamente.")
                OutlinedTextField(text,{text=it.take(8000)},Modifier.fillMaxWidth(),label={Text("Texto")},minLines=3)
                TextButton(enabled=text.isNotBlank(),onClick={onAction(Action.Ask("Traducí al español este texto:\n$text",true))}){Text("Revisar traducción con IA")}
                Text("Requiere configurar el proveedor de IA; se revisa el envío antes de consultar.",style=MaterialTheme.typography.bodySmall)
            }
        }
    }
}
