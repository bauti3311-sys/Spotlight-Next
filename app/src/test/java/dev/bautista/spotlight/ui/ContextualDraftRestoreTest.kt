package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import dev.bautista.spotlight.search.Action
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=dev.bautista.spotlight.SpotlightApp::class)
class ContextualDraftRestoreTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun searchDraftSurvivesLeavingItsContextAndRecreationWithoutRunning() {
        var current by mutableStateOf("first")
        var runs=0
        val restore=StateRestorationTester(ui)
        restore.setContent {
            ContextualDraftHost(current) {
                if(current=="first")SearchEditor(Action.ComposeSearch("youtube","com.google.android.youtube"),{runs++},{})
                else SearchEditor(Action.ComposeSearch("spotify","com.spotify.music"),{runs++},{})
            }
        }
        ui.onNode(hasSetTextAction()).performTextInput("Cerati en vivo")
        ui.runOnIdle{current="second"}
        ui.onNode(hasSetTextAction()).assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText,androidx.compose.ui.text.AnnotatedString(""))).performTextInput("Bocanada")
        ui.runOnIdle{current="first"}
        ui.onNode(hasSetTextAction()).assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText,androidx.compose.ui.text.AnnotatedString("Cerati en vivo")))
        restore.emulateSavedInstanceStateRestore()
        ui.onNode(hasSetTextAction()).assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText,androidx.compose.ui.text.AnnotatedString("Cerati en vivo")))
        ui.runOnIdle{org.junit.Assert.assertEquals(0,runs)}
    }
    @Test fun editedPlanSurvivesContextSwitchAndRecreationWithoutExecuting() {
        var current by mutableStateOf("plan")
        val app=org.robolectric.RuntimeEnvironment.getApplication() as dev.bautista.spotlight.SpotlightApp
        val initial=dev.bautista.spotlight.search.LocalPlan(listOf(dev.bautista.spotlight.search.PlanStep.Volume(20),dev.bautista.spotlight.search.PlanStep.Volume(30)))
        val audio=app.getSystemService(android.media.AudioManager::class.java)
        val originalVolume=audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        val restore=StateRestorationTester(ui)
        restore.setContent { ContextualDraftHost(current) {
            if(current=="plan")EditablePlanPanel(app,dev.bautista.spotlight.data.Config(),"volume-test",initial,{})
            else SearchEditor(Action.ComposeSearch("youtube","com.google.android.youtube"),{},{})
        } }
        ui.onAllNodesWithText("Editar")[0].performClick()
        ui.onNode(hasSetTextAction()).performTextReplacement("volumen al 47%")
        ui.runOnIdle{current="other"}
        ui.runOnIdle{current="plan"}
        ui.onNode(hasSetTextAction()).assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText,androidx.compose.ui.text.AnnotatedString("volumen al 47%")))
        restore.emulateSavedInstanceStateRestore()
        ui.onNode(hasSetTextAction()).assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText,androidx.compose.ui.text.AnnotatedString("volumen al 47%")))
        ui.onNodeWithText("Confirmar pasos seleccionados").assertExists()
        org.junit.Assert.assertEquals(originalVolume,audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC))
    }
}
