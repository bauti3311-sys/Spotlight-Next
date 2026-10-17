package dev.bautista.spotlight.ui

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config as AppConfig
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

/** Exercises production SearchEngine -> SpotlightPanel -> DirectAi; HTTP and keys are test fixtures only. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
class UniversalProductionFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private val server=MockWebServer()
    @Before fun start(){server.start();server.enqueue(MockResponse().setHeader("Content-Type","text/event-stream").setBody("data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"**Respuesta de prueba**\"}]}}]}\n\n"))}
    @After fun stop(){server.shutdown()}
    private fun client()=DirectAi(object:AiCredentials {
        override fun read(provider:String)="fixture-not-a-real-key"
        override fun contains(provider:String)=true
    },OkHttpClient.Builder().addInterceptor{chain->chain.proceed(chain.request().newBuilder().url(server.url("/gemini-fixture")).build())}.build())
    private fun show(query:String,files:Boolean=false){ui.setContent {SpotlightPanel(app,AppConfig(files=files,animations=false,aiProvider="gemini",aiModel="fixture-model"),{},initialQuery=query,aiClient=client())}}
    @Test fun typedQuestionDoesNotSendUntilEnterThenStreamsMarkdownInline(){
        val query="cuántas veces ganó Boca la Libertadores"
        show(query)
        ui.waitUntil(15000){ui.onAllNodesWithText("Responder con Gemini").fetchSemanticsNodes().isNotEmpty()}
        assertEquals(0,server.requestCount)
        ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.Enter)}
        ui.waitUntil(15000){ui.onAllNodesWithText("Respuesta de prueba").fetchSemanticsNodes().isNotEmpty()}
        val req=server.takeRequest(2,TimeUnit.SECONDS)!!
        val contents=JSONObject(req.body.readUtf8()).getJSONArray("contents")
        assertEquals(1,contents.length())
        assertEquals(query,contents.getJSONObject(0).getJSONArray("parts").getJSONObject(0).getString("text"))
        assertEquals(1,server.requestCount)
        ui.onNodeWithText("Copiar").assertExists()
    }
    @Test fun naturalCalculatorEnterCopiesLocallyWithoutGeminiRequest(){
        show("¿Cuánto es 15% de 48.000?")
        ui.waitUntil(15000){ui.onAllNodesWithText("7200").fetchSemanticsNodes().isNotEmpty()}
        ui.onAllNodesWithText("Responder con Gemini").assertCountEquals(0)
        assertEquals(0,server.requestCount)
        ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.Enter)}
        val clipboard=app.getSystemService(android.content.ClipboardManager::class.java)
        ui.waitUntil(15000){clipboard.primaryClip?.getItemAt(0)?.text?.toString()=="7200"}
        assertEquals(0,server.requestCount)
    }
    private fun openSavedThread():List<Pair<String,String>> {
        val history=listOf("user" to "Mi glosario vestibular","assistant" to "Definición anterior autorizada","user" to "Detallá la compensación","assistant" to "Respuesta previa guardada")
        runBlocking{app.contexts.saveConversation(history)}
        show("conversaciones")
        ui.waitUntil(15000){ui.onAllNodesWithText("Mi glosario vestibular").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Mi glosario vestibular").performClick()
        ui.onNode(hasSetTextAction() and hasText("Nueva pregunta")).performScrollTo().performTextInput("Dame otro ejemplo concreto")
        assertEquals(0,server.requestCount)
        ui.onNodeWithText("Revisar continuación").performScrollTo().performClick()
        ui.waitUntil(15000){ui.onAllNodesWithTag("ai-review-prompt",useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()}
        val displayed=ui.onNodeWithTag("ai-review-prompt",useUnmergedTree=true).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString(""){it.text}
        history.forEach{assertTrue(displayed.contains(it.second))};assertTrue(displayed.contains("Dame otro ejemplo concreto"))
        assertEquals(0,server.requestCount)
        return history
    }
    @Test fun savedThreadSearchReviewSendsExactlyAuthorizedHistory(){
        val history=openSavedThread()
        ui.onNodeWithText("Enviar",useUnmergedTree=true).performClick()
        ui.waitUntil(15000){server.requestCount==1}
        val messages=JSONObject(server.takeRequest(2,TimeUnit.SECONDS)!!.body.readUtf8()).getJSONArray("contents")
        val expected=history+listOf("user" to "Dame otro ejemplo concreto")
        assertEquals(expected.size,messages.length())
        expected.forEachIndexed{i,(role,text)->
            assertEquals(if(role=="assistant")"model" else "user",messages.getJSONObject(i).getString("role"))
            assertEquals(text,messages.getJSONObject(i).getJSONArray("parts").getJSONObject(0).getString("text"))
        }
    }
    @Test fun cancellingSavedThreadReviewDoesNotSendAnything(){
        openSavedThread();ui.onNodeWithText("Cancelar").performClick();ui.waitForIdle()
        assertEquals(0,server.requestCount);assertEquals(1,app.contexts.conversations.value.size)
    }
    @Test fun savingInlineConversationRetainsAllCompletedExchanges(){
        server.enqueue(MockResponse().setHeader("Content-Type","text/event-stream").setBody("data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Segunda respuesta de prueba\"}]}}]}\n\n"))
        show("Explicame la compensación vestibular")
        ui.waitUntil(15000){ui.onAllNodesWithText("Responder con Gemini").fetchSemanticsNodes().isNotEmpty()}
        ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.Enter)}
        ui.waitUntil(15000){ui.onAllNodes(hasText("Guardar conversación") and isEnabled()).fetchSemanticsNodes().isNotEmpty()}
        ui.onNode(hasSetTextAction() and hasText("Continuar conversación")).performTextInput("Dame un ejemplo")
        ui.onNodeWithText("Enviar").performClick()
        ui.waitUntil(15000){ui.onAllNodesWithText("Segunda respuesta de prueba").fetchSemanticsNodes().isNotEmpty() && ui.onAllNodes(hasText("Guardar conversación") and isEnabled()).fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Guardar conversación").performScrollTo().performClick()
        ui.waitUntil(15000){app.contexts.conversations.value.isNotEmpty()}
        val saved=app.contexts.conversations.value.single()
        assertEquals(listOf("user" to "Explicame la compensación vestibular","assistant" to "**Respuesta de prueba**","user" to "Dame un ejemplo","assistant" to "Segunda respuesta de prueba"),saved.turns)
        assertEquals(2,server.requestCount)
    }
    @Test fun existingExecutionRegistrySharesNaturalLocalTools()=runBlocking {
        val executor=ToolExecutor(app)
        val config=AppConfig(aiProvider="gemini")
        val calculation=executor.execute(ToolCall("calculate",mapOf("expression" to "¿Cuánto es 15% de 48.000?")),config)
        assertEquals("7200",calculation.text)
        assertNull(calculation.action)
        val conversion=executor.execute(ToolCall("convert_units",mapOf("query" to "¿Cuántos metros son 2 km?")),config)
        assertTrue(conversion.text.startsWith("2000 metros ·"))
        assertNull(conversion.action)
        assertEquals(0,server.requestCount)
    }
    @Test fun directPercentageActionUsesObservedRegistryExecution(){
        val audio=app.getSystemService(android.media.AudioManager::class.java)
        audio.setStreamVolume(android.media.AudioManager.STREAM_MUSIC,audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC),0)
        show("volumen al 40%")
        ui.waitUntil(15000){ui.onAllNodesWithText("Volumen al 40%").fetchSemanticsNodes().isNotEmpty()}
        ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.Enter)}
        ui.waitUntil(15000){ui.onAllNodesWithText("Volumen multimedia observado:",substring=true).fetchSemanticsNodes().isNotEmpty()}
        assertEquals(audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)*40/100,audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC))
        assertEquals(0,server.requestCount)
    }
    @Test fun disablingMediaAfterReviewBlocksDirectExecution(){
        val audio=app.getSystemService(android.media.AudioManager::class.java)
        val original=audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        audio.setStreamVolume(android.media.AudioManager.STREAM_MUSIC,original,0)
        show("volumen al 40%")
        ui.waitUntil(15000){ui.onAllNodesWithText("Volumen al 40%").fetchSemanticsNodes().isNotEmpty()}
        dev.bautista.spotlight.extensions.ExtensionStore.setEnabled(app,"media",false)
        try{
            ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.Enter)}
            ui.waitUntil(15000){ui.onAllNodesWithText("La herramienta no está disponible en este dispositivo.").fetchSemanticsNodes().isNotEmpty()}
            assertEquals(original,audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC))
            assertEquals(0,server.requestCount)
        }finally{dev.bautista.spotlight.extensions.ExtensionStore.setEnabled(app,"media",true)}
    }
    @Test fun spotifyAndVolumeUseReviewedNativeToolsNotRemoteAi(){
        val pkg="com.spotify.music"
        val application=android.content.pm.ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel="Spotify"}
        val activity=android.content.pm.ActivityInfo().apply{packageName=pkg;name="$pkg.Main";applicationInfo=application;nonLocalizedLabel="Spotify";exported=true}
        org.robolectric.Shadows.shadowOf(app.packageManager).installPackage(android.content.pm.PackageInfo().apply{packageName=pkg;applicationInfo=application;activities=arrayOf(activity)})
        val launcher=android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        val info=android.content.pm.ResolveInfo().apply{activityInfo=activity}
        org.robolectric.Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(launcher,info)
        org.robolectric.Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(android.content.Intent(launcher).setPackage(pkg),info)
        org.robolectric.Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_INFO).setPackage(pkg),info)
        app.search.invalidateApps()
        val audio=app.getSystemService(android.media.AudioManager::class.java)
        audio.setStreamVolume(android.media.AudioManager.STREAM_MUSIC,audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC),0)
        show("Abrí Spotify y bajá el volumen al 40%")
        ui.waitUntil(15000){ui.onAllNodesWithText("Revisar interpretación").fetchSemanticsNodes().isNotEmpty()}
        assertEquals(0,server.requestCount)
        ui.onNodeWithText("Revisar interpretación").performClick()
        ui.waitUntil(15000){ui.onAllNodes(hasText("Confirmar pasos seleccionados") and isEnabled()).fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Confirmar pasos seleccionados").assertIsEnabled().performClick()
        ui.waitUntil(15000){audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)==audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)*40/100}
        val launch=org.robolectric.Shadows.shadowOf(app).nextStartedActivity
        assertEquals(pkg,launch.component!!.packageName)
        ui.onNodeWithText("Solicitud enviada · Android recibió la apertura",substring=true).assertExists()
        ui.onNodeWithText("Verificado · Volumen multimedia observado:",substring=true).assertExists()
        assertEquals(0,server.requestCount)
    }

    @Test fun documentCompoundSendsExactlyFrozenReviewedPromptAfterSeparateConsent()=runBlocking {
        val file=File(app.cacheDir,"rehabilitacion vestibular.pdf")
        requireNotNull(javaClass.getResourceAsStream("/visual/raycast-fixture.pdf")).use{input->file.outputStream().use{input.copyTo(it)}}
        app.index.storeTranscript(Uri.fromFile(file).toString(),file.name,"application/pdf","rehabilitacion vestibular")
        show("Buscá el PDF de rehabilitación vestibular y explicame el capítulo 3",files=true)
        ui.waitUntil(15000){ui.onAllNodesWithText("Preparar consulta sobre PDF con Gemini").fetchSemanticsNodes().isNotEmpty()}
        assertEquals(0,server.requestCount)
        ui.onNodeWithText("Preparar consulta sobre PDF con Gemini").performClick()
        ui.waitUntil(15000){ui.onAllNodes(hasText("Revisar y autorizar envío a Gemini") and isEnabled()).fetchSemanticsNodes().isNotEmpty()}
        assertEquals(0,server.requestCount)
        ui.onNodeWithText("Revisar y autorizar envío a Gemini").performScrollTo().assertIsDisplayed().performClick()
        ui.waitUntil(15000){ui.onAllNodesWithTag("ai-review-prompt",useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()}
        val displayed=ui.onNodeWithTag("ai-review-prompt",useUnmergedTree=true).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString(""){it.text}
        assertTrue(displayed.contains("rehabilitacion vestibular.pdf"))
        assertTrue("Coverage must survive section selection into the exact reviewed payload",displayed.contains("Extracción local"))
        assertEquals(0,server.requestCount)
        ui.onNodeWithText("Enviar",useUnmergedTree=true).performClick()
        ui.waitUntil(15000){server.requestCount==1}
        val body=JSONObject(server.takeRequest(2,TimeUnit.SECONDS)!!.body.readUtf8())
        val messages=body.getJSONArray("contents")
        assertEquals(1,messages.length())
        assertEquals(displayed,messages.getJSONObject(0).getJSONArray("parts").getJSONObject(0).getString("text"))
    }
}
