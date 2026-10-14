package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.data.WindowSizePolicy

@Composable fun WindowSizeSettings(c:Config,update:(Config)->Unit) {
    val screen=LocalConfiguration.current
    Column(Modifier.fillMaxWidth().testTag("window-size-settings"),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Ancho de la ventana",style=MaterialTheme.typography.titleSmall)
        Text("Se adapta a la orientación y al espacio disponible.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(WindowSizePolicy.displayScale(c,screen.screenWidthDp,screen.screenWidthDp>screen.screenHeightDp),
            {update(c.copy(windowScale=it.coerceIn(0f,1f)))},
            Modifier.testTag("window-width-slider").semantics{contentDescription="Ancho de la ventana"},valueRange=0f..1f)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text("Compacta",style=MaterialTheme.typography.labelSmall);Text("Amplia",style=MaterialTheme.typography.labelSmall) }
    }
}
