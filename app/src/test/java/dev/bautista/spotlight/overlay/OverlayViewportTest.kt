package dev.bautista.spotlight.overlay

import android.graphics.Insets
import android.graphics.Rect
import android.view.View
import android.view.WindowManager
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import android.view.WindowInsets
import android.view.WindowMetrics
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class OverlayViewportTest {
    @Test @Config(sdk = [28], qualifiers = "w800dp-h1280dp", application = Application::class)
    fun legacyVisibleFrameDoesNotSubtractKeyboardTwice() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val host = object : View(context) {
            override fun getWindowVisibleDisplayFrame(outRect: Rect) { outRect.set(0, 24, 800, 980) }
        }
        val screen = OverlayViewport.read(context.getSystemService(WindowManager::class.java),
            context.resources, 48, host, 300)
        assertEquals(800, screen.width)
        assertEquals(1208, screen.height)
        assertEquals(956, screen.height - screen.keyboardOcclusion(300))
    }

    @Test @Config(sdk = [28], qualifiers = "w800dp-h1280dp", application = Application::class)
    fun resizedLegacyOverlayStillDetectsKeyboardWhenLocalInsetIsZero() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val host = object : View(context) {
            override fun getWindowVisibleDisplayFrame(outRect: Rect) { outRect.set(0, 24, 800, 980) }
        }
        val screen = OverlayViewport.read(context.getSystemService(WindowManager::class.java),
            context.resources, 48, host, 0)
        assertEquals(1208, screen.height)
        assertEquals(252, screen.keyboardOcclusion(0))
    }

    @Test fun modernMetricsSupplyKeyboardEvenWhenOverlayLocalInsetIsZero() {
        val insets = WindowInsets.Builder()
            .setInsetsIgnoringVisibility(WindowInsets.Type.systemBars(), Insets.of(0, 24, 0, 48))
            .setInsets(WindowInsets.Type.ime(), Insets.of(0, 0, 0, 300))
            .setVisible(WindowInsets.Type.ime(), true).build()
        val screen = OverlayViewport.fromWindowMetrics(WindowMetrics(Rect(0, 0, 1280, 800), insets), 1f)
        assertEquals(728, screen.height)
        assertEquals(252, screen.keyboardOcclusion(0))
    }

    @Test @Config(application = Application::class)
    fun modernVisibleFrameCoversMissingGlobalAndLocalImeInsets() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val insets = WindowInsets.Builder().setInsetsIgnoringVisibility(
            WindowInsets.Type.systemBars(), Insets.of(0, 24, 0, 48)).build()
        val actual = context.getSystemService(WindowManager::class.java)
        val wm = object : WindowManager by actual {
            override fun getCurrentWindowMetrics() = WindowMetrics(Rect(0, 0, 1280, 800), insets)
        }
        val host = object : View(context) {
            override fun getWindowVisibleDisplayFrame(outRect: Rect) { outRect.set(0, 24, 1280, 500) }
        }
        val screen = OverlayViewport.read(wm, context.resources, 48, host, 0)
        assertEquals(728, screen.height)
        assertEquals(252, screen.keyboardOcclusion(0))
    }

    @Test fun keyboardCannotChooseHorizontalWidthOnPortraitTablet() {
        val c = AppConfig(width = 680, landscapeWidth = 460)
        val screen = OverlayViewport(800, 600, 1f, 48, landscape = false)
        val bounds = AdaptiveLayout.bounds(screen.width, screen.height, screen.density, 0, c,
            landscape = screen.landscape)
        assertEquals(680, bounds.width)
        assertEquals(460, AdaptiveLayout.bounds(800, 600, 1f, 0, c, landscape = true).width)
    }

    @Test fun lowPositionMovesAboveKeyboardInsteadOfInventingAvailableSpace() {
        val c = AppConfig(position = .35f, landscapePosition = .35f)
        val bounds = AdaptiveLayout.bounds(1280, 800, 1f, 600, c)
        assertTrue(bounds.top + bounds.heightDp + 24f <= 200f)
        assertEquals(AdaptiveLayout.bounds(1280, 800, 1f, 0, c).width, bounds.width)
    }

    @Test fun usableFrameExcludesBarsAndCutoutInBothOrientations() {
        for ((width, height) in listOf(390 to 844, 844 to 390, 800 to 1280, 1280 to 800)) {
            val insets = WindowInsets.Builder().setInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout(),
                Insets.of(40, 24, 16, 48)
            ).build()
            val screen = OverlayViewport.fromWindowMetrics(WindowMetrics(Rect(0, 0, width, height), insets), 1f)
            assertEquals(width - 56, screen.width)
            assertEquals(height - 72, screen.height)
            assertEquals(0, screen.keyboardOcclusion(48))
            assertEquals(252, screen.keyboardOcclusion(300))
            val c = AppConfig(width = 820, landscapeWidth = 460, density = "spacious")
            val visible = AdaptiveLayout.bounds(screen.width, screen.height, screen.density, screen.keyboardOcclusion(300), c)
            val hidden = AdaptiveLayout.bounds(screen.width, screen.height, screen.density, screen.keyboardOcclusion(48), c)
            assertEquals(hidden.width, visible.width)
            assertTrue(visible.width <= screen.width)
            assertTrue(visible.heightDp <= hidden.heightDp)
            assertEquals(820, c.width)
            assertEquals(460, c.landscapeWidth)
        }
    }
}
