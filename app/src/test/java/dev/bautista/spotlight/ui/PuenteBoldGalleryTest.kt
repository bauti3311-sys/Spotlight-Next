package dev.bautista.spotlight.ui

import android.content.Intent
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.takahirom.roborazzi.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import dev.bautista.spotlight.data.Config as AppConfig
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Puente finish samples only. One visual axis at a time; no product changes or live mixed-theme claim. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h1000dp-mdpi",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
class PuenteBoldGalleryTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private data class Profile(val id:String,val w:Int,val h:Int)
 private val tablet=Profile("tablet",1200,800)
 private val phone=Profile("phone",412,860)
 private var design by mutableIntStateOf(1)
 private var stage by mutableStateOf("inicio")
 private var query by mutableStateOf("")
 private var selected by mutableIntStateOf(0)
 private var receipt by mutableStateOf("")
 // Fixed ordered fixtures, not a second ranker. Same IDs/actions/order in every concept.
 private val results=listOf(
  Result("fixture.project/Main","Proyecto","Aplicación de demostración","Aplicaciones",4000,Action.Launch(Intent(Intent.ACTION_MAIN).setComponent(ComponentName("fixture.project","Main")))),
  Result("fixture:atlas","Proyecto Atlas.pdf","PDF local · fixture","Archivos",3000,Action.Copy("documento ficticio")),
  Result("fixture:note","Notas del proyecto","Nota local · fixture","Notas",2000,Action.Copy("nota ficticia")),
  Result("fixture:web","Buscar proyecto en la web","Revisar destino antes de abrir","Comandos",1000,Action.ComposeSearch("google","","proyecto"))
 )
 private data class Look(val id:String,val name:String,val bg:Long,val fg:Long,val secondary:Long,val accent:Long,val family:FontFamily,val scale:Float,val padding:Int,val row:Int,val queryHeight:Int,val separation:Int,val radius:Int,val opacity:Float,val solidSelection:Boolean=false)
 private val looks=listOf(
  Look("X01","Terminal ácido",0xFF080B08,0xFFE6FFBE,0xFFA3BC88,0xFFC4FF38,FontFamily.Monospace,.94f,8,48,56,12,0,1f,true),
  Look("X02","Papel editorial",0xFFFFF1CF,0xFF302113,0xFF705337,0xFFA23012,FontFamily.Serif,1.22f,28,72,88,32,2,1f),
  Look("X03","Cobalto eléctrico",0xFF123DD1,0xFFFFFFFF,0xFFD9E4FF,0xFFFFE766,Inter,1.12f,20,62,78,24,8,1f,true),
  Look("X04","Chicle",0xFFFFCBE3,0xFF54123C,0xFF794461,0xFF87215F,Geist,1.18f,24,68,84,24,30,1f,true),
  Look("X05","Señal naranja",0xFFFF662B,0xFF211309,0xFF51240B,0xFF211309,Inter,1.08f,12,52,68,14,0,1f,true),
  Look("X06","Hielo translúcido",0xFFE6FAFF,0xFF0C3344,0xFF244E61,0xFF075D82,FontFamily.Default,1f,16,56,70,22,24,.76f),
  Look("X07","Berenjena teatral",0xFF351039,0xFFFFE4B4,0xFFD6ACCF,0xFFFFD26A,FontFamily.Serif,1.18f,22,66,82,26,20,.94f),
  Look("X08","Blanco brutal",0xFFFFFFFF,0xFF080808,0xFF464646,0xFF080808,Geist,1.25f,10,60,80,12,0,1f,true)
 )
 private data class Sample(val id:String,val group:String,val index:Int,val name:String)
 private val samples=looks.mapIndexed{i,l->Sample(l.id,"look",i,l.name)}+
  listOf("Sólido 100 %","Vidrio 90 %","Vidrio 76 %","Vidrio 60 %").mapIndexed{i,n->Sample("O0${i+1}","opacity",i,n)}
 private var sampleIndex by mutableIntStateOf(0)
 private var isDark by mutableStateOf(true)
 private val sample get()=samples[sampleIndex]
 private val look get()=if(sample.group=="look")looks[sample.index] else looks[5]
 private val opacity get()=if(sample.group=="opacity")listOf(1f,.90f,.76f,.60f)[sample.index]else look.opacity
 private val surface get()=Color(look.bg)
 private val ink get()=Color(look.fg)
 private val muted get()=Color(look.secondary)
 private val accent get()=Color(look.accent)
 private val low get()=lerp(surface,ink,.045f).copy(alpha=opacity)
 private val high get()=lerp(surface,ink,.075f)
 private val line get()=ink.copy(alpha=if(look.radius==0).85f else .24f)
 private val family get()=look.family
 private val inset get()=look.padding
 private val rowSize get()=look.row
 private val querySize get()=look.queryHeight
 private val gap get()=look.separation
 private val finish get()=1
 private val selectedFill get()=if(look.solidSelection)accent else accent.copy(alpha=.18f)
 private val selectedInk get()=if(look.solidSelection)surface else ink
 private fun choose(label:String){receipt="Prototipo: $label elegida. Sin ejecución externa."}
 private fun context(){selected=1;stage="contexto";receipt=""}
 @Composable private fun Copy(text:String,size:Int=14,strong:Boolean=false,color:Color=ink,modifier:Modifier=Modifier,max:Int=2,fontOverride:FontFamily?=null) {
  Text(text,modifier,fontFamily=fontOverride?:family,fontSize=(size*look.scale).sp,lineHeight=((size+5)*look.scale).sp,letterSpacing=(if(look.id=="X01") .3f else if(look.id=="X08") -.3f else 0f).sp,fontWeight=if(strong)FontWeight.Bold else FontWeight.Normal,color=color,maxLines=max,overflow=TextOverflow.Ellipsis)
 }
 @Composable private fun DocCopy(text:String,size:Int=14,strong:Boolean=false,color:Color=ink,modifier:Modifier=Modifier,max:Int=2) {
  Copy(text,size,strong,color,modifier,max,fontOverride=Geist)
 }
 @Composable private fun Glyph(index:Int,size:Int=32) {
  val icon=when(index){0->CommandIcons.Apps;1->CommandIcons.Folder;2->CommandIcons.Snippet;else->CommandIcons.Search}
  Box(Modifier.size(size.dp).background(if(index==0)accent.copy(alpha=.18f)else ink.copy(alpha=.065f),RoundedCornerShape((size/4).dp)),contentAlignment=Alignment.Center){Icon(icon,null,Modifier.size((size*.60f).dp),tint=if(index==0)accent else muted)}
 }
 @Composable private fun Panel(modifier:Modifier=Modifier,radius:Int=16,content:@Composable ColumnScope.()->Unit) {
  val shape=RoundedCornerShape(look.radius.dp)
  val fill=Brush.verticalGradient(listOf(surface.copy(alpha=opacity),surface.copy(alpha=opacity)))
  Column(modifier.shadow(if(look.radius==0)0.dp else 12.dp,shape).clip(shape).background(fill).border(if(look.radius==0)2.dp else 1.dp,line,shape),content=content)
 }
 @Composable private fun Query(p:Profile,large:Boolean=false,pill:Boolean=false,prefix:String="") {
  Row(Modifier.fillMaxWidth().height(if(large)84.dp else querySize.dp).then(if(pill)Modifier.background(high,RoundedCornerShape(40.dp))else Modifier).padding(horizontal=if(large)24.dp else inset.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
   if(prefix.isNotBlank())Copy(prefix,12,color=accent) else Icon(CommandIcons.Search,null,Modifier.size(if(large)28.dp else 22.dp),tint=muted)
   BasicTextField(query,{query=it;stage=if(it.isBlank())"inicio" else "resultados";selected=0;receipt=""},Modifier.weight(1f).testTag("lab-query").onPreviewKeyEvent {
    if(it.type!=KeyEventType.KeyDown)false else when(it.key){
     Key.Tab->{stage="acciones";true}
     Key.Escape->{stage="resultados";receipt="";true}
     Key.Enter->{choose("Abrir aplicación");true}
     Key.DirectionDown->{selected=(selected+1).coerceAtMost(3);true}
     Key.DirectionUp->{selected=(selected-1).coerceAtLeast(0);true}
     else->false
    }
   },textStyle=TextStyle(fontFamily=family,fontSize=((if(large)30 else 19)*look.scale).sp,color=ink),singleLine=true,
    keyboardOptions=KeyboardOptions(imeAction=ImeAction.Search),keyboardActions=KeyboardActions(onSearch={choose("Abrir aplicación")} ),
    decorationBox={inner->Box{if(query.isEmpty())Copy("Buscar…",if(large)30 else 19,color=muted);inner()}})
   if(query.isNotEmpty())IconButton(onClick={query="";stage="inicio"}){Icon(CommandIcons.Close,"Limpiar consulta",Modifier.size(18.dp),tint=muted)}
   if(p.w>600 && design in listOf(2,4))Copy("Esc",11,color=muted)
  }
 }
 @Composable private fun LabRows(p:Profile,compact:Boolean=false,numbered:Boolean=false,titlesOnly:Boolean=false,modifier:Modifier=Modifier) {
  Column(modifier.fillMaxWidth().testTag("lab-results")) {
   for((i,r) in results.withIndex()) {
    Row(Modifier.fillMaxWidth().height(if(compact && p.w>600)48.dp else if(numbered)62.dp else 64.dp)
     .background(if(selected==i)accent.copy(alpha=.20f) else Color.Transparent)
     .clickable{selected=i;receipt=""}.testTag("lab-result:$i").padding(horizontal=if(numbered)20.dp else 14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(if(numbered)16.dp else 12.dp)) {
     Glyph(i,if(numbered)40 else if(compact)25 else 32)
     Column(Modifier.weight(1f)){Copy(r.title,if(numbered)19 else 15,selected==i,max=1);if(!titlesOnly && !compact)Copy(r.subtitle,12,color=muted,max=1)}
     if(numbered)Copy(if(p.w>600)"⌘${i+1}" else "${i+1}",12,color=muted)
     else if(p.w>600)Copy(if(i==0)"Aplicación" else if(i==1)"PDF" else if(i==2)"Nota" else "Comando",11,color=muted,max=1)
    }
   }
  }
 }
 @Composable private fun ActionButton(label:String,primary:Boolean=false,wide:Boolean=true) {
  Row((if(wide)Modifier.fillMaxWidth()else Modifier).heightIn(min=48.dp).background(if(primary)selectedFill else Color.Transparent,RoundedCornerShape(minOf(look.radius,16).dp)).clickable{choose(label)}.testTag("lab-action:$label").padding(horizontal=14.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   Icon(if(primary)CommandIcons.Arrow else if(label.contains("favorito"))CommandIcons.Pin else CommandIcons.Info,null,Modifier.size(19.dp),tint=if(primary && look.solidSelection)surface else if(primary)accent else muted)
   Copy(label,14,primary,color=if(primary)selectedInk else ink,modifier=Modifier.weight(1f))
  }
 }
 @Composable private fun ActionList(p:Profile,heading:Boolean=true) {
  Column(Modifier.fillMaxWidth().padding(if(inset>20)12.dp else 0.dp).testTag("lab-actions")) {
   if(heading)Copy("Acciones de ${results[selected].title}",12,color=muted,modifier=Modifier.padding(8.dp))
   ActionButton(when(selected){0->"Abrir aplicación";1->"Abrir archivo explícitamente";2->"Copiar nota";else->"Revisar destino web"},true);ActionButton("Fijar favorito");ActionButton(if(selected==0)"Información de la app" else "Información de la fuente")
   if(receipt.isNotEmpty())Copy(receipt,11,color=accent,modifier=Modifier.padding(8.dp),max=3)
  }
 }
 @Composable private fun Document(p:Profile,large:Boolean=false) {
  Column(Modifier.fillMaxWidth().testTag("lab-context")) {
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
    Glyph(1,if(large)42 else 30);Column(Modifier.weight(1f)){Copy("Proyecto Atlas.pdf",if(large)22 else 17,true);Copy("Documento ficticio · vista contextual",11,color=muted)}
   }
   Spacer(Modifier.height(12.dp))
   Column(Modifier.fillMaxWidth().background(Color(0xFFF3F1EB),RoundedCornerShape(4.dp)).padding(if(large)24.dp else 16.dp)) {
    DocCopy("PROYECTO ATLAS",if(large)22 else 16,true,Color(0xFF29333D))
    DocCopy("Plan de trabajo",if(large)16 else 13,color=Color(0xFF52616F),modifier=Modifier.padding(top=8.dp))
    HorizontalDivider(Modifier.padding(vertical=12.dp),color=Color(0xFFCED2D3))
    DocCopy("Objetivo",13,true,Color(0xFF29333D))
    DocCopy("Organizar una exposición con material de demostración. Revisar la propuesta antes de compartirla.",if(large)14 else 12,color=Color(0xFF465360),max=4)
    if(large){Spacer(Modifier.height(16.dp));DocCopy("Próximos pasos",13,true,Color(0xFF29333D));DocCopy("Reunir referencias · ordenar materiales · preparar presentación",13,color=Color(0xFF465360),max=3)}
   }
   Copy("Texto fijo de laboratorio. Sin extracción PDF, permisos SAF ni citas C5.",10,color=muted,modifier=Modifier.padding(top=10.dp),max=2)
  }
 }
 @Composable private fun Footer(p:Profile,label:String="Abrir aplicación") {
  HorizontalDivider(color=line)
  Row(Modifier.fillMaxWidth().height(42.dp).padding(horizontal=14.dp),verticalAlignment=Alignment.CenterVertically) {
   Copy("Spotlight",11,color=muted,modifier=Modifier.weight(1f))
   TextButton(onClick={choose(label)}){Copy(label,12,true)}
   if(p.w>600)Copy("↵",11,color=muted,modifier=Modifier.padding(end=12.dp))
   TextButton(onClick={stage="acciones"}){Copy("Acciones",12,true)}
  }
 }
 @Composable private fun InitialRows(p:Profile) {
  Column(Modifier.padding(16.dp)){Copy("Para empezar",12,color=muted);Spacer(Modifier.height(10.dp));for(i in 0..2)Row(Modifier.fillMaxWidth().height(44.dp).clickable{query="proyecto";stage="resultados"},verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Glyph(i,24);Copy(results[i].title,14)}}
 }

 @Composable private fun WorkspaceHeader(label:String) {
  Row(Modifier.fillMaxWidth().height(32.dp).background(low).padding(horizontal=14.dp),verticalAlignment=Alignment.CenterVertically){Copy(label,10,true,color=muted,modifier=Modifier.weight(1f));Copy(if(query.isEmpty())"LAB" else "4 DESTINOS",10,color=muted)}
 }
 @Composable private fun CompactRows() {
  Column(Modifier.fillMaxWidth()) {for(i in results.indices)Row(Modifier.fillMaxWidth().height(rowSize.dp).background(if(selected==i)selectedFill else Color.Transparent).clickable{selected=i;stage="seleccion"}.testTag("lab-result:$i").padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {Glyph(i,24);Copy(results[i].title,14,selected==i,color=if(selected==i)selectedInk else ink,modifier=Modifier.weight(1f),max=1)}}
 }
 @Composable private fun Preview(p:Profile,actions:Boolean=false,modifier:Modifier=Modifier) {
  Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(inset.dp)) {
   if(stage=="inicio"){Copy("Un lugar para revisar antes de actuar.",if(p.w>600)27 else 22,true);Spacer(Modifier.height(16.dp));Copy("Buscá una aplicación, un archivo o una acción. Datos de demostración, sin contenido personal.",14,color=muted,max=4)}
   else if(actions && stage=="acciones"){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Glyph(selected,30);Copy(results[selected].title,18,true,modifier=Modifier.weight(1f))};Spacer(Modifier.height(10.dp));ActionList(p,false)}
   else if(stage=="contexto" || selected==1){Document(p);if(actions)ActionButton("Abrir archivo explícitamente",true)}
   else {Glyph(selected,if(p.w>600)64 else 46);Spacer(Modifier.height(16.dp));Copy(results[selected].title,if(p.w>600)28 else 22,true);Copy(results[selected].subtitle,13,color=muted,max=2);Spacer(Modifier.height(18.dp));Copy("DESTINO SELECCIONADO",10,true,color=muted);Copy("Seleccionar permite revisar. La apertura requiere una acción explícita.",14,color=muted,max=4);if(actions)ActionList(p,false)}
  }
 }
 @Composable private fun Puente(p:Profile) {
  if(stage=="inicio")Panel(Modifier.fillMaxWidth(),18){Query(p);Copy("Una consulta conecta lista y destino.",15,color=muted,modifier=Modifier.padding(20.dp))}
  else if(p.w>600)Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(gap.dp)) {
   Panel(Modifier.width(340.dp).padding(top=36.dp),14){WorkspaceHeader("BUSCAR / ELEGIR");Query(p);CompactRows();Copy("La selección se revisa en la otra superficie →",11,color=muted,modifier=Modifier.padding(16.dp),max=3)}
   Panel(Modifier.weight(1f).height(650.dp),18){WorkspaceHeader("DESTINO / CONTEXTO");Box(Modifier.weight(1f)){Preview(p,stage=="acciones" || stage=="contexto")};if(stage!="acciones" && stage!="contexto")TextButton(onClick={stage="acciones"},modifier=Modifier.fillMaxWidth()){Copy("Revisar acciones →",13,color=accent)}}
  }else Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(gap.dp)) {
   Panel(Modifier.fillMaxWidth(),14){Query(p);CompactRows();Copy("Lista retenida · destino debajo",10,color=muted,modifier=Modifier.padding(12.dp))}
   Panel(Modifier.fillMaxWidth().height(minOf(370,p.h-50-querySize-4*rowSize-42-gap-24).dp),18){WorkspaceHeader("DESTINO");Box(Modifier.weight(1f)){Preview(p,stage=="acciones" || stage=="contexto")};if(stage!="acciones" && stage!="contexto")TextButton(onClick={stage="acciones"},modifier=Modifier.fillMaxWidth()){Copy("Acciones",12,color=accent)}}
  }
 }
 @Composable private fun Backdrop(p:Profile) {
  Column(Modifier.fillMaxSize().background(Color(0xFFB9CED5)).padding(24.dp)) {
   for(i in 0..13){
    Text("DOCUMENTO DE FONDO · REUNIÓN $i",fontFamily=FontFamily.Default,fontSize=16.sp,color=Color(0xFF31505E),modifier=Modifier.padding(top=12.dp))
    Text("Planificar el trabajo, revisar materiales y compartir la propuesta.",fontFamily=FontFamily.Default,fontSize=13.sp,color=Color(0xFF4D6975),maxLines=1)
   }
  }
 }

 @Composable private fun Canvas(p:Profile) {
  SpotlightTheme(if(isDark)"dark"else"light",AppConfig(focusInterface=true,theme=if(isDark)"dark"else"light",animations=false)) {
   Box(Modifier.size(p.w.dp,p.h.dp).testTag("lab-canvas")) {
    Backdrop(p)
    Box(Modifier.align(Alignment.TopCenter).padding(top=if(p.w>600)60.dp else 50.dp).width(if(p.w>600)1140.dp else(p.w-24).dp).testTag("lab-envelope")){Puente(p)}
    Copy("PUENTE LAB · ${sample.id} · ${sample.name}",9,color=muted,modifier=Modifier.align(Alignment.TopEnd).padding(8.dp))
    if(false)Column(Modifier.align(Alignment.BottomCenter).padding(bottom=8.dp).width((p.w-28).dp).background(low,RoundedCornerShape(10.dp)).padding(10.dp)){
     Copy("Aa · Ñáéíóú · 012345 · ¿Qué querés encontrar?",16)
     Copy("Muestra tipográfica de laboratorio; el documento conserva su fuente.",10,color=muted)
    }
   }
  }
 }
 private fun show(p:Profile){ui.setContent{Canvas(p)};ui.waitForIdle()}
 private fun capture(p:Profile) {
  show(p)
  for((i,item) in samples.withIndex()) {
   val themes=listOf(true)
   val stages=if(item.group=="look")listOf("acciones","contexto")else listOf("acciones")
   for(dark in themes)for(s in stages) {
    ui.runOnIdle{sampleIndex=i;isDark=dark;stage=s;query="proyecto";selected=if(s=="contexto")1 else 0;receipt=""};ui.waitForIdle()
    val box=ui.onNodeWithTag("lab-envelope").getUnclippedBoundsInRoot()
    assertTrue("${item.id} stays in ${p.id}: $box",box.left>=0.dp && box.top>=0.dp && box.right<=p.w.dp && box.bottom<=p.h.dp)
    assertTrue(rowSize>=48 && querySize>=48)
    ui.onNodeWithTag("lab-query").assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText,AnnotatedString("proyecto")))
    val q=ui.onNodeWithTag("lab-query").getUnclippedBoundsInRoot()
    val file="${p.id}-${item.id}-${if(dark)"dark"else"light"}-$s.png"
    ui.onNodeWithTag("lab-canvas").captureRoboImage("build/puente-bold/$file",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
    File("build/puente-bold/manifest.tsv").apply{parentFile!!.mkdirs();appendText("$file\t${item.group}\t${box.left.value}\t${box.top.value}\t${box.width.value}\t${box.height.value}\t${q.left.value}\t${q.top.value}\t${q.width.value}\t${q.height.value}\n")}
   }
  }
  assertEquals("",receipt);assertNull(Shadows.shadowOf(ui.activity.application as SpotlightApp).nextStartedActivity)
 }
 @Test fun tabletFinishSamples()=capture(tablet)
 @Test fun phoneFinishSamples()=capture(phone)
 @Test fun solidSelectionAndTouchTargetsRemainReadable() {
  fun contrast(a:Color,b:Color):Float {val x=a.luminance();val y=b.luminance();return (maxOf(x,y)+.05f)/(minOf(x,y)+.05f)}
  for(i in looks.indices){sampleIndex=i
   assertTrue("body ${look.id}",contrast(ink,surface)>=4.5f)
   assertTrue("selected ${look.id}",contrast(selectedInk,selectedFill.compositeOver(surface))>=4.5f)
   assertTrue(rowSize>=48 && querySize>=48)
  }
 }
 @Test fun keyboardSelectionAndLocalReceiptNeverLaunchExternally() {
  show(phone)
  ui.runOnIdle{sampleIndex=samples.indexOfFirst{it.id=="X02"};stage="acciones";query="proyecto";selected=0}
  ui.onNodeWithTag("lab-query").performClick();ui.onNodeWithTag("lab-query").performKeyInput{pressKey(Key.DirectionDown)}
  ui.runOnIdle{assertEquals(1,selected);assertEquals("proyecto",query);selected=0}
  ui.onNodeWithTag("lab-action:Abrir aplicación").performScrollTo().performClick()
  ui.runOnIdle{assertEquals("Prototipo: Abrir aplicación elegida. Sin ejecución externa.",receipt)}
  assertNull(Shadows.shadowOf(ui.activity.application as SpotlightApp).nextStartedActivity)
 }
}
