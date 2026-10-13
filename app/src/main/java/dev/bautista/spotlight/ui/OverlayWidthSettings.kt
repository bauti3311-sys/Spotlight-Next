package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.data.OverlayWidthPolicy

/** Same width rules in the live invocation settings and the appearance draft. */
@Composable fun OverlayWidthSettings(c:Config,update:(Config)->Unit) {
    Surface(Modifier.fillMaxWidth().testTag("width-settings"),shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("Ancho de la ventana",style=MaterialTheme.typography.titleSmall)
            if(c.landscapeWidth>0)Text("Horizontal: ${c.landscapeWidth} dp · ancho específico",color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Ancho general automático")
                    Text(if(c.width<=0)"Se adapta al espacio disponible" else "Manual: ${c.width} dp",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(c.width<=0,{update(OverlayWidthPolicy.automatic(c,it))},Modifier.semantics{contentDescription="Ancho general automático"})
            }
            if(c.width>0) {
                Text("Ancho general: ${c.width} dp",style=MaterialTheme.typography.bodySmall)
                Slider(c.width.toFloat(),{update(OverlayWidthPolicy.manual(c,it.toInt()))},Modifier.testTag("width-general-slider"),valueRange=380f..1100f)
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Text("Ancho horizontal específico",Modifier.weight(1f))
                Switch(c.landscapeWidth>0,{update(OverlayWidthPolicy.horizontal(c,it))},Modifier.semantics{contentDescription="Ancho horizontal específico"})
            }
            if(c.landscapeWidth>0) {
                Text("Reemplaza al ancho general solo en horizontal.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(c.landscapeWidth.toFloat(),{update(c.copy(landscapeWidth=it.toInt()))},Modifier.testTag("width-horizontal-slider"),valueRange=380f..1100f)
                TextButton(onClick={update(OverlayWidthPolicy.horizontal(c,false))}){Text("Restablecer ancho horizontal")}
            } else {
                Text(if(c.width<=0)"Horizontal: automático general" else "Horizontal: ${c.width} dp · ancho general",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("El ancho se limita al espacio disponible en cada orientación.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
