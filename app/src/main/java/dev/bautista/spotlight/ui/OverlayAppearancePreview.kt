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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.data.OverlayWidthPolicy
import dev.bautista.spotlight.overlay.AdaptiveLayout
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import kotlin.math.roundToInt

@Composable
fun OverlayDensitySettings(c: Config, simple:Boolean=false, update: (Config) -> Unit) {
    val textScale=GlassTokens.uiTextScale(c)
    val densityScale=GlassTokens.densityScale(c)
    val isDefault=textScale==1f && densityScale==1f && (c.uiDensityScale==1f || (c.uiDensityScale<0f && c.density in setOf("balanced","auto")))
    Surface(Modifier.fillMaxWidth().testTag("density-settings"), shape=RoundedCornerShape(16.dp), color=MaterialTheme.colorScheme.onSurface.copy(alpha=.025f)) {
        Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Escala y densidad", style=MaterialTheme.typography.titleSmall)
                    Text("Ajuste independiente de 85 % a 115 % en pasos de 5 %. No altera el ancho de la ventana.", style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(
                    onClick={ update(c.copy(textScale=1f, uiDensityScale=1f, density="balanced")) },
                    enabled=!isDefault,
                    modifier=Modifier.testTag("reset-scale-density").heightIn(min=GlassTokens.hit)
                ) {
                    Text("Restablecer valores")
                }
            }
            if(c.density=="auto" && c.uiDensityScale<0f && !simple) {
                Text("Actual: automático según el tamaño de pantalla.", style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Escala del texto", style=MaterialTheme.typography.titleSmall)
                    Text("Afecta barra, resultados, subtítulos, inspector y acciones.", style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${(textScale*100).roundToInt()}%", Modifier.testTag("text-scale-value"), style=MaterialTheme.typography.labelLarge)
            }
            GlassSlider(
                value=textScale,
                onValueChange={ val snapped=GlassTokens.snapScale(it); update(c.copy(textScale=snapped)) },
                modifier=Modifier.testTag("text-scale-slider").semantics { contentDescription="Escala del texto" },
                valueRange=GlassTokens.minScale..GlassTokens.maxScale,
                steps=5,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                Text("85 %", style=MaterialTheme.typography.labelSmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text("100 %", style=MaterialTheme.typography.labelSmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text("115 %", style=MaterialTheme.typography.labelSmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(color=MaterialTheme.colorScheme.onSurface.copy(alpha=.08f))
            Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Densidad de interfaz", style=MaterialTheme.typography.titleSmall)
                    Text("Compacta o expande paddings, separaciones e íconos conservando objetivos táctiles.", style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${(densityScale*100).roundToInt()}%", Modifier.testTag("ui-density-value"), style=MaterialTheme.typography.labelLarge)
            }
            GlassSlider(
                value=densityScale,
                onValueChange={
                    val snapped=GlassTokens.snapScale(it)
                    update(c.copy(uiDensityScale=snapped, density=GlassTokens.densityKeyForScale(snapped)))
                },
                modifier=Modifier.testTag("ui-density-slider").semantics { contentDescription="Densidad de interfaz" },
                valueRange=GlassTokens.minScale..GlassTokens.maxScale,
                steps=5,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                Text("85 % · Compacta", style=MaterialTheme.typography.labelSmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text("100 % · Normal", style=MaterialTheme.typography.labelSmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text("115 % · Amplia", style=MaterialTheme.typography.labelSmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
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
    val d=GlassTokens.densityScale(c)
    Column(Modifier.fillMaxWidth(), verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween) {
            Text("Vista previa · ${if(landscape) "horizontal" else "vertical"}", style=MaterialTheme.typography.titleSmall)
            if(simple) Text("Texto ${(GlassTokens.uiTextScale(c)*100).roundToInt()} % · Densidad ${(d*100).roundToInt()} %", style=MaterialTheme.typography.labelSmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if(!simple) Text("Ancho visible: $width dp · $source", style=MaterialTheme.typography.bodySmall, modifier=Modifier.testTag("preview-width-label"))
        if(!simple && requested>width) Text("Configurado: $requested dp; limitado al espacio de esta pantalla.", style=MaterialTheme.typography.bodySmall)
        if(!simple) Text("Tamaño real · deslizá horizontalmente si no cabe en Ajustes. Tocá una fila para probar la selección; no ejecuta acciones.", style=MaterialTheme.typography.bodySmall)
        BoxWithConstraints(Modifier.fillMaxWidth().testTag("appearance-preview-viewport")) {
            val displayWidth=if(simple)minOf(maxWidth,width.dp) else width.dp
            val canvasWidth=if(simple)maxWidth else maxOf(maxWidth, width.dp)
            Box(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Box(Modifier.width(canvasWidth), contentAlignment=Alignment.TopCenter) {
                    SpotlightTheme(c.theme, c) {
                        Column(Modifier.width(displayWidth).testTag("appearance-preview-window").heightIn(max=240.dp).glassSurface(c).verticalScroll(rememberScrollState()).padding(horizontal=GlassTokens.listHorizontalInset(c), vertical=(10f*d).dp)) {
                            Text("Buscar en Spotlight", fontSize=GlassTokens.scaledSp(15.sp, c), style=MaterialTheme.typography.titleMedium, modifier=Modifier.padding(start=GlassTokens.rowHorizontalPadding(c), bottom=(6f*d).dp))
                            HorizontalDivider()
                            Spacer(Modifier.height((4f*d).dp))
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
