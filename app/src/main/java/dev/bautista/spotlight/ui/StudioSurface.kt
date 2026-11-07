package dev.bautista.spotlight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/** Focus finish only. Decide from the allocated width, never physical screen size.
 * Narrow/enlarged-text layouts retain their original content budget and hit targets. */
@Composable internal fun StudioSurface(kind:String,modifier:Modifier=Modifier,content:@Composable ()->Unit) {
 if(!LocalFocusUi.current){Box(modifier){content()};return}
 BoxWithConstraints(modifier) {
  val scale=(LocalDensity.current.fontScale*GlassTokens.textScale(LocalAppearance.current)).coerceAtLeast(1f)
  val room=maxWidth>= (if(kind=="search")360.dp else 420.dp)*scale
  val colors=MaterialTheme.colorScheme
  val shape=RoundedCornerShape(if(kind=="search")10.dp else 14.dp)
  val gap=if(room)if(kind=="search")8.dp else 6.dp else 0.dp
  Box(Modifier.fillMaxWidth().padding(gap)
   .background(if(kind=="search")colors.surfaceContainerLow else colors.surfaceContainerHigh,shape)
   .border(.75.dp,colors.outlineVariant.copy(alpha=.65f),shape)) {content()}
 }
}
