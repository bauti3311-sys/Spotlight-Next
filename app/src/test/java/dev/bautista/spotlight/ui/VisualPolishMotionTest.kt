package dev.bautista.spotlight.ui

import android.media.session.*
import android.os.Looper
import android.provider.Settings
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
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.MediaSnapshot
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** D5 ONLY in tests. The 160ms helper is a reference, not the actual 140ms View window entry. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w600dp-h500dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
class VisualPolishMotionTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private class Probe {var starts=0;var completed=0;var cancelled=0;var allowed=true;var animation:Animatable<Float,AnimationVector1D>?=null}
 private val probe=Probe()
 private var variant by mutableStateOf("B")
 private var generation by mutableIntStateOf(0)
 private var visible by mutableStateOf(true)
 private var requests=0
 private var mediaSession:MediaSession?=null
 @Before fun setup(){Settings.Global.putFloat(ui.activity.contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE,1f)}
 @After fun clean(){mediaSession?.release();Settings.Global.putFloat(ui.activity.contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE,1f)}
 @Composable private fun Modifier.experimentalFade(key:Int):Modifier {
  val allowed=LocalMotion.current
  val alpha=remember(key){Animatable(if(allowed)0f else 1f)}
  var appeared by remember(alpha){mutableStateOf(false)}
  SideEffect{probe.animation=alpha;probe.allowed=allowed}
  LaunchedEffect(alpha,allowed) {
   if(!allowed || appeared){appeared=true;alpha.snapTo(1f)}
   else {
    appeared=true;probe.starts++
    try {alpha.animateTo(1f,tween(120));probe.completed++}
    finally {if(!currentCoroutineContext().isActive)probe.cancelled++}
   }
  }
  return graphicsLayer{this.alpha=alpha.value}
 }
 private fun content(theme:String="dark") {
  val app=ui.activity.application as SpotlightApp
  val session=MediaSession(app,"d5-no-audio").also{mediaSession=it}
  val snapshot=MediaSnapshot(session.sessionToken,"com.spotify.music","Horizonte","Fixture de movimiento · sin audio","Sesión sintética en pausa",null,180000,
   PlaybackState.Builder().setState(PlaybackState.STATE_PAUSED,42000,0f,1).setActions(PlaybackState.ACTION_PLAY).build(),1)
  ui.mainClock.autoAdvance=false
  ui.setContent {SpotlightTheme(theme,AppConfig(focusInterface=true,animations=true)) {
   Box(Modifier.size(360.dp,300.dp).background(MaterialTheme.colorScheme.background).testTag("d5-scene")) {
    if(visible)key(variant,generation) {
     val entry=if(variant=="A")Modifier.revealMotion(generation,true) else Modifier.experimentalFade(generation)
     Column(Modifier.padding(12.dp).then(entry).background(MaterialTheme.colorScheme.surface).padding(12.dp)) {
      MediaInspector(snapshot,{requests++},{requests++})
     }
    }
   }
  }}
 }
 private fun advance(ms:Long){ui.mainClock.advanceTimeBy(ms);ui.waitForIdle()}
 private fun capture(name:String){ui.onNodeWithTag("d5-scene").captureRoboImage("build/visual-polish/d5/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))}
 private fun systemMotion(enabled:Boolean) {
  ui.runOnIdle{
   Settings.Global.putFloat(ui.activity.contentResolver,Settings.Global.ANIMATOR_DURATION_SCALE,if(enabled)1f else 0f)
   ui.activity.contentResolver.notifyChange(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),null)
  }
  Shadows.shadowOf(Looper.getMainLooper()).idle();advance(32)
 }
 private fun compare(theme:String) {
  content(theme)
  for(v in listOf("A","B")) {
   ui.runOnIdle{variant=v;generation++};advance(16)
   var last=0L
   for(t in listOf(0L,48L,96L,120L,160L,208L)){advance(t-last);last=t;capture("$theme-$v-$t")}
  }
  assertEquals(1f,probe.animation!!.value,.001f)
  assertFalse(probe.animation!!.isRunning);assertEquals(0,requests)
 }
 @Test fun darkReference160AndExperimental120Frames()=compare("dark")
 @Test fun lightReference160AndExperimental120Frames()=compare("light")
 @Test fun interruptedEntryCancelsTheOldCoroutineAndOnlyCompletesTheNewOne() {
  content();advance(48)
  assertTrue(probe.animation!!.value in 0f..1f);assertTrue(probe.animation!!.isRunning)
  ui.runOnIdle{generation++};advance(208)
  assertEquals(2,probe.starts);assertEquals(1,probe.cancelled);assertEquals(1,probe.completed)
  assertEquals(1f,probe.animation!!.value,.001f);assertFalse(probe.animation!!.isRunning)
  assertEquals(0,requests)
 }
 @Test fun aNewEntryAfterHidingNeverLeavesTheOldAnimationRunning() {
  content();advance(48)
  val old=probe.animation!!
  ui.runOnIdle{visible=false};advance(16)
  assertFalse(old.isRunning);assertEquals(1,probe.cancelled)
  ui.runOnIdle{generation++;visible=true};advance(16);advance(208)
  assertEquals(2,probe.starts);assertEquals(1,probe.completed)
  assertFalse(probe.animation!!.isRunning);assertEquals(0,requests)
 }
 @Test fun systemReducedMotionStopsImmediatelyAndReenablingDoesNotReplayEntry() {
  content();advance(48)
  systemMotion(false)
  assertFalse(probe.allowed);assertEquals(1f,probe.animation!!.value,.001f);assertFalse(probe.animation!!.isRunning)
  capture("reduced-during-entry")
  val starts=probe.starts
  systemMotion(true);advance(208)
  assertTrue(probe.allowed);assertEquals(starts,probe.starts)
  assertEquals(1f,probe.animation!!.value,.001f);assertEquals(0,requests)
 }
}
