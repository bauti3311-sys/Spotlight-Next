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
    private fun capture(name:String)=ui.runOnIdle{capture091(ui.activity,"f1-width-$name")}
    @Test fun appearanceShowsActiveHorizontalExceptionToAutomatic() {
        show{GlassAppearance(AppConfig(width=0,landscapeWidth=380,animations=false),{})}
        ui.onNodeWithText("Altura máxima",substring=true).performScrollTo()
        capture("tablet-automatic-horizontal")
        ui.onNodeWithText("Horizontal: 380 dp · ancho específico").assertIsDisplayed()
    }
    @Test fun resettingPositionMustNotEraseHorizontalWidth() {
        var config by mutableStateOf(AppConfig(width=820,landscapeWidth=460,landscapePosition=.25f,animations=false))
        show{InvocationSettings(config){config=it}}
        ui.onNodeWithText("Usar la posición general en ambas orientaciones").performScrollTo().performClick()
        ui.runOnIdle{assertEquals(460,config.landscapeWidth);assertEquals(-1f,config.landscapePosition);assertEquals(820,config.width)}
    }
    @Test fun restoringHorizontalWidthKeepsPositionAndOtherPreferences() {
        var config by mutableStateOf(AppConfig(width=0,landscapeWidth=380,landscapePosition=.25f,pins=setOf("pdf de ayer"),animations=false))
        show{InvocationSettings(config){config=it}}
        ui.onNodeWithText("Restablecer ancho horizontal").performScrollTo().performClick()
        ui.onNodeWithText("Horizontal: automático general").assertIsDisplayed()
        ui.runOnIdle{assertEquals(0,config.width);assertEquals(0,config.landscapeWidth);assertEquals(.25f,config.landscapePosition);assertEquals(setOf("pdf de ayer"),config.pins)}
    }
    @Test fun appearanceDraftRemembersManualWidthAndOnlyAppliesOnRequest() {
        var config by mutableStateOf(AppConfig(width=820,landscapeWidth=460,animations=false))
        show{GlassAppearance(config){config=it}}
        ui.onNodeWithContentDescription("Ancho general automático").performScrollTo().performClick()
        ui.onNodeWithText("Horizontal: 460 dp · ancho específico").assertIsDisplayed()
        ui.runOnIdle{assertEquals(820,config.width)}
        ui.onNodeWithContentDescription("Ancho general automático").performClick()
        ui.onNodeWithText("Ancho general: 820 dp").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Aplicar tema").performScrollTo().performClick()
        ui.runOnIdle{assertEquals(820,config.width);assertEquals(820,config.manualWidth);assertEquals(460,config.landscapeWidth)}
    }
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneShowsExplicitHorizontalOverrideAndAccessibleControls() {
        var config by mutableStateOf(AppConfig(width=0,landscapeWidth=380,animations=false))
        show{OverlayWidthSettings(config){config=it}}
        ui.onNodeWithContentDescription("Ancho general automático").assertIsDisplayed().assertIsOn()
        ui.onNodeWithContentDescription("Ancho horizontal específico").assertIsDisplayed().assertIsOn()
        ui.onNodeWithText("Horizontal: 380 dp · ancho específico").assertIsDisplayed()
        capture("phone-automatic-horizontal")
        ui.onNodeWithText("Restablecer ancho horizontal").performClick()
        ui.onNodeWithContentDescription("Ancho horizontal específico").assertIsOff()
        ui.onNodeWithTag("width-horizontal-slider").assertDoesNotExist()
        ui.onNodeWithText("Horizontal: automático general").assertIsDisplayed()
        capture("phone-reset-horizontal")
    }
    @Test fun enablingHorizontalWidthIsExplicitAndDoesNotDisableGeneralAutomatic() {
        var config by mutableStateOf(AppConfig(width=0,manualWidth=900,animations=false))
        show{OverlayWidthSettings(config){config=it}}
        ui.onNodeWithTag("width-horizontal-slider").assertDoesNotExist()
        ui.onNodeWithContentDescription("Ancho horizontal específico").performClick()
        ui.onNodeWithText("Horizontal: 900 dp · ancho específico").assertIsDisplayed()
        ui.onNodeWithContentDescription("Ancho general automático").assertIsOn()
        ui.runOnIdle{assertEquals(0,config.width);assertEquals(900,config.landscapeWidth)}
        capture("tablet-explicit-horizontal")
    }
    @Test @Config(qualifiers="w844dp-h390dp") fun landscapeControlsRemainReachableAndResetKeepsManualGeneral() {
        var config by mutableStateOf(AppConfig(width=820,landscapeWidth=460,landscapePosition=.25f,animations=false))
        show{OverlayWidthSettings(config){config=it}}
        ui.onNodeWithText("Restablecer ancho horizontal").performScrollTo().assertIsDisplayed().performClick()
        ui.onNodeWithText("Horizontal: 820 dp · ancho general").assertIsDisplayed()
        ui.runOnIdle{assertEquals(820,config.width);assertEquals(.25f,config.landscapePosition)}
        capture("phone-landscape-reset")
    }
}
