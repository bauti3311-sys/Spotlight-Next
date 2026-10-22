package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], qualifiers="w1280dp-h800dp", application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UnifiedInspectorFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private var width by mutableIntStateOf(800)
    private var closed=0
    private fun search()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
    private fun show(query:String) {
        ui.setContent { Box(Modifier.width(width.dp)) {
            SpotlightPanel(app,AppConfig(files=false,online=false,animations=false,learning=false,suggestions=false),{closed++},query,availableHeightDp=700f)
        } }
    }
    private fun awaitTag(tag:String) { ui.waitUntil(15000){ui.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()} }
    private fun capture(name:String) { captureScreenRoboImage("build/unified-inspector/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record)) }

    @Test fun pinnedLocalPreviewSurvivesQueryAndActionsKeepTheCurrentSelection() {
        val text="Fijable prueba local\n"+"Contenido privado local. ".repeat(14)
        runBlocking { app.local.saveNote(text) }
        ui.waitUntil(15000){app.local.notes.value.any{it.text==text}}
        val id=app.local.notes.value.first{it.text==text}.id
        show("Fijable")
        awaitTag("result:note:$id")
        search().performKeyInput{pressKey(Key.DirectionDown);pressKey(Key.Spacebar)}
        ui.onNodeWithTag("context-inspector").assertIsDisplayed()
        ui.onNodeWithContentDescription("Fijar inspector").assertHeightIsAtLeast(48.dp).performClick()
        search().performClick().performTextReplacement("2+2")
        awaitTag("result:calc")
        ui.onNodeWithContentDescription("Desfijar inspector").assertIsDisplayed()
        ui.onNode(hasText(text) and hasAnyAncestor(hasTestTag("inspector-content"))).assertExists()
        capture("tablet-pinned-local")
        search().performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.K);keyUp(Key.CtrlLeft)}
        ui.onNodeWithTag("action-surface").assertIsDisplayed()
        ui.onNodeWithText("Copiar resultado").assertExists()
        ui.onNodeWithTag("action-filter").performKeyInput{pressKey(Key.Escape)}
        search().assertTextEquals("2+2").assertIsFocused()
        ui.onNodeWithContentDescription("Desfijar inspector").assertIsDisplayed()
        search().performKeyInput{pressKey(Key.Escape)}
        ui.onNodeWithTag("inspector-content").assertDoesNotExist()
        search().assertTextEquals("2+2").assertIsFocused()
        assertEquals(0,closed)
    }

    @Test fun pinnedSearchFormPreservesDraftAndResultsAcrossResponsiveLayout() {
        show("youtube")
        awaitTag("result:catalog:youtube:search")
        ui.onNodeWithTag("result:catalog:youtube:search").performClick()
        search().performKeyInput{pressKey(Key.Enter)}
        ui.onNodeWithTag("context-inspector").assertIsDisplayed()
        ui.onNodeWithTag("result:catalog:youtube:search").assertIsDisplayed()
        ui.onNodeWithText("Qué querés buscar").performTextReplacement("Cerati en vivo")
        ui.onNodeWithContentDescription("Fijar inspector").performClick()
        search().performClick().performTextReplacement("2+2")
        awaitTag("result:calc")
        ui.onNode(hasSetTextAction() and hasText("Cerati en vivo")).assertExists()
        capture("tablet-search-form")
        ui.runOnIdle{width=360}
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        ui.onNode(hasSetTextAction() and hasText("Cerati en vivo")).assertIsDisplayed()
        capture("phone-search-form")
        ui.runOnIdle{width=800}
        ui.onNodeWithTag("context-inspector").assertIsDisplayed()
        ui.onNode(hasSetTextAction() and hasText("Cerati en vivo")).assertExists()
        assertEquals(0,closed)
    }
}
