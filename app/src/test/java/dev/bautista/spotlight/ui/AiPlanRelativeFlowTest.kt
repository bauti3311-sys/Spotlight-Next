package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
class AiPlanRelativeFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private val initial=ToolPlan("Propuesta local de prueba",listOf(
        ToolCall("set_volume",mapOf("percent" to "40")),ToolCall("set_volume",mapOf("percent" to "10"))))
    @Test fun relativeDraftRestoresAndEachResolvedActionStillNeedsConfirmation() {
        val calls=mutableListOf<ToolCall>();val restore=StateRestorationTester(ui)
        restore.setContent{ContextualDraftHost("relative-ai"){
            AiToolTaskPanel(app,AppConfig(),"Volumen encadenado",{},initial,executeOverride={calls+=it;ToolExecution("Cambio observado",state=ExecutionState.VERIFIED)})
        }}
        ui.onNodeWithTag("tool-relative-1").performScrollTo().performClick()
        ui.onNodeWithTag("tool-target-1").assertTextEquals("Objetivo revisado: 50%")
        ui.onNodeWithTag("tool-execute-1").performScrollTo().assertIsNotEnabled()
        restore.emulateSavedInstanceStateRestore()
        ui.onNodeWithTag("tool-target-1").performScrollTo().assertTextEquals("Objetivo revisado: 50%")
        assertTrue(calls.isEmpty())
        ui.onNodeWithTag("tool-execute-0").performScrollTo().performClick()
        ui.waitUntil(10000){calls.size==1}
        ui.onNodeWithTag("tool-execute-1").performScrollTo().assertIsEnabled().performClick()
        ui.waitUntil(10000){calls.size==2}
        assertEquals(listOf("40","50"),calls.map{it.arguments["percent"]})
        restore.emulateSavedInstanceStateRestore()
        assertEquals(2,calls.size)
        ui.onNodeWithTag("tool-execute-1").assertDoesNotExist()
    }
    @Test fun cancelledPredecessorNeverUnlocksOrReplaysItsDependent() {
        var calls=0;val pending=CompletableDeferred<Unit>();val restore=StateRestorationTester(ui)
        restore.setContent{ContextualDraftHost("cancel-relative"){
            AiToolTaskPanel(app,AppConfig(),"Cancelar cadena",{},initial,executeOverride={calls++;pending.await();ToolExecution("Unreachable",state=ExecutionState.VERIFIED)})
        }}
        ui.onNodeWithTag("tool-relative-1").performScrollTo().performClick()
        ui.onNodeWithTag("tool-execute-0").performScrollTo().performClick()
        ui.runOnIdle{assertEquals(1,calls)}
        ui.onNodeWithText("Cancelar").performScrollTo().performClick()
        restore.emulateSavedInstanceStateRestore()
        ui.onNodeWithTag("tool-execute-1").performScrollTo().assertIsNotEnabled()
        ui.onNodeWithTag("tool-execute-0").assertDoesNotExist()
        assertEquals(1,calls)
    }
    @Test fun invalidEditedArgumentCannotExecuteAndCanBeCorrectedWithoutInterruption() {
        var calls=0
        ui.setContent{AiToolTaskPanel(app,AppConfig(),"Validación",{},initial,executeOverride={calls++;ToolExecution("done")})}
        ui.onNodeWithTag("tool-argument-0-percent").performScrollTo().performTextReplacement("no válido")
        ui.onNodeWithTag("tool-execute-0").performScrollTo().assertIsNotEnabled()
        ui.onNodeWithTag("tool-argument-0-percent").performScrollTo().performTextReplacement("60")
        ui.onNodeWithTag("tool-execute-0").performScrollTo().assertIsEnabled()
        assertEquals(0,calls)
    }
}
