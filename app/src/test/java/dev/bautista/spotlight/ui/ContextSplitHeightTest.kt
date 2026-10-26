package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import org.junit.Assert.assertTrue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h1100dp",application=SpotlightApp::class)
class ContextSplitHeightTest {
    @get:Rule val ui=createComposeRule()
    @Test fun shortInspectorDoesNotReserveUnusedHeight() {
        ui.setContent { Box(Modifier.width(800.dp)) { ContextSplitPanel(700.dp,
            { Text("Result") },{ Text("Small inspector") },scrollInspector=true) } }
        assertTrue(measuredHeight(ui.onNodeWithTag("context-inspector").getUnclippedBoundsInRoot()) <= 100.dp)
        assertTrue(measuredHeight(ui.onNodeWithTag("results-pane").getUnclippedBoundsInRoot()) <= 100.dp)
        assertTrue(measuredHeight(ui.onNodeWithContentDescription("Divisor ajustable del panel contextual").getUnclippedBoundsInRoot()) <= 100.dp)
    }
    @Test fun longInspectorUsesAvailableHeightAndOnlyScrollsWhenNecessary() {
        var budget by mutableStateOf(700.dp)
        ui.setContent { Box(Modifier.width(800.dp)) { ContextSplitPanel(budget,
            { Text("Result") },{ Column { repeat(10) { Text("Inspector row $it",Modifier.height(48.dp)) } } },scrollInspector=true) } }
        ui.onNodeWithText("Inspector row 9").assertIsDisplayed()
        ui.onNodeWithTag("context-inspector").assertHeightIsAtLeast(480.dp)
        assertTrue(measuredHeight(ui.onNodeWithTag("context-inspector").getUnclippedBoundsInRoot()) <= 500.dp)
        ui.runOnIdle { budget=220.dp }
        assertTrue(measuredHeight(ui.onNodeWithTag("context-inspector").getUnclippedBoundsInRoot()) <= 220.dp)
        ui.onNodeWithText("Inspector row 9").performScrollTo().assertIsDisplayed()
    }

    private fun measuredHeight(bounds: androidx.compose.ui.unit.DpRect) =
        bounds.bottom - bounds.top
}
