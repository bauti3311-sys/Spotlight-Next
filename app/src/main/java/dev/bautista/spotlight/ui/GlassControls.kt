package dev.bautista.spotlight.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.*

/** Material's slider keeps native gestures, keyboard, range and accessibility semantics. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun GlassSlider(value:Float,onValueChange:(Float)->Unit,modifier:Modifier=Modifier,valueRange:ClosedFloatingPointRange<Float> = 0f..1f) {
 val colors=MaterialTheme.colorScheme
 val rtl=LocalLayoutDirection.current==LayoutDirection.Rtl
 Slider(value=value,onValueChange=onValueChange,modifier=modifier.heightIn(min=48.dp),valueRange=valueRange,
  thumb={Box(Modifier.size(18.dp).background(Color.White,CircleShape).border(1.dp,Color.Black.copy(alpha=.09f),CircleShape))},
  track={state->Canvas(Modifier.fillMaxWidth().height(4.dp)) {
   val fraction=((state.value-valueRange.start)/(valueRange.endInclusive-valueRange.start)).coerceIn(0f,1f)
   drawLine(colors.onSurface.copy(alpha=.14f),Offset(0f,size.height/2),Offset(size.width,size.height/2),strokeWidth=4.dp.toPx(),cap=StrokeCap.Round)
   val start=if(rtl)size.width else 0f;val end=if(rtl)size.width*(1f-fraction) else size.width*fraction
   drawLine(colors.primary,Offset(start,size.height/2),Offset(end,size.height/2),strokeWidth=4.dp.toPx(),cap=StrokeCap.Round)
  }})
}
