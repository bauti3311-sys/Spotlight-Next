package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.SemanticsActions
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

/** Real footer in an isolated viewport. No search, playback or external effects. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1200dp-h600dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class,androidx.compose.ui.ExperimentalComposeUiApi::class)
class VisualFooterBudgetTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private fun scene(width:Int,scale:Float,theme:String,keyboard:Boolean=false,options:Boolean=false,textPreference:Float=1f) {
  val calls=mutableListOf<String>();var opened by mutableStateOf(false)
  lateinit var input:InputModeManager
  ui.setContent {SpotlightTheme(theme,AppConfig(focusInterface=true,animations=false,textScale=textPreference)) {
   val density=LocalDensity.current;val manager=LocalInputModeManager.current
   SideEffect{input=manager}
   CompositionLocalProvider(LocalDensity provides Density(density.density,scale)) {
    Surface(Modifier.width(width.dp).testTag("footer-fixture")) {
     Column {
      Text("Footer real · búsqueda con más resultados · fixture",Modifier.padding(12.dp),style=MaterialTheme.typography.bodySmall)
      GlassContextFooter(suggestion=true,enabled=true,primaryLabel="Buscar en aplicación seleccionada",
       onExecute={calls+="execute"},onActions={calls+="actions";opened=!opened},autocomplete=true,
       onExpand={calls+="expand"},actionsOpen=opened,onOptions=if(options)({calls+="options"})else null)
     }
    }
   }
  }}
  ui.waitForIdle()
  val phase=System.getenv("SPOTLIGHT_FOOTER_PHASE") ?: "before"
  val extra=if(options)"-options" else if(textPreference!=1f)"-preference130" else ""
  ui.onNodeWithTag("footer-fixture").captureRoboImage("build/visual-footer/$phase/$theme-${width}dp-text${(scale*100).toInt()}$extra.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  val root=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
  for(tag in listOf("results-expand","command-primary","command-actions")) {
   val b=ui.onNodeWithTag(tag).getUnclippedBoundsInRoot()
   assertTrue("$tag must keep a visible >=48dp width, got $b",b.right-b.left>=48.dp)
   assertTrue("$tag outside footer $root: $b",b.left>=root.left && b.right<=root.right && b.top>=root.top && b.bottom<=root.bottom)
   ui.onNodeWithTag(tag).assertIsDisplayed()
  }
  if(keyboard) {
   ui.runOnIdle{assertTrue(input.requestInputMode(InputMode.Keyboard))}
   ui.onNodeWithTag("results-expand").performSemanticsAction(SemanticsActions.RequestFocus){it()}
   ui.onNodeWithTag("results-expand").performKeyInput{pressKey(Key.Enter);pressKey(Key.Tab)}
   ui.onNodeWithTag("command-primary").assertIsFocused().performKeyInput{pressKey(Key.Enter);pressKey(Key.Tab)}
   ui.onNodeWithTag("command-actions").assertIsFocused().performKeyInput{pressKey(Key.Enter)}
  } else {
   ui.onNodeWithTag("results-expand").performClick();ui.onNodeWithTag("command-primary").performClick();ui.onNodeWithTag("command-actions").performClick()
  }
  ui.onNodeWithTag("command-primary").assertIsNotEnabled()
  ui.onNodeWithTag("command-actions").performClick()
  assertEquals(listOf("expand","execute","actions","actions"),calls)
  if(options){ui.onNodeWithTag("command-dock-menu").performClick();assertEquals("options",calls.last());assertEquals(5,calls.size)}
 }
 @Test fun tabletDarkText200()=scene(1000,2f,"dark")
 @Test fun tabletLightText200Keyboard()=scene(1000,2f,"light",true)
 @Test fun thresholdDarkText200()=scene(700,2f,"dark")
 @Test fun phoneLightText200()=scene(330,2f,"light")
 @Test fun phoneDarkText200WithOptions()=scene(330,2f,"dark",options=true)
 @Test fun windowLightTextAndPreference130()=scene(700,1.3f,"light",textPreference=1.3f)
 @Test fun tabletLightStandard()=scene(1000,1f,"light")
}
