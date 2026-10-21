package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/** Same-window overlay. matchParentSize excludes the menu from the palette's measurement. */
@Composable internal fun BoxScope.ContextActionSurface(onDismiss:()->Unit,content:@Composable ()->Unit) {
    BoxWithConstraints(Modifier.matchParentSize().zIndex(2f)) {
        Box(Modifier.matchParentSize().pointerInput(onDismiss){detectTapGestures{onDismiss()}})
        val available=(maxHeight-64.dp).coerceAtLeast(0.dp)
        val dark=MaterialTheme.colorScheme.surface.luminance()<.5f
        if(available>0.dp) Surface(Modifier.align(Alignment.BottomEnd).padding(end=8.dp,bottom=52.dp)
            .width(minOf(390.dp,(maxWidth-16.dp).coerceAtLeast(0.dp)))
            .height(minOf(GlassTokens.actionPanelMaxHeight,available))
            .shadow(18.dp,RoundedCornerShape(15.dp),clip=false)
            .testTag("action-surface"),
            shape=RoundedCornerShape(15.dp),
            color=if(dark)GlassTokens.actionSurface else MaterialTheme.colorScheme.surfaceContainerHigh,
            border=BorderStroke(1.dp,MaterialTheme.colorScheme.onSurface.copy(alpha=.09f))) {content()}
    }
}
