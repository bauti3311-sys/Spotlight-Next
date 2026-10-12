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
            is PlanStep.RelativeBrightness->{val value=(android.provider.Settings.System.getInt(app.contentResolver,"screen_brightness",128)*100/255+step.delta).coerceIn(8,100);title="Brillo ${if(step.delta>0)"+" else ""}${step.delta}% → $value%";call=ToolCall("write_display_setting",mapOf("key" to "screen_brightness","value" to (value*255/100).toString()))}
            is PlanStep.RelativeVolume->{val a=app.getSystemService(android.media.AudioManager::class.java);val max=a.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC).coerceAtLeast(1);val value=(a.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)*100/max+step.delta).coerceIn(0,100);title="Volumen ${if(step.delta>0)"+" else ""}${step.delta}% → $value%";call=ToolCall("set_volume",mapOf("percent" to value.toString()))}
            is PlanStep.Media->{title=when(step.command){"pause"->"Pausar música";"play"->"Reanudar música";"next"->"Siguiente tema";else->"Tema anterior"};call=if(dev.bautista.spotlight.control.MediaControl.available(app,step.command))ToolCall("control_media",mapOf("command" to step.command)) else null}
            is PlanStep.Open->{title="Abrir ${step.name}";val apps=app.search.search(step.name,c,SearchScope("","apps")).first().filter { it.action is Action.Launch && it.category=="Aplicaciones" };val exact=apps.filter { normalize(it.title)==normalize(step.name) };val match=exact.singleOrNull() ?: apps.singleOrNull();call=(match?.action as? Action.Launch)?.intent?.component?.packageName?.let { ToolCall("open_app",mapOf("package" to it)) }}
        }
        val ready=call!=null && call.name in available
        ReviewedStep(title,if(ready)call else null,when {
            ready->"Disponible · requiere tu confirmación"
            step is PlanStep.Media->"Necesita acceso a notificaciones y una sesión multimedia que soporte esta acción."
            step is PlanStep.Brightness && step.percent*255/100<20->"El ajuste reversible admite un mínimo de 20/255; elegí un porcentaje mayor."
            step is PlanStep.Brightness->"Requiere Shizuku conectado, autorización de escritura y extensión Sistema activa."
            step is PlanStep.Open && call==null->"No hay una app única coincidente. Buscá y elegí la aplicación."
            else->"La extensión necesaria está desactivada."
        })
    }
}

@Composable fun InterpretationPanel(app:SpotlightApp,c:Config,raw:String,onAction:(Action)->Unit) {
    val plan=remember(raw){LocalPlan.parse(raw) ?: LocalPlan.step(raw)?.takeIf{it is PlanStep.RelativeBrightness || it is PlanStep.RelativeVolume || it is PlanStep.Media}?.let{LocalPlan(listOf(it))}}
    if(plan!=null){EditablePlanPanel(app,c,raw,plan,onAction);return}
    Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        when(normalize(raw)) {
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
