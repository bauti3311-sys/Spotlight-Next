package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.input.*
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class,qualifiers="w1000dp-h800dp")
class KeyboardShortcutEditorTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun editorRejectsConflictsThenStoresTheCapturedCombination() {
        var c by mutableStateOf(AppConfig())
        ui.setContent { SpotlightTheme("dark",c){Column(Modifier.width(500.dp)){KeyboardShortcutEditor(c){c=it}}} }
        ui.onNodeWithTag("shortcut-edit-actions").performClick()
        ui.onNodeWithTag("shortcut-capture").performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.M);keyUp(Key.CtrlLeft)}
        ui.onNodeWithText("Conflicto con Expandir resultados.").assertExists()
        ui.runOnIdle{assertTrue(c.keyBindings.isEmpty())}
        ui.onNodeWithTag("shortcut-capture").performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.L);keyUp(Key.CtrlLeft)}
        ui.onNodeWithTag("shortcut-capture").assertDoesNotExist()
        ui.onNodeWithTag("shortcut-edit-actions").assertTextContains("Ctrl+L")
        ui.runOnIdle{assertEquals(KeyChord(android.view.KeyEvent.KEYCODE_L),KeyboardBindings.resolve(c,"actions"))}
    }
    @Test fun reassignedActionShortcutTogglesTheActualFloatingPanelAndPreservesSearch() {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        val c=KeyboardBindings.assign(AppConfig(theme="dark",files=false,online=false,suggestions=false,learning=false,animations=false),"actions",KeyChord(android.view.KeyEvent.KEYCODE_L))
        ui.setContent{Box(Modifier.width(800.dp)){SpotlightPanel(app,c,{},"2+2",availableHeightDp=700f)}}
        val search=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:calc").fetchSemanticsNodes().isNotEmpty()}
        search.performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.L);keyUp(Key.CtrlLeft)}
        ui.onNodeWithTag("action-surface").assertIsDisplayed()
        ui.onNodeWithTag("context-action-panel").performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.L);keyUp(Key.CtrlLeft)}
        ui.onNodeWithTag("action-surface").assertDoesNotExist()
        search.assertTextEquals("2+2").assertIsFocused()
    }
}
