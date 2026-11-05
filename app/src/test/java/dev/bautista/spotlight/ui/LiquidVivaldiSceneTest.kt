package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Same live search, selection, content and viewport; only presentation changes. */
@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LiquidVivaldiSceneTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private var face by mutableStateOf<LiquidTypeface?>(null)
 private var closes=0
 private fun field()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
 private fun scene(width:Int,theme:String) {
  val app=RuntimeEnvironment.getApplication() as SpotlightApp
  val pkg="com.vivaldi.browser"
  val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel="Vivaldi";enabled=true}
  val activity=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=a;nonLocalizedLabel="Vivaldi";enabled=true;exported=true}
  val pm=Shadows.shadowOf(app.packageManager)
  pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity);versionName="7.2-fixture"})
  pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=activity})
  app.search.invalidateApps()
  ui.setContent{CompositionLocalProvider(LocalLiquidScene provides face,LocalPremiumReview provides (face!=null)) {
   SpotlightTheme(theme){Surface{Box(Modifier.width(width.dp)) {
    SpotlightPanel(app,AppConfig(theme=theme,preset="glass",files=false,online=false,contacts=false,animations=false,learning=false),{closes++},"",700f)
   }}}
  }}
  field().performClick().performTextReplacement("Vivaldi")
  ui.waitUntil(15000){ui.onAllNodesWithTag("result:$pkg/Main").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithTag("result:$pkg/Main").performClick()
  ui.waitUntil(15000){ui.onAllNodesWithText("7.2-fixture").fetchSemanticsNodes().isNotEmpty()}
  ui.waitUntil(15000){ui.onAllNodes(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.ProgressBarRangeInfo,androidx.compose.ui.semantics.ProgressBarRangeInfo.Indeterminate)).fetchSemanticsNodes().isEmpty()}
  val profile="${if(width>600)"tablet" else "phone"}-$theme"
  for(mode in listOf("preview63","inter","geist")) {
   ui.runOnIdle{face=when(mode){"inter"->LiquidTypeface.Inter;"geist"->LiquidTypeface.Geist;else->null}}
   ui.waitForIdle()
   ui.onNodeWithText("7.2-fixture").assertExists()
   field().assertTextEquals("Vivaldi")
   ui.onNodeWithTag("spotlight-root").captureRoboImage("build/liquid-vivaldi/$profile-$mode.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
   val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
   val footer=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
   assertTrue(footer.bottom<=root.bottom);assertTrue(root.bottom-root.top<=700.dp)
   if(mode!="preview63") {
    ui.onNodeWithTag("liquid-app-identity").assertExists()
    ui.onNodeWithText(pkg).assertIsDisplayed()
   }
   assertEquals(0,closes);assertNull(Shadows.shadowOf(app).nextStartedActivity)
  }
  field().performClick().performKeyInput{pressKey(Key.Tab)}
  ui.waitForIdle()
  ui.onNodeWithTag("context-action-panel").assertExists()
  field().performKeyInput{pressKey(Key.Escape)}
  field().assertTextEquals("Vivaldi").assertIsFocused()
  assertEquals(0,closes);assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
 @Test fun tabletDark()=scene(960,"dark")
 @Test fun tabletLight()=scene(960,"light")
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneDark()=scene(360,"dark")
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneLight()=scene(360,"light")
}
