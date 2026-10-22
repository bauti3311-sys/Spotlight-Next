package dev.bautista.spotlight.ui
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class LegacyPermissionRouteTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 @Test fun legacyEntryRoutesToTheSinglePermissionCenterWithoutGrantToggles() {
  ui.setContent{SpotlightTheme{PreferencePanel(AppConfig(),"permissions",{})}}
  ui.onAllNodes(isToggleable()).assertCountEquals(0)
  ui.onNodeWithText("Abrir centro de permisos").performClick()
  val intent=Shadows.shadowOf(ui.activity).nextStartedActivity
  assertEquals("dev.bautista.spotlight.MainActivity",intent.component?.className)
  assertEquals("Permisos y accesos",intent.getStringExtra("group"))
 }
}
