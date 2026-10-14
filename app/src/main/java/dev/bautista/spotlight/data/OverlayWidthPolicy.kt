package dev.bautista.spotlight.data

/** Existing zero values keep their meaning: automatic general / inherited horizontal. */
object OverlayWidthPolicy {
    fun configured(c:Config,landscape:Boolean):Int =
        if(landscape && c.landscapeWidth>0)c.landscapeWidth else c.width

    fun automatic(c:Config,enabled:Boolean):Config = when {
        enabled && c.width>0 -> c.copy(width=0,manualWidth=c.width)
        !enabled && c.width<=0 -> c.copy(width=c.manualWidth)
        else -> c
    }
    fun manual(c:Config,width:Int):Config =
        c.copy(width=width.coerceIn(380,1100),manualWidth=width.coerceIn(380,1100))

    fun horizontal(c:Config,enabled:Boolean):Config = when {
        enabled && c.landscapeWidth<=0 -> c.copy(landscapeWidth=(if(c.width>0)c.width else c.manualWidth).coerceIn(380,1100))
        !enabled -> c.copy(landscapeWidth=0)
        else -> c
    }
}
