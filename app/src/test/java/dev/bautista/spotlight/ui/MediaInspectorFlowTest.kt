package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MediaInspectorFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private var width by mutableIntStateOf(800)
    private fun search()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
    private fun show(session:Boolean=true) {
        val pkg="com.spotify.music"
        val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel="Spotify"}
        val activity=ActivityInfo().apply{packageName=pkg;name="Main";nonLocalizedLabel="Spotify";applicationInfo=a;exported=true}
        Shadows.shadowOf(app.packageManager).installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity)})
        Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=activity})
        app.search.invalidateApps()
        if(session) {
            val c=MediaSession(app,"inspector-fixture").controller
            Shadows.shadowOf(c).setPackageName(pkg)
            Shadows.shadowOf(c).setMetadata(MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE,"Pista de prueba local").putString(MediaMetadata.METADATA_KEY_ARTIST,"Sesión de prueba · sin audio").putLong(MediaMetadata.METADATA_KEY_DURATION,180000).putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART,Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888).apply{eraseColor(android.graphics.Color.DKGRAY)}).build())
            Shadows.shadowOf(c).setPlaybackState(PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING,42000,1f,android.os.SystemClock.elapsedRealtime()).setActions(PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_SKIP_TO_NEXT).build())
            Shadows.shadowOf(app.getSystemService(MediaSessionManager::class.java)).addController(c)
        }
        ui.setContent { Box(Modifier.width(width.dp)){SpotlightPanel(app,AppConfig(theme="dark",preset="glass",files=false,online=false,animations=false,learning=false,suggestions=false),{},"spot",availableHeightDp=700f)} }
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:com.spotify.music/Main").fetchSemanticsNodes().isNotEmpty()}
    }
    @Test fun realSessionMetadataIsContextualAndPinSurvivesSearchAndWidthChange() {
        show()
        ui.onNodeWithTag("media-inspector").assertIsDisplayed()
        ui.onNodeWithTag("context-inspector").assertExists()
        ui.onNodeWithText("Pista de prueba local").assertIsDisplayed()
        ui.onNodeWithContentDescription("Pausar reproducción").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        ui.onNodeWithContentDescription("Pista anterior").assertDoesNotExist()
        ui.onNodeWithTag("media-seek").assertDoesNotExist()
        captureScreenRoboImage("build/media-inspector/tablet-session-fixture.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        ui.onNodeWithContentDescription("Fijar inspector").performClick()
        search().performClick().performTextReplacement("2+2")
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:calc").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText("Pista de prueba local").assertIsDisplayed()
        ui.runOnIdle{width=360}
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        ui.onNodeWithTag("media-inspector").assertExists()
        search().assertTextEquals("2+2")
    }
    @Test fun absentSessionDoesNotCreateAnEmptyInspector() {
        show(false)
        ui.onNodeWithTag("media-inspector").assertDoesNotExist()
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
    }
    @Test @Config(qualifiers="w390dp-h844dp") fun phonePlayerFitsAndEscapeRestoresQuery() {
        width=360;show()
        ui.onNodeWithTag("media-inspector").assertIsDisplayed()
        ui.onNodeWithContentDescription("Pausar reproducción").assertIsDisplayed()
        captureScreenRoboImage("build/media-inspector/phone-session-fixture.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        search().performKeyInput{pressKey(Key.Escape)}
        ui.onNodeWithTag("media-inspector").assertDoesNotExist()
        search().assertTextEquals("spot").assertIsFocused()
    }
}
