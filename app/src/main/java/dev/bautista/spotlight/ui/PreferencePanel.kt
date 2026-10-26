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
  "appearance"->AppearanceSettings(c,update)
  "ai"->AiSettings(c,update)
  "shortcuts"->{KeyboardShortcutEditor(c,update);GlobalKeyboardShortcutOptions(c,update)}
  "suggestions"->{Text("Sugerencias",style=MaterialTheme.typography.titleMedium);Row{Text("Mostrar sugerencias",Modifier.weight(1f));Switch(c.suggestions,{update(c.copy(suggestions=it))})};Text("El inicio puede sugerir apps y acciones recientes cuando Aprendizaje está activo. Fijá u ocultá cada sugerencia desde su panel de acciones.");TextButton(onClick={update(c.copy(hiddenSuggestions=emptySet(),pinnedSuggestions=emptySet()))}){Text("Restablecer sugerencias")}}
  "permissions"->{Text("Permisos y accesos",style=MaterialTheme.typography.titleMedium);Text("Consultá y gestioná todos los accesos en el centro único de permisos.");TextButton(onClick={context.startActivity(Intent(context,dev.bautista.spotlight.MainActivity::class.java).putExtra("group","Permisos y accesos").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))}){Text("Abrir centro de permisos")}}

 }
 }
}
