package dev.bautista.spotlight.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ActionHubVisualTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private fun start(theme:String){
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        val info=android.content.pm.ResolveInfo().apply { activityInfo=android.content.pm.ActivityInfo().apply { packageName="com.whatsapp";name="com.whatsapp.Main";applicationInfo=android.content.pm.ApplicationInfo().apply {packageName="com.whatsapp"} } }
        org.robolectric.Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER).setPackage("com.whatsapp"),info)
        ui.setContent {Box(Modifier.width(780.dp).padding(24.dp)){SpotlightPanel(app,AppConfig(theme=theme,files=false,animations=false),{},initialQuery="whatsapp",availableHeightDp=640f)}}
        ui.waitUntil(15000){ui.onAllNodesWithText("Enviar mensaje").fetchSemanticsNodes().isNotEmpty()}
    }
    private fun image(name:String){ui.runOnIdle {
        val view=ui.activity.window.decorView
        val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888);view.draw(Canvas(bitmap))
        val output=File("build/$name.png");output.parentFile?.mkdirs();output.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
    }}
    private fun awaitText(text:String){
        try { ui.waitUntil(15000){ui.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()} }
        catch(e:androidx.compose.ui.test.ComposeTimeoutException){ui.onRoot(useUnmergedTree=true).printToLog("action-hub-navigation");image("091-ui/action-hub-navigation-failure");throw e}
    }
    @Test fun lightHubOpensTypesBeforeListingFiles(){
        start("light");image("spotlight-whatsapp-light")
        // Changing only the cursor must preserve the current results.
        ui.onNode(hasSetTextAction()).performTextInputSelection(androidx.compose.ui.text.TextRange(0))
        ui.onNodeWithText("Enviar mensaje").assertExists()
        ui.onNodeWithText("Buscar archivos").performClick()
        awaitText("Documentos PDF")
        ui.onNodeWithText("Imágenes").assertIsDisplayed()
        ui.onNodeWithText("Documentos PDF").performClick()
        awaitText("Activar búsqueda de archivos")
        ui.onNodeWithText("Volver").performClick()
        awaitText("Documentos PDF")
    }
    @Test fun volumeSliderChangesAndroidMultimediaVolume(){
        val audio=ui.activity.getSystemService(android.media.AudioManager::class.java)
        val desired=(audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)/2).coerceAtLeast(1)
        ui.setContent {SpotlightTheme("dark"){AudioControl{}}}
        ui.onNode(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.ProgressBarRangeInfo)).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.SetProgress){it(desired.toFloat())}
        org.junit.Assert.assertEquals(desired,audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC))
    }
    @Test fun darkHubComposerStaysInsidePanelAndReturns(){
        start("dark");image("spotlight-whatsapp-dark")
        ui.onNodeWithText("Enviar mensaje").performClick()
        ui.onNodeWithText("Contacto o número").assertIsDisplayed()
        ui.onAllNodesWithText("No hay resultados para esta consulta.").assertCountEquals(0)
        ui.onNodeWithText("Mensaje",useUnmergedTree=true).assertExists()
        image("spotlight-message-dark")
        ui.onAllNodesWithText("Volver")[0].performClick()
        ui.onNodeWithText("Enviar mensaje").assertIsDisplayed()
    }
}
