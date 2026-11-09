package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class StitchInteractionTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val focus=FocusRequester()
 private var effects=0
 private var closed=0
 private fun show(enlarged:Boolean=false) {
  val config=AppConfig(theme="dark",focusInterface=true,animations=false)
  ui.setContent { SpotlightTheme("dark",config){PuenteOverlayTheme(config){
   CompositionLocalProvider(LocalPuenteUi provides true,LocalDensity provides Density(1f,if(enlarged)2f else 1f)){
    Box(Modifier.width(330.dp).heightIn(max=460.dp)) {
     ActionPanelView((1..12).map{n->ContextAction("Acción $n","Revisar el destino",CommandIcons.Apps,{effects++},id="action:$n")},focus,{closed++})
    }
   }
  }} }
  ui.runOnIdle{focus.requestFocus()}
 }
 @Test fun keyboardFiltersExecutesOnceAndEscapeClosesWithoutAnotherEffect(){
  show()
  ui.onNodeWithTag("action-filter").performClick().performTextInput("Acción 12")
  ui.onNodeWithTag("action-filter").performKeyInput{pressKey(Key.Enter)}
  assertEquals(1,effects)
  ui.onNodeWithTag("action-filter").performKeyInput{pressKey(Key.Escape)}
  assertEquals(1,closed);assertEquals(1,effects)
 }
 @Test fun enlargedMenuKeepsLastActionAccessibleWithoutExecutingOnScroll(){
  show(true)
  ui.onNodeWithText("Acción 12").performScrollTo().assertIsDisplayed()
  assertEquals(0,effects)
  assertTrue(ui.onNodeWithText("Acción 12").getUnclippedBoundsInRoot().height>=48.dp)
  ui.onNodeWithText("Acción 12").performClick();assertEquals(1,effects)
 }
}
