package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
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

/** Native production UI with explicit package-manager fixtures; never a physical-device claim. */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], qualifiers="w1280dp-h800dp", application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OverlayDesignReviewTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun show(width:Int=960,height:Float=640f,query:String="Chrome",theme:String="dark",preset:String="glass") {
        val launcher=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        (0..4).forEach { n ->
            val pkg="fixture.chrome$n"
            val info=ActivityInfo().apply {
                packageName=pkg;name="Main";nonLocalizedLabel=if(n==0)"Chrome" else "Chrome $n"
                applicationInfo=ApplicationInfo().apply { packageName=pkg }
            }
            Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(launcher,ResolveInfo().apply{activityInfo=info})
        }
        app.search.invalidateApps()
        ui.setContent { Box(Modifier.width(width.dp)) {
            SpotlightPanel(app,AppConfig(files=false,animations=false,theme=theme,preset=preset,
                favorites=setOf("fixture.chrome0/Main"),learning=false),{},query,height)
        } }
        ui.waitUntil(15000) { ui.onAllNodesWithTag(if(query.isBlank())"result:suggest:app:fixture.chrome0" else "result:fixture.chrome0/Main").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun capture(name:String) {
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/design-review/$name.png",
            roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    @Test fun tabletSelectionUpdatesInspectorWithoutLaunchingAnotherApp() {
        show()
        ui.onNodeWithTag("results-pane").assertWidthIsAtLeast(400.dp)
        ui.onNodeWithTag("context-inspector").assertWidthIsAtLeast(280.dp)
        ui.onNodeWithTag("result:fixture.chrome2/Main").assertIsDisplayed().assertHeightIsEqualTo(48.dp)
        ui.onNodeWithTag("result:fixture.chrome1/Main").assertIsDisplayed().performClick()
        ui.onNodeWithTag("inspector-title").assertTextEquals("Chrome 1")
        ui.onNodeWithTag("inspector-primary-action").assertIsDisplayed().assertHasClickAction()
        assertNull(Shadows.shadowOf(app).peekNextStartedActivity())
        capture("tablet-selected-second")
    }
    @Test fun dividerCannotCompressEitherPaneBelowItsFunctionalMinimum() {
        show()
        val divider=ui.onNodeWithContentDescription("Divisor ajustable del panel contextual")
        divider.performTouchInput { swipe(center,center.copy(x=center.x+500f),300) }
        ui.onNodeWithTag("results-pane").assertWidthIsAtLeast(400.dp)
        ui.onNodeWithTag("context-inspector").assertWidthIsAtLeast(280.dp)
        divider.performTouchInput { swipe(center,center.copy(x=center.x-500f),300) }
        ui.onNodeWithTag("results-pane").assertWidthIsAtLeast(400.dp)
        ui.onNodeWithTag("context-inspector").assertWidthIsAtLeast(280.dp)
    }
    @Test fun intermediateWidthOffersActionsWithoutAnEmptyInspectorColumn() {
        show(width=640)
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        ui.onNodeWithTag("result:fixture.chrome0/Main").performClick()
        ui.onNodeWithContentDescription("Cerrar acciones").assertExists()
        capture("intermediate-contextual-actions")
    }
    @Test fun reducedImeViewportKeepsSearchVisibleAndRemovesAutomaticInspector() {
        show(height=220f)
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        ui.onNode(hasSetTextAction()).assertIsDisplayed().assertIsFocused()
        assertTrue(ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot.height<=220f*app.resources.displayMetrics.density)
        capture("tablet-ime-budget-simulation")
    }
    @Test fun homeShowsOneAuthorizedFavoriteApplicationWithInspector() {
        show(query="")
        ui.onNodeWithTag("inspector-title").assertTextEquals("Chrome")
        capture("tablet-home-favorite")
    }
    @Test fun lightThemeRetainsTheSameInspectorAndActions() { show(theme="light",preset="macos");capture("tablet-light") }
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneHasOneColumnAndTouchActions() {
        show(width=360)
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        ui.onNodeWithTag("result:fixture.chrome0/Main").performClick()
        ui.onNodeWithContentDescription("Cerrar acciones").assertExists()
        capture("phone-contextual-actions")
    }
    @Test fun permissionsRemainRealAndReachableInTheUnifiedCenter() {
        ui.setContent { SpotlightTheme("dark") { Column(Modifier.width(900.dp).height(600.dp).verticalScroll(rememberScrollState())) {
            SettingsAccessSummary(app,AppConfig(),0,{})
        } } }
        ui.onNodeWithText("Ventana flotante").assertExists()
        ui.onNodeWithText("Shizuku").performScrollTo().assertIsDisplayed()
        ui.onRoot().captureRoboImage("build/design-review/permissions-center.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
}
