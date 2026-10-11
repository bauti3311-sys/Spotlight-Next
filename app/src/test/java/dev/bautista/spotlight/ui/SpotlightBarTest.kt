package dev.bautista.spotlight.ui

import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w1280dp-h800dp", application = android.app.Application::class)
class SpotlightBarTest {
    @get:Rule val ui = createComposeRule()

    @Test
    fun optionsRespectKeyboardBudgetAndReportVisibility() {
        var opened=false
        ui.setContent {
            SpotlightTheme("light") {
                SpotlightBar("",{},"Todo",{}, {}, {},
                    remember { FocusRequester() },.96f,true,
                    onOptionsChange={opened=it},optionsMaxHeight=99.dp)
            }
        }
        ui.onNodeWithContentDescription("Opciones de búsqueda").performClick()
        ui.runOnIdle { assertTrue(opened) }
        val height=ui.onNodeWithTag("spotlight-bar").fetchSemanticsNode().boundsInRoot.height
        val density=org.robolectric.RuntimeEnvironment.getApplication().resources.displayMetrics.density
        assertTrue("Options must fit above the keyboard",height<=160*density+1f)
        ui.onNodeWithContentDescription("Opciones de búsqueda").performClick()
        ui.runOnIdle { assertFalse(opened) }
    }

    @Test
    fun optionsStayInComposeWindowAndToggleWithoutClearingQuery() {
        var text by mutableStateOf("Cerati")
        ui.setContent {
            SpotlightTheme("light") {
                SpotlightBar(text,{text=it},"Todo",{}, {}, {},
                    remember { FocusRequester() },.96f,false)
            }
        }
        ui.onNodeWithContentDescription("Opciones de búsqueda").performClick()
        ui.onNodeWithContentDescription("Búsqueda universal").assertExists()
        ui.onAllNodes(isPopup()).assertCountEquals(0)
        ui.onNode(hasSetTextAction()).assertTextEquals("Cerati")
        ui.onNodeWithContentDescription("Opciones de búsqueda").performClick()
        ui.onNodeWithContentDescription("Búsqueda universal").assertDoesNotExist()
        ui.runOnIdle { assertEquals("Cerati",text) }
    }

    @Test
    fun optionalMenuSelectsExistingScopesAndReturnsToUniversal() {
        var category by mutableStateOf("Todo")
        ui.setContent {
            SpotlightTheme("light") {
                SpotlightBar(
                    "",
                    {},
                    category,
                    { category = it },
                    {},
                    {},
                    remember { FocusRequester() },
                    .96f,
                    false,
                )
            }
        }
        listOf(
                "Aplicaciones · Ctrl/⌘+1" to "Apps",
                "Archivos · Ctrl/⌘+2" to "Archivos",
                "Comandos · Ctrl/⌘+3" to "Acciones",
                "IA · Ctrl/⌘+4" to "IA",
            )
            .forEach { (description, value) ->
                ui.onNodeWithContentDescription("Opciones de búsqueda").performClick()
                ui.onNodeWithContentDescription(description).assertExists().performClick()
                ui.runOnIdle { assertEquals(value, category) }
            }
        ui.onNodeWithContentDescription("Opciones de búsqueda").performClick();ui.onNodeWithContentDescription("IA · Ctrl/⌘+4").performClick()
        ui.runOnIdle { assertEquals("Todo", category) }
    }

    @Test
    fun capsuleAcceptsTextAndClearButtonRunsAction() {
        var text by mutableStateOf("")
        ui.setContent {
            SpotlightTheme("light") {
                SpotlightBar(
                    text,
                    { text = it },
                    "Todo",
                    {},
                    { text = "" },
                    {},
                    remember { FocusRequester() },
                    .96f,
                    false,
                )
            }
        }
        ui.onNode(hasSetTextAction()).performTextInput("kind:pdf factura")
        ui.runOnIdle { assertEquals("kind:pdf factura", text) }
        ui.onNodeWithContentDescription("Limpiar búsqueda").performClick()
        ui.runOnIdle { assertEquals("", text) }
    }
}
