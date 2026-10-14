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

/** Gemini's contextual capsule, retaining the native Enter/Tab contract and touch targets. */
@Composable fun GlassContextFooter(suggestion:Boolean,enabled:Boolean,primaryLabel:String,onExecute:()->Unit,onActions:()->Unit) {
    val colors=MaterialTheme.colorScheme
    val dark=colors.surface.luminance()<.5f
    val graphite=LocalAppearance.current.preset in setOf("raycast","graphite")
    val base=when { !dark->Color(0xF0EBEBF2);graphite->Color(0xF0191A1E);else->Color(0xF0101014) }
    val pill=when { !dark->Color(0xF0F4F4F8);graphite->Color(0xF01F2025);else->Color(0xEB18181D) }
    BoxWithConstraints(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(base.copy(alpha=0f),base)))) {
        val showHints=maxWidth>=520.dp
        Row(Modifier.fillMaxWidth().heightIn(min=56.dp).padding(horizontal=12.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically) {
            if(showHints) Text(if(suggestion)"Ctrl+Space completar" else "↑↓ elegir · esc cerrar",fontSize=11.sp,color=colors.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            if(enabled) Row(Modifier.shadow(8.dp,CircleShape).clip(CircleShape).background(pill)
                .border(1.dp,(if(dark)Color.White else Color.Black).copy(alpha=.13f),CircleShape),verticalAlignment=Alignment.CenterVertically) {
                TextButton(onClick=onExecute,contentPadding=PaddingValues(horizontal=12.dp),modifier=Modifier.heightIn(min=48.dp)) {
                    Text("↵ $primaryLabel",fontSize=12.sp,color=colors.onSurface)
                }
                Box(Modifier.width(1.dp).height(12.dp).background(colors.onSurface.copy(alpha=.10f)))
                TextButton(onClick=onActions,contentPadding=PaddingValues(horizontal=12.dp),modifier=Modifier.heightIn(min=48.dp)) {
                    Text("Tab acciones",fontSize=12.sp,color=colors.onSurfaceVariant)
                }
            }
        }
    }
}
