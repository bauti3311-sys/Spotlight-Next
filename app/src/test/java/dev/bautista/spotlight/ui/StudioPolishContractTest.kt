package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.integrations.spotify.SpotifyConnectionState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.util.ReflectionHelpers
import kotlin.math.abs
import com.github.takahirom.roborazzi.*
import java.io.File
import android.graphics.BitmapFactory

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w800dp-h900dp-mdpi",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalTestApi::class,androidx.compose.ui.ExperimentalComposeUiApi::class,ExperimentalRoborazziApi::class)
class StudioPolishContractTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 @Test fun innerBackingRejectsCompetingBackgroundWithoutChangingStoredOpacityOrLegacy() {
  var background by mutableStateOf(Color.Black);var theme by mutableStateOf("dark");var focus by mutableStateOf(true);var footer by mutableStateOf(false)
  val c=AppConfig(focusInterface=true,preset="glass",opacity=.70f,animations=false)
  ui.setContent {SpotlightTheme(theme,c.copy(focusInterface=focus)) {
   Box(Modifier.size(160.dp,80.dp).background(background).testTag("sample")) {
    Column(Modifier.fillMaxSize().glassSurface(c.copy(focusInterface=focus),true)) {
     Box(Modifier.fillMaxSize().studioContentBacking(footer)){Text("Resultado")}
    }
   }
  }}
  fun pixel(name:String):IntArray {
   val path="build/studio-physical-polish/contrast/"+name+".png"
   ui.onNodeWithTag("sample").captureRoboImage(path,roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
   val rgb=BitmapFactory.decodeFile(path).let { bitmap -> bitmap.getPixel(80,55).also { bitmap.recycle() } }
   return intArrayOf((rgb ushr 16) and 255,(rgb ushr 8) and 255,rgb and 255)
  }
  for(t in listOf("dark","light"))for(f in listOf(true,false))for(role in listOf(false,true)) {
   ui.runOnIdle{theme=t;focus=f;footer=role;background=Color.Black};ui.waitForIdle()
   val black=pixel("$t-$f-$role-black")
   ui.runOnIdle{background=Color.White};ui.waitForIdle()
   val white=pixel("$t-$f-$role-white")
   val delta=black.indices.maxOf{abs(black[it]-white[it])}
   println("CONTRAST $t focus=$f footer=$role delta8bit=$delta")
   if(f)assertTrue("Inner Focus text backdrop should barely vary: $delta",delta<=2)
   else assertTrue("Legacy retains original translucent working surface: $delta",delta>=5)
  }
  assertEquals(.70f,c.opacity)
 }
 @Test fun boundedLargeTextMenuScrollTabEnterEscapePreserveActions() {
  val focus=FocusRequester();val executed=mutableListOf<Int>();var closed=0
  ui.setContent{CompositionLocalProvider(LocalDensity provides Density(1f,2f)){
   SpotlightTheme("dark",AppConfig(focusInterface=true,animations=false)) {
    Box(Modifier.width(344.dp).heightIn(max=304.dp)) {
     ActionPanelView((0..11).map{i->ContextAction("Acción $i del resultado","Detalle legible",CommandIcons.Apps,{executed+=i},id="a$i")},focus,{closed++})
    }
   }
  }}
  ui.runOnIdle{focus.requestFocus()};ui.waitForIdle()
  ui.onNodeWithTag("context-action-panel").performTouchInput{swipeUp()}
  assertTrue(executed.isEmpty())
  repeat(11){ui.onRoot().performKeyInput{pressKey(Key.Tab)};ui.waitForIdle()}
  ui.onNodeWithText("Acción 11 del resultado").assertIsFocused().assertIsDisplayed()
  ui.onRoot().performKeyInput{pressKey(Key.Enter)}
  ui.runOnIdle{assertEquals(listOf(11),executed)}
  ui.onRoot().performKeyInput{pressKey(Key.Escape)}
  ui.runOnIdle{assertEquals(1,closed);assertEquals(listOf(11),executed)}
 }
 @Test fun narrowLargeTextFooterReservesReadableSearchAction() {
  ui.setContent { CompositionLocalProvider(LocalDensity provides Density(1f,2f)) {
   SpotlightTheme("dark",AppConfig(focusInterface=true,animations=false)) {
    Box(Modifier.width(328.dp)) { FocusSearchTaskFooter(dev.bautista.spotlight.search.Action.ComposeSearch("spotify","com.spotify.music",""),remember{FocusSearchReviewController()},true,{},{}) }
   }
  }}
  val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
  ui.onNodeWithText("Buscar",useUnmergedTree=true).performSemanticsAction(SemanticsActions.GetTextLayoutResult){it(layouts)}
  assertEquals("Search must stay legible on one line",1,layouts.single().lineCount)
  ui.onNodeWithTag("focus-prepare").assertIsDisplayed().assertIsNotEnabled()
 }
 @Test fun compactSpotifyToolbarRetainsTouchAndKeyboardFilterOrderAt200Percent() {
  val app=ui.activity.application as SpotlightApp;lateinit var input:InputModeManager
  runBlocking{app.spotify.configure("a".repeat(32));app.spotify.inspect()}
  ui.setContent{CompositionLocalProvider(LocalDensity provides Density(1f,2f)){
   val manager=LocalInputModeManager.current;SideEffect{input=manager}
   SpotlightTheme("light",AppConfig(focusInterface=true,animations=false)) {
    Box(Modifier.width(328.dp)){SpotifySearchPanel(app,externalQuery="",searchTrigger=0)}
   }
  }}
  ui.waitForIdle()
  ui.runOnIdle{ReflectionHelpers.getField<MutableStateFlow<SpotifyConnectionState>>(app.spotify,"mutableState").value=SpotifyConnectionState.AUTHORIZED}
  ui.waitForIdle()
  for(label in listOf("Tema","Artista","Álbum","Lista")) {
   val chip=ui.onNode(hasText(label) and hasClickAction()).assertIsDisplayed()
   assertTrue("Actual touch target must remain 48px at mdpi: $label",chip.fetchSemanticsNode().touchBoundsInRoot.height>=48f)
   chip.performClick().assertIsSelected()
  }
  ui.runOnIdle{assertTrue(input.requestInputMode(InputMode.Keyboard))}
  ui.onNodeWithText("Tema").performSemanticsAction(SemanticsActions.RequestFocus){it()}
  ui.onNodeWithText("Tema").assertIsFocused().performKeyInput{pressKey(Key.Tab)}
  ui.onNodeWithText("Artista").assertIsFocused().performKeyInput{pressKey(Key.Enter)}
  ui.onNodeWithText("Artista").assertIsSelected().performKeyInput{keyDown(Key.ShiftLeft);pressKey(Key.Tab);keyUp(Key.ShiftLeft)}
  ui.onNodeWithText("Tema").assertIsFocused()
  ui.onNodeWithText("Confirmar reproducción").assertDoesNotExist()
 }
}
