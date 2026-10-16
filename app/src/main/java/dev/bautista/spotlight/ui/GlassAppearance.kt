package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config

/** Product settings: technical values remain in storage, not in the main user flow. */
@Composable fun GlassAppearance(c:Config,update:(Config)->Unit) {
    var draft by remember { mutableStateOf(c) }
    var dirty by remember { mutableStateOf(false) }
    LaunchedEffect(c) { if(!dirty)draft=c }
    fun edit(next:Config){draft=next;dirty=true}
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        Text("Apariencia",style=MaterialTheme.typography.headlineSmall)
        Text("Una ventana a tu medida",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("auto" to "Sistema","light" to "Claro","dark" to "Oscuro").forEach { (key,label)->
            FilterChip(draft.theme==key,{edit(draft.copy(theme=key))},label={Text(label)})
        } }
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf(Triple("glass","dark","Obsidian Glass"),Triple("raycast","dark","Graphite"),Triple("glass","light","macOS Light")).forEach { (preset,theme,label)->
                FilterChip(draft.preset==preset && draft.theme==theme,{edit(draft.copy(preset=preset,theme=theme))},label={Text(label)})
            }
        }
        WindowSizeSettings(draft){edit(it)}
        Text("Acento",style=MaterialTheme.typography.titleSmall)
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(4.dp)) { listOf("blue" to "Azul","purple" to "Lila","rose" to "Rosa","green" to "Verde","graphite" to "Neutro").forEach { (key,label)->
            FilterChip(draft.accent==key,{edit(draft.copy(accent=key))},label={Text(label)})
        } }
        OverlayDensitySettings(draft,simple=true){edit(it)}
        OverlayAppearancePreview(draft,simple=true)
        Row(Modifier.fillMaxWidth()) { Column(Modifier.weight(1f)) {
            Text("Reducir movimiento",style=MaterialTheme.typography.titleSmall)
            Text("También respeta la preferencia del sistema.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        };Switch(!draft.animations,{edit(draft.copy(animations=!it))}) }
        Text("La altura se adapta al contenido, al teclado y al espacio disponible.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { Button(onClick={update(AppearanceEdits.merge(c,draft));dirty=false}){Text("Aplicar apariencia")};TextButton(onClick={draft=c;dirty=false}){Text("Descartar cambios")} }
        ConfirmedSettingsButton("Restablecer apariencia","Restablece tema, acento, densidad, movimiento y ancho. Tus datos y otras preferencias se conservan.") { edit(AppearanceEdits.merge(c,Config(windowScale=.5f))) }
    }
}
