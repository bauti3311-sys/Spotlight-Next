package dev.bautista.spotlight.ui

import android.app.*
import android.content.*
import android.content.pm.*
import android.graphics.*
import android.service.notification.StatusBarNotification
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.Replies
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

fun capture091(activity:android.app.Activity,name:String){val view=activity.window.decorView;val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888);view.draw(Canvas(bitmap));val target=File("build/091-ui/$name.png");target.parentFile?.mkdirs();target.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Next091VisualTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun capture(name:String){ui.waitForIdle();ui.runOnIdle{capture091(ui.activity,name)}}
    @Test fun allFourThemesHaveFreshHomeCaptures(){
        var preset by mutableStateOf("macOS Glass")
        ui.setContent{SpotlightPanel(app,AppearancePresets.apply(preset,AppConfig(files=false,animations=false)),{},availableHeightDp=700f)}
        AppearancePresets.names.forEach{name->ui.runOnIdle{preset=name};ui.waitUntil(15000){ui.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()};capture("home-"+name.substringBefore(' ').lowercase())}
    }
    @Test fun filesCommandsAndAiModesKeepCleanQueriesAndHaveFreshCaptures(){
        ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},availableHeightDp=700f)}
        ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithContentDescription("Archivos · Ctrl/⌘+2").performClick();try { ui.waitUntil(15000){ui.onAllNodesWithText("Documentos PDF").fetchSemanticsNodes().isNotEmpty()} } catch (failure: androidx.compose.ui.test.ComposeTimeoutException) { println(ui.onRoot().printToString()); throw failure };capture("files-tablet")
        ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithContentDescription("Comandos · Ctrl/⌘+3").performClick();ui.waitUntil(15000){ui.onAllNodesWithText("Crear nota").fetchSemanticsNodes().isNotEmpty()};capture("commands-tablet")
        ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithContentDescription("IA · Ctrl/⌘+4").performClick();ui.waitUntil(15000){ui.onAllNodesWithText("Preguntá o pegá un texto en la barra").fetchSemanticsNodes().isNotEmpty()};capture("ai-setup-tablet")
    }
    @Test fun applicationsGridContainsInstalledFixtureApps(){
        val launcher=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        repeat(9){i->val info=ActivityInfo().apply{packageName="fixture.next$i";name="fixture.next$i.Main";nonLocalizedLabel="Aplicación ${'A'+i}";applicationInfo=ApplicationInfo().apply{packageName="fixture.next$i"}};Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(launcher,ResolveInfo().apply{activityInfo=info})}
        app.search.invalidateApps();ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},availableHeightDp=700f)}
        ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithContentDescription("Aplicaciones · Ctrl/⌘+1").performClick();ui.waitUntil(15000){ui.onAllNodesWithText("Aplicación A").fetchSemanticsNodes().isNotEmpty()};capture("apps-tablet")
    }
    @OptIn(ExperimentalTestApi::class)
    @Test fun contextualActionsAndWebQuickLookHaveFreshCaptures(){
        ui.setContent{SpotlightPanel(app,AppearancePresets.apply("Raycast Dark",AppConfig(files=false,animations=false)),{},"25 * 8",700f)}
        ui.waitUntil(15000){ui.onAllNodesWithText("200",useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()};ui.onAllNodesWithContentDescription("Operaciones del resultado").onFirst().performClick();ui.onNodeWithText("Filtrar acciones").performTextInput("Copiar");ui.onNodeWithText("Copiar resultado").assertExists();ui.onNodeWithText("Copiar texto").assertDoesNotExist();capture("actions-tablet")
        ui.onNodeWithContentDescription("Cerrar vista rápida").performClick();ui.onNode(hasSetTextAction()).performTextReplacement("rehabilitación vestibular");ui.waitUntil(15000){ui.onAllNodesWithText("Buscar «rehabilitación vestibular»").fetchSemanticsNodes().isNotEmpty()};ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.DirectionDown)};ui.waitForIdle();ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.Spacebar)};ui.waitForIdle();capture("quicklook-web-tablet")
    }
    @Test fun whatsappMessageRemainsVisibleWhileReplying(){
        Replies.clear();Replies.connected.value=true
        val style=Notification.MessagingStyle(android.app.Person.Builder().setName("Yo").build()).addMessage("¿A qué hora nos encontramos?",1700000000000,android.app.Person.Builder().setName("Juan").build())
        val remote=RemoteInput.Builder("reply_text").build();val pending=PendingIntent.getBroadcast(app,91,Intent("fixture.reply"),PendingIntent.FLAG_MUTABLE)
        val notification=Notification.Builder(app,"fixture").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Juan").setStyle(style).addAction(Notification.Action.Builder(null,"Responder",pending).addRemoteInput(remote).build()).build()
        Replies.put(StatusBarNotification("com.whatsapp","com.whatsapp",91,null,1000,0,0,notification,android.os.Process.myUserHandle(),1700000000000))
        try{
            ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},"mensajes",700f)};ui.waitUntil(15000){ui.onAllNodesWithText("Juan").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("Juan").performClick();ui.onNodeWithText("¿A qué hora nos encontramos?").assertExists();capture("whatsapp-preview-tablet")
            ui.onNodeWithText("Responder").performClick();ui.onNodeWithText("¿A qué hora nos encontramos?").assertExists();ui.onNodeWithText("Mensaje").performTextInput("A las 18.");capture("whatsapp-reply-tablet")
        }finally{Replies.clear()}
    }
    @Test @Config(qualifiers="w360dp-h800dp") fun phoneHomeHasTouchSizedModes(){ui.setContent{SpotlightPanel(app,AppearancePresets.apply("iOS Light",AppConfig(files=false,animations=false)),{},availableHeightDp=700f)};ui.waitUntil(15000){ui.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithContentDescription("IA · Ctrl/⌘+4").assertIsDisplayed();capture("home-phone")}
    @Test @Config(qualifiers="w915dp-h412dp") fun landscapeFitsAvailableHeight(){ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},"25 * 8",360f)};ui.waitUntil(15000){ui.onAllNodesWithText("200",useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()};assertTrue(ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot.height<=360*app.resources.displayMetrics.density);capture("landscape-phone")}
    @OptIn(ExperimentalTestApi::class)
    @Test fun actionFilterSupportsArrowSelectionAndEnter(){var result="";val focus=FocusRequester();ui.setContent{SpotlightTheme{Column(Modifier.width(420.dp)){ActionPanelView(listOf(ContextAction("Abrir","Principal",CommandIcons.Apps,{result="open"}),ContextAction("Copiar","Texto",CommandIcons.ContentCopy,{result="copy"})),focus)}}};ui.onNode(hasSetTextAction()).performClick();ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.DirectionDown)};ui.waitForIdle();ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.Enter)};ui.runOnIdle{assertEquals("copy",result)}}
}
