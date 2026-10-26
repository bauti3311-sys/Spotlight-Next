package dev.bautista.spotlight.ui

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.*
import org.json.JSONObject

@Composable fun PrivacySettings(app:SpotlightApp,c:Config,update:(Config)->Unit) {
 val scope=rememberCoroutineScope()
 Row{Text("Modo privado",Modifier.weight(1f));Switch(c.privateMode,{update(c.copy(privateMode=it))})}
 Text("Suspende capturas, aprendizaje y recuperación durable de esta ventana. Descarta su recuperación temporal; los guardados explícitos se conservan. Las consultas manuales de IA se revisan antes de enviarse.",style=MaterialTheme.typography.bodySmall)
 Row{Text("Ofrecer contexto del portapapeles al abrir",Modifier.weight(1f));Switch(c.captureContext,{update(c.copy(captureContext=it))},enabled=!c.privateMode)}
 Text("Solo con Spotlight enfocado. El contenido marcado sensible no se captura.",style=MaterialTheme.typography.bodySmall)
 val data=runCatching{JSONObject(c.usageData)}.getOrDefault(JSONObject())
 Text("Hábitos: ${data.length()} resultados · ${c.recent.size} apps recientes · ${c.actionRecent.size} acciones recientes")
 var show by remember{mutableStateOf(false)}
 TextButton(onClick={show=!show}){Text(if(show)"Ocultar hábitos" else "Ver hábitos aprendidos")}
 if(show)data.keys().asSequence().take(30).forEach{key->Text("${key.take(100)} · ${data.optJSONObject(key)?.optInt("count",0) ?: 0} usos",style=MaterialTheme.typography.bodySmall)}
 var clear by remember{mutableStateOf(false)}
 TextButton(onClick={clear=true}){Text("Borrar historial y restablecer aprendizaje")}
 if(clear){Text("¿Borrar recientes y hábitos? Tus preferencias y elementos guardados se conservan.");Row{TextButton(onClick={clear=false;scope.launch{app.preferences.clearHistory()}}){Text("Confirmar borrado")};TextButton(onClick={clear=false}){Text("Cancelar")}}}
 ConfirmedSettingsButton("Vaciar portapapeles temporal","Se borrará únicamente el historial temporal de Spotlight."){app.local.clearClips()}
 TextButton(onClick={app.startActivity(Intent(app,dev.bautista.spotlight.InvokeActivity::class.java).putExtra("query","contextos").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}){Text("Gestionar contextos y conversaciones")}
}
@Composable fun InvocationSettings(c:Config,update:(Config)->Unit) {
 Text("Teclado virtual",style=MaterialTheme.typography.titleSmall)
 Row(Modifier.horizontalScroll(rememberScrollState())){listOf("adaptive" to "Según invocación","always" to "Automático","never" to "Manual").forEach{(key,label)->FilterChip(c.autoKeyboard==key,{update(c.copy(autoKeyboard=key))},label={Text(label)})}}
 Text("La invocación táctil muestra primero la barra. El atajo físico enfoca el campo sin esperar al IME.",style=MaterialTheme.typography.bodySmall)
 Text("Posición horizontal");GlassSlider((if(c.landscapePosition<0)c.position else c.landscapePosition).coerceIn(.04f,.35f),{update(c.copy(landscapePosition=it))},valueRange=.04f..0.35f)
 TextButton(onClick={update(c.copy(landscapePosition=-1f))}){Text("Usar la posición general en ambas orientaciones")}
 Text("Gesto lateral compatible",style=MaterialTheme.typography.titleSmall)
 Text("Para no competir con Volver, elegí «Spotlight · Buscar» como aplicación de un gesto de One Hand Operation+, si su selector permite elegir actividades. Allí configurás lado, zona, sensibilidad y apps excluidas; Spotlight no agrega una burbuja ni una capa invisible permanente.",style=MaterialTheme.typography.bodySmall)
 val context=LocalContext.current
 TextButton(onClick={val launch=context.packageManager.getLaunchIntentForPackage("com.samsung.android.sidegesturepad");runCatching{context.startActivity(launch ?: Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://galaxystore.samsung.com/detail/com.samsung.android.sidegesturepad")))}}){Text("Abrir One Hand Operation+")}
}
@Composable fun SearchTuning(c:Config,update:(Config)->Unit) {
 val priorities=runCatching{JSONObject(c.sourcePriority)}.getOrDefault(JSONObject())
 Text("Prioridades de fuentes",style=MaterialTheme.typography.titleSmall);Text("Ajustes moderados: las coincidencias exactas conservan prioridad.",style=MaterialTheme.typography.bodySmall)
 listOf("Aplicaciones","Archivos","Contactos","Comandos","IA").forEach{source->Text("$source · ${priorities.optInt(source,0)}");GlassSlider(priorities.optInt(source,0).toFloat(),{value->val next=JSONObject(priorities.toString());next.put(source,value.toInt().coerceIn(-150,150));update(c.copy(sourcePriority=next.toString()))},valueRange=-150f..150f)}
}
@Composable fun DiagnosticsPanel(app:SpotlightApp) {
 val state by app.shizuku.state.collectAsState();val writeCapability by app.shizuku.writeCapability.collectAsState();val notifications by dev.bautista.spotlight.control.Replies.connected.collectAsState();val openings by app.diagnostics.openings.collectAsState();val index by app.index.status.collectAsState()
 fun allowed(permission:String)=ContextCompat.checkSelfPermission(app,permission)==PackageManager.PERMISSION_GRANTED
 Text("Diagnóstico local",style=MaterialTheme.typography.titleMedium)
 Text("Superposición: ${if(Settings.canDrawOverlays(app))"permitida" else "pendiente"}")
 val services=Settings.Secure.getString(app.contentResolver,Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
 Text("Atajo accesible: ${if(services.contains(app.packageName))"habilitado" else "opcional, no habilitado"}")
 Text("Shizuku: ${state.status.label} · escritura ${if(writeCapability==true)"autorizada" else "pendiente"}")
 Text("Contactos: ${allowed(Manifest.permission.READ_CONTACTS)} · Calendario: ${allowed(Manifest.permission.READ_CALENDAR)}")
 Text("Notificaciones: ${notifications}")
 Text("Índice: ${index.count} archivos · ${if(index.busy)"actualizando" else "en reposo"}")
 Text("Muestras de apertura: ${openings.size}");Text(app.diagnostics.report(),style=MaterialTheme.typography.bodySmall)
 Text("Estados del dispositivo actual. Los permisos pueden cambiar desde los ajustes de Android.",style=MaterialTheme.typography.bodySmall)
 val context=LocalContext.current
 TextButton(onClick={context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,app.diagnostics.report()),"Exportar diagnóstico"))}){Text("Exportar diagnóstico sin contenido privado")}
 TextButton(onClick={app.diagnostics.clear()}){Text("Limpiar mediciones")}
}
