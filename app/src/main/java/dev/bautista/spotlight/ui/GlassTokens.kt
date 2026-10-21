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
    val actionPanelMaxHeight=560.dp
    val actionSurface=Color(0xFF20212B);val actionSelected=Color(0xFF343646)
    val indigo=Color(0xFF7275E8);val tertiary=Color(0xFF777B8D)
    val windowRadius=16.dp;val searchHeight=48.dp;val listInset=12.dp
    val rowRadius=11.dp;val rowMinimum=42.dp;val glyph=20.dp
    val searchText=18.sp;val compactSearchText=18.sp
    val resultText=14.sp;val secondaryText=12.5.sp;val metadataText=11.sp
    val graphite=Color(0xFF1C1C21);val paper=Color(0xFFFAFAFC)
    val darkInk=Color(0xFFF5F5F8);val lightInk=Color(0xFF18181B)
    val darkSecondary=Color(0xFFBEC3D2);val lightSecondary=Color(0xFF52525B)
    @Composable fun selection()=MaterialTheme.colorScheme.onSurface.copy(alpha=.08f)
    @Composable fun hover()=MaterialTheme.colorScheme.onSurface.copy(alpha=.04f)
    const val glyphStroke=1.5f
    const val selectionMs=90
    fun rowHeight(c:Config)=when(c.density){"compact"->36.dp;"spacious"->48.dp;else->42.dp}
    fun resultRowHeight(c:Config)=maxOf(56.dp,rowHeight(c))
    fun rowText(c:Config)=when(c.density){"compact"->13.sp;"spacious"->14.5.sp;else->15.sp}
    const val minimumOpacity=.70f
    const val defaultOpacity=.80f
    fun opacity(c:Config)=c.opacity.takeIf{it.isFinite()}?.coerceIn(minimumOpacity,1f) ?: defaultOpacity
    fun windowColors(c:Config,dark:Boolean):List<Color> {
        val alpha=opacity(c)
        return if(dark) listOf(Color(0xFF20212B).copy(alpha=alpha),Color(0xFF171921).copy(alpha=alpha))
        else listOf(Color(0xFFFAFAFC).copy(alpha=alpha),Color(0xFFF1F2F7).copy(alpha=alpha))
    }
    fun secondary(c:Config,dark:Boolean)=if(dark) {if(opacity(c)<.80f)Color(0xFFE3E5ED) else darkSecondary} else {if(opacity(c)<.85f)Color(0xFF373C48) else lightSecondary}
    fun actionColor(c:Config,dark:Boolean)=
        (if(dark)actionSurface else paper).copy(alpha=(opacity(c)+.16f).coerceIn(.90f,1f))
    fun selectedColors(dark:Boolean,amount:Float=1f)=if(dark)
        listOf(Color(0xFFB8C2DB).copy(alpha=.17f*amount),Color(0xFFA8B5D0).copy(alpha=.11f*amount))
        else listOf(.09f,.05f).map{Color.Black.copy(alpha=it*amount)}
    fun selectionDuration(c:Config)=when(c.movement){"precise"->55;"liquid_immersive"->125;else->90}
}
@Composable fun Modifier.glassSurface(c:Config, legibleBackdrop:Boolean=false):Modifier {
    val colors=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(GlassTokens.windowRadius)
    val dark=colors.surface.luminance()<.5f
    val material=GlassTokens.windowColors(c,dark)
    return shadow(20.dp,shape,clip=false,
            ambientColor=Color.Black.copy(alpha=.24f),spotColor=Color.Black.copy(alpha=.40f))
        .clip(shape)
        .background(Brush.verticalGradient(material))
        .border(1.dp,(if(dark)Color.White else Color.Black).copy(alpha=if(dark).19f else .12f),shape)
        .drawWithContent {
            drawContent()
            drawLine(Color.White.copy(alpha=if(dark).12f else .60f),
                androidx.compose.ui.geometry.Offset(GlassTokens.windowRadius.toPx(),.5.dp.toPx()),
                androidx.compose.ui.geometry.Offset(size.width-GlassTokens.windowRadius.toPx(),.5.dp.toPx()),
                strokeWidth=1.dp.toPx())
        }
}
@Composable fun GlassEmptyState(title:String,detail:String) {
    Column(Modifier.fillMaxWidth().padding(GlassTokens.inset),verticalArrangement=Arrangement.spacedBy(GlassTokens.small)) {
        Text(title,style=MaterialTheme.typography.titleSmall)
        Text(detail,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
