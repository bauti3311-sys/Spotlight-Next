package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Color
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RaycastCompositionCaptureTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private lateinit var focus:androidx.compose.ui.focus.FocusManager
    private fun show(query:String,theme:String="dark",files:Boolean=false,width:Int=720) {
        ui.setContent {focus=LocalFocusManager.current;Box(Modifier.width(width.dp)) {
            SpotlightPanel(app,AppConfig(files=files,online=false,animations=false,learning=false,suggestions=false,theme=theme),{},query,700f)
        }}
        ui.onNode(hasSetTextAction()).assertIsFocused().assertTextEquals(query)
    }
    private fun await(text:String)=ui.waitUntil(15000){ui.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()}
    private fun capture(name:String) {
        ui.runOnIdle{focus.clearFocus(force=true)};ui.waitForIdle()
        ui.onRoot().captureRoboImage("build/raycast-captures/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    @Test fun installedApplicationResultsAndSelection() {
        val launcher=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        listOf("Galería","Galería de fotos","Galería privada","Galería compartida").forEachIndexed { i,label ->
            val info=ActivityInfo().apply {packageName="fixture.gallery$i";name="$packageName.Main";nonLocalizedLabel=label;applicationInfo=ApplicationInfo().apply{packageName=infoPackage(i)}}
            Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(launcher,ResolveInfo().apply{activityInfo=info})
        }
        app.search.invalidateApps();show("Galería");await("Galería de fotos")
        ui.onNodeWithContentDescription("Divisor ajustable del panel contextual").assertDoesNotExist()
        capture("tablet-applications-dark")
        ui.onNode(hasSetTextAction()).performClick().performKeyInput {pressKey(Key.DirectionDown)}
        ui.onNode(hasSetTextAction()).assertTextEquals("Galería")
        capture("tablet-multiple-selection-dark")
    }
    private fun infoPackage(i:Int)="fixture.gallery$i"
    @Test fun calculatorHasOneValueAndActionsUnderDemand() {
        show("2+2");await("4");ui.onAllNodesWithText("4").assertCountEquals(1)
        ui.onNodeWithContentDescription("Divisor ajustable del panel contextual").assertDoesNotExist()
        ui.onNodeWithText("Tab acciones").performClick();await("Copiar resultado")
        ui.onNodeWithText("Copiar resultado").assertIsFocused()
        ui.onAllNodesWithText("4").assertCountEquals(1)
        ui.onNodeWithContentDescription("Cerrar acciones").assertWidthIsAtLeast(48.dp)
        capture("tablet-actions-dark")
    }
    @Test fun realPdfQuickLookComposition()=runBlocking {
        val file=File(app.cacheDir,"raycast-fixture.pdf")
        val document=PdfDocument()
        try {val page=document.startPage(PdfDocument.PageInfo.Builder(300,400,1).create())
            page.canvas.drawColor(Color.WHITE);page.canvas.drawText("Spotlight Next",24f,50f,Paint().apply{color=Color.BLACK;textSize=20f})
            document.finishPage(page);file.outputStream().use{document.writeTo(it)}
        } finally{document.close()}
        app.index.storeTranscript(Uri.fromFile(file).toString(),"raycast-fixture.pdf","application/pdf","Documento local de prueba")
        show("raycast-fixture",files=true);await("raycast-fixture.pdf")
        ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.DirectionDown)}
        ui.waitUntil(15000){ui.onAllNodesWithContentDescription("Cerrar vista rápida").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithContentDescription("Divisor ajustable del panel contextual").assertExists()
        // This is real production Quick Look. A renderer error is captured honestly,
        // never replaced by an illustrative bitmap or accepted as a rendered page.
        ui.waitUntil(15000){ui.onAllNodesWithContentDescription("Página 1 del PDF").fetchSemanticsNodes().isNotEmpty() || ui.onAllNodesWithText("No se pudo abrir el PDF. Revisá el permiso o probá abrirlo en otra app.").fetchSemanticsNodes().isNotEmpty() || ui.onAllNodesWithText("No se pudo renderizar esta página.").fetchSemanticsNodes().isNotEmpty()}
        capture("tablet-pdf-native-composition")
        ui.onNode(hasSetTextAction()).performClick().performKeyInput{pressKey(Key.Escape)}
        ui.onNodeWithContentDescription("Cerrar vista rápida").assertDoesNotExist()
        ui.onNode(hasSetTextAction()).assertTextEquals("raycast-fixture")
        file.delete()
        Unit
    }
}
