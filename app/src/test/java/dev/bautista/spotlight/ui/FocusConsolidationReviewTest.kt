package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.pm.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.ActionCatalog
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*

/** Presentation proposals ONLY. No production flag, executor, transport or persisted preference changes. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
class FocusConsolidationReviewTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 private val pkg="com.vivaldi.browser"
 private fun install() {
  val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel="Vivaldi";enabled=true}
  val ai=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=a;nonLocalizedLabel="Vivaldi";enabled=true;exported=true}
  val pm=Shadows.shadowOf(app.packageManager);val ri=ResolveInfo().apply{activityInfo=ai}
  pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(ai);versionName="7.2-fixture"})
  val main=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
  pm.addResolveInfoForIntent(main,ri);pm.addResolveInfoForIntent(Intent(main).setPackage(pkg),ri)
  for(q in listOf("consulta","android tablet"))pm.addResolveInfoForIntent((ActionCatalog.searchAction("vivaldi",pkg,q) as Action.InApp).intent,ri)
 }
 private fun capture(name:String) {
  ui.waitForIdle()
  ui.onNodeWithTag("review-canvas").captureRoboImage("build/focus-consolidation/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  val b=ui.onNodeWithTag("review-canvas").getUnclippedBoundsInRoot()
  java.io.File("build/focus-consolidation/metrics.csv").apply{parentFile!!.mkdirs();appendText("$name,${b.right.value-b.left.value},${b.bottom.value-b.top.value}\n")}
 }
 private fun paired(width:Int,theme:String) {
  install()
  var proposed by mutableStateOf(false)
  var contextual by mutableStateOf(false)
  var draft by mutableStateOf(Action.ComposeSearch("vivaldi",pkg,"android tablet"))
  val review=FocusSearchReviewController()
  var requests=0;var info=0;var favorites=0
  val item=Result("$pkg/Main","Vivaldi","Aplicación","Aplicaciones",4000,Action.Launch(Intent(Intent.ACTION_MAIN).setPackage(pkg)))
  val config=AppConfig(theme=theme,focusInterface=true,animations=false,online=false,contacts=false,files=false)
  val profile="${if(width>400)"tablet-inspector" else "phone"}-$theme"
  ui.setContent {
   SpotlightTheme(theme,config) {
    Surface(Modifier.width(width.dp).testTag("review-canvas"),shape=RoundedCornerShape(FocusDesign.radius),color=MaterialTheme.colorScheme.surfaceContainerLow) {
     Column {
      val focus=remember{FocusRequester()}
      SpotlightBar(if(contextual)draft.query else "Vivaldi",{if(contextual){draft=draft.copy(query=it);review.invalidate()}},"Todo",{},{},{},focus,1f,width<400,
       animations=false,onBack=if(contextual)({contextual=false}) else null,showOptionsButton=true,
       contextualLabel=if(contextual)"Vivaldi" else null,contextualPlaceholder=if(contextual)"Buscar en la web" else null)
      HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
      if(contextual) {
       if(!proposed || review.prepared==null)FocusSearchTaskBody(app,draft,review,false,0)
       else {
        // B: only the two review labels change. The real prepared Action remains untouched.
        val plan=review.prepared as Action.InApp
        Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp).testTag("review-explicit-destination"),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
         Column(Modifier.weight(1f)){Text("Abrir con",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("Vivaldi",style=MaterialTheme.typography.titleSmall)}
         Column(Modifier.weight(1f)){Text("Sitio web",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(plan.intent.data?.host.orEmpty(),style=MaterialTheme.typography.bodySmall)}
        }
       }
       FocusSearchTaskFooter(draft,review,false,{}, {requests++})
      } else {
       Column(Modifier.padding(horizontal=20.dp,vertical=8.dp)) {
        if(!proposed)ApplicationInspector(item,pkg,true,false,emptyList(),{},{favorites++},{info++},{},{},embedded=true,
         capabilitiesContent={AppCapabilityActions(pkg,appInfoAlreadyVisible=true,compact=width<400){}})
        else ReviewInspector(item,width,{contextual=true;review.invalidate()},{info++},{favorites++})

       }
       GlassContextFooter(false,true,"Abrir aplicación",{},{},onOptions={})
      }
     }
    }
   }
  }
  ui.waitUntil(15000){ui.onAllNodesWithText("7.2-fixture").fetchSemanticsNodes().isNotEmpty()}
  capture("$profile-inspector-A")
  ui.runOnIdle{proposed=true};capture("$profile-inspector-B")
  val target=ui.onNodeWithTag("review-search-entry").getUnclippedBoundsInRoot();assertTrue(target.bottom-target.top>=48.dp)
  ui.onNodeWithText("Información y favoritos").performClick()
  ui.onNodeWithText("Versión · 7.2-fixture").assertIsDisplayed()
  ui.onNodeWithText("Quitar favorita").assertIsDisplayed()
  ui.onNodeWithText("Ocultar información y favoritos").performClick()
  ui.onNodeWithTag("review-search-entry").performClick()
  ui.onNode(hasSetTextAction()).assertTextEquals("android tablet")
  ui.onNodeWithTag("focus-prepare").performClick()
  assertNotNull(review.prepared)
  val prepared=review.prepared
  ui.runOnIdle{proposed=false};capture("$profile-context-A")
  ui.runOnIdle{proposed=true};capture("$profile-context-B")
  assertSame(prepared,review.prepared)
  ui.onNode(hasText("Vivaldi") and hasAnyAncestor(hasTestTag("review-explicit-destination"))).assertExists()
  ui.onNodeWithText("www.google.com").assertExists()
  ui.onNodeWithTag("focus-prepare").performClick();assertEquals(1,requests);assertNull(review.prepared)
  ui.onNode(hasSetTextAction()).performTextReplacement("consulta cambiada")
  assertNull(review.prepared)
  ui.onNodeWithContentDescription("Volver a resultados").performClick()
  ui.onNodeWithTag("review-search-entry").assertExists()
  assertEquals(0,info);assertEquals(0,favorites)
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
 @Test fun pairedPhoneDark()=paired(360,"dark")
 @Test fun pairedPhoneLight()=paired(360,"light")
 @Test fun pairedTabletDark()=paired(470,"dark")
 @Test fun pairedTabletLight()=paired(470,"light")
 @Composable private fun ReviewInspector(item:Result,width:Int,onSearch:()->Unit,onInfo:()->Unit,onFavorite:()->Unit) {
  var details by remember{mutableStateOf(false)}
  Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(GlassTokens.inspectorGap(LocalAppearance.current))) {
   Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
    ResultIcon(item,Modifier.size(40.dp))
    Column{Text(item.title,style=MaterialTheme.typography.titleLarge);Text("Aplicación instalada",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
   }
   TextButton(onClick=onSearch,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("review-search-entry"),contentPadding=PaddingValues(horizontal=2.dp)) {
    IosCommandIcon(CommandIcons.Search,null,Modifier.size(20.dp));Spacer(Modifier.width(10.dp))
    Text("Buscar en Vivaldi",Modifier.weight(1f),style=MaterialTheme.typography.titleSmall)
   }
   AppCapabilityActions(pkg,appInfoAlreadyVisible=true,compact=width<400){}
   TextButton(onClick={details=!details},modifier=Modifier.heightIn(min=48.dp),contentPadding=PaddingValues(horizontal=2.dp)) {
    Text(if(details)"Ocultar información y favoritos" else "Información y favoritos",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
   }
   if(details)Column {
    TextButton(onClick=onInfo,modifier=Modifier.heightIn(min=48.dp)){Text("Ver información de la app")}
    TextButton(onClick=onFavorite,modifier=Modifier.heightIn(min=48.dp)){Text("Quitar favorita")}
    Text("Versión · 7.2-fixture",style=MaterialTheme.typography.bodySmall)
    Text("Paquete · $pkg",style=MaterialTheme.typography.bodySmall)
   }
  }
 }
 private fun actualActions(theme:String) {
  var copies=0;var closes=0
  val focus=FocusRequester()
  val actions=listOf(ContextAction("Copiar resultado","4",CommandIcons.ContentCopy,{copies++},id="copy",group=ContextActionGroup.Primary),
   ContextAction("Copiar expresión","2+2",CommandIcons.ContentCopy,{},id="expression"))
  ui.setContent {SpotlightTheme(theme,AppConfig(theme=theme,focusInterface=true,animations=false)) {
   Surface(Modifier.width(344.dp).testTag("review-canvas"),shape=RoundedCornerShape(16.dp)) {ActionPanelView(actions,focus,onClose={closes++})}
  }}
  ui.onNodeWithText("Copiar resultado").assertIsFocused()
  capture("actual-action-panel-$theme")
  ui.onNodeWithText("Copiar resultado").performKeyInput{pressKey(Key.Tab)}
  ui.onNodeWithText("Copiar expresión").assertIsFocused().performKeyInput{keyDown(Key.ShiftLeft);pressKey(Key.Tab);keyUp(Key.ShiftLeft)}
  ui.onNodeWithText("Copiar resultado").assertIsFocused().performKeyInput{pressKey(Key.Enter)}
  assertEquals(1,copies)
  ui.onNodeWithText("Copiar resultado").performKeyInput{pressKey(Key.Escape)};assertEquals(1,closes)
 }
 @Test fun actualActionPanelDark()=actualActions("dark")
 @Test fun actualActionPanelLight()=actualActions("light")
}
