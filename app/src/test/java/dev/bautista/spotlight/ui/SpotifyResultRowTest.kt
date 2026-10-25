package dev.bautista.spotlight.ui
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import dev.bautista.spotlight.data.Config as AppConfig
import com.github.takahirom.roborazzi.*
import org.robolectric.annotation.GraphicsMode
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.integrations.spotify.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalRoborazziApi::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class SpotifyResultRowTest {
 @get:Rule val ui=createComposeRule()
 private val item=SpotifySearchItem(SpotifyItemType.TRACK,"a".repeat(22),"Tema fixture","Artista fixture")
 @Test fun selectionIsAccessibleAndDoesNotImplyPlayback() {
  var selected by mutableStateOf(false)
  var selections=0
  ui.setContent { SpotlightTheme("dark",AppConfig(theme="dark",preset="graphite",animations=false)) { SpotifyResultRow(item,selected,true,{selected=true;selections++},Modifier.testTag("row").background(MaterialTheme.colorScheme.surface)) } }
  ui.onNodeWithTag("row").assertIsNotSelected().assertHeightIsAtLeast(48.dp)
  ui.onNodeWithTag("row").captureRoboImage("build/spotify-row/unselected.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  ui.onNodeWithTag("row").performClick().assertIsSelected()
  ui.onNodeWithTag("row").captureRoboImage("build/spotify-row/selected.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  ui.runOnIdle { assertEquals(1,selections) }
 }
 @Test fun busyRowCannotChangeTheApprovedSelection() {
  var selections=0
  ui.setContent { SpotlightTheme("dark",AppConfig(theme="dark",preset="graphite",animations=false)) { SpotifyResultRow(item,true,false,{selections++},Modifier.testTag("row").background(MaterialTheme.colorScheme.surface)) } }
  ui.onNodeWithTag("row").assertIsSelected().assertIsNotEnabled().performClick()
  ui.runOnIdle { assertEquals(0,selections) }
 }
}
