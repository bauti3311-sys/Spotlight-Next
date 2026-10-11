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
    @Test fun filesCommandsAppsAndBackNeverInjectQuery(){start();ui.onNodeWithContentDescription("Archivos · Ctrl/⌘+2").performClick();ui.waitUntil(15000){ui.onAllNodesWithText("Documentos PDF").fetchSemanticsNodes().isNotEmpty()};query();ui.onAllNodesWithContentDescription("Operaciones del resultado").onFirst().performClick();ui.onNodeWithContentDescription("Cerrar vista rápida").performClick();query();ui.onNodeWithContentDescription("Comandos · Ctrl/⌘+3").performClick();query();ui.onNodeWithContentDescription("Aplicaciones · Ctrl/⌘+1").performClick();query();ui.onNodeWithContentDescription("IA · Ctrl/⌘+4").performClick();ui.waitUntil(15000){ui.onAllNodesWithText("Preguntá o pegá un texto en la barra").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("Todo").performClick();query()}
    @Test fun explicitQuerySurvivesModeSwitch(){start("anatomía");ui.onNodeWithContentDescription("Archivos · Ctrl/⌘+2").performClick();ui.onNode(hasSetTextAction()).assertTextEquals("anatomía");ui.onNodeWithContentDescription("Comandos · Ctrl/⌘+3").performClick();ui.onNode(hasSetTextAction()).assertTextEquals("anatomía");ui.onNodeWithText("Volver").performClick();ui.onNode(hasSetTextAction()).assertTextEquals("anatomía")}
    @Test fun emptyHomeIsSuggestionsNeverHistory(){start();ui.waitUntil(15000){ui.onAllNodesWithText("Crear una nota").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("SUGERENCIAS").assertExists();ui.onNodeWithText("Privado").assertDoesNotExist();ui.onNodeWithText("Recientes").assertDoesNotExist()}
}
