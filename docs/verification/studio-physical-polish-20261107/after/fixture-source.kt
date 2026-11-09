package dev.bautista.spotlight.ui

import android.graphics.Rect
import android.view.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.*
import dev.bautista.spotlight.overlay.ActionPopupHost
import dev.bautista.spotlight.integrations.spotify.SpotifyConnectionState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.util.ReflectionHelpers
import java.io.File

/** Production components on a labelled busy backdrop; not an APK or a native One UI screenshot. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1100dp-h900dp-mdpi",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
class StudioPhysicalPolishTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=ui.activity.application as SpotlightApp
 private data class Profile(val id:String,val w:Int,val h:Int,val theme:String,val scale:Float)
 private var profile by mutableStateOf(Profile("tablet-dark-100",1000,720,"dark",1f))
 private var scene by mutableStateOf("resultados")
 private var menu by mutableStateOf(Rect())
 private var menuCap by mutableIntStateOf(1)
 private var showMenu by mutableStateOf(false)
 private var panel=Rect()
 private var effects=0
 private val target=Result("com.spotify.music/Main","Spotify","","Aplicaciones",2000,
  Action.Launch(android.content.Intent().setComponent(android.content.ComponentName("com.spotify.music","Main"))))
 private val results=listOf(target,Result("search","Buscar en Spotify","Ingresar búsqueda aquí","Acciones",1,Action.ComposeSearch("spotify","com.spotify.music","")))
 private val actions=listOf(
  ContextAction("Abrir","",CommandIcons.Apps,{effects++},id="open",group=ContextActionGroup.Primary),
  ContextAction("Buscar archivos","",CommandIcons.Search,{effects++},id="files"),
  ContextAction("Buscar en Spotify","",CommandIcons.Search,{effects++},id="search"),
  ContextAction("Quitar favorita","",CommandIcons.Apps,{effects++},id="favorite",group=ContextActionGroup.Organize),
  ContextAction("Fijar consulta","",CommandIcons.Apps,{effects++},id="pin",group=ContextActionGroup.Organize),
  ContextAction("Copiar…","",CommandIcons.ContentCopy,{effects++},id="copy",group=ContextActionGroup.Text),
  ContextAction("Permisos y componentes públicos","",CommandIcons.Apps,{effects++},id="permissions",group=ContextActionGroup.System),
  ContextAction("Información de la app","Intervención en Android",CommandIcons.Apps,{effects++},id="info",group=ContextActionGroup.System))
 private fun c()=AppConfig(theme=profile.theme,focusInterface=true,focusTypeface="geist",preset="glass",opacity=.70f,animations=false,files=false,contacts=false,online=false)
 private fun measureMenu(p:Profile) {
  ui.runOnIdle {
   val safe=Rect(0,24,p.w,p.h-24)
   val anchor=object:View(ui.activity){override fun getWindowVisibleDisplayFrame(outRect:Rect){outRect.set(safe)}}
   ui.activity.addContentView(anchor,ViewGroup.LayoutParams(1,1))
   val host=ActionPopupHost(ui.activity,anchor,{},{},studio=true)
   host.setViewTreeLifecycleOwner(ui.activity);host.setViewTreeViewModelStoreOwner(ui.activity);host.setViewTreeSavedStateRegistryOwner(ui.activity)
   try {
    host.show(panel,minOf(360,panel.width()-8),minOf(576,p.h-16))
    val params=host.layoutParams as WindowManager.LayoutParams
    menu=Rect(params.x,params.y,params.x+params.width,params.y+host.maximumHeight)
    menuCap=host.maximumHeight;showMenu=true
   } finally {host.close();(anchor.parent as ViewGroup).removeView(anchor)}
  }
  ui.waitForIdle()
 }
 @Composable private fun Scene() {
  val p=profile;val config=c()
  CompositionLocalProvider(LocalDensity provides Density(1f,p.scale)) {
   SpotlightTheme(p.theme,config) {
    val dark=p.theme=="dark"
    Box(Modifier.size(p.w.dp,p.h.dp).background(if(dark)Color.Black else Color.White).testTag("physical-scene")) {
     Column {repeat(36){Text("APLICACIÓN DE FONDO · Texto ficticio detrás del buscador · 0123456789",
      color=if(dark)Color.White else Color.Black,style=MaterialTheme.typography.bodyLarge)}}
     Column(Modifier.offset(x=if(p.w>600)(p.w*.09f).dp else 16.dp,y=64.dp)
      .width(if(p.w>600)(p.w*.82f).dp else (p.w-32).dp).glassSurface(config,true).testTag("polish-panel")
      .onGloballyPositioned{val b=it.localToRoot(androidx.compose.ui.geometry.Offset.Zero);panel=Rect(b.x.toInt(),b.y.toInt(),(b.x+it.size.width).toInt(),(b.y+it.size.height).toInt())}) {
      StudioSurface("search"){SpotlightBar(if(scene=="formulario")"" else "spotify",{},"Todo",{},{},{},remember{FocusRequester()},1f,p.w<600,animations=false)}
      Column(Modifier.fillMaxWidth().studioContentBacking().testTag("polish-body")) {
       if(scene=="formulario")SpotifySearchPanel(app,externalQuery="",searchTrigger=0)
       else SpotlightResultList(results,config,"spotify",0,rememberLazyListState(),360.dp,false,false,true,false,emptySet(),{true},{},{_,_->},{_,_->},{},{})
      }
      if(scene=="formulario")FocusSearchTaskFooter(Action.ComposeSearch("spotify","com.spotify.music",""),remember{FocusSearchReviewController()},true,{effects++},{effects++})
      else GlassContextFooter(false,true,"Abrir aplicación",{effects++},{effects++},onOptions=null)
     }
     if(showMenu)Box(Modifier.offset(menu.left.dp,menu.top.dp).width(menu.width().dp).heightIn(max=menuCap.dp).padding(8.dp)) {
      Surface(color=GlassTokens.actionColor(config,dark),shape=androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
       modifier=Modifier.fillMaxWidth().heightIn(max=(menuCap-16).dp)) {
       ActionPanelView(actions,remember{FocusRequester()},{effects++},contextResult=target)
      }
     }
    }
   }
  }
 }
 @Test fun wideFocusFormUsesOneToolbarRowWhenItFits() {
  runBlocking{app.spotify.configure("a".repeat(32));app.spotify.inspect()}
  ui.setContent{CompositionLocalProvider(LocalDensity provides Density(1f,1f)){SpotlightTheme("dark",c()){
   Box(Modifier.width(820.dp)){SpotifySearchPanel(app,externalQuery="",searchTrigger=0)}
  }}}
  ui.waitForIdle()
  ui.runOnIdle{ReflectionHelpers.getField<MutableStateFlow<SpotifyConnectionState>>(app.spotify,"mutableState").value=SpotifyConnectionState.AUTHORIZED}
  ui.waitForIdle()
  val bounds=ui.onNodeWithTag("spotify-search-panel").getUnclippedBoundsInRoot()
  assertTrue("Wide external-query form should fit a compact toolbar, was "+bounds.height,bounds.height<=112.dp)
 }
 @Test fun equivalentScenesAcrossProfiles() {
  runBlocking{app.spotify.configure("a".repeat(32));app.spotify.inspect()}
  ui.setContent{Scene()}
  val profiles=buildList {for(device in listOf("tablet","phone"))for(theme in listOf("dark","light"))for(scale in listOf(1f,2f))
   add(Profile("$device-$theme-"+(scale*100).toInt(),if(device=="tablet")1000 else 360,if(device=="tablet")720 else 780,theme,scale))}
  val metrics=StringBuilder()
  for(p in profiles)for(s in listOf("resultados","acciones","formulario")) {
   ui.runOnIdle{profile=p;scene=s;showMenu=false};ui.waitForIdle()
   if(s=="formulario") {
    ui.runOnIdle{ReflectionHelpers.getField<MutableStateFlow<SpotifyConnectionState>>(app.spotify,"mutableState").value=SpotifyConnectionState.AUTHORIZED}
    ui.waitForIdle()
    ui.onNodeWithTag("physical-scene").captureRoboImage("build/studio-physical-polish/debug-"+p.id+".png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    for(label in listOf("Tema","Artista","Álbum","Lista")) {
     ui.waitUntil(timeoutMillis=5000){runCatching{ui.onNodeWithText(label).assertIsDisplayed()}.isSuccess}
     ui.onNodeWithText(label).assertIsDisplayed()
    }
    ui.onNode(hasText("Buscar en Spotify") and hasClickAction()).assertIsNotEnabled()
    val form=ui.onNodeWithTag("spotify-search-panel").getUnclippedBoundsInRoot()
    metrics.appendLine("${p.id} form-height=${form.height.value}")
   }
   if(s=="acciones")measureMenu(p)
   ui.onNodeWithTag("physical-scene").captureRoboImage("build/studio-physical-polish/"+p.id+"-"+s+".png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
   if(s=="acciones") {
    metrics.appendLine(p.id+" menu="+menu+" cap="+menuCap+" parent="+panel)
    ui.onNodeWithText("Información de la app").performScrollTo().assertIsDisplayed()
   }
  }
  File("build/studio-physical-polish/metrics.txt").writeText(metrics.toString())
  assertEquals(0,effects)
 }
}
