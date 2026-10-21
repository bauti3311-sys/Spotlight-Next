package dev.bautista.spotlight.ui

import android.content.*
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h800dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FloatingSearchFlowTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private var fixtureBackdrop by mutableStateOf(Color(0xFF303136))
    private var fixtureOpacity by mutableFloatStateOf(.80f)
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private fun search()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
    private fun capture(name:String) {
        ui.waitForIdle()
        captureScreenRoboImage("build/floating-search/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    }
    private fun show(width:Int=740,height:Float=640f) {
        val launcher=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        listOf("com.spotify.music" to "Spotify","com.whatsapp" to "WhatsApp","webos.fixture" to "webOS Dev Manager").forEach{(pkg,title)->
            val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=title}
            val activity=ActivityInfo().apply{packageName=pkg;name="Main";nonLocalizedLabel=title;applicationInfo=a;exported=true}
            Shadows.shadowOf(app.packageManager).installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity)})
            Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(launcher,ResolveInfo().apply{activityInfo=activity})
        }
        app.search.invalidateApps()
        ui.setContent{Box(Modifier.fillMaxSize().background(fixtureBackdrop)) {
            Box(Modifier.width(width.dp).align(Alignment.TopCenter).padding(top=80.dp)) {
                SpotlightPanel(app,AppConfig(theme="dark",preset="glass",files=false,animations=false,learning=false,
                    favorites=setOf("com.spotify.music/Main"),opacity=fixtureOpacity),{},availableHeightDp=height)
            }
        }}
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:suggest:app:com.spotify.music").fetchSemanticsNodes().isNotEmpty()}
    }
    private fun open(){search().performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.K);keyUp(Key.CtrlLeft)};ui.onNodeWithTag("action-surface").assertIsDisplayed()}
    private fun scenes(prefix:String,width:Int=740,height:Float=640f) {
        show(width,height);capture("$prefix-01-home")
        search().performTextInput("spot")
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:com.spotify.music/Main").fetchSemanticsNodes().isNotEmpty()}
        ui.onNodeWithTag("result:com.spotify.music/Main").assertIsSelected()
        capture("$prefix-02-spot")
        val bounds=ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot
        val rowBounds=ui.onNodeWithTag("result:com.spotify.music/Main").fetchSemanticsNode().boundsInRoot
        open();capture("$prefix-03-actions")
        assertEquals(bounds,ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot)
        assertEquals(rowBounds,ui.onNodeWithTag("result:com.spotify.music/Main").fetchSemanticsNode().boundsInRoot)
        ui.onNodeWithTag("action-filter").performTextInput("información")
        ui.onNodeWithText("Información de la app").assertIsDisplayed();capture("$prefix-04-filter")
        ui.onNodeWithTag("action-filter").performTextClearance()
        ui.onNodeWithText("Copiar…").performScrollTo().performClick()
        ui.onNode(hasText("Copiar paquete") and hasClickAction()).assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        capture("$prefix-05-submenu")
        ui.onNodeWithTag("action-filter").performKeyInput{pressKey(Key.Escape)}
        ui.onNode(hasText("Copiar…") and isFocused()).assertExists()
        ui.onNode(hasText("Copiar…") and isFocused()).performKeyInput{pressKey(Key.Escape)}
        search().assertTextEquals("spot").assertIsFocused()
        ui.onNodeWithTag("result:com.spotify.music/Main").assertIsSelected()
        assertEquals(bounds,ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot)
        assertNull(Shadows.shadowOf(app).peekNextStartedActivity())
        repeat(2){open();ui.onNodeWithContentDescription("Cerrar acciones").performClick();search().assertIsFocused().assertTextEquals("spot")}
    }
    @Test fun realPaletteSurfacesOnControlledLightAndDarkBackdrops() {
        show()
        search().performTextInput("spot")
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:com.spotify.music/Main").fetchSemanticsNodes().isNotEmpty()}
        for((name,color) in listOf("light" to Color.White,"dark" to Color(0xFF101218)))for(alpha in listOf(.70f,.80f,1f)) {
            ui.runOnIdle{fixtureBackdrop=color;fixtureOpacity=alpha}
            ui.onNodeWithTag("result:com.spotify.music/Main").assertIsDisplayed().assertIsSelected()
            capture("contrast-$name-${(alpha*100).toInt()}")
        }
        ui.runOnIdle{fixtureOpacity=.80f;fixtureBackdrop=Color(0xFFE5D8D0)}
        open();capture("contrast-actions-light")
        ui.onNodeWithTag("action-filter").performKeyInput{pressKey(Key.Escape)}
        search().assertTextEquals("spot").assertIsFocused()
    }
    @Test fun tabletScenesPreserveSearchSelectionAndPaletteGeometry(){scenes("tablet")}
    @Test @Config(qualifiers="w390dp-h844dp") fun phoneScenesPreserveSearchSelectionAndPaletteGeometry(){scenes("phone",360,640f)}
    @Test fun tabletShowsSeveralActionsWithoutResizingThePalette() {
        show();search().performTextInput("spot")
        ui.waitUntil(15000){ui.onAllNodesWithTag("result:com.spotify.music/Main").fetchSemanticsNodes().isNotEmpty()}
        val before=ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot
        open()
        ui.onNode(hasText("Abrir") and hasClickAction()).assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        ui.onNodeWithText("Copiar…").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        ui.onNodeWithText("Quitar favorita").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        ui.onNodeWithText("Información de la app").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
        assertEquals(before,ui.onNodeWithTag("spotlight-root").fetchSemanticsNode().boundsInRoot)
    }
}
