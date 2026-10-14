package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config

/** Product settings: technical values remain in storage, not in the main user flow. */
@Composable fun GlassAppearance(c:Config,update:(Config)->Unit) {
    var draft by remember(c){mutableStateOf(c)}
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        Text("Apariencia",style=MaterialTheme.typography.headlineSmall)
        Text("Una ventana a tu medida",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("auto" to "Sistema","light" to "Claro","dark" to "Oscuro").forEach { (key,label)->
            FilterChip(draft.theme==key,{draft=draft.copy(theme=key)},label={Text(label)})
        } }
        WindowSizeSettings(draft){draft=it}
        Text("Acento",style=MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) { listOf("blue" to "Azul","purple" to "Lila","rose" to "Rosa","graphite" to "Grafito").forEach { (key,label)->
            FilterChip(draft.accent==key,{draft=draft.copy(accent=key)},label={Text(label)})
        } }
        OverlayDensitySettings(draft,simple=true){draft=it}
        OverlayAppearancePreview(draft,simple=true)
        Row(Modifier.fillMaxWidth()) { Column(Modifier.weight(1f)) {
            Text("Reducir movimiento",style=MaterialTheme.typography.titleSmall)
            Text("También respeta la preferencia del sistema.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        };Switch(!draft.animations,{draft=draft.copy(animations=!it)}) }
        Text("La altura se adapta al contenido, al teclado y al espacio disponible.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { Button(onClick={update(draft)}){Text("Aplicar apariencia")};TextButton(onClick={draft=c}){Text("Descartar cambios")} }
    }
}
