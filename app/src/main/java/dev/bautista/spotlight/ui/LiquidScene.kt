package dev.bautista.spotlight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.R
import dev.bautista.spotlight.search.Result

/** Vertical review only. Not persisted and never enabled by a production entry point. */
internal enum class LiquidTypeface { Inter, Geist }
internal val LocalLiquidScene = staticCompositionLocalOf<LiquidTypeface?> { null }
internal val Geist = FontFamily(Font(R.font.geist_regular,FontWeight.Normal),Font(R.font.geist_semibold,FontWeight.SemiBold))
@Composable internal fun liquidFont() = if(LocalLiquidScene.current==LiquidTypeface.Geist)Geist else Inter
internal fun liquidPalette(base:ColorScheme,dark:Boolean):ColorScheme = base.copy(
    background=if(dark)Color(0xFF111214) else Color(0xFFE9E6E0),
    surface=if(dark)Color(0xFF232529) else Color(0xFFF8F6F1),
    onSurface=if(dark)Color(0xFFF3F1ED) else Color(0xFF242627),
    onSurfaceVariant=if(dark)Color(0xFFB8BABE) else Color(0xFF595C60),
    surfaceContainerLow=if(dark)Color(0xFF1B1D20) else Color(0xFFEFECE6),
    surfaceContainerHigh=if(dark)Color(0xFF303338) else Color(0xFFFFFEFA),
    outline=if(dark)Color(0xFF777B82) else Color(0xFF96918B),
    outlineVariant=if(dark)Color(0xFF3D4147) else Color(0xFFD8D3CB),
    primary=if(dark)Color(0xFFB9C4D3) else Color(0xFF445367))

/** One contained plane, no card around each property/action; no blur or runtime shader. */
@Composable internal fun Modifier.liquidInspector():Modifier {
    if(LocalLiquidScene.current==null)return this
    val c=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(20.dp)
    return padding(start=8.dp,end=12.dp,bottom=10.dp).shadow(8.dp,shape)
        .background(Brush.verticalGradient(listOf(c.surfaceContainerHigh,c.surfaceContainerLow)),shape)
        .padding(horizontal=10.dp,vertical=12.dp)
}
@Composable internal fun LiquidApplicationIdentity(item:Result) {
    val c=MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().testTag("liquid-app-identity").padding(top=8.dp,bottom=14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("APLICACIÓN INSTALADA",style=MaterialTheme.typography.labelSmall,letterSpacing=1.8.sp,color=c.onSurfaceVariant)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val compact=maxWidth<360.dp
            val identity:@Composable ()->Unit = {
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text(item.title,Modifier.testTag("inspector-title").semantics{heading()},
                        style=if(compact)MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineLarge,
                        fontWeight=FontWeight.SemiBold,maxLines=2)
                    Text("Navegador",style=MaterialTheme.typography.bodyMedium,color=c.onSurfaceVariant)
                }
            }
            if(compact)Row(horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                ResultIcon(item,Modifier.size(48.dp))
                Box(Modifier.weight(1f)){identity()}
            } else Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                ResultIcon(item,Modifier.size(64.dp));identity()
            }
        }
    }
}
