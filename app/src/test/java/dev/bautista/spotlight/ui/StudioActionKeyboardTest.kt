package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StudioActionKeyboardTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun ctrlFFiltersActionsAndCtrlKClosesWithoutExecutingAnotherAction() {
        var executed=""
        var closed=0
        val focus=FocusRequester()
        ui.setContent { SpotlightTheme("dark",dev.bautista.spotlight.data.Config(focusInterface=true,animations=false)) {
            ActionPanelView(listOf(
                ContextAction("Abrir","",CommandIcons.Apps,{executed="open"},id="open"),
                ContextAction("Copiar","",CommandIcons.ContentCopy,{executed="copy"},id="copy")
            ),focus,{closed++})
        } }
        ui.runOnIdle { focus.requestFocus() }
        ui.onNodeWithText("Abrir").performKeyInput { keyDown(Key.CtrlLeft);pressKey(Key.F);keyUp(Key.CtrlLeft) }
        ui.onNodeWithTag("action-filter").assertIsFocused().performTextInput("copiar")
        ui.onNodeWithText("Abrir").assertDoesNotExist()
        ui.onNodeWithTag("action-filter").performKeyInput { pressKey(Key.Enter) }
        ui.runOnIdle { assertEquals("copy",executed);assertEquals(0,closed) }
        ui.onNodeWithTag("action-filter").performKeyInput { keyDown(Key.CtrlLeft);pressKey(Key.K);keyUp(Key.CtrlLeft) }
        ui.runOnIdle { assertEquals(1,closed);assertEquals("copy",executed) }
    }

    @Test fun tabCyclesAndEnterRunsOnlyFocusedContextAction(){
        var action=""
        val focus=FocusRequester()
        ui.setContent{SpotlightTheme("dark",dev.bautista.spotlight.data.Config(focusInterface=true,animations=false)){ActionPanelView(listOf(ContextAction("Abrir","Principal",CommandIcons.Chevron,{action="open"}),ContextAction("Compartir","Secundaria",CommandIcons.Share,{action="share"})),focus)}}
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
