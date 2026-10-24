package dev.bautista.spotlight.ui

import android.media.AudioManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.automation.*
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h1100dp",application=SpotlightApp::class)
class CommandTemplateFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun template()=CommandTemplate("parameter-fixture","Mi volumen","mivolumen",ToolPlan("",listOf(ToolCall("set_volume",mapOf("percent" to "40")))),
        listOf(CommandParameter("nivel",CommandParameterType.NUMBER,"40")),listOf(CommandParameterTarget(0,"percent","nivel")))
    @Test fun parametersPrepareD27ButDoNotExecuteAndSurviveRestoration() {
        val audio=app.getSystemService(AudioManager::class.java);val before=audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        val t=template();val restore=StateRestorationTester(ui)
        restore.setContent{SpotlightTheme{SavedTemplatePanel(app,AppConfig(),t){}}}
        ui.onNodeWithText("nivel").performTextReplacement("60")
        ui.onNodeWithTag("template-prepare").performClick()
        ui.onNodeWithTag("tool-sequence-start").assertExists()
        assertEquals(before,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
        restore.emulateSavedInstanceStateRestore()
        ui.onNodeWithTag("tool-sequence-start").assertExists()
        assertEquals(before,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
        ui.onNodeWithTag("tool-sequence-start").performClick()
        ui.waitUntil(15000){ui.onAllNodesWithTag("tool-sequence-status").fetchSemanticsNodes().isNotEmpty()}
        // Starting an ordered plan still pauses for the sensitive action's own confirmation.
        assertEquals(before,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
        ui.onNodeWithTag("tool-sequence-status").assertTextEquals("Revisá y confirmá este paso.")
    }
    @Test fun invalidParameterCannotPrepareOrRunTheCommand() {
        val audio=app.getSystemService(AudioManager::class.java);val before=audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        ui.setContent{SpotlightTheme{SavedTemplatePanel(app,AppConfig(),template()){}}}
        ui.onNodeWithText("nivel").performTextReplacement("101")
        ui.onNodeWithTag("template-prepare").performClick()
        ui.onNodeWithTag("tool-sequence-start").assertDoesNotExist()
        assertEquals(before,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
    }
    @Test fun editingTemplatePersistsArgumentsAndNeverChangesVolume() {
        val previous=runBlocking{app.preferences.config.first()}
        val t=template();val audio=app.getSystemService(AudioManager::class.java);val before=audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        runBlocking{app.preferences.update(previous.copy(routines="[]"));app.preferences.saveCommandTemplate(t)}
        try {
            val closed=AtomicBoolean(false)
            ui.setContent{SpotlightTheme{CommandTemplateEditor(app,t,0){closed.set(true)}}}
            ui.onNodeWithText("Valor 1 · percent").performTextReplacement("55")
            ui.onNodeWithTag("template-save").performClick()
            ui.waitUntil(15000){closed.get()}
            val restored=CommandTemplates.parse(runBlocking{app.preferences.config.first()}.routines).single()
            assertEquals(1,restored.revision)
            assertEquals("55",CommandTemplates.instantiate(restored).calls.single().arguments["percent"])
            assertEquals(before,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
        } finally {runBlocking{app.preferences.update(app.preferences.config.first().copy(routines=previous.routines))}}
    }
    @Test fun cancelEditingDoesNotWriteOrExecute() {
        val previous=runBlocking{app.preferences.config.first()};val audio=app.getSystemService(AudioManager::class.java);val before=audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        val closed=AtomicBoolean(false)
        ui.setContent{SpotlightTheme{CommandTemplateEditor(app,template(),null){closed.set(true)}}}
        ui.onNodeWithText("Valor 1 · percent").performTextReplacement("75")
        ui.onNodeWithText("Cancelar edición").performClick()
        assertTrue(closed.get());assertEquals(previous.routines,runBlocking{app.preferences.config.first()}.routines)
        assertEquals(before,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
    }
}
