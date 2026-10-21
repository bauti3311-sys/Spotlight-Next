package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.search.Result

/** Presentation only: public intents, favorites and media retain their existing executors. */
@Composable internal fun ApplicationInspector(
    item: Result,
    packageName: String?,
    favorite: Boolean,
    playing: Boolean,
    mediaCommands: List<String>,
    onOpen: () -> Unit,
    onFavorite: () -> Unit,
    onActions: () -> Unit,
    onMedia: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ResultIcon(item, Modifier.size(40.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, Modifier.testTag("inspector-title"), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if(playing)"En reproducción" else "Aplicación instalada",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Button(onClick = onOpen, shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("inspector-primary-action")) {
            Icon(CommandIcons.Chevron, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Abrir aplicación")
        }
        TextButton(onClick = onFavorite, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Icon(if(favorite)CommandIcons.Star else CommandIcons.StarBorder, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if(favorite)"Quitar favorita" else "Añadir favorita")
        }
        mediaCommands.forEach { command ->
            TextButton(onClick = { onMedia(command) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Icon(CommandIcons.Bolt, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(when(command){"pause"->"Pausar reproducción";"play"->"Reproducir";"next"->"Siguiente canción";else->"Canción anterior"})
            }
        }
        TextButton(onClick = onActions, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("Ver acciones contextuales")
            Spacer(Modifier.weight(1f))
            Text("Ctrl+K", style = MaterialTheme.typography.labelSmall)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        QuickLookMetadata(item)
        packageName?.let { pkg -> SelectionContainer {
            Text(pkg, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } }
    }
}
