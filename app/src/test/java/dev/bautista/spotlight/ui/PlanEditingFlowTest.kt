package dev.bautista.spotlight.ui

import android.media.AudioManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.LocalPlan
import dev.bautista.spotlight.search.PlanStep
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], qualifiers="w1280dp-h800dp", application=SpotlightApp::class)
class PlanEditingFlowTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val app get() = RuntimeEnvironment.getApplication() as SpotlightApp
    private fun ready() = ui.waitUntil(15000) {
        ui.onAllNodes(hasText("Confirmar pasos seleccionados") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
    }
    @Test fun addingRelativeStepSurvivesRestoreAndNeverExecutesBeforeConfirmation() {
        val audio = app.getSystemService(AudioManager::class.java)
        val before = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        val restore = StateRestorationTester(ui)
        restore.setContent { SpotlightTheme { EditablePlanPanel(app, AppConfig(), "add-flow",
            LocalPlan(listOf(PlanStep.Volume(40))), {}) } }
        ready()
        ui.onNodeWithText("Añadir paso").performScrollTo().performClick()
        ui.onNodeWithTag("plan-new-step").performScrollTo().performTextInput("subir volumen 10%")
        restore.emulateSavedInstanceStateRestore()
        ui.onNodeWithTag("plan-new-step").assertTextContains("subir volumen 10%")
        ui.onNodeWithTag("plan-add-step").performScrollTo().performClick()
        ready()
        ui.onNodeWithText("2. Volumen +10% → 50%").assertExists()
        restore.emulateSavedInstanceStateRestore()
        ready()
        ui.onNodeWithText("2. Volumen +10% → 50%").assertExists()
        assertEquals(before, audio.getStreamVolume(AudioManager.STREAM_MUSIC))
    }
    @Test fun unsupportedStepCannotBeAddedAndSixStepLimitIsEnforced() {
        ui.setContent { SpotlightTheme { EditablePlanPanel(app, AppConfig(), "add-limit",
            LocalPlan(List(5) { PlanStep.Volume(40) }), {}) } }
        ready()
        ui.onNodeWithText("Añadir paso").performScrollTo().performClick()
        ui.onNodeWithTag("plan-new-step").performScrollTo().performTextInput("borrar documentos")
        ui.onNodeWithTag("plan-add-step").assertIsNotEnabled()
        ui.onNodeWithTag("plan-new-step").performTextReplacement("volumen 50%")
        ui.onNodeWithTag("plan-add-step").performScrollTo().performClick()
        ready()
        ui.onNodeWithTag("plan-step-5").assertExists()
        ui.onNodeWithText("Añadir paso").performScrollTo().assertIsNotEnabled()
        ui.onAllNodesWithText("Quitar")[5].performScrollTo().performClick()
        ready()
        ui.onNodeWithTag("plan-step-5").assertDoesNotExist()
        ui.onNodeWithText("Añadir paso").performScrollTo().assertIsEnabled()
    }
}
