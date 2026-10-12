package dev.bautista.spotlight.ui

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.*

@Composable fun AppearanceSettings(config:Config,update:(Config)->Unit){GlassAppearance(config,update)}

@Composable fun IntegrationSettings(config: Config, update: (Config) -> Unit) {
    val context=LocalContext.current
    var apps by remember { mutableStateOf(emptyList<Pair<String,List<Capability>>>()) }
    LaunchedEffect(Unit) {
        apps=withContext(Dispatchers.IO) {
            val registry=IntegrationRegistry(context)
            context.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)
                .distinctBy { it.activityInfo.packageName }.sortedBy { it.loadLabel(context.packageManager).toString().lowercase() }
                .map { it.loadLabel(context.packageManager).toString() to registry.capabilities(it.activityInfo.packageName) }.filter{it.second.any{cap->cap.title!="Abrir" && cap.state==CapabilityState.AVAILABLE}}
        }
    }
    Text("Las apps y sus actividades públicas se detectan automáticamente. No necesitás pegar enlaces.")
    Text("Mensajería predeterminada",style=MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        listOf("whatsapp" to "WhatsApp","whatsapp_business" to "WhatsApp Business","sms" to "SMS","email" to "Correo").forEach { (id,label) ->
            if(id !in setOf("whatsapp","whatsapp_business") || context.packageManager.getLaunchIntentForPackage(if(id=="whatsapp_business")"com.whatsapp.w4b" else "com.whatsapp")!=null)
                FilterChip(config.messageApp==id,{update(config.copy(messageApp=id))},label={Text(label)})
        }
    }
    Text("Los mensajes nuevos se preparan en la app receptora. Las respuestas directas requieren una notificación con RemoteInput y confirmación.",style=MaterialTheme.typography.bodySmall)
    var expanded by remember { mutableStateOf(false) }
    TextButton(onClick={expanded=!expanded}) { Text(if(expanded)"Ocultar capacidades" else "Ver capacidades · ${apps.size} apps") }
    if(expanded) apps.forEach { (name,capabilities) ->
        Text(name,style=MaterialTheme.typography.titleSmall)
        capabilities.forEach { c -> Text("${c.title} · ${if(c.state==CapabilityState.AVAILABLE) "Disponible" else if(c.state==CapabilityState.PERMISSION_REQUIRED) "Permiso pendiente" else "No compatible"}\n${c.explanation}",style=MaterialTheme.typography.bodySmall) }
        HorizontalDivider(Modifier.padding(vertical=8.dp))
    }
}
