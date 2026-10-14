package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Next091NavigationTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private fun start(query:String=""){val app=RuntimeEnvironment.getApplication() as SpotlightApp;ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false,actionRecent=listOf("Privado"),recent=listOf("old/app")),{},query,700f)}}
    private fun query()=ui.onNode(hasSetTextAction()).assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText,androidx.compose.ui.text.AnnotatedString("")))
    private fun awaitText(text:String) {
        ui.waitUntil(15000) {
            // Advance paused Android time so delayed debounce/provider messages can run; keep the real 15 s deadline.
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(16,java.util.concurrent.TimeUnit.MILLISECONDS)
            ui.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
    @Test fun filesCommandsAppsAndBackNeverInjectQuery(){start();ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithContentDescription("Archivos · Ctrl/⌘+2").performClick();awaitText("Documentos PDF");query();ui.onAllNodesWithContentDescription("Operaciones del resultado").onFirst().performClick();ui.onNodeWithContentDescription("Cerrar vista rápida").performClick();query();ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithContentDescription("Comandos · Ctrl/⌘+3").performClick();query();ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithContentDescription("Aplicaciones · Ctrl/⌘+1").performClick();query();ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithContentDescription("IA · Ctrl/⌘+4").performClick();awaitText("Preguntá o pegá un texto en la barra");ui.onNodeWithText("Todo").performClick();query()}
    @Test fun explicitQuerySurvivesModeSwitch(){start("anatomía");ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithContentDescription("Archivos · Ctrl/⌘+2").performClick();ui.onNode(hasSetTextAction()).assertTextEquals("anatomía");ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithContentDescription("Comandos · Ctrl/⌘+3").performClick();ui.onNode(hasSetTextAction()).assertTextEquals("anatomía");ui.onNodeWithText("Volver").performClick();ui.onNode(hasSetTextAction()).assertTextEquals("anatomía")}
    @Test fun emptyHomeIsSuggestionsNeverHistory(){start();ui.waitUntil(15000){ui.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("Crear una nota").assertDoesNotExist();ui.onNodeWithText("SUGERENCIAS").assertDoesNotExist();ui.onNodeWithText("Privado").assertDoesNotExist();ui.onNodeWithText("Recientes").assertDoesNotExist()}
}
