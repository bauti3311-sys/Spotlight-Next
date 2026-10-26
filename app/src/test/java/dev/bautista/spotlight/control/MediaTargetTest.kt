package dev.bautista.spotlight.control

import android.app.Application
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], application=Application::class)
class MediaTargetTest {
    private val app get() = RuntimeEnvironment.getApplication()
    private val manager get() = shadowOf(app.getSystemService(MediaSessionManager::class.java))
    private fun session(name:String, state:Int, actions:Long=PlaybackState.ACTION_PAUSE):MediaController {
        val controller=MediaSession(app,name).controller
        shadowOf(controller).setPackageName(name)
        shadowOf(controller).setPlaybackState(PlaybackState.Builder().setState(state,0,1f).setActions(actions).build())
        manager.addController(controller)
        return controller
    }
    private fun action(controller:MediaController)=shadowOf(controller.transportControls).lastPerformedAction

    @Test fun activePlaybackWinsOverFirstPausedSession() {
        val paused=session("test.paused",PlaybackState.STATE_PAUSED)
        val playing=session("test.playing",PlaybackState.STATE_PLAYING)
        assertTrue(MediaControl.available(app,"pause"))
        assertTrue(MediaControl.execute(app,"pause").contains("test.playing"))
        assertEquals(0L,action(paused))
        assertEquals(PlaybackState.ACTION_PAUSE,action(playing))
    }
    @Test fun simultaneousPlayersNeverReceiveAnArbitraryCommand() {
        val first=session("test.first",PlaybackState.STATE_PLAYING)
        val second=session("test.second",PlaybackState.STATE_PLAYING)
        assertFalse(MediaControl.available(app,"pause"))
        assertTrue(runCatching{MediaControl.execute(app,"pause")}.isFailure)
        assertEquals(0L,action(first));assertEquals(0L,action(second))
    }
    @Test fun severalPausedPlayersAreAmbiguous() {
        val first=session("test.first",PlaybackState.STATE_PAUSED,PlaybackState.ACTION_PLAY)
        val second=session("test.second",PlaybackState.STATE_PAUSED,PlaybackState.ACTION_PLAY)
        assertFalse(MediaControl.available(app,"play"))
        assertTrue(runCatching{MediaControl.execute(app,"play")}.isFailure)
        assertEquals(0L,action(first));assertEquals(0L,action(second))
    }
    @Test fun singlePausedPlayerStillSupportsResume() {
        val only=session("test.only",PlaybackState.STATE_PAUSED,PlaybackState.ACTION_PLAY)
        assertTrue(MediaControl.available(app,"play"))
        MediaControl.execute(app,"play")
        assertEquals(PlaybackState.ACTION_PLAY,action(only))
    }
    @Test fun unsupportedCommandsNeverDispatch() {
        val only=session("test.only",PlaybackState.STATE_PLAYING)
        for(command in listOf("next","invalid")) {
            assertFalse(MediaControl.available(app,command))
            assertTrue(runCatching{MediaControl.execute(app,command)}.isFailure)
        }
        assertEquals(0L,action(only))
    }
    @Test fun contextualCommandNeverControlsUnrelatedPlayer() {
        val spotify=session("com.spotify.music",PlaybackState.STATE_PLAYING)
        val youtube=session("com.google.android.youtube",PlaybackState.STATE_PLAYING)
        assertTrue(MediaControl.availableForPackage(app,"com.spotify.music","pause"))
        assertFalse(MediaControl.availableForPackage(app,"com.unknown.music","pause"))
        assertTrue(MediaControl.executeForPackage(app,"com.spotify.music","pause").contains("com.spotify.music"))
        assertEquals(PlaybackState.ACTION_PAUSE,action(spotify))
        assertEquals(0L,action(youtube))
    }

    @Test fun contextualCommandRejectsTwoSessionsFromSameApp() {
        val first=session("com.spotify.music",PlaybackState.STATE_PLAYING)
        val second=session("com.spotify.music",PlaybackState.STATE_PLAYING)
        assertFalse(MediaControl.availableForPackage(app,"com.spotify.music","pause"))
        assertTrue(runCatching { MediaControl.executeForPackage(app,"com.spotify.music","pause") }.isFailure)
        assertEquals(0L,action(first))
        assertEquals(0L,action(second))
    }

    @Test fun contextualCommandRejectsUnsupportedActionEvenIfPlayerExists() {
        val spotify=session("com.spotify.music",PlaybackState.STATE_PLAYING,PlaybackState.ACTION_PAUSE)
        assertFalse(MediaControl.availableForPackage(app,"com.spotify.music","next"))
        assertTrue(runCatching { MediaControl.executeForPackage(app,"com.spotify.music","next") }.isFailure)
        assertEquals(0L,action(spotify))
    }

    @Test fun executionRefreshesSessionsAfterAvailabilityCheck() {
        val first=session("test.first",PlaybackState.STATE_PLAYING)
        assertTrue(MediaControl.available(app,"pause"))
        val second=session("test.second",PlaybackState.STATE_PLAYING)
        assertTrue(runCatching{MediaControl.execute(app,"pause")}.isFailure)
        assertEquals(0L,action(first));assertEquals(0L,action(second))
        manager.clearControllers()
        assertFalse(MediaControl.available(app,"pause"))
    }
}
