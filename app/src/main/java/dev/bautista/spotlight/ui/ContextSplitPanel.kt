package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*

@Composable fun ContextSplitPanel(height:Dp,left:@Composable ()->Unit,right:@Composable ()->Unit,scrollInspector:Boolean=false,
    inspectorKey:Any?=null,inspectorHeader:(@Composable ()->Unit)?=null) {
    ContextSplitPanel(height,left,right,scrollInspector,inspectorKey,inspectorHeader,initialFraction=.62f)
}

@Composable fun ContextSplitPanel(height:Dp,left:@Composable ()->Unit,right:@Composable ()->Unit,scrollInspector:Boolean=false,
    inspectorKey:Any?=null,inspectorHeader:(@Composable ()->Unit)?=null,initialFraction:Float) {
    var fraction by remember(initialFraction){mutableFloatStateOf(initialFraction.takeIf{it.isFinite()}?.coerceIn(.2f,.8f) ?: .62f)}
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width=with(LocalDensity.current){maxWidth.toPx()}.coerceAtLeast(1f)
        val layout=dev.bautista.spotlight.overlay.OverlayLayoutPolicy.resolve(
            maxWidth.value, Float.MAX_VALUE, LocalDensity.current.fontScale * LocalAppearance.current.textScale)
        val effectiveFraction=layout.fraction(fraction)
        // Measure content before the divider. A fixed-height divider used to force
        // empty space into short inspectors and a separate cap clipped long ones.
        Layout(modifier=Modifier.fillMaxWidth().heightIn(max=height),content={
            Box(Modifier.testTag("results-pane")){left()}
            Box(Modifier.width(12.dp).semantics {
                contentDescription="Divisor ajustable del panel contextual"
                stateDescription="${(effectiveFraction*100).toInt()}% resultados"
                customActions=listOf(CustomAccessibilityAction("Ampliar resultados"){fraction=layout.fraction(effectiveFraction+.05f);true},CustomAccessibilityAction("Ampliar vista previa"){fraction=layout.fraction(effectiveFraction-.05f);true})
            }.pointerInput(width,layout){detectDragGestures{change,amount->change.consume();fraction=layout.fraction(layout.fraction(fraction)+amount.x/width)}},contentAlignment=androidx.compose.ui.Alignment.Center){
                Box(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.onSurface.copy(alpha=.07f)))
                Box(Modifier.width(3.dp).height(32.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha=.18f)))
            }
            Column(Modifier.testTag("context-inspector").heightIn(max=height)) {
                inspectorHeader?.invoke()
                key(inspectorKey) {
                    if(scrollInspector)Column(Modifier.weight(1f,fill=false).fillMaxWidth()
                        .testTag("inspector-scroll").verticalScroll(rememberScrollState())) { right() }
                    else right()
                }
            }
        }) { measurables, constraints ->
            val dividerWidth=12.dp.roundToPx().coerceAtMost(constraints.maxWidth)
            val usable=(constraints.maxWidth-dividerWidth).coerceAtLeast(0)
            val leftWidth=(usable*effectiveFraction).toInt()
            val rightWidth=usable-leftWidth
            val leftPane=measurables[0].measure(Constraints(
                minWidth=leftWidth,maxWidth=leftWidth,maxHeight=constraints.maxHeight))
            val rightPane=measurables[2].measure(Constraints(
                minWidth=rightWidth,maxWidth=rightWidth,maxHeight=constraints.maxHeight))
            val contentHeight=constraints.constrainHeight(maxOf(leftPane.height,rightPane.height))
            val divider=measurables[1].measure(Constraints.fixed(dividerWidth,contentHeight))
            layout(constraints.maxWidth,contentHeight) {
                leftPane.placeRelative(0,0)
                divider.placeRelative(leftWidth,0)
                rightPane.placeRelative(leftWidth+dividerWidth,0)
            }
        }
    }
}

