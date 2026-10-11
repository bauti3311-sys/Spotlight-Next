package dev.bautista.spotlight.overlay

import android.content.Context
import android.view.MotionEvent
import android.widget.FrameLayout

/** Owns only the window events. Search content and accessible close action live in Compose. */
internal class WindowFocusHost(
    context: Context,
    private val onOutside: () -> Unit,
    private val onFocus: (Boolean) -> Unit,
) : FrameLayout(context) {
    init {
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        clipChildren = false
        clipToPadding = false
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        onFocus(hasWindowFocus)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_OUTSIDE) {
            onOutside()
            return true
        }
        return super.dispatchTouchEvent(event)
    }
}
