package dev.bautista.spotlight.overlay

import dev.bautista.spotlight.data.Config

data class OverlayBounds(val width:Int,val top:Int,val heightDp:Float)
object AdaptiveLayout {
    fun bounds(width:Int,height:Int,density:Float,ime:Int,c:Config):OverlayBounds {
        val phone=width/density<600;val margin=if(phone && c.adaptive)12 else 16
        val top=(height*if(phone && c.adaptive)minOf(c.position,.08f) else c.position).toInt()
        return OverlayBounds(minOf((c.width*density).toInt(),width-(margin*2*density).toInt()).coerceAtLeast(1),top,((height-top-ime)/density-24).coerceAtLeast(100f))
    }
}
