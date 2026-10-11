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

@Composable fun AppearanceSettings(config: Config, update: (Config) -> Unit) {
    Text("Diseños preparados",style=MaterialTheme.typography.labelLarge)
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){AppearancePresets.names.forEach{name->AssistChip(onClick={update(AppearancePresets.apply(name,config))},label={Text(name)})}}
    Surface(shape=androidx.compose.foundation.shape.RoundedCornerShape(config.corner.dp),color=MaterialTheme.colorScheme.surfaceVariant){Column(Modifier.fillMaxWidth().padding(16.dp)){Text("Buscar en Spotlight",style=MaterialTheme.typography.titleMedium);HorizontalDivider(Modifier.padding(vertical=10.dp));Text("Aplicaciones y acciones",color=MaterialTheme.colorScheme.primary);Text("Vista previa del diseño",style=MaterialTheme.typography.bodySmall)}}
    Text("Estilo", style=MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        listOf("spotlight" to "Spotlight", "raycast" to "Raycast").forEach { (id, label) ->
            FilterChip(config.appearance==id, {update(config.copy(appearance=id))}, label={Text(label)})
        }
    }
    Text("Acento", style=MaterialTheme.typography.labelLarge)
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        listOf("blue" to "Océano", "purple" to "Lavanda", "rose" to "Rosa", "green" to "Bosque", "orange" to "Ámbar").forEach { (id,label) ->
            FilterChip(config.accent==id,{update(config.copy(accent=id))},label={Text(label)})
        }
    }
    Text("Densidad", style=MaterialTheme.typography.labelLarge)
    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        listOf("auto" to "Automática", "compact" to "Compacta", "balanced" to "Equilibrada", "spacious" to "Espaciosa").forEach { (id,label) ->
            FilterChip(config.density==id,{update(config.copy(density=id))},label={Text(label)})
        }
    }
    Text("Esquinas · ${config.corner} dp");Slider(config.corner.toFloat(),{update(config.copy(corner=it.toInt()))},valueRange=12f..32f)
    Text("Texto · ${(config.textScale*100).toInt()} %");Slider(config.textScale,{update(config.copy(textScale=it))},valueRange=.9f..1.2f)
    Text("Iconos · ${config.iconSize} dp");Slider(config.iconSize.toFloat(),{update(config.copy(iconSize=it.toInt()))},valueRange=24f..36f)
    Text("Altura máxima · ${config.maxHeight} dp");Slider(config.maxHeight.toFloat(),{update(config.copy(maxHeight=it.toInt()))},valueRange=320f..640f)
    Row{Text("Adaptar a teléfono y tablet",Modifier.weight(1f));Switch(config.adaptive,{update(config.copy(adaptive=it))})}
    Row { Text("Material translúcido",Modifier.weight(1f));Switch(config.blur,{update(config.copy(blur=it))}) }
    Text("La superposición usa un material translúcido. El desenfoque de pantalla se reemplaza por un degradado suave para evitar un rectángulo detrás de la barra.",style=MaterialTheme.typography.bodySmall)
}

@Composable fun IntegrationSettings(config: Config, update: (Config) -> Unit) {
    val context=LocalContext.current
    var apps by remember { mutableStateOf(emptyList<Pair<String,List<Capability>>>()) }
    LaunchedEffect(Unit) {
        apps=withContext(Dispatchers.IO) {
            val registry=IntegrationRegistry(context)
            context.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)
                .distinctBy { it.activityInfo.packageName }.sortedBy { it.loadLabel(context.packageManager).toString().lowercase() }
                .map { it.loadLabel(context.packageManager).toString() to registry.capabilities(it.activityInfo.packageName) }
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
