package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class C1ChromeContractTest {
    @get:Rule val ui=createComposeRule()
    @Test fun quietTouchDockKeepsTargetsHeightAndIndependentActions() {
        var opens=0;var actions=0
        ui.setContent { SpotlightTheme("light",AppConfig(theme="light")) { Box(Modifier.width(320.dp)) {
            GlassContextFooter(false,true,"Abrir aplicación",{opens++},{actions++})
        } } }
        ui.onNodeWithTag("context-footer").assertHeightIsEqualTo(52.dp)
        ui.onNodeWithTag("command-primary").assertHeightIsAtLeast(48.dp).performClick()
        ui.onNodeWithTag("command-actions").assertHeightIsAtLeast(48.dp).performClick()
        ui.runOnIdle{assertEquals(1,opens);assertEquals(1,actions)}
    }
    @Test fun openActionMenuCannotExecuteTheStalePrimaryFromDock() {
        var opens=0;var backs=0
        ui.setContent { SpotlightTheme("dark") { Box(Modifier.width(360.dp)) {
            GlassContextFooter(false,true,"Abrir",{opens++},{backs++},actionsOpen=true)
        } } }
        ui.onNodeWithTag("command-primary").assertIsNotEnabled()
        ui.onNodeWithTag("command-actions").assertTextContains("Volver").performClick()
        ui.runOnIdle{assertEquals(0,opens);assertEquals(1,backs)}
    }
    @Test fun restrainedResultFocusStillContrastsInBothThemes() {
        assertTrue(InspectorAccent.contrast(GlassTokens.resultFocusColor(false),GlassTokens.paper)>=3f)
        assertTrue(InspectorAccent.contrast(GlassTokens.resultFocusColor(true),GlassTokens.graphite)>=3f)
        assertEquals(1.dp,GlassTokens.resultFocusStroke)
        assertEquals(2.dp,GlassTokens.focusStroke) // ActionPanel remains independently accessible.
    }
}
