package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
class ProductEntryPointsTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun visibleShortcutsOpenRealSavedAndSettingsWithoutChangingQuery() {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent { SpotlightPanel(app,AppConfig(files=false,animations=false,pins=setOf("pdf de ayer")),{},initialQuery="2+2") }
        ui.onNodeWithContentDescription("Opciones de búsqueda").assertDoesNotExist()
        ui.onNodeWithContentDescription("Abrir Guardados").assertIsDisplayed().performClick()
        ui.onNodeWithText("pdf de ayer").assertExists()
        ui.onNode(hasSetTextAction() and hasText("2+2")).assertExists()
        ui.onNodeWithContentDescription("Abrir Ajustes").performClick()
        val settings=org.robolectric.Shadows.shadowOf(ui.activity).nextStartedActivity
        assertEquals(dev.bautista.spotlight.MainActivity::class.java.name,settings.component!!.className)
        assertEquals(app.packageName,settings.component!!.packageName)
        assertEquals("General",settings.getStringExtra("group"))
        ui.onNode(hasSetTextAction() and hasText("2+2")).assertExists()
    }
    @Test fun homeHintDisappearsInSavedAndReturnsOnlyOnHome() {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent { SpotlightPanel(app,AppConfig(files=false,animations=false,suggestions=false),{},availableHeightDp=700f) }
        ui.onNodeWithTag("home-suggestion-hint").assertIsDisplayed()
        assertTrue(ui.onNodeWithTag("home-suggestion-hint").getUnclippedBoundsInRoot().top >=
            ui.onNodeWithTag("spotlight-bar").getUnclippedBoundsInRoot().bottom)
        ui.onNodeWithContentDescription("Abrir Guardados").performClick()
        ui.onNodeWithTag("home-suggestion-hint").assertDoesNotExist()
        ui.onNodeWithText("Volver").performClick()
        ui.onNodeWithTag("home-suggestion-hint").assertIsDisplayed()
        ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick()
        ui.onNodeWithContentDescription("Aplicaciones · Ctrl/⌘+1").performClick()
        ui.onNodeWithTag("home-suggestion-hint").assertDoesNotExist()
    }
    @Test fun catalogAlsoHidesRetiredModesAndKeepsRealAndroidActions() {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent { SpotlightTheme { NextPanel(app,"catalog",""){} } }
        ui.onNodeWithText("Modo estudio").assertDoesNotExist()
        ui.onNodeWithText("Modo lectura").assertDoesNotExist()
        ui.onNodeWithText("Ajustes de Wi-Fi").performScrollTo().assertIsDisplayed()
    }
    @Test fun retiredCapabilitiesAreHiddenWhileDataAndUsefulActionsRemain() {
        val legacy=AppConfig(snippets=setOf("old|preserve"),routines="[]",pins=setOf("pdf"))
        assertFalse(ProductVisibility.allowed(Result("snippet:old","old","","Textos",1,Action.Copy("preserve"))))
        assertFalse(ProductVisibility.allowed(Result("routine:old","old","","Rutinas",1,Action.WorkflowRun("old"))))
        assertFalse(ProductVisibility.allowed(Result("mode:old","old","","Modos",1,Action.SystemPanel("profiles"))))
        assertTrue(ProductVisibility.allowed(Result("note:keep","keep","","Notas",1,Action.Copy("keep"))))
        assertEquals(setOf("old|preserve"),legacy.snippets)
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent { SpotlightPanel(app,legacy.copy(files=false,animations=false),{},initialQuery="2+2") }
        ui.waitUntil(15000) { ui.onAllNodesWithText("4").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Tab acciones").performClick()
        ui.onNodeWithText("Guardar como nota").assertExists()
        ui.onNodeWithText("Guardar texto frecuente").assertDoesNotExist()
    }
}
