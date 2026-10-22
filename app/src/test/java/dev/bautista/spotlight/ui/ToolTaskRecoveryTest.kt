package dev.bautista.spotlight.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.CompletableDeferred
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.github.takahirom.roborazzi.*

class RecoverableToolActivity:ComponentActivity() {
    companion object {var calls=0;var launched=0}
    override fun onCreate(savedInstanceState:Bundle?) {super.onCreate(savedInstanceState);setContent {
        SpotlightTheme("dark",AppConfig(theme="dark")){ContextualDraftHost("task-one") {
            AiToolTaskPanel(application as SpotlightApp,AppConfig(),"Fixture local",{launched++},
                ToolPlan("Plan de prueba sin proveedor",listOf(ToolCall("calculate",mapOf("expression" to "2+2")),ToolCall("create_note",mapOf("text" to "Borrador inicial")))),
                executeOverride={calls++;ToolExecution("Resultado local: 4")})
        }}
    }}
}
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class,qualifiers="w900dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ToolActivityRecoveryTest {
    @get:Rule val ui=createAndroidComposeRule<RecoverableToolActivity>()
    @Test fun activityRecreationKeepsEditedArgumentsAndCompletedResultWithoutReplay() {
        RecoverableToolActivity.calls=0;RecoverableToolActivity.launched=0
        ui.onNodeWithTag("tool-argument-1-text").performScrollTo().performTextReplacement("Conservar argumento editado")
        ui.onNodeWithTag("tool-execute-0").performScrollTo().performClick()
        ui.onNodeWithText("Resultado local: 4").assertExists()
        ui.activityRule.scenario.recreate()
        ui.onNodeWithTag("tool-argument-1-text").performScrollTo().assertTextContains("Conservar argumento editado")
        ui.onNodeWithText("Resultado local: 4").assertExists()
        assertEquals(1,RecoverableToolActivity.calls);assertEquals(0,RecoverableToolActivity.launched)
        captureScreenRoboImage("build/task-recovery/activity-tool-restored.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
}
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class ToolTaskRecoveryTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun suspendedSensitiveOperationIsNotReplayedOrConfirmedAfterRestore() {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        val restore=StateRestorationTester(ui);var calls=0
        val pending=CompletableDeferred<Unit>()
        restore.setContent{ContextualDraftHost("sensitive") {
            AiToolTaskPanel(app,AppConfig(),"Tarea",{},ToolPlan("",listOf(ToolCall("create_note",mapOf("text" to "No repetir")))),executeOverride={calls++;pending.await();ToolExecution("done")})
        }}
        ui.onNodeWithTag("tool-execute-0").performScrollTo().performClick()
        ui.runOnIdle{assertEquals(1,calls)}
        restore.emulateSavedInstanceStateRestore()
        ui.onNodeWithText("Operación interrumpida. Su efecto externo no se puede deducir al restaurar.").assertExists()
        ui.onNodeWithTag("tool-execute-0").assertDoesNotExist()
        ui.runOnIdle{assertEquals(1,calls)}
    }
    @Test fun taskSwitchKeepsSeparateArguments() {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        var task by mutableStateOf("one")
        ui.setContent{ContextualDraftHost(task) {AiToolTaskPanel(app,AppConfig(),task,{},ToolPlan("",listOf(ToolCall("calculate",mapOf("expression" to "1+1")))))} }
        ui.onNodeWithTag("tool-argument-0-expression").performTextReplacement("47+2")
        ui.runOnIdle{task="two"}
        ui.onNodeWithTag("tool-argument-0-expression").assertTextContains("1+1")
        ui.runOnIdle{task="one"}
        ui.onNodeWithTag("tool-argument-0-expression").assertTextContains("47+2")
    }
}
