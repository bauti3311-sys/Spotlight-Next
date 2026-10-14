package dev.bautista.spotlight.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NextInteractionTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun legacyRoutineRetainsReviewButHasNoProductEntryPoint(){
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        val routine=dev.bautista.spotlight.automation.Workflow("test","Preparación",steps=listOf(dev.bautista.spotlight.automation.WorkflowStep("text","Hola")))
        val data=dev.bautista.spotlight.automation.Workflows.encode(listOf(routine))
        val row=dev.bautista.spotlight.search.Result("routine:test","Preparación","","Rutinas",1,dev.bautista.spotlight.search.Action.WorkflowRun("test"))
        org.junit.Assert.assertFalse(ProductVisibility.allowed(row))
        org.junit.Assert.assertEquals("Preparación",dev.bautista.spotlight.automation.Workflows.parse(data).single().name)
        // Retain legacy executor safety coverage; it is no longer a product entry point.
        ui.setContent{SpotlightTheme{WorkflowRunner(routine,app.local){}}}
        ui.onNodeWithText("Revisá los pasos antes de ejecutar:").assertExists()
        ui.onNodeWithText("Ejecutar").performClick()
        ui.waitUntil(15000){ui.onAllNodesWithText("1/1 · Completada").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Cerrar").assertExists().performClick()
    }
    private fun capture(name:String){ui.runOnIdle{val view=ui.activity.window.decorView;val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888);view.draw(Canvas(bitmap));File("build/$name.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}}
    @Test fun tabletActionsAreFilterableAndBackKeepsQuery(){
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},"25 * 8",640f)}
        ui.waitUntil(15000){ui.onAllNodesWithText("200",useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()}
        ui.onAllNodesWithContentDescription("Operaciones del resultado").onFirst().performClick()
        ui.onNodeWithText("Buscar acciones").performClick()
        ui.onNodeWithText("Filtrar acciones").performTextInput("Copiar")
        ui.onNodeWithText("Copiar resultado").assertExists()
        capture("spotlight-next-tablet-actions")
    }
    @Test @Config(qualifiers="w360dp-h800dp") fun phoneKeepsSearchFieldAndCategoriesUsable(){
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent{SpotlightPanel(app,AppearancePresets.apply("iOS Light",AppConfig(files=false,animations=false)),{},"25 * 8",640f)}
        ui.waitUntil(15000){ui.onAllNodesWithText("200",useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithContentDescription("Aplicaciones · Ctrl/⌘+1").assertIsDisplayed()
        ui.onNodeWithText("25 * 8").assertIsDisplayed()
        capture("spotlight-next-phone-ios")
    }
    @Test fun unconfiguredAiShowsRealActionableError(){
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false,aiProvider="backend",endpoint=""),{},"ia hola",640f)}
        ui.waitUntil(15000){ui.onAllNodesWithText("Preguntar a la IA",substring=true).fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Preguntar a la IA",substring=true).performClick()
        ui.onNodeWithText("Guardá tu API key",substring=true).assertExists()
    }
}
