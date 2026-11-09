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
import androidx.compose.ui.text.font.FontFamily
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

/** Isolated Compose proposals using production components. Synthetic data, no installed APK or external execution. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h1000dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
class ChoiceCatalogueGalleryTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=ui.activity.application as SpotlightApp
 private data class Profile(val id:String,val width:Int,val height:Int,val theme:String,val scale:Float=1f)
 private data class Design(val id:String,val name:String,val dark:Long,val light:Long,val accent:Long,val radius:Int,val gap:Int,val inset:Int,val split:Float,val font:String="geist",val outlined:Boolean=false)
 private val designs=listOf(
  Design("C","Focus Studio actual",0xFF26282C,0xFFFAF8F3,0xFF87BFFF,22,0,12,.48f),
  Design("01","Carbon",0xFF202124,0xFFF4F4F5,0xFF85A6FA,10,0,8,.64f),
  Design("02","Pearl",0xFF292A30,0xFFF5F5F7,0xFF9D89F4,24,2,16,.56f,outlined=true),
  Design("03","Ink",0xFF101012,0xFFFFFFFF,0xFFB497FD,8,0,10,.62f),
  Design("04","Editorial",0xFF2B2622,0xFFF6F0E6,0xFFC88A63,4,6,20,.54f,"serif"),
  Design("05","Nordic",0xFF202A33,0xFFECF2F6,0xFF79AFC9,16,4,18,.58f),
  Design("06","Terminal",0xFF101B17,0xFFF0F7F2,0xFF72BA8A,2,0,8,.66f,"mono",true),
  Design("07","Tonal",0xFF2C263A,0xFFF4EDFF,0xFFBBA1EC,30,5,18,.58f),
  Design("08","Navigator",0xFF25302E,0xFFF0F4F0,0xFF8DB4A2,8,0,8,.68f,outlined=true),
  Design("09","Copper",0xFF302824,0xFFF8EEDD,0xFFD4A270,18,3,14,.60f),
  Design("10","Pop",0xFF25213B,0xFFF6EEFF,0xFFC48BF0,24,6,18,.56f,outlined=true),
  Design("11","Titanium",0xFF222930,0xFFE8EDF1,0xFF98B3C7,6,1,10,.65f,outlined=true),
  Design("12","Paper",0xFF292823,0xFFFFFDF4,0xFFC78176,0,7,20,.58f)
 )
 private val spec get()=designs.first{it.id==direction}
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
  found=found.filter{it.category=="Aplicaciones"}.take(3)+Result("fixture:public-search","Buscar en Vivaldi","Ingresar consulta","Comandos",1000,Action.ComposeSearch("vivaldi",pkg))
  assertEquals(3,home.size);assertEquals(4,found.size)
  vivaldi=found.first{it.id=="$pkg/Main"};assertEquals(vivaldi.id,found.first().id)
 }
 @Composable private fun Materials(p:Profile,content:@Composable ()->Unit) {
  val c=MaterialTheme.colorScheme;val dark=p.theme=="dark"
  val surface=Color(if(dark)spec.dark else spec.light)
  val ink=if(dark)Color(0xFFF3F2F1)else Color(0xFF20232A)
  val accent=if(dark)Color(spec.accent)else lerp(Color(spec.accent),Color.Black,.38f)
  val palette=if(direction=="C")c else c.copy(primary=accent,onPrimary=Color.White,
   background=lerp(surface,if(dark)Color.Black else Color.White,.25f),surface=surface,
   surfaceVariant=lerp(surface,ink,.07f),surfaceContainerLow=lerp(surface,if(dark)Color.Black else Color.White,.18f),
   surfaceContainerHigh=lerp(surface,ink,.045f),onSurface=ink,onSurfaceVariant=lerp(ink,surface,.25f),
   outlineVariant=lerp(surface,ink,if(spec.outlined).32f else .16f))
  val t=MaterialTheme.typography
  val family=when(spec.font){"mono"->FontFamily.Monospace;"serif"->FontFamily.Serif;else->Geist}
  val typography=if(direction=="C")t else t.copy(bodyLarge=t.bodyLarge.copy(fontFamily=family),bodyMedium=t.bodyMedium.copy(fontFamily=family),bodySmall=t.bodySmall.copy(fontFamily=family),titleLarge=t.titleLarge.copy(fontFamily=family),titleMedium=t.titleMedium.copy(fontFamily=family),titleSmall=t.titleSmall.copy(fontFamily=family),labelLarge=t.labelLarge.copy(fontFamily=family))
  MaterialTheme(colorScheme=palette,typography=typography,shapes=MaterialTheme.shapes,content=content)
 }
 @Composable private fun SearchBar(p:Profile) {
  val c=MaterialTheme.colorScheme
  val decoration=if(direction!="C")Modifier.padding(horizontal=spec.inset.dp,vertical=spec.inset.dp/2).background(c.surfaceContainerLow,RoundedCornerShape(spec.radius.coerceAtMost(24).dp))
   .border(1.dp,c.outlineVariant.copy(alpha=.65f),RoundedCornerShape(spec.radius.coerceAtMost(24).dp)) else Modifier.background(if(direction=="A")c.surfaceContainerHigh else Color.Transparent)
  Box(Modifier.fillMaxWidth().testTag("directions-bar").then(decoration)) {
   SpotlightBar(query,{query=it},"Todo",{},{query=""},{callbacks++},remember{FocusRequester()},1f,p.width<600,animations=false,showOptionsButton=true,onBack=if(scene=="inspector" && p.width<760)({scene="resultados"})else null)
  }
 }
 @Composable private fun Results(p:Profile,c:AppConfig) {
  val colors=MaterialTheme.colorScheme;val items=if(scene=="inicio")home else found
  Column(Modifier.fillMaxWidth().testTag("directions-results")) {
   if(direction=="07" && p.width>=760) {
    SectionHeading(if(scene=="inicio")"Sugerencias" else "Aplicaciones",true)
    Column(Modifier.fillMaxWidth().padding(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
     for(pair in items.chunked(2))Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
      for(item in pair)Box(Modifier.weight(1f).background(colors.surfaceContainerHigh,RoundedCornerShape(20.dp)).border(1.dp,colors.outlineVariant,RoundedCornerShape(20.dp))) {
       ResultRow(item,items.indexOf(item)==selected,item.id in c.favorites,{selected=items.indexOf(item)},{scene="inspector"},{callbacks++},animations=false,inline=false)
      }
      if(pair.size==1)Spacer(Modifier.weight(1f))
     }
    }
   } else if(direction=="C") {
    SpotlightResultList(items,c,if(scene=="inicio")"" else "Vivaldi",selected,rememberLazyListState(),p.height.dp,true,scene=="inicio",p.width>=600,false,emptySet(),{it.id in c.favorites},{},{i,_->selected=i},{i,_->selected=i;scene="inspector"},{callbacks++},{})
   } else {
   SectionHeading(if(scene=="inicio")"Sugerencias" else "Aplicaciones",true)
   LazyColumn(Modifier.fillMaxWidth(),state=rememberLazyListState()) {
    itemsIndexed(items,key={_,r->r.id}) {index,item->
     Box(Modifier.fillMaxWidth().padding(horizontal=spec.inset.dp,vertical=spec.gap.dp)
      .then(if(direction!="C" && index==selected)Modifier.background(colors.primary.copy(alpha=if(spec.outlined).15f else .08f),RoundedCornerShape(spec.radius.coerceAtMost(24).dp)).border(.75.dp,colors.primary.copy(alpha=if(spec.outlined).5f else 0f),RoundedCornerShape(spec.radius.coerceAtMost(24).dp)) else Modifier)) {
      ResultRow(item,index==selected,item.id in c.favorites,onOpen={selected=index},onDetails={selected=index;scene="inspector"},onFavorite={callbacks++},animations=false,inline=p.width>=600)
      if(direction!="C" && index==selected)Box(Modifier.align(Alignment.CenterStart).width(if(spec.outlined)3.dp else 2.dp).height(20.dp).background(if(direction!="C")colors.primary else colors.onSurfaceVariant.copy(alpha=.5f),RoundedCornerShape(2.dp)))
     }
    }
   }
   }
  }
 }
 @Composable private fun Inspector(p:Profile) {
  val colors=MaterialTheme.colorScheme
  val card=if(direction!="C")Modifier.padding(spec.inset.dp/2).background(colors.surfaceContainerHigh,RoundedCornerShape(spec.radius.dp)).border(.75.dp,colors.outlineVariant,RoundedCornerShape(spec.radius.dp)) else Modifier.background(if(direction=="A")colors.surfaceContainerLow else Color.Transparent)
  Box(Modifier.fillMaxWidth().testTag("directions-inspector").then(card)) {
   InspectorTheme(pkg,vivaldi.icon) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(if(direction!="C")spec.inset.dp else 20.dp)) {
     if(spec.outlined)Box(Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.primary))
     ApplicationInspector(vivaldi,pkg,true,false,emptyList(),{callbacks++},{callbacks++},{callbacks++},{scene="acciones"},{callbacks++},
      capabilitiesContent={AppCapabilityActions(pkg,appInfoAlreadyVisible=true,compact=p.width<600,onAction={callbacks++})},embedded=true)
    }
   }
  }
 }
 @Composable private fun Actions() {
  val colors=MaterialTheme.colorScheme
  val frame=if(direction!="C")Modifier.padding(spec.inset.dp).background(colors.surfaceContainerHigh,RoundedCornerShape(spec.radius.dp)).border(.75.dp,colors.outlineVariant,RoundedCornerShape(spec.radius.dp)) else Modifier.background(if(direction=="A")colors.surfaceContainerLow else Color.Transparent)
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
     val group=if(direction=="B")Modifier.background(colors.surfaceContainerHigh,RoundedCornerShape(spec.radius.dp)).border(.75.dp,colors.outlineVariant.copy(alpha=.65f),RoundedCornerShape(spec.radius.dp)).padding(if(p.width>=760)12.dp else 8.dp) else Modifier
     Box(Modifier.fillMaxWidth().then(group)){GlassAppearance(c,showHeading=false,update={callbacks++})}
    }
   }
  }
 }
 @Composable private fun Window(p:Profile,c:AppConfig) {
  val colors=MaterialTheme.colorScheme
  Column(Modifier.fillMaxWidth().height(when(scene){"inicio"->380.dp;"resultados"->430.dp;"acciones"->580.dp;else->if(p.width<760 || direction=="C") (p.height-24).dp else when(direction){"01","03","06","08","11"->480.dp;"02","09"->560.dp;"05"->660.dp;else->600.dp}}).padding(12.dp).then(if(direction=="C")Modifier.glassSurface(c,true)else Modifier.clip(RoundedCornerShape(spec.radius.dp)).background(colors.surface).border(if(spec.outlined)1.dp else .5.dp,colors.outlineVariant,RoundedCornerShape(spec.radius.dp))).testTag("directions-window")) {
   SearchBar(p)
   HorizontalDivider(color=colors.outlineVariant.copy(alpha=if(direction=="C").7f else .8f))
   Box(Modifier.weight(1f).fillMaxWidth()) {
    when(scene) {
     "ajustes"->Settings(p,c)
     "acciones"->Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){Actions()}
     "inspector"->if(p.width>=760 && direction=="05")Column(Modifier.fillMaxSize()) {
      Box(Modifier.weight(.43f)){Results(p,c)}
      HorizontalDivider(color=colors.outlineVariant)
      Box(Modifier.weight(.57f)){Inspector(p)}
     }else if(p.width>=760 && direction=="10")Row(Modifier.fillMaxSize()) {
      Box(Modifier.weight(.42f)){Inspector(p)}
      VerticalDivider(color=colors.outlineVariant)
      Box(Modifier.weight(.58f)){Results(p,c)}
     }else if(p.width>=760)Row(Modifier.fillMaxSize()) {
      Box(Modifier.weight(spec.split)){Results(p,c)}
      VerticalDivider(color=colors.outlineVariant.copy(alpha=.65f))
      Box(Modifier.weight(1f-spec.split)){Inspector(p)}
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
  val scenes=System.getenv("SPOTLIGHT_CATALOGUE_SCENE")?.let{listOf(it)} ?: if(p.scale>1f)listOf("inspector","acciones") else listOf("inicio","resultados","inspector","acciones","ajustes")
  for(s in scenes)for(v in designs.map{it.id}.filter{System.getenv("SPOTLIGHT_CATALOGUE_STYLES")?.split(",")?.contains(it)!=false}) {
   ui.runOnIdle{scene=s;direction=v;selected=0;query=if(s=="inicio")"" else if(s=="ajustes")"Ajustes" else "Vivaldi"};ready()
   val name="${p.id}-$s-$v.png"
   ui.onNodeWithTag("directions-canvas").captureRoboImage("build/choice-catalogue/$name",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
   val footer=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
   assertTrue(footer.bottom<=p.height.dp && footer.right<=p.width.dp)
   if(s=="inicio")ui.onAllNodes(SemanticsMatcher("result rows"){it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("result:")==true}).assertCountEquals(3)
   if(s=="ajustes" && p.width>=760) {
    val bounds=listOf("Solid Graphite","Dark Glass","macOS Light").map{ui.onNodeWithText(it).getUnclippedBoundsInRoot()}
    println("THEME_ROW $v $bounds")
    assertTrue("Three existing themes remain accessible",bounds.size==3)
   }
   java.io.File("build/choice-catalogue/manifest.tsv").apply{parentFile!!.mkdirs();appendText("${p.id}\t$s\t$v\t${p.width}\t${p.height}\t${p.scale}\t$name\n")}
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
