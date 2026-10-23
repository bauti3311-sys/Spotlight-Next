package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
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

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], qualifiers="w1280dp-h800dp", application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AdaptiveInspectorFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun search()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))

    @Test fun localThemeChangesWithPackageAndDoesNotLeakOutsideInspector() {
        var pkg by mutableStateOf("com.spotify.music")
        var outer=Color.Unspecified;var inner=Color.Unspecified;var after=Color.Unspecified
        ui.setContent { SpotlightTheme("dark",AppConfig(theme="dark")) {
            val before=MaterialTheme.colorScheme.primary
            SideEffect{outer=before}
            InspectorTheme(pkg,null) { val value=MaterialTheme.colorScheme.primary;SideEffect{inner=value} }
            val value=MaterialTheme.colorScheme.primary;SideEffect{after=value}
        } }
        ui.runOnIdle { assertEquals(InspectorAccent.brand(pkg),inner);assertEquals(outer,after);pkg="com.google.android.gm" }
        ui.runOnIdle { assertEquals(InspectorAccent.brand(pkg),inner);assertEquals(outer,after);assertNotEquals(outer,inner) }
    }

    private fun install(name:String,pkg:String) {
        val info=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=name}
        val activity=ActivityInfo().apply{packageName=pkg;this.name="Main";nonLocalizedLabel=name;applicationInfo=info;exported=true}
        Shadows.shadowOf(app.packageManager).installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=info;activities=arrayOf(activity);versionName="1.0-fixture"})
        Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=activity})
    }
    private fun show(width:Int=800) {
        install("Spotify","com.spotify.music");install("Gmail","com.google.android.gm");app.search.invalidateApps()
        ui.setContent {Box(Modifier.width(width.dp)){SpotlightPanel(app,
            AppConfig(theme="dark",preset="glass",files=false,online=false,animations=false,learning=false,suggestions=false),{},"Spotify",availableHeightDp=700f)}}
        awaitResult("com.spotify.music")
    }
    private fun awaitResult(pkg:String) {ui.waitUntil(15000){ui.onAllNodesWithTag("result:$pkg/Main").fetchSemanticsNodes().isNotEmpty()}}
    private fun assertButtonColor(expected:Color) {
        val image=ui.onNodeWithTag("inspector-primary-action").captureToImage()
        val actual=image.toPixelMap()[image.width/2,4]
        assertEquals(expected.red,actual.red,.02f);assertEquals(expected.green,actual.green,.02f);assertEquals(expected.blue,actual.blue,.02f)
    }
    @Test fun selectedInstalledAppsUseRealInspectorAndPinRetainsItsOwnColor() {
        show()
        ui.onNodeWithTag("result:com.spotify.music/Main").performClick()
        ui.onNodeWithTag("inspector-primary-action").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        assertButtonColor(InspectorAccent.brand("com.spotify.music")!!)
        captureScreenRoboImage("build/adaptive-inspector/tablet-spotify-fixture.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        ui.onNodeWithContentDescription("Fijar inspector").performClick()
        search().performClick().performTextReplacement("Gmail");awaitResult("com.google.android.gm")
        ui.onNodeWithTag("result:com.google.android.gm/Main").performClick()
        ui.onNodeWithTag("inspector-title").assertTextEquals("Spotify")
        assertButtonColor(InspectorAccent.brand("com.spotify.music")!!)
        ui.onNodeWithContentDescription("Desfijar inspector").performClick()
        ui.onNodeWithTag("inspector-title").assertTextEquals("Gmail")
        assertButtonColor(InspectorAccent.brand("com.google.android.gm")!!)
        captureScreenRoboImage("build/adaptive-inspector/tablet-gmail-fixture.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
    }

    @Test @Config(qualifiers="w390dp-h844dp") fun phoneUsesSameInspectorAndKeepsSearchAfterClosing() {
        show(360)
        ui.onNodeWithTag("result:com.spotify.music/Main").performClick()
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        ui.onNodeWithTag("inspector-primary-action").assertIsDisplayed()
        captureScreenRoboImage("build/adaptive-inspector/phone-spotify-fixture.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        ui.onNodeWithContentDescription("Cerrar inspector").performClick()
        ui.onNodeWithTag("inspector-content").assertDoesNotExist()
        search().assertTextEquals("Spotify").assertIsFocused()
        assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
    }
}
