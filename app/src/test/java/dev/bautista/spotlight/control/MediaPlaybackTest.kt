package dev.bautista.spotlight.control

import android.app.Application
import android.media.session.PlaybackState
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class MediaPlaybackTest {
    @Test fun badgeRequiresActualPlayingState() {
        assertFalse(MediaControl.isPlaying(null))
        for(state in listOf(PlaybackState.STATE_NONE,PlaybackState.STATE_PAUSED,PlaybackState.STATE_BUFFERING,PlaybackState.STATE_STOPPED))assertFalse(MediaControl.isPlaying(PlaybackState.Builder().setState(state,0,1f).build()))
        assertTrue(MediaControl.isPlaying(PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING,0,1f).build()))
    }
}
