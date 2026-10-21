package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*

@Composable fun ContextSplitPanel(height:Dp,left:@Composable ()->Unit,right:@Composable ()->Unit,scrollInspector:Boolean=false) {
    var fraction by remember{mutableFloatStateOf(.62f)}
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width=with(LocalDensity.current){maxWidth.toPx()}.coerceAtLeast(1f)
        val layout=dev.bautista.spotlight.overlay.OverlayLayoutPolicy.resolve(
            maxWidth.value, Float.MAX_VALUE, LocalDensity.current.fontScale * LocalAppearance.current.textScale)
        val effectiveFraction=layout.fraction(fraction)
        Row(Modifier.fillMaxWidth().then(if(scrollInspector)Modifier.heightIn(max=height) else Modifier)){
            Box(Modifier.weight(effectiveFraction).testTag("results-pane")){left()}
            Box(Modifier.width(12.dp).height(height).semantics {
                contentDescription="Divisor ajustable del panel contextual"
                stateDescription="${(effectiveFraction*100).toInt()}% resultados"
                customActions=listOf(CustomAccessibilityAction("Ampliar resultados"){fraction=layout.fraction(effectiveFraction+.05f);true},CustomAccessibilityAction("Ampliar vista previa"){fraction=layout.fraction(effectiveFraction-.05f);true})
            }.pointerInput(width,layout){detectDragGestures{change,amount->change.consume();fraction=layout.fraction(layout.fraction(fraction)+amount.x/width)}},contentAlignment=androidx.compose.ui.Alignment.Center){
                Box(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.onSurface.copy(alpha=.07f)))
                Box(Modifier.width(3.dp).height(32.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha=.18f)))
            }
            Column(Modifier.weight(1-effectiveFraction).testTag("context-inspector")
                .then(if(scrollInspector)Modifier.heightIn(max=height).verticalScroll(rememberScrollState()) else Modifier)
                ){right()}
        }
    }
}
