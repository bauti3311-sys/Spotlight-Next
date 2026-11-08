package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.data.Config

/** Presentation only. One Android window, existing slots and executors; no independent focus host. */
internal val LocalPuenteUi=staticCompositionLocalOf{false}

@Composable internal fun PuenteOverlayTheme(config:Config,content:@Composable ()->Unit) {
 if(!config.focusInterface){content();return}
 val base=MaterialTheme.colorScheme
 val dark=base.surface.luminance()<.5f
 val ice=if(dark)base.copy(surface=Color(0xFF182B37),surfaceContainerLow=Color(0xFF152632),surfaceContainerHigh=Color(0xFF203744),
  onSurface=Color(0xFFE4F3F7),onSurfaceVariant=Color(0xFFB0C7D1),outlineVariant=Color(0xFF476674))
 else base.copy(surface=Color(0xFFF5FCFE),surfaceContainerLow=Color(0xFFEDF7F9),surfaceContainerHigh=Color(0xFFE6F3F6),
  onSurface=Color(0xFF203C4A),onSurfaceVariant=Color(0xFF526E7B),outlineVariant=Color(0xFFBBCFD6))
 val palette=if(config.accent=="blue")PremiumMaterial.accent(ice,if(dark)Color(0xFF8CDAEA)else Color(0xFF17758C)) else ice
 MaterialTheme(colorScheme=palette,content=content)
}

@Composable private fun PuenteSurface(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit) {
 val colors=MaterialTheme.colorScheme
 val shape=RoundedCornerShape(18.dp)
 Column(modifier.shadow(10.dp,shape,clip=false).clip(shape)
  .background(colors.surface.copy(alpha=.98f))
  .border(.75.dp,colors.outlineVariant.copy(alpha=.65f),shape),content=content)
}

/** No imposed content height: each surface wraps up to the supplied viewport budget.
 * Compact layouts keep search/footer visible and allocate the remainder to a scrolling inspector. */
@Composable internal fun PuenteFrame(
 maxHeight:Dp,split:Boolean,inspectorKey:Any?,
 header:@Composable ()->Unit,results:@Composable ()->Unit,footer:@Composable ()->Unit,
 inspector:(@Composable ()->Unit)?,inspectorHeader:@Composable ()->Unit,
 modifier:Modifier=Modifier,
) {
 val scale=(LocalDensity.current.fontScale*GlassTokens.textScale(LocalAppearance.current)).coerceAtLeast(1f)
 val finderHost=remember{movableContentOf<@Composable ()->Unit>{content->content()}}
 val finder:@Composable ()->Unit = { finderHost {
  PuenteSurface(Modifier.fillMaxWidth().testTag("puente-finder")) {
   header();results();footer()
  }
 } }
 val destination:@Composable ()->Unit = {
  PuenteSurface(Modifier.fillMaxWidth().heightIn(max=maxHeight).testTag("puente-destination")) {
   inspectorHeader()
   key(inspectorKey) {
    Column(Modifier.weight(1f,fill=false).fillMaxWidth().testTag("inspector-scroll")
     .verticalScroll(rememberScrollState())) {inspector?.invoke()}
   }
  }
 }
 CompositionLocalProvider(LocalPuenteUi provides true) {
  Box(modifier.fillMaxWidth().testTag("puente-frame"),contentAlignment=Alignment.TopCenter) {
  if(split && inspector!=null)ContextSplitPanel(maxHeight,
   left={finder()},right={Box(Modifier.padding(top=20.dp)){destination()}},
   initialFraction=.44f,detached=true)
  else Column(Modifier.widthIn(max=if(inspector==null)560.dp*scale else Dp.Infinity).fillMaxWidth().heightIn(max=maxHeight)
   .testTag("compact-inspector-scroll"),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   finder()
   if(inspector!=null)Box(Modifier.weight(1f,fill=false)){destination()}
  }
  }
 }
}
