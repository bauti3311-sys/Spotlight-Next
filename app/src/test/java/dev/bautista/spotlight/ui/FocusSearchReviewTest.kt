package dev.bautista.spotlight.ui

import android.content.Intent
import android.net.Uri
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.search.*
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
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
class FocusSearchReviewTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 private var mode by mutableStateOf("preview63")
 private val state=FocusSearchState()
 private val requested=mutableListOf<Action>()
 private val info=mutableMapOf<String,ResolveInfo>()
 private var baselineCloses=0
 private val metrics=mutableListOf<String>()
 private fun install(pkg:String,label:String,publicLink:Boolean=true) {
  val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=label;enabled=true}
  val activity=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=a;nonLocalizedLabel=label;enabled=true;exported=true}
  val r=ResolveInfo().apply{activityInfo=activity};info[pkg]=r
  val pm=Shadows.shadowOf(app.packageManager)
  pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity);versionName="7.2-fixture"})
  val main=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
  pm.addResolveInfoForIntent(main,r);pm.addResolveInfoForIntent(Intent(main).setPackage(pkg),r)
  if(publicLink)for(term in listOf("consulta","Luna","android tablet")) {
   val topic=if(pkg=="com.spotify.music")"spotify" else "vivaldi"
   pm.addResolveInfoForIntent((ActionCatalog.searchAction(topic,pkg,term) as Action.InApp).intent,r)
  }
 }
 private fun fixtures() {install("com.spotify.music","Spotify");install("com.vivaldi.browser","Vivaldi");app.search.invalidateApps()}
 private fun globalField()=ui.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("spotlight-bar")))
 private fun current()=ui.onNodeWithTag("focus-query")
 private fun settle(name:String) {
  ui.waitUntil(15000){state.rows.firstOrNull()?.title==name && state.rows.any{it.action is Action.ComposeSearch}}
 }
 private fun enter(name:String) {
  val pkg=if(name=="Spotify")"com.spotify.music" else "com.vivaldi.browser"
  val i=state.rows.indexOfFirst{(it.action as? Action.ComposeSearch)?.packageName==pkg}
  assertTrue(i>=0);ui.onNodeWithTag("focus-result-$i").performClick()
  ui.waitUntil(15000){state.context?.name==name}
  current().assertIsFocused()
 }
 private fun capture(profile:String,scene:String,tag:String) {
  ui.waitForIdle()
  val bounds=ui.onNodeWithTag(tag).getUnclippedBoundsInRoot()
  val surface=if(mode=="preview63")(bounds.bottom-bounds.top).value-24f else (bounds.bottom-bounds.top).value
  metrics+="$profile,$mode,$scene,$surface,${surface+24},${(if(profile.startsWith("tablet"))928 else 328)*surface}"
  ui.onNodeWithTag("focus-canvas").captureRoboImage("build/focus-search/$profile-$mode-$scene.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
 }
 private fun setup(width:Int,theme:String,many:Boolean=false,fontScale:Float=1f) {
  fixtures();if(many){repeat(12){install("fixture.spotify.studio${it}","Spotify Studio $it",false)};app.search.invalidateApps()}
  val config=AppConfig(theme=theme,preset="glass",files=false,online=false,contacts=false,animations=false,learning=false,
   favorites=setOf("com.spotify.music/Main","com.vivaldi.browser/Main"))
  ui.setContent {
   val baseDensity=androidx.compose.ui.platform.LocalDensity.current
   CompositionLocalProvider(LocalFocusProductFinish provides (System.getenv("SPOTLIGHT_FOCUS_FINISH")=="true"),androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(baseDensity.density,fontScale),LocalLiquidScene provides (if(mode=="preview63")null else LiquidTypeface.Geist),LocalPremiumReview provides (mode!="preview63")) {
    SpotlightTheme(theme) {
     Box(Modifier.width(width.dp).height(700.dp).background(if(theme=="dark")Color(0xFF101214) else Color(0xFFE9E6E0)).testTag("focus-canvas")) {
      if(mode=="preview63")SpotlightPanel(app,config,{baselineCloses++},"",700f)
      else if(mode=="B")StructuralVivaldiPrototype(StructuralOption.Focus,state.rows,"7.2-fixture",{requested+=it.action},{},{},{requested+=it})
      else FocusSearchReview(app,config,state){requested+=it}
      androidx.compose.material3.Text(
       when(mode){"preview63"->"Referencia 63 · render Compose con fixtures\nNo es una captura física del APK";"B"->"B771 anterior · escena fija Vivaldi\nSin búsqueda contextual implementada";else->"Revisión Compose · contratos públicos en fixture\nIntegración global y dispositivo pendientes"},
       Modifier.align(androidx.compose.ui.Alignment.BottomStart).padding(horizontal=16.dp,vertical=10.dp),
       style=FocusSearchType.metadata,color=if(theme=="dark")Color(0xFFAAB0B8) else Color(0xFF595C60))
     }
    }
   }
  }
 }
 private fun baseline(profile:String) {
  ui.waitUntil(15000){ui.onAllNodesWithTag("result:suggest:app:com.spotify.music").fetchSemanticsNodes().isNotEmpty()}
  capture(profile,"01-home","spotlight-root")
  for((name,prefix) in listOf("Spotify" to "02","Vivaldi" to "04")) {
   globalField().performTextReplacement(name)
   ui.waitUntil(15000){ui.onAllNodesWithText("Buscar en $name").fetchSemanticsNodes().isNotEmpty()}
   capture(profile,"$prefix-global-${name.lowercase()}","spotlight-root")
   ui.onNodeWithText("Buscar en $name").performClick();ui.onNodeWithText("Buscar en $name").performClick()
   ui.waitUntil(15000){ui.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size>=2}
   capture(profile,if(name=="Spotify")"03-context-spotify" else "05-context-vivaldi","spotlight-root")
   ui.onAllNodesWithText("Volver").onLast().performClick()
   globalField().assertTextEquals(name)
  }
  globalField().performClick();capture(profile,"06-return","spotlight-root")
  assertEquals(0,baselineCloses)
 }
 private fun scene(width:Int,theme:String) {
  setup(width,theme)
  val profile="${if(width>600)"tablet" else "phone"}-$theme"
  baseline(profile)
  ui.runOnIdle{mode="review"}
  ui.waitUntil(15000){state.rows.isNotEmpty()}
  assertTrue(state.rows.size<=3);capture(profile,"01-home","focus-window")
  for((name,prefix,term) in listOf(Triple("Spotify","02","Luna"),Triple("Vivaldi","04","android tablet"))) {
   current().performTextReplacement(name);settle(name)
   assertEquals(name,state.rows.first().title)
   capture(profile,"$prefix-global-${name.lowercase()}","focus-window")
   current().performTextInputSelection(TextRange(1,4))
   val selection=state.global.selection
   enter(name);val selected=state.selected
   ui.onAllNodes(hasSetTextAction()).assertCountEquals(1)
   current().assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.EditableText,androidx.compose.ui.text.AnnotatedString("")))
   ui.onNodeWithText(if(name=="Spotify")"Buscar canción o artista" else "Buscar en la web").assertIsDisplayed()
   capture(profile,if(name=="Spotify")"03-context-spotify" else "05-context-vivaldi","focus-window")
   current().performTextReplacement(term)
   ui.onNodeWithTag("focus-prepare").performClick()
   ui.waitUntil(15000){state.review!=null}
   val plan=state.review!!
   assertEquals(if(name=="Spotify")"com.spotify.music" else "com.vivaldi.browser",plan.intent.`package`)
   assertEquals(Intent.ACTION_VIEW,plan.intent.action)
   assertTrue(IntegrationRegistry(app).resolves(plan.intent))
   assertEquals(0,requested.size)
   current().performKeyInput{pressKey(Key.Escape)}
   assertNull(state.review)
   ui.onNodeWithTag("focus-back").performClick()
   current().assertTextEquals(name).assertIsFocused()
   assertEquals(selection,state.global.selection);assertEquals(selected,state.selected)
   if(name=="Vivaldi")capture(profile,"06-return","focus-window")
  }
  ui.runOnIdle{mode="B"};capture(profile,"04-vivaldi-supported-context","prototype-window")
  java.io.File("build/focus-search/$profile-metrics.csv").apply{parentFile!!.mkdirs();writeText("profile,mode,scene,surfaceHeightDp,footprintHeightDp,surfaceAreaDp2\n"+metrics.joinToString("\n")+"\n")}
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
 @Test fun tabletDark()=scene(960,"dark")
 @Test fun tabletLight()=scene(960,"light")
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneDark()=scene(360,"dark")
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneLight()=scene(360,"light")
 @Test fun scrollDraftAndReceiverRevocation() {
  mode="review";setup(960,"dark",true)
  current().performTextReplacement("Spotify");settle("Spotify")
  ui.waitUntil(15000){state.rows.size>8}
  val i=state.rows.indexOfFirst{it.action is Action.ComposeSearch};assertTrue(i>=0)
  ui.runOnIdle {state.selected=i}
  runBlocking{state.list.scrollToItem(2,13)}
  val before=state.list.firstVisibleItemIndex to state.list.firstVisibleItemScrollOffset
  current().performKeyInput{pressKey(Key.Enter)}
  ui.waitUntil(15000){state.context!=null}
  current().performTextReplacement("Luna")
  ui.onNodeWithTag("focus-back").performClick()
  assertEquals(before,state.list.firstVisibleItemIndex to state.list.firstVisibleItemScrollOffset)
  assertEquals(i,state.selected);current().assertTextEquals("Spotify").assertIsFocused()
  current().performKeyInput{pressKey(Key.Enter)}
  ui.waitUntil(15000){state.context!=null};current().assertTextEquals("Luna")
  ui.onNodeWithTag("focus-prepare").performClick();ui.waitUntil(15000){state.review!=null}
  val intent=state.review!!.intent
  Shadows.shadowOf(app.packageManager).removeResolveInfosForIntent(intent,"com.spotify.music")
  ui.onNodeWithTag("focus-prepare").performClick();ui.waitUntil(15000){state.error.isNotBlank()}
  assertEquals(0,requested.size);assertNull(state.review)
 }
 @Test @Config(qualifiers="w390dp-h844dp") fun unavailableContextDoesNotPretendSupport() {
  mode="review";setup(360,"light")
  current().performTextReplacement("Vivaldi");settle("Vivaldi")
  val probe=(ActionCatalog.searchAction("vivaldi","com.vivaldi.browser","consulta") as Action.InApp).intent
  Shadows.shadowOf(app.packageManager).removeResolveInfosForIntent(probe,"com.vivaldi.browser")
  val i=state.rows.indexOfFirst{it.action is Action.ComposeSearch}
  ui.onNodeWithTag("focus-result-$i").performClick()
  ui.waitUntil(15000){state.error.isNotBlank()}
  assertNull(state.context);assertEquals(0,requested.size)
  ui.onAllNodes(hasSetTextAction()).assertCountEquals(1)
 }
 @Test fun confirmationUsesOneOriginalPublicContract() {
  mode="review";setup(960,"dark")
  current().performTextReplacement("Spotify");settle("Spotify");enter("Spotify")
  current().performTextReplacement("Luna")
  current().performImeAction()
  ui.waitUntil(15000){state.review!=null}
  val prepared=state.review!!
  assertEquals("https://open.spotify.com/search/Luna",prepared.intent.data.toString())
  assertEquals(0,requested.size)
  ui.onNodeWithTag("focus-prepare").performClick()
  assertEquals(1,requested.size)
  assertTrue(requested.single()===prepared)
  ui.onNodeWithTag("focus-prepare").performClick()
  ui.waitUntil(15000){state.review!=null}
  assertEquals(1,requested.size)
  ui.onNodeWithTag("focus-back").performClick()
  assertNull(state.review)
  enter("Spotify");assertNull(state.review)
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
 @Test @Config(qualifiers="w390dp-h844dp") fun enlargedTextPreservesOneFieldAndFooter() {
  mode="review";setup(360,"light",fontScale=1.6f)
  current().performTextReplacement("Vivaldi");settle("Vivaldi");enter("Vivaldi")
  ui.onAllNodes(hasSetTextAction()).assertCountEquals(1)
  current().assertIsFocused()
  ui.onNodeWithText("Buscar en la web").assertIsDisplayed()
  ui.onNodeWithTag("focus-back").assertIsDisplayed()
  val footer=ui.onNodeWithTag("focus-footer").getUnclippedBoundsInRoot()
  assertTrue(footer.bottom<=700.dp)
  ui.onNodeWithTag("focus-back").performClick()
  current().assertTextEquals("Vivaldi").assertIsFocused()
 }

}
