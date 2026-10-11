package dev.bautista.spotlight.ui

import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

val LocalMotion=staticCompositionLocalOf{true}
@Composable fun rememberMotionAllowed(requested:Boolean):Boolean {
    val context=LocalContext.current
    fun allowed()=runCatching{Settings.Global.getFloat(context.contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE,1f)>0}.getOrDefault(false)
    var system by remember(context){mutableStateOf(allowed())}
    DisposableEffect(context){val observer=object:android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())){override fun onChange(selfChange:Boolean){system=allowed()}};runCatching{context.contentResolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),false,observer)};onDispose{runCatching{context.contentResolver.unregisterContentObserver(observer)}}}
    return requested && system
}
@Composable fun Modifier.revealMotion(key: Any, requested: Boolean): Modifier {
    val context = LocalContext.current
    val enabled = requested && LocalMotion.current
    val progress = remember { Animatable(if(enabled) 0f else 1f) }
    val offset = with(LocalDensity.current){6.dp.toPx()}
    LaunchedEffect(key,enabled) {
        if(enabled) {progress.snapTo(0f);progress.animateTo(1f,tween(160,easing=FastOutSlowInEasing))}
        else progress.snapTo(1f)
    }
    return graphicsLayer {
        alpha=progress.value; translationY=(1-progress.value)*offset
        scaleX=.99f+.01f*progress.value;scaleY=scaleX
    }
}
