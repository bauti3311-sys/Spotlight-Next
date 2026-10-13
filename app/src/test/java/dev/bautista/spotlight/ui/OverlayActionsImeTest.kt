package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w1280dp-h800dp", application = SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OverlayActionsImeTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    @Test fun tabReleasesImeSpaceAndKeepsActionHeaderVisible() {
        var viewport by mutableFloatStateOf(700f)
        var hides = 0
        val keyboard = object : SoftwareKeyboardController {
            override fun show() { viewport = 246f }
            override fun hide() { hides++; viewport = 700f }
        }
        val app = RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent {
            CompositionLocalProvider(LocalSoftwareKeyboardController provides keyboard) {
                Box(Modifier.width(720.dp)) {
                    SpotlightPanel(app, AppConfig(files = false, animations = false), {},
                        initialQuery = "2+2", availableHeightDp = viewport)
                }
            }
        }
        ui.waitUntil(15000) { ui.onAllNodesWithText("4").fetchSemanticsNodes().isNotEmpty() }
        ui.runOnIdle { keyboard.show() }
        val previous = hides
        ui.onNode(hasSetTextAction()).performKeyInput { pressKey(Key.Tab) }
        ui.waitUntil(15000) { ui.onAllNodesWithText("Copiar resultado").fetchSemanticsNodes().isNotEmpty() }
        ui.waitForIdle()
        ui.runOnIdle { capture091(ui.activity, "f13-tablet-actions-ime") }
        ui.onNodeWithText("Copiar resultado").assertIsFocused()
        ui.onNodeWithText("Acciones").assertIsDisplayed()
        ui.runOnIdle { assertTrue("Tab must release IME space for contextual actions", hides > previous) }
    }
}
