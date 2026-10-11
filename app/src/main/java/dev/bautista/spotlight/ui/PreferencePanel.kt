package dev.bautista.spotlight.ui
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config
@Composable fun PreferencePanel(c:Config,page:String,update:(Config)->Unit){
 val context=LocalContext.current
 Column(Modifier.fillMaxWidth().heightIn(max=340.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
 when(page){
  "appearance"->{Text("Apariencia",style=MaterialTheme.typography.titleMedium);Row{listOf("auto" to "Automático","light" to "Claro","dark" to "Oscuro").forEach{(value,label)->FilterChip(c.theme==value,{update(c.copy(theme=value))},label={Text(label)})}};Text("Transparencia · ${(c.opacity*100).toInt()} %");Slider(c.opacity,{update(c.copy(opacity=it))},valueRange=.8f..1f);AppearanceSettings(c,update)}
  "ai"->AiSettings(c,update)
  "shortcuts"->{Text("Atajo global",style=MaterialTheme.typography.titleMedium);Text("Requiere el servicio optativo de teclado de Accesibilidad.");listOf("ctrl_alt" to "Ctrl+Alt+Espacio","alt" to "Alt+Espacio","ctrl" to "Ctrl+Espacio").forEach{(value,label)->FilterChip(c.shortcut==value,{update(c.copy(shortcut=value))},label={Text(label)})};Text("Portapapeles dentro de la barra");listOf("ctrl_shift_v" to "Ctrl+Shift+V","ctrl_p" to "Ctrl+P","off" to "Sin atajo").forEach{(value,label)->FilterChip(c.clipboardShortcut==value,{update(c.copy(clipboardShortcut=value))},label={Text(label)})}}
  "suggestions"->{Text("Sugerencias",style=MaterialTheme.typography.titleMedium);Row{Text("Mostrar sugerencias",Modifier.weight(1f));Switch(c.suggestions,{update(c.copy(suggestions=it))})};Text("El inicio puede sugerir apps y acciones recientes cuando Aprendizaje está activo. Fijá u ocultá cada sugerencia desde su panel de acciones.");TextButton(onClick={update(c.copy(hiddenSuggestions=emptySet(),pinnedSuggestions=emptySet()))}){Text("Restablecer sugerencias")}}
  "permissions"->{Text("Permisos opcionales",style=MaterialTheme.typography.titleMedium);Text("Cada acceso habilita una función concreta. Podés usar Spotlight sin ellos.");listOf("contacts" to "Contactos: elegir destinatarios","files" to "Archivos: búsqueda local","calendar" to "Calendario: consultar agenda","notifications" to "Notificaciones: mensajes actuales").forEach{(access,title)->TextButton(onClick={val intent=when(access){"notifications"->Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);"files"->if(Build.VERSION.SDK_INT>=30)Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,Uri.parse("package:${context.packageName}")) else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:${context.packageName}"));else->Intent(context,dev.bautista.spotlight.PermissionActivity::class.java).putExtra("access",access)};runCatching{context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}.onFailure{android.widget.Toast.makeText(context,"No se pudo abrir este ajuste en el dispositivo",android.widget.Toast.LENGTH_SHORT).show()}}){Text(title)}}}
 }
 }
}
