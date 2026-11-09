package dev.bautista.spotlight.ui

import android.content.Intent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.MainActivity
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.overlay.AdaptiveLayout
import dev.bautista.spotlight.overlay.OverlayService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp-mdpi",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class SettingsRefreshTest {
 @get:Rule val ui=createAndroidComposeRule<MainActivity>()
 private val app get()=ui.activity.application as SpotlightApp
 private val phase get()=System.getenv("SETTINGS_CAPTURE_PHASE") ?: "after"
 private fun ready(){ui.waitUntil(15000){ui.onAllNodesWithTag("settings-search").fetchSemanticsNodes().isNotEmpty()}}
 private fun config(theme:String="light"){runBlocking{app.preferences.updateAppearance(app.preferences.config.first().copy(focusInterface=true,theme=theme,animations=false))};ui.waitForIdle()}
 private fun appearance(){if(ui.onAllNodesWithTag("settings-area:Apariencia").fetchSemanticsNodes().isNotEmpty())ui.onNodeWithTag("settings-area:Apariencia").performScrollTo().performClick() else{ui.onNodeWithText("Cambiar").performClick();ui.onNodeWithText("Apariencia").performClick()};ui.onNodeWithTag("window-width-slider").assertExists()}
 private fun capture(name:String){ui.waitForIdle();ui.onRoot().captureRoboImage("build/settings-refresh/$phase/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))}
 @Test fun tabletLightCapture(){ready();config();capture("tablet-light-home");appearance();capture("tablet-light-appearance")}
 @Test fun tabletDarkCapture(){ready();config("dark");capture("tablet-dark-home");appearance();capture("tablet-dark-appearance")}
 @Test @Config(qualifiers="w390dp-h844dp-mdpi") fun phoneCapture(){ready();config();capture("phone-light-home");appearance();capture("phone-light-appearance")}
 @Test @Config(qualifiers="w330dp-h800dp-mdpi") fun enlargedPhoneCapture(){ready();config("dark");ui.activity.resources.configuration.fontScale=2f;ui.activityRule.scenario.recreate();ready();appearance();capture("phone-dark-200-appearance")}
 @Test fun changedWidthReachesTheAlreadyOpenAndroidWindow(){
  ready();config();runBlocking{app.preferences.updateAppearance(app.preferences.config.first().copy(windowScale=0f))}
  org.robolectric.shadows.ShadowSettings.setCanDrawOverlays(true)
  val controller=Robolectric.buildService(OverlayService::class.java).create();val service=controller.get()
  val read=service.javaClass.getDeclaredMethod("getConfig").apply{isAccessible=true}
  fun host()=ReflectionHelpers.getField<View?>(service,"view")
  fun await(scale:Float){ui.waitUntil(15000){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();host()?.onWindowFocusChanged(true);host()!=null && (read.invoke(service) as AppConfig).windowScale==scale}}
  try{
   service.onStartCommand(Intent(ui.activity,service.javaClass),0,1);await(0f)
   val first=host();val narrow=(first!!.layoutParams as WindowManager.LayoutParams).width
   service.updateAvailableHeight(250)
   runBlocking{app.preferences.updateAppearance(app.preferences.config.first().copy(windowScale=1f))};await(1f)
   assertSame("Changing width must retain the active window",first,host())
   assertTrue("Stored width changed, but open WindowManager width stayed $narrow",(host()!!.layoutParams as WindowManager.LayoutParams).width>narrow)
   runBlocking{app.preferences.updateAppearance(app.preferences.config.first().copy(windowScale=0f))};await(0f)
   assertEquals(narrow,(host()!!.layoutParams as WindowManager.LayoutParams).width)
  }finally{controller.destroy();org.robolectric.shadows.ShadowSettings.setCanDrawOverlays(false)}
 }
}
