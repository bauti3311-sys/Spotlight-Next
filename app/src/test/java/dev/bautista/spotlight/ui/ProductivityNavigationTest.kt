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
@RunWith(RobolectricTestRunner::class) @Config(sdk=[33],qualifiers="w360dp-h800dp",application=SpotlightApp::class)
class ProductivityNavigationTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 @Test fun focusPanelReturnsToQuery(){val app=RuntimeEnvironment.getApplication() as SpotlightApp;ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},"pomodoro",700f)};ui.waitUntil(15000){ui.onAllNodesWithText("Concentración").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("Concentración").performClick();ui.onNodeWithText("Minutos · 1 a 180").assertExists();ui.onAllNodesWithText("Volver").onFirst().performClick();ui.onNode(hasSetTextAction()).assertTextContains("pomodoro")}
 @Test fun preferencesOpenInlineWithoutLosingSearch(){val app=RuntimeEnvironment.getApplication() as SpotlightApp;ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},"administrar sugerencias",700f)};ui.waitUntil(15000){ui.onAllNodesWithText("Administrar sugerencias").fetchSemanticsNodes().isNotEmpty()};ui.onNodeWithText("Administrar sugerencias").performClick();ui.onNodeWithText("Restablecer sugerencias").assertExists();ui.onAllNodesWithText("Volver").onFirst().performClick();ui.onNode(hasSetTextAction()).assertTextContains("administrar sugerencias")}
}
