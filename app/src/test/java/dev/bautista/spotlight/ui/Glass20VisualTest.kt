package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Glass20VisualTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 @Test fun themeFamiliesRenderRealCalculatorResult(){var preset by mutableStateOf(AppearancePresets.names.first());ui.setContent{SpotlightPanel(app,AppearancePresets.apply(preset,AppConfig(files=false,animations=false)),{},"(45 * 12) / 3",700f)};AppearancePresets.names.forEach{name->ui.runOnIdle{preset=name};ui.waitUntil(15000){ui.onAllNodesWithText("180").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("180").assertExists();ui.runOnIdle{capture091(ui.activity,"glass20-"+name.substringBefore(' ').lowercase())}}}
 @Test @Config(qualifiers="w360dp-h800dp") fun quickMenuStaysInWindowAndEscapeKeepsQuery(){var closed=false;ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{closed=true},"2+2",700f)};ui.waitUntil(15000){ui.onAllNodesWithText("4").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithText("Modo privado").assertExists();ui.runOnIdle{capture091(ui.activity,"glass20-phone-menu")};ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.Escape)};ui.onNode(hasSetTextAction()).assertTextContains("2+2");Assert.assertFalse(closed)}
 @Test fun sportsWithoutFavoritesHasUsefulOnboarding(){ui.setContent{SpotlightTheme{Surface{SportsFavoritesPanel(app,"") {}}}};ui.onNodeWithText("Elegí tu primer equipo",substring=true).assertExists();ui.onNodeWithText("Marcador no informado").assertDoesNotExist();ui.onNodeWithText("Buscar en el proveedor").assertExists();ui.runOnIdle{capture091(ui.activity,"glass20-sports-onboarding")}}
}
