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
import kotlin.math.roundToInt

object GlassTokens {
    val small=8.dp;val gap=12.dp;val inset=18.dp;val hit=48.dp
    val contentAxis=22.dp
    const val minScale=.85f
    const val maxScale=1.15f
    const val scaleStep=.05f
    fun snapScale(raw:Float):Float=(((raw.coerceIn(minScale,maxScale)/scaleStep).roundToInt()*5).coerceIn(85,115))/100f
    fun textScale(c:Config):Float=c.textScale.takeIf{it.isFinite()}?.coerceIn(minScale,1.3f) ?: 1f
    fun uiTextScale(c:Config):Float=snapScale(textScale(c))
    fun densityScale(c:Config):Float=c.uiDensityScale.takeIf{it.isFinite() && it in minScale..maxScale}?.let(::snapScale)
        ?: when(c.density){"compact"->.85f;"spacious"->1.15f;else->1f}
    fun densityKeyForScale(scale:Float):String=when{
        scale<=.90f->"compact"
        scale>=1.10f->"spacious"
        else->"balanced"
    }
    fun scaledSp(base:TextUnit,c:Config,minSp:Float=10f):TextUnit=(base.value*textScale(c)).coerceAtLeast(minSp).sp
    fun listHorizontalInset(c:Config)=(10f*densityScale(c)).dp
    fun rowHorizontalPadding(c:Config)=(12f*densityScale(c)).dp
    fun rowVerticalPadding(c:Config,inline:Boolean=true)=((if(inline)4f else 6f)*densityScale(c)).dp
    fun appIconSize(c:Config)=(26f*densityScale(c)).coerceIn(22f,31f).dp
    fun glyphSize(c:Config)=(20f*densityScale(c)).coerceIn(17f,24f).dp
    fun iconSlotWidth(c:Config)=maxOf(appIconSize(c),glyphSize(c))
    fun inspectorPadding(c:Config)=(16f*densityScale(c)).dp
    fun inspectorGap(c:Config)=(8f*densityScale(c)).dp
    val actionPanelMaxHeight=560.dp
    val actionSurface=Color(0xFF19191D);val actionSelected=Color(0xFF333338)
    val indigo=Color(0xFF7275E8);val tertiary=Color(0xFF777B8D)
    val windowRadius=22.dp;val searchHeight=56.dp;val listInset=10.dp
    val rowRadius=10.dp;val rowMinimum=42.dp;val glyph=20.dp
    val searchText=20.sp;val compactSearchText=18.sp
    val resultText=14.sp;val secondaryText=12.5.sp;val metadataText=11.sp
    val graphite=Color(0xFF1C1C21);val paper=Color(0xFFFAFAFC)
    val darkInk=Color(0xFFF5F5F8);val lightInk=Color(0xFF18181B)
    val darkSecondary=Color(0xFFBEC3D2);val lightSecondary=Color(0xFF52525B)
    @Composable fun selection()=MaterialTheme.colorScheme.onSurface.copy(alpha=.08f)
    @Composable fun hover()=MaterialTheme.colorScheme.onSurface.copy(alpha=.04f)
    val focusStroke=2.dp
    // Result focus is quieter than the independent ActionPanel, including in light mode.
    val resultFocusStroke=1.dp
    fun resultFocusColor(dark:Boolean)=if(dark)Color(0xFF969AAB) else Color(0xFF747783)
    @Composable fun focusRing()=MaterialTheme.colorScheme.onSurface
    @Composable fun inputOutline()=MaterialTheme.colorScheme.onSurface.copy(alpha=.50f)
    const val glyphStroke=1.5f
    const val selectionMs=90
    fun rowHeight(c:Config):Dp {
        val d=densityScale(c)
        return if(d>1f)(48f*d).dp else (42f*d).dp
    }
    // 44dp visual rhythm, with at least 48dp allocated to each accessible touch target.
    fun resultRowHeight(c:Config)=maxOf(hit,rowHeight(c))
    fun rowText(c:Config)=14.sp
    const val minimumOpacity=.70f
    const val defaultOpacity=.98f
    fun opacity(c:Config)=c.opacity.takeIf{it.isFinite()}?.coerceIn(minimumOpacity,1f) ?: defaultOpacity
    fun windowColors(c:Config,dark:Boolean,legibleBackdrop:Boolean=false):List<Color> {
        // Keep the stored preference intact. The working search surface must not
        // let another window's text compete with results and editable fields.
        val alpha=if(legibleBackdrop && c.preset=="raycast")1f
            else if(legibleBackdrop)maxOf(.96f,opacity(c)) else opacity(c)
        return if(dark && c.preset!="glass") listOf(Color(0xFF222222).copy(alpha=alpha),Color(0xFF181818).copy(alpha=alpha))
        else if(dark) listOf(Color(0xFF20212B).copy(alpha=alpha),Color(0xFF171921).copy(alpha=alpha))
        else listOf(Color(0xFFFAFAFC).copy(alpha=alpha),Color(0xFFF1F2F7).copy(alpha=alpha))
    }
    fun secondary(c:Config,dark:Boolean)=if(dark) {if(opacity(c)<.80f)Color(0xFFE3E5ED) else darkSecondary} else {if(opacity(c)<.85f)Color(0xFF373C48) else lightSecondary}
    fun actionOpacity(c:Config)=c.actionOpacity.takeIf{it.isFinite()}?.coerceIn(minimumOpacity,1f) ?: 1f
    fun settingsOpacity(c:Config)=c.settingsOpacity.takeIf{it.isFinite()}?.coerceIn(minimumOpacity,1f) ?: .99f
    fun actionColor(c:Config,dark:Boolean)=
        (if(c.focusInterface){if(dark)Color(0xFF26282C) else Color(0xFFFAF8F3)} else if(dark && c.preset=="glass")Color(0xFF20212B) else if(dark)actionSurface else paper).copy(alpha=actionOpacity(c))
    fun selectedColors(dark:Boolean,amount:Float=1f)=if(dark)
        listOf(Color(0xFFC7CBD4).copy(alpha=.17f*amount),Color(0xFFBFC3CC).copy(alpha=.11f*amount))
        else listOf(.09f,.05f).map{Color.Black.copy(alpha=it*amount)}
    fun selectionDuration(c:Config)=when(c.movement){"precise"->55;"liquid_immersive"->125;else->90}
}
private fun opacityForFocus(c:Config,legible:Boolean)=if(legible)maxOf(.90f,GlassTokens.opacity(c)) else GlassTokens.opacity(c)
@Composable fun Modifier.glassSurface(c:Config, legibleBackdrop:Boolean=false):Modifier {
    val colors=MaterialTheme.colorScheme
    val premium=LocalPremiumReview.current
    val shape=RoundedCornerShape(if(premium)26.dp else GlassTokens.windowRadius)
    val dark=colors.surface.luminance()<.5f
    val material=if(c.focusInterface)listOf(colors.surface.copy(alpha=opacityForFocus(c,legibleBackdrop)),colors.surfaceContainerLow.copy(alpha=opacityForFocus(c,legibleBackdrop))) else if(premium)listOf(colors.surface.copy(alpha=1f),colors.surfaceContainerLow.copy(alpha=1f))
        else GlassTokens.windowColors(c,dark,legibleBackdrop)
    return shadow(if(c.focusInterface)c.shadowSize.dp else 18.dp,shape,clip=false,
            ambientColor=Color.Black.copy(alpha=.18f),spotColor=Color.Black.copy(alpha=.30f))
        .clip(shape)
        .background(Brush.verticalGradient(material))
        .border(if(premium).75.dp else 1.dp,(if(premium)colors.outline else if(dark)Color.White else Color.Black).copy(alpha=if(premium).48f else if(dark).14f else .10f),shape)
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

