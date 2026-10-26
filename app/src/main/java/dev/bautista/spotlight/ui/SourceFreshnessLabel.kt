package dev.bautista.spotlight.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import dev.bautista.spotlight.next.SourceFreshness
import java.text.DateFormat
import java.util.Date

@Composable internal fun SourceFreshnessLabel(value:SourceFreshness) {
    val stamp=value.knownTimestamp(System.currentTimeMillis())?.let{DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(Date(it))}
    Text("${value.provider} · ${value.status} · ${stamp ?: "Hora de consulta no verificable"}",
        style=MaterialTheme.typography.labelSmall,color=if(value.stale)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
}
