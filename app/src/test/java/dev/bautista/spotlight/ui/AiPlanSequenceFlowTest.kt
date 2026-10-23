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
class AiPlanSequenceFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private val calc=ToolCall("calculate",mapOf("expression" to "40+10"))
    private val volume=ToolCall("set_volume",mapOf("percent" to "0"))
    @Test fun typedBindingSequencePausesForConfirmationAndRestoresWithoutRepeatingReads() {
        val executed=mutableListOf<ToolCall>();val restore=StateRestorationTester(ui)
        restore.setContent{ContextualDraftHost("typed-sequence"){
            AiToolTaskPanel(app,AppConfig(),"Resultado tipado",{},ToolPlan("",listOf(calc,volume)),executeOverride={call->
                executed+=call
                ToolExecution("Texto descriptivo no ejecutable",state=ExecutionState.VERIFIED,outputs=mapOf((if(call.name=="calculate")"value" else "percent") to ToolValue(ToolValueType.NUMBER,"50")))
            })
        }}
        ui.onNodeWithTag("tool-bind-1-percent-0-value").performScrollTo().performClick()
        restore.emulateSavedInstanceStateRestore()
        ui.onNodeWithTag("tool-binding-1-percent").performScrollTo().assertExists()
        assertTrue(executed.isEmpty())
        ui.onNodeWithTag("tool-sequence-start").performScrollTo().performClick()
        ui.waitUntil(10000){executed.size==1}
        ui.onNodeWithTag("tool-execute-1").performScrollTo().assertIsEnabled()
        restore.emulateSavedInstanceStateRestore()
        assertEquals(1,executed.size)
        ui.onNodeWithTag("tool-execute-1").performScrollTo().performClick()
        ui.waitUntil(10000){executed.size==2}
        assertEquals("50",executed[1].arguments["percent"])
        restore.emulateSavedInstanceStateRestore()
        assertEquals(2,executed.size)
        ui.onNodeWithTag("tool-execute-1").assertDoesNotExist()
        ui.onNodeWithTag("tool-sequence-trace").assertExists()
    }
    @Test fun cancellationPersistsFenceAndNeverContinuesLaterSteps() {
        var calls=0;val pending=CompletableDeferred<Unit>();val restore=StateRestorationTester(ui)
        restore.setContent{ContextualDraftHost("cancel-sequence"){
            AiToolTaskPanel(app,AppConfig(),"Secuencia cancelada",{},ToolPlan("",listOf(calc,calc)),executeOverride={calls++;pending.await();ToolExecution("late",state=ExecutionState.VERIFIED)})
        }}
        ui.onNodeWithTag("tool-sequence-start").performScrollTo().performClick()
        ui.runOnIdle{assertEquals(1,calls)}
        ui.onNodeWithText("Cancelar").performScrollTo().performClick()
        restore.emulateSavedInstanceStateRestore()
        assertEquals(1,calls)
        ui.onNodeWithTag("tool-sequence-start").performScrollTo().assertIsNotEnabled()
        ui.onNodeWithTag("tool-execute-1").performScrollTo().assertIsNotEnabled()
    }
    @Test fun brightnessRelativeDraftRecalculatesAndRestoresWithoutPermissionsOrExecution() {
        var calls=0;val restore=StateRestorationTester(ui)
        fun bright(v:String)=ToolCall("write_display_setting",mapOf("key" to "screen_brightness","value" to v))
        restore.setContent{ContextualDraftHost("bright-draft"){
            AiToolTaskPanel(app,AppConfig(),"Brillo relativo",{},ToolPlan("",listOf(bright("102"),bright("10"))),executeOverride={calls++;ToolExecution("never")})
        }}
        ui.onNodeWithTag("tool-brightness-relative-1").performScrollTo().performClick()
        ui.onNodeWithTag("tool-brightness-target-1").assertTextEquals("Objetivo revisado: 50%")
        restore.emulateSavedInstanceStateRestore()
        ui.onNodeWithTag("tool-brightness-target-1").performScrollTo().assertTextEquals("Objetivo revisado: 50%")
        ui.onNodeWithTag("tool-execute-1").performScrollTo().assertIsNotEnabled()
        assertEquals(0,calls)
    }
}
