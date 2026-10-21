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
    val actionSurface=Color(0xFF242430);val actionSelected=Color(0xFF343646)
    val indigo=Color(0xFF7275E8);val tertiary=Color(0xFF777B8D)
    val windowRadius=20.dp;val searchHeight=48.dp;val listInset=12.dp
    val rowRadius=11.dp;val rowMinimum=42.dp;val glyph=20.dp
    val searchText=16.sp;val compactSearchText=16.sp
    val resultText=14.sp;val secondaryText=12.5.sp;val metadataText=11.sp
    val graphite=Color(0xFF1C1C21);val paper=Color(0xFFFAFAFC)
    val darkInk=Color(0xFFF5F5F8);val lightInk=Color(0xFF18181B)
    val darkSecondary=Color(0xFFA5A7B7);val lightSecondary=Color(0xFF52525B)
    @Composable fun selection()=MaterialTheme.colorScheme.onSurface.copy(alpha=.08f)
    @Composable fun hover()=MaterialTheme.colorScheme.onSurface.copy(alpha=.04f)
    const val glyphStroke=1.5f
    const val selectionMs=90
    fun rowHeight(c:Config)=when(c.density){"compact"->36.dp;"spacious"->48.dp;else->42.dp}
    fun resultRowHeight(c:Config)=maxOf(48.dp,rowHeight(c))
    fun rowText(c:Config)=when(c.density){"compact"->13.sp;"spacious"->14.5.sp;else->14.5.sp}
    fun windowColors(c:Config,dark:Boolean):List<Color> = when {
        !dark -> listOf(Color(0xE0FAFAFC),Color(0xF0EEEEF3))
        c.preset in setOf("raycast","graphite") -> listOf(Color(0xEB26272C),Color(0xF7191A1E))
        else -> listOf(Color(0xFA1B1B23),Color(0xFD17171D))
    }
    fun selectedColors(dark:Boolean,amount:Float=1f)=if(dark)
        listOf(Color(0xFFB8C2DB).copy(alpha=.17f*amount),Color(0xFFA8B5D0).copy(alpha=.11f*amount))
        else listOf(.09f,.05f).map{Color.Black.copy(alpha=it*amount)}
    fun selectionDuration(c:Config)=when(c.movement){"precise"->55;"liquid_immersive"->125;else->90}
    fun opacity(c:Config)=if(c.preset in setOf("raycast","pure","graphite","hybrid"))1f else c.opacity.coerceIn(.90f,1f)
}
@Composable fun Modifier.glassSurface(c:Config, legibleBackdrop:Boolean=false):Modifier {
    val colors=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(GlassTokens.windowRadius)
    val dark=colors.surface.luminance()<.5f
    val referenceMaterial=c.preset in setOf("glass","macos","raycast","graphite")
    val material=if(referenceMaterial)GlassTokens.windowColors(c,dark).map {
        it.copy(alpha=(it.alpha*(c.opacity/.96f)).coerceIn(if(legibleBackdrop).995f else .88f,1f))
    } else listOf(colors.surface.copy(alpha=GlassTokens.opacity(c)),colors.surface.copy(alpha=GlassTokens.opacity(c)))
    return shadow(28.dp,shape,clip=false,
            ambientColor=Color.Black.copy(alpha=.24f),spotColor=Color.Black.copy(alpha=.40f))
        .clip(shape)
        .background(Brush.verticalGradient(material))
        .border(1.dp,(if(dark)Color.White else Color.Black).copy(alpha=if(dark).13f else .12f),shape)
        .drawWithContent {
            drawContent()
            drawLine(Color.White.copy(alpha=if(dark).22f else .85f),
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
