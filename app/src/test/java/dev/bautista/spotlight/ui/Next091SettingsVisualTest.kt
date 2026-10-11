package dev.bautista.spotlight.ui
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.MainActivity
import dev.bautista.spotlight.SpotlightApp
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Next091SettingsVisualTest {
    @get:Rule val ui=createAndroidComposeRule<MainActivity>()
    @Test fun groupedSettingsHaveFreshCaptures(){ui.onNodeWithText("Preferencias").assertExists();ui.runOnIdle{capture091(ui.activity,"settings-general-tablet")};ui.onNodeWithText("Apariencia").performClick();ui.waitUntil(15000){ui.onAllNodesWithText("Diseños preparados").fetchSemanticsNodes().isNotEmpty()};ui.runOnIdle{capture091(ui.activity,"settings-appearance-tablet")}}
}
