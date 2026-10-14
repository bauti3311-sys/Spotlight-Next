package dev.bautista.spotlight.data

object WindowSizePolicy {
    fun width(screenDp:Float,scale:Float,expanded:Boolean=false):Int {
        val fraction=.55f+.37f*scale.coerceIn(0f,1f)
        return (screenDp*(fraction+if(expanded).04f else 0f)).toInt().coerceIn(380,1100)
    }
    fun displayScale(c:Config,screenDp:Int,landscape:Boolean):Float =
        if(c.windowScale>=0f)c.windowScale.coerceIn(0f,1f)
        else if(OverlayWidthPolicy.configured(c,landscape)<=0).5f
        else ((OverlayWidthPolicy.configured(c,landscape)/screenDp.toFloat()-.55f)/.37f).coerceIn(0f,1f)
}
