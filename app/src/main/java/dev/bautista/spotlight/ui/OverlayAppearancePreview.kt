package dev.bautista.spotlight.ui

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.data.OverlayWidthPolicy
import dev.bautista.spotlight.overlay.AdaptiveLayout
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result

@Composable
fun OverlayDensitySettings(c: Config, simple:Boolean=false, update: (Config) -> Unit) {
    Surface(Modifier.fillMaxWidth().testTag("density-settings"), shape=RoundedCornerShape(16.dp), color=MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("Espaciado de resultados", style=MaterialTheme.typography.titleSmall)
            if(!simple) Text("La densidad cambia el espacio dentro de las filas. No cambia el ancho de la ventana.", style=MaterialTheme.typography.bodySmall)
            if(c.density=="auto" && !simple) Text("Actual: automático según el tamaño de pantalla.", style=MaterialTheme.typography.bodySmall)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf("compact" to "Compacta", "balanced" to if(simple)"Normal" else "Equilibrada", "spacious" to "Espaciosa").forEach { (key, label) ->
                    FilterChip(c.density==key || (simple && key=="balanced" && c.density=="auto"), { update(c.copy(density=key)) }, label={ Text(label) })
                }
            }
        }
    }
}

/** Uses the same geometry and result renderer as the overlay; never launches sample actions. */
@Composable
fun OverlayAppearancePreview(c: Config, simple:Boolean=false) {
    val screen=LocalConfiguration.current
    val landscape=screen.screenWidthDp>screen.screenHeightDp
    // Inputs are dp with density 1, so the geometry's returned width is also dp.
    val width=AdaptiveLayout.bounds(screen.screenWidthDp, screen.screenHeightDp, 1f, 0, c).width
    val requested=OverlayWidthPolicy.configured(c, landscape)
    val source=if(landscape && c.landscapeWidth>0) "horizontal específico" else if(requested<=0) "automático general" else "manual general"
    var selected by remember { mutableStateOf(0) }
    val examples=remember {
        listOf(Result("preview:calculator", "2 + 2 = 4", "Calculadora · resultado local", "Acciones", 0, Action.Copy("4")),
            Result("preview:note", "Apuntes", "Guardados · texto de ejemplo", "Notas", 0, Action.Copy("Apuntes")))
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Vista previa · ${if(landscape) "horizontal" else "vertical"}", style=MaterialTheme.typography.titleSmall)
        if(!simple) Text("Ancho visible: $width dp · $source", style=MaterialTheme.typography.bodySmall, modifier=Modifier.testTag("preview-width-label"))
        if(!simple && requested>width) Text("Configurado: $requested dp; limitado al espacio de esta pantalla.", style=MaterialTheme.typography.bodySmall)
        if(!simple) Text("Tamaño real · deslizá horizontalmente si no cabe en Ajustes. Tocá una fila para probar la selección; no ejecuta acciones.", style=MaterialTheme.typography.bodySmall)
        BoxWithConstraints(Modifier.fillMaxWidth().testTag("appearance-preview-viewport")) {
            val displayWidth=if(simple)minOf(maxWidth,width.dp) else width.dp
            val canvasWidth=if(simple)maxWidth else maxOf(maxWidth, width.dp)
            Box(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Box(Modifier.width(canvasWidth), contentAlignment=Alignment.TopCenter) {
                    SpotlightTheme(c.theme, c) {
                        Column(Modifier.width(displayWidth).testTag("appearance-preview-window").heightIn(max=240.dp).glassSurface(c).verticalScroll(rememberScrollState()).padding(16.dp)) {
                            Text("Buscar en Spotlight", style=MaterialTheme.typography.titleMedium, modifier=Modifier.padding(bottom=8.dp))
                            HorizontalDivider()
                            examples.forEachIndexed { index, result ->
                                ResultRow(result, selected==index, false, { selected=index }, { selected=index }, {}, animations=c.animations)
                            }
                        }
                    }
                }
            }
        }
    }
}
