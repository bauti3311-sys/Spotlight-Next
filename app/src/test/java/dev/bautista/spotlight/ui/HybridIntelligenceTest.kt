package dev.bautista.spotlight.ui

import android.media.AudioManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
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
        ui.waitUntil(15000){ui.onAllNodes(hasText("Confirmar pasos seleccionados") and isEnabled()).fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Conexión pendiente · Shizuku",substring=true).assertExists()
        assertEquals(initial,audio.getStreamVolume(AudioManager.STREAM_MUSIC))
        ui.runOnIdle{capture091(ui.activity,"hybrid-plan-review")}
        ui.onNodeWithText("Confirmar pasos seleccionados").performClick()
        ui.waitUntil(15000){ui.onAllNodesWithText("Verificado · Volumen multimedia observado:",substring=true).fetchSemanticsNodes().isNotEmpty()}
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
        ui.waitUntil(15000){ui.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithContentDescription("Fuentes de búsqueda").assertIsDisplayed()
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
        ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false,actionRecent=listOf("volumen al 60%")),{},availableHeightDp=700f)}
        ui.waitUntil(15000){ui.onAllNodesWithText("volumen al 60%").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("volumen al 60%").performTouchInput{longClick()}
        ui.onNodeWithText("Ocultar sugerencia").performScrollTo().assertIsDisplayed()
        ui.runOnIdle{capture091(ui.activity,"hybrid-phone-actions")}
        val hidden=java.util.concurrent.atomic.AtomicBoolean(false)
        val observation=app.scope.launch {
            app.preferences.config.first{it.hiddenSuggestions.contains("suggest:action:volumen al 60%")}
            hidden.set(true)
        }
        try {
            ui.onNodeWithText("Ocultar sugerencia").performClick()
            // Pump Compose/main-looper work while the UI coroutine persists the change.
            ui.waitUntil(5000){hidden.get()}
        } finally {observation.cancel()}
    }

    @Test fun correctionAndPdfSummaryRequireExplicitReviewWithoutAutomaticRead() {
        var action:Action?=null
        var step by androidx.compose.runtime.mutableStateOf("resumí este PDF")
        ui.setContent{SpotlightTheme{InterpretationPanel(app,AppConfig(files=false),step){action=it}}}
        ui.onNodeWithText("Elegí primero el PDF").assertExists()
        ui.runOnIdle{step="corregí este texto"}
        ui.onNodeWithText("Texto para corregir").assertExists()
        ui.onNodeWithText("Revisar corrección con IA").assertIsNotEnabled()
        assertNull(action)
        ui.onNodeWithText("Texto").performTextInput("borrador inicial")
        ui.onNodeWithText("Revisar corrección con IA").performClick()
        assertEquals(
            Action.Ask("Corregí y mejorá este texto:\nborrador inicial",true,origin=AiInputOrigin.PRIVATE_CONTEXT),
            action,
        )
    }

    @Test fun messageEditorSwitchesToCompatibleRemoteInputAndRequiresExplicitConfirmation() {
        dev.bautista.spotlight.control.Replies.clear()
        dev.bautista.spotlight.control.Replies.connected.value=true
        try {
            val pi=android.app.PendingIntent.getBroadcast(app,41,android.content.Intent("reply.hybrid.41"),android.app.PendingIntent.FLAG_MUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT)
            val action=android.app.Notification.Action.Builder(null,"Responder",pi).addRemoteInput(android.app.RemoteInput.Builder("reply_text").build()).build()
            val n=android.app.Notification.Builder(app,"test").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Juan").setContentText("¿Llegás a las 19?").setShortcutId("juan-chat").addAction(action).build()
            dev.bautista.spotlight.control.Replies.put(android.service.notification.StatusBarNotification("com.whatsapp","com.whatsapp",41,null,1000,0,0,n,android.os.Process.myUserHandle(),1700000000041))
            var launched=0
            ui.setContent{SpotlightTheme{MessageEditor(Action.ComposeMessage("whatsapp","Juan","Sí, llego en 10"),{launched++},{},{})}}
            ui.onNodeWithText("Responder notificación activa · RemoteInput (Juan)").assertExists().performClick()
            ui.onNodeWithText("Mecanismo Android: RemoteInput de notificación activa (com.whatsapp)").assertExists()
            ui.onNodeWithText("Revisar respuesta").performClick()
            assertEquals(1,dev.bautista.spotlight.control.Replies.targets.value.size)
            ui.onNodeWithText("Enviar respuesta").performClick()
            ui.waitUntil(5000){dev.bautista.spotlight.control.Replies.targets.value.isEmpty()}
            assertEquals(0,launched)
            val sent=Shadows.shadowOf(app).broadcastIntents.last{it.action=="reply.hybrid.41"}
            assertEquals("Sí, llego en 10",android.app.RemoteInput.getResultsFromIntent(sent)!!.getCharSequence("reply_text"))
        } finally {
            dev.bautista.spotlight.control.Replies.clear()
        }
    }

}
