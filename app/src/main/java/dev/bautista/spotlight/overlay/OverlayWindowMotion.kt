package dev.bautista.spotlight.overlay

import android.view.View

/** Changes drawing properties only; window geometry and input focus remain immediate. */
internal object OverlayWindowMotion {
    fun prepare(host: View, enabled: Boolean) {
        host.alpha = if (enabled) .92f else 1f
        host.translationY = if (enabled) -2f * host.resources.displayMetrics.density else 0f
    }

    fun open(host: View, enabled: Boolean) {
        if (enabled) host.animate().alpha(1f).translationY(0f).setDuration(140L).start()
    }

    fun close(host: View, enabled: Boolean, finished: () -> Unit) {
        host.animate().cancel()
        if (enabled) host.animate().alpha(0f).scaleX(.99f).scaleY(.99f)
            .translationY(-3f * host.resources.displayMetrics.density)
            .setDuration(100L).withEndAction(finished).start()
        else finished()
    }
}
