package dev.bautista.spotlight.ui

import android.content.*
import android.content.pm.*
import android.graphics.Bitmap
import android.media.session.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.core.content.FileProvider
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.MediaSnapshot
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Test-only presentation review. No saved preferences, capabilities, launchers or production tokens changed. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h900dp",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
class VisualFirstGalleryTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 private data class Profile(val id:String,val width:Int,val height:Int,val theme:String,val scale:Float=1f,val ime:Int=0)
 private val scenes=listOf("inicio","resultados","inspector","acciones","ajustes","formulario","documento","multimedia","estado-error")
 private val preserved=setOf("inicio","resultados","inspector")
 private var scene by mutableStateOf("inicio")
 private var proposed by mutableStateOf(false)
 private var requests=0
 private val integrationPhase=System.getenv("SPOTLIGHT_VISUAL_PHASE")?.also{require(it in setOf("before","after"))}
 private lateinit var document:Result
 private lateinit var media:MediaSnapshot
 private val pkg="com.vivaldi.browser"
 private val installed=Result("com.vivaldi.browser/Main","Vivaldi","Aplicación · fixture PM","Aplicaciones",4000,Action.Launch(Intent(Intent.ACTION_MAIN).setComponent(ComponentName(pkg,"Main"))))
 private fun install(packageName:String,label:String) {
  val a=ApplicationInfo().apply{this.packageName=packageName;nonLocalizedLabel=label;enabled=true;icon=0x7f010001}
  val ai=ActivityInfo().apply{this.packageName=packageName;name="Main";applicationInfo=a;enabled=true;exported=true;nonLocalizedLabel=label}
  val pm=Shadows.shadowOf(app.packageManager);val ri=ResolveInfo().apply{activityInfo=ai}
  pm.installPackage(PackageInfo().apply{this.packageName=packageName;applicationInfo=a;activities=arrayOf(ai);versionName="fixture-PM"})
  val bitmap=Bitmap.createBitmap(64,64,Bitmap.Config.ARGB_8888)
  android.graphics.Canvas(bitmap).apply {
   val p=android.graphics.Paint(3).apply{color=if(label=="Spotify")0xff32694a.toInt() else 0xffa44542.toInt()}
   drawRoundRect(0f,0f,64f,64f,14f,14f,p);p.color=android.graphics.Color.WHITE;p.textSize=36f;p.textAlign=android.graphics.Paint.Align.CENTER;drawText(label.take(1),32f,45f,p)
  }
  val icon=android.graphics.drawable.BitmapDrawable(app.resources,bitmap)
  pm.addDrawableResolution(packageName,a.icon,icon);pm.setApplicationIcon(packageName,icon);pm.addActivityIcon(ComponentName(packageName,"Main"),icon)
  val intent=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
  pm.addResolveInfoForIntent(intent,ri);pm.addResolveInfoForIntent(Intent(intent).setPackage(packageName),ri)
  if(packageName==pkg)for(q in listOf("consulta","android tablet"))pm.addResolveInfoForIntent((dev.bautista.spotlight.control.ActionCatalog.searchAction("vivaldi",pkg,q) as Action.InApp).intent,ri)
 }
 private fun fixtures() {
  install(pkg,"Vivaldi");install("com.spotify.music","Spotify");install("com.whatsapp","WhatsApp");app.search.invalidateApps()
  val f=java.io.File(app.cacheDir,"review/visual-first.txt").apply{parentFile!!.mkdirs();writeText("DOCUMENTO DE PRUEBA\n\nReunión de diseño\n\nPrioridad: legibilidad, búsqueda y contexto.\n\n1. Revisar el documento local.\n2. Conservar el origen y los permisos.\n3. Confirmar antes de abrir otra aplicación.\n\nEste archivo es un fixture, no información del usuario.")}
  document=Result("visual-document","Notas de diseño.txt","Archivo local autorizado · fixture","Archivos",1200,Action.Launch(Intent(Intent.ACTION_VIEW,FileProvider.getUriForFile(app,app.packageName+".files",f))),mime="text/plain")
  val art=Bitmap.createBitmap(96,96,Bitmap.Config.ARGB_8888)
  android.graphics.Canvas(art).apply{drawColor(0xff294b42.toInt());drawCircle(48f,48f,32f,android.graphics.Paint(3).apply{color=0xffb2c6a6.toInt()})}
  media=MediaSnapshot(MediaSession(app,"visual-fixture").sessionToken,"com.spotify.music","Horizonte","Sesión sintética de laboratorio","Fixture · sin audio real",art,180000,
   PlaybackState.Builder().setState(PlaybackState.STATE_PAUSED,42000,0f,1).setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_SKIP_TO_NEXT).build(),1)
 }
 private fun config(p:Profile)=AppConfig(theme=p.theme,focusInterface=true,focusTypeface="geist",animations=false,files=false,contacts=false,online=false,learning=false,
  favorites=setOf("com.spotify.music/Main","com.vivaldi.browser/Main"))
 @Composable private fun Proposal(enabled:Boolean,content:@Composable ()->Unit) {
  if(!enabled)content() else {
   val c=MaterialTheme.colorScheme
   MaterialTheme(colorScheme=c.copy(primaryContainer=c.surfaceContainerHigh,secondaryContainer=c.surfaceContainerHigh),
    typography=MaterialTheme.typography.copy(titleMedium=MaterialTheme.typography.titleMedium.copy(fontSize=16.sp,lineHeight=22.sp)),
    shapes=MaterialTheme.shapes.copy(extraSmall=RoundedCornerShape(10.dp),small=RoundedCornerShape(10.dp)),content=content)
  }
 }
 @Composable private fun SurfaceScene(p:Profile,c:AppConfig,available:Int) {
  if(scene in preserved) {
   SpotlightPanel(app,c,{requests++},if(scene=="inicio")"" else "Vivaldi",available.toFloat())
  } else {
   Column(Modifier.fillMaxWidth().padding(12.dp).heightIn(max=(available-24).dp).glassSurface(c,true)) {
    SpotlightBar(when(scene){"formulario"->"WhatsApp";"multimedia"->"Spotify";"documento"->"Notas de diseño";"ajustes"->"Ajustes";else->"Vivaldi"},{},"Todo",{},{},{requests++},remember{FocusRequester()},1f,p.width<600,animations=false)
    HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
    Box(Modifier.weight(1f,fill=false).fillMaxWidth()) {
     val width=if(proposed)if(scene=="ajustes")860.dp else 720.dp else Dp.Unspecified
     if(scene=="ajustes") Box(Modifier.align(Alignment.TopCenter).widthIn(max=width).fillMaxWidth().height((available-160).coerceAtLeast(100).dp).padding(8.dp)) {
      Proposal(proposed) { SettingsLayout("Apariencia",{},Modifier.fillMaxSize()) { CompositionLocalProvider(LocalSettingsGroup provides "Apariencia") { SettingsPreferenceSection("Apariencia") {GlassAppearance(c,showHeading=false,update={requests++})} } } }
     } else Column(Modifier.align(Alignment.TopCenter).widthIn(max=width).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
      Proposal(proposed) {
       when(scene) {
        "acciones" -> ActionPanelView(listOf(
         ContextAction("Buscar en Vivaldi","Buscar en la web",CommandIcons.Search,{requests++},"Enter",id="search",group=ContextActionGroup.Primary),
         ContextAction("Abrir aplicación","Aplicación instalada · fixture",CommandIcons.Apps,{requests++},id="open"),
         ContextAction("Información de la aplicación","Administración en Android",HugeIcons.Settings,{requests++},id="info",group=ContextActionGroup.Management)
        ),remember{FocusRequester()},{},installed)
        "ajustes" -> SettingsLayout("Apariencia",{},Modifier.fillMaxWidth()) { CompositionLocalProvider(LocalSettingsGroup provides "Apariencia") { SettingsPreferenceSection("Apariencia") {GlassAppearance(c,showHeading=false,update={requests++})} } }
        "formulario" -> InspectorTheme("com.whatsapp",null) { MessageEditor(Action.ComposeMessage("whatsapp","+541100000001","Hola, ¿nos encontramos a las 18?"),{requests++},{requests++},{}) }
        "documento" -> {Text(document.title,style=MaterialTheme.typography.titleMedium);Text(document.subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp));FilePreview(app,document)}
        "multimedia" -> InspectorTheme("com.spotify.music",null) {MediaInspector(media,{requests++},{requests++})}
        "estado-error" -> {GlassEmptyState("No se pudo completar la solicitud","El resultado no está verificado. Podés revisar la conexión y volver a intentarlo de forma explícita.");OutlinedButton(onClick={requests++}){Text("Revisar conexión")};Spacer(Modifier.height(12.dp));Text("Estado de prueba · no se consultó ninguna cuenta",style=MaterialTheme.typography.bodySmall)}
       }
      }
     }
    }
    if(scene=="formulario")Text("Tab siguiente campo   esc volver",Modifier.testTag("context-footer").padding(horizontal=18.dp,vertical=10.dp),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
    else GlassContextFooter(false,true,when(scene){"documento"->"Abrir documento";"formulario"->"Revisar mensaje";"multimedia"->"Ver acciones";else->"Acciones disponibles"},{requests++},{requests++},onOptions={})
   }
  }
 }
 private fun ready() {
  if(scene in preserved) {
   val tag=if(scene=="inicio")"result:suggest:app:com.spotify.music" else "result:com.vivaldi.browser/Main"
   ui.waitUntil(15000){ui.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()}
   if(scene=="inspector") {
    ui.onNodeWithTag(tag).performClick()
    ui.waitUntil(15000){ui.onAllNodesWithTag("app-promoted-search").fetchSemanticsNodes().isNotEmpty()}
   }
  } else if(scene=="formulario") {
   ui.waitUntil(15000){ui.onAllNodes(hasSetTextAction() and hasText("Número de destino")).fetchSemanticsNodes().firstOrNull()?.config?.getOrNull(SemanticsProperties.EditableText)?.text=="+541100000001"}
  } else if(scene=="ajustes") {
   ui.onNodeWithTag("focus-interface-toggle").assertExists()
  } else if(scene=="documento") {
   ui.waitUntil(15000){ui.onAllNodes(hasText("DOCUMENTO DE PRUEBA",substring=true)).fetchSemanticsNodes().isNotEmpty()}
  }
  ui.waitForIdle()
 }
 private fun capture(p:Profile,variant:String) {
  val name="${p.id}-$scene-$variant"
  val output=System.getenv("SPOTLIGHT_CAPTURE_ROOT")?.let{"$it/${integrationPhase ?: "after"}"} ?: if(integrationPhase==null)"build/visual-first" else "build/visual-polish/$integrationPhase"
  ui.onNodeWithTag("visual-canvas").captureRoboImage("$output/$name.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
  val footers=ui.onAllNodesWithTag("context-footer").fetchSemanticsNodes()
  assertTrue("Every scene needs its existing footer",footers.isNotEmpty())
  val b=ui.onNodeWithTag("context-footer").getUnclippedBoundsInRoot()
  assertTrue("Footer must fit available app viewport",b.bottom.value<=p.height-p.ime+1)
  java.io.File("$output/manifest.tsv").apply{parentFile!!.mkdirs();appendText("${p.id}\t$scene\t$variant\t${p.width}\t${p.height}\t${p.scale}\t${p.ime}\t${p.theme}\t$name.png\t${scene in preserved}\n")}
 }
 private fun profile(p:Profile) {
  fixtures();val c=config(p)
  ui.setContent {
   SpotlightTheme(p.theme,c) {
    Column(Modifier.width(p.width.dp).height(p.height.dp).background(MaterialTheme.colorScheme.background).testTag("visual-canvas")) {
     val density=LocalDensity.current
     CompositionLocalProvider(LocalDensity provides Density(density.density,p.scale)) {
      Box(Modifier.weight(1f).fillMaxWidth()) { key(scene) { SurfaceScene(p,c,p.height-p.ime) } }
     }
     if(p.ime>0)Box(Modifier.fillMaxWidth().height(p.ime.dp).background(MaterialTheme.colorScheme.surfaceVariant),contentAlignment=Alignment.Center) {
      Text("Área IME reservada · ${p.ime} dp\nSimulación de oclusión, no Gboard real",style=MaterialTheme.typography.bodySmall)
     }
    }
   }
  }
  for(s in scenes.filter{System.getenv("SPOTLIGHT_MAIN_SCENES")==null || it in preserved || it in setOf("acciones","ajustes")}) {
   ui.runOnIdle{scene=s;proposed=false};ready();capture(p,"A")
   if(integrationPhase==null && s !in preserved){ui.runOnIdle{proposed=true};ready();capture(p,"B")}
  }
  assertEquals("Rendering must not issue external requests",0,requests)
  assertNull(Shadows.shadowOf(app).nextStartedActivity)
 }
 @Test fun tabletDark()=profile(Profile("tablet-dark",1000,720,"dark"))
 @Test fun tabletLight()=profile(Profile("tablet-light",1000,720,"light"))
 @Test fun phoneDark()=profile(Profile("phone-dark",390,760,"dark"))
 @Test fun phoneLight()=profile(Profile("phone-light",390,760,"light"))
 @Test fun enlargedDark()=profile(Profile("phone-dark-text200",330,760,"dark",2f))
 @Test fun enlargedLight()=profile(Profile("phone-light-text200",330,760,"light",2f))
 @Test fun compactImeDark()=profile(Profile("phone-dark-compact-ime",330,760,"dark",1.3f,300))
 @Test fun compactImeLight()=profile(Profile("phone-light-compact-ime",330,760,"light",1.3f,300))
}
