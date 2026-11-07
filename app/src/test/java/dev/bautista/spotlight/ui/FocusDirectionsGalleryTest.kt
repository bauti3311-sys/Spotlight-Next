package dev.bautista.spotlight.ui

import android.content.*
import android.content.pm.*
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.search.*
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Test-only presentation composition of production components, not SpotlightPanel or an APK screen. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h1000dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
class FocusDirectionsGalleryTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=ui.activity.application as SpotlightApp
 private data class Profile(val id:String,val width:Int,val height:Int,val theme:String,val scale:Float=1f)
 private var direction by mutableStateOf("C")
 private var scene by mutableStateOf("inicio")
 private var selected by mutableIntStateOf(0)
 private var query by mutableStateOf("")
 private var callbacks=0
 @After fun clean(){app.contexts.clearActive()}
 private val pkg="com.vivaldi.browser"
 private lateinit var home:List<Result>
 private lateinit var found:List<Result>
 private lateinit var vivaldi:Result
 private fun install(packageName:String,label:String,color:Int) {
  val a=ApplicationInfo().apply{this.packageName=packageName;nonLocalizedLabel=label;enabled=true;icon=0x7f010001}
  val ai=ActivityInfo().apply{this.packageName=packageName;name="Main";applicationInfo=a;enabled=true;exported=true;nonLocalizedLabel=label}
  val pm=Shadows.shadowOf(app.packageManager);val ri=ResolveInfo().apply{activityInfo=ai}
  pm.installPackage(PackageInfo().apply{this.packageName=packageName;applicationInfo=a;activities=arrayOf(ai);versionName="fixture-PM"})
  val bitmap=Bitmap.createBitmap(64,64,Bitmap.Config.ARGB_8888)
  android.graphics.Canvas(bitmap).apply {
   val paint=android.graphics.Paint(3).apply{this.color=color}
   drawRoundRect(0f,0f,64f,64f,14f,14f,paint);paint.color=android.graphics.Color.WHITE;paint.textSize=30f;paint.textAlign=android.graphics.Paint.Align.CENTER;drawText(label.take(1),32f,43f,paint)
  }
  val icon=android.graphics.drawable.BitmapDrawable(app.resources,bitmap)
  pm.addDrawableResolution(packageName,a.icon,icon);pm.setApplicationIcon(packageName,icon);pm.addActivityIcon(ComponentName(packageName,"Main"),icon)
  val launcher=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
  pm.addResolveInfoForIntent(launcher,ri);pm.addResolveInfoForIntent(Intent(launcher).setPackage(packageName),ri)
  if(packageName==pkg)for(q in listOf("consulta","android tablet")) {
   val action=dev.bautista.spotlight.control.ActionCatalog.searchAction("vivaldi",pkg,q) as Action.InApp
   pm.addResolveInfoForIntent(action.intent,ri)
  }
 }
 private fun config(p:Profile)=AppConfig(theme=p.theme,focusInterface=true,focusTypeface="geist",files=false,contacts=false,online=false,learning=true,animations=false,actionRecent=listOf("volumen 40%"),
  favorites=setOf("$pkg/Main"))
 private fun fixtures(p:Profile) {
  install(pkg,"Vivaldi",0xffa44542.toInt());install("fixture.vivaldi.snapshot","Vivaldi Snapshot",0xff5f656c.toInt())
  install("fixture.vivaldi.tools","Vivaldi Tools",0xff5e766b.toInt());install("com.android.documentsui","Archivos",0xffa77c36.toInt());install("com.android.calculator2","Calculadora",0xff4c687f.toInt())
  app.search.invalidateApps()
  app.contexts.setText("Texto de laboratorio, sin datos del usuario.","Notas de diseño · fixture")
  runBlocking {
   home=withTimeout(15000){app.search.search("",config(p)).first{it.size>=3}}.take(3)
   found=withTimeout(15000){app.search.search("Vivaldi",config(p)).first{r->r.count{it.category=="Aplicaciones"}>=3}}
    .let{CompactResults.select("Vivaldi",it,config(p)).relevant}.take(6)
  }
  assertEquals(3,home.size);assertTrue(found.size>=3)
  vivaldi=found.first{it.id=="$pkg/Main"};assertEquals(vivaldi.id,found.first().id)
 }
 @Composable private fun Materials(p:Profile,content:@Composable ()->Unit) {
  val c=MaterialTheme.colorScheme;val dark=p.theme=="dark"
  val palette=when(direction) {
   "A"->c.copy(surfaceContainerLow=lerp(c.surface,c.background,.45f),surfaceContainerHigh=lerp(c.surface,c.onSurface,.025f))
   "B"->c.copy(surface=if(dark)Color(0xFF25272B) else Color(0xFFFAF8F3),
    surfaceContainerLow=if(dark)Color(0xFF191C20) else Color(0xFFECE8E0),
    surfaceContainerHigh=if(dark)Color(0xFF30343A) else Color(0xFFFFFDF8),outlineVariant=if(dark)Color(0xFF545961) else Color(0xFFC9C2B7))
   else->c
  }
  MaterialTheme(colorScheme=palette,typography=MaterialTheme.typography,shapes=MaterialTheme.shapes,content=content)
 }
 @Composable private fun SearchBar(p:Profile) {
  val c=MaterialTheme.colorScheme
  val decoration=if(direction=="B")Modifier.padding(horizontal=10.dp,vertical=8.dp).background(c.surfaceContainerLow,RoundedCornerShape(10.dp))
   .border(1.dp,c.outlineVariant.copy(alpha=.65f),RoundedCornerShape(10.dp)) else Modifier.background(if(direction=="A")c.surfaceContainerHigh else Color.Transparent)
  Box(Modifier.fillMaxWidth().testTag("directions-bar").then(decoration)) {
   SpotlightBar(query,{query=it},"Todo",{},{query=""},{callbacks++},remember{FocusRequester()},1f,p.width<600,animations=false,showOptionsButton=true,onBack=if(scene=="inspector" && p.width<760)({scene="resultados"})else null)
  }
 }
 @Composable private fun Results(p:Profile,c:AppConfig) {
  val colors=MaterialTheme.colorScheme;val items=if(scene=="inicio")home else found
  Column(Modifier.fillMaxWidth().testTag("directions-results")) {
   if(direction=="C") {
    SpotlightResultList(items,c,if(scene=="inicio")"" else "Vivaldi",selected,rememberLazyListState(),p.height.dp,true,scene=="inicio",p.width>=600,false,emptySet(),{it.id in c.favorites},{},{i,_->selected=i},{i,_->selected=i;scene="inspector"},{callbacks++},{})
   } else {
   SectionHeading(if(scene=="inicio")"Sugerencias" else "Aplicaciones",true)
   LazyColumn(Modifier.fillMaxWidth(),state=rememberLazyListState()) {
    itemsIndexed(items,key={_,r->r.id}) {index,item->
     Box(Modifier.fillMaxWidth().padding(horizontal=if(direction=="C")10.dp else 12.dp,vertical=if(direction=="C")0.dp else 2.dp)
      .then(if(direction=="B" && index==selected)Modifier.background(colors.primary.copy(alpha=.07f),RoundedCornerShape(10.dp)).border(.75.dp,colors.primary.copy(alpha=.27f),RoundedCornerShape(10.dp)) else Modifier)) {
      ResultRow(item,index==selected,item.id in c.favorites,onOpen={selected=index},onDetails={selected=index;scene="inspector"},onFavorite={callbacks++},animations=false,inline=p.width>=600)
      if(direction!="C" && index==selected)Box(Modifier.align(Alignment.CenterStart).width(if(direction=="B")3.dp else 2.dp).height(20.dp).background(if(direction=="B")colors.primary else colors.onSurfaceVariant.copy(alpha=.5f),RoundedCornerShape(2.dp)))
     }
    }
   }
   }
  }
 }
 @Composable private fun Inspector(p:Profile) {
  val colors=MaterialTheme.colorScheme
  val card=if(direction=="B")Modifier.padding(8.dp).background(colors.surfaceContainerHigh,RoundedCornerShape(14.dp)).border(.75.dp,colors.outlineVariant,RoundedCornerShape(14.dp)) else Modifier.background(if(direction=="A")colors.surfaceContainerLow else Color.Transparent)
  Box(Modifier.fillMaxWidth().testTag("directions-inspector").then(card)) {
   InspectorTheme(pkg,vivaldi.icon) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(if(direction=="B")16.dp else 20.dp)) {
     if(direction=="B")Box(Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.primary))
     ApplicationInspector(vivaldi,pkg,true,false,emptyList(),{callbacks++},{callbacks++},{callbacks++},{scene="acciones"},{callbacks++},
      capabilitiesContent={AppCapabilityActions(pkg,appInfoAlreadyVisible=true,compact=p.width<600,onAction={callbacks++})},embedded=true)
    }
   }
  }
 }
 @Composable private fun Actions() {
  val colors=MaterialTheme.colorScheme
  val frame=if(direction=="B")Modifier.padding(10.dp).background(colors.surfaceContainerHigh,RoundedCornerShape(14.dp)).border(.75.dp,colors.outlineVariant,RoundedCornerShape(14.dp)) else Modifier.background(if(direction=="A")colors.surfaceContainerLow else Color.Transparent)
  Box(Modifier.fillMaxWidth().then(frame)) {
   ActionPanelView(listOf(
    ContextAction("Buscar en Vivaldi","Buscar en la web",CommandIcons.Search,{callbacks++},"Enter",id="search",group=ContextActionGroup.Primary),
    ContextAction("Abrir aplicación","Aplicación instalada · fixture",CommandIcons.Apps,{callbacks++},id="open"),
    ContextAction("Información de la aplicación","Administración de Android",HugeIcons.Settings,{callbacks++},id="info",group=ContextActionGroup.Management)
   ),remember{FocusRequester()},{scene="resultados"},vivaldi)
  }
 }
 @Composable private fun Settings(p:Profile,c:AppConfig) {
  SettingsLayout("Apariencia",{},Modifier.fillMaxSize().padding(8.dp)) {
   CompositionLocalProvider(LocalSettingsGroup provides "Apariencia") {
    SettingsPreferenceSection("Apariencia") {
     val colors=MaterialTheme.colorScheme
     val group=if(direction=="B")Modifier.background(colors.surfaceContainerHigh,RoundedCornerShape(14.dp)).border(.75.dp,colors.outlineVariant.copy(alpha=.65f),RoundedCornerShape(14.dp)).padding(if(p.width>=760)12.dp else 8.dp) else Modifier
     Box(Modifier.fillMaxWidth().then(group)){GlassAppearance(c,showHeading=false,update={callbacks++})}
    }
   }
  }
 }
 @Composable private fun Window(p:Profile,c:AppConfig) {
  val colors=MaterialTheme.colorScheme
  Column(Modifier.fillMaxWidth().height(when(scene){"inicio"->380.dp;"resultados"->430.dp;"acciones"->580.dp;else->(p.height-24).dp}).padding(12.dp).glassSurface(c,true).testTag("directions-window")) {
   SearchBar(p)
   HorizontalDivider(color=colors.outlineVariant.copy(alpha=if(direction=="C").7f else .8f))
   Box(Modifier.weight(1f).fillMaxWidth()) {
    when(scene) {
     "ajustes"->Settings(p,c)
     "acciones"->Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){Actions()}
     "inspector"->if(p.width>=760)Row(Modifier.fillMaxSize()) {
      Box(Modifier.weight(.48f)){Results(p,c)}
      VerticalDivider(color=colors.outlineVariant.copy(alpha=.65f))
      Box(Modifier.weight(.52f)){Inspector(p)}
     } else Column(Modifier.fillMaxSize()) {Inspector(p)}
     else->Results(p,c)
    }
   }
   if(direction!="C")HorizontalDivider(color=colors.outlineVariant.copy(alpha=.6f))
   GlassContextFooter(false,true,if(scene=="inicio" || scene=="resultados")"Abrir aplicación" else "Acciones disponibles",{callbacks++},{scene="acciones"},onOptions=null)
  }
 }
 private fun ready() {
  ui.waitForIdle()
  if(scene=="inspector")ui.waitUntil(15000){ui.onAllNodesWithTag("app-promoted-search").fetchSemanticsNodes().isNotEmpty()}
 }
 private fun render(p:Profile) {
  fixtures(p);val c=config(p)
  ui.setContent {SpotlightTheme(p.theme,c) {
   val density=LocalDensity.current
   CompositionLocalProvider(LocalDensity provides Density(density.density,p.scale)) {
    Box(Modifier.size(p.width.dp,p.height.dp).background(MaterialTheme.colorScheme.background).testTag("directions-canvas")) {
     Materials(p){key(scene,direction){Window(p,c)}}
    }
   }
  }}
  val scenes=System.getenv("SPOTLIGHT_DIRECTION_SCENE")?.let{listOf(it)} ?: if(p.scale>1f)listOf("inspector","acciones","ajustes") else listOf("inicio","resultados","inspector","acciones","ajustes")
  for(s in scenes)for(v in listOf("C","A","B")) {
   ui.runOnIdle{scene=s;direction=v;selected=0;query=if(s=="inicio")"" else if(s=="ajustes")"Ajustes" else "Vivaldi"};ready()
   val name="${p.id}-$s-$v.png"
   ui.onNodeWithTag("directions-canvas").captureRoboImage("build/focus-directions/$name",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
   val footer=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
   assertTrue(footer.bottom<=p.height.dp && footer.right<=p.width.dp)
   if(s=="inicio")ui.onAllNodes(SemanticsMatcher("result rows"){it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("result:")==true}).assertCountEquals(3)
   if(s=="ajustes" && p.width>=760) {
    val bounds=listOf("Solid Graphite","Dark Glass","macOS Light").map{ui.onNodeWithText(it).getUnclippedBoundsInRoot()}
    println("THEME_ROW $v $bounds")
    assertTrue("Keep all three tablet themes on one row",bounds.zipWithNext().all{(a,b)->a.right<=b.left})
   }
   java.io.File("build/focus-directions/manifest.tsv").apply{parentFile!!.mkdirs();appendText("${p.id}\t$s\t$v\t${p.width}\t${p.height}\t${p.scale}\t$name\n")}
  }
  assertEquals(0,callbacks);assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
 @Test fun tabletDark()=render(Profile("tablet-dark",1000,720,"dark"))
 @Test fun tabletLight()=render(Profile("tablet-light",1000,720,"light"))
 @Test fun phoneDark()=render(Profile("phone-dark",390,760,"dark"))
 @Test fun phoneLight()=render(Profile("phone-light",390,760,"light"))
 @Test fun compactDarkText200()=render(Profile("compact-dark",330,760,"dark",2f))
 @Test fun compactLightText200()=render(Profile("compact-light",330,760,"light",2f))
}
