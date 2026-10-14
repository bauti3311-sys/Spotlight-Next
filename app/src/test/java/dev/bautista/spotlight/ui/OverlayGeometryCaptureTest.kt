package dev.bautista.spotlight.ui

import android.graphics.Insets
import android.graphics.Rect
import android.view.WindowInsets
import android.view.WindowMetrics
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.overlay.AdaptiveLayout
import dev.bautista.spotlight.overlay.OverlayBounds
import dev.bautista.spotlight.overlay.OverlayViewport
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Real Compose rasterization with explicit synthetic system/IME geometry, not a device screenshot. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w1280dp-h800dp", application = SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OverlayGeometryCaptureTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    @Test fun tabletLandscape() = capture(1280, 800, 600, "tablet-landscape")
    @Test @Config(qualifiers = "w390dp-h844dp")
    fun phonePortrait() = capture(390, 844, 300, "phone-portrait")

    private fun capture(width: Int, height: Int, ime: Int, name: String) {
        val app = RuntimeEnvironment.getApplication() as SpotlightApp
        val c = AppConfig(width = 820, landscapeWidth = 680, position = .35f,
            landscapePosition = .35f, files = false, animations = false)
        val safeLeft = if (width > height) 40 else 0
        val insets = WindowInsets.Builder().setInsetsIgnoringVisibility(
            WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout(),
            Insets.of(safeLeft, 24, 0, 48)
        ).build()
        val frame = OverlayViewport.fromWindowMetrics(WindowMetrics(Rect(0, 0, width, height), insets), 1f)
        val next = AdaptiveLayout.bounds(frame.width, frame.height, 1f,
            frame.keyboardOcclusion(ime), c, landscape = frame.landscape)
        // Historical service used full display dimensions and no IME for its position.
        val old = AdaptiveLayout.bounds(width, height, 1f, 0, c).let {
            OverlayBounds(it.width, it.top, (height - it.top - ime - 24f).coerceAtLeast(100f))
        }
        var shown by mutableStateOf(old)
        ui.setContent {
            Box(Modifier.fillMaxSize().background(Color(0xffe4e7ec))) {
                Box(Modifier.offset(y = (height - ime).dp).fillMaxWidth().height(ime.dp)
                    .background(Color(0xffb7c0ce)))
                Box(Modifier.offset {
                    val x = safeLeft + (frame.width - shown.width) / 2
                    IntOffset(x.dp.roundToPx(), (24 + shown.top).dp.roundToPx())
                }.width(shown.width.dp)) {
                    SpotlightPanel(app, c, {}, availableHeightDp = shown.heightDp)
                }
            }
        }
        ui.onNode(hasSetTextAction()).assertExists()
        ui.waitForIdle()
        ui.runOnIdle { capture091(ui.activity, "f13-$name-before") }
        ui.runOnIdle { shown = next }
        ui.waitForIdle()
        val actual = ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot
        val density = app.resources.displayMetrics.density
        assertTrue(actual.left >= safeLeft * density)
        assertTrue(actual.right <= width * density)
        assertTrue(actual.top >= 24 * density)
        assertTrue("Panel overlaps keyboard: $actual", actual.bottom <= (height - ime) * density)
        ui.runOnIdle { capture091(ui.activity, "f13-$name-after") }
    }
}
