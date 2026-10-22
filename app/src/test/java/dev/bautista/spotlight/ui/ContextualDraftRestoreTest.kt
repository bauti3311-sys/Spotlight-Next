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
@Config(sdk=[33])
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
}
