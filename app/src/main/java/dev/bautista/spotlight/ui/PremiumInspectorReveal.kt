package dev.bautista.spotlight.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Paint only: no height animation, duplicate outgoing forms, focus moves or state restoration. */
@Composable internal fun PremiumInspectorReveal(identity:Any?,motion:Boolean,content:@Composable ()->Unit) {
    val opacity=remember(identity,motion){Animatable(if(motion)0f else 1f)}
    LaunchedEffect(opacity){if(motion)opacity.animateTo(1f,tween(110))}
    Box(Modifier.graphicsLayer{alpha=opacity.value}) {content()}
}
