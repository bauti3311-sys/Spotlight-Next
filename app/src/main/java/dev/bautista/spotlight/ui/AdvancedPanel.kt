package dev.bautista.spotlight.ui

import android.app.ActivityManager
import android.content.*
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.advanced.*

@Composable fun AdvancedPanel(app:SpotlightApp,page:String,onBack:()->Unit={}) {
    LaunchedEffect(page){if(page!="device")app.shizuku.start()}
    val context=LocalContext.current
    val state by app.shizuku.state.collectAsState()
    var error by remember{mutableStateOf("")};var showDetails by remember{mutableStateOf(false)}
    fun open(intent:Intent){runCatching{context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}.onFailure{error="No hay una aplicación compatible para abrir esta pantalla."}}
    Column(Modifier.fillMaxWidth().heightIn(max=340.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(if(page=="device")"Dispositivo" else if(page=="controls")"Sistema y controles" else "Shizuku",style=MaterialTheme.typography.titleMedium)}
        if(page=="device") {
            val resources=remember{dev.bautista.spotlight.control.DeviceResources.read(context)}
            val disk=remember{StatFs(Environment.getDataDirectory().path)}
            val dm=context.resources.displayMetrics
            InfoField("Modelo","${Build.MANUFACTURER} ${Build.MODEL}",CommandIcons.Display)
            InfoField("Sistema","Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}",CommandIcons.Info)
            InfoField("Memoria disponible",resources.availableMemory?.let{android.text.format.Formatter.formatFileSize(context,it)} ?: "No disponible",CommandIcons.Storage)
            InfoField("Almacenamiento libre",android.text.format.Formatter.formatFileSize(context,disk.availableBytes),CommandIcons.Folder)
            InfoField("Pantalla","${dm.widthPixels} × ${dm.heightPixels} px · ${dm.densityDpi} dpi",CommandIcons.Display)
            InfoField("Batería",resources.batteryPercent?.let{"$it %"} ?: "No disponible",CommandIcons.Battery)
            Text("Datos obtenidos del dispositivo actual mediante APIs públicas.",style=MaterialTheme.typography.bodySmall)
        } else {
            ConnectionCard(state)
            TextButton(onClick={showDetails=!showDetails}){Text(if(showDetails)"Ocultar detalles técnicos" else "Ver detalles técnicos")};if(showDetails)SelectionContainer{Text("${state.privilege} · API del servicio ${state.version ?: "—"}",style=MaterialTheme.typography.bodySmall)}
            Text("La búsqueda y las funciones locales siguen disponibles sin Shizuku.",style=MaterialTheme.typography.bodySmall)
            if(!state.authorized) Button(enabled=state.status in setOf(ConnectionStatus.AUTHORIZATION,ConnectionStatus.DENIED),onClick={error=app.shizuku.requestAuthorization().orEmpty()}){Text("Autorizar Spotlight")}
            Row{TextButton(onClick={app.shizuku.refresh()}){Text("Comprobar conexión")};TextButton(onClick={val intent=context.packageManager.getLaunchIntentForPackage(ShizukuManager.PACKAGE);open(intent ?: Intent(Intent.ACTION_VIEW,Uri.parse("https://shizuku.rikka.app/download/")))}){Text(if(state.status==ConnectionStatus.ABSENT)"Instalar" else "Abrir Shizuku")}}
            Text("Sin root: iniciá Shizuku mediante depuración inalámbrica y autorizá Spotlight. Después de reiniciar Android, volvé a iniciar el servicio. Spotlight no activa depuración ni concede permisos automáticamente.",style=MaterialTheme.typography.bodySmall)
            TextButton(onClick={open(Intent(Intent.ACTION_VIEW,Uri.parse("https://shizuku.rikka.app/guide/setup/")))}){Text("Guía oficial de inicio")}
            if(page=="controls") {
                AdvancedControls(app)
                listOf("Pantalla" to Settings.ACTION_DISPLAY_SETTINGS,"Aplicaciones" to Settings.ACTION_APPLICATION_SETTINGS,"Permisos" to Settings.ACTION_APPLICATION_DETAILS_SETTINGS,"Multimedia" to Settings.ACTION_SOUND_SETTINGS,"Desarrollador" to Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).forEach{(label,action)->TextButton(onClick={open(if(action==Settings.ACTION_APPLICATION_DETAILS_SETTINGS)Intent(action,Uri.parse("package:"+context.packageName)) else Intent(action))}){Icon(CommandIcons.Settings,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text(label)}}
            }
        }
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
    }
}
