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
 private fun awaitText(text:String) {
  try {
   ui.waitUntil(15000) {
    Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(16,java.util.concurrent.TimeUnit.MILLISECONDS)
    ui.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
   }
  } catch(e:androidx.compose.ui.test.ComposeTimeoutException) {
   println("Productivity timeout waiting for $text\n"+ui.onRoot(useUnmergedTree=true).printToString())
   throw e
  }
 }
 @Test fun focusPanelReturnsToQuery(){val app=RuntimeEnvironment.getApplication() as SpotlightApp;ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},"pomodoro",700f)};awaitText("Concentración");ui.onNodeWithText("Concentración").performClick();ui.onNodeWithText("Minutos · 1 a 180").assertExists();ui.onAllNodesWithText("Volver").onFirst().performClick();ui.onNode(hasSetTextAction()).assertTextContains("pomodoro")}
 @Test fun preferencesOpenInlineWithoutLosingSearch(){val app=RuntimeEnvironment.getApplication() as SpotlightApp;ui.setContent{SpotlightPanel(app,AppConfig(files=false,animations=false),{},"administrar sugerencias",700f)};try{awaitText("Administrar sugerencias")}catch(e:Exception){println(ui.onRoot(useUnmergedTree=true).printToString());ui.runOnIdle{capture091(ui.activity,"glass20-preferences-failure")};throw e};ui.onNodeWithText("Administrar sugerencias").performClick();ui.onNodeWithText("Restablecer sugerencias").assertExists();ui.onAllNodesWithText("Volver").onFirst().performClick();ui.onNode(hasSetTextAction()).assertTextContains("administrar sugerencias")}
}
