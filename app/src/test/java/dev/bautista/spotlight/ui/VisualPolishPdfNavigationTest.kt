package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w600dp-h600dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class, ExperimentalTestApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
class VisualPolishPdfNavigationTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private fun scene(theme:String,scale:Float) {
  val selected=mutableListOf<Int>()
  ui.setContent {SpotlightTheme(theme,AppConfig(focusInterface=true)) {
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,scale)) {
    Surface(Modifier.width(330.dp).testTag("pdf-nav-scene")) {
     Column(Modifier.padding(12.dp)) {
      Text("Paginación PDF · fixture de 3 páginas",style=MaterialTheme.typography.bodySmall)
      PdfPageNavigation(1,3){selected+=it}
     }
    }
   }
  }}
  ui.waitForIdle()
  val phase=System.getenv("SPOTLIGHT_VISUAL_PHASE") ?: "before"
  ui.onNodeWithTag("pdf-nav-scene").captureRoboImage("build/visual-polish/$phase/pdf-nav-$theme-text${(scale*100).toInt()}.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  val previous=ui.onNodeWithText("Anterior").getUnclippedBoundsInRoot()
  val next=ui.onNodeWithText("Siguiente").getUnclippedBoundsInRoot()
  assertTrue("Previous target remains >=48dp",previous.bottom-previous.top>=48.dp)
  assertTrue("Next target remains >=48dp",next.bottom-next.top>=48.dp)
  assertTrue("Previous label must not wrap differently from next",kotlin.math.abs((previous.bottom-previous.top).value-(next.bottom-next.top).value)<=1f)
  assertTrue(previous.left>=0.dp && next.right<=330.dp)
  ui.onNodeWithText("Anterior").performClick();ui.onNodeWithText("Siguiente").performClick()
  assertEquals(listOf(0,2),selected)
 }
 @Test fun darkText200()=scene("dark",2f)
 @Test fun lightText200()=scene("light",2f)
 @Test fun normalText()=scene("light",1f)
 @Test fun wrappedNavigationKeepsTabShiftTabAndEnterOrder() {
  lateinit var input:InputModeManager
  val requested=mutableListOf<Int>()
  ui.setContent {SpotlightTheme("dark",AppConfig(focusInterface=true,animations=false)) {
   val manager=LocalInputModeManager.current;val d=LocalDensity.current
   SideEffect{input=manager}
   CompositionLocalProvider(LocalDensity provides Density(d.density,2f)) {
    Surface(Modifier.width(330.dp)){PdfPageNavigation(1,3){requested+=it}}
   }
  }}
  ui.runOnIdle{assertTrue(input.requestInputMode(InputMode.Keyboard))};ui.waitForIdle()
  ui.onNodeWithText("Anterior").performSemanticsAction(SemanticsActions.RequestFocus){it()}
  ui.onNodeWithText("Anterior").assertIsFocused().performKeyInput{pressKey(Key.Tab)}
  ui.onNodeWithText("Siguiente").assertIsFocused().performKeyInput{pressKey(Key.Enter)}
  ui.onNodeWithText("Siguiente").performKeyInput{keyDown(Key.ShiftLeft);pressKey(Key.Tab);keyUp(Key.ShiftLeft)}
  ui.onNodeWithText("Anterior").assertIsFocused().performKeyInput{pressKey(Key.Enter);pressKey(Key.Escape)}
  assertEquals(listOf(2,0),requested)
 }

}
