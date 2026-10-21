package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ActionPanelKeyboardContractTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun groupsAndIdentityDoNotDependOnTheVisibleTitle() {
        ui.setContent { SpotlightTheme {
            ActionPanelView(listOf(
                ContextAction("Abrir", "", CommandIcons.Apps, {}, id="open"),
                ContextAction("Copiar", "Organizar", CommandIcons.Star, {}, id="pin", group=ContextActionGroup.Organize),
                ContextAction("Copiar", "Texto", CommandIcons.ContentCopy, {}, id="copy", group=ContextActionGroup.Text)
            ),FocusRequester())
        } }
        ui.onAllNodesWithText("Copiar").assertCountEquals(2)
        ui.onNodeWithText("ORGANIZAR").assertExists()
        ui.onNodeWithText("TEXTO").assertExists()
    }
    @Test fun actionGroupsHaveVisibleHeadingsWithoutExecutingItems() {
        var action=""
        val focus=FocusRequester()
        ui.setContent {
            SpotlightTheme("dark") {
                ActionPanelView(listOf(
                    ContextAction("Abrir","Principal",CommandIcons.Chevron,{action="open"}),
                    ContextAction("Añadir favorita","Ordenar",CommandIcons.Star,{action="favorite"},id="favorite",group=ContextActionGroup.Organize),
                    ContextAction("Información de la app","Sistema",CommandIcons.Info,{action="system"},id="info",group=ContextActionGroup.System),
                    ContextAction("Copiar texto","Texto",CommandIcons.ContentCopy,{action="copy"},id="copy",group=ContextActionGroup.Text)
                ),focus)
            }
        }
        ui.onNodeWithText("PRINCIPAL").assertExists()
        ui.onNodeWithText("ORGANIZAR").assertExists()
        ui.onNodeWithText("TEXTO").assertExists()
        ui.runOnIdle { assertEquals("",action) }
    }

    @Test fun tabCyclesAndEnterRunsOnlyFocusedContextAction(){
        var action=""
        val focus=FocusRequester()
        ui.setContent{SpotlightTheme("dark"){ActionPanelView(listOf(ContextAction("Abrir","Principal",CommandIcons.Chevron,{action="open"}),ContextAction("Compartir","Secundaria",CommandIcons.Share,{action="share"})),focus)}}
        ui.runOnIdle{focus.requestFocus()}
        ui.waitUntil(15000){ui.onAllNodes(isFocused()).fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Abrir").assertIsFocused()
        ui.onNodeWithText("Abrir").performKeyInput{pressKey(Key.Tab)}
        ui.onNodeWithText("Compartir").assertIsFocused();assertEquals("",action)
        ui.onNodeWithText("Compartir").performKeyInput{keyDown(Key.ShiftLeft);pressKey(Key.Tab);keyUp(Key.ShiftLeft)}
        ui.onNodeWithText("Abrir").assertIsFocused()
        ui.onNodeWithText("Abrir").performKeyInput{pressKey(Key.DirectionDown);pressKey(Key.Enter)}
        ui.runOnIdle{assertEquals("share",action)}
    }
}
