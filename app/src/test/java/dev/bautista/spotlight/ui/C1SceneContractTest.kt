package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
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

/** Matched production Compose scenes, synthetic packages, no account or physical-device claims. */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], qualifiers="w1280dp-h900dp", application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class C1SceneContractTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun show(width:Int,theme:String,query:String="Spotify",height:Float=680f) {
        val info=ApplicationInfo().apply{packageName="com.spotify.music";nonLocalizedLabel="Spotify";enabled=true}
        val activity=ActivityInfo().apply{packageName=info.packageName;name="Main";nonLocalizedLabel="Spotify";applicationInfo=info;exported=true;enabled=true}
        Shadows.shadowOf(app.packageManager).installPackage(PackageInfo().apply{
            packageName=info.packageName;applicationInfo=info;activities=arrayOf(activity);versionName="1.0-fixture"
        })
        Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=activity})
        app.search.invalidateApps()
        ui.setContent { Box(Modifier.width(width.dp)) {
            SpotlightPanel(app,AppConfig(theme=theme,preset="glass",files=false,online=false,animations=false,learning=false,
                favorites=setOf("com.spotify.music/Main")),{},query,height)
        } }
        ui.waitUntil(15000){ui.onAllNodesWithTag(if(query.isBlank())"result:suggest:app:com.spotify.music" else "result:com.spotify.music/Main").fetchSemanticsNodes().isNotEmpty()}
    }
    private fun capture(name:String) {
        val phase=System.getenv("SPOTLIGHT_C1_CAPTURE_PHASE") ?: "after"
        require(phase in setOf("before","refactor","after"))
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/c1/$phase/$name.png",
            roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    private fun inspector(width:Int,theme:String) {
        show(width,theme)
        // Preview60 uses the existing stacked60dp row on compact phones, inline48dp on tablet.
        ui.onNodeWithTag("result:com.spotify.music/Main").assertHeightIsEqualTo(if(width<700)60.dp else 48.dp).performClick()
        ui.onNodeWithTag("inspector-title").assertTextEquals("Spotify")
        // Package metadata loads on IO. Capture the settled scene, not an intermediate height.
        ui.waitUntil(15000){ui.onAllNodesWithText("1.0-fixture").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("context-footer").assertIsDisplayed()
        if(width<700)ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        capture("${if(width<700)"phone" else "tablet"}-$theme-inspector")
        ui.onNodeWithContentDescription("Cerrar inspector").performClick()
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).assertTextEquals("Spotify").assertIsFocused()
        assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
    }
    @Test fun tabletDark()=inspector(960,"dark")
    @Test fun tabletLight()=inspector(960,"light")
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneDark()=inspector(360,"dark")
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneLight()=inspector(360,"light")
    @Test fun homeRemainsCompactWithAtMostThreeSuggestions() {
        show(960,"dark",query="")
        val rows=ui.onAllNodes(SemanticsMatcher("result rows") { it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("result:")==true }).fetchSemanticsNodes()
        assertTrue(rows.size in 1..3)
        ui.onNodeWithTag("result:suggest:app:com.spotify.music").assertHeightIsEqualTo(48.dp)
        capture("tablet-dark-home")
    }
    @Test @Config(qualifiers="w390dp-h844dp") fun reducedViewportPreservesSearchAndFooter() {
        show(360,"light",height=300f)
        ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar"))).assertIsDisplayed().assertTextEquals("Spotify")
        ui.onNodeWithTag("context-footer").assertIsDisplayed()
        val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
        assertTrue(root.bottom-root.top<=300.dp)
        assertTrue(ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot().bottom<=root.bottom)
        capture("phone-light-ime-budget")
    }
}
