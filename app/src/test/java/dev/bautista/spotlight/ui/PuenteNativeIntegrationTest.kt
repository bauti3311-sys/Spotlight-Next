package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h1000dp-mdpi",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
class PuenteNativeIntegrationTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=ui.activity.application as SpotlightApp
 private data class Profile(val id:String,val w:Int,val h:Int,val theme:String,val scale:Float)
 private var profile by mutableStateOf(Profile("tablet-light",1200,800,"light",1f))
 private var revision by mutableIntStateOf(0)
 private val phase get()=System.getenv("PUENTE_CAPTURE_PHASE") ?: "after"
 private val id="fixture.proyecto/Main"
 private fun install() {
  val infos=listOf("Proyecto","Proyecto Atlas","Proyecto Notas").mapIndexed { index,name->
   val pkg=if(index==0)"fixture.proyecto" else "fixture.proyecto$index"
   val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=name;enabled=true}
   val ai=ActivityInfo().apply{packageName=pkg;this.name="Main";applicationInfo=a;nonLocalizedLabel=name;enabled=true;exported=true}
   Shadows.shadowOf(app.packageManager).installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(ai);versionName="fixture"})
   ResolveInfo().apply{activityInfo=ai}
  }
  val main=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
  Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(main,infos)
  infos.forEach{Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(Intent(main).setPackage(it.activityInfo.packageName),it)}
  app.search.invalidateApps()
 }
 private fun show() {
  install()
  ui.setContent {
   val p=profile
   CompositionLocalProvider(LocalDensity provides Density(1f,p.scale)) {
    Box(Modifier.size(p.w.dp,p.h.dp).background(if(p.theme=="light")Color(0xFFDCE9ED)else Color(0xFF263C4A)).testTag("puente-canvas")) {
     Column(Modifier.padding(30.dp)){repeat(10){Text("Fondo de laboratorio · Revisar materiales y próximos pasos",color=if(p.theme=="light")Color(0xFF78939E)else Color(0xFF7996A4),modifier=Modifier.padding(vertical=15.dp))}}
     key(revision){SpotlightPanel(app,AppConfig(theme=p.theme,focusInterface=true,files=false,contacts=false,online=false,suggestions=false,learning=false,animations=false),{},"Proyecto",(p.h-24).toFloat())}
    }
   }
  }
 }
 private fun ready(){ui.waitUntil(15000){ui.onAllNodesWithTag("result:$id").fetchSemanticsNodes().isNotEmpty()}}
 @Test fun captureScenes() {
  show()
  val profiles=buildList {for(theme in listOf("light","dark")){
   add(Profile("tablet-$theme",1200,800,theme,1f));add(Profile("phone-$theme",412,860,theme,1f));add(Profile("compact200-$theme",330,760,theme,2f))}}
  val metrics=StringBuilder()
  for(p in profiles) {
   ui.runOnIdle{profile=p;revision++};ready()
   for(scene in listOf("resultados","inspector")) {
    if(scene=="inspector"){ui.onNodeWithTag("result:$id").performClick();ui.onNodeWithTag("inspector-title").assertTextEquals("Proyecto")}
    ui.waitForIdle()
    ui.onNodeWithTag("puente-canvas").captureRoboImage("build/puente-native/$phase/${p.id}-$scene.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot();assertTrue(root.right<=p.w.dp && root.bottom<=p.h.dp)
    metrics.appendLine("${p.id} $scene root=$root")
   }
  }
  File("build/puente-native/$phase/metrics.txt").apply{parentFile.mkdirs();writeText(metrics.toString())}
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
}
