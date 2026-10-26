package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

/** Actual local tool adapters and DataStore; Android runtime remains simulated. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
class StructuredNativePlanFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun nativeTransformNoteAndCalculationRespectConfirmationAndDoNotDuplicateAfterRestore() {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        val before=runBlocking{app.preferences.noteData.first()}
        val plan=ToolPlan("",listOf(
            ToolCall("transform_text",mapOf("operation" to "mayusculas","text" to "No enviar mensajes")),
            ToolCall("create_note",mapOf("text" to "")),ToolCall("calculate",mapOf("expression" to "1+1"))))
        val restore=StateRestorationTester(ui)
        restore.setContent{ContextualDraftHost("native-chain"){AiToolTaskPanel(app,AppConfig(),"Cadena nativa",{},plan)}}
        ui.onNodeWithTag("tool-bind-1-text-0-text").performScrollTo().performClick()
        ui.onNodeWithTag("tool-sequence-start").performScrollTo().performClick()
        ui.waitUntil(15000){ui.onAllNodesWithText("Revisá y confirmá este paso.").fetchSemanticsNodes().isNotEmpty()}
        assertEquals(before,runBlocking{app.preferences.noteData.first()})
        ui.onNodeWithTag("tool-resolved-1-text").performScrollTo().assertTextEquals("text revisado: NO ENVIAR MENSAJES")
        restore.emulateSavedInstanceStateRestore()
        assertEquals(before,runBlocking{app.preferences.noteData.first()})
        ui.onNodeWithTag("tool-execute-1").performScrollTo().performClick()
        ui.waitUntil(15000){ui.onAllNodesWithText("Plan terminado; estados de cada paso conservados.").fetchSemanticsNodes().isNotEmpty()}
        val after=runBlocking{app.preferences.noteData.first()}
        assertEquals("NO ENVIAR MENSAJES",JSONArray(after).getJSONObject(0).getString("text"))
        restore.emulateSavedInstanceStateRestore()
        assertEquals(after,runBlocking{app.preferences.noteData.first()})
        ui.onNodeWithTag("tool-execute-1").assertDoesNotExist()
    }
}
