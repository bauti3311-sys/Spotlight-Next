package dev.bautista.spotlight.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.FileBuffer
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.index.ExtractedDocument
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.TimeUnit

/** Real selection -> local extraction -> overlay review -> exact authorized DirectAi request. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
class DocumentSelectionFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private val server=MockWebServer()
    @Before fun start(){server.start();server.enqueue(MockResponse().setHeader("Content-Type","text/event-stream").setBody("data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"**Comparación de prueba**\"}]}}]}\n\n"))}
    @After fun stop(){server.shutdown()}
    private fun row(n:Int,uri:Uri=Uri.parse("content://selection/$n"))=Result("selected:$n","Documento $n","","Archivos",1,Action.Launch(Intent(Intent.ACTION_VIEW,uri)),mime="text/plain")
    private fun show(buffer:FileBuffer) {
        val client=DirectAi(object:AiCredentials {
            override fun read(provider:String)="fixture-not-a-real-key"
            override fun contains(provider:String)=true
        },OkHttpClient.Builder().addInterceptor{chain->chain.proceed(chain.request().newBuilder().url(server.url("/gemini-fixture")).build())}.build())
        ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false,aiProvider="gemini",aiModel="fixture-model"),{},aiClient=client,fileSelection=buffer)}
    }
    @Test fun comparingTwoActualFilesShowsAndSendsExactlyTheReviewedExcerpts() {
        val files=(1..2).map{File.createTempFile("selection-flow-",".txt",app.cacheDir).apply{writeText("Contenido autorizado de comparación $it")}}
        try {
            val buffer=FileBuffer();files.forEachIndexed{i,file->buffer.add(row(i+1,Uri.fromFile(file)))}
            show(buffer);assertEquals(0,server.requestCount)
            ui.onNodeWithText("Comparar con IA").performClick()
            ui.waitUntil(15000){ui.onAllNodesWithTag("ai-review-prompt",useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()}
            val reviewed=ui.onNodeWithTag("ai-review-prompt",useUnmergedTree=true).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString(""){it.text}
            assertTrue(reviewed.contains("[D1]"));assertTrue(reviewed.contains("[Fin D2 ·"));assertTrue(reviewed.contains("SHA-256:"))
            files.forEach{assertTrue(reviewed.contains(it.readText()))}
            assertEquals(0,server.requestCount)
            ui.onNodeWithText("Enviar",useUnmergedTree=true).performClick()
            ui.waitUntil(15000){server.requestCount==1}
            val request=server.takeRequest(2,TimeUnit.SECONDS)!!
            val sent=JSONObject(request.body.readUtf8()).getJSONArray("contents").getJSONObject(0).getJSONArray("parts").getJSONObject(0).getString("text")
            assertEquals(reviewed,sent)
            ui.waitUntil(15000){ui.onAllNodesWithText("Comparación de prueba").fetchSemanticsNodes().isNotEmpty()}
        } finally{files.forEach{it.delete()}}
    }
    private fun latePreparation(cancel:Boolean) {
        val started=CompletableDeferred<Unit>();val release=CompletableDeferred<Unit>();val finished=CompletableDeferred<Unit>()
        val buffer=FileBuffer{_,_,_,_->
            withContext(NonCancellable){started.complete(Unit);release.await();finished.complete(Unit)}
            ExtractedDocument("Contenido tardío privado",emptyList(),emptyList(),0,0,0,"a".repeat(64))
        }
        buffer.add(row(1));buffer.add(row(2));show(buffer)
        ui.onNodeWithText("Comparar con IA").performClick()
        ui.waitUntil(15000){started.isCompleted}
        if(cancel)ui.onNodeWithText("Cancelar preparación").performClick()
        else ui.onNodeWithText("Documento 2").performClick()
        ui.waitUntil(15000){ui.onAllNodesWithText("Preparar resumen").fetchSemanticsNodes().isNotEmpty()}
        release.complete(Unit);runBlocking{withTimeout(5000){finished.await()}};ui.waitForIdle()
        ui.onAllNodesWithTag("ai-review-prompt",useUnmergedTree=true).assertCountEquals(0)
        assertEquals(0,server.requestCount)
    }
    @Test fun cancelledSelectionPreparationDiscardsLateText(){latePreparation(true)}
    @Test fun removingDocumentDiscardsLateSelectionPreparation(){latePreparation(false)}
}
