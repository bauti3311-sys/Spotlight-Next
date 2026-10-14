package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.overlay.AdaptiveLayout
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w1280dp-h800dp", application = SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ContentExpansionTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()

    @Test fun resultsExpandAndClearingQueryContractsWithoutChangingPreferences() {
        exercise(AppConfig(width = 0, landscapeWidth = 0, files = false, animations = false), true)
    }

    @Test fun manualWidthStaysFixedWhenContentRequestsExpansion() {
        exercise(AppConfig(width = 680, landscapeWidth = 460, files = false, animations = false), false)
    }

    private fun capture(name: String) {
        // Diagnostic artifacts only, never overwrite the versioned verification baselines.
        ui.onRoot().captureRoboImage("build/091-ui/$name.png",
            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record))
    }

    private fun exercise(config: AppConfig, automatic: Boolean) {
        val app = RuntimeEnvironment.getApplication() as SpotlightApp
        var expanded by mutableStateOf(false)
        ui.setContent {
            val bounds = AdaptiveLayout.bounds(1200, 800, 1f, 0, config, expanded, landscape = true)
            Box(Modifier.width(bounds.width.dp)) {
                SpotlightPanel(app, config, {}, initialQuery = "2+2", onContentExpansion = { expanded = it })
            }
        }
        ui.waitUntil(15000) { ui.onAllNodesWithText("4").fetchSemanticsNodes().isNotEmpty() }
        ui.waitForIdle()
        if (automatic) capture("f14-tablet-results")
        ui.runOnIdle { assertTrue("Universal results must request content expansion", expanded) }
        val wide = ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot.width
        ui.onNode(hasSetTextAction()).performTextClearance()
        ui.waitUntil(15000) { !expanded }
        ui.waitForIdle()
        val compact = ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot.width
        if (automatic) {
            assertTrue("Clearing search must restore compact automatic width", compact < wide)
            capture("f14-tablet-cleared")
        } else assertEquals("Expansion must preserve horizontal manual width", wide, compact, .1f)
        assertEquals(if (automatic) 0 else 680, config.width)
        assertEquals(if (automatic) 0 else 460, config.landscapeWidth)
    }
}
