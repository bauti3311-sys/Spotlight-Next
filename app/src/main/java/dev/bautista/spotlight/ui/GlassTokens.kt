package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.data.Config

object GlassTokens {
    val small=8.dp;val gap=12.dp;val inset=18.dp;val hit=48.dp
    val actionPanelMaxHeight=360.dp
    const val selectionMs=90
    fun selectionDuration(c:Config)=when(c.movement){"precise"->55;"liquid_immersive"->125;else->90}
    fun opacity(c:Config)=if(c.preset in setOf("raycast","pure","graphite","hybrid"))1f else c.opacity.coerceIn(.90f,1f)
}
@Composable fun Modifier.glassSurface(c:Config):Modifier {
    val colors=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(c.corner.coerceIn(12,32).dp)
    val intensity=if(c.preset=="glass" || c.preset=="macos")c.glassIntensity.coerceIn(0f,1f) else 0f
    return shadow(c.shadowSize.coerceIn(0f,20f).dp,shape).clip(shape)
        .background(colors.surface.copy(alpha=GlassTokens.opacity(c)))
        .background(Brush.linearGradient(listOf(Color.White.copy(alpha=.055f*intensity),Color.Transparent,colors.primary.copy(alpha=.025f*intensity))))
        .border(c.borderWidth.coerceIn(0f,2f).dp,Brush.linearGradient(listOf(colors.onSurface.copy(alpha=.18f+intensity*.06f),colors.outlineVariant,colors.onSurface.copy(alpha=.06f))),shape)
}
@Composable fun GlassEmptyState(title:String,detail:String) {
    Column(Modifier.fillMaxWidth().padding(GlassTokens.inset),verticalArrangement=Arrangement.spacedBy(GlassTokens.small)) {
        Text(title,style=MaterialTheme.typography.titleSmall)
        Text(detail,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
