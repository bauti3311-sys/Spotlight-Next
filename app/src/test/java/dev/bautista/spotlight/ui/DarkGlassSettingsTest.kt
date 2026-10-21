package dev.bautista.spotlight.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
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

/** Native Activity captures. System wallpaper and One UI input dispatch require a device. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class DarkGlassSettingsTest {
 @get:Rule val ui=createAndroidComposeRule<MainActivity>()
 private val app get()=ui.activity.application as SpotlightApp
 private fun ready(){ui.waitUntil(15000){ui.onAllNodesWithTag("settings-search").fetchSemanticsNodes().isNotEmpty()};runBlocking{val c=app.preferences.config.first();app.preferences.updateAppearance(c.copy(theme="dark",preset="glass",animations=false))};ui.waitForIdle()}
 private fun capture(n:String){ui.waitForIdle();captureScreenRoboImage("build/dark-glass/$n.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))}
 @Test fun tabletCapturesAllExistingAreasWithoutRequestingPermissions(){
  ready()
  if(ui.onAllNodesWithText("Más opciones").fetchSemanticsNodes().isNotEmpty())ui.onNodeWithText("Más opciones").performClick()
  SettingsGroups.names.forEachIndexed{i,group->
   ui.onNodeWithTag("settings-area:$group").performScrollTo().performClick()
   ui.onNodeWithTag("settings-details").assertExists();capture("tablet-${i+1}")
   assertNull("Visiting $group must not request access",Shadows.shadowOf(ui.activity).nextStartedActivity)
  }
 }
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneAppearanceAndPermissionsRemainNavigable(){
  ready();capture("phone-general")
  ui.onNodeWithText("Cambiar").performClick();ui.onNodeWithText("Apariencia").performClick();capture("phone-appearance")
  ui.onNodeWithText("Restablecer apariencia").performScrollTo().assertIsDisplayed()
  ui.onNodeWithText("Cambiar").performClick();ui.onNodeWithText("Permisos y accesos").performClick();capture("phone-permissions")
  listOf("Búsqueda y archivos","Integraciones","Inteligencia artificial","Atajos","Avanzado").forEachIndexed{i,group->
   ui.onNodeWithText("Cambiar").performClick();ui.onNodeWithText(group).performClick();capture("phone-area-${i+1}")
   ui.onNodeWithTag("settings-details").assertExists()
  }
  assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
 }
 @Test fun opacityIsImmediatePersistentAndSurvivesActivityRecreation(){
  ready();ui.onNodeWithTag("settings-area:Apariencia").performClick()
  ui.onNodeWithTag("surface-opacity-slider").performScrollTo().performSemanticsAction(SemanticsActions.SetProgress){it(.73f)}
  ui.waitUntil(15000){runBlocking{app.preferences.config.first().opacity==.73f}}
  ui.onNodeWithTag("surface-opacity-value").assertTextEquals("73%")
  capture("tablet-opacity-73")
  ui.activityRule.scenario.recreate()
  ui.waitUntil(15000){ui.onAllNodesWithTag("surface-opacity-slider").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithTag("surface-opacity-value").assertTextEquals("73%")
  ui.onNodeWithText("Restablecer opacidad · 80%").performScrollTo().performClick()
  ui.waitUntil(15000){runBlocking{app.preferences.config.first().opacity==.80f}}
  ui.onNodeWithTag("surface-opacity-value").assertTextEquals("80%")
  assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
 }

}
