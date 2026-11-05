package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PremiumInteractionTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun fadeDoesNotDuplicateContentResizeItsOwnerOrTakeEditorFocus() {
        var identity by mutableStateOf("a")
        var query by mutableStateOf("consulta conservada")
        ui.setContent{SpotlightTheme("dark") { Column(Modifier.width(360.dp)) {
            BasicTextField(query,{query=it},Modifier.testTag("review-editor"))
            PremiumInspectorReveal(identity,true){Box(Modifier.height(180.dp).fillMaxWidth().testTag("review-reveal")){Text("Contenido $identity")}}
        }}}
        ui.onNodeWithTag("review-editor").performClick().assertIsFocused()
        val bounds=ui.onNodeWithTag("review-reveal").getUnclippedBoundsInRoot()
        ui.mainClock.autoAdvance=false
        ui.runOnIdle{identity="b"}
        ui.mainClock.advanceTimeBy(32)
        assertEquals(bounds,ui.onNodeWithTag("review-reveal").getUnclippedBoundsInRoot())
        ui.onAllNodesWithTag("review-reveal").assertCountEquals(1)
        ui.onNodeWithTag("review-editor").assertIsFocused().assertTextEquals("consulta conservada")
        ui.mainClock.advanceTimeBy(160)
        ui.onNodeWithText("Contenido b").assertIsDisplayed()
        ui.mainClock.autoAdvance=true
    }
    @Test fun touchFooterKeepsIndependentAccessibleCallbacksAtTwoHundredPercentText() {
        var opens=0;var actions=0
        ui.setContent{
            val density=LocalDensity.current
            CompositionLocalProvider(LocalPremiumReview provides true,LocalDensity provides Density(density.density,2f)) {
                SpotlightTheme("light"){Box(Modifier.width(360.dp)) {
                    GlassContextFooter(false,true,"Preparar envío",{opens++},{actions++},onOptions={})
                }}
            }
        }
        ui.onNodeWithTag("command-primary").assertHeightIsAtLeast(48.dp).assertIsDisplayed().performClick()
        ui.onNodeWithTag("command-actions").assertHeightIsAtLeast(48.dp).assertIsDisplayed().performClick()
        assertEquals(1,opens);assertEquals(1,actions)
    }
}
