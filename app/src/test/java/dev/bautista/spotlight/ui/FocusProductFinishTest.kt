package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
class FocusProductFinishTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 private var finish by mutableStateOf(false)
 private val state=FocusSearchState()
 private val requested=mutableListOf<Action>()
 private val measurements=mutableListOf<String>()
 private fun field()=ui.onNodeWithTag("focus-query")
 private fun install(pkg:String,label:String,glyph:String,color:Int) {
  val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=label;enabled=true;this.icon=0x7f010001}
  val activity=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=a;nonLocalizedLabel=label;enabled=true;exported=true}
  val resolve=ResolveInfo().apply{activityInfo=activity}
  val pm=Shadows.shadowOf(app.packageManager)
  pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity);versionName="7.2-fixture"})
  val main=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
  pm.addResolveInfoForIntent(main,resolve);pm.addResolveInfoForIntent(Intent(main).setPackage(pkg),resolve)
  val bitmap=Bitmap.createBitmap(96,96,Bitmap.Config.ARGB_8888)
  val canvas=Canvas(bitmap)
  val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{this.color=color}
  canvas.drawRoundRect(0f,0f,96f,96f,22f,22f,paint)
  paint.color=android.graphics.Color.WHITE;paint.textSize=53f;paint.typeface=Typeface.create("sans-serif",Typeface.BOLD);paint.textAlign=Paint.Align.CENTER
  canvas.drawText(glyph,48f,67f,paint)
  val icon=BitmapDrawable(app.resources,bitmap)
  pm.addDrawableResolution(pkg,a.icon,icon)
  pm.setApplicationIcon(pkg,icon);pm.addActivityIcon(android.content.ComponentName(pkg,"Main"),icon)
  val topic=if(pkg=="com.spotify.music")"spotify" else "vivaldi"
  for(term in listOf("consulta","Luna","android tablet"))pm.addResolveInfoForIntent((ActionCatalog.searchAction(topic,pkg,term) as Action.InApp).intent,resolve)
 }
 private fun capture(profile:String,scene:String) {
  val nodeId=field().fetchSemanticsNode().id
  for(after in listOf(false,true)) {
   ui.runOnIdle{finish=after};ui.waitForIdle()
   assertEquals("The input node must survive a presentation switch",nodeId,field().fetchSemanticsNode().id)
   val mode=if(after)"after" else "6d9289c"
   val bounds=ui.onNodeWithTag("focus-window").getUnclippedBoundsInRoot()
   val footer=ui.onNodeWithTag("focus-footer").getUnclippedBoundsInRoot()
   assertTrue(footer.bottom<=bounds.bottom)
   assertTrue(bounds.bottom<=700.dp)
   measurements+="$profile,$scene,$mode,${(bounds.bottom-bounds.top).value},${(bounds.bottom-bounds.top).value+24}"
   ui.onNodeWithTag("finish-canvas").captureRoboImage("build/focus-finish/$profile-$mode-$scene.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  }
 }
 private fun scene(width:Int,theme:String) {
  install("com.spotify.music","Spotify","S",android.graphics.Color.rgb(30,94,70))
  install("com.vivaldi.browser","Vivaldi","V",android.graphics.Color.rgb(153,62,60))
  app.search.invalidateApps()
  val config=AppConfig(theme=theme,preset="glass",files=false,online=false,contacts=false,animations=false,learning=false,
   favorites=setOf("com.spotify.music/Main","com.vivaldi.browser/Main"))
  ui.setContent {
   CompositionLocalProvider(LocalFocusProductFinish provides finish,LocalLiquidScene provides LiquidTypeface.Geist,LocalPremiumReview provides true) {
    SpotlightTheme(theme) {
     val palette=FocusProductType.palette(MaterialTheme.colorScheme)
     for(surface in listOf(palette.surface,palette.surfaceContainerLow,palette.surfaceContainerHigh)) {
      assertTrue(InspectorAccent.contrast(palette.onSurface,surface)>=4.5f)
      assertTrue(InspectorAccent.contrast(palette.onSurfaceVariant,surface)>=4.5f)
     }
     Box(Modifier.width(width.dp).height(700.dp).background(if(theme=="dark")Color(0xFF101214) else Color(0xFFE9E6E0)).testTag("finish-canvas")) {
      FocusSearchReview(app,config,state){requested+=it}
      Text("Monogramas S/V sintéticos · captura Compose\nContratos de fixture; integración global pendiente",
       Modifier.align(androidx.compose.ui.Alignment.BottomStart).padding(horizontal=16.dp,vertical=10.dp),
       style=FocusSearchType.metadata,color=if(theme=="dark")Color(0xFFAAB0B8) else Color(0xFF595C60))
     }
    }
   }
  }
  val profile="${if(width>600)"tablet" else "phone"}-$theme"
  ui.waitUntil(15000){state.rows.isNotEmpty()}
  assertTrue(state.rows.size<=3)
  val home=state.rows.first()
  val homePackage=(home.action as Action.Launch).intent.component!!.packageName
  val expected=app.packageManager.getApplicationIcon(homePackage) as BitmapDrawable
  assertTrue("The home suggestion must use the installed fixture icon",(home.icon as BitmapDrawable).bitmap.sameAs(expected.bitmap))
  capture(profile,"01-home")
  for((name,term) in listOf("Spotify" to "Luna","Vivaldi" to "android tablet")) {
   field().performTextReplacement(name)
   ui.waitUntil(15000){state.rows.firstOrNull()?.title==name && state.rows.any{it.action is Action.ComposeSearch}}
   capture(profile,"02-${name.lowercase()}-global")
   field().performTextInputSelection(TextRange(1,4));val global=state.global
   val pkg=if(name=="Spotify")"com.spotify.music" else "com.vivaldi.browser"
   val index=state.rows.indexOfFirst{(it.action as? Action.ComposeSearch)?.packageName==pkg}
   ui.onNodeWithTag("focus-result-$index").performClick()
   ui.waitUntil(15000){state.context!=null}
   assertEquals(app.packageManager.getApplicationLabel(app.packageManager.getApplicationInfo(pkg,0)).toString(),state.context!!.name)
   assertSame(app.packageManager.getApplicationIcon(pkg),state.context!!.item.icon)
   field().assertIsFocused();ui.onAllNodes(hasSetTextAction()).assertCountEquals(1)
   capture(profile,"03-${name.lowercase()}-empty")
   ui.onNodeWithTag("focus-information").performClick()
   ui.onNodeWithTag("focus-public-information").assertIsDisplayed()
   ui.onNodeWithTag("focus-information").performClick()
   field().performTextReplacement(term)
   ui.onNodeWithTag("focus-prepare").performClick();ui.waitUntil(15000){state.review!=null}
   val plan=state.review!!
   assertEquals(pkg,plan.intent.`package`)
   assertTrue(IntegrationRegistry(app).resolves(plan.intent))
   capture(profile,"04-${name.lowercase()}-prepared")
   ui.onAllNodesWithText(term).assertCountEquals(1)
   ui.onNodeWithTag("focus-action-review").assertIsDisplayed()
   ui.onNodeWithText(plan.intent.data!!.host!!).assertIsDisplayed()
   assertEquals(0,requested.size)
   ui.onNodeWithTag("focus-back").performClick()
   assertEquals(global,state.global);assertEquals(index,state.selected)
   field().assertTextEquals(name).assertIsFocused()
   assertNull(state.review)
  }
  java.io.File("build/focus-finish/$profile-metrics.csv").apply {parentFile!!.mkdirs();writeText("profile,scene,mode,surfaceHeightDp,footprintHeightDp\n"+measurements.joinToString("\n")+"\n")}
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
 @Test fun tabletDark()=scene(960,"dark")
 @Test fun tabletLight()=scene(960,"light")
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneDark()=scene(360,"dark")
 @Test @Config(qualifiers="w390dp-h844dp") fun phoneLight()=scene(360,"light")
}
