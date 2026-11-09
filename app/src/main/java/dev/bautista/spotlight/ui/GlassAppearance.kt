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
import androidx.compose.runtime.saveable.rememberSaveable
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

/** One live width control; everyday choices first, technical tuning on demand. */
@Composable fun GlassAppearance(c:Config,update:(Config)->Unit) = GlassAppearance(c,true,update)

@Composable fun GlassAppearance(c:Config,showHeading:Boolean,update:(Config)->Unit) {
    var draft by remember { mutableStateOf(c) }
    val opening=remember { c }
    var awaitingSave by remember { mutableStateOf(false) }
    var advanced by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(c) { if(!awaitingSave || AppearanceEdits.merge(c,draft)==c){draft=c;awaitingSave=false} }
    fun edit(next:Config){draft=next;awaitingSave=true;update(AppearanceEdits.merge(c,next))}
    val colors=MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(24.dp)) {
        if(showHeading)Text("Apariencia y ventana",style=MaterialTheme.typography.headlineMedium)
        SettingsGroupSurface {WindowSizeSettings(draft){edit(it)}}
        Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Tema",style=MaterialTheme.typography.titleMedium)
            GlassThemeChoices(draft){edit(it)}
        }
        SettingsGroupSurface {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text("Interfaz Puente",style=MaterialTheme.typography.titleSmall)
                    Text(if(draft.focusInterface)"Buscador y contexto separados. Podés volver a la interfaz anterior." else "Interfaz anterior activa. Tus preferencias se conservan.",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
                }
                Switch(draft.focusInterface,{edit(draft.copy(focusInterface=it))},modifier=Modifier.testTag("focus-interface-toggle"))
            }
            HorizontalDivider(color=colors.outlineVariant.copy(alpha=.5f))
            Text("Color de acento",style=MaterialTheme.typography.titleSmall)
            SettingsOptions { listOf("blue" to "Azul","purple" to "Lila","rose" to "Rosa","green" to "Verde","graphite" to "Neutro").forEach { (key,label)->
                FilterChip(draft.accent==key,{edit(draft.copy(accent=key))},label={Text(label)})
            } }
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {Text("Reducir movimiento",style=MaterialTheme.typography.titleSmall);Text("También respeta la preferencia de Android.",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)}
                Switch(!draft.animations,{edit(draft.copy(animations=!it))})
            }
        }
        TextButton(onClick={advanced=!advanced},modifier=Modifier.testTag("appearance-advanced")){Text(if(advanced)"Ocultar ajustes de detalle" else "Texto, espaciado y transparencia")}
        val technicalQuery=LocalSettingsQuery.current.let{q->listOf("opacidad","transparencia","texto","densidad","tipografía","fuente").any{it in q.lowercase()}}
        if(advanced || technicalQuery) {
            OverlayDensitySettings(draft,simple=true){edit(it)}
            if(draft.focusInterface)SettingsGroupSurface {
                Text("Tipografía",style=MaterialTheme.typography.titleSmall)
                SettingsOptions {listOf("geist" to "Geist","inter" to "Inter","system" to "Sistema","mono" to "Monoespaciada").forEach{(key,label)->FilterChip(draft.focusTypeface==key,{edit(draft.copy(focusTypeface=key))},label={Text(label)})}}
                Text("Puente conserva un respaldo de alta opacidad para evitar que el fondo interfiera con la lectura.",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
            }
            SettingsGroupSurface {
                Text("Transparencia de otras superficies",style=MaterialTheme.typography.titleSmall)
                if(!draft.focusInterface)RoleOpacityControl("buscador",GlassTokens.opacity(draft),"surface"){edit(draft.copy(opacity=it))}
                RoleOpacityControl("menú de acciones",GlassTokens.actionOpacity(draft),"action"){edit(draft.copy(actionOpacity=it))}
                RoleOpacityControl("Ajustes",GlassTokens.settingsOpacity(draft),"settings"){edit(draft.copy(settingsOpacity=it))}
                Text("100 % es opaco. No cambia el tamaño ni el texto.",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
            }
        }
        Text("Guardado automático. El ancho se aplica también a la ventana que ya esté abierta.",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
        SettingsOptions {
            TextButton(onClick={edit(AppearanceEdits.merge(c,opening))}){Text("Deshacer cambios de esta visita")}
            ConfirmedSettingsButton("Restablecer apariencia","Restablece tema, acento, escala, densidad, movimiento y ancho. Tus datos, permisos y otras preferencias se conservan.") { edit(AppearanceEdits.merge(c,Config(windowScale=.5f,textScale=1f,uiDensityScale=1f,density="balanced"))) }
        }
    }
}

@Composable private fun GlassThemeChoices(c:Config,edit:(Config)->Unit) {
 val colors=MaterialTheme.colorScheme
 SettingsGroupSurface {
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
   Text("Usar tema de Android",Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium)
   Switch(c.theme=="auto",{edit(c.copy(theme=if(it)"auto" else "dark"))})
  }
  BoxWithConstraints(Modifier.fillMaxWidth()) {
   val columns=if(maxWidth>=560.dp)3 else if(maxWidth>=360.dp)2 else 1
   val width=(maxWidth-12.dp*(columns-1))/columns-1.dp
   @OptIn(ExperimentalLayoutApi::class)
   FlowRow(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
    listOf(Triple("raycast","dark","Grafito"),Triple("glass","dark","Vidrio oscuro"),Triple("glass","light","Claro")).forEach{(preset,theme,label)->
     val active=c.preset==preset && c.theme==theme
     Surface(onClick={edit(if(theme=="dark")AppearancePresets.apply(if(preset=="glass")"Dark Glass" else "Solid Graphite",c) else c.copy(preset=preset,theme=theme))},modifier=Modifier.width(width).heightIn(min=104.dp).semantics{selected=active},
      shape=RoundedCornerShape(12.dp),color=colors.surface,border=BorderStroke(if(active)2.dp else 1.dp,if(active)colors.primary else colors.outlineVariant)) {
      Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
       Box(Modifier.fillMaxWidth().height(28.dp).background(if(theme=="light")Color(0xFFE7EFF3) else if(preset=="glass")Color(0xFF253B47) else Color(0xFF282C34),RoundedCornerShape(6.dp)).padding(7.dp)) {
        Box(Modifier.fillMaxWidth(.65f).height(4.dp).background(if(theme=="light")Color(0xFF78909C) else Color(0xFFA9BAC5),RoundedCornerShape(2.dp)))
       }
       Text(label,style=MaterialTheme.typography.titleSmall)
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
