package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
class AiPlanEditingFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private val initial=ToolPlan("Propuesta IA simulada",listOf(
        ToolCall("calculate",mapOf("expression" to "1+1")),
        ToolCall("create_note",mapOf("text" to "No guardar automáticamente"))))
    @Test fun addEditReorderRemoveAndRestoreRequireExplicitExecution() {
        val executed=mutableListOf<ToolCall>()
        val restore=StateRestorationTester(ui)
        restore.setContent{ContextualDraftHost("edit-ai"){
            AiToolTaskPanel(app,AppConfig(),"Plan editable",{},initial,executeOverride={executed+=it;ToolExecution("Resultado confirmado")})
        }}
        ui.onNodeWithTag("tool-add").performScrollTo().performClick()
        ui.waitUntil(10000){ui.onAllNodesWithTag("tool-add-calculate").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("tool-add-calculate").performScrollTo().performClick()
        ui.onNodeWithTag("tool-argument-2-expression").performScrollTo().performTextReplacement("40+10")
        ui.onNodeWithTag("tool-up-2").performScrollTo().performClick()
        ui.onNodeWithTag("tool-argument-1-expression").performScrollTo().assertTextContains("40+10")
        ui.onNodeWithTag("tool-remove-0").performScrollTo().performClick()
        ui.onNodeWithTag("tool-include-1").performScrollTo().performClick()
        restore.emulateSavedInstanceStateRestore()
        ui.onNodeWithTag("tool-argument-0-expression").performScrollTo().assertTextContains("40+10")
        ui.onNodeWithTag("tool-include-1").performScrollTo().assertIsOff()
        ui.onNodeWithTag("tool-execute-1").assertDoesNotExist()
        assertTrue(executed.isEmpty())
        ui.onNodeWithTag("tool-execute-0").performScrollTo().performClick()
        ui.onNodeWithText("Resultado confirmado").assertExists()
        assertEquals(listOf(ToolCall("calculate",mapOf("expression" to "40+10"))),executed)
        restore.emulateSavedInstanceStateRestore()
        assertEquals(1,executed.size)
        ui.onNodeWithTag("tool-add").performScrollTo().assertIsNotEnabled()
        ui.onNodeWithTag("tool-execute-0").assertDoesNotExist()
    }
    @Test fun removingAllProposedStepsNeverExecutesAndCanAddAgain() {
        var calls=0
        ui.setContent{AiToolTaskPanel(app,AppConfig(),"Vaciar",{},initial,executeOverride={calls++;ToolExecution("done")})}
        ui.onNodeWithTag("tool-remove-1").performScrollTo().performClick()
        ui.onNodeWithTag("tool-remove-0").performScrollTo().performClick()
        ui.onNodeWithTag("tool-execute-0").assertDoesNotExist()
        ui.onNodeWithTag("tool-add").performScrollTo().assertIsEnabled()
        assertEquals(0,calls)
    }
}
