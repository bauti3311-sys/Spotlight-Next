package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Time samples of real Compose layers; no FPS, GPU or RAM performance claim. B is test-only. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w600dp-h400dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
class VisualFirstMotionTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 @Test fun compareEntryFramesAndReducedMotion() {
  var variant by mutableStateOf("A")
  var generation by mutableIntStateOf(0)
  var reduced by mutableStateOf(false)
  ui.mainClock.autoAdvance=false
  ui.setContent {SpotlightTheme("dark",AppConfig(focusInterface=true,animations=true)) {
   CompositionLocalProvider(LocalMotion provides !reduced) {
    Box(Modifier.size(360.dp,220.dp).background(MaterialTheme.colorScheme.background).testTag("motion-canvas")) {
     key(variant,generation,reduced) {
      val progress=remember {Animatable(if(reduced)1f else 0f)}
      if(variant=="B")LaunchedEffect(Unit){if(!reduced)progress.animateTo(1f,tween(120))}
      Column(Modifier.padding(12.dp).then(if(variant=="A")Modifier.revealMotion(generation,true) else Modifier.graphicsLayer{alpha=progress.value})
       .background(MaterialTheme.colorScheme.surface).padding(18.dp)) {
       Text("Spotlight",style=MaterialTheme.typography.titleMedium)
       Text("Mismo contenido · entrada en Compose",style=MaterialTheme.typography.bodyMedium)
       Spacer(Modifier.height(12.dp));Text("A: 160 ms + 6 dp · B: opacidad 120 ms",style=MaterialTheme.typography.bodySmall)
      }
     }
    }
   }
  }}
  for(v in listOf("A","B")) {
   ui.runOnUiThread{variant=v;generation++};ui.mainClock.advanceTimeByFrame();ui.waitForIdle()
   var previous=0L
   for(time in listOf(0L,48L,96L,160L,208L)) {
    ui.mainClock.advanceTimeBy(time-previous);previous=time;ui.waitForIdle()
    ui.onNodeWithTag("motion-canvas").captureRoboImage("build/visual-first/motion-$v-$time.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
   }
   ui.runOnUiThread{reduced=true;generation++};ui.mainClock.advanceTimeByFrame();ui.waitForIdle()
   ui.onNodeWithText("Spotlight").assertIsDisplayed()
   ui.onNodeWithTag("motion-canvas").captureRoboImage("build/visual-first/motion-$v-reduced.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
   ui.runOnUiThread{reduced=false}
  }
  // Token contrast is measured separately from antialiased screenshot pixels.
  for(dark in listOf(true,false)) {
   val colors=FocusDesign.palette(if(dark)darkColorScheme() else lightColorScheme(),dark,AppConfig(focusInterface=true))
   assertTrue(InspectorAccent.contrast(colors.onSurface,colors.surface)>=4.5f)
   assertTrue(InspectorAccent.contrast(colors.onSurfaceVariant,colors.surface)>=4.5f)
  }
 }
}
