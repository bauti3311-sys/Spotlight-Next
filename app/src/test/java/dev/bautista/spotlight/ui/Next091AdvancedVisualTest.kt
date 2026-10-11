package dev.bautista.spotlight.ui

import android.content.ClipboardManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Next091AdvancedVisualTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun capture(name:String){ui.waitForIdle();ui.runOnIdle{capture091(ui.activity,name)}}
    @Test fun localTextCardWorksWithoutInternet(){ui.setContent{SpotlightPanel(app,AppConfig(files=false,online=false,animations=false),{},"mayúsculas: hola",700f)};ui.waitUntil(15000){ui.onAllNodesWithText("Procesado localmente").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("HOLA").assertExists();capture("text-tools-tablet");ui.onNodeWithText("Copiar").performClick();assertEquals("HOLA",app.getSystemService(ClipboardManager::class.java).primaryClip!!.getItemAt(0).text.toString())}
    @Test fun snippetHasRequiredFieldsAndCopiesCompletedText(){ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false,snippets=setOf("saludo|Hola {nombre}")),{},"snippets",700f)};ui.waitUntil(15000){ui.onAllNodesWithText("saludo").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("saludo").performClick();ui.onNodeWithText("Copiar fragmento").assertIsNotEnabled();ui.onNodeWithText("Nombre").performTextInput("Juan");ui.onNodeWithText("Hola Juan").assertExists();capture("snippet-form-tablet");ui.onNodeWithText("Copiar fragmento").performClick();assertEquals("Hola Juan",app.getSystemService(ClipboardManager::class.java).primaryClip!!.getItemAt(0).text.toString())}
    @Test fun timerShowsUsefulErrorWhenClockIsMissing(){ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},"temporizador 20 minutos",700f)};ui.waitUntil(15000){ui.onAllNodesWithText("Preparar temporizador").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("Preparar temporizador").performClick();ui.onNodeWithText("Iniciar con Reloj").assertExists();capture("timer-form-tablet");ui.onNodeWithText("Iniciar con Reloj").performClick();ui.onNodeWithText("No hay una app Reloj compatible instalada.").assertExists()}
    @Test fun clipboardEditingPreservesTemporaryFavorite(){app.local.clearClips();app.local.capture("Original");app.local.toggleClipFavorite("Original");ui.setContent{SpotlightTheme{ClipboardEditor("Original",{app.local.editClip("Original",it)},{})}};ui.onNodeWithText("Original").performTextReplacement("Editado");ui.onNodeWithText("Guardar cambio").performClick();assertEquals(listOf("Editado"),app.local.clips.value);assertTrue("Editado" in app.local.clipFavorites.value);capture("clipboard-editor-tablet");app.local.clearClips()}
    @Test fun longTemplateScrollsToConfirmation(){var copied="";ui.setContent{SpotlightTheme{SnippetForm((1..8).joinToString(" "){"{campo$it}"},{copied=it},{})}};ui.onNodeWithText("Copiar fragmento").performScrollTo().assertIsNotEnabled();capture("snippet-long-fields-tablet");assertEquals("",copied)}

    @Test fun savedNoteLeavesCommandFilterAndAppearsImmediately(){ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},availableHeightDp=700f)};ui.onNodeWithContentDescription("Comandos · Ctrl/⌘+3").performClick();ui.waitUntil(15000){ui.onAllNodesWithText("Crear nota").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("Crear nota").performClick();ui.onNodeWithText("Texto de la nota").performTextInput("Nota nueva desde comandos");ui.onNodeWithText("Guardar").performClick();ui.waitUntil(15000){ui.onAllNodesWithText("Texto de la nota").fetchSemanticsNodes().isEmpty() && ui.onAllNodesWithText("notas").fetchSemanticsNodes().isNotEmpty() && ui.onAllNodesWithText("Nota nueva desde comandos").fetchSemanticsNodes().isNotEmpty()};capture("saved-note-tablet")}

}
