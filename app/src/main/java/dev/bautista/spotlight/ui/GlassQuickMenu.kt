package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config

/** Overlay-safe Raycast-like command menu: no popups, only the existing window. */
@Composable fun GlassQuickMenu(
    c:Config,
    update:(Config)->Unit,
    onSettings:(String)->Unit,
    onFilter:(String)->Unit
) {
    val colors=MaterialTheme.colorScheme
    val shortcuts=listOf(
        Triple("General",CommandIcons.Settings,"Ajustes"),
        Triple("Apariencia",CommandIcons.Display,"Apariencia"),
        Triple("Permisos y accesos",CommandIcons.Shield,"Permisos"),
        Triple("Atajos",CommandIcons.Keyboard,"Atajos")
    )
    Column(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=8.dp)) {
        shortcuts.forEach{(destination,icon,label)->
            Row(Modifier.fillMaxWidth().heightIn(min=48.dp).clickable{onSettings(destination)}.padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Icon(icon,null,Modifier.size(18.dp),tint=colors.onSurfaceVariant)
                Text(label,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium)
                Icon(CommandIcons.Chevron,null,Modifier.size(14.dp),tint=colors.onSurfaceVariant)
            }
        }
    }
}
