package dev.bautista.spotlight.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.search.*
import dev.bautista.spotlight.data.Config as AppConfig
import org.json.JSONArray
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Presentation examples only. Does not implement features, invoke integrations or simulate external success. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1000dp-h800dp-mdpi",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class FunctionChoiceGalleryTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 @Test fun render36PresentationFixturesWithoutExecutingActions() {
  val data=JSONArray(javaClass.classLoader!!.getResourceAsStream("choice-catalogue/functions.json")!!.bufferedReader().readText())
  var current by mutableIntStateOf(0)
  var callbacks=0
  val c=AppConfig(focusInterface=true,theme="dark",animations=false)
  ui.setContent {SpotlightTheme("dark",c) {
   val f=data.getJSONObject(current)
   Column(Modifier.size(760.dp,430.dp).background(MaterialTheme.colorScheme.surface).padding(16.dp).testTag("function-canvas")) {
    Text("${f.getString("id")} · PROPUESTA · DATOS FICTICIOS",style=MaterialTheme.typography.labelSmall)
    Spacer(Modifier.height(12.dp))
    SpotlightBar(f.getString("example"),{},"Todo",{},{},{callbacks++},remember{FocusRequester()},1f,false,animations=false)
    HorizontalDivider()
    ResultRow(Result("proposal:${f.getString("id")}",f.getString("result"),"Vista de ejemplo · capacidad por elegir","Herramientas",1000,Action.Copy("fixture")),true,false,{callbacks++},{callbacks++},{callbacks++},animations=false,inline=false)
    Spacer(Modifier.height(16.dp))
    Text(f.getString("value"),style=MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))
    Text(f.getString("limit"),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.weight(1f))
    Text("No se ejecuta ni envía nada. Elegir esta ficha no implementa la función.",style=MaterialTheme.typography.labelSmall)
   }
  }}
  for(i in 0 until data.length()) {
   ui.runOnIdle{current=i};ui.waitForIdle()
   val id=data.getJSONObject(i).getString("id")
   ui.onNodeWithTag("function-canvas").captureRoboImage("build/choice-catalogue/functions/$id.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  }
  assertEquals(36,data.length());assertEquals(0,callbacks)
  val app=ui.activity.application as SpotlightApp
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
}
