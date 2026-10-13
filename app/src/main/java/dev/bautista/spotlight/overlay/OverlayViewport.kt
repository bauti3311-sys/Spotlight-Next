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
    val frameOcclusion: Int = 0,
) {
    // IME and navigation insets overlap; the navigation bar is already outside this frame.
    fun keyboardOcclusion(bottomInset: Int) = maxOf((bottomInset - excludedBottom).coerceAtLeast(0), frameOcclusion)

    companion object {
        fun read(wm: WindowManager, resources: Resources, legacySystemBottom: Int, host: View? = null, bottomInset: Int = 0): OverlayViewport {
            if (Build.VERSION.SDK_INT >= 30) {
                runCatching {
                    val screen = fromWindowMetrics(wm.currentWindowMetrics, resources.displayMetrics.density)
                    val frame = Rect()
                    host?.getWindowVisibleDisplayFrame(frame)
                    if (frame.width() > 0 && frame.height() > 0) {
                        screen.copy(width = minOf(screen.width, frame.width()),
                            frameOcclusion = maxOf(screen.frameOcclusion,
                                (screen.height - frame.height()).coerceAtLeast(0)))
                    } else screen
                }.getOrNull()?.let { return it }
            }
            // Legacy app metrics are a startup fallback; the attached host frame is authoritative.
            val dm = DisplayMetrics().apply { setTo(resources.displayMetrics) }
            @Suppress("DEPRECATION")
            runCatching { wm.defaultDisplay.getMetrics(dm) }
            val frame = Rect()
            host?.getWindowVisibleDisplayFrame(frame)
            if (frame.width() > 0 && frame.height() > 0) {
                val real = DisplayMetrics().apply { setTo(dm) }
                @Suppress("DEPRECATION")
                runCatching { wm.defaultDisplay.getRealMetrics(real) }
                val visibleHeight = frame.height().coerceAtMost(
                    (real.heightPixels - frame.top).coerceAtLeast(1)
                )
                // Local IME insets can be zero after WM has resized an overlay.
                // Infer missing bottom space from the fitted global display frame.
                val fullHeight = (real.heightPixels - frame.top - legacySystemBottom)
                    .coerceAtLeast(visibleHeight)
                return OverlayViewport(minOf(frame.width(), real.widthPixels), fullHeight, dm.density,
                    legacySystemBottom, real.widthPixels > real.heightPixels,
                    (fullHeight - visibleHeight).coerceAtLeast(0))
            }
            return OverlayViewport(dm.widthPixels, dm.heightPixels, dm.density, legacySystemBottom)
        }

        @androidx.annotation.RequiresApi(30)
        fun fromWindowMetrics(metrics: WindowMetrics, density: Float): OverlayViewport {
            val bounds = metrics.bounds
            require(bounds.width() > 0 && bounds.height() > 0)
            val safe = metrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
            )
            return OverlayViewport(
                (bounds.width() - safe.left - safe.right).coerceAtLeast(1),
                (bounds.height() - safe.top - safe.bottom).coerceAtLeast(1),
                density, safe.bottom, bounds.width() > bounds.height(),
                (metrics.windowInsets.getInsets(WindowInsets.Type.ime()).bottom - safe.bottom)
                    .coerceAtLeast(0)
            )
        }
    }
}
