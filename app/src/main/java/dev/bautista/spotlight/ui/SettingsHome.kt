package dev.bautista.spotlight.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.CredentialVault
import dev.bautista.spotlight.control.Replies
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class SettingsFeature(val id:String,val title:String,val detail:String,val status:String,val ready:Boolean,val icon:ImageVector)

/** Reads permissions only. Merely visiting this page never requests access or contacts a provider. */
internal suspend fun settingsFeatures(app:SpotlightApp,c:Config):List<SettingsFeature> = withContext(Dispatchers.IO) {
 fun granted(p:String)=ContextCompat.checkSelfPermission(app,p)==PackageManager.PERMISSION_GRANTED
 val folders=app.contentResolver.persistedUriPermissions.count{it.isReadPermission && it.uri.toString() in c.trees}
 val files=c.files && (folders>0 || app.index.broadAllowed() || app.index.hasImports())
 val overlay=Settings.canDrawOverlays(app)
 val contacts=granted(Manifest.permission.READ_CONTACTS)
 val calendar=granted(Manifest.permission.READ_CALENDAR)
 val location=granted(Manifest.permission.ACCESS_COARSE_LOCATION)
 val listeners=Settings.Secure.getString(app.contentResolver,"enabled_notification_listeners").orEmpty().split(':')
 val notifications=listeners.any{ComponentName.unflattenFromString(it)==ComponentName(app,dev.bautista.spotlight.control.ReplyService::class.java)}
 val services=Settings.Secure.getString(app.contentResolver,Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty().split(':')
 val keyboard=services.any{ComponentName.unflattenFromString(it)==ComponentName(app,dev.bautista.spotlight.access.ShortcutService::class.java)}
 val phoneUse=services.any{ComponentName.unflattenFromString(it)==ComponentName(app,dev.bautista.spotlight.agent.PhoneUseService::class.java)}
 val key=CredentialVault(app).contains("gemini")
 listOf(
  SettingsFeature("overlay","Ventana flotante","Abrí Spotlight sobre otras apps. Android te pide este acceso por separado.",if(overlay)"Permitido" else "Necesita autorización",overlay,CommandIcons.Display),
  SettingsFeature("files","Tus documentos","Elegí una carpeta; el resto del dispositivo permanece fuera de ese acceso.",if(!c.files)"Desactivado en Spotlight" else if(folders>0)"$folders carpetas autorizadas" else if(files)"Fuente local autorizada" else "Elegí una carpeta",files,CommandIcons.Folder),
  SettingsFeature("gemini","Gemini","Conectá tu propia clave. Las consultas se envían solo cuando lo decidís.",if(key)"Clave guardada · conexión por verificar" else "Sin configurar",false,CommandIcons.AutoAwesome),
  SettingsFeature("contacts","Contactos","Encontrá personas y prepará mensajes; no se envían automáticamente.",if(!c.contacts)"Desactivado en Spotlight" else if(contacts)"Permitido" else "Necesita autorización",contacts && c.contacts,CommandIcons.Person),
  SettingsFeature("notifications","Mensajes y música","Accedé a mensajes y controles que expongan las notificaciones compatibles.",if(notifications)if(Replies.connected.value)"Acceso y servicio activos" else "Acceso concedido · servicio desconectado" else "Opcional · sin acceso",notifications,CommandIcons.ChatBubbleOutline),
  SettingsFeature("calendar","Tu agenda","Consultá eventos. Crear o cambiar eventos se revisa por separado.",if(calendar)"Lectura permitida" else "Opcional · sin acceso",calendar,CommandIcons.Event),
  SettingsFeature("phone_use","Control de aplicaciones","Permití que Spotlight identifique y accione controles accesibles de otras apps solo cuando iniciás y confirmás una tarea. Se puede desactivar en cualquier momento.",if(phoneUse)"Servicio habilitado" else "Opcional · sin configurar",phoneUse,CommandIcons.Settings),
  SettingsFeature("keyboard","Atajos de teclado","Configurá el servicio de accesibilidad para invocar Spotlight con el teclado.",if(keyboard)"Servicio habilitado" else "Opcional · sin configurar",keyboard,CommandIcons.Keyboard),
  SettingsFeature("location","Clima cerca tuyo","Compartí ubicación aproximada con Android cuando elijas usarla.",if(location)"Ubicación aproximada permitida" else "Opcional · podés buscar por ciudad",location,CommandIcons.Cloud),
  SettingsFeature("shizuku","Control avanzado","Conectá Shizuku si necesitás acciones adicionales. No requiere root.",if(app.shizuku.state.value.available)"Servicio disponible" else "Opcional · sin conexión",app.shizuku.state.value.available,CommandIcons.Settings),
 )
}

@Composable fun SettingsHome(app:SpotlightApp,c:Config,revision:Int,onNavigate:(String)->Unit,onRequest:(String)->Unit,onDetails:()->Unit) {
 var center by rememberSaveable{mutableStateOf(false)}
 var selected by rememberSaveable{mutableStateOf(listOf<String>())}
 var queue by rememberSaveable{mutableStateOf(listOf<String>())}
 var step by rememberSaveable{mutableIntStateOf(-1)}
 var pending by rememberSaveable{mutableStateOf<String?>(null)}
 var features by remember{mutableStateOf<List<SettingsFeature>>(emptyList())}
 var failed by remember{mutableStateOf(false)}
 val connection by app.shizuku.state.collectAsState()
 val notificationConnection by Replies.connected.collectAsState()
 LaunchedEffect(c,revision,connection,notificationConnection) {failed=false;try{features=settingsFeatures(app,c)}catch(e:kotlinx.coroutines.CancellationException){throw e}catch(_:Exception){failed=true}}
 BackHandler(center && step<0 && pending==null){center=false}
 val colors=MaterialTheme.colorScheme
 fun configure(id:String){when(id){"phone_use"->{app.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))};"gemini"->{center=false;onNavigate("Inteligencia artificial")};"shizuku"->{center=false;onRequest("shizuku")};else->onRequest(id)}}
 Column(Modifier.fillMaxWidth().testTag(if(center)"settings-function-center" else "settings-home"),verticalArrangement=Arrangement.spacedBy(20.dp)) {
  if(center) {
   TextButton(onClick={center=false}){Text("Volver al inicio")}
   Text("Elegí qué puede hacer Spotlight",style=MaterialTheme.typography.headlineSmall)
   Text("Seleccioná solo lo que necesitás. Cada autorización se revisa por separado y podés omitirla.",style=MaterialTheme.typography.bodyMedium,color=colors.onSurfaceVariant)
   if(failed)Text("No se pudo comprobar el estado. No se asume ningún permiso concedido.",color=colors.error)
   if(features.isEmpty() && !failed)Text("Comprobando accesos locales…")
   features.forEach{f->
    Surface(shape=RoundedCornerShape(14.dp),color=colors.onSurface.copy(alpha=.035f)) {
     Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.Top,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
      Checkbox(f.id in selected,{checked->selected=if(checked)(selected+f.id).distinct() else selected-f.id},modifier=Modifier.testTag("feature-select-${f.id}"))
      Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
       Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(9.dp)){Icon(f.icon,null,Modifier.size(18.dp),tint=colors.onSurfaceVariant);Text(f.title,style=MaterialTheme.typography.titleSmall)}
       Text(f.detail,style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
       Text(f.status,style=MaterialTheme.typography.labelSmall,color=if(f.ready)colors.primary else colors.onSurfaceVariant)
       TextButton(onClick={pending=f.id},contentPadding=PaddingValues(vertical=4.dp)){Text("Configurar ${f.title.lowercase()}")}
      }
     }
    }
   }
   FilledTonalButton(onClick={queue=selected;step=0},enabled=selected.isNotEmpty(),modifier=Modifier.fillMaxWidth().testTag("guided-setup-start")){Text("Configurar selección · ${selected.size}")}
  } else {
   SettingsGroupSurface {
    Text("Configuración a tu medida",style=MaterialTheme.typography.titleSmall)
    Text("Elegí funciones y revisá cada autorización por separado.",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
    Text(if(failed)"Estado no disponible" else if(features.isEmpty())"Comprobando configuración…" else "${features.count{it.ready}} accesos habilitados · IA bajo tu control",style=MaterialTheme.typography.labelSmall,color=colors.onSurfaceVariant)
    TextButton(onClick={center=true},modifier=Modifier.testTag("open-function-center")){Icon(CommandIcons.Shield,null,Modifier.size(17.dp));Spacer(Modifier.width(8.dp));Text("Elegir funciones")}
   }
   HomeDestination("Conectar Gemini","Tu clave, tus decisiones",CommandIcons.AutoAwesome){onNavigate("Inteligencia artificial")}
   HomeDestination("Personalizar apariencia","Raycast Dark Glass y opacidad",CommandIcons.Display){onNavigate("Apariencia")}
   HomeDestination("Búsqueda y documentos","Carpetas, texto, OCR y significado local",CommandIcons.Folder){onNavigate("Búsqueda y archivos")}
   HomeDestination("Conectar tus apps","Mensajes, música y control del dispositivo",CommandIcons.Layers){onNavigate("Integraciones")}
   HomeDestination("Permisos y accesos","Todos los accesos Android reunidos y bajo tu control",CommandIcons.Shield){onNavigate("Permisos y accesos")}
   TextButton(onClick=onDetails){Text("Opciones de inicio y teclado")}
  }
 }
 val current=if(step>=0)queue.getOrNull(step) else pending
 val feature=features.firstOrNull{it.id==current}
 if(feature!=null)AlertDialog(onDismissRequest={step=-1;pending=null},title={Text(if(step>=0)"${step+1} de ${queue.size} · ${feature.title}" else feature.title)},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){Text(feature.detail);Text(feature.status,style=MaterialTheme.typography.bodySmall);Text("Se abrirá la configuración correspondiente. Nada se autoriza automáticamente.",style=MaterialTheme.typography.bodySmall)}},confirmButton={TextButton(onClick={configure(feature.id);if(feature.id in setOf("gemini","shizuku","phone_use")){step=-1;pending=null}}){Text("Configurar")}},dismissButton={TextButton(onClick={if(step>=0 && step+1<queue.size)step++ else{step=-1;pending=null}}){Text(if(step>=0)if(step+1<queue.size)"Siguiente / omitir" else "Finalizar" else "Cancelar")}})
}
@Composable private fun HomeShortcut(title:String,detail:String,icon:ImageVector,modifier:Modifier=Modifier,onClick:()->Unit) {
 val colors=MaterialTheme.colorScheme
 Surface(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(15.dp),color=colors.onSurface.copy(alpha=.045f)) {
  Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Icon(icon,null,Modifier.size(21.dp),tint=colors.onSurfaceVariant);Text(title,style=MaterialTheme.typography.titleSmall);Text(detail,style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)}
 }
}
@Composable private fun HomeDestination(title:String,detail:String,icon:ImageVector,onClick:()->Unit) {
 val colors=MaterialTheme.colorScheme
 Row(Modifier.fillMaxWidth().clickable(onClick=onClick).heightIn(min=64.dp).padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
  Icon(icon,null,Modifier.size(21.dp),tint=colors.onSurfaceVariant)
  Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){Text(title,style=MaterialTheme.typography.titleSmall);Text(detail,style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)}
  Text("›",style=MaterialTheme.typography.titleLarge,color=colors.onSurfaceVariant)
 }
}
