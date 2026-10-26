package dev.bautista.spotlight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.integrations.spotify.SpotifySearchItem

/** Flat, caller-placeable result; selecting is separate from playback confirmation. */
@Composable internal fun SpotifyResultRow(
    item: SpotifySearchItem,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().heightIn(min=48.dp)
            .background(if(selected) MaterialTheme.colorScheme.onSurface.copy(alpha=.08f)
                else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(8.dp))
            .selectable(selected=selected,enabled=enabled,role=Role.RadioButton,onClick=onSelect)
            .padding(horizontal=10.dp,vertical=8.dp),
    ) {
        Text(item.name,style=MaterialTheme.typography.bodyMedium,maxLines=2,overflow=TextOverflow.Ellipsis)
        if(item.subtitle.isNotBlank()) Text(item.subtitle,style=MaterialTheme.typography.bodySmall,
            color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=2,overflow=TextOverflow.Ellipsis)
    }
}
