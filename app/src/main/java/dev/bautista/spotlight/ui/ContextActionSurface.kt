package dev.bautista.spotlight.ui

import android.graphics.Rect
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dev.bautista.spotlight.overlay.ActionPopupHost

internal class ActionBackNavigation { var back:(()->Unit)?=null }
internal val LocalActionBackNavigation=staticCompositionLocalOf<ActionBackNavigation?>{null}

/** The empty anchor does not participate in palette measurement. Its bounded child
 * surface uses the parent's token and its own public touch rectangle, not fullscreen.
 */
@Composable internal fun BoxScope.ContextActionSurface(availableHeight:Dp,onDismiss:()->Unit,content:@Composable ()->Unit) {
    val studio=LocalFocusUi.current
    val anchor=LocalView.current
    val density=LocalDensity.current
    val parent=rememberCompositionContext()
    val navigation=remember{ActionBackNavigation()}
    val currentDismiss by rememberUpdatedState(onDismiss)
    val currentContent by rememberUpdatedState(content)
    var rect by remember{mutableStateOf<Rect?>(null)}
    var popup by remember{mutableStateOf<ActionPopupHost?>(null)}
    Box(Modifier.matchParentSize().onGloballyPositioned { coordinates ->
        val b=coordinates.boundsInWindow()
        val screen=IntArray(2);val window=IntArray(2)
        anchor.getLocationOnScreen(screen);anchor.getLocationInWindow(window)
        val left=(b.left+screen[0]-window[0]).toInt();val top=(b.top+screen[1]-window[1]).toInt()
        val next=Rect(left,top,left+b.width.toInt(),top+b.height.toInt())
        if(next!=rect)rect=next
    })
    val width=with(density){minOf(360.dp,(rect?.width() ?: 1).toDp()-8.dp).coerceAtLeast(1.dp).roundToPx()}
    val height=with(density){minOf(GlassTokens.actionPanelMaxHeight+16.dp,availableHeight-16.dp).coerceAtLeast(1.dp).roundToPx()}
    if(rect!=null) {
        DisposableEffect(anchor,parent,studio) {
            val host=ActionPopupHost(anchor.context,anchor,{currentDismiss()},{navigation.back?.invoke() ?: currentDismiss()},studio=studio)
            host.setParentCompositionContext(parent)
            anchor.findViewTreeLifecycleOwner()?.let{host.setViewTreeLifecycleOwner(it)}
            anchor.findViewTreeSavedStateRegistryOwner()?.let{host.setViewTreeSavedStateRegistryOwner(it)}
            anchor.findViewTreeViewModelStoreOwner()?.let{host.setViewTreeViewModelStoreOwner(it)}
            host.body={
                val maxHeight=with(LocalDensity.current){host.maximumHeight.toDp()}
                val dark=MaterialTheme.colorScheme.surface.luminance()<.5f
                CompositionLocalProvider(LocalActionBackNavigation provides navigation) {
                    Box(Modifier.heightIn(max=maxHeight).padding(8.dp)) {
                        Surface(Modifier.fillMaxWidth().heightIn(max=(maxHeight-16.dp).coerceAtLeast(0.dp))
                            .shadow(20.dp,RoundedCornerShape(16.dp),clip=false).testTag("action-surface"),
                            shape=RoundedCornerShape(16.dp),
                            color=GlassTokens.actionColor(LocalAppearance.current,dark),
                            border=BorderStroke(1.dp,MaterialTheme.colorScheme.onSurface.copy(alpha=.17f))) {currentContent()}
                    }
                }
            }
            host.show(rect!!,width,height);popup=host
            onDispose{popup=null;host.close()}
        }
        SideEffect {popup?.update(rect!!,width,height)}
    }
}
