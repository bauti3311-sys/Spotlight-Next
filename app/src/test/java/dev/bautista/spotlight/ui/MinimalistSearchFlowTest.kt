package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
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
import java.io.File

/** Production package manager -> SearchEngine -> panel. Installed apps below are explicit fixtures. */
@OptIn(ExperimentalTestApi::class, com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MinimalistSearchFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun show(query:String="Chrome", theme:String="dark", preset:String="glass") {
        val launcher=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        (listOf("Chrome")+(1..6).map{"Chrome $it"}+"Firefox").forEachIndexed{n,label->
            val info=ActivityInfo().apply{packageName="fixture.chrome$n";name="Main";nonLocalizedLabel=label;applicationInfo=ApplicationInfo().apply{packageName="fixture.chrome$n"}}
            Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(launcher,ResolveInfo().apply{activityInfo=info})
        }
        app.search.invalidateApps()
        ui.setContent{Box(Modifier.width(760.dp)){SpotlightPanel(app,AppConfig(theme=theme,preset=preset,files=false,animations=false),{},query,availableHeightDp=640f)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:fixture.chrome0/Main").fetchSemanticsNodes().isNotEmpty()}
    }
    private fun rows()=ui.onAllNodes(SemanticsMatcher("result row"){it.config.getOrElse(SemanticsProperties.TestTag){""}.startsWith("result:")})
    private fun search()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
    @Test fun compactExpansionAndCollapseKeepQueryFocusAndSelectedIdentity(){
        show()
        rows().assertCountEquals(6)
        search().assertTextEquals("Chrome").assertIsFocused()
        ui.onNodeWithTag("results-expand").performClick()
        ui.waitUntil(15000){rows().fetchSemanticsNodes().size>=6}
        search().assertTextEquals("Chrome").assertIsFocused()
        repeat(6){search().performKeyInput{pressKey(Key.DirectionDown)}}
        val selected=rows().fetchSemanticsNodes().single{it.config.getOrElse(SemanticsProperties.Selected){false}}.config[SemanticsProperties.TestTag]
        search().performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.M);keyUp(Key.CtrlLeft)}
        rows().assertCountEquals(6)
        assertEquals(selected,rows().fetchSemanticsNodes().single{it.config.getOrElse(SemanticsProperties.Selected){false}}.config[SemanticsProperties.TestTag])
        search().assertIsFocused().assertTextEquals("Chrome")
    }
    @Test fun rightArrowAtTextBoundaryNeverFocusesMicrophoneAndTabCompletes(){
        show("Chro")
        search().performClick().performKeyInput{repeat(12){pressKey(Key.DirectionRight)}}
        search().assertIsFocused().assertTextEquals("Chro")
        search().performKeyInput{pressKey(Key.Tab)}
        // Chro matches six installed apps: Tab opens actions instead of choosing a completion.
        search().assertTextEquals("Chro")
        ui.onNodeWithTag("action-surface").assertIsDisplayed()
        ui.onNodeWithTag("action-filter").performKeyInput{pressKey(Key.Escape)}
        search().assertIsFocused().assertTextEquals("Chro")
        search().performTextClearance()
        search().performTextInput("Fire")
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:fixture.chrome7/Main").fetchSemanticsNodes().isNotEmpty()}
        search().performKeyInput{repeat(12){pressKey(Key.DirectionRight)}}
        search().assertIsFocused().assertTextEquals("Fire")
        search().performKeyInput{pressKey(Key.Tab)}
        search().assertIsFocused().assertTextEquals("Firefox")
        ui.onNodeWithTag("action-surface").assertDoesNotExist()
    }
    @Test fun tabWithCaretInsideQueryKeepsNativeTextAndOpensActions(){
        show("Chro")
        search().performKeyInput{pressKey(Key.MoveHome);pressKey(Key.Tab)}
        search().assertTextEquals("Chro")
        ui.onNode(hasText("Abrir") and isFocused()).assertExists()
        ui.onNode(hasText("Abrir") and isFocused()).performKeyInput{pressKey(Key.Escape)}
        search().assertTextEquals("Chro").assertIsFocused()
    }
    @Test fun escapeCollapsesExpandedResultsBeforeClosingSearch(){
        show()
        search().performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.M);keyUp(Key.CtrlLeft)}
        ui.waitUntil(15000){rows().fetchSemanticsNodes().size>=6}
        search().performKeyInput{pressKey(Key.Escape)}
        rows().assertCountEquals(6)
        search().assertTextEquals("Chrome").assertIsFocused()
    }
    @Test fun tabletGraphiteCapture(){
        show(preset="graphite")
        File("build/minimalist-search").mkdirs()
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/minimalist-search/tablet-graphite.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    @Test fun tabletLightCapture(){
        show(theme="light",preset="macos")
        File("build/minimalist-search").mkdirs()
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/minimalist-search/tablet-light.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    @Test fun tabletCaptureAndCompactHeight(){
        show()
        val root=ui.onNodeWithTag("spotlight-root")
        assertTrue("Six rows and chrome must fit the available tablet viewport",root.fetchSemanticsNode().boundsInRoot.height<480*app.resources.displayMetrics.density)
        (0..5).forEach{ui.onNodeWithTag("result:fixture.chrome$it/Main").assertIsDisplayed().assertHeightIsAtLeast(48.dp)}
        rows().assertCountEquals(6)
        ui.onNodeWithTag("context-inspector").assertDoesNotExist()
        ui.onNodeWithTag("result:fixture.chrome0/Main").performTouchInput{longClick()}
        ui.onNodeWithTag("action-surface").assertIsDisplayed()
        ui.onNodeWithContentDescription("Cerrar acciones").performClick()
        val file=File("build/minimalist-search/tablet-compact.png");file.parentFile?.mkdirs()
        root.captureRoboImage(file.absolutePath,roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
        ui.onNodeWithTag("results-expand").performClick()
        ui.waitUntil(15000){rows().fetchSemanticsNodes().size>=6}
        root.captureRoboImage("build/minimalist-search/tablet-expanded.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
}
