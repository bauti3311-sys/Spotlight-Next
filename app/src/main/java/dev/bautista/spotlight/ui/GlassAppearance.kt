package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun SettingsOptions(content:@Composable ()->Unit) {
    FlowRow(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){content()}
}

/** Product settings: technical values remain in storage, not in the main user flow. */
@Composable fun GlassAppearance(c:Config,update:(Config)->Unit) = GlassAppearance(c,true,update)

@Composable fun GlassAppearance(c:Config,showHeading:Boolean,update:(Config)->Unit) {
    var draft by remember { mutableStateOf(c) }
    var dirty by remember { mutableStateOf(false) }
    LaunchedEffect(c) { if(!dirty)draft=c }
    fun edit(next:Config){draft=next;dirty=true}
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        if(showHeading)Text("Apariencia",style=MaterialTheme.typography.headlineSmall)
        Text("Raycast Dark Glass",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        GlassThemeChoices(draft){edit(it)}
        SettingsGroupSurface {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Opacidad de superficie",style=MaterialTheme.typography.titleSmall)
                    Text("100% es opaco. Texto e iconos siempre nítidos.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${(GlassTokens.opacity(draft)*100).roundToInt()}%",Modifier.testTag("surface-opacity-value"),style=MaterialTheme.typography.labelLarge)
            }
            GlassSlider(GlassTokens.opacity(draft),{value->
                draft=draft.copy(opacity=value)
                // Only this persisted field is immediate; other draft edits still require Apply.
                update(AppearanceEdits.merge(c,c.copy(opacity=value)))
            },Modifier.testTag("surface-opacity-slider").semantics{contentDescription="Opacidad de superficie";stateDescription="${(GlassTokens.opacity(draft)*100).roundToInt()}%"},valueRange=GlassTokens.minimumOpacity..1f)
            TextButton(onClick={draft=draft.copy(opacity=GlassTokens.defaultOpacity);update(AppearanceEdits.merge(c,c.copy(opacity=GlassTokens.defaultOpacity)))}){Text("Restablecer opacidad · 80%")}
            var preview by remember{mutableStateOf(false)}
            TextButton(onClick={preview=!preview}){Text(if(preview)"Ocultar vista previa" else "Vista previa en vivo")}
            // The complete settings surface updates immediately; this optional sample uses the same renderer.
            if(preview)SpotlightTheme(draft.theme,draft){OverlayAppearancePreview(draft,simple=true)}
        }
        WindowSizeSettings(draft){edit(it)}
        Text("Acento",style=MaterialTheme.typography.titleSmall)
        SettingsOptions { listOf("blue" to "Azul","purple" to "Lila","rose" to "Rosa","green" to "Verde","graphite" to "Neutro").forEach { (key,label)->
            FilterChip(draft.accent==key,{edit(draft.copy(accent=key))},label={Text(label)})
        } }
        OverlayDensitySettings(draft,simple=true){edit(it)}
        Row(Modifier.fillMaxWidth()) { Column(Modifier.weight(1f)) {
            Text("Reducir movimiento",style=MaterialTheme.typography.titleSmall)
            Text("También respeta la preferencia del sistema.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        };Switch(!draft.animations,{edit(draft.copy(animations=!it))}) }
        Text("La altura se adapta al contenido, al teclado y al espacio disponible.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { Button(onClick={update(AppearanceEdits.merge(c,draft));dirty=false}){Text("Aplicar apariencia")};TextButton(onClick={draft=c;dirty=false}){Text("Descartar cambios")} }
        ConfirmedSettingsButton("Restablecer apariencia","Restablece tema, opacidad, acento, densidad, movimiento y ancho. Tus datos y otras preferencias se conservan.") { edit(AppearanceEdits.merge(c,Config(windowScale=.5f))) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable private fun GlassThemeChoices(c:Config,edit:(Config)->Unit) {
 val colors=MaterialTheme.colorScheme
 Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
  Text("Estilo y contraste",Modifier.weight(1f),style=MaterialTheme.typography.titleSmall)
  FilterChip(c.theme=="auto",{edit(c.copy(theme="auto"))},label={Text("Sistema")})
 }
 BoxWithConstraints(Modifier.fillMaxWidth()) {
  val width=if(maxWidth>=560.dp)(maxWidth-16.dp)/3 else (maxWidth-8.dp)/2
  FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
   listOf(Triple("glass","dark","Raycast Dark Glass"),Triple("raycast","dark","Graphite"),Triple("glass","light","macOS Light")).forEach{(preset,theme,label)->
    val active=c.preset==preset && c.theme==theme
    Surface(onClick={edit(c.copy(preset=preset,theme=theme))},modifier=Modifier.width(width).heightIn(min=76.dp).semantics{selected=active},
     shape=RoundedCornerShape(10.dp),color=colors.onSurface.copy(alpha=.025f),border=BorderStroke(1.dp,if(active)colors.primary else colors.onSurface.copy(alpha=.10f))) {
     Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
      Box(Modifier.size(30.dp).background(Brush.linearGradient(if(theme=="light")listOf(Color.White,Color(0xFFD9DFED)) else listOf(Color(0xFF383C50),Color(0xFF14161E))),RoundedCornerShape(7.dp)).border(1.dp,colors.onSurface.copy(alpha=.10f),RoundedCornerShape(7.dp)))
      Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {
       Text(label,fontWeight=FontWeight.Medium,style=MaterialTheme.typography.bodySmall)
       Text(if(theme=="light")"Claro" else if(preset=="raycast")"Grafito" else "Translúcido",style=MaterialTheme.typography.labelSmall,color=colors.onSurfaceVariant)
      }
     }
    }
   }
  }
 }
}
