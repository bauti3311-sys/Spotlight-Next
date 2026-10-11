package dev.bautista.spotlight.ui

import android.media.AudioManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HybridIntelligenceTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    @Test fun planReviewHasNoSideEffectsAndUnavailableBrightnessDoesNotBlockVolume() {
        val audio=app.getSystemService(AudioManager::class.java)
        val initial=audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},"bajar el brillo al 35% y volumen al 60%",700f)}
        ui.waitUntil(15000){ui.onAllNodesWithText("Revisar interpretación").fetchSemanticsNodes().isNotEmpty()}
        assertEquals(initial,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
        ui.onNodeWithText("Revisar interpretación").performClick()
        ui.waitUntil(15000){ui.onAllNodesWithText("Confirmar pasos seleccionados").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Requiere Shizuku",substring=true).assertExists()
        assertEquals(initial,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
        ui.runOnIdle{capture091(ui.activity,"hybrid-plan-review")}
        ui.onNodeWithText("Confirmar pasos seleccionados").performClick()
        ui.waitUntil(15000){ui.onAllNodesWithText("Volumen multimedia solicitado: 60 %.").fetchSemanticsNodes().isNotEmpty()}
        assertEquals(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*60/100,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
        ui.onNodeWithText("Plan revisado").assertIsNotEnabled()
    }
    @Test fun missingPdfAndTextAskForContextWithoutReadingOrSending() {
        var action:Action?=null
        ui.setContent{SpotlightTheme{InterpretationPanel(app,AppConfig(files=false),"explicame este PDF"){action=it}}}
        ui.onNodeWithText("Elegí primero el PDF").assertExists()
        ui.onNodeWithText("Autorizar búsqueda de archivos").performClick()
        assertEquals(Action.Access("files","kind:pdf "),action)
    }
    @Test fun translationRequiresUserTextAndReturnsSensitiveReview() {
        var action:Action?=null
        ui.setContent{SpotlightTheme{InterpretationPanel(app,AppConfig(),"traducí este texto"){action=it}}}
        ui.onNodeWithText("Revisar traducción con IA").assertIsNotEnabled()
        assertNull(action)
        ui.onNodeWithText("Texto").performTextInput("hello")
        ui.onNodeWithText("Revisar traducción con IA").performClick()
        assertEquals(Action.Ask("Traducí al español este texto:\nhello",true),action)
    }
    @Test fun compoundResolutionExplainsMissingAppAndMinimumBrightness()=runBlocking {
        val rows=resolvePlan(app,AppConfig(),LocalPlan(listOf(PlanStep.Open("AppInexistenteUnica"),PlanStep.Brightness(1))))
        assertTrue(rows.all{it.call==null})
        assertTrue(rows[0].requirement.contains("única"))
        assertTrue(rows[1].requirement.contains("20/255"))
    }
    @Test fun unconfiguredAiReviewStaysLocalAndOffersSettings() {
        ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false,aiProvider="backend",endpoint=""),{},"traducí este texto",700f)}
        ui.waitUntil(15000){ui.onAllNodesWithText("Revisar interpretación").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Revisar interpretación").performClick()
        ui.onNodeWithText("Texto").performTextInput("hello")
        ui.onNodeWithText("Revisar traducción con IA").performClick()
        ui.onNodeWithText("hello",substring=true).assertExists()
        ui.onNodeWithText("Enviar").assertIsNotEnabled()
    }
    @Test fun lightHybridHasUnifiedSuggestionsWithoutCategoryCircles() {
        ui.setContent{SpotlightPanel(app,AppConfig(theme="light",files=false,animations=false),{},availableHeightDp=700f)}
        ui.waitUntil(15000){ui.onAllNodesWithText("Crear una nota").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithContentDescription("Opciones de búsqueda").assertIsDisplayed()
        ui.onNodeWithContentDescription("Aplicaciones · Ctrl/⌘+1").assertDoesNotExist()
        ui.onNodeWithText("SUGERENCIAS").assertDoesNotExist()
        ui.runOnIdle{capture091(ui.activity,"hybrid-light-home")}
    }
    @Test fun darkHybridPreservesTouchActionsAndUsefulStudyChoices() {
        ui.setContent{SpotlightPanel(app,AppConfig(theme="dark",files=false,animations=false),{},"quiero estudiar sin distracciones",700f)}
        ui.waitUntil(15000){ui.onAllNodesWithText("Revisar interpretación").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Revisar interpretación").performClick()
        ui.onNodeWithText("Preparar temporizador de 25 minutos").assertIsDisplayed()
        ui.runOnIdle{capture091(ui.activity,"hybrid-dark-study")}
    }
    @Test fun configuredBackendReceivesNoRequestWhileTypingOrReviewingContext() {
        val requests=java.util.concurrent.atomic.AtomicInteger()
        val server=java.net.ServerSocket(0,1,java.net.InetAddress.getByName("127.0.0.1"))
        val listener=kotlin.concurrent.thread(isDaemon=true) {
            while(!server.isClosed)try {
                server.accept().use { socket ->
                    requests.incrementAndGet()
                    socket.getOutputStream().write("HTTP/1.1 503 Service Unavailable\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".toByteArray())
                }
            }catch(e:java.net.SocketException){if(!server.isClosed)throw e}
        }
        try {
            ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false,online=false,aiProvider="backend",endpoint="http://127.0.0.1:${server.localPort}"),{},"traducí este texto",700f)}
            ui.waitUntil(15000){ui.onAllNodesWithText("Revisar interpretación").fetchSemanticsNodes().isNotEmpty()}
            assertEquals(0,requests.get())
            ui.onNodeWithText("Revisar interpretación").performClick()
            ui.onNodeWithText("Texto").performTextInput("private test text")
            ui.onNodeWithText("Revisar traducción con IA").performClick()
            ui.onNodeWithText("Enviar").assertIsEnabled()
            ui.onNodeWithText("private test text",substring=true).assertExists()
            assertEquals(0,requests.get())
        } finally {server.close();listener.join(1000)}
    }
    @Test @Config(qualifiers="w360dp-h800dp") fun phoneHybridSupportsLongPressSecondaryActions() {
        ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},availableHeightDp=700f)}
        ui.waitUntil(15000){ui.onAllNodesWithText("Crear una nota").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Buscar una aplicación").performTouchInput{longClick()}
        ui.onNodeWithText("Ocultar sugerencia").performScrollTo().assertIsDisplayed()
        ui.runOnIdle{capture091(ui.activity,"hybrid-phone-actions")}
        ui.onNodeWithText("Ocultar sugerencia").performClick()
        runBlocking {kotlinx.coroutines.withTimeout(5000){app.preferences.config.first{it.hiddenSuggestions.contains("suggest:apps")}}}
    }

}
