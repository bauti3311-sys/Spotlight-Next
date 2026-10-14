package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
class KeyboardHintsTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    @Test fun hintsDescribeActualTabAndLayeredEscapeBehavior() {
        val app = RuntimeEnvironment.getApplication() as SpotlightApp
        var closed = 0
        ui.setContent { Box(Modifier.width(720.dp)) { SpotlightPanel(app, AppConfig(files = false, animations = false), { closed++ }, initialQuery = "2+2") } }
        ui.waitUntil(15000) { ui.onAllNodesWithText("4").fetchSemanticsNodes().isNotEmpty() }
        ui.waitForIdle()
        ui.onNodeWithText("Tab acciones", substring = true).assertIsDisplayed()
        ui.onRoot().captureRoboImage("build/091-ui/f3-keyboard-results.png",
            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record))
        ui.onNodeWithText("Tab acciones", substring = true).assertIsDisplayed()
        ui.onNode(hasSetTextAction()).performKeyInput { pressKey(Key.Tab) }
        ui.waitUntil(15000) { ui.onAllNodesWithText("Copiar resultado").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Copiar resultado").assertIsFocused()
        ui.onNodeWithText("Abrir en otra app").assertDoesNotExist()
        ui.onNodeWithText("Sin vista previa").assertDoesNotExist()
        ui.onNodeWithText("Shift+Tab anterior", substring = true).assertIsDisplayed()
        ui.onRoot().captureRoboImage("build/091-ui/f3-keyboard-actions.png",
            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record))
        ui.onNodeWithText("Copiar resultado").performKeyInput { pressKey(Key.Escape) }
        ui.onNodeWithText("Tab acciones", substring = true).assertIsDisplayed()
        ui.onNodeWithText("Acciones").assertDoesNotExist()
        ui.onAllNodesWithText("4").assertCountEquals(2)
        ui.runOnIdle { assertEquals("Escape must dismiss actions before the overlay", 0, closed) }
        ui.onNode(hasSetTextAction()).performKeyInput { pressKey(Key.Escape) }
        ui.onAllNodesWithText("4").assertCountEquals(1)
        ui.runOnIdle { assertEquals("Escape must dismiss preview before the overlay", 0, closed) }
        ui.onNode(hasSetTextAction()).performKeyInput { pressKey(Key.Escape) }
        ui.runOnIdle { assertEquals(1, closed) }
    }
}
