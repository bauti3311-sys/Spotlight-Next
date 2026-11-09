package dev.bautista.spotlight.data

object WindowSizePolicy {
    fun width(screenDp:Float,scale:Float,expanded:Boolean=false):Int {
        // Map the whole slider into the usable range instead of clamping most of
        // a phone to 380dp or the upper part of a tablet to 1100dp.
        val upper=minOf(screenDp*.92f,1100f).coerceAtLeast(1f)
        val lower=minOf(if(screenDp<600f)300f else maxOf(380f,screenDp*.55f),upper)
        val choice=(scale.takeIf{it.isFinite()} ?: .5f).coerceIn(0f,1f)
        return (lower+(upper-lower)*choice+if(expanded)screenDp*.04f else 0f).toInt().coerceIn(1,1100)
    }
    fun displayScale(c:Config,screenDp:Int,landscape:Boolean):Float {
        if(c.windowScale>=0f)return c.windowScale.coerceIn(0f,1f)
        val configured=OverlayWidthPolicy.configured(c,landscape)
        if(configured<=0)return .5f
        val low=width(screenDp.toFloat(),0f);val high=width(screenDp.toFloat(),1f)
        return if(high<=low).5f else ((configured-low).toFloat()/(high-low)).coerceIn(0f,1f)
    }
}
