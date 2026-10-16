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

data class ToolExecution(val text:String,val action:Action?=null,val state:ExecutionState=if(action==null)ExecutionState.COMPLETED else ExecutionState.PREPARED)
class ToolAudit(context:Context) {
    private val privacy=(context.applicationContext as? SpotlightApp)?.preferences
    private val prefs=context.getSharedPreferences("tool_audit",Context.MODE_PRIVATE)
    @Synchronized fun record(id:String,status:String){if(privacy?.isPrivate==true)return;val old=runCatching{JSONArray(prefs.getString("events","[]"))}.getOrDefault(JSONArray());val next=JSONArray();for(i in maxOf(0,old.length()-49) until old.length())next.put(old.getJSONObject(i));next.put(JSONObject().put("tool",id).put("status",status).put("time",System.currentTimeMillis()));prefs.edit().putString("events",next.toString()).apply()}
    fun summary():String=runCatching{val a=JSONArray(prefs.getString("events","[]"));(0 until a.length()).joinToString("\n"){val j=a.getJSONObject(it);"${j.getLong("time")} · ${j.getString("tool")} · ${j.getString("status")}"}}.getOrDefault("")
    fun clear(){prefs.edit().remove("events").apply()}
}
class ToolExecutor(private val app:SpotlightApp) {
    suspend fun capabilities(c:Config):List<ToolCapability> = withContext(Dispatchers.IO) {
        val imported=app.index.hasImports()
        ToolRegistry.definitions
        .filter{dev.bautista.spotlight.ui.ProductVisibility.allowedExtension(it.extension)}.map { d ->
        val status:CapabilityStatus
        val reason:String
        when {
            !dev.bautista.spotlight.extensions.ExtensionStore.enabled(app,d.extension) -> {status=CapabilityStatus.DISABLED;reason="Activá la integración correspondiente en Ajustes."}
            d.advanced && !app.shizuku.state.value.available -> {status=CapabilityStatus.CONNECTION_REQUIRED;reason="Shizuku debe estar conectado y autorizado."}
            d.id=="write_display_setting" && app.shizuku.writeCapability.value!=true -> {status=CapabilityStatus.PERMISSION_REQUIRED;reason="Falta autorización de escritura verificada con Shizuku."}
            d.id=="control_media" && !listOf("play","pause","next","previous").any{MediaControl.available(app,it)} -> {status=CapabilityStatus.CONNECTION_REQUIRED;reason="No hay sesión multimedia compatible y autorizada."}
            d.id=="search_contacts" && !c.contacts -> {status=CapabilityStatus.DISABLED;reason="La búsqueda de contactos está desactivada."}
            d.id=="search_contacts" && !People.allowed(app) -> {status=CapabilityStatus.PERMISSION_REQUIRED;reason="Android no concedió acceso a contactos."}
            d.id=="calendar_agenda" && ContextCompat.checkSelfPermission(app,Manifest.permission.READ_CALENDAR)!=PackageManager.PERMISSION_GRANTED -> {status=CapabilityStatus.PERMISSION_REQUIRED;reason="Android no concedió acceso al calendario."}
            d.id=="search_files" && !c.files -> {status=CapabilityStatus.DISABLED;reason="La búsqueda de archivos está desactivada."}
            d.id=="search_files" && !app.index.broadAllowed() && !hasAuthorizedTree(c) && !imported -> {status=CapabilityStatus.PERMISSION_REQUIRED;reason="Elegí una carpeta con autorización vigente o acceso compartido."}
            d.id=="set_volume" && app.getSystemService(AudioManager::class.java).isVolumeFixed -> {status=CapabilityStatus.UNSUPPORTED;reason="Android informa un volumen fijo en este dispositivo."}
            else -> {status=CapabilityStatus.AVAILABLE;reason=d.risk.label}
        }
        ToolCapability(d,status,reason)
    }
    }
    private fun hasAuthorizedTree(c:Config)=runCatching {
        app.contentResolver.persistedUriPermissions.any { it.isReadPermission && it.uri.toString() in c.trees }
    }.getOrDefault(false)
    suspend fun available(c:Config)=capabilities(c).filter{it.status==CapabilityStatus.AVAILABLE}.map{it.definition}
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
                is ToolRequest.SettingWrite->{app.reversibleSettings.apply(request.key,request.value,confirmed);ToolExecution("Cambio verificado. El valor anterior quedó guardado para restaurarlo.",state=ExecutionState.VERIFIED)}
                is ToolRequest.PrepareMessage->ToolExecution("Borrador preparado. Elegí el destinatario y revisá el envío.",Action.ComposeMessage(request.channel,request.name,request.text))
                is ToolRequest.Workflow->{check(Workflows.parse(c.routines).any{it.id==request.id}){"La rutina no existe."};ToolExecution("Rutina preparada para revisión.",Action.WorkflowRun(request.id))}
                is ToolRequest.OpenApp->{val intent=app.packageManager.getLaunchIntentForPackage(request.packageName) ?: error("La aplicación no está instalada.");ToolExecution("Aplicación disponible para abrir.",Action.Launch(intent))}
                is ToolRequest.Volume->{
                    val audio=app.getSystemService(AudioManager::class.java)
                    check(!audio.isVolumeFixed){"Android no permite cambiar el volumen en este dispositivo."}
                    val max=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val min=if(Build.VERSION.SDK_INT>=28)audio.getStreamMinVolume(AudioManager.STREAM_MUSIC) else 0
                    check(max>min){"No hay un rango de volumen modificable."}
                    val target=min+(max-min)*request.percent/100
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC,target,0)
                    val observed=audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                    check(observed==target){"Android informó un nivel distinto al solicitado: $observed/$max. Revisá el estado; no se reintenta automáticamente."}
                    ToolExecution("Volumen multimedia observado: $observed/$max (${(observed-min)*100/(max-min)} %).",state=ExecutionState.VERIFIED)
                }
                is ToolRequest.Media->ToolExecution(MediaControl.execute(app,request.command),state=ExecutionState.REQUESTED)
                ToolRequest.Device->ToolExecution("${Build.MANUFACTURER} ${Build.MODEL}\nAndroid ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}",Action.SystemPanel("device"))
                ToolRequest.Diagnostics->{val state=app.shizuku.state.value;ToolExecution("${state.status.label}\n${state.privilege}\nAPI de servicio ${state.version ?: "—"}",Action.SystemPanel("shizuku"))}
            }}}
            app.toolAudit.record(call.name,execution.state.name.lowercase());return execution
        }catch(e:CancellationException){app.toolAudit.record(call.name,"cancelled");throw e}catch(e:Exception){app.toolAudit.record(call.name,"failed");throw e}
    }
}
