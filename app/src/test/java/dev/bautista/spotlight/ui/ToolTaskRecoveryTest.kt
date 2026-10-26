package dev.bautista.spotlight.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
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
    val ui=createAndroidComposeRule<RecoverableToolActivity>()
    @get:Rule val rules=org.junit.rules.RuleChain.outerRule(registerFixtureActivity(RecoverableToolActivity::class.java)).around(ui)
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
    }    @Test fun partialWorkflowOutputSurvivesRestoreWithoutContinuingSensitiveSteps() {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        val audio=app.getSystemService(android.media.AudioManager::class.java);val original=audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        val restore=StateRestorationTester(ui)
        val workflow=dev.bautista.spotlight.automation.Workflow("partial","Parcial",steps=listOf(dev.bautista.spotlight.automation.WorkflowStep("text","Resultado parcial conservado"),dev.bautista.spotlight.automation.WorkflowStep("volume","0")))
        restore.setContent{ContextualDraftHost("workflow"){WorkflowRunner(workflow,app.local){}}}
        ui.onNodeWithText("Ejecutar").performClick()
        ui.waitUntil(10000){ui.onAllNodesWithText("Confirmar paso").fetchSemanticsNodes().isNotEmpty()}
        restore.emulateSavedInstanceStateRestore()
        ui.onNodeWithText("Resultado parcial conservado").assertExists()
        ui.onNodeWithText("2/2 · Interrumpida · revisar antes de repetir").assertExists()
        assertEquals(original,audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC))
    }

    @Test fun unfinishedArgumentStillRestoresAsAnEditableDraftWithoutExecution() {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        val restore=StateRestorationTester(ui)
        restore.setContent{ContextualDraftHost("unfinished"){AiToolTaskPanel(app,AppConfig(),"Tarea",{},ToolPlan("",listOf(ToolCall("create_note",mapOf("text" to "Inicial")))))}}
        ui.onNodeWithTag("tool-argument-0-text").performTextClearance()
        restore.emulateSavedInstanceStateRestore()
        ui.onNodeWithTag("tool-argument-0-text").assertExists()
        ui.onNodeWithTag("tool-execute-0").assertExists()
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

class RecoverableSearchActivity:ComponentActivity() {
 override fun onCreate(savedInstanceState:Bundle?) {super.onCreate(savedInstanceState);setContent {
  androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.width(800.dp)) {
   SpotlightPanel(application as SpotlightApp,AppConfig(theme="dark",files=false,online=false,learning=false,suggestions=false,animations=false),{},"youtube",availableHeightDp=700f)
  }
 }}
}
@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class,qualifiers="w1000dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SearchActivityRecoveryTest {
 val ui=createAndroidComposeRule<RecoverableSearchActivity>()
 @get:Rule val rules=org.junit.rules.RuleChain.outerRule(registerFixtureActivity(RecoverableSearchActivity::class.java)).around(ui)
 @Test fun activityRecreationRestoresTheActualInspectorFormAndQuery() {
  fun search()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
  ui.waitUntil(15000){ui.onAllNodesWithTag("result:catalog:youtube:search").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithTag("result:catalog:youtube:search").performClick();search().performKeyInput{pressKey(androidx.compose.ui.input.key.Key.Enter)}
  ui.onNodeWithText("Qué querés buscar").performTextReplacement("Cerati conservado")
  ui.activityRule.scenario.recreate()
  ui.onNodeWithText("Qué querés buscar").assertTextContains("Cerati conservado")
  search().assertTextEquals("youtube")
  captureScreenRoboImage("build/task-recovery/activity-form-restored.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
 }
}

private fun registerFixtureActivity(type:Class<out ComponentActivity>)=object:org.junit.rules.ExternalResource() {
 override fun before() {
  val app=RuntimeEnvironment.getApplication() as SpotlightApp
  val component=android.content.ComponentName(app,type)
  val manager=Shadows.shadowOf(app.packageManager)
  val info=manager.addActivityIfNotPresent(component);info.exported=true
  manager.addResolveInfoForIntent(android.content.Intent.makeMainActivity(component),android.content.pm.ResolveInfo().apply{activityInfo=info})
 }
}

class RecoverableAiActivity:ComponentActivity() {
 companion object {lateinit var server:okhttp3.mockwebserver.MockWebServer}
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  val client=dev.bautista.spotlight.ai.DirectAi(object:dev.bautista.spotlight.ai.AiCredentials {
   override fun read(provider:String)="fixture-key";override fun contains(provider:String)=true
  },okhttp3.OkHttpClient.Builder().addInterceptor{chain->chain.proceed(chain.request().newBuilder().url(server.url("/fixture")).build())}.build())
  setContent{androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.width(800.dp)) {
   SpotlightPanel(application as SpotlightApp,AppConfig(theme="dark",aiProvider="gemini",aiModel="fixture-model",files=false,online=false,learning=false,suggestions=false,animations=false),{},"Explicame el reflejo vestibuloocular",availableHeightDp=800f,aiClient=client)
  }}
 }
}
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class,qualifiers="w1000dp-h1000dp")
class AiActivityRecoveryTest {
 val ui=createAndroidComposeRule<RecoverableAiActivity>()
 private val serverRule=object:org.junit.rules.ExternalResource(){
  override fun before(){val server=okhttp3.mockwebserver.MockWebServer();server.start();RecoverableAiActivity.server=server
   for(text in listOf("Respuesta conservada","Seguimiento conservado"))server.enqueue(okhttp3.mockwebserver.MockResponse().setHeader("Content-Type","text/event-stream").setBody("data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"$text\"}]}}]}\n\n"))
  }
  override fun after(){RecoverableAiActivity.server.shutdown()}
 }
 @get:Rule val rules=org.junit.rules.RuleChain.outerRule(serverRule).around(registerFixtureActivity(RecoverableAiActivity::class.java)).around(ui)
 @Test fun completedConversationSurvivesActivityRecreationWithoutResendingAndRetainsTurns() {
  fun search()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
  ui.waitUntil(15000){ui.onAllNodesWithText("Responder con Gemini").fetchSemanticsNodes().isNotEmpty()}
  search().performKeyInput{pressKey(androidx.compose.ui.input.key.Key.Enter)}
  ui.waitUntil(15000){ui.onAllNodesWithText("Respuesta conservada").fetchSemanticsNodes().isNotEmpty()}
  ui.activityRule.scenario.recreate()
  ui.onNodeWithText("Respuesta conservada").assertExists()
  assertEquals(1,RecoverableAiActivity.server.requestCount)
  ui.onNode(hasSetTextAction() and hasText("Continuar conversación")).performScrollTo().performTextInput("Dame un ejemplo")
  ui.onNodeWithText("Enviar").performScrollTo().performClick()
  ui.waitUntil(15000){ui.onAllNodesWithText("Seguimiento conservado").fetchSemanticsNodes().isNotEmpty()}
  val server=RecoverableAiActivity.server;assertEquals(2,server.requestCount)
  server.takeRequest(2,java.util.concurrent.TimeUnit.SECONDS)
  val contents=org.json.JSONObject(server.takeRequest(2,java.util.concurrent.TimeUnit.SECONDS)!!.body.readUtf8()).getJSONArray("contents")
  assertEquals(3,contents.length());assertEquals("Respuesta conservada",contents.getJSONObject(1).getJSONArray("parts").getJSONObject(0).getString("text"))
 }
}
