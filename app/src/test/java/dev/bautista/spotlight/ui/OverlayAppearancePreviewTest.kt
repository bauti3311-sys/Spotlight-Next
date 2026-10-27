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
class OverlayAppearancePreviewTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private fun show(c:AppConfig) {
        ui.setContent{SpotlightTheme("dark",c){Surface(Modifier.fillMaxWidth()){
            Column(Modifier.width(680.dp).verticalScroll(rememberScrollState()).padding(16.dp)){OverlayAppearancePreview(c)}
        }}}
    }
    private fun check(width:Int, source:String, capture:String) {
        ui.onNodeWithTag("preview-width-label").assertTextContains("Ancho visible: $width dp · $source")
        ui.onNodeWithTag("appearance-preview-window").assertWidthIsEqualTo(width.dp)
        ui.onNodeWithText("2 + 2 = 4").assertIsDisplayed()
        ui.runOnIdle{capture091(ui.activity,"f12-preview-$capture")}
    }
    @Test fun tabletUsesHorizontalOverrideAtRealSize() {
        show(AppConfig(width=820,landscapeWidth=460,animations=false));check(460,"horizontal específico","tablet-horizontal")
        ui.onNodeWithText("Apuntes").performClick().assertIsSelected()
        ui.onNodeWithText("2 + 2 = 4").assertIsNotSelected()
    }
    @Test @Config(qualifiers="w800dp-h1280dp") fun tabletPortraitInheritsAutomaticGeneral() {
        show(AppConfig(width=0,landscapeWidth=460,animations=false));check(640,"automático general","tablet-vertical")
    }
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneClampsManualWidthWithoutPretendingItFits() {
        show(AppConfig(width=820,animations=false));check(366,"manual general","phone-vertical")
        ui.onNodeWithText("Configurado: 820 dp; limitado al espacio de esta pantalla.").assertIsDisplayed()
    }
    @Test @Config(qualifiers="w844dp-h390dp") fun phoneLandscapeUsesActualHorizontalOverride() {
        show(AppConfig(width=0,landscapeWidth=460,animations=false));check(460,"horizontal específico","phone-horizontal")
        ui.onNodeWithText("Calculadora · resultado local").performScrollTo().assertIsDisplayed()
    }
    @Test fun widthDraftUpdatesPreviewAndRestoresOnUndo() {
        var saved by mutableStateOf(AppConfig(width=820,landscapeWidth=460,manualWidth=820,animations=false))
        ui.setContent{SpotlightTheme("dark",saved){Surface{
            Column(Modifier.fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp)){
                GlassAppearance(saved){saved=it}
            }
        }}}
        ui.onNodeWithTag("appearance-preview-window").performScrollTo().assertWidthIsEqualTo(460.dp)
        ui.onNodeWithTag("window-width-slider").performScrollTo().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.SetProgress){it(.7f)}
        val expected=dev.bautista.spotlight.data.WindowSizePolicy.width(1280f,.7f).dp
        ui.onNodeWithTag("appearance-preview-window").performScrollTo().assertWidthIsEqualTo(expected)
        ui.onNodeWithText("Deshacer cambios de esta visita").performScrollTo().performClick()
        ui.onNodeWithTag("appearance-preview-window").performScrollTo().assertWidthIsEqualTo(460.dp)
        ui.runOnIdle{assertEquals(820,saved.width);assertEquals(460,saved.landscapeWidth);assertEquals(820,saved.manualWidth);assertEquals(-1f,saved.windowScale)}
    }
    @Test fun legacyAutomaticDensityIsExplainedWithoutChangingPreferences() {
        val c=AppConfig(width=0,landscapeWidth=460,density="auto",animations=false)
        ui.setContent{SpotlightTheme("dark",c){OverlayDensitySettings(c){fail("Rendering must not save preferences")}}}
        ui.onNodeWithText("Actual: automático según el tamaño de pantalla.").assertIsDisplayed()
        ui.onNodeWithTag("text-scale-value").assertTextEquals("100%")
        ui.onNodeWithTag("ui-density-value").assertTextEquals("100%")
    }
    @Test fun textScaleAndDensitySlidersSnapInFivePercentStepsAndResetWithoutChangingWindowWidth() {
        var saved by mutableStateOf(AppConfig(width=820,landscapeWidth=460,manualWidth=820,pins=setOf("apuntes"),animations=false,density="compact"))
        lateinit var pageScroll:androidx.compose.foundation.ScrollState
        ui.setContent{SpotlightTheme("dark",saved){Surface{
            Column(Modifier.fillMaxHeight().verticalScroll(rememberScrollState().also{pageScroll=it}).padding(16.dp)){GlassAppearance(saved){saved=it}}
        }}}
        fun selectedRowHeight():Float {ui.onNodeWithTag("appearance-preview-viewport").performScrollTo();ui.onNodeWithText("2 + 2 = 4").assertIsDisplayed();val bounds=ui.onNode(isSelected() and hasText("2 + 2 = 4")).getUnclippedBoundsInRoot();return bounds.bottom.value-bounds.top.value}
        val compact=selectedRowHeight()
        ui.onNodeWithTag("density-settings").performScrollTo()
        ui.onNodeWithTag("ui-density-value").assertTextEquals("85%")
        ui.onNodeWithTag("text-scale-slider").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.SetProgress){it(1.13f)}
        ui.onNodeWithTag("ui-density-slider").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.SetProgress){it(1.14f)}
        ui.runOnIdle{
            assertEquals(1.15f,saved.textScale,.001f)
            assertEquals(1.15f,saved.uiDensityScale,.001f)
            assertEquals("spacious",saved.density)
            assertEquals(820,saved.width)
            assertEquals(460,saved.landscapeWidth)
            assertEquals(setOf("apuntes"),saved.pins)
        }
        val spacious=selectedRowHeight()
        ui.runOnIdle{pageScroll.dispatchRawDelta(-pageScroll.value.toFloat())}
        ui.waitForIdle()
        ui.runOnIdle{capture091(ui.activity,"f12-density-draft-spacious")}
        assertTrue("Spacious rows must add vertical room: $compact -> $spacious",spacious>compact)
        ui.onNodeWithTag("appearance-preview-window").assertWidthIsEqualTo(460.dp)
        ui.onNodeWithTag("reset-scale-density").performScrollTo().performClick()
        ui.runOnIdle{
            assertEquals(1f,saved.textScale,.001f)
            assertEquals(1f,saved.uiDensityScale,.001f)
            assertEquals("balanced",saved.density)
        }
    }
    @Test fun scales85And100And115RespectAndroidFontScaleTouchTargetsAndAvoidClipping() {
        var config by mutableStateOf(AppConfig(theme="dark",preset="raycast",textScale=.85f,uiDensityScale=.85f,density="compact",animations=false))
        var fontScale by mutableStateOf(1f)
        ui.setContent {
            val baseDensity=androidx.compose.ui.platform.LocalDensity.current
            CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(baseDensity.density,fontScale)) {
                SpotlightTheme("dark",config) {
                    Surface(Modifier.width(760.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            OverlayAppearancePreview(config,simple=true)
                        }
                    }
                }
            }
        }
        listOf(.85f to .85f, 1f to 1f, 1.15f to 1.15f, .85f to 1.15f, 1.15f to .85f).forEach { (tScale, dScale) ->
            for (sysFont in listOf(1f, 1.25f)) {
                ui.runOnIdle {
                    fontScale=sysFont
                    config=config.copy(textScale=tScale,uiDensityScale=dScale,density=GlassTokens.densityKeyForScale(dScale))
                }
                ui.waitForIdle()
                val windowBounds=ui.onNodeWithTag("appearance-preview-window").getUnclippedBoundsInRoot()
                val row0=ui.onNodeWithTag("result:preview:calculator").assertIsDisplayed().getUnclippedBoundsInRoot()
                val row1=ui.onNodeWithTag("result:preview:note").assertIsDisplayed().getUnclippedBoundsInRoot()
                assertTrue("Row 0 touch target must be >= 48dp at text=$tScale density=$dScale fontScale=$sysFont", (row0.bottom-row0.top) >= 48.dp)
                assertTrue("Row 1 touch target must be >= 48dp at text=$tScale density=$dScale fontScale=$sysFont", (row1.bottom-row1.top) >= 48.dp)
                assertTrue("Row 0 must not overflow preview window horizontally", row0.left >= windowBounds.left && row0.right <= windowBounds.right)
            }
        }
    }
}
