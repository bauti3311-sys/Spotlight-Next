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
    val opening=remember { c }
    var awaitingSave by remember { mutableStateOf(false) }
    LaunchedEffect(c) { if(!awaitingSave || AppearanceEdits.merge(c,draft)==c){draft=c;awaitingSave=false} }
    fun edit(next:Config){draft=next;awaitingSave=true;update(AppearanceEdits.merge(c,next))}
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        if(showHeading)Text("Apariencia",style=MaterialTheme.typography.headlineSmall)
        Text("Raycast · Solid Graphite",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        GlassThemeChoices(draft){edit(it)}
        SettingsGroupSurface {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Opacidad del buscador",style=MaterialTheme.typography.titleSmall)
                    Text("100% es opaco. Texto e iconos siempre nítidos.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${(GlassTokens.opacity(draft)*100).roundToInt()}%",Modifier.testTag("surface-opacity-value"),style=MaterialTheme.typography.labelLarge)
            }
            GlassSlider(GlassTokens.opacity(draft),{edit(draft.copy(opacity=it))},
                Modifier.testTag("surface-opacity-slider").semantics{contentDescription="Opacidad del buscador"},valueRange=GlassTokens.minimumOpacity..1f)
            RoleOpacityControl("ActionPanel",GlassTokens.actionOpacity(draft),"action"){edit(draft.copy(actionOpacity=it))}
            RoleOpacityControl("Ajustes",GlassTokens.settingsOpacity(draft),"settings"){edit(draft.copy(settingsOpacity=it))}
            TextButton(onClick={edit(draft.copy(opacity=if(draft.preset=="glass").84f else .98f,actionOpacity=if(draft.preset=="glass").92f else 1f,settingsOpacity=if(draft.preset=="glass").94f else .99f))}){Text("Restablecer opacidades del material")}
            // Compact live preview stays visible permanently so scale, density, width and opacity can be verified immediately.
            SpotlightTheme(draft.theme,draft){OverlayAppearancePreview(draft,simple=true)}
        }
        OverlayDensitySettings(draft,simple=true){edit(it)}
        WindowSizeSettings(draft){edit(it)}
        Text("Acento",style=MaterialTheme.typography.titleSmall)
        SettingsOptions { listOf("blue" to "Azul","purple" to "Lila","rose" to "Rosa","green" to "Verde","graphite" to "Neutro").forEach { (key,label)->
            FilterChip(draft.accent==key,{edit(draft.copy(accent=key))},label={Text(label)})
        } }
        Row(Modifier.fillMaxWidth()) { Column(Modifier.weight(1f)) {
            Text("Reducir movimiento",style=MaterialTheme.typography.titleSmall)
            Text("También respeta la preferencia del sistema.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        };Switch(!draft.animations,{edit(draft.copy(animations=!it))}) }
        Text("La altura se adapta al contenido, al teclado y al espacio disponible.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Los cambios se guardan y se muestran al instante.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick={edit(AppearanceEdits.merge(c,opening))}){Text("Deshacer cambios de esta visita")}
        ConfirmedSettingsButton("Restablecer apariencia","Restablece tema, opacidad, acento, escala, densidad, movimiento y ancho. Tus datos y otras preferencias se conservan.") { edit(AppearanceEdits.merge(c,Config(windowScale=.5f,textScale=1f,uiDensityScale=1f,density="balanced"))) }
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
   listOf(Triple("raycast","dark","Solid Graphite"),Triple("glass","dark","Dark Glass"),Triple("glass","light","macOS Light")).forEach{(preset,theme,label)->
    val active=c.preset==preset && c.theme==theme
    Surface(onClick={edit(if(theme=="dark")AppearancePresets.apply(if(preset=="glass")"Dark Glass" else "Solid Graphite",c) else c.copy(preset=preset,theme=theme))},modifier=Modifier.width(width).heightIn(min=76.dp).semantics{selected=active},
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

@Composable private fun RoleOpacityControl(label:String,value:Float,tag:String,onChange:(Float)->Unit) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        Text("Opacidad de $label",Modifier.weight(1f),style=MaterialTheme.typography.titleSmall)
        Text("${(value*100).roundToInt()}%",Modifier.testTag("$tag-opacity-value"),style=MaterialTheme.typography.labelLarge)
    }
    GlassSlider(value,onChange,Modifier.testTag("$tag-opacity-slider").semantics{contentDescription="Opacidad de $label"},valueRange=GlassTokens.minimumOpacity..1f)
}
