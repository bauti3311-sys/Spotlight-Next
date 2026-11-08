package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h1000dp-mdpi")
class PuenteFrameTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 @Test fun shortDestinationWrapsContentAndRetainsAdjustableDivider() {
  ui.setContent{SpotlightTheme("light",AppConfig(focusInterface=true,animations=false)){
   PuenteFrame(600.dp,true,"fixture",header={Text("Buscar")},results={Text("Tres resultados")},footer={Text("Acciones")},
    inspector={Text("Contenido breve",Modifier.testTag("body"))},inspectorHeader={Text("Inspector")},modifier=Modifier.width(1000.dp))
  }}
  val b=ui.onNodeWithTag("puente-destination").getUnclippedBoundsInRoot();assertTrue(b.height<200.dp)
  val divider=ui.onNodeWithContentDescription("Divisor ajustable del panel contextual")
  divider.assertExists().performTouchInput{swipeLeft()}
  ui.onNodeWithText("Contenido breve").assertIsDisplayed()
 }
 @Test fun compactEnlargedContextScrollsToAnExplicitActionWithoutExecutingOnScroll() {
  var effects=0
  ui.setContent{CompositionLocalProvider(LocalDensity provides Density(1f,2f)){SpotlightTheme("light",AppConfig(focusInterface=true,animations=false)){
   PuenteFrame(330.dp,false,"fixture",header={Text("Búsqueda")},results={Text("Resultado")},footer={Text("Acciones")},
    inspector={repeat(12){Text("Contenido $it")};Button({effects++},Modifier.heightIn(min=48.dp).testTag("explicit")){Text("Abrir explícitamente")}},
    inspectorHeader={Text("Inspector")},modifier=Modifier.width(330.dp))
  }}}
  val root=ui.onNodeWithTag("puente-frame").getUnclippedBoundsInRoot();assertTrue(root.height<=330.dp && root.width<=330.dp)
  ui.onNodeWithTag("explicit").performScrollTo().assertIsDisplayed();assertEquals(0,effects)
  val target=ui.onNodeWithTag("explicit").getUnclippedBoundsInRoot();assertTrue(target.height>=48.dp)
  ui.onNodeWithTag("explicit").performClick();assertEquals(1,effects)
 }
 @Test fun detachedPanesKeepMinimumWidthsAtTheSplitThreshold() {
  var width by mutableStateOf(696.dp)
  ui.setContent{SpotlightTheme("light",AppConfig(focusInterface=true,animations=false)){
   Box(Modifier.width(width)){ContextSplitPanel(300.dp,{Text("Resultados")},{Text("Inspector")},initialFraction=.44f,detached=true)}
  }}
  for(w in listOf(696.dp,700.dp)) {
   ui.runOnIdle{width=w}
   assertTrue(ui.onNodeWithTag("results-pane").getUnclippedBoundsInRoot().width>=400.dp)
   assertTrue(ui.onNodeWithTag("context-inspector").getUnclippedBoundsInRoot().width>=280.dp)
  }
 }

}
