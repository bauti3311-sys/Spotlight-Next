package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.*
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import dev.bautista.spotlight.data.Config as AppConfig

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class DurableTaskRegistryTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 @Test fun persistedRunningMarkerAndArgumentsRestoreWithoutReplayingTheEffect() {
  var generation by mutableIntStateOf(0);var restored:Map<String,List<Any?>>?=null
  var registry:SaveableStateRegistry?=null;var effects=0
  val pending=CompletableDeferred<ToolExecution>()
  ui.setContent {
   key(generation){
    val current=remember{SaveableStateRegistry(restored,OverlaySnapshotCodec::canSave)}
    SideEffect{registry=current}
    CompositionLocalProvider(LocalSaveableStateRegistry provides current,LocalTaskCheckpoint provides {app.contexts.saveOverlaySnapshot(current.performSave())}) {
     SpotlightTheme("dark",AppConfig()) {ContextualDraftHost("task-one") {
      AiToolTaskPanel(app,AppConfig(),"Durable fixture",{},ToolPlan("",listOf(ToolCall("create_note",mapOf("text" to "Inicial")))),executeOverride={effects++;pending.await()})
     }}
    }
   }
  }
  ui.onNodeWithTag("tool-argument-0-text").performTextReplacement("Argumento conservado en disco")
  ui.onNodeWithTag("tool-execute-0").performClick()
  ui.waitUntil(10000){effects==1}
  val disk=runBlocking{ContextStore(app,app.scope,app.preferences).overlaySnapshot()}
  assertNotNull(disk);assertNotNull(registry)
  ui.runOnIdle{restored=disk;generation++}
  ui.onNodeWithText("Operación interrumpida. Su efecto externo no se puede deducir al restaurar.").assertExists()
  ui.onNodeWithTag("tool-execute-0").assertDoesNotExist()
  ui.onNodeWithText("text: Argumento conservado en disco").assertExists()
  ui.runOnIdle{assertEquals(1,effects)}
 }
 @Test fun failedDurableFlushBlocksTheSensitiveExecutor() {
  var effects=0
  ui.setContent {SpotlightTheme("dark",AppConfig()){
   CompositionLocalProvider(LocalTaskCheckpoint provides {error("No se pudo guardar la recuperación")}) {
    AiToolTaskPanel(app,AppConfig(),"Fixture",{},ToolPlan("",listOf(ToolCall("create_note",mapOf("text" to "No ejecutar")))),executeOverride={effects++;ToolExecution("unexpected")})
   }
  }}
  ui.onNodeWithTag("tool-execute-0").performClick()
  ui.onNodeWithText("No se pudo guardar la recuperación").assertExists();assertEquals(0,effects)
 }
}
