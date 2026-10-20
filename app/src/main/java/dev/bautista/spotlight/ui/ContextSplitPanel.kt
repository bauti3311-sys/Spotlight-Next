package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*

@Composable fun ContextSplitPanel(height:Dp,left:@Composable ()->Unit,right:@Composable ()->Unit) {
    var fraction by remember{mutableFloatStateOf(.62f)}
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width=with(LocalDensity.current){maxWidth.toPx()}.coerceAtLeast(1f)
        Row(Modifier.fillMaxWidth()){
            Box(Modifier.weight(fraction)){left()}
            Box(Modifier.width(12.dp).height(height).semantics {
                contentDescription="Divisor ajustable del panel contextual"
                customActions=listOf(CustomAccessibilityAction("Ampliar resultados"){fraction=(fraction+.05f).coerceAtMost(.65f);true},CustomAccessibilityAction("Ampliar vista previa"){fraction=(fraction-.05f).coerceAtLeast(.35f);true})
            }.pointerInput(width){detectDragGestures{change,amount->change.consume();fraction=(fraction+amount.x/width).coerceIn(.35f,.65f)}},contentAlignment=androidx.compose.ui.Alignment.Center){
                Box(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.onSurface.copy(alpha=.07f)))
                Box(Modifier.width(3.dp).height(32.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha=.18f)))
            }
            Column(Modifier.weight(1-fraction).background(MaterialTheme.colorScheme.onSurface.copy(alpha=.018f))){right()}
        }
    }
}
