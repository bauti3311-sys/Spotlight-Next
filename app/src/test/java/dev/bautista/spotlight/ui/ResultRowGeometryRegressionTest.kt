package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.search.*
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ResultRowGeometryRegressionTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun selectionKeepsRowAndUsefulTextGeometryStable() {
        var selected by mutableStateOf(true)
        var density by mutableStateOf("balanced")
        val a=Result("row-a","Primera","Descripción","Calculadora",0,Action.Copy("4"))
        val b=a.copy(id="row-b",title="Segunda")
        ui.setContent {SpotlightTheme("dark",AppConfig(density=density)) {Column(Modifier.width(720.dp)) {
            Box(Modifier.testTag("first-row")){ResultRow(a,selected,false,{},{},{},animations=false)}
            Box(Modifier.testTag("second-row")){ResultRow(b,!selected,false,{},{},{},animations=false)}
        }}}
        for(value in listOf("compact","balanced","spacious")) {
            ui.runOnIdle{density=value}
            val first=ui.onNodeWithTag("first-row").fetchSemanticsNode().boundsInRoot
            val second=ui.onNodeWithTag("second-row").fetchSemanticsNode().boundsInRoot
            Assert.assertEquals("Selection must not change row height in $value",first.height,second.height,.01f)
            ui.onNodeWithText("Primera").assertIsDisplayed()
            ui.onNodeWithText("Segunda").assertIsDisplayed()
            ui.onAllNodesWithText("Calculadora",useUnmergedTree=true).assertCountEquals(0)
            val metadata=ui.onAllNodesWithText("Descripción",useUnmergedTree=true).fetchSemanticsNodes()
            Assert.assertEquals(2,metadata.size)
            // Secondary text flows after each title; the removed category column is no
            // longer the alignment anchor. App/title anchors and selection stay fixed.
            val titleA=ui.onNodeWithText("Primera",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot
            val titleB=ui.onNodeWithText("Segunda",useUnmergedTree=true).fetchSemanticsNode().boundsInRoot
            Assert.assertEquals("Title anchors align in $value",titleA.left,titleB.left,.01f)
            Assert.assertTrue(metadata[0].boundsInRoot.left>titleA.right)
            Assert.assertTrue(metadata[1].boundsInRoot.left>titleB.right)
            Assert.assertTrue(metadata[0].boundsInRoot.right<=first.right)
            Assert.assertTrue(metadata[1].boundsInRoot.right<=second.right)
            val subtitleLeft=metadata[0].boundsInRoot.left
            ui.runOnIdle{selected=!selected}
            Assert.assertEquals("Selection must not shift text in $value",subtitleLeft,
                ui.onAllNodesWithText("Descripción",useUnmergedTree=true).fetchSemanticsNodes()[0].boundsInRoot.left,.01f)
            Assert.assertEquals(first.height,ui.onNodeWithTag("first-row").fetchSemanticsNode().boundsInRoot.height,.01f)
        }
    }
}
