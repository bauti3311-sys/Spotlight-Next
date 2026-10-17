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

/** Real MainActivity/Compose on Robolectric native graphics; not One UI screenshots. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class SettingsReviewCaptureTest {
 @get:Rule val ui=createAndroidComposeRule<MainActivity>()
 private fun ready(){ui.waitUntil(15000){ui.onAllNodesWithTag("settings-home").fetchSemanticsNodes().isNotEmpty()}}
 private fun capture(name:String){ui.waitForIdle();ui.onRoot().captureRoboImage("build/review-settings/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))}
 @Test fun homeIsCompactAndDoesNotRequestPermissions(){
  ready();ui.onNodeWithText("Tu espacio, a tu manera.").assertIsDisplayed()
  ui.onNodeWithText("Diagnóstico de Spotlight").assertDoesNotExist()
  ui.onNodeWithText("Avanzado").assertDoesNotExist()
  assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
  val app=ui.activity.application as SpotlightApp
  runBlocking{val c=app.preferences.config.first();app.preferences.updateAppearance(c.copy(theme="dark",preset="glass",animations=false))}
  ui.waitForIdle();capture("tablet-home-obsidian")
 }
 @Test fun guideRequiresSelectionAndExplicitActionForEachPermission(){
  ready();ui.onNodeWithTag("open-function-center").performClick()
  ui.onNodeWithTag("guided-setup-start").performScrollTo().assertIsNotEnabled()
  ui.onNodeWithTag("feature-select-contacts").performScrollTo().performClick()
  ui.onNodeWithTag("guided-setup-start").performScrollTo().performClick()
  ui.onNodeWithText("1 de 1 · Contactos").assertExists()
  assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
  ui.onNodeWithText("Configurar",useUnmergedTree=true).performClick()
  val intent=Shadows.shadowOf(ui.activity).nextStartedActivity
  assertEquals("contacts",intent.getStringExtra("access"));assertFalse(intent.getBooleanExtra("reopen",true))
 }
 @Test fun functionCenterShowsRealUnconfiguredStatesAndCanReturn(){
  ready();ui.onNodeWithTag("open-function-center").performClick()
  ui.waitUntil(15000){ui.onAllNodesWithTag("feature-select-overlay").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithText("Ventana flotante").assertExists()
  val app=ui.activity.application as SpotlightApp
  runBlocking{val c=app.preferences.config.first();app.preferences.updateAppearance(c.copy(theme="dark",preset="glass",animations=false))}
  ui.waitForIdle();capture("tablet-function-center")
  ui.onNodeWithText("Volver al inicio").performScrollTo().performClick()
  ui.onNodeWithTag("settings-home").assertExists()
 }
 @Test fun shortcutsNavigateWithoutChangingPreferences(){
  ready();val app=ui.activity.application as SpotlightApp;val before=runBlocking{app.preferences.config.first()}
  ui.onNodeWithText("Conectar Gemini").performClick();ui.onNodeWithText("Modelo").assertExists()
  assertEquals(before,runBlocking{app.preferences.config.first()})
 }
 @Test @Config(qualifiers="w360dp-h800dp") fun phoneHomeAndLightThemeRemainUsable(){
  ready();val app=ui.activity.application as SpotlightApp
  runBlocking{val c=app.preferences.config.first();app.preferences.updateAppearance(c.copy(theme="light",animations=false))}
  ui.waitForIdle();capture("phone-home-light")
  ui.onNodeWithTag("open-function-center").performClick()
  ui.onNodeWithTag("guided-setup-start").performScrollTo().assertExists()
 }
}
