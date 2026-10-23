package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.ContextStore
import dev.bautista.spotlight.data.OverlaySnapshotCodec
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
class DurablePlanSequenceTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private val calc=ToolCall("calculate",mapOf("expression" to "40+10"))
    @Test fun diskSnapshotRestoresTypedResultAndPendingConfirmationWithoutReplay() {
        var visible by mutableStateOf(true);var restored:Map<String,List<Any?>>?=null;var calls=0
        val initial=ToolPlan("",listOf(calc,ToolCall("set_volume",mapOf("percent" to "0"))))
        ui.setContent{if(visible){
            val registry=remember{SaveableStateRegistry(restored,OverlaySnapshotCodec::canSave)}
            CompositionLocalProvider(LocalSaveableStateRegistry provides registry,LocalTaskCheckpoint provides {app.contexts.saveOverlaySnapshot(registry.performSave())}){
                ContextualDraftHost("disk-sequence") {
                    AiToolTaskPanel(app,AppConfig(),"Disco",{},initial,executeOverride={
                        calls++
                        ToolExecution("fixture",state=ExecutionState.VERIFIED,outputs=mapOf(
                            "value" to ToolValue(ToolValueType.NUMBER,"50"),
                            "percent" to ToolValue(ToolValueType.NUMBER,"50")))
                    })
                }
            }
        }}
        ui.onNodeWithTag("tool-bind-1-percent-0-value").performScrollTo().performClick()
        ui.onNodeWithTag("tool-sequence-start").performScrollTo().performClick()
        ui.waitUntil(10000){calls==1 && ui.onAllNodesWithText("Revisá y confirmá este paso.").fetchSemanticsNodes().isNotEmpty()}
        ui.waitForIdle()
        val disk=runBlocking{ContextStore(app,app.scope,app.preferences).overlaySnapshot()};assertNotNull(disk)
        ui.runOnIdle{visible=false};ui.waitForIdle();ui.runOnIdle{restored=disk;visible=true}
        ui.onNodeWithTag("tool-resolved-1-percent").performScrollTo().assertTextEquals("percent revisado: 50")
        ui.onNodeWithTag("tool-execute-1").performScrollTo().assertIsEnabled()
        assertEquals(1,calls)
        ui.onNodeWithTag("tool-execute-1").performClick()
        ui.waitUntil(10000){calls==2}
        ui.waitForIdle()
        val done=runBlocking{ContextStore(app,app.scope,app.preferences).overlaySnapshot()}
        ui.runOnIdle{visible=false};ui.waitForIdle();ui.runOnIdle{restored=done;visible=true}
        ui.onNodeWithTag("tool-execute-1").assertDoesNotExist();assertEquals(2,calls)
    }
    @Test fun corruptReceiptAndBindingSnapshotsFailClosed() {
        assertTrue(ToolOutcomesSaver.restore("not valid json")!!.containsKey(-1))
        val bindings=ToolBindingsSaver.restore("not valid json")!!
        assertTrue(runCatching{StructuredToolResults.validate(listOf(calc),bindings,emptySet())}.isFailure)
    }
    @Test fun sequenceCannotExecuteWhenDurableCheckpointFails() {
        var calls=0
        ui.setContent{CompositionLocalProvider(LocalTaskCheckpoint provides {error("No se pudo guardar la recuperación")}){
            AiToolTaskPanel(app,AppConfig(),"Sin disco",{},ToolPlan("",listOf(calc)),executeOverride={calls++;ToolExecution("never")})
        }}
        ui.onNodeWithTag("tool-sequence-start").performScrollTo().performClick()
        ui.waitUntil(10000){ui.onAllNodesWithText("No se pudo guardar la recuperación").fetchSemanticsNodes().isNotEmpty()}
        assertEquals(0,calls)
    }
}
