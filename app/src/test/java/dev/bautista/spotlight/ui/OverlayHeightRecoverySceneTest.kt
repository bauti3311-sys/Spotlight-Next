package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import android.graphics.Insets
import android.graphics.Rect
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.overlay.AdaptiveLayout
import dev.bautista.spotlight.overlay.OverlayViewport
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Real production Compose fed by the same WindowMetrics/frame calculation as the service. */
@OptIn(ExperimentalRoborazziApi::class, ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OverlayHeightRecoverySceneTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private var viewport by mutableStateOf(600f)
    private val config=AppConfig(files=false,online=false,animations=false,learning=false,preset="glass")
    private fun budget(ime:Boolean,portrait:Boolean=false):Float {
        val width=if(portrait)800 else 1280; val height=if(portrait)1280 else 800
        val insets=WindowInsets.Builder()
            .setInsetsIgnoringVisibility(WindowInsets.Type.systemBars(),Insets.of(0,24,0,48))
            .setInsets(WindowInsets.Type.ime(),Insets.of(0,0,0,if(ime)300 else 0))
            .setVisible(WindowInsets.Type.ime(),ime).build()
        val actual=app.getSystemService(WindowManager::class.java)
        val wm=object:WindowManager by actual {
            override fun getCurrentWindowMetrics()=WindowMetrics(Rect(0,0,width,height),insets)
        }
        val host=object:View(app) {
            override fun getWindowVisibleDisplayFrame(outRect:Rect){outRect.set(240,160,1040,500)}
        }
        val frame=OverlayViewport.read(wm,app.resources,48,host,0)
        return AdaptiveLayout.bounds(frame.width,frame.height,1f,frame.keyboardOcclusion(0),config).heightDp
    }
    private fun show(theme:String,longNames:Boolean=false) {
        val pm=Shadows.shadowOf(app.packageManager)
        repeat(12){i->
            val pkg="fixture.height${i.toString().padStart(2,'0')}"
            val application=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel="Altura ${i.toString().padStart(2,'0')}" + if(longNames) " · Documento de prueba con un nombre completo muy largo para revisar el inspector y el scroll" else "";enabled=true}
            val activity=ActivityInfo().apply{packageName=pkg;name="Main";nonLocalizedLabel=application.nonLocalizedLabel;applicationInfo=application;exported=true;enabled=true}
            pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=application;activities=arrayOf(activity);versionName="1.0-fixture"})
            pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=activity})
        }
        app.search.invalidateApps()
        viewport=budget(false)
        ui.setContent {Box(Modifier.width(960.dp)){SpotlightPanel(app,config.copy(theme=theme),{},"Altura",viewport)}}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:fixture.height00/Main").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("result:fixture.height00/Main").performClick()
        ui.waitUntil(15000){ui.onAllNodesWithText("1.0-fixture").fetchSemanticsNodes().isNotEmpty()}
    }
    private fun capture(name:String) {
        val phase=System.getenv("SPOTLIGHT_WINDOW_CAPTURE_PHASE") ?: "after"
        ui.onNodeWithTag("spotlight-root").captureRoboImage("build/window-recovery/$phase/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    private fun fits() {
        val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
        val footer=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
        assertTrue(footer.bottom<=root.bottom)
        ui.onNodeWithTag("context-footer").assertIsDisplayed()
        val inspector=ui.onNodeWithTag("context-inspector").getUnclippedBoundsInRoot()
        assertTrue(inspector.bottom<=footer.top)
    }
    private fun recovery(theme:String) {
        show(theme);capture("tablet-$theme-closed")
        // The capture precedes assertions so the first red run preserves the defect.
        assertTrue("Closed keyboard budget compressed to $viewport",viewport>500f)
        fits()
        ui.runOnIdle{viewport=budget(true)};ui.waitForIdle();fits();capture("tablet-$theme-ime")
        ui.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Altura 11"))
        ui.onNodeWithText("Altura 11").assertIsDisplayed()
        ui.runOnIdle{viewport=budget(false)};ui.waitForIdle();fits();capture("tablet-$theme-restored")
        assertTrue(viewport>500f)
        assertTrue("Portrait parent retains its independent full-height budget",budget(false,portrait=true)>900f)
        assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
    }
    @Test fun longContentKeepsLastResultAndInspectorMetadataReachable() {
        show("dark",longNames=true)
        ui.runOnIdle{viewport=budget(true)};ui.waitForIdle();fits()
        ui.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Altura 11",substring=true))
        ui.onNodeWithText("Altura 11",substring=true).assertIsDisplayed()
        ui.onNodeWithTag("inspector-scroll").performTouchInput{swipeUp()}
        ui.onNodeWithText("fixture.height00").assertIsDisplayed()
        capture("tablet-dark-long-ime")
        ui.runOnIdle{viewport=budget(false)};ui.waitForIdle();fits()
        capture("tablet-dark-long-restored")
        assertNull(Shadows.shadowOf(ui.activity).nextStartedActivity)
    }
    @Test fun darkWindowRestoresAfterImeAndRotation()=recovery("dark")
    @Test fun lightWindowRestoresAfterImeAndRotation()=recovery("light")
}
