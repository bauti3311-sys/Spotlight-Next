package dev.bautista.spotlight.overlay

/** Measured overlay content, after outer padding/insets; never the device's screen class. */
object OverlayLayoutPolicy {
    const val dividerDp = 12f
    const val resultsMinimumDp = 400f
    const val inspectorMinimumDp = 280f
    enum class Mode { Compact, Intermediate, Expanded }
    data class Layout(val mode: Mode, val minFraction: Float, val maxFraction: Float) {
        val split: Boolean get() = mode == Mode.Expanded
        fun fraction(preferred: Float = .62f): Float = preferred.coerceIn(minFraction, maxFraction)
    }

    fun resolve(contentWidthDp: Float, availableHeightDp: Float, fontScale: Float = 1f): Layout {
        val scale = fontScale.coerceAtLeast(1f)
        val usable = (contentWidthDp - dividerDp).coerceAtLeast(1f)
        val minimumResults = resultsMinimumDp * scale
        val minimumInspector = inspectorMinimumDp * scale
        if (usable < minimumResults + minimumInspector || availableHeightDp < 240f * scale) {
            return Layout(if (contentWidthDp < 600f) Mode.Compact else Mode.Intermediate, .62f, .62f)
        }
        val minFraction = minimumResults / usable
        // At the exact breakpoint, Float rounding must not invert an otherwise valid range.
        return Layout(Mode.Expanded, minFraction, maxOf(minFraction, 1f - minimumInspector / usable))
    }
}
