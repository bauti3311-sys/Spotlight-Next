package dev.bautista.spotlight.ui

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** New approved UI contract; legacy geometry/storage remain covered by policy/persistence tests. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OverlayWidthSettingsTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private fun show(content:@Composable ()->Unit) {
        ui.setContent{SpotlightTheme("dark"){Surface(Modifier.fillMaxWidth()){
            Column(Modifier.widthIn(max=680.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp)){content()}
        }}}
    }
    @Test fun appearanceHasOneWidthControlAndNoTechnicalOverrides() {
        show{GlassAppearance(AppConfig(width=0,landscapeWidth=380,animations=false),{})}
        ui.onNodeWithTag("window-width-slider").performScrollTo().assertIsDisplayed()
        ui.onAllNodesWithTag("window-width-slider").assertCountEquals(1)
        ui.onNodeWithContentDescription("Ancho general automático").assertDoesNotExist()
        ui.onNodeWithContentDescription("Ancho horizontal específico").assertDoesNotExist()
        ui.onNodeWithText("Altura máxima",substring=true).assertDoesNotExist()
    }
    @Test fun resettingPositionMustNotEraseHorizontalWidth() {
        var config by mutableStateOf(AppConfig(width=820,landscapeWidth=460,landscapePosition=.25f,animations=false))
        show{InvocationSettings(config){config=it}}
        ui.onNodeWithText("Usar la posición general en ambas orientaciones").performScrollTo().performClick()
        ui.runOnIdle{assertEquals(460,config.landscapeWidth);assertEquals(-1f,config.landscapePosition);assertEquals(820,config.width)}
        ui.onNodeWithTag("window-width-slider").assertDoesNotExist()
    }
    @Test fun newWidthChoicePreservesPositionSavedDataAndLegacyValues() {
        var config by mutableStateOf(AppConfig(width=0,landscapeWidth=380,landscapePosition=.25f,pins=setOf("pdf de ayer"),animations=false))
        show{WindowSizeSettings(config){config=it}}
        ui.onNodeWithTag("window-width-slider").performSemanticsAction(SemanticsActions.SetProgress){it(.75f)}
        ui.runOnIdle{assertEquals(.75f,config.windowScale);assertEquals(0,config.width);assertEquals(380,config.landscapeWidth);assertEquals(.25f,config.landscapePosition);assertEquals(setOf("pdf de ayer"),config.pins)}
    }
    @Test fun appearanceUpdatesImmediatelyAndUndoPreservesLegacyGeometry() {
        var config by mutableStateOf(AppConfig(width=820,landscapeWidth=460,animations=false))
        show{GlassAppearance(config){config=it}}
        ui.onNodeWithTag("window-width-slider").performScrollTo().performSemanticsAction(SemanticsActions.SetProgress){it(.6f)}
        ui.onNodeWithText("Aplicar apariencia").assertDoesNotExist()
        ui.runOnIdle{assertEquals(.6f,config.windowScale);assertEquals(820,config.width);assertEquals(820,config.manualWidth);assertEquals(460,config.landscapeWidth)}
        ui.onNodeWithText("Deshacer cambios de esta visita").performScrollTo().performClick()
        ui.runOnIdle{assertEquals(-1f,config.windowScale);assertEquals(820,config.width);assertEquals(460,config.landscapeWidth)}
    }
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneExposesOneAccessibleCompactToWideSlider() {
        show{WindowSizeSettings(AppConfig(width=0,landscapeWidth=380),{})}
        ui.onNodeWithContentDescription("Ancho de la ventana").assertIsDisplayed()
        ui.onNodeWithText("Compacta").assertIsDisplayed();ui.onNodeWithText("Amplia").assertIsDisplayed()
        ui.onAllNodesWithTag("window-width-slider").assertCountEquals(1)
        ui.onNodeWithTag("width-horizontal-slider").assertDoesNotExist()
    }
    @Test fun openingSettingsDoesNotMigrateLegacyGeometryArbitrarily() {
        var config by mutableStateOf(AppConfig(width=0,manualWidth=900,landscapeWidth=460,animations=false))
        show{WindowSizeSettings(config){config=it}}
        ui.onNodeWithTag("window-width-slider").assertExists()
        ui.runOnIdle{assertEquals(-1f,config.windowScale);assertEquals(0,config.width);assertEquals(900,config.manualWidth);assertEquals(460,config.landscapeWidth)}
    }
    @Test @Config(qualifiers="w844dp-h390dp") fun landscapeControlRemainsReachableWithoutTechnicalText() {
        var config by mutableStateOf(AppConfig(width=820,landscapeWidth=460,landscapePosition=.25f,animations=false))
        show{WindowSizeSettings(config){config=it}}
        ui.onNodeWithTag("window-width-slider").performScrollTo().assertIsDisplayed().performSemanticsAction(SemanticsActions.SetProgress){it(.4f)}
        ui.runOnIdle{assertEquals(.4f,config.windowScale);assertEquals(820,config.width);assertEquals(460,config.landscapeWidth);assertEquals(.25f,config.landscapePosition)}
        ui.onNodeWithText("Ancho general",substring=true).assertDoesNotExist()
    }
}
