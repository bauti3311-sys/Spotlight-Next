package dev.bautista.spotlight.ai

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import androidx.core.content.ContextCompat
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.advanced.AppInspector
import dev.bautista.spotlight.automation.Workflows
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.*

data class ToolExecution(val text:String,val action:Action?=null)
class ToolAudit(context:Context) {
    private val privacy=(context.applicationContext as? SpotlightApp)?.preferences
    private val prefs=context.getSharedPreferences("tool_audit",Context.MODE_PRIVATE)
    @Synchronized fun record(id:String,status:String){if(privacy?.isPrivate==true)return;val old=runCatching{JSONArray(prefs.getString("events","[]"))}.getOrDefault(JSONArray());val next=JSONArray();for(i in maxOf(0,old.length()-49) until old.length())next.put(old.getJSONObject(i));next.put(JSONObject().put("tool",id).put("status",status).put("time",System.currentTimeMillis()));prefs.edit().putString("events",next.toString()).apply()}
    fun summary():String=runCatching{val a=JSONArray(prefs.getString("events","[]"));(0 until a.length()).joinToString("\n"){val j=a.getJSONObject(it);"${j.getLong("time")} · ${j.getString("tool")} · ${j.getString("status")}"}}.getOrDefault("")
    fun clear(){prefs.edit().remove("events").apply()}
}
class ToolExecutor(private val app:SpotlightApp) {
    fun available(c:Config):List<ToolDefinition> = ToolRegistry.definitions.filter{d->dev.bautista.spotlight.extensions.ExtensionStore.enabled(app,d.extension) && when{d.advanced->app.shizuku.state.value.available && (d.id!="write_display_setting" || app.shizuku.writeCapability.value==true);d.id=="control_media"->listOf("play","pause","next","previous").any{MediaControl.available(app,it)};d.id=="search_contacts"->c.contacts && People.allowed(app);d.id=="calendar_agenda"->ContextCompat.checkSelfPermission(app,Manifest.permission.READ_CALENDAR)==PackageManager.PERMISSION_GRANTED;d.id=="search_files"->c.files && (app.index.broadAllowed() || c.trees.isNotEmpty());else->true}}
    suspend fun execute(call:ToolCall,c:Config,confirmed:Boolean=false):ToolExecution {
        val request=ToolRegistry.authorize(call,available(c).map{it.id}.toSet(),confirmed)
        try {
            val execution=withTimeout(20000){withContext(Dispatchers.IO){when(request){
                is ToolRequest.Calculate->ToolExecution(Calculator.evaluate(request.expression) ?: error("Expresión no válida."))
                is ToolRequest.Convert->ToolExecution(LocalTools.convert(request.query)?.let{"${it.title} · ${it.subtitle}"} ?: error("Conversión no compatible."))
                is ToolRequest.Transform->ToolExecution(TextTools.transform(request.operation,request.text))
                is ToolRequest.CreateNote->{app.local.saveNote(request.text);ToolExecution("Nota guardada en este dispositivo.")}
                is ToolRequest.Search->{val result=when(request.kind){"apps"->app.search.search(request.query,c,SearchScope("","apps")).first();"files"->{app.index.ensure(c);app.index.search(request.query,c)};else->People.find(app,request.query).map{person->Result("contact:${person.id}:${person.address}",person.name,person.address,"Contactos",0,Action.Copy(person.address))}};ToolExecution(result.take(15).joinToString("\n"){"${it.title} · ${it.subtitle}"}.ifBlank{"No se encontraron coincidencias."})}
                is ToolRequest.Calendar->ToolExecution(Agenda.search(app,"",request.period).take(15).joinToString("\n"){"${it.title} · ${it.subtitle}"}.ifBlank{"Sin eventos en ese período."})
                is ToolRequest.InspectApp->{val d=AppInspector.inspect(app,request.packageName);ToolExecution("${d.label} · ${d.packageName}\nVersión ${d.version}\n${d.permissions.size} permisos declarados · ${d.components.size} componentes públicos",Action.SystemPanel("app",request.packageName))}
                is ToolRequest.SettingRead->ToolExecution("${request.key}: ${app.advancedBackend.read(request.key)}")
                is ToolRequest.SettingWrite->{app.reversibleSettings.apply(request.key,request.value,confirmed);ToolExecution("Cambio verificado. El valor anterior quedó guardado para restaurarlo.")}
                is ToolRequest.PrepareMessage->ToolExecution("Borrador preparado. Elegí el destinatario y revisá el envío.",Action.ComposeMessage(request.channel,request.name,request.text))
                is ToolRequest.Workflow->{check(Workflows.parse(c.routines).any{it.id==request.id}){"La rutina no existe."};ToolExecution("Rutina preparada para revisión.",Action.WorkflowRun(request.id))}
                is ToolRequest.OpenApp->{val intent=app.packageManager.getLaunchIntentForPackage(request.packageName) ?: error("La aplicación no está instalada.");ToolExecution("Aplicación disponible para abrir.",Action.Launch(intent))}
                is ToolRequest.Volume->{val audio=app.getSystemService(AudioManager::class.java);audio.setStreamVolume(AudioManager.STREAM_MUSIC,audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*request.percent/100,0);ToolExecution("Volumen multimedia solicitado: ${request.percent} %.")}
                is ToolRequest.Media->ToolExecution(MediaControl.execute(app,request.command))
                ToolRequest.Device->ToolExecution("${Build.MANUFACTURER} ${Build.MODEL}\nAndroid ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}",Action.SystemPanel("device"))
                ToolRequest.Diagnostics->{val state=app.shizuku.state.value;ToolExecution("${state.status.label}\n${state.privilege}\nAPI de servicio ${state.version ?: "—"}",Action.SystemPanel("shizuku"))}
            }}}
            app.toolAudit.record(call.name,if(execution.action!=null)"prepared" else "completed");return execution
        }catch(e:CancellationException){app.toolAudit.record(call.name,"cancelled");throw e}catch(e:Exception){app.toolAudit.record(call.name,"failed");throw e}
    }
}
