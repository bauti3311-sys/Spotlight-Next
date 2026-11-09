package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w320dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ActionPanelCraftTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val focus=FocusRequester()
    private var executed=0
    private var closed=0
    private val title="Copiar nombre completo del archivo seleccionado"
    private val detail="Revisá el destino antes de continuar"
    private fun show(theme:String="dark",fontScale:Float=1f,rtl:Boolean=false,contextTitle:String?=null) {
        ui.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,fontScale),
                LocalLayoutDirection provides if(rtl)LayoutDirection.Rtl else LayoutDirection.Ltr) {
                SpotlightTheme(theme,AppConfig(theme=theme,preset="glass",animations=false)) {
                    Surface(color=MaterialTheme.colorScheme.surface) {
                        Box(Modifier.width(320.dp)) {
                            ActionPanelView(listOf(
                                ContextAction(title,detail,CommandIcons.ContentCopy,{executed++},id="copy"),
                                ContextAction("Abrir archivo","",CommandIcons.Apps,{executed++},id="open")
                            ),focus,{closed++},contextResult=contextTitle?.let {
                                dev.bautista.spotlight.search.Result("doc:review",it,"","Archivos",0,dev.bautista.spotlight.search.Action.Copy(""))
                            })
                        }
                    }
                }
            }
        }
        ui.runOnIdle { focus.requestFocus() }
    }
    private fun capture(name:String)=ui.onNodeWithTag("context-action-panel").captureRoboImage(
        "build/action-panel-craft/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    private fun assertComplete(text:String) {
        val layouts=mutableListOf<TextLayoutResult>()
        ui.onNodeWithText(text,useUnmergedTree=true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertFalse("The complete action must remain readable: $text",layouts.single().hasVisualOverflow)
    }
    @Test fun largeTextKeepsCompleteActionAndDescription() {
        show(fontScale=2f)
        capture("phone-large-text")
        assertComplete(title);assertComplete(detail)
        ui.onNodeWithText(title).assertHeightIsAtLeast(48.dp)
        assertEquals(0,executed)
    }
    @Test fun rtlLargeTextKeepsCompleteActionAndKeyboardExecution() {
        show(fontScale=2f,rtl=true)
        assertComplete(title);assertComplete(detail)
        ui.onNodeWithText(title).assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        ui.runOnIdle { assertEquals(1,executed);assertEquals(0,closed) }
    }
    private fun keyboardFocus(theme:String) {
        show(theme)
        ui.onNodeWithText(title).assertIsFocused()
        capture("$theme-row-focus")
        ui.onNodeWithText(title).performKeyInput { keyDown(Key.CtrlLeft);pressKey(Key.F);keyUp(Key.CtrlLeft) }
        val filter=ui.onNodeWithTag("action-filter")
        filter.assertIsFocused().assertContentDescriptionEquals("Buscar acciones")
        capture("$theme-filter-focus")
        filter.performTextInput("zzzzz")
        filter.assertContentDescriptionEquals("Buscar acciones")
        ui.onNodeWithText("No se encontraron acciones. Probá otro término.").assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion,LiveRegionMode.Polite))
        filter.performTextClearance()
        ui.onNodeWithText(title).assertIsDisplayed()
        filter.performKeyInput { pressKey(Key.Tab) }
        ui.onNodeWithText("Abrir archivo").assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        ui.runOnIdle { assertEquals(1,executed);assertEquals(0,closed) }
        ui.onNodeWithText("Abrir archivo").performKeyInput { pressKey(Key.Escape) }
        ui.runOnIdle { assertEquals(1,closed) }
    }
    @Test fun darkFilterHasStableNameRecoveryAndKeyboardFocus()=keyboardFocus("dark")
    @Test fun lightFilterHasStableNameRecoveryAndKeyboardFocus()=keyboardFocus("light")
    @Test fun contextualFilterKeepsAccessibleNameWhileEditing() {
        show(contextTitle="Informe clínico")
        ui.onNodeWithText(title).performKeyInput { keyDown(Key.CtrlLeft);pressKey(Key.F);keyUp(Key.CtrlLeft) }
        val filter=ui.onNodeWithTag("action-filter")
        filter.assertIsFocused().assertContentDescriptionEquals("Buscar acciones de Informe clínico")
        filter.performTextInput("abrir")
        filter.assertContentDescriptionEquals("Buscar acciones de Informe clínico")
        ui.onNodeWithText("Abrir archivo").assertIsDisplayed()
        ui.onNodeWithText(title).assertDoesNotExist()
        filter.performTextClearance()
        filter.assertContentDescriptionEquals("Buscar acciones de Informe clínico")
        ui.onNodeWithText(title).assertIsDisplayed()
        ui.runOnIdle { assertEquals(0,executed);assertEquals(0,closed) }
    }
}
