package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], qualifiers="w1280dp-h800dp", application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UnifiedInspectorFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private var width by mutableIntStateOf(800)
    private var closed=0
    private fun search()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
    private fun show(query:String,client:dev.bautista.spotlight.ai.DirectAi?=null) {
        ui.setContent { Box(Modifier.width(width.dp)) {
            SpotlightPanel(app,AppConfig(theme="dark",preset="glass",aiProvider="gemini",aiModel="fixture-model",files=false,online=false,animations=false,learning=false,suggestions=false),{closed++},query,availableHeightDp=700f,aiClient=client)
        } }
    }
    private fun awaitTag(tag:String) { ui.waitUntil(15000){ui.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()} }
    private fun capture(name:String) { captureScreenRoboImage("build/unified-inspector/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record)) }

    @Test fun pinnedLocalPreviewSurvivesQueryAndActionsKeepTheCurrentSelection() {
        val text="Fijable prueba local\n"+"Contenido privado local. ".repeat(14)
        runBlocking { app.local.saveNote(text) }
        ui.waitUntil(15000){app.local.notes.value.any{it.text==text}}
        val id=app.local.notes.value.first{it.text==text}.id
        show("Fijable")
        awaitTag("result:note:$id")
        search().performKeyInput{pressKey(Key.DirectionDown);pressKey(Key.Spacebar)}
        ui.onNodeWithTag("context-inspector").assertIsDisplayed()
        ui.onNodeWithContentDescription("Fijar inspector").assertHeightIsAtLeast(48.dp).performClick()
        search().performClick().performTextReplacement("2+2")
        awaitTag("result:calc")
        ui.onNodeWithContentDescription("Desfijar inspector").assertIsDisplayed()
        ui.onNode(hasText(text) and hasAnyAncestor(hasTestTag("inspector-content"))).assertExists()
        capture("tablet-pinned-local")
        search().performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.K);keyUp(Key.CtrlLeft)}
        ui.onNodeWithTag("action-surface").assertIsDisplayed()
        ui.onNodeWithText("Copiar resultado").performClick()
        ui.runOnIdle {
            val clipboard=app.getSystemService(android.content.ClipboardManager::class.java)
            assertEquals("4",clipboard.primaryClip?.getItemAt(0)?.text?.toString())
            assertEquals(0,closed)
        }
        search().assertTextEquals("2+2").assertIsFocused()
        ui.onNodeWithContentDescription("Desfijar inspector").assertIsDisplayed()
        search().performKeyInput{pressKey(Key.Escape)}
        ui.onNodeWithTag("inspector-content").assertDoesNotExist()
        search().assertTextEquals("2+2").assertIsFocused()
        assertEquals(0,closed)
        search().performKeyInput{pressKey(Key.Enter)}
        ui.runOnIdle{assertEquals(1,closed)}
    }

    @Test fun pinnedSearchFormPreservesDraftAndResultsAcrossResponsiveLayout() {
        show("youtube")
        awaitTag("result:catalog:youtube:search")
        ui.onNodeWithTag("result:catalog:youtube:search").performClick()
        search().performKeyInput{pressKey(Key.Enter)}
        ui.onNodeWithTag("context-inspector").assertIsDisplayed()
        ui.onNodeWithTag("result:catalog:youtube:search").assertIsDisplayed()
        ui.onNodeWithText("Qué querés buscar").performTextReplacement("Cerati en vivo")
        ui.onNodeWithContentDescription("Fijar inspector").performClick()
        search().performClick().performTextReplacement("2+2")
        awaitTag("result:calc")
        ui.onNode(hasSetTextAction() and hasText("Cerati en vivo")).assertExists()
        capture("tablet-search-form")
        ui.runOnIdle{width=360}
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        ui.onNode(hasSetTextAction() and hasText("Cerati en vivo")).assertIsDisplayed()
        capture("phone-search-form")
        ui.runOnIdle{width=800}
        ui.onNodeWithTag("context-inspector").assertIsDisplayed()
        ui.onNode(hasSetTextAction() and hasText("Cerati en vivo")).assertExists()
        assertEquals(0,closed)
    }
    @Test fun pinnedAiKeepsAuthorizedConversationAcrossLocalSearchAndFollowUp() {
        val server=okhttp3.mockwebserver.MockWebServer()
        server.start()
        fun response(text:String)=okhttp3.mockwebserver.MockResponse().setHeader("Content-Type","text/event-stream")
            .setBody("data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"$text\"}]}}]}\n\n")
        server.enqueue(response("Primera respuesta fixture"))
        server.enqueue(response("Continuación fixture"))
        try {
            val client=dev.bautista.spotlight.ai.DirectAi(object:dev.bautista.spotlight.ai.AiCredentials {
                override fun read(provider:String)="fixture-key"
                override fun contains(provider:String)=true
            },okhttp3.OkHttpClient.Builder().addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().url(server.url("/fixture")).build())
            }.build())
            val question="Explicame el reflejo vestibuloocular"
            show(question,client)
            ui.waitUntil(15000){ui.onAllNodesWithText("Responder con Gemini").fetchSemanticsNodes().isNotEmpty()}
            assertEquals(0,server.requestCount)
            search().performKeyInput{pressKey(Key.Enter)}
            ui.waitUntil(15000){ui.onAllNodesWithText("Primera respuesta fixture").fetchSemanticsNodes().isNotEmpty()}
            ui.onNodeWithContentDescription("Fijar inspector").performClick()
            search().performClick().performTextReplacement("2+2")
            awaitTag("result:calc")
            ui.onNodeWithText("Primera respuesta fixture").assertExists()
            assertEquals(1,server.requestCount)
            ui.onNode(hasSetTextAction() and hasText("Continuar conversación")).performScrollTo().performTextInput("Dame un ejemplo")
            ui.onNodeWithText("Enviar").performScrollTo().performClick()
            ui.waitUntil(15000){ui.onAllNodesWithText("Continuación fixture").fetchSemanticsNodes().isNotEmpty()}
            assertEquals(2,server.requestCount)
            server.takeRequest(2,java.util.concurrent.TimeUnit.SECONDS)
            val request=server.takeRequest(2,java.util.concurrent.TimeUnit.SECONDS)!!
            val contents=org.json.JSONObject(request.body.readUtf8()).getJSONArray("contents")
            assertEquals(3,contents.length())
            assertEquals(question,contents.getJSONObject(0).getJSONArray("parts").getJSONObject(0).getString("text"))
            assertEquals("Primera respuesta fixture",contents.getJSONObject(1).getJSONArray("parts").getJSONObject(0).getString("text"))
            assertEquals("Dame un ejemplo",contents.getJSONObject(2).getJSONArray("parts").getJSONObject(0).getString("text"))
            search().assertTextEquals("2+2")
            capture("tablet-pinned-ai-fixture")
            val shareText=ui.onNode(hasText("Compartir") and hasAnyAncestor(hasTestTag("inspector-content")),useUnmergedTree=true).fetchSemanticsNode()
            assertTrue("Share label must fit one line in the tablet inspector",shareText.boundsInRoot.height<=24*app.resources.displayMetrics.density)
            ui.onNode(hasText("Copiar") and hasClickAction() and hasAnyAncestor(hasTestTag("inspector-content"))).performScrollTo().assertIsDisplayed().assertHeightIsAtLeast(48.dp)
            assertEquals(0,closed)
        } finally { server.shutdown() }
    }

    @Test @Config(qualifiers="w390dp-h844dp")
    fun phoneFormFitsItsViewportAndDoesNotCreateAnEmptySideColumn() {
        width=360
        show("youtube")
        awaitTag("result:catalog:youtube:search")
        ui.onNodeWithTag("result:catalog:youtube:search").performClick()
        search().performKeyInput{pressKey(Key.Enter)}
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        ui.onNodeWithContentDescription("Fijar inspector").assertIsDisplayed().performClick()
        ui.onNodeWithText("Qué querés buscar").performTextReplacement("Cerati acústico")
        ui.onNode(hasSetTextAction() and hasText("Cerati acústico")).assertIsDisplayed()
        ui.onNodeWithText("Buscar",useUnmergedTree=true).assertIsDisplayed()
        capture("phone-390-search-form-dark")
        ui.onNodeWithContentDescription("Cerrar inspector").performClick()
        search().assertTextEquals("youtube").assertIsFocused()
        assertEquals(0,closed)
    }

    @Test fun switchingAiTasksRestoresOnlyTheirOwnCompletedConversationWithoutNewRequests() {
        val server=okhttp3.mockwebserver.MockWebServer();server.start()
        fun response(text:String)=okhttp3.mockwebserver.MockResponse().setHeader("Content-Type","text/event-stream")
            .setBody("data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"$text\"}]}}]}\n\n")
        server.enqueue(response("Respuesta tarea uno"));server.enqueue(response("Respuesta tarea dos"))
        try {
            val client=dev.bautista.spotlight.ai.DirectAi(object:dev.bautista.spotlight.ai.AiCredentials {
                override fun read(provider:String)="fixture-key"
                override fun contains(provider:String)=true
            },okhttp3.OkHttpClient.Builder().addInterceptor{chain->chain.proceed(chain.request().newBuilder().url(server.url("/fixture")).build())}.build())
            val first="Explicame el reflejo vestibuloocular"
            val second="Explicame el reflejo fotomotor"
            show(first,client)
            ui.waitUntil(15000){ui.onAllNodesWithText("Responder con Gemini").fetchSemanticsNodes().isNotEmpty()}
            search().performKeyInput{pressKey(Key.Enter)}
            ui.waitUntil(15000){ui.onAllNodesWithText("Respuesta tarea uno").fetchSemanticsNodes().isNotEmpty()}
            search().performClick().performTextReplacement(second)
            ui.waitUntil(15000){ui.onAllNodesWithText("Responder con Gemini").fetchSemanticsNodes().isNotEmpty()}
            search().performKeyInput{pressKey(Key.Enter)}
            ui.waitUntil(15000){ui.onAllNodesWithText("Respuesta tarea dos").fetchSemanticsNodes().isNotEmpty()}
            search().performClick().performTextReplacement(first)
            ui.waitUntil(15000){ui.onAllNodesWithText("Responder con Gemini").fetchSemanticsNodes().isNotEmpty()}
            search().performKeyInput{pressKey(Key.Enter)}
            ui.waitUntil(15000){ui.onAllNodesWithText("Respuesta tarea uno").fetchSemanticsNodes().isNotEmpty()}
            assertEquals(2,server.requestCount)
            server.takeRequest(2,java.util.concurrent.TimeUnit.SECONDS)
            val contents=org.json.JSONObject(server.takeRequest(2,java.util.concurrent.TimeUnit.SECONDS)!!.body.readUtf8()).getJSONArray("contents")
            assertEquals(1,contents.length())
            assertEquals(second,contents.getJSONObject(0).getJSONArray("parts").getJSONObject(0).getString("text"))
            ui.onNodeWithText("Conversación de esta tarea recuperada localmente.").assertExists()
        } finally {server.shutdown()}
    }
}
