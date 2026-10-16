package dev.bautista.spotlight.ui

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Neutral pixel captures; actual focus is asserted before removing the blinking cursor. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class VisualSnapshotTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private val config=AppConfig(files=false,animations=false,online=false,suggestions=false,learning=false,theme="dark")
    private lateinit var focus:FocusManager
    private fun show(query:String="") {
        ui.setContent{focus=LocalFocusManager.current;SpotlightPanel(app,config,{},query,700f)}
        ui.onNode(hasSetTextAction()).assertExists()
    }
    private fun await(text:String)=ui.waitUntil(15000){ui.onAllNodesWithText(text,useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()}
    private fun capture(name:String) {
        ui.runOnIdle{focus.clearFocus(force=true)}
        ui.waitForIdle()
        ui.onRoot().captureRoboImage("src/test/snapshots/$name.png")
    }
    private fun component(content:@Composable ()->Unit) {
        ui.setContent{focus=LocalFocusManager.current;SpotlightTheme("dark"){Surface(Modifier.width(680.dp)){content()}}}
    }
    @Test fun initialBar(){show();ui.onNode(hasSetTextAction()).assertIsFocused();capture("tablet-initial")}
    @Test fun calculatorResults(){show("2+2");await("4");capture("tablet-results")}
    @Test fun keyboardSelection(){show("2+2");await("4");ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.DirectionDown)};ui.onNode(hasSetTextAction()).assertTextEquals("2+2");capture("tablet-selection")}
    @Test fun contextualPanel(){show("2+2");await("4");ui.onNode(hasSetTextAction()).performKeyInput{pressKey(Key.Tab)};await("Copiar resultado");ui.onNode(hasText("Copiar resultado") and isFocused()).assertExists();capture("tablet-context-panel")}
    @Test fun optionsMenu(){show("2+2");await("4");ui.onNodeWithContentDescription("Fuentes de búsqueda").performClick();ui.onNodeWithText("Modo privado").assertExists();capture("tablet-options")}
    @Test fun savedContent(){component{SavedPanel(app,config.copy(pins=setOf("pdf de ayer"),snippets=setOf("saludo|Hola {nombre}"))) {}};ui.onNodeWithText("Guardados").assertExists();ui.onNodeWithText("pdf de ayer").assertExists();ui.onNodeWithText("Nota nueva desde comandos").assertDoesNotExist();capture("tablet-saved")}
    @Test fun preferences(){component{PreferencePanel(config,"appearance") {}};ui.onNodeWithText("Apariencia").assertExists();capture("tablet-preferences")}
    @Test fun pdfPermissionOrMissingFileError(){component{PdfQuickLook(LocalContext.current,Uri.parse("file:///missing-spotlight-fixture.pdf"),0,{},false)};await("No se pudo abrir el PDF. Revisá el permiso o probá abrirlo en otra app.");capture("tablet-pdf-error")}
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneResults(){show("2+2");await("4");capture("phone-results")}
    @Test @Config(qualifiers="w915dp-h412dp") fun landscapeResults(){show("2+2");await("4");capture("landscape-results")}
}
