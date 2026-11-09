package dev.bautista.spotlight.ui

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.overlay.AdaptiveLayout
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp-mdpi",application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class WidthResponseTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 @Test fun explicitWidthChangesVisiblePuenteFinder(){
  var c by mutableStateOf(AppConfig(focusInterface=true,windowScale=0f,animations=false))
  ui.setContent{SpotlightTheme("light",c){Box(Modifier.fillMaxSize().testTag("width-canvas")){
   PuenteFrame(400.dp,false,null,header={Text("Buscar aplicaciones y comandos")},results={Text("Proyecto Aurora")},footer={Text("Acciones")},inspector=null,inspectorHeader={},modifier=Modifier.width(AdaptiveLayout.bounds(1280,900,1f,0,c).width.dp))
  }}}
  val phase=System.getenv("SETTINGS_CAPTURE_PHASE") ?: "after"
  val narrow=ui.onNodeWithTag("puente-finder").getUnclippedBoundsInRoot().width
  ui.onNodeWithTag("width-canvas").captureRoboImage("build/settings-refresh/$phase/width-narrow.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  ui.runOnIdle{c=c.copy(windowScale=1f)}
  ui.onNodeWithTag("width-canvas").captureRoboImage("build/settings-refresh/$phase/width-wide.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  assertTrue("Visible Puente finder stayed capped at $narrow",ui.onNodeWithTag("puente-finder").getUnclippedBoundsInRoot().width>narrow)
 }
 @Test fun phoneWidthSliderHasAnEffectiveRange(){
  val a=AdaptiveLayout.bounds(412,860,1f,0,AppConfig(windowScale=0f))
  val b=AdaptiveLayout.bounds(412,860,1f,0,AppConfig(windowScale=1f))
  assertTrue("Entire phone slider resolves to same width: ${a.width}",b.width-a.width>=40)
 }
}
