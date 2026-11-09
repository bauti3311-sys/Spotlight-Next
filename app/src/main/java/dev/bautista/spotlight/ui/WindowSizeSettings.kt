package dev.bautista.spotlight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.data.WindowSizePolicy
import dev.bautista.spotlight.overlay.AdaptiveLayout

@Composable fun WindowSizeSettings(c:Config,update:(Config)->Unit) {
    val screen=LocalConfiguration.current
    val bounds=AdaptiveLayout.bounds(screen.screenWidthDp,screen.screenHeightDp,1f,0,c)
    val scale=WindowSizePolicy.displayScale(c,screen.screenWidthDp,screen.screenWidthDp>screen.screenHeightDp)
    Column(Modifier.fillMaxWidth().testTag("window-size-settings"),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Ancho del buscador",style=MaterialTheme.typography.titleMedium)
        Text(if(c.windowScale<0f)"Tamaño anterior conservado" else "${bounds.width} dp · estimado en esta pantalla",modifier=Modifier.testTag("window-width-value"),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.primary)
        GlassSlider(scale,{update(c.copy(windowScale=it.coerceIn(0f,1f)))},
            Modifier.testTag("window-width-slider").semantics{contentDescription="Ancho de la ventana"},valueRange=0f..1f)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {Text("Compacta",style=MaterialTheme.typography.labelMedium);Text("Amplia",style=MaterialTheme.typography.labelMedium)}
        Text("Se aplica al instante. Respeta los bordes de la pantalla; la altura se adapta al contenido y al teclado.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        if(screen.screenWidthDp<=350)Text("En una ventana tan estrecha, el margen de ajuste es pequeño para conservar la legibilidad.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        if(c.windowScale>=0f)TextButton(onClick={update(c.copy(windowScale=-1f))}){Text("Restaurar ancho anterior")}
        HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.5f))
        Text("Resultados visibles",style=MaterialTheme.typography.titleSmall)
        SettingsOptions {
            FilterChip(!c.compactSearchResults,{update(c.copy(compactSearchResults=false))},label={Text("Adaptativa")})
            FilterChip(c.compactSearchResults,{update(c.copy(compactSearchResults=true))},label={Text("Menos resultados")})
        }
    }
}
