package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.*
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.ActionCatalog
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
class FocusBIntegrationTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 private val pkg="com.vivaldi.browser"
 private val before get()=System.getenv("FOCUS_B_CAPTURE_PHASE")=="before"
 private fun field()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
 private fun install() {
  val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel="Vivaldi";enabled=true}
  val ai=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=a;nonLocalizedLabel="Vivaldi";enabled=true;exported=true}
  val ri=ResolveInfo().apply{activityInfo=ai};val pm=Shadows.shadowOf(app.packageManager)
  pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(ai);versionName="7.2-fixture"})
  val main=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
  pm.addResolveInfoForIntent(main,ri);pm.addResolveInfoForIntent(Intent(main).setPackage(pkg),ri)
  for(q in listOf("consulta","android tablet"))pm.addResolveInfoForIntent((ActionCatalog.searchAction("vivaldi",pkg,q) as Action.InApp).intent,ri)
  app.search.invalidateApps()
 }
 private fun capture(name:String,scene:String) {
  ui.waitForIdle()
  ui.onNodeWithTag("integration-canvas").captureRoboImage("build/focus-b-integration/${if(before)"before" else "after"}/$name-$scene.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
  val footer=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
  assertTrue("Footer must stay inside root",footer.bottom<=root.bottom)
  assertTrue("330dp viewport budget",root.bottom-root.top<=330.dp)
 }
 private fun scene(width:Int,theme:String,enlarged:Boolean=false) {
  install();app.diagnostics.enableSearchTracing();var enabled by mutableStateOf(true)
  val restore=StateRestorationTester(ui)
  val name="${if(width>600)"tablet" else "phone"}-$theme${if(enlarged)"-font200" else ""}"
  restore.setContent {
   val d=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(d.density,if(enlarged)2f else 1f)) {
    Box(Modifier.width(width.dp).height(650.dp).testTag("integration-canvas")) {
     SpotlightPanel(app,AppConfig(theme=theme,focusInterface=enabled,files=false,online=false,contacts=false,animations=false,learning=false),{},"Vivaldi",330f)
    }
   }
  }
  ui.waitUntil(15000){ui.onAllNodesWithTag("result:$pkg/Main").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithTag("result:$pkg/Main").performClick()
  if(before)ui.waitUntil(15000){ui.onAllNodesWithText("7.2-fixture").fetchSemanticsNodes().isNotEmpty()}
  else ui.waitUntil(15000){ui.onAllNodesWithTag("app-promoted-search").fetchSemanticsNodes().isNotEmpty()}
  capture(name,"inspector")
  if(!before) {
   ui.onNodeWithTag("app-promoted-search").performScrollTo().assertIsEnabled()
   val target=ui.onNodeWithTag("app-promoted-search").getUnclippedBoundsInRoot();assertTrue(target.bottom-target.top>=48.dp)
   if(enlarged){ui.onNodeWithTag("app-promoted-search").assertIsDisplayed();capture(name,"primary-visible")}
   ui.onAllNodesWithText("7.2-fixture").assertCountEquals(0)
   ui.onNodeWithTag("inspector-administration-toggle").performScrollTo().performClick()
   ui.onNodeWithText("7.2-fixture").performScrollTo().assertIsDisplayed()
   ui.onNodeWithText("Quitar favorita").assertDoesNotExist()
   ui.onNodeWithText("Añadir favorita").performScrollTo().assertIsDisplayed()
   restore.emulateSavedInstanceStateRestore();ui.waitForIdle()
   try{ui.waitUntil(15000){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();ui.onAllNodesWithTag("app-promoted-search").fetchSemanticsNodes().isNotEmpty()}}
   finally{java.io.File("build/focus-b-integration/trace-$name.txt").writeText(app.diagnostics.searchTraceReport())}
   ui.onNodeWithTag("inspector-metadata").assertExists()
   ui.waitUntil(15000){ui.onAllNodesWithText("7.2-fixture").fetchSemanticsNodes().isNotEmpty()}
   ui.onNodeWithTag("inspector-administration-toggle").performScrollTo().performClick()
   // The promoted search must not be duplicated by expanding the secondary catalog.
   ui.onNodeWithTag("inspector-more-actions").performScrollTo().performClick()
   ui.onAllNodes(hasText("Buscar en Vivaldi") and hasAnyAncestor(hasTestTag("app-capabilities"))).assertCountEquals(1)
   ui.onNodeWithTag("inspector-more-actions").performScrollTo().performClick()
   ui.waitUntil(15000){ui.onAllNodes(hasTestTag("command-primary") and hasText("Abrir aplicación")).fetchSemanticsNodes().isNotEmpty()}
   field().performClick().assertIsFocused()
   field().performKeyInput{keyDown(Key.CtrlLeft);pressKey(Key.K);keyUp(Key.CtrlLeft)}
   ui.waitUntil(15000){ui.onAllNodesWithTag("context-action-panel").fetchSemanticsNodes().isNotEmpty()}
   ui.onAllNodes(hasText("Buscar en Vivaldi") and hasAnyAncestor(hasTestTag("context-action-panel"))).assertCountEquals(1)
   ui.onNodeWithTag("context-action-panel").performKeyInput{pressKey(Key.Escape)}
   ui.waitUntil(15000){ui.onAllNodesWithTag("context-action-panel").fetchSemanticsNodes().isEmpty()}
   field().assertIsFocused().assertTextEquals("Vivaldi")
   ui.onNodeWithTag("app-promoted-search").performScrollTo().performClick()
  } else {
   ui.onNodeWithContentDescription("Cerrar inspector").performClick()
   ui.onNodeWithText("Buscar en Vivaldi").performClick();ui.onNodeWithText("Buscar en Vivaldi").performClick()
  }
  try{ui.waitUntil(15000){ui.onAllNodesWithTag("focus-prepare").fetchSemanticsNodes().isNotEmpty()}}
  finally{java.io.File("build/focus-b-integration/entry-$name.txt").writeText(ui.onRoot(useUnmergedTree=true).printToString()+"\n"+app.diagnostics.searchTraceReport())}
  field().performTextReplacement("android tablet")
  field().performTextInputSelection(TextRange(2,7))
  ui.onNodeWithTag("focus-prepare").performClick()
  ui.onNodeWithTag("focus-action-review").assertExists()
  capture(name,"review")
  if(!before) {
   ui.onNodeWithText("Abrir con").assertExists();ui.onNodeWithText("Sitio web").assertExists()
   ui.onNodeWithText("www.google.com").assertExists()
   restore.emulateSavedInstanceStateRestore();ui.waitForIdle()
   field().assertTextEquals("android tablet")
   assertEquals(TextRange(2,7),field().fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange])
   ui.onNodeWithTag("focus-action-review").assertDoesNotExist()
   field().performKeyInput{pressKey(Key.Escape)}
   field().assertTextEquals("Vivaldi").assertIsFocused()
   ui.runOnIdle{enabled=false};ui.waitForIdle()
   ui.onNodeWithTag("app-promoted-search").assertDoesNotExist()
   if(width==360 && theme=="light" && !enlarged) {
    field().assertTextEquals("Vivaldi").assertIsFocused()
    ui.runOnIdle{enabled=true};ui.waitForIdle()
    field().assertTextEquals("Vivaldi").assertIsFocused()
   }
  }
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneDark()=scene(360,"dark")
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneLight()=scene(360,"light")
 @Test fun tabletDark()=scene(960,"dark")
 @Test fun tabletLight()=scene(960,"light")
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneDarkText200()=scene(360,"dark",true)
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneLightText200()=scene(360,"light",true)
}
