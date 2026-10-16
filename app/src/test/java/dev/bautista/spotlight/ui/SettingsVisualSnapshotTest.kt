package dev.bautista.spotlight.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import dev.bautista.spotlight.MainActivity
import dev.bautista.spotlight.SpotlightApp
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*

/** Real MainActivity composition, native graphics. New references are confined to authorized Settings. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsVisualSnapshotTest {
 @get:Rule val ui=createAndroidComposeRule<MainActivity>()
 private fun appearance(theme:String,preset:String,name:String) {
  ui.waitUntil(15000){ui.onAllNodesWithTag("settings-search").fetchSemanticsNodes().isNotEmpty()}
  val app=ui.activity.application as SpotlightApp
  runBlocking{val c=app.preferences.config.first();app.preferences.updateAppearance(c.copy(theme=theme,preset=preset,animations=false))}
  if(ui.onAllNodesWithText("Cambiar").fetchSemanticsNodes().isNotEmpty())ui.onNodeWithText("Cambiar").performClick()
  ui.onNodeWithText("Apariencia").performClick()
  ui.onAllNodesWithTag("window-width-slider").assertCountEquals(1)
  ui.waitForIdle();ui.onRoot().captureRoboImage("src/test/snapshots/$name.png")
 }
 @Test fun obsidianTablet()=appearance("dark","glass","settings-tablet-obsidian")
 @Test fun graphiteTablet()=appearance("dark","raycast","settings-tablet-graphite")
 @Test fun macosLightTablet()=appearance("light","glass","settings-tablet-light")
 @Test @Config(qualifiers="w360dp-h800dp") fun obsidianPhone()=appearance("dark","glass","settings-phone-obsidian")
}
