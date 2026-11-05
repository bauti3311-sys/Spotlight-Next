package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
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

/** Controlled Compose evidence inspired by physical62; not a OneUI/IME emulator. */
@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TabletPhysicalRegressionTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private var viewport by mutableStateOf(600f)
    private var closes=0
    private val field get()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
    private fun show(theme:String="dark") {
        val pm=Shadows.shadowOf(app.packageManager)
        repeat(16){i->
            val pkg="fixture.tablet${i.toString().padStart(2,'0')}"
            val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel="Tablet ${i.toString().padStart(2,'0')}";enabled=true}
            val activity=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=a;nonLocalizedLabel=a.nonLocalizedLabel;enabled=true;exported=true}
            pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity);versionName="1.0-fixture"})
            pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=activity})
        }
        app.search.invalidateApps()
        ui.setContent{SpotlightTheme(theme){Surface{Box(Modifier.width(1050.dp)){
            SpotlightPanel(app,AppConfig(theme=theme,files=false,online=false,contacts=false,animations=false,learning=false,compactSearchResults=true,preset="glass"),{closes++},"Tablet",viewport)
        }}}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:fixture.tablet00/Main").fetchSemanticsNodes().isNotEmpty()}
    }
    private fun capture(name:String) {
        val phase=System.getenv("SPOTLIGHT_TABLET_PHASE") ?: "after"
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/tablet-physical/$phase/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    private fun fits() {
        val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
        val footer=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
        assertTrue("footer $footer outside $root",footer.bottom<=root.bottom)
        ui.onNodeWithTag("context-footer").assertIsDisplayed()
        assertTrue(root.bottom-root.top<=viewport.dp)
    }
    private fun inspector(theme:String) {
        show(theme)
        ui.onNodeWithTag("result:fixture.tablet00/Main").performClick()
        ui.waitUntil(15000){ui.onAllNodesWithText("1.0-fixture").fetchSemanticsNodes().isNotEmpty()}
        capture("$theme-closed")
        val closed=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
        ui.runOnIdle{viewport=300f};ui.waitForIdle();capture("$theme-ime")
        fits()
        val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
        assertTrue("Unused independent viewport: $root / $viewport",root.bottom-root.top>=296.dp)
        val inspector=ui.onNodeWithTag("context-inspector").getUnclippedBoundsInRoot()
        val bar=ui.onNodeWithTag("spotlight-bar").getUnclippedBoundsInRoot()
        val footer=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
        // Root padding24 + measured search56 + measured footer52 + divider3.
        val remaining=viewport.dp-24.dp-(bar.bottom-bar.top)-(footer.bottom-footer.top)-3.dp
        assertEquals("Inspector uses all remaining space",remaining,inspector.bottom-inspector.top)
        ui.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Tablet 05"))
        ui.onNodeWithText("Tablet 05").assertIsDisplayed()
        ui.runOnIdle{viewport=600f};ui.waitForIdle();capture("$theme-restored");fits()
        assertEquals(closed.bottom-closed.top,ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot().let{it.bottom-it.top})
        ui.onNodeWithText("fixture.tablet00").assertExists()
        assertEquals(0,closes);assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun darkInspectorUsesActualChromeAndRestoresAfterKeyboard()=inspector("dark")
    @Test fun lightInspectorUsesActualChromeAndRestoresAfterKeyboard()=inspector("light")
    @Test fun apkFileInspectorKeepsOriginalSelectionAndFooterAcrossKeyboardBudgets() {
        kotlinx.coroutines.runBlocking {
            repeat(8){i->app.index.storeTranscript("content://fixture-tablet/apk$i",
                if(i==0)"Spotlight-Next-Preview61-11b647c.apk" else "Spotlight-Next-Preview$i.apk",
                "application/vnd.android.package-archive","Archivo de prueba autorizado")}
        }
        ui.setContent{SpotlightTheme("dark"){Surface{Box(Modifier.width(1050.dp)){
            SpotlightPanel(app,AppConfig(files=true,online=false,contacts=false,animations=false,learning=false,compactSearchResults=true,preset="glass"),{closes++},"Preview",viewport)
        }}}}
        val title="Spotlight-Next-Preview61-11b647c.apk"
        ui.waitUntil(15000){ui.onAllNodesWithText("Spotlight-Next-Preview1.apk").fetchSemanticsNodes().isNotEmpty()}
        // Compact search deliberately offers six rows; expose all eight fixtures.
        ui.onNodeWithTag("results-expand").performClick()
        ui.waitUntil(15000){ui.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithText(title).performClick()
        ui.onNodeWithTag("context-inspector").assertExists()
        ui.runOnIdle{viewport=300f};ui.waitForIdle();fits();capture("apk-ime")
        // Header and metadata both show the name; verify the visible header.
        ui.onAllNodes(hasText(title) and hasAnyAncestor(hasTestTag("context-inspector"))).onFirst().assertIsDisplayed()
        ui.runOnIdle{viewport=600f};ui.waitForIdle();fits();capture("apk-restored")
        ui.onNodeWithTag("context-inspector").assertExists()
        ui.onAllNodes(hasText(title) and hasAnyAncestor(hasTestTag("context-inspector"))).onFirst().assertIsDisplayed()
        assertEquals(0,closes);assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    private fun menu(key:Key?) {
        show();ui.onNodeWithTag("result:fixture.tablet00/Main").performClick()
        ui.onNodeWithContentDescription("Opciones de Spotlight").performClick();capture("menu-open")
        ui.onNodeWithText("Volver a resultados").assertIsDisplayed()
        if(key==null) {
            ui.runOnIdle{viewport=220f};ui.waitForIdle();capture("menu-short")
            ui.onNodeWithTag("spotlight-menu-return").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
            ui.onNodeWithText("Volver a resultados").performClick()
        }
        else ui.onNodeWithTag("spotlight-root").performKeyInput{pressKey(key)}
        field.assertTextEquals("Tablet").assertIsFocused()
        ui.onNodeWithText("Apariencia").assertDoesNotExist()
        // At220dp Adaptive correctly stacks the inspector. Widen the height
        // again before checking the side-by-side representation, not its state.
        ui.runOnIdle{viewport=600f};ui.waitForIdle()
        ui.onNodeWithTag("context-inspector").assertExists()
        assertEquals(0,closes);capture("menu-return")
    }
    @Test fun visibleMenuReturnPreservesQueryAndInspector()=menu(null)
    @Test fun escapeClosesMenuBeforeInspectorOrOverlay()=menu(Key.Escape)
    @Test fun androidBackClosesMenuBeforeInspectorOrOverlay()=menu(Key.Back)
}
