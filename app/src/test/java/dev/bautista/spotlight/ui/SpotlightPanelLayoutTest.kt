package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w1280dp-h800dp", application = SpotlightApp::class)
class SpotlightPanelLayoutTest {
    @get:Rule val ui = createComposeRule()

    @Test
    fun gridOpensMultipleRowsAndStopsResizingAcrossFrames() {
        val app = RuntimeEnvironment.getApplication() as SpotlightApp
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        repeat(28) { n ->
            val activity =
                ActivityInfo().apply {
                    packageName = "fixture.app$n"
                    name = "fixture.app$n.Main"
                    nonLocalizedLabel = "Fixture $n"
                    applicationInfo = ApplicationInfo().apply { packageName = "fixture.app$n" }
                }
            shadowOf(app.packageManager)
                .addResolveInfoForIntent(launcher, ResolveInfo().apply { activityInfo = activity })
        }
        var viewport by mutableStateOf(560f)
        ui.setContent {
            SpotlightPanel(app, AppConfig(files = false), {}, availableHeightDp = viewport)
        }
        ui.onNodeWithContentDescription("Opciones de búsqueda").performClick();ui.onNodeWithContentDescription("Aplicaciones · Ctrl/⌘+1").performClick()
        ui.waitUntil(15000) {
            ui.onAllNodesWithText("Fixture 2").fetchSemanticsNodes().isNotEmpty()
        }
        val height = ui.onNodeWithTag("spotlight-results").fetchSemanticsNode().boundsInRoot.height
        val density = app.resources.displayMetrics.density
        assertTrue("Grid must expand past the cropped first row", height > 220 * density)
        repeat(6) {
            ui.mainClock.advanceTimeBy(180)
            ui.waitForIdle()
            assertEquals(
                height,
                ui.onNodeWithTag("spotlight-results").fetchSemanticsNode().boundsInRoot.height,
                1f,
            )
        }
        ui.runOnIdle { viewport = 280f }
        ui.waitForIdle()
        val smaller = ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot.height
        assertTrue(
            "Keyboard budget must bound the entire overlay ($smaller px)",
            smaller <= 280 * density,
        )
        ui.runOnIdle { viewport = 120f }
        ui.waitForIdle()
        assertTrue(
            ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot.height <=
                120 * density
        )
        ui.onNodeWithTag("spotlight-results").assertDoesNotExist()
        ui.runOnIdle { viewport = 280f }
        ui.waitForIdle()
        ui.onNodeWithTag("spotlight-results").assertIsDisplayed()
        ui.onNodeWithContentDescription("Opciones de búsqueda").performClick();ui.onNodeWithContentDescription("Aplicaciones · Ctrl/⌘+1").performClick()
        // Search runs asynchronously; popup/resize animations used to hide this race.
        ui.waitUntil(15000) { ui.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Crear una nota").assertDoesNotExist();ui.onNodeWithText("SUGERENCIAS").assertDoesNotExist()
        ui.onNodeWithContentDescription("Opciones de búsqueda").performClick();ui.onNodeWithContentDescription("Aplicaciones · Ctrl/⌘+1").performClick()
        ui.onNodeWithTag("spotlight-results").assertIsDisplayed()
    }

    @OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
    @Test
    fun rightArrowAcceptsSuggestionWithoutOpeningActionsOrLaunchingAnApp() {
        val app = RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent {
            SpotlightPanel(
                app,
                AppConfig(files = false),
                {},
                initialQuery = "yout",
                availableHeightDp = 500f,
            )
        }
        ui.waitUntil(15000) { ui.onAllNodesWithText("→").fetchSemanticsNodes().isNotEmpty() }
        ui.onNode(hasSetTextAction()).performClick()
        ui.onNode(hasSetTextAction()).performKeyInput {
            pressKey(androidx.compose.ui.input.key.Key.DirectionRight)
        }
        ui.onNode(hasSetTextAction()).assertTextEquals("youtube ")
        ui.onAllNodesWithText("Menú de acciones").assertCountEquals(0)
    }

    @Test
    fun newNoteEditorStaysInsideOverlayInsteadOfOpeningADialog() {
        val app = RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent {
            SpotlightPanel(
                app,
                AppConfig(files = false),
                {},
                initialQuery = "crear nota",
                availableHeightDp = 500f,
            )
        }
        ui.waitUntil(15000) {
            ui.onAllNodesWithText("Crear nota").fetchSemanticsNodes().isNotEmpty()
        }
        ui.onNodeWithText("Crear nota").performClick()
        ui.onNodeWithText("Texto de la nota").assertIsDisplayed()
        ui.onAllNodes(hasSetTextAction())[1].performTextInput("Una nota de prueba")
        ui.onNodeWithText("Guardar").performClick()
        ui.waitUntil(15000) { app.local.notes.value.any { it.text == "Una nota de prueba" } }
    }
}
