package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.control.Replies
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.CancellationException

/** Uses the same live prerequisites that gate local plans and AI tools. No permission requests. */
@Composable fun CapabilitySettings(app:SpotlightApp,c:Config,revision:Int) {
 val shizuku by app.shizuku.state.collectAsState()
 val write by app.shizuku.writeCapability.collectAsState()
 val notifications by Replies.connected.collectAsState()
 var refresh by remember{mutableIntStateOf(0)}
 var capabilities by remember{mutableStateOf<List<ToolCapability>?>(null)}
 var error by remember{mutableStateOf(false)}
 LaunchedEffect(c,revision,shizuku,write,notifications,refresh) {
  capabilities=null;error=false
  try{capabilities=ToolExecutor(app).capabilities(c)}
  catch(e:CancellationException){throw e}
  catch(e:Exception){error=true}
 }
 Text("Estas herramientas se comprueban de nuevo antes de cada ejecución. Estar disponible no autoriza enviar datos a Gemini ni ejecutar cambios sin confirmación.",style=MaterialTheme.typography.bodySmall)
 when {
  error->Text("No se pudieron consultar los requisitos. No se asume que estén disponibles.",color=MaterialTheme.colorScheme.error)
  capabilities==null->Text("Comprobando capacidades locales…",style=MaterialTheme.typography.bodySmall)
  else->capabilities.orEmpty().forEach{capability->
   Column(Modifier.fillMaxWidth().padding(vertical=6.dp),verticalArrangement=Arrangement.spacedBy(3.dp)) {
    Text(capability.definition.title,style=MaterialTheme.typography.bodyMedium)
    Text("${capability.status.label} · ${capability.reason}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
   }
  }
 }
 TextButton(onClick={refresh++}){Text("Volver a comprobar capacidades")}
}
