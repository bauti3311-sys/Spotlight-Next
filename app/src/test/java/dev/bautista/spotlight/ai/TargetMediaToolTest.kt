package dev.bautista.spotlight.ai

import android.media.session.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class TargetMediaToolTest {
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 private fun session(pkg:String):MediaController {
  val c=MediaSession(app,pkg).controller
  Shadows.shadowOf(c).setPackageName(pkg)
  Shadows.shadowOf(c).setPlaybackState(PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING,0,1f).setActions(PlaybackState.ACTION_PAUSE).build())
  Shadows.shadowOf(app.getSystemService(MediaSessionManager::class.java)).addController(c);return c
 }
 @Test fun explicitlyConfirmedTargetNeverControlsAnotherApplication()=runBlocking {
  val other=session("com.other.player");val target=session("com.spotify.music")
  val call=ToolCall("control_app_media",mapOf("package" to "com.spotify.music","command" to "pause"))
  val executor=ToolExecutor(app)
  assertTrue(runCatching{executor.execute(call,AppConfig(),false)}.isFailure)
  assertEquals(0L,Shadows.shadowOf(target.transportControls).lastPerformedAction)
  val result=executor.execute(call,AppConfig(),true)
  assertEquals(ExecutionState.REQUESTED,result.state);assertTrue(result.text.contains("com.spotify.music"))
  assertEquals(PlaybackState.ACTION_PAUSE,Shadows.shadowOf(target.transportControls).lastPerformedAction)
  assertEquals(0L,Shadows.shadowOf(other.transportControls).lastPerformedAction)
 }
 @Test fun missingTargetDoesNotFallBackToAnotherPlayer()=runBlocking {
  val other=session("com.other.player")
  val call=ToolCall("control_app_media",mapOf("package" to "com.spotify.music","command" to "pause"))
  assertTrue(runCatching{ToolExecutor(app).execute(call,AppConfig(),true)}.isFailure)
  assertEquals(0L,Shadows.shadowOf(other.transportControls).lastPerformedAction)
 }
}
