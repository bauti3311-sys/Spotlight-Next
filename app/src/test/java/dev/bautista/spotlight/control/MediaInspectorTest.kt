package dev.bautista.spotlight.control

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.*
import org.junit.Assert.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], application=Application::class)
class MediaInspectorTest {
    private val app get()=RuntimeEnvironment.getApplication()
    private val manager get()=shadowOf(app.getSystemService(MediaSessionManager::class.java))
    private fun session(pkg:String,actions:Long=PlaybackState.ACTION_PAUSE):MediaController {
        val c=MediaSession(app,pkg).controller
        shadowOf(c).setPackageName(pkg)
        shadowOf(c).setPlaybackState(PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING,2000,1f,1000).setActions(actions).build())
        manager.addController(c)
        return c
    }
    @Test fun snapshotUsesOnlyTheExactPackageAndRealMetadata() {
        val other=session("other")
        val target=session("target")
        val art=Bitmap.createBitmap(8,8,Bitmap.Config.ARGB_8888)
        shadowOf(target).setMetadata(MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE,"Título local").putString(MediaMetadata.METADATA_KEY_ARTIST,"Artista").putLong(MediaMetadata.METADATA_KEY_DURATION,90000).putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART,art).build())
        val s=MediaControl.snapshotForPackage(app,"target")!!
        assertEquals("Título local",s.title);assertEquals("Artista",s.artist);assertSame(art,s.artwork)
        assertEquals(90000L,s.duration);assertEquals(5000L,s.positionAt(4000))
        assertTrue(s.supports(PlaybackState.ACTION_PAUSE));assertFalse(s.supports(PlaybackState.ACTION_SKIP_TO_NEXT))
        assertEquals(0L,shadowOf(other.transportControls).lastPerformedAction)
    }
    @Test fun ambiguousPackageNeverShowsMetadataOrDispatchesEvenIfOnlyOneSessionSupportsAction() {
        val first=session("target")
        session("target",0)
        assertNull(MediaControl.snapshotForPackage(app,"target"))
        assertFalse(MediaControl.availableForPackage(app,"target","pause"))
        assertTrue(runCatching{MediaControl.executeForPackage(app,"target","pause")}.isFailure)
        assertEquals(0L,shadowOf(first.transportControls).lastPerformedAction)
    }
    @Test fun disappearedSessionClearsSnapshotAndRejectsStaleControls() {
        session("target",PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_SEEK_TO)
        val s=MediaControl.snapshotForPackage(app,"target")!!
        manager.clearControllers()
        assertNull(MediaControl.snapshotForPackage(app,"target"))
        assertTrue(runCatching{MediaControl.seekForPackage(app,"target",3000,s.token)}.isFailure)
    }
    @Test fun progressIsClampedAndPausedOrUnknownPositionIsHonest() {
        val c=session("target")
        shadowOf(c).setMetadata(MediaMetadata.Builder().putLong(MediaMetadata.METADATA_KEY_DURATION,5000).build())
        assertEquals(5000L,MediaControl.snapshotForPackage(app,"target")!!.positionAt(9000))
        shadowOf(c).setPlaybackState(PlaybackState.Builder().setState(PlaybackState.STATE_PAUSED,2000,1f,1000).build())
        assertEquals(2000L,MediaControl.snapshotForPackage(app,"target")!!.positionAt(9000))
        shadowOf(c).setPlaybackState(PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING,PlaybackState.PLAYBACK_POSITION_UNKNOWN,1f,1000).build())
        assertNull(MediaControl.snapshotForPackage(app,"target")!!.positionAt(9000))
    }
    @Test fun seekingRequiresSupportedActionAndSameLiveSession() {
        val c=session("target",PlaybackState.ACTION_PAUSE)
        val old=MediaControl.snapshotForPackage(app,"target")!!
        assertTrue(runCatching{MediaControl.seekForPackage(app,"target",3000,old.token)}.isFailure)
        assertEquals(0L,shadowOf(c.transportControls).lastPerformedAction)
        manager.clearControllers();session("target",PlaybackState.ACTION_SEEK_TO)
        assertTrue(runCatching{MediaControl.seekForPackage(app,"target",3000,old.token)}.isFailure)
    }
    @Test fun observingRegistersOnlyTheTargetAndDisposalUnregistersCallbacks()=runBlocking(Dispatchers.Unconfined) {
        val target=session("target")
        val other=session("other")
        val values=mutableListOf<MediaSnapshot?>()
        val first=CompletableDeferred<MediaSnapshot?>()
        val job=launch{MediaControl.inspect(app,"target").collect{values+=it;first.complete(it)}}
        first.await()
        assertEquals(1,shadowOf(target).callbacks.size)
        assertEquals(0,shadowOf(other).callbacks.size)
        assertEquals("target",values.last()?.packageName)
        job.cancelAndJoin()
        assertEquals(0,shadowOf(target).callbacks.size)
    }
}
