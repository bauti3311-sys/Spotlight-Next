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
    val windowRadius=20.dp;val searchHeight=68.dp;val listInset=8.dp
    val rowRadius=9.dp;val rowMinimum=56.dp;val glyph=22.dp
    val searchText=21.sp;val compactSearchText=18.sp
    val resultText=15.5.sp;val secondaryText=12.5.sp;val metadataText=11.sp
    val graphite=Color(0xFF202122);val paper=Color(0xFFFAFAF9)
    val darkInk=Color(0xFFF2F2F2);val lightInk=Color(0xFF232426)
    val darkSecondary=Color(0xFFAAA9A7);val lightSecondary=Color(0xFF646568)
    @Composable fun selection()=MaterialTheme.colorScheme.onSurface.copy(alpha=.08f)
    @Composable fun hover()=MaterialTheme.colorScheme.onSurface.copy(alpha=.04f)
    const val glyphStroke=1.5f
    const val selectionMs=90
    fun selectionDuration(c:Config)=when(c.movement){"precise"->55;"liquid_immersive"->125;else->90}
    fun opacity(c:Config)=if(c.preset in setOf("raycast","pure","graphite","hybrid"))1f else c.opacity.coerceIn(.90f,1f)
}
@Composable fun Modifier.glassSurface(c:Config):Modifier {
    val colors=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(GlassTokens.windowRadius)
    val dark=colors.surface.luminance()<.5f
    return shadow(20.dp,shape,clip=false,
            ambientColor=Color.Black.copy(alpha=.28f),spotColor=Color.Black.copy(alpha=.30f))
        .clip(shape)
        .background(colors.surface.copy(alpha=GlassTokens.opacity(c)))
        .background(Brush.linearGradient(listOf(
            colors.onSurface.copy(alpha=if(dark).035f else .025f),Color.Transparent,
            Color.Black.copy(alpha=if(dark).07f else .015f))))
        .border(.5.dp,colors.onSurface.copy(alpha=if(dark).13f else .10f),shape)
}
@Composable fun GlassEmptyState(title:String,detail:String) {
    Column(Modifier.fillMaxWidth().padding(GlassTokens.inset),verticalArrangement=Arrangement.spacedBy(GlassTokens.small)) {
        Text(title,style=MaterialTheme.typography.titleSmall)
        Text(detail,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
