package dev.bautista.spotlight.integrations.spotify
import dev.bautista.spotlight.SpotlightApp
import android.content.Context
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class SpotifyLocalConsentTest {
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 private val item=SpotifySearchItem(SpotifyItemType.TRACK,"a".repeat(22),"Fixture","Artist")
 @Test fun selectedPlaybackRequiresExplicitLocalOptInBeforeAnyClientConnection()=runBlocking {
  app.spotify.configure("a".repeat(32))
  assertFalse(app.spotify.localPlaybackEnabled)
  val error=runCatching{app.spotify.playback("play",item)}.exceptionOrNull()
  assertTrue(error!!.message!!.contains("Autorizá primero"))
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
 @Test fun disconnectRevokesTheLocalOptInWithoutPlayingAnything()=runBlocking {
  // Marker represents local user preference, never a real Spotify authorization.
  app.getSharedPreferences("spotify-public",Context.MODE_PRIVATE).edit().putBoolean("local-playback-consent",true).commit()
  assertTrue(app.spotify.localPlaybackEnabled)
  app.spotify.disconnect()
  assertFalse(app.spotify.localPlaybackEnabled)
  assertEquals(SpotifyConnectionState.DISCONNECTED,app.spotify.state.value)
  assertTrue(runCatching{app.spotify.playback("play",item)}.exceptionOrNull()!!.message!!.contains("Autorizá primero"))
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
}
