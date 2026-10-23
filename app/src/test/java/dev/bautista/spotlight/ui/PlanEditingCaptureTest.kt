package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.LocalPlan
import dev.bautista.spotlight.search.PlanStep
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class,qualifiers="w1280dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PlanEditingCaptureTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private fun capture(width:Int,name:String) {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent { SpotlightTheme { Box(Modifier.width(width.dp)) { EditablePlanPanel(app,AppConfig(),name,
            LocalPlan(listOf(PlanStep.Volume(40),PlanStep.RelativeVolume(10))),{}) } } }
        ui.waitUntil(15000) { ui.onAllNodesWithText("2. Volumen +10% → 50%").fetchSemanticsNodes().isNotEmpty() }
        ui.onRoot().captureRoboImage("build/functional-native/$name-review.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        ui.onNodeWithText("Añadir paso").performScrollTo().performClick()
        ui.onNodeWithTag("plan-new-step").performScrollTo().performTextInput("bajar volumen 5%")
        ui.onNodeWithTag("plan-add-step").performScrollTo().assertIsDisplayed().assertIsEnabled()
        ui.onRoot().captureRoboImage("build/functional-native/$name-add.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    @Test fun tabletInspectorUsesNativePlanReviewAndAddForm() = capture(500,"plan-tablet")
    @Test @Config(qualifiers="w412dp-h915dp")
    fun phoneInspectorUsesNativePlanReviewAndAddForm() = capture(390,"plan-phone")
}
