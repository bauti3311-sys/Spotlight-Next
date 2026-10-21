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
        Triple("General",CommandIcons.Settings,"Todos los ajustes"),
        Triple("Apariencia",CommandIcons.Display,"Apariencia"),
        Triple("Inteligencia artificial",CommandIcons.AutoAwesome,"IA"),
        Triple("Permisos y accesos",CommandIcons.Shield,"Permisos")
    )
    Column(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("BÚSQUEDA",style=MaterialTheme.typography.labelSmall,color=colors.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
            listOf("Todo" to CommandIcons.Search,"Apps" to CommandIcons.Apps,
                "Archivos" to CommandIcons.Folder,"Acciones" to CommandIcons.Bolt,
                "IA" to CommandIcons.AutoAwesome).forEachIndexed{i,(mode,icon)->
                val label=when(mode){"Apps"->"Aplicaciones";"Acciones"->"Comandos";else->mode}
                IconButton(onClick={onFilter(mode)},modifier=Modifier.size(48.dp).semantics{
                    contentDescription=if(i==0)"Búsqueda universal" else "$label · Ctrl+$i"
                }) {Icon(icon,null,Modifier.size(18.dp),tint=colors.onSurfaceVariant)}
            }
        }
        HorizontalDivider(color=colors.onSurface.copy(alpha=.07f))
        Row(Modifier.fillMaxWidth().heightIn(min=40.dp),verticalAlignment=Alignment.CenterVertically) {
            Icon(CommandIcons.Display,null,Modifier.size(16.dp),tint=colors.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            TextButton(onClick={update(c.copy(theme=when(c.theme){"auto"->"light";"light"->"dark";else->"auto"}))}) {
                Text("Tema · "+when(c.theme){"light"->"Claro";"dark"->"Oscuro";else->"Automático"},
                    style=MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.weight(1f))
            Text("Privado",style=MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(6.dp))
            Switch(c.privateMode,{update(c.copy(privateMode=it))},
                modifier=Modifier.semantics{contentDescription="Modo privado"})
        }
        HorizontalDivider(color=colors.onSurface.copy(alpha=.07f))
        Text("AJUSTES",style=MaterialTheme.typography.labelSmall,color=colors.onSurfaceVariant)
        shortcuts.forEach{(destination,icon,label)->
            Row(Modifier.fillMaxWidth().heightIn(min=48.dp).clickable{onSettings(destination)}.padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Icon(icon,null,Modifier.size(18.dp),tint=colors.onSurfaceVariant)
                Text(label,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium)
                Icon(CommandIcons.Chevron,null,Modifier.size(14.dp),tint=colors.onSurfaceVariant)
            }
        }
    }
}
