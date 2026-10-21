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
        (listOf("Chrome")+(1..5).map{"Chrome $it"}).forEachIndexed{n,label->
            val info=ActivityInfo().apply{packageName="fixture.chrome$n";name="Main";nonLocalizedLabel=label;applicationInfo=ApplicationInfo().apply{packageName="fixture.chrome$n"}}
            Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(launcher,ResolveInfo().apply{activityInfo=info})
        }
        app.search.invalidateApps()
        ui.setContent{Box(Modifier.width(760.dp)){SpotlightPanel(app,AppConfig(theme=theme,preset=preset,files=false,animations=false),{},query,availableHeightDp=640f)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:fixture.chrome0/Main").fetchSemanticsNodes().isNotEmpty()}
    }
    private fun rows()=ui.onAllNodes(SemanticsMatcher("result row"){it.config.getOrElse(SemanticsProperties.TestTag){""}.startsWith("result:")})
    @Test fun compactExpansionAndCollapseKeepQueryFocusAndSelectedIdentity(){
        show()
        rows().assertCountEquals(3)
        ui.onNode(hasSetTextAction()).assertTextEquals("Chrome").assertIsFocused()
        ui.onNodeWithTag("results-expand").performClick()
        ui.waitUntil(15000){rows().fetchSemanticsNodes().size>=6}
        ui.onNode(hasSetTextAction()).assertTextEquals("Chrome").assertIsFocused()
        repeat(4){ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.DirectionDown)}}
        val selected=rows().fetchSemanticsNodes().single{it.config.getOrElse(SemanticsProperties.Selected){false}}.config[SemanticsProperties.TestTag]
        ui.onNode(hasSetTextAction()).performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.M);keyUp(Key.CtrlLeft)}
        rows().assertCountEquals(3)
        assertEquals(selected,rows().fetchSemanticsNodes().single{it.config.getOrElse(SemanticsProperties.Selected){false}}.config[SemanticsProperties.TestTag])
        ui.onNode(hasSetTextAction()).assertIsFocused().assertTextEquals("Chrome")
    }
    @Test fun rightArrowAtTextBoundaryNeverFocusesMicrophoneAndTabCompletes(){
        show("Chro")
        ui.onNode(hasSetTextAction()).performClick().performKeyInput{repeat(12){pressKey(Key.DirectionRight)}}
        ui.onNode(hasSetTextAction()).assertIsFocused().assertTextEquals("Chro")
        ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.Tab)}
        ui.onNode(hasSetTextAction()).assertIsFocused().assertTextEquals("Chrome")
        ui.onAllNodesWithText("Menú de acciones").assertCountEquals(0)
    }
    @Test fun tabWithCaretInsideQueryKeepsNativeTextAndOpensActions(){
        show("Chro")
        ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.MoveHome);pressKey(Key.Tab)}
        ui.onNode(hasSetTextAction()).assertTextEquals("Chro")
        ui.onNode(hasText("Abrir") and isFocused()).assertExists()
        ui.onNode(hasText("Abrir") and isFocused()).performKeyInput{pressKey(Key.Escape)}
        ui.onNode(hasSetTextAction()).assertTextEquals("Chro").assertIsFocused()
    }
    @Test fun escapeCollapsesExpandedResultsBeforeClosingSearch(){
        show()
        ui.onNode(hasSetTextAction()).performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.M);keyUp(Key.CtrlLeft)}
        ui.waitUntil(15000){rows().fetchSemanticsNodes().size>=6}
        ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.Escape)}
        rows().assertCountEquals(3)
        ui.onNode(hasSetTextAction()).assertTextEquals("Chrome").assertIsFocused()
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
        assertTrue(root.fetchSemanticsNode().boundsInRoot.height<340*app.resources.displayMetrics.density)
        rows().assertCountEquals(3)
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
