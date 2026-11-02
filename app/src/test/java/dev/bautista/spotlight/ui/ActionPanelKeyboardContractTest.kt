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
class ActionPanelKeyboardContractTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun nativeBackRestoresParentFilterBeforeClosingAndUnregistersOnDispose() {
        val navigation=ActionBackNavigation()
        var closed=0
        var shown by mutableStateOf(true)
        ui.setContent { SpotlightTheme("dark") {
            if(shown)CompositionLocalProvider(LocalActionBackNavigation provides navigation) {
                ActionPanelView(listOf(
                    ContextAction("Abrir","",CommandIcons.Apps,{},id="open"),
                    ContextAction("Sistema","",CommandIcons.Info,{},id="system",children=listOf(
                        ContextAction("Permisos","",CommandIcons.Info,{},id="permissions")
                    ))
                ),FocusRequester(),{closed++})
            }
        } }
        ui.onNodeWithTag("action-filter").performTextInput("Sistema")
        ui.onNode(hasText("Sistema") and !hasTestTag("action-filter"),useUnmergedTree=true).performClick()
        ui.onNodeWithText("Permisos").assertExists()
        // The merged field also exposes its placeholder; inspect the editable value itself.
        ui.onNodeWithTag("action-filter").assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText,AnnotatedString("")))
        ui.runOnIdle { assertNotNull(navigation.back);navigation.back!!.invoke() }
        ui.onNodeWithTag("action-filter").assertTextEquals("Sistema")
        ui.onNode(hasText("Sistema") and !hasTestTag("action-filter"),useUnmergedTree=true).assertExists()
        ui.runOnIdle { assertEquals(0,closed);navigation.back!!.invoke();assertEquals(1,closed);shown=false }
        ui.runOnIdle { assertNull(navigation.back) }
    }

    @Test fun ctrlFFiltersActionsAndCtrlKClosesWithoutExecutingAnotherAction() {
        var executed=""
        var closed=0
        val focus=FocusRequester()
        ui.setContent { SpotlightTheme("dark") {
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

    @Test fun asynchronousReorderingRetainsSelectedIdentityAndRemovalUsesLiveFallback() {
        var executed=""
        fun action(id:String)=ContextAction(id,"",CommandIcons.Apps,{executed=id},id=id)
        var actions by mutableStateOf(listOf(action("Abrir"),action("Copiar"),action("Compartir")))
        val focus=FocusRequester()
        ui.setContent { SpotlightTheme("dark") {ActionPanelView(actions,focus)} }
        ui.runOnIdle { focus.requestFocus() }
        ui.onNodeWithText("Abrir").performKeyInput { pressKey(Key.DirectionDown) }
        ui.onNodeWithText("Copiar").assertIsFocused()
        ui.runOnIdle { actions=listOf(action("Compartir"),action("Copiar"),action("Abrir")) }
        ui.onNodeWithText("Copiar").assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        ui.runOnIdle { assertEquals("Copiar",executed);actions=listOf(action("Compartir"),action("Abrir")) }
        ui.onNodeWithText("Compartir").assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        ui.runOnIdle { assertEquals("Compartir",executed) }
    }
    @Test fun groupsAndIdentityDoNotDependOnTheVisibleTitle() {
        ui.setContent { SpotlightTheme {
            ActionPanelView(listOf(
                ContextAction("Abrir", "", CommandIcons.Apps, {}, id="open"),
                ContextAction("Copiar", "Organizar", CommandIcons.Star, {}, id="pin", group=ContextActionGroup.Organize),
                ContextAction("Copiar", "Texto", CommandIcons.ContentCopy, {}, id="copy", group=ContextActionGroup.Text)
            ),FocusRequester())
        } }
        ui.onAllNodesWithText("Copiar").assertCountEquals(2)
        ui.onNodeWithText("Organizar").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        ui.onNodeWithText("Texto").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
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
        ui.onNodeWithText("Principal").assertIsDisplayed().assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        ui.onNodeWithText("Organizar").assertIsDisplayed().assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        ui.onNodeWithText("Texto").assertIsDisplayed().assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        ui.runOnIdle { assertEquals("",action) }
        ui.onNodeWithTag("context-action-panel").captureRoboImage(
            "build/unslop-actions/groups.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
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

    @Test fun densityAndTextScalePreserveMinimum48dpTouchTargetsInActionPanel() {
        val focus=FocusRequester()
        ui.setContent {
            SpotlightTheme(
                "dark",
                config=dev.bautista.spotlight.data.Config(preset="glass",textScale=1.15f,uiDensityScale=0.85f),
            ) {
                ActionPanelView(
                    listOf(
                        ContextAction("Abrir","Abrir documento",CommandIcons.Chevron,{},id="open"),
                        ContextAction("Copiar…","Elegir formato",CommandIcons.ContentCopy,{},id="copy.formats",group=ContextActionGroup.Text,children=listOf(
                            ContextAction("Copiar nombre","Nombre del archivo",CommandIcons.Description,{},id="file.copy-name"),
                            ContextAction("Copiar URI","Identificador de documento Android",CommandIcons.ContentCopy,{},id="file.copy-uri")
                        )),
                        ContextAction("Corregir con IA","Revisar antes de enviar al proveedor",CommandIcons.AutoAwesome,{},id="ai:corregir",group=ContextActionGroup.Text)
                    ),
                    focus,
                )
            }
        }
        ui.onNodeWithText("Abrir").assertHeightIsAtLeast(48.dp)
        ui.onNodeWithText("Copiar…").assertHeightIsAtLeast(48.dp)
        ui.onNodeWithText("Corregir con IA").assertHeightIsAtLeast(48.dp)
        ui.onNodeWithText("Revisar antes de enviar al proveedor").assertExists()
        ui.onNodeWithTag("context-action-panel").captureRoboImage(
            "build/unslop-actions/compact-large-text.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
}
