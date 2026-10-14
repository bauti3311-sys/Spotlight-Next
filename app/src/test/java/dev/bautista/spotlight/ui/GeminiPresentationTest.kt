package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.*
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GeminiPresentationTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun tabletRowUsesReferenceDensitiesAndInlineSubtitleWithoutSelectionJump() {
        var density by mutableStateOf("balanced")
        var selected by mutableStateOf(false)
        val item=Result("gemini-row","Calculadora","Resultado local","Calculadora",0,Action.Copy("4"))
        ui.setContent {SpotlightTheme("dark",AppConfig(density=density)) {
            Box(Modifier.width(660.dp).testTag("row")) {ResultRow(item,selected,false,{},{},{},animations=false)}
        }}
        for((name,height) in listOf("compact" to 36.dp,"balanced" to 42.dp,"spacious" to 48.dp)) {
            ui.runOnIdle {density=name}
            ui.onNodeWithTag("row").assertHeightIsEqualTo(height)
            val title=ui.onAllNodesWithText("Calculadora",useUnmergedTree=true).fetchSemanticsNodes().first()
            val subtitle=ui.onNodeWithText("Resultado local",useUnmergedTree=true).fetchSemanticsNode()
            Assert.assertEquals(title.boundsInRoot.center.y,subtitle.boundsInRoot.center.y,2f)
            ui.runOnIdle {selected=!selected}
            ui.onNodeWithTag("row").assertHeightIsEqualTo(height)
        }
    }
    @Test fun narrowOverlayKeepsSubtitleReadableAndDoesNotAllocateTabletMetadata() {
        val item=Result("narrow-row","Documento","Detalle legible","Archivos",0,Action.Copy("document"))
        ui.setContent {SpotlightTheme("light") {Box(Modifier.width(360.dp).testTag("row")) {
            ResultRow(item,true,false,{},{},{},animations=false)
        }}}
        ui.onNodeWithTag("row").assertHeightIsAtLeast(56.dp)
        ui.onNodeWithText("Detalle legible").assertIsDisplayed()
        ui.onNodeWithText("Archivos").assertDoesNotExist()
    }
    @Test fun referenceSearchBarIs56DpAndAllThreeHeaderActionsRemainAvailable() {
        var clicked=""
        ui.setContent {SpotlightTheme("dark") {Box(Modifier.width(660.dp)) {
            SpotlightBar("",{},"Todo",{},{},{},remember {FocusRequester()},.96f,false,
                animations=false,onSaved={clicked="saved"},onDictate={clicked="voice"},onSettings={clicked="settings"})
        }}}
        ui.onNodeWithTag("spotlight-bar").assertHeightIsEqualTo(56.dp)
        for((label,expected) in listOf("Abrir Guardados" to "saved","Dictar consulta" to "voice","Abrir Ajustes" to "settings")) {
            ui.onNodeWithContentDescription(label).assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp).performClick()
            ui.runOnIdle {Assert.assertEquals(expected,clicked)}
        }
        ui.onNode(hasSetTextAction()).assertIsDisplayed().performTextInput("2+2")
    }
}
