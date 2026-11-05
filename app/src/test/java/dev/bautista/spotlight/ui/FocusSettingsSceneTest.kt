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
class FocusSettingsSceneTest {
 @get:Rule val ui=createAndroidComposeRule<MainActivity>()
 private val app get()=ui.activity.application as SpotlightApp
 private fun ready(material:String="raycast",theme:String="dark"){ui.waitUntil(15000){ui.onAllNodesWithTag("settings-search").fetchSemanticsNodes().isNotEmpty()};runBlocking{val c=app.preferences.config.first();app.preferences.updateAppearance(c.copy(theme=theme,preset=material,animations=false,focusInterface=true))};ui.waitForIdle()}
 private fun capture(n:String){ui.waitForIdle();captureScreenRoboImage("build/focus-settings/$n.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))}
 private fun scene(theme:String,phone:Boolean){
  ready(theme=theme)
  fun presentation(focus:Boolean){runBlocking{app.preferences.updateAppearance(app.preferences.config.first().copy(focusInterface=focus))};ui.waitForIdle()}
  presentation(false);capture("${if(phone)"phone" else "tablet"}-$theme-legacy-general")
  presentation(true);capture("${if(phone)"phone" else "tablet"}-$theme-general")
  SettingsGroups.names.forEachIndexed{i,group->
   if(phone && group!="General"){ui.onNodeWithText("Cambiar").performClick();ui.onNode(hasText(group) and hasAnyAncestor(isPopup())).performClick()}
   else if(!phone)ui.onNodeWithTag("settings-area:$group").performScrollTo().performClick()
   ui.onNodeWithTag("settings-details").assertExists()
   if(group=="Apariencia"){presentation(false);capture("${if(phone)"phone" else "tablet"}-$theme-legacy-appearance");presentation(true)}
   capture("${if(phone)"phone" else "tablet"}-$theme-${i+1}")
   assertNull("Visiting $group must not launch an external activity",Shadows.shadowOf(ui.activity).nextStartedActivity)
  }
 }
 @Test fun tabletDark()=scene("dark",false)
 @Test fun tabletLight()=scene("light",false)
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneDark()=scene("dark",true)
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneLight()=scene("light",true)
 @Test fun opacityIsImmediatePersistentAndSurvivesActivityRecreation(){
  ready("glass");ui.onNodeWithTag("settings-area:Apariencia").performClick()
  ui.onNodeWithTag("surface-opacity-slider").performScrollTo().performSemanticsAction(SemanticsActions.SetProgress){it(.73f)}
  ui.waitUntil(15000){runBlocking{app.preferences.config.first().opacity==.73f}}
  ui.onNodeWithTag("surface-opacity-value").assertTextEquals("73%")
  capture("tablet-opacity-73")
  ui.activityRule.scenario.recreate()
  ui.waitUntil(15000){ui.onAllNodesWithTag("surface-opacity-slider").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithTag("surface-opacity-value").assertTextEquals("73%")
  ui.onNodeWithText("Restablecer opacidades del material").performScrollTo().performClick()
  ui.waitUntil(15000){runBlocking{app.preferences.config.first().opacity==.84f}}
  ui.onNodeWithTag("surface-opacity-value").assertTextEquals("84%")
  assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
 }

 @Test fun roleOpacityAndThemeSaveImmediatelyWithoutApply(){
  ready();ui.onNodeWithTag("settings-area:Apariencia").performClick()
  ui.onNodeWithTag("action-opacity-slider").performScrollTo().performSemanticsAction(SemanticsActions.SetProgress){it(.81f)}
  ui.waitUntil(15000){runBlocking{app.preferences.config.first().actionOpacity==.81f}}
  ui.onNodeWithTag("settings-opacity-slider").performScrollTo().performSemanticsAction(SemanticsActions.SetProgress){it(.89f)}
  ui.waitUntil(15000){runBlocking{app.preferences.config.first().settingsOpacity==.89f}}
  ui.onNodeWithText("macOS Light").performScrollTo().performClick()
  ui.waitUntil(15000){runBlocking{app.preferences.config.first().theme=="light"}}
  ui.onNodeWithText("Aplicar apariencia").assertDoesNotExist()
  ui.activityRule.scenario.recreate()
  ui.waitUntil(15000){ui.onAllNodesWithTag("settings-opacity-slider").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithTag("action-opacity-value").assertTextEquals("81%")
  ui.onNodeWithTag("settings-opacity-value").assertTextEquals("89%")
  assertEquals("light",runBlocking{app.preferences.config.first().theme})
 }

}
