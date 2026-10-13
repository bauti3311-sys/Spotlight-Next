package dev.bautista.spotlight.overlay

import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.graphics.Rect
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics

/** Dimensions of WindowManager's fitted parent, not the WRAP_CONTENT overlay itself. */
internal data class OverlayViewport(
    val width: Int,
    val height: Int,
    val density: Float,
    val excludedBottom: Int,
    val landscape: Boolean = width > height,
) {
    // IME and navigation insets overlap; the navigation bar is already outside this frame.
    fun keyboardOcclusion(bottomInset: Int) = (bottomInset - excludedBottom).coerceAtLeast(0)

    companion object {
        fun read(wm: WindowManager, resources: Resources, legacySystemBottom: Int, host: View? = null, bottomInset: Int = 0): OverlayViewport {
            if (Build.VERSION.SDK_INT >= 30) {
                runCatching { fromWindowMetrics(wm.currentWindowMetrics, resources.displayMetrics.density) }
                    .getOrNull()?.let { return it }
            }
            // Legacy app metrics are a startup fallback; the attached host frame is authoritative.
            val dm = DisplayMetrics().apply { setTo(resources.displayMetrics) }
            @Suppress("DEPRECATION")
            runCatching { wm.defaultDisplay.getMetrics(dm) }
            val frame = Rect()
            host?.getWindowVisibleDisplayFrame(frame)
            if (frame.width() > 0 && frame.height() > 0) {
                // This legacy frame already avoids decor and the visible keyboard.
                // Restore the unobscured height before subtracting IME once below.
                val occlusion = (bottomInset - legacySystemBottom).coerceAtLeast(0)
                return OverlayViewport(frame.width(), frame.height() + occlusion, dm.density,
                    legacySystemBottom, dm.widthPixels > dm.heightPixels)
            }
            return OverlayViewport(dm.widthPixels, dm.heightPixels, dm.density, legacySystemBottom)
        }

        @androidx.annotation.RequiresApi(30)
        fun fromWindowMetrics(metrics: WindowMetrics, density: Float): OverlayViewport {
            val bounds = metrics.bounds
            val safe = metrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
            )
            return OverlayViewport(
                (bounds.width() - safe.left - safe.right).coerceAtLeast(1),
                (bounds.height() - safe.top - safe.bottom).coerceAtLeast(1),
                density, safe.bottom, bounds.width() > bounds.height()
            )
        }
    }
}
