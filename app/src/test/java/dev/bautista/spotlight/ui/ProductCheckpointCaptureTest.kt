package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProductCheckpointCaptureTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private fun capture(name:String,width:Int,theme:String,query:String,actions:Boolean=false) {
        val app=RuntimeEnvironment.getApplication() as SpotlightApp
        lateinit var manager:androidx.compose.ui.focus.FocusManager
        ui.setContent { manager=LocalFocusManager.current;Box(Modifier.width(width.dp)) {
            SpotlightPanel(app,AppConfig(files=false,online=false,animations=false,learning=false,suggestions=false,theme=theme),{},initialQuery=query)
        } }
        ui.onNode(hasSetTextAction()).assertIsFocused()
        if(query.isNotBlank()) ui.waitUntil(15000) { ui.onAllNodesWithText("4").fetchSemanticsNodes().isNotEmpty() }
        if(actions) {
            ui.onNode(hasSetTextAction()).performKeyInput { pressKey(Key.Tab) }
            ui.waitUntil(15000) { ui.onAllNodesWithText("Copiar resultado").fetchSemanticsNodes().isNotEmpty() }
            ui.onNodeWithText("Copiar resultado").assertIsFocused()
        }
        ui.runOnIdle { manager.clearFocus(force=true) }
        ui.waitForIdle()
        ui.onRoot().captureRoboImage("build/product-captures/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    @Test fun tabletInitialDark()=capture("tablet-initial-dark",720,"dark","")
    @Test fun tabletResultsDark()=capture("tablet-results-dark",720,"dark","2+2")
    @Test fun tabletInspectorLight()=capture("tablet-inspector-light",720,"light","2+2",true)
    @Test @Config(qualifiers="w800dp-h1280dp") fun tabletPortraitLight()=capture("tablet-portrait-light",640,"light","2+2")
}
