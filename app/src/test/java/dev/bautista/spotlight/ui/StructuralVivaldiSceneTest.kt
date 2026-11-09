package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.search.Result
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StructuralVivaldiSceneTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private var mode by mutableStateOf("preview63")
 private val invoked=mutableListOf<Result>()
 private var info=0;private var favorite=0
 private val capabilities=mutableListOf<dev.bautista.spotlight.search.Action>()
 private fun scene(width:Int,theme:String) {
  val app=RuntimeEnvironment.getApplication() as SpotlightApp
  val pkg="com.vivaldi.browser"
  val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel="Vivaldi";enabled=true}
  val activity=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=a;nonLocalizedLabel="Vivaldi";enabled=true;exported=true}
  val pm=Shadows.shadowOf(app.packageManager)
  pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity);versionName="7.2-fixture"})
  pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=activity})
  app.search.invalidateApps()
  val config=AppConfig(theme=theme,preset="glass",files=false,online=false,contacts=false,animations=false,learning=false)
  val raw=runBlocking{withTimeout(15000){app.search.search("Vivaldi",config).first { r ->r.any{it.id=="$pkg/Main"} && r.any{it.title=="Buscar en Vivaldi"}}}}
  val rows=dev.bautista.spotlight.search.CompactResults.select("Vivaldi",raw.filter(ProductVisibility::allowed),config,true).visible(false,adaptive=true)
  assertEquals(2,rows.size)
  assertEquals("$pkg/Main",rows.first().id)
  val version=app.packageManager.getPackageInfo(pkg,0).versionName!!
  ui.setContent {
   val option=when(mode){"A"->StructuralOption.Unified;"B"->StructuralOption.Focus;else->null}
   CompositionLocalProvider(LocalLiquidScene provides (if(option==null)null else LiquidTypeface.Geist),LocalPremiumReview provides (option!=null)) {
    SpotlightTheme(theme) {
     Box(Modifier.width(width.dp).height(700.dp).background(if(theme=="dark")Color(0xFF101214) else Color(0xFFE9E6E0)).testTag("comparison-canvas")) {
      if(option==null)SpotlightPanel(app,config,{},"",700f)
      else StructuralVivaldiPrototype(option,rows,version,{invoked+=it},{info++},{favorite++},{capabilities+=it})
     }
    }
   }
  }
  val field=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
  field.performClick().performTextReplacement("Vivaldi")
  ui.waitUntil(15000){ui.onAllNodesWithTag("result:$pkg/Main").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithTag("result:$pkg/Main").performClick()
  ui.waitUntil(15000){ui.onAllNodesWithText(version).fetchSemanticsNodes().isNotEmpty()}
  ui.waitUntil(15000){ui.onAllNodes(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.ProgressBarRangeInfo,androidx.compose.ui.semantics.ProgressBarRangeInfo.Indeterminate)).fetchSemanticsNodes().isEmpty()}
  val visibleIds=ui.onAllNodes(SemanticsMatcher("result row") {
   it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.TestTag) && it.config[androidx.compose.ui.semantics.SemanticsProperties.TestTag].startsWith("result:")
  }).fetchSemanticsNodes().map{it.config[androidx.compose.ui.semantics.SemanticsProperties.TestTag].removePrefix("result:")}
  assertEquals(rows.map{it.id},visibleIds)
  val profile="${if(width>600)"tablet" else "phone"}-$theme"
  val metrics=mutableListOf<String>()
  for(m in listOf("preview63","A","B")) {
   ui.runOnIdle{mode=m};ui.waitForIdle()
   val tag=if(m=="preview63")"spotlight-root" else "prototype-footprint"
   val bounds=ui.onNodeWithTag(tag).getUnclippedBoundsInRoot()
   val surfaceHeight=if(m=="preview63")(bounds.bottom-bounds.top).value-24f else ui.onNodeWithTag("prototype-window").getUnclippedBoundsInRoot().let{(it.bottom-it.top).value}
   val surfaceWidth=width-32f
   val header=ui.onNodeWithTag(if(m=="preview63")"spotlight-bar" else "prototype-search").getUnclippedBoundsInRoot().let{(it.bottom-it.top).value}
   val footer=ui.onNodeWithTag(if(m=="preview63")"context-footer" else "prototype-footer").getUnclippedBoundsInRoot().let{(it.bottom-it.top).value}
   val bodyArea=surfaceWidth*(surfaceHeight-header-footer)
   metrics+="$m,${(bounds.right-bounds.left).value},${(bounds.bottom-bounds.top).value},${width*700},${(bounds.right-bounds.left).value*(bounds.bottom-bounds.top).value},$surfaceWidth,$surfaceHeight,$bodyArea"
   ui.onNodeWithTag("comparison-canvas").captureRoboImage("build/structural-vivaldi/$profile-$m.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
   assertTrue(bounds.bottom<=700.dp)
   if(m!="preview63") {
    ui.onNodeWithTag("prototype-open").performClick()
    assertEquals(rows.first().id,invoked.last().id)
    ui.onNodeWithTag("prototype-details").performClick()
    ui.onNodeWithText("Versión  $version").assertIsDisplayed()
    ui.onNodeWithText("Información de la app").performClick();assertTrue(info>0)
    ui.onNodeWithText("Añadir favorita").performClick();assertTrue(favorite>0)
    ui.onNodeWithTag("prototype-query").performKeyInput{pressKey(Key.Escape)}
    ui.onNodeWithTag("prototype-details").assertExists()
    ui.onNodeWithTag("prototype-back").performClick()
    ui.onNodeWithTag("prototype-context").assertDoesNotExist()
    ui.onNodeWithTag("prototype-query").assertTextEquals("Vivaldi").assertIsFocused()
    ui.onNodeWithTag("prototype-result-0").performClick()
    ui.onNodeWithTag("prototype-context").assertExists()
    ui.onNodeWithTag("prototype-query").performKeyInput{pressKey(Key.Tab)}
    ui.onNodeWithText("Versión  $version").assertIsDisplayed()
   }
  }
  java.io.File("build/structural-vivaldi/$profile-metrics.csv").apply{parentFile!!.mkdirs();writeText("mode,footprintWidthDp,footprintHeightDp,canvasAreaDp2,footprintAreaDp2,surfaceWidthDp,surfaceHeightDp,bodyAreaDp2\n"+metrics.joinToString("\n")+"\n")}
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
 @Test fun tabletDark()=scene(960,"dark")
 @Test fun tabletLight()=scene(960,"light")
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneDark()=scene(360,"dark")
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneLight()=scene(360,"light")
}
