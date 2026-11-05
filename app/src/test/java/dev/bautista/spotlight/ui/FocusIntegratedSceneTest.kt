package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.ActionCatalog
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Calls the production SpotlightPanel and its real NativeLaunchSession, never a test UI. */
@OptIn(ExperimentalTestApi::class,ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FocusIntegratedSceneTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 private var enabled by mutableStateOf(false)
 private var viewport by mutableStateOf(700f)
 private var closes=0
 private val restoration=androidx.compose.ui.test.junit4.StateRestorationTester(ui)
 private fun field()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
 private fun install(pkg:String,label:String) {
  val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=label;this.enabled=true;icon=0x7f010001}
  val ai=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=a;nonLocalizedLabel=label;this.enabled=true;exported=true}
  val ri=ResolveInfo().apply{activityInfo=ai};val pm=Shadows.shadowOf(app.packageManager)
  pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(ai);versionName="7.2-fixture"})
  val bitmap=android.graphics.Bitmap.createBitmap(96,96,android.graphics.Bitmap.Config.ARGB_8888)
  val canvas=android.graphics.Canvas(bitmap)
  val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply{color=if(label=="Spotify")android.graphics.Color.rgb(30,94,70) else android.graphics.Color.rgb(153,62,60)}
  canvas.drawRoundRect(0f,0f,96f,96f,22f,22f,paint)
  paint.color=android.graphics.Color.WHITE;paint.textSize=53f;paint.typeface=android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.BOLD);paint.textAlign=android.graphics.Paint.Align.CENTER
  canvas.drawText(label.take(1),48f,67f,paint)
  val icon=android.graphics.drawable.BitmapDrawable(app.resources,bitmap)
  pm.addDrawableResolution(pkg,a.icon,icon);pm.setApplicationIcon(pkg,icon);pm.addActivityIcon(android.content.ComponentName(pkg,"Main"),icon)

  val main=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
  pm.addResolveInfoForIntent(main,ri);pm.addResolveInfoForIntent(Intent(main).setPackage(pkg),ri)
  for(term in listOf("consulta","Luna","android tablet"))pm.addResolveInfoForIntent((ActionCatalog.searchAction(if(label=="Spotify")"spotify" else "vivaldi",pkg,term) as Action.InApp).intent,ri)
 }
 private fun capture(profile:String,scene:String) {
  ui.waitForIdle()
  val footer=if(scene=="menu")ui.onNode(hasText("Volver a resultados") and hasClickAction()).getUnclippedBoundsInRoot() else ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
  val root=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
  assertTrue(footer.bottom<=root.bottom);assertTrue(root.bottom-root.top<=viewport.dp)
  val surface=ui.onNodeWithTag("spotlight-bar").getUnclippedBoundsInRoot()
  java.io.File("build/focus-integrated/$profile-metrics.csv").apply{parentFile!!.mkdirs();if(scene=="home" && !enabled)writeText("mode,scene,windowHeightDp,headerHeightDp,viewportDp\n");appendText("${if(enabled)"focus" else "legacy"},$scene,${root.bottom.value-root.top.value-24},${if(scene=="menu")"" else surface.bottom.value-surface.top.value},$viewport\n")}

  ui.onNodeWithTag("integrated-canvas").captureRoboImage("build/focus-integrated/$profile-${if(enabled)"focus" else "legacy"}-$scene.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
 }
 private fun enter(name:String) {
  field().performTextReplacement(name)
  ui.waitUntil(15000){ui.onAllNodesWithText("Buscar en $name").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithText("Buscar en $name").performClick();ui.onNodeWithText("Buscar en $name").performClick()
  ui.waitUntil(15000){ui.onAllNodesWithTag("focus-prepare").fetchSemanticsNodes().isNotEmpty()}
 }
 private fun scene(width:Int,theme:String) {
  install("com.spotify.music","Spotify");install("com.vivaldi.browser","Vivaldi");app.search.invalidateApps()
  restoration.setContent {Box(Modifier.width(width.dp).height(700.dp).testTag("integrated-canvas")) {
   SpotlightPanel(app,AppConfig(theme=theme,focusInterface=enabled,files=false,contacts=false,online=false,animations=false,learning=false,
    favorites=setOf("com.spotify.music/Main","com.vivaldi.browser/Main")),{closes++},"",viewport)
  }}
  val profile="${if(width>600)"tablet" else "phone"}-$theme"
  ui.waitUntil(15000){ui.onAllNodesWithTag("result:suggest:app:com.spotify.music").fetchSemanticsNodes().isNotEmpty()}
  capture(profile,"home")
  field().performTextReplacement("Vivaldi")
  ui.waitUntil(15000){ui.onAllNodesWithTag("result:com.vivaldi.browser/Main").fetchSemanticsNodes().isNotEmpty()}
  ui.onNodeWithTag("result:com.vivaldi.browser/Main").performClick()
  ui.waitUntil(15000){ui.onAllNodesWithText("7.2-fixture").fetchSemanticsNodes().isNotEmpty()}
  capture(profile,"inspector")
  val previousBounds=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
  val previousHeight=previousBounds.bottom-previousBounds.top
  ui.runOnIdle{enabled=true};ui.waitForIdle();capture(profile,"inspector")
  if(width<600) {
   val nextBounds=ui.onNodeWithTag("spotlight-root").getUnclippedBoundsInRoot()
   assertTrue(nextBounds.bottom-nextBounds.top<previousHeight)
   ui.onAllNodesWithTag("result:com.vivaldi.browser/Main").assertCountEquals(0)
   ui.onNodeWithContentDescription("Cerrar inspector").performClick()
   field().assertTextEquals("Vivaldi").assertIsFocused()
   ui.onNodeWithTag("result:com.vivaldi.browser/Main").assertExists()
  }
  field().performTextReplacement("")
  ui.waitUntil(15000){ui.onAllNodesWithTag("result:suggest:app:com.spotify.music").fetchSemanticsNodes().isNotEmpty()}
  capture(profile,"home")
  enter("Spotify")
  ui.onAllNodes(hasSetTextAction()).assertCountEquals(1)
  val inputId=field().fetchSemanticsNode().id
  capture(profile,"spotify-context")
  field().performTextReplacement("Luna")
  ui.onNodeWithTag("focus-prepare").performClick()
  ui.waitUntil(15000){ui.onAllNodesWithTag("focus-action-review").fetchSemanticsNodes().isNotEmpty()}
  capture(profile,"spotify-review")
  assertEquals(inputId,field().fetchSemanticsNode().id)
  field().performKeyInput{pressKey(Key.Escape)}
  field().performKeyInput{pressKey(Key.Escape)}
  field().assertTextEquals("Spotify").assertIsFocused()
  field().performTextReplacement("Vivaldi")
  ui.waitUntil(15000){ui.onAllNodesWithText("Buscar en Vivaldi").fetchSemanticsNodes().isNotEmpty()}
  field().performTextInputSelection(TextRange(1,4))
  val cursor=field().fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange]
  ui.onNodeWithText("Buscar en Vivaldi").performClick();ui.onNodeWithText("Buscar en Vivaldi").performClick()
  ui.waitUntil(15000){ui.onAllNodesWithTag("focus-prepare").fetchSemanticsNodes().isNotEmpty()}
  field().performTextReplacement("android tablet");ui.onNodeWithTag("focus-prepare").performClick()
  ui.waitUntil(15000){ui.onAllNodesWithTag("focus-action-review").fetchSemanticsNodes().isNotEmpty()}
  capture(profile,"vivaldi-review")
  ui.runOnIdle{viewport=330f};ui.waitForIdle();capture(profile,"ime-budget")
  ui.runOnIdle{viewport=700f};ui.waitForIdle()
  ui.onNodeWithContentDescription("Volver a resultados").performClick()
  field().assertTextEquals("Vivaldi").assertIsFocused()
  assertEquals(cursor,field().fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange])
  val options=ui.onAllNodesWithTag("command-dock-menu")
  if(options.fetchSemanticsNodes().isNotEmpty())options.onFirst().performClick() else ui.onNodeWithContentDescription("Opciones de Spotlight").performClick()
  ui.onNodeWithText("Volver a resultados").assertIsDisplayed();capture(profile,"menu")
  ui.onNodeWithText("Volver a resultados").performClick();field().assertTextEquals("Vivaldi").assertIsFocused()
  assertEquals(0,closes);assertNull(Shadows.shadowOf(app).nextStartedActivity)
  if(width==360 && theme=="light") {
   enter("Vivaldi");field().assertTextEquals("android tablet")
   field().performKeyInput{pressKey(Key.Enter)}
   ui.onNodeWithTag("focus-action-review").assertExists()
   assertNull(Shadows.shadowOf(app).nextStartedActivity)
   restoration.emulateSavedInstanceStateRestore();ui.waitForIdle()
   field().assertTextEquals("android tablet")
   ui.onNodeWithTag("focus-action-review").assertDoesNotExist()
   ui.onNodeWithContentDescription("Volver a resultados").performClick()
   field().assertTextEquals("Vivaldi").assertIsFocused()
   ui.waitUntil(15000){ui.onAllNodesWithText("Buscar en Vivaldi").fetchSemanticsNodes().isNotEmpty()}
   ui.onNodeWithText("Buscar en Vivaldi").assertIsSelected()
   ui.onNodeWithText("Buscar en Vivaldi").performClick();ui.onNodeWithText("Buscar en Vivaldi").performClick()
   field().assertTextEquals("android tablet")
   field().performKeyInput{pressKey(Key.Enter)}
   ui.onNodeWithTag("focus-action-review").assertExists()
   field().performKeyInput{pressKey(Key.Enter)};ui.waitForIdle()
   assertNotNull(Shadows.shadowOf(app).nextStartedActivity)
   assertNull(Shadows.shadowOf(app).nextStartedActivity)
  }
 }
 @Test fun tabletDark()=scene(960,"dark")
 @Test fun tabletLight()=scene(960,"light")
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneDark()=scene(360,"dark")
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneLight()=scene(360,"light")
}
