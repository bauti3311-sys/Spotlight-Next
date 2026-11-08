package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.*
import java.io.File
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1000dp-h900dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class FocusTitlePriorityTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val item=Result("title-fixture","Leer mensajes de WhatsApp","Notificaciones actuales autorizadas","Sugerencias",0,Action.Copy("fixture"))
 private var width by mutableStateOf(470)
 private var scale by mutableStateOf(1f)
 private var theme by mutableStateOf("dark")
 private var focus by mutableStateOf(true)
 private var selected by mutableStateOf(true)
 private fun show()=ui.setContent {CompositionLocalProvider(LocalDensity provides Density(1f,scale)){
  SpotlightTheme(theme,AppConfig(focusInterface=focus,focusTypeface="geist",animations=false)) {
   Surface(Modifier.width(width.dp).testTag("scene")) {
    ResultRow(item,selected,false,{},{},{},animations=false)
   }
  }
 }}
 private fun layout():TextLayoutResult {
  val values=mutableListOf<TextLayoutResult>()
  ui.onNodeWithText(item.title,useUnmergedTree=true).performSemanticsAction(SemanticsActions.GetTextLayoutResult){it(values)}
  return values.single()
 }
 @Test fun constrainedInspectorListKeepsUsefulTitleComplete() {
  show()
  assertFalse("470dp list has room for the title if secondary text yields",layout().isLineEllipsized(0))
  val before=ui.onNodeWithTag("result:${item.id}").getUnclippedBoundsInRoot()
  ui.runOnIdle{selected=false}
  assertEquals(before,ui.onNodeWithTag("result:${item.id}").getUnclippedBoundsInRoot())
 }
 @Test fun equivalentRowsPreserveBoundsAndFullAccessibleTextAcrossProfiles() {
  show();val metrics=StringBuilder()
  for(w in listOf(470,780,328))for(t in listOf("dark","light"))for(f in listOf(true,false)) {
   ui.runOnIdle{width=w;theme=t;scale=if(w==328)2f else 1f;focus=f;selected=true};ui.waitForIdle()
   val title=ui.onNodeWithText(item.title,useUnmergedTree=true).assertIsDisplayed()
   ui.onNodeWithText(item.subtitle,useUnmergedTree=true).assertIsDisplayed()
   ui.onNodeWithText("Sugerencias",useUnmergedTree=true).assertIsDisplayed()
   val row=ui.onNodeWithTag("result:${item.id}").getUnclippedBoundsInRoot()
   assertEquals(w.toFloat(),row.width.value,.01f);assertTrue(row.height>=48.dp)
   val bounds=title.getUnclippedBoundsInRoot();assertTrue(bounds.left>=row.left && bounds.right<=row.right)
   val name="$w-$t-${if(w==328)200 else 100}-${if(f)"focus" else "legacy"}"
   metrics.appendLine("$name title-width=${layout().size.width} ellipsis=${layout().isLineEllipsized(0)} row=$row")
   ui.onNodeWithTag("scene").captureRoboImage("build/focus-title-priority/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  }
  File("build/focus-title-priority/metrics.txt").writeText(metrics.toString())
 }
}
