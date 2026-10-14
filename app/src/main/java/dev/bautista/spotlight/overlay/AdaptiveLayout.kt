package dev.bautista.spotlight.overlay

import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.data.OverlayWidthPolicy
import dev.bautista.spotlight.data.WindowSizePolicy

data class OverlayBounds(val width:Int,val top:Int,val heightDp:Float)
object AdaptiveLayout {
    fun bounds(width:Int,height:Int,density:Float,ime:Int,c:Config,expanded:Boolean=false,landscape:Boolean=width>height):OverlayBounds {
        val phone=width/density<600;val margin=if(phone && c.adaptive)12 else 16
        val position=if(landscape && c.landscapePosition>=0)c.landscapePosition.coerceIn(.04f,.35f) else c.position.coerceIn(.04f,.35f)
        val configuredWidth=OverlayWidthPolicy.configured(c,landscape)
        val preferredTop=(height*if(phone && c.adaptive)minOf(position,.08f) else position).toInt()
        // Keep room for the capsule above the keyboard without changing saved position.
        val top=minOf(preferredTop,((height-ime)-124f*density).coerceAtLeast(0f).toInt())
        val resolvedWidth=if(c.windowScale>=0f) WindowSizePolicy.width(width/density,c.windowScale,expanded) else if(configuredWidth<=0)(width/density*(if(phone && c.adaptive)1f else if(expanded).9f else .8f)).toInt() else configuredWidth
        return OverlayBounds(minOf((resolvedWidth.coerceIn(300,1100)*density).toInt(),width-(margin*2*density).toInt()).coerceAtLeast(1),top,((height-top-ime)/density-24).coerceAtLeast(0f))
    }
}
