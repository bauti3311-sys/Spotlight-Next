package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.*

/** Shared contextual actions; transport availability is rechecked at selection time. */
@Composable internal fun AppCapabilityActions(packageName:String,onAction:(Action)->Unit) {
    val context=LocalContext.current;val scope=rememberCoroutineScope()
    val registry=remember(context){AppCapabilities(context)}
    var capabilities by remember(packageName){mutableStateOf(emptyList<AppCapability>())}
    var error by remember(packageName){mutableStateOf("")}
    var busy by remember(packageName){mutableStateOf(false)}
    LaunchedEffect(packageName){capabilities=withContext(Dispatchers.IO){runCatching{registry.available(packageName)}.getOrDefault(emptyList())}}
    if(capabilities.isNotEmpty())Column(Modifier.fillMaxWidth().testTag("app-capabilities")) {
        capabilities.forEach { capability ->
            TextButton(enabled=!busy,onClick={busy=true;scope.launch {
                try{val action=withContext(Dispatchers.IO){registry.prepare(packageName,capability.id)};error="";onAction(action)}
                catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo revisar la capacidad."}finally{busy=false}
            }},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("app-capability-${capability.id}")) {
                Column(Modifier.fillMaxWidth()){Text(capability.title);Text(capability.expectedResult,style=MaterialTheme.typography.labelSmall)}
            }
        }
        if(error.isNotBlank())Text(error,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.error)
    }
}
