package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.testTag

/** Gemini's contextual capsule, retaining the native Enter/Tab contract and touch targets. */
@Composable fun GlassContextFooter(suggestion:Boolean,enabled:Boolean,primaryLabel:String,onExecute:()->Unit,onActions:()->Unit,autocomplete:Boolean=false,expandLabel:String="Ver más resultados",onExpand:(()->Unit)?=null) {
    val colors=MaterialTheme.colorScheme
    val dark=colors.surface.luminance()<.5f
    val graphite=LocalAppearance.current.preset in setOf("raycast","graphite")
    val base=when { !dark->Color(0xF0EBEBF2);graphite->Color(0xF0191A1E);else->Color(0xF0101014) }
    val pill=when { !dark->Color(0xF0F4F4F8);graphite->Color(0xF01F2025);else->Color(0xEB18181D) }
    BoxWithConstraints(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(base.copy(alpha=0f),base)))) {
        val showHints=maxWidth>=520.dp
        Row(Modifier.fillMaxWidth().heightIn(min=48.dp).padding(horizontal=12.dp,vertical=1.dp),verticalAlignment=Alignment.CenterVertically) {
            if(onExpand!=null) TextButton(onClick=onExpand,modifier=Modifier.heightIn(min=48.dp).testTag("results-expand"),contentPadding=PaddingValues(horizontal=4.dp)) {
                Text(if(showHints)expandLabel else if(expandLabel.startsWith("Ver menos"))"Menos" else "Más",fontSize=11.sp,color=colors.onSurfaceVariant)
            }
            else if(showHints && suggestion) Text(if(autocomplete)"Tab completar" else "Ctrl+Space completar",fontSize=11.sp,color=colors.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            // The visible capsule is 32 dp; the native buttons retain 48 dp hit areas.
            if(enabled) Box {
                Box(Modifier.matchParentSize().padding(vertical=8.dp).clip(CircleShape).background(pill)
                    .border(1.dp,(if(dark)Color.White else Color.Black).copy(alpha=.10f),CircleShape))
                Row(verticalAlignment=Alignment.CenterVertically) {
                TextButton(onClick=onExecute,contentPadding=PaddingValues(horizontal=12.dp),modifier=Modifier.heightIn(min=48.dp)) {
                    Text("↵ $primaryLabel",fontSize=12.sp,color=colors.onSurface)
                }
                Box(Modifier.width(1.dp).height(12.dp).background(colors.onSurface.copy(alpha=.10f)))
                TextButton(onClick=onActions,contentPadding=PaddingValues(horizontal=12.dp),modifier=Modifier.heightIn(min=48.dp)) {
                    Text(if(autocomplete)"Ctrl+K acciones" else "Tab acciones",fontSize=12.sp,color=colors.onSurfaceVariant)
                }
                }
            }
        }
    }
}
