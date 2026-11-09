package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Existing licensed glyphs only. A is a size inventory, not an invented application screen. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w600dp-h500dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class VisualFirstIconReviewTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private fun board(theme:String) {
  var proposed by mutableStateOf(false)
  val entries=listOf("Buscar" to CommandIcons.Search,"Cerrar" to CommandIcons.Close,"Apps" to CommandIcons.Apps,
   "Teclado" to CommandIcons.Keyboard,"Ajustes" to HugeIcons.Settings,"Volver" to CommandIcons.Reply,
   "Más" to CommandIcons.MoreHoriz,"Cerrar acciones" to CommandIcons.Close,"Submenú" to CommandIcons.Chevron)
  val sizes=listOf(20,18,20,20,20,17,20,17,16)
  ui.setContent {SpotlightTheme(theme,AppConfig(focusInterface=true)) {
   Surface(Modifier.width(360.dp).testTag("icon-board")) {
    Column(Modifier.padding(12.dp)) {
     Text("Iconos de comando existentes",style=MaterialTheme.typography.titleSmall)
     Text("Inventario de tamaños · no iconos de aplicaciones",style=MaterialTheme.typography.bodySmall)
     entries.chunked(3).forEachIndexed { row,items ->
      Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
       items.forEachIndexed { col,entry ->
        val index=row*3+col
        Column(Modifier.width(106.dp).padding(vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
         IconButton(onClick={},modifier=Modifier.size(48.dp).testTag("icon-hit-$index")) {
          IosCommandIcon(entry.second,entry.first,Modifier.size((if(proposed)20 else sizes[index]).dp))
         }
         Text(entry.first,style=MaterialTheme.typography.labelSmall)
        }
       }
      }
     }
    }
   }
  }}
  for(v in listOf("A","B")) {
   ui.runOnIdle{proposed=v=="B"};ui.waitForIdle()
   for(i in entries.indices) {val b=ui.onNodeWithTag("icon-hit-$i").getUnclippedBoundsInRoot();assertTrue(b.right-b.left>=48.dp);assertTrue(b.bottom-b.top>=48.dp)}
   ui.onNodeWithTag("icon-board").captureRoboImage("build/visual-first/icons-$theme-$v.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  }
 }
 @Test fun dark()=board("dark")
 @Test fun light()=board("light")
}
