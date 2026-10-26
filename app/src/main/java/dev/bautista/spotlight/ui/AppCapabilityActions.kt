package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.*

/** Shared contextual actions; transport availability is rechecked at selection time. */
@Composable internal fun AppCapabilityActions(packageName:String,onAction:(Action)->Unit) =
    AppCapabilityActions(packageName,appInfoAlreadyVisible=false,onAction=onAction)

@Composable internal fun AppCapabilityActions(packageName:String,appInfoAlreadyVisible:Boolean,onAction:(Action)->Unit) {
    val context=LocalContext.current;val scope=rememberCoroutineScope()
    val registry=remember(context){AppCapabilities(context)}
    var capabilities by remember(packageName){mutableStateOf(emptyList<AppCapability>())}
    var error by remember(packageName){mutableStateOf("")}
    var busy by remember(packageName){mutableStateOf(false)}
    val catalog=remember(packageName,appInfoAlreadyVisible){ActionCatalog.applicationActions(packageName).filterNot {
        appInfoAlreadyVisible && (it.action as? Action.Launch)?.intent?.action==android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    }}
    LaunchedEffect(packageName){capabilities=withContext(Dispatchers.IO){runCatching{registry.available(packageName)}.getOrDefault(emptyList())}}
    Column(Modifier.fillMaxWidth().testTag("app-capabilities")) {
        HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant,modifier=Modifier.padding(bottom=12.dp))
        Text("Acciones de app",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant,
            modifier=Modifier.padding(bottom=4.dp))
        catalog.forEach { target ->
            TextButton(enabled=!busy,onClick={onAction(target.action)},
                colors=ButtonDefaults.textButtonColors(contentColor=MaterialTheme.colorScheme.onSurface),
                contentPadding=PaddingValues(horizontal=2.dp,vertical=8.dp),
                modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("app-action-${target.id}")) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    ResultIcon(target,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {
                        Text(target.title,style=MaterialTheme.typography.bodySmall)
                        Text(target.subtitle,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        val extra=capabilities.filterNot { capability ->
            capability.id=="search" && catalog.any{it.action is Action.ComposeSearch} ||
                capability.id=="share-text" && catalog.any{it.action is Action.ComposeMessage}
        }
        extra.forEach { capability ->
            TextButton(enabled=!busy,onClick={busy=true;scope.launch {
                try{val action=withContext(Dispatchers.IO){registry.prepare(packageName,capability.id)};error="";onAction(action)}
                catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo revisar la capacidad."}finally{busy=false}
            }},colors=ButtonDefaults.textButtonColors(contentColor=MaterialTheme.colorScheme.onSurface),
                contentPadding=PaddingValues(horizontal=2.dp,vertical=8.dp),
                modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("app-capability-${capability.id}")) {
                Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(3.dp)) {
                    Text(capability.title,style=MaterialTheme.typography.bodySmall)
                    Text(capability.expectedResult,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if(catalog.isEmpty() && extra.isEmpty())Text("La app no publica acciones adicionales compatibles.",style=MaterialTheme.typography.bodySmall)
        if(error.isNotBlank())Text(error,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.error)
    }
}

