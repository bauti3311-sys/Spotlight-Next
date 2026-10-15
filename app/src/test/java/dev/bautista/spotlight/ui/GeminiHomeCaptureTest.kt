package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.ContentValues
import android.content.pm.*
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

/** Real production search + Compose rendering. PackageManager/files are explicit test fixtures. */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GeminiHomeCaptureTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun fixtures()=runBlocking {
        val launcher=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        listOf("Archivos de prueba","Calculadora de prueba","Música de prueba","Navegador de prueba","Notas de prueba").forEachIndexed { i,label ->
            val info=ActivityInfo().apply {packageName="fixture.gemini$i";name="$packageName.Main";nonLocalizedLabel=label;applicationInfo=ApplicationInfo().apply {packageName="fixture.gemini$i";icon=android.R.drawable.sym_def_app_icon}}
            Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(launcher,ResolveInfo().apply{activityInfo=info})
        }
        for((i,name) in listOf("Informe autorizado de prueba.pdf","Atlas de estudio de prueba.pdf").withIndex()) {
            val file=File(app.cacheDir,name)
            requireNotNull(javaClass.getResourceAsStream("/visual/raycast-fixture.pdf")).use {source->file.outputStream().use{source.copyTo(it)}}
            val uri=Uri.fromFile(file).toString()
            app.index.storeTranscript(uri,name,"application/pdf","Documento de prueba local")
            app.openOrCreateDatabase("search-index.db",0,null).use {db->db.update("files",ContentValues().apply{put("modified",System.currentTimeMillis()-i*1000);put("size",file.length())},"uri=?",arrayOf(uri))}
        }
        app.search.invalidateApps()
    }
    private fun render(name:String,theme:String="dark",preset:String="glass",width:Int=692) {
        fixtures()
        lateinit var focus:androidx.compose.ui.focus.FocusManager
        ui.setContent {focus=LocalFocusManager.current;Box(Modifier.width(width.dp)) {
            SpotlightPanel(app,AppConfig(files=true,online=false,animations=false,learning=false,suggestions=true,theme=theme,preset=preset),{},"",700f)
        }}
        ui.waitUntil(15000){ui.onAllNodesWithText("Atlas de estudio de prueba.pdf").fetchSemanticsNodes().isNotEmpty()}
        for(heading in listOf("Sugerencias frecuentes","Archivos recientes","Aplicaciones"))ui.onNodeWithText(heading).assertIsDisplayed()
        for(label in listOf("Archivos de prueba","Calculadora de prueba","Música de prueba","Navegador de prueba","Notas de prueba","Informe autorizado de prueba.pdf","Atlas de estudio de prueba.pdf"))ui.onNodeWithText(label).assertIsDisplayed()
        val last=ui.onNodeWithText("Notas de prueba").fetchSemanticsNode().boundsInRoot
        val footer=ui.onNodeWithText("↵ Abrir aplicación").fetchSemanticsNode().boundsInRoot
        Assert.assertTrue("The final row must be fully above the footer",last.bottom+8f<footer.top)
        ui.onAllNodesWithText("En reproducción").assertCountEquals(0)
        ui.onNodeWithText("↵ Abrir aplicación").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        ui.onNodeWithContentDescription("Divisor ajustable del panel contextual").assertDoesNotExist()
        ui.onNode(hasSetTextAction()).assertIsFocused()
        ui.runOnIdle{focus.clearFocus(force=true)}
        ui.onRoot().captureRoboImage("build/gemini-home/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    @Test fun obsidianThreeSectionsFromActualSearch(){render("obsidian-home-landscape")}
    @Test fun graphiteThreeSectionsFromActualSearch(){render("graphite-home-landscape",preset="raycast")}
    @Test @Config(qualifiers="w800dp-h1280dp") fun lightThreeSectionsPortrait(){render("light-home-portrait",theme="light")}
}
