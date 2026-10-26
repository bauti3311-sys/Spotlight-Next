package dev.bautista.spotlight.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import dev.bautista.spotlight.search.Action
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.advanced.*
import kotlinx.coroutines.*

@Composable fun AdvancedControls(app:SpotlightApp) {
    val state by app.shizuku.state.collectAsState();val scope=rememberCoroutineScope()
    var values by remember{mutableStateOf<Map<String,String>>(emptyMap())};var supported by remember{mutableStateOf(false)};var busy by remember{mutableStateOf(false)};var error by remember{mutableStateOf("")};var pending by remember{mutableStateOf<Triple<String,String,Boolean>?>(null)}
    val labels=mapOf("screen_brightness" to "Brillo manual","screen_off_timeout" to "Tiempo para apagar pantalla","accelerometer_rotation" to "Rotación automática")
    fun refresh(){scope.launch{busy=true;error="";try{supported=app.advancedBackend.canWrite();values=labels.keys.associateWith{app.advancedBackend.read(it)}}catch(e:CancellationException){throw e}catch(e:Exception){supported=false;error=e.message.orEmpty()}finally{busy=false}}}
    LaunchedEffect(state.available){if(state.available)refresh() else {values=emptyMap();supported=false;pending=null}}
    Text("Pantalla",style=MaterialTheme.typography.titleSmall)
    Text("Ajustes opcionales mediante ADB. La compatibilidad se comprueba al conectarse; cada cambio guarda el valor anterior.",style=MaterialTheme.typography.bodySmall)
    if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
    if(!state.available)Text("Iniciá Shizuku y autorizá Spotlight para comprobar estos controles.",style=MaterialTheme.typography.bodySmall)
    else if(!supported && !busy)Text("Modificación no disponible en esta conexión. Podés abrir los ajustes públicos de Android.",style=MaterialTheme.typography.bodySmall)
    if(supported && !busy) {
        labels.forEach{(key,label)->
            Text("$label · ${values[key] ?: "—"}",style=MaterialTheme.typography.bodyMedium)
            Row(Modifier.horizontalScroll(rememberScrollState())){
                val options=when(key){"screen_brightness"->listOf("60" to "Bajo","128" to "Medio","220" to "Alto");"screen_off_timeout"->listOf("60000" to "1 minuto","300000" to "5 minutos","600000" to "10 minutos");else->listOf("0" to "Bloqueada","1" to "Automática")}
                options.forEach{(value,title)->TextButton(onClick={pending=Triple(key,value,false)}){Text(title)}}
                if(app.settingsJournal.original(key)!=null)TextButton(onClick={pending=Triple(key,app.settingsJournal.original(key)!!,true)}){Text("Restaurar")}
            }
        }
        Text("El brillo manual puede no afectar la pantalla mientras el brillo automático esté activo. No se cambia ese modo.",style=MaterialTheme.typography.bodySmall)
    }
    pending?.let{(key,value,restore)->
        Surface(color=MaterialTheme.colorScheme.secondaryContainer,shape=MaterialTheme.shapes.medium){Column(Modifier.padding(12.dp)){
            Text(if(restore)"Restaurar ${labels[key]}" else "Cambiar ${labels[key]}",style=MaterialTheme.typography.titleSmall)
            Text("Actual: ${values[key]} · nuevo: $value. Podés restaurar el valor anterior desde este panel.")
            Row{TextButton(enabled=state.available && !busy,onClick={pending=null;scope.launch{busy=true;try{withContext(Dispatchers.IO){if(restore)app.reversibleSettings.restore(key,true) else app.reversibleSettings.apply(key,value,true)};refresh()}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message.orEmpty()}finally{busy=false}}}){Text("Confirmar")};TextButton(onClick={pending=null}){Text("Cancelar")}}
        }}
    }
    if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
    TextButton(enabled=state.available && !busy,onClick={refresh()}){Text("Actualizar controles")}
}
@Composable fun ApplicationInspector(app:SpotlightApp,packageName:String,onAction:(Action)->Unit={action->if(action is Action.Launch)app.startActivity(action.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}) {
    var details by remember(packageName){mutableStateOf<InspectedApp?>(null)};var error by remember{mutableStateOf("")};var confirm by remember{mutableStateOf(false)};var busy by remember{mutableStateOf(false)}
    var permissionsExpanded by remember{mutableStateOf(false)};var componentsExpanded by remember{mutableStateOf(false)}
    val scope=rememberCoroutineScope();val state by app.shizuku.state.collectAsState()
    LaunchedEffect(packageName){try{details=AppInspector.inspect(app,packageName)}catch(e:CancellationException){throw e}catch(e:Exception){error="La aplicación no está disponible o no publica esta información."}}
    Column(Modifier.fillMaxWidth().heightIn(max=340.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        Text(details?.label ?: "Información de la aplicación",style=MaterialTheme.typography.titleMedium)
        details?.let{d->
            Row(Modifier.horizontalScroll(rememberScrollState())){TextButton(onClick={val intent=app.packageManager.getLaunchIntentForPackage(d.packageName);if(intent!=null)onAction(Action.Launch(intent)) else error="La aplicación no ofrece una pantalla de inicio."}){Text("Abrir aplicación")};TextButton(onClick={onAction(Action.Launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:${d.packageName}"))))}){Text("Ajustes y permisos")}}
            SelectionContainer{Text("${d.packageName}\nVersión ${d.version}\n${if(d.system)"Aplicación del sistema · protegida" else "Aplicación instalada"}")}
            if(state.available && AdvancedPolicy.canStop(d.packageName,d.system,setOf(app.packageName,ShizukuManager.PACKAGE))) {
                if(!confirm)TextButton(onClick={confirm=true}){Text("Detener aplicación · Shizuku")}
                else Surface(color=MaterialTheme.colorScheme.errorContainer,shape=MaterialTheme.shapes.medium){Column(Modifier.padding(12.dp)){Text("Detener ${d.label}");Text("Se detendrán servicios y notificaciones; puede perderse estado sin guardar. Volvé a abrirla para reanudarla. Los paquetes esenciales están protegidos.");Row{TextButton(enabled=!busy,onClick={confirm=false;scope.launch{busy=true;try{app.advancedBackend.stop(d.packageName,true);error="La solicitud de detención se completó."}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message.orEmpty()}finally{busy=false}}}){Text("Confirmar detención")};TextButton(onClick={confirm=false}){Text("Cancelar")}}}}
            }
            AppCapabilityActions(d.packageName,onAction)
            HorizontalDivider()
            TextButton(onClick={permissionsExpanded=!permissionsExpanded}){Text("Permisos declarados · ${d.permissions.size}")}
            if(permissionsExpanded){if(d.permissions.isEmpty())Text("No declara permisos.");d.permissions.take(80).forEach{(permission,granted)->Text("${permission.substringAfterLast('.')} · ${if(granted)"concedido" else "no concedido"}",style=MaterialTheme.typography.bodySmall)}}
            TextButton(onClick={componentsExpanded=!componentsExpanded}){Text("Componentes públicos · ${d.components.size}")}
            if(componentsExpanded){if(d.components.isEmpty())Text("No publica componentes accesibles.");d.components.forEach{Text(it,style=MaterialTheme.typography.bodySmall)};Text("Un componente exportado puede requerir permisos propios; se consulta sin intentar ejecutarlo.",style=MaterialTheme.typography.bodySmall)}
        }
        if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
        if(error.isNotBlank())Text(error,style=MaterialTheme.typography.bodySmall)
    }
}
