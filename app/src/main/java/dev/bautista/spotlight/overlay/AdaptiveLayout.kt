package dev.bautista.spotlight.overlay

import dev.bautista.spotlight.data.Config

data class OverlayBounds(val width:Int,val top:Int,val heightDp:Float)
object AdaptiveLayout {
    fun bounds(width:Int,height:Int,density:Float,ime:Int,c:Config,expanded:Boolean=false):OverlayBounds {
        val phone=width/density<600;val margin=if(phone && c.adaptive)12 else 16
        val position=if(width>height && c.landscapePosition>=0)c.landscapePosition.coerceIn(.04f,.35f) else c.position.coerceIn(.04f,.35f)
        val configuredWidth=if(width>height && c.landscapeWidth>0)c.landscapeWidth else c.width
        val top=(height*if(phone && c.adaptive)minOf(position,.08f) else position).toInt()
        val resolvedWidth=if(configuredWidth<=0)(width/density*(if(phone && c.adaptive)1f else if(expanded).9f else .8f)).toInt() else configuredWidth
        return OverlayBounds(minOf((resolvedWidth.coerceIn(300,1100)*density).toInt(),width-(margin*2*density).toInt()).coerceAtLeast(1),top,((height-top-ime)/density-24).coerceAtLeast(100f))
    }
}
