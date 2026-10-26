package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
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

/** Whole production window, not an independently rendered inspector. No account/network. */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RaycastWindowCompositionTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private var viewport by mutableStateOf(510f)
    private fun search()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
    private fun install(title:String,pkg:String) {
        val info=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=title}
        val activity=ActivityInfo().apply{packageName=pkg;name="Main";nonLocalizedLabel=title;applicationInfo=info;exported=true}
        Shadows.shadowOf(app.packageManager).installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=info;activities=arrayOf(activity);versionName="1.0-fixture"})
        Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=activity})
    }
    private fun show(width:Int=940,fontScale:Float=1f) {
        install("Spotify","com.spotify.music");install("Gmail","com.google.android.gm");app.search.invalidateApps()
        ui.setContent { CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density,fontScale)) { Box(Modifier.width(width.dp)) { SpotlightPanel(app,
            AppConfig(theme="dark",preset="raycast",opacity=.70f,files=false,online=false,
                animations=false,learning=false,suggestions=false),{},"Spotify",availableHeightDp=viewport) } } }
        awaitApp("com.spotify.music")
        ui.onNodeWithTag("result:com.spotify.music/Main").performClick()
    }
    private fun awaitApp(pkg:String) {ui.waitUntil(15000){ui.onAllNodesWithTag("result:$pkg/Main").fetchSemanticsNodes().isNotEmpty()}}
    private fun capture(name:String)=ui.onNodeWithTag("spotlight-root").captureRoboImage(
        "build/raycast-window/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    private fun footerFits() {
        val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
        val footer=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
        ui.onNodeWithTag("context-footer").assertIsDisplayed()
        assertTrue(footer.bottom<=root.bottom);assertTrue(root.bottom-root.top<=viewport.dp)
    }
    @Test fun tabletInspectorHeaderDoesNotScrollAwayAndNewSelectionStartsAtTop() {
        show();capture("tablet-initial")
        val top=ui.onNodeWithTag("inspector-toolbar").getUnclippedBoundsInRoot().top
        ui.onNodeWithTag("context-inspector").performTouchInput{swipeUp()}
        ui.onNodeWithTag("inspector-toolbar").assertIsDisplayed()
        assertEquals(top,ui.onNodeWithTag("inspector-toolbar").getUnclippedBoundsInRoot().top)
        footerFits();capture("tablet-scrolled")
        search().performClick().performTextReplacement("Gmail");awaitApp("com.google.android.gm")
        ui.onNodeWithTag("result:com.google.android.gm/Main").performClick()
        ui.onNodeWithTag("inspector-title").assertTextEquals("Gmail").assertIsDisplayed()
        ui.onAllNodesWithText("Ver información de la app").assertCountEquals(1)
        ui.onNodeWithText("Información de la app").assertDoesNotExist()
        assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
    }
    @Test fun keyboardBudgetKeepsSearchHeaderAndFooterOutsideInspectorScroll() {
        show();ui.runOnIdle{viewport=350f}
        ui.onNodeWithTag("context-inspector").performTouchInput{swipeUp()}
        ui.onNodeWithTag("spotlight-bar").assertIsDisplayed()
        ui.onNodeWithTag("inspector-toolbar").assertIsDisplayed();footerFits()
        ui.runOnIdle{viewport=510f};footerFits()
    }
    @Test @Config(qualifiers="w390dp-h844dp") fun phonePreservesCloseAndSearchWithoutCreatingSideColumn() {
        viewport=740f;show(360)
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        ui.onNodeWithContentDescription("Cerrar inspector").assertIsDisplayed().performClick()
        search().assertTextEquals("Spotify").assertIsFocused();footerFits();capture("phone-closed")
        assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
    }
    @Test fun largeTextKeepsCompleteSelectionAndFixedInspectorHeader() {
        viewport=640f;show(width=1080,fontScale=1.3f)
        val row=ui.onNodeWithTag("result:com.spotify.music/Main").getUnclippedBoundsInRoot()
        val footer=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
        assertTrue(row.bottom<=footer.top)
        ui.onNodeWithTag("inspector-toolbar").assertIsDisplayed();footerFits();capture("tablet-large-text")
    }
}
