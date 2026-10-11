package dev.bautista.spotlight.ui

import android.graphics.*
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Next100AdvancedVisualTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun capture(name:String){ui.waitForIdle();ui.runOnIdle{val view=ui.activity.window.decorView;val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888);view.draw(Canvas(bitmap));File("build/100-ui/$name.png").also{it.parentFile?.mkdirs()}.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}}
    @Test fun devicePanelReturnsToOriginalQuery(){
        ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},"información del dispositivo",700f)}
        ui.waitUntil(15000){ui.onAllNodesWithText("Información del dispositivo").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("Información del dispositivo").performClick();ui.onNodeWithText("Dispositivo").assertExists();capture("device-tablet");ui.onAllNodesWithText("Volver").onFirst().performClick();ui.onNode(hasSetTextAction()).assertTextContains("información del dispositivo")
    }
    @Test fun absentShizukuShowsHonestState(){ui.setContent{SpotlightTheme{Surface{AdvancedPanel(app,"shizuku")}}};ui.onNodeWithText("No instalado").assertExists();ui.onNodeWithText("Instalar").assertExists();capture("shizuku-absent-tablet")}
    @Test @Config(qualifiers="w360dp-h800dp") fun advancedPanelFitsPhone(){ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},"shizuku",700f)};ui.waitUntil(15000){ui.onAllNodesWithText("Diagnóstico de Shizuku").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("Diagnóstico de Shizuku").performClick();ui.onNodeWithText("Autorizar Spotlight").assertIsDisplayed();capture("shizuku-absent-phone")}
    @Test fun extensionRegistryFiltersAndKeepsSearchContext(){
        ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},"extensiones",700f)}
        ui.waitUntil(15000){ui.onAllNodesWithText("Extensiones e integraciones").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("Extensiones e integraciones").performClick()
        ui.onNodeWithText("Filtrar extensiones").performTextInput("Notas");ui.waitUntil(15000){ui.onAllNodesWithText("Notas").fetchSemanticsNodes().isNotEmpty()};ui.onNode(hasText("Notas") and !hasSetTextAction()).performClick();ui.onNodeWithText("Crear nota").assertExists();capture("extensions-notes-tablet")
        ui.onAllNodesWithText("Volver").onFirst().performClick();ui.onNode(hasSetTextAction()).assertTextContains("extensiones")
    }
}
