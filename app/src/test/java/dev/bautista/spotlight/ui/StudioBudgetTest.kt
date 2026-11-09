package dev.bautista.spotlight.ui
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
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
@Config(sdk=[33],qualifiers="w800dp-h800dp")
class StudioBudgetTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 @Test fun usefulWidthWinsOverDecorationAndLegacyHasNoInsets() {
  var width by mutableIntStateOf(640);var scale by mutableFloatStateOf(1f);var focus by mutableStateOf(true);var kind by mutableStateOf("search");var clicks=0
  ui.setContent {SpotlightTheme("dark",AppConfig(focusInterface=focus,animations=false)) {
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,scale)) {
    Box(Modifier.width(width.dp)){StudioSurface(kind){Button({clicks++},Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("content")){Text("Acción")}}}
   }
  }}
  for(case in listOf(Triple(640,1f,"search"),Triple(640,1f,"inspector"),Triple(330,2f,"search"),Triple(330,2f,"inspector"))) {
   ui.runOnIdle{width=case.first;scale=case.second;kind=case.third};ui.waitForIdle()
   val b=ui.onNodeWithTag("content").getUnclippedBoundsInRoot()
   val expected=if(width==330)330.dp else if(kind=="search")624.dp else 628.dp
   assertEquals(expected,b.right-b.left);assertTrue(b.bottom-b.top>=48.dp)
   ui.onNodeWithTag("content").performClick()
  }
  ui.runOnIdle{focus=false;width=640;scale=1f};ui.waitForIdle()
  val legacy=ui.onNodeWithTag("content").getUnclippedBoundsInRoot();assertEquals(640.dp,legacy.right-legacy.left);assertEquals(4,clicks)
 }
}
