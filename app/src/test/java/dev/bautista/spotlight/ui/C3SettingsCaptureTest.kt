package dev.bautista.spotlight.ui
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.MainActivity
import dev.bautista.spotlight.SpotlightApp
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class,qualifiers="w1280dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class C3SettingsCaptureTest {
 @get:Rule val ui=createAndroidComposeRule<MainActivity>()
 @Test fun backupAndPermissionPagesUseTheExistingGlassWorkspaceWithoutRequestingAccess() {
  ui.waitUntil(15000){ui.onAllNodesWithTag("settings-search").fetchSemanticsNodes().isNotEmpty()}
  val app=ui.activity.application as SpotlightApp
  runBlocking{val c=app.preferences.config.first();app.preferences.updateAppearance(c.copy(theme="dark",preset="glass",animations=false))}
  for((area,name) in listOf("Avanzado" to "backup","Permisos y accesos" to "permissions")) {
   ui.onNodeWithTag("settings-area:$area").performScrollTo().performClick()
   ui.onNodeWithTag("settings-details").assertExists()
   captureScreenRoboImage("build/task-recovery/settings-$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
   assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
  }
 }
}
