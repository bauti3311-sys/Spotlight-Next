package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class, com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w1280dp-h800dp", application = SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class QuickLookTouchTargetTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    @Test fun tabletCloseTargetDoesNotCloseOverlayOrClearQuery() = exercise(720, "tablet")

    @Test @Config(qualifiers = "w390dp-h844dp")
    fun phoneCloseTargetDoesNotCloseOverlayOrClearQuery() = exercise(360, "phone")

    private fun exercise(width: Int, device: String) {
        val app = RuntimeEnvironment.getApplication() as SpotlightApp
        var closed = 0
        ui.setContent {
            Box(Modifier.width(width.dp)) {
                SpotlightPanel(app, AppConfig(files = false, animations = false), { closed++ }, initialQuery = "2+2")
            }
        }
        ui.waitUntil(15000) { ui.onAllNodesWithText("4").fetchSemanticsNodes().isNotEmpty() }
        ui.onNode(hasSetTextAction()).performKeyInput { pressKey(Key.Tab) }
        ui.waitUntil(15000) { ui.onAllNodesWithContentDescription("Cerrar acciones").fetchSemanticsNodes().isNotEmpty() }
        ui.waitForIdle()
        ui.onNodeWithText("Copiar resultado").assertIsDisplayed()
        ui.onNodeWithText("Enter · Ejecutar").assertIsDisplayed()
        ui.onRoot().captureRoboImage("build/091-ui/f2-close-$device.png",
            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record))
        ui.onNodeWithText("Guardar como nota").performScrollTo().assertIsDisplayed()
        ui.onNodeWithContentDescription("Cerrar acciones")
            .assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
            .performTouchInput { click(Offset(4f, center.y)) }
        ui.onNodeWithContentDescription("Cerrar acciones").assertDoesNotExist()
        ui.onNodeWithText("Acciones").assertDoesNotExist()
        ui.onNode(hasSetTextAction()).assertTextEquals("2+2")
        ui.onAllNodesWithText("4").assertCountEquals(1)
        ui.runOnIdle { assertEquals("Close preview must preserve the overlay", 0, closed) }
    }
}
