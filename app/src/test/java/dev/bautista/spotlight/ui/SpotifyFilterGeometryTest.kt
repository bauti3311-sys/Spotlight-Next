package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.integrations.spotify.SpotifyConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.util.ReflectionHelpers

/** Real production inspector. Controlled public config/state; no account, token or network. */
@OptIn(ExperimentalRoborazziApi::class, ExperimentalTestApi::class, ExperimentalComposeUiApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w360dp-h1000dp-mdpi",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SpotifyFilterGeometryTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 @Test fun allFilterTargetsFitNarrowInspectorAtLargeText() {
  ui.setContent {
   CompositionLocalProvider(LocalDensity provides Density(1f,1.5f)) {
    SpotlightTheme("dark",AppConfig(theme="dark",preset="graphite",animations=false)) {
     Box(Modifier.width(240.dp).background(MaterialTheme.colorScheme.surface).testTag("fixture")) { SpotifySearchPanel(app) }
    }
   }
  }
  ui.waitForIdle()
  runBlocking { app.spotify.configure("a".repeat(32));app.spotify.inspect() }
  ui.runOnIdle {
   ReflectionHelpers.getField<MutableStateFlow<SpotifyConnectionState>>(app.spotify,"mutableState").value=SpotifyConnectionState.AUTHORIZED
  }
  ui.waitUntil(5000) { ui.onAllNodesWithTag("spotify-search-query").fetchSemanticsNodes().isNotEmpty() }
  ui.onNodeWithTag("fixture").captureRoboImage("build/visual-native/spotify-filters.png",
   roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  val bounds=ui.onNodeWithTag("fixture").getUnclippedBoundsInRoot()
  for(label in listOf("Tema","Artista","Álbum","Lista")) {
   val node=ui.onNodeWithText(label)
   val chip=node.getUnclippedBoundsInRoot()
   assertTrue("$label extends past inspector right edge: $chip / $bounds",chip.right<=bounds.right)
   assertTrue("$label extends past inspector left edge",chip.left>=bounds.left)
   node.assertIsDisplayed().performClick().assertIsSelected()
  }
  ui.onNodeWithText("Confirmar reproducción").assertDoesNotExist()
 }

 private fun authorizeUiFixture() {
  ui.waitForIdle()
  runBlocking { app.spotify.configure("a".repeat(32));app.spotify.inspect() }
  ui.runOnIdle {
   ReflectionHelpers.getField<MutableStateFlow<SpotifyConnectionState>>(app.spotify,"mutableState").value=SpotifyConnectionState.AUTHORIZED
  }
  ui.waitUntil(5000) { ui.onAllNodesWithTag("spotify-search-query").fetchSemanticsNodes().isNotEmpty() }
 }

 @Test @Config(qualifiers="w800dp-h1280dp-mdpi")
 fun tabletFiltersRemainVisibleWithoutAnArtificialHeight() {
  ui.setContent {
   CompositionLocalProvider(LocalDensity provides Density(1f,1.5f)) {
    SpotlightTheme("dark",AppConfig(theme="dark",preset="graphite",animations=false)) {
     Box(Modifier.width(500.dp).background(MaterialTheme.colorScheme.surface).testTag("tablet")) { SpotifySearchPanel(app) }
    }
   }
  }
  authorizeUiFixture()
  val bounds=ui.onNodeWithTag("tablet").getUnclippedBoundsInRoot()
  for(label in listOf("Tema","Artista","Álbum","Lista")) {
   val node=ui.onNodeWithText(label)
   val chip=node.getUnclippedBoundsInRoot()
   assertTrue(label,chip.left>=bounds.left && chip.right<=bounds.right)
   node.assertIsDisplayed().performClick().assertIsSelected()
  }
  ui.onNodeWithText("Confirmar reproducción").assertDoesNotExist()
 }

 @Test fun keyboardTraversalAndSelectionNeverStartPlayback() {
  lateinit var inputMode:InputModeManager
  ui.setContent {
   val manager=LocalInputModeManager.current
   SideEffect { inputMode=manager }
   SpotlightTheme("dark",AppConfig(theme="dark",preset="graphite",animations=false)) {
    Box(Modifier.width(240.dp)) { SpotifySearchPanel(app) }
   }
  }
  authorizeUiFixture()
  ui.onNodeWithTag("spotify-search-query").performClick().assertIsFocused()
  // Android's official focus codelab requires Keyboard mode without a hardware keyboard.
  // https://developer.android.com/codelabs/large-screens/keyboard-focus-management-in-compose
  ui.runOnIdle { assertTrue(inputMode.requestInputMode(InputMode.Keyboard)) }
  ui.waitForIdle()
  ui.runOnIdle { assertEquals(InputMode.Keyboard,inputMode.inputMode) }
  ui.onNodeWithTag("spotify-search-query").assertIsFocused().performKeyInput { pressKey(Key.Tab) }
  ui.onNodeWithText("Tema").assertIsFocused()
  ui.onNodeWithText("Tema").performKeyInput { pressKey(Key.Tab) }
  ui.onNodeWithText("Artista").assertIsFocused().performKeyInput { pressKey(Key.Enter) }
  ui.onNodeWithText("Artista").assertIsSelected()
  ui.onNodeWithText("Confirmar reproducción").assertDoesNotExist()
 }
}
