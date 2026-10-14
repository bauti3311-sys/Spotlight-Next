package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
class SearchEditingRegressionTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private fun show() {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent { SpotlightPanel(app,AppConfig(files=false,animations=false,aliases=setOf("Cerati música|test/package")),{},initialQuery="Cerati") }
        ui.onNode(hasSetTextAction()).assertIsFocused()
    }
    @Test fun rightArrowDoesNotCompleteOrLeaveEditor() {
        show()
        val editor=ui.onNode(hasSetTextAction())
        editor.performKeyInput { pressKey(Key.DirectionLeft);pressKey(Key.DirectionRight) }
        editor.assertTextEquals("Cerati").assertIsFocused()
        editor.performKeyInput { pressKey(Key.DirectionRight) }
        editor.assertTextEquals("Cerati").assertIsFocused()
    }
    @Test fun homeEndAndShiftRightPreserveNativeEditing() {
        show()
        val editor=ui.onNode(hasSetTextAction())
        editor.performKeyInput { pressKey(Key.MoveHome);keyDown(Key.ShiftLeft);pressKey(Key.DirectionRight);keyUp(Key.ShiftLeft) }
        editor.performTextInput("X")
        editor.assertTextEquals("Xerati").assertIsFocused()
        editor.performKeyInput { pressKey(Key.MoveEnd) }
        editor.performTextInput("!")
        editor.assertTextEquals("Xerati!").assertIsFocused()
    }
    @Test fun shiftArrowsSelectWithoutAcceptingSuggestion() {
        show()
        val editor=ui.onNode(hasSetTextAction())
        editor.performKeyInput { pressKey(Key.MoveEnd);keyDown(Key.ShiftLeft);pressKey(Key.DirectionLeft);keyUp(Key.ShiftLeft) }
        editor.performTextInput("X")
        editor.assertTextEquals("CeratX").assertIsFocused()
    }
}
