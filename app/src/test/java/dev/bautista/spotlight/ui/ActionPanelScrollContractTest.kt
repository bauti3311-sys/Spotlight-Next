package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
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

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ActionPanelScrollContractTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val focus=FocusRequester()
    private val executed=mutableListOf<Int>()
    private var closed=0
    private val shortTitles=listOf("Abrir","Copiar","Compartir","Información","Favorita","Cerrar")
    private val longTitles=(1..12).map { "Acción $it del documento seleccionado" }
    private fun show(width:Int=320,large:Boolean=false) {
        ui.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,if(large)2f else 1f)) {
                SpotlightTheme("dark",AppConfig(theme="dark",preset="glass",animations=false,
                    textScale=1.15f,uiDensityScale=.85f)) {
                    Surface(color=MaterialTheme.colorScheme.surface) {
                        Box(Modifier.width(width.dp).heightIn(max=if(large)420.dp else 560.dp).testTag("action-panel-frame")) {
                            ActionPanelView((if(large)longTitles else shortTitles).mapIndexed { i,title ->
                                ContextAction(title,if(large)"Revisar el destino" else "",CommandIcons.Apps,
                                    {executed+=i},id="action:$i")
                            },focus,{closed++})
                        }
                    }
                }
            }
        }
        ui.runOnIdle { focus.requestFocus() }
    }
    private fun capture(name:String)=ui.onNodeWithTag("action-panel-frame").captureRoboImage(
        "build/action-panel-scroll/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    private fun shortDensity(width:Int,name:String) {
        show(width)
        ui.onNodeWithTag("action-panel-frame").assertWidthIsEqualTo(width.dp)
        shortTitles.forEach { title ->
            ui.onNodeWithText(title).assertIsDisplayed().assertHeightIsAtLeast(48.dp)
                .assertHeightIsEqualTo(48.dp)
        }
        capture(name)
        ui.runOnIdle { assertTrue(executed.isEmpty());assertEquals(0,closed) }
    }
    @Test fun phoneShortActionsKeepCompactDensity()=shortDensity(320,"phone-compact")
    @Test @Config(qualifiers="w800dp-h1000dp")
    fun tabletShortActionsKeepCompactDensity()=shortDensity(720,"tablet-compact")

    private fun assertFullyReadable(title:String) {
        val layouts=mutableListOf<TextLayoutResult>()
        ui.onNodeWithText(title,useUnmergedTree=true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertFalse("Text must wrap without ellipsis: $title",layouts.single().hasVisualOverflow)
    }
    @Test fun enlargedTextScrollAndKeyboardReachLastActionWithoutExecuting() {
        show(large=true)
        val panel=ui.onNodeWithTag("context-action-panel")
        val panelBounds=panel.fetchSemanticsNode().boundsInRoot
        val pxPerDp=org.robolectric.RuntimeEnvironment.getApplication().resources.displayMetrics.density
        assertTrue("Keep the constrained menu height",panelBounds.height<=420*pxPerDp+1)
        val scroll=ui.onNode(hasScrollAction())
        fun offset()=scroll.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()
        assertTrue("The fixture must really overflow",scroll.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].maxValue()>0)
        val initial=offset()
        scroll.performTouchInput { swipeUp() }
        ui.waitForIdle()
        assertTrue("A touch drag must move the list",offset()>initial)
        ui.runOnIdle { assertTrue(executed.isEmpty()) }
        ui.onNodeWithText(longTitles.first()).performScrollTo().assertIsDisplayed()
        assertFullyReadable(longTitles.first())
        ui.runOnIdle { focus.requestFocus() }
        repeat(longTitles.size-1) { i ->
            ui.onNodeWithText(longTitles[i]).assertIsFocused().performKeyInput { pressKey(Key.Tab) }
        }
        val last=ui.onNodeWithText(longTitles.last())
        last.assertIsFocused().assertIsDisplayed()
        assertFullyReadable(longTitles.last())
        val lastBounds=last.fetchSemanticsNode().boundsInRoot
        val filterBounds=ui.onNodeWithTag("action-filter").fetchSemanticsNode().boundsInRoot
        assertTrue("Focused last action must be below the fixed filter",lastBounds.top>=filterBounds.bottom-1)
        assertTrue("Focused last action must fit inside menu",lastBounds.bottom<=panelBounds.bottom+1)
        capture("phone-large-text-last-action")
        ui.runOnIdle { assertTrue(executed.isEmpty()) }
        last.performKeyInput { keyDown(Key.ShiftLeft);pressKey(Key.Tab);keyUp(Key.ShiftLeft) }
        ui.onNodeWithText(longTitles[10]).assertIsFocused().assertIsDisplayed()
            .performKeyInput { pressKey(Key.DirectionDown) }
        last.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        ui.runOnIdle { assertEquals(listOf(11),executed);assertEquals(0,closed) }
        last.performKeyInput { pressKey(Key.Escape) }
        ui.runOnIdle { assertEquals(listOf(11),executed);assertEquals(1,closed) }
    }
}
