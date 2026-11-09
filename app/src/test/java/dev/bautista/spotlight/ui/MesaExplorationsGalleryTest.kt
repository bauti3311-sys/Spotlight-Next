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

/** Five new workspace compositions, lab only. Mesa P05 reference is reused byte-for-byte, not rendered again. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],qualifiers="w1280dp-h1000dp-mdpi",application=SpotlightApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class,ExperimentalTestApi::class)
class MesaExplorationsGalleryTest {
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
 private val names=listOf("Atril","Folio","Puente","Tablero","Nexo")
 private val ink=Color(0xFFF3F5F8);private val muted=Color(0xFFAEB6C3);private val accent=Color(0xFF83B5FF)
 private val surface=Color(0xFF252930);private val line=Color(0xFF414750)
 private fun choose(label:String){receipt="Prototipo: $label elegida. Sin ejecución externa."}
 private fun context(){selected=1;stage="contexto";receipt=""}
 @Composable private fun Copy(text:String,size:Int=14,strong:Boolean=false,color:Color=ink,modifier:Modifier=Modifier,max:Int=2) {
  Text(text,modifier,fontSize=size.sp,lineHeight=(size+5).sp,fontWeight=if(strong)FontWeight.SemiBold else FontWeight.Normal,color=color,maxLines=max,overflow=TextOverflow.Ellipsis)
 }
 @Composable private fun Glyph(index:Int,size:Int=32) {
  val icon=when(index){0->CommandIcons.Apps;1->CommandIcons.Folder;2->CommandIcons.Snippet;else->CommandIcons.Search}
  Box(Modifier.size(size.dp).background(if(index==0)accent.copy(alpha=.18f)else Color.White.copy(alpha=.06f),RoundedCornerShape((size/4).dp)),contentAlignment=Alignment.Center){Icon(icon,null,Modifier.size((size*.60f).dp),tint=if(index==0)accent else muted)}
 }
 @Composable private fun Panel(modifier:Modifier=Modifier,radius:Int=16,content:@Composable ColumnScope.()->Unit) {
  Column(modifier.shadow(16.dp,RoundedCornerShape(radius.dp)).clip(RoundedCornerShape(radius.dp)).background(surface).border(1.dp,line,RoundedCornerShape(radius.dp)),content=content)
 }
 @Composable private fun Query(p:Profile,large:Boolean=false,pill:Boolean=false,prefix:String="") {
  Row(Modifier.fillMaxWidth().height(if(large)84.dp else 64.dp).then(if(pill)Modifier.background(Color(0xFF303640),RoundedCornerShape(40.dp))else Modifier).padding(horizontal=if(large)24.dp else 18.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
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
   },textStyle=TextStyle(fontFamily=Geist,fontSize=(if(large)30 else 19).sp,color=ink),singleLine=true,
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
  Row((if(wide)Modifier.fillMaxWidth()else Modifier).heightIn(min=48.dp).background(if(primary)accent.copy(alpha=.16f)else Color.Transparent,RoundedCornerShape(8.dp)).clickable{choose(label)}.testTag("lab-action:$label").padding(horizontal=14.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   Icon(if(primary)CommandIcons.Arrow else if(label.contains("favorito"))CommandIcons.Pin else CommandIcons.Info,null,Modifier.size(19.dp),tint=if(primary)accent else muted)
   Copy(label,14,primary,color=if(primary)accent else ink,modifier=Modifier.weight(1f))
  }
 }
 @Composable private fun ActionList(p:Profile,heading:Boolean=true) {
  Column(Modifier.fillMaxWidth().padding(10.dp).testTag("lab-actions")) {
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
    Copy("PROYECTO ATLAS",if(large)22 else 16,true,Color(0xFF29333D))
    Copy("Plan de trabajo",if(large)16 else 13,color=Color(0xFF52616F),modifier=Modifier.padding(top=8.dp))
    HorizontalDivider(Modifier.padding(vertical=12.dp),color=Color(0xFFCED2D3))
    Copy("Objetivo",13,true,Color(0xFF29333D))
    Copy("Organizar una exposición con material de demostración. Revisar la propuesta antes de compartirla.",if(large)14 else 12,color=Color(0xFF465360),max=4)
    if(large){Spacer(Modifier.height(16.dp));Copy("Próximos pasos",13,true,Color(0xFF29333D));Copy("Reunir referencias · ordenar materiales · preparar presentación",13,color=Color(0xFF465360),max=3)}
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
  Row(Modifier.fillMaxWidth().height(32.dp).background(Color(0xFF1B222A)).padding(horizontal=14.dp),verticalAlignment=Alignment.CenterVertically){Copy(label,10,true,color=muted,modifier=Modifier.weight(1f));Copy(if(query.isEmpty())"LAB" else "4 DESTINOS",10,color=muted)}
 }
 @Composable private fun CompactRows() {
  Column(Modifier.fillMaxWidth()) {for(i in results.indices)Row(Modifier.fillMaxWidth().height(48.dp).background(if(selected==i)accent.copy(alpha=.17f)else Color.Transparent).clickable{selected=i;stage="seleccion"}.testTag("lab-result:$i").padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {Glyph(i,24);Copy(results[i].title,14,selected==i,modifier=Modifier.weight(1f),max=1)}}
 }
 @Composable private fun ResultDock() {
  Row(Modifier.fillMaxWidth().height(88.dp).background(Color(0xFF1C232B)).padding(6.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
   for(i in results.indices)Column(Modifier.weight(1f).fillMaxHeight().background(if(selected==i)accent.copy(alpha=.15f)else Color.Transparent,RoundedCornerShape(8.dp)).clickable{selected=i;stage="seleccion"}.testTag("lab-result:$i").padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally) {
    Glyph(i,24);Spacer(Modifier.height(4.dp));Copy(results[i].title,11,selected==i,max=2)
   }
  }
 }
 @Composable private fun Preview(p:Profile,actions:Boolean=false,modifier:Modifier=Modifier) {
  Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
   if(stage=="inicio"){Copy("Un lugar para revisar antes de actuar.",if(p.w>600)27 else 22,true);Spacer(Modifier.height(16.dp));Copy("Buscá una aplicación, un archivo o una acción. Datos de demostración, sin contenido personal.",14,color=muted,max=4)}
   else if(actions && stage=="acciones"){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Glyph(selected,30);Copy(results[selected].title,18,true,modifier=Modifier.weight(1f))};Spacer(Modifier.height(10.dp));ActionList(p,false)}
   else if(stage=="contexto" || selected==1){Document(p);if(actions)ActionButton("Abrir archivo explícitamente",true)}
   else {Glyph(selected,if(p.w>600)64 else 46);Spacer(Modifier.height(16.dp));Copy(results[selected].title,if(p.w>600)28 else 22,true);Copy(results[selected].subtitle,13,color=muted,max=2);Spacer(Modifier.height(18.dp));Copy("DESTINO SELECCIONADO",10,true,color=muted);Copy("Seleccionar permite revisar. La apertura requiere una acción explícita.",14,color=muted,max=4);if(actions)ActionList(p,false)}
  }
 }
 @Composable private fun ActionArea(p:Profile) {
  Column(Modifier.fillMaxWidth()) {
   if(query.isEmpty())Copy("Elegí un resultado",12,color=muted,modifier=Modifier.padding(16.dp))
   else if(stage=="contexto" || selected==1)ActionButton("Abrir archivo explícitamente",true)
   else ActionList(p,false)
  }
 }
 @Composable private fun Atril(p:Profile) {
  Panel(Modifier.fillMaxWidth().height(if(stage=="inicio")250.dp else if(p.w>600)650.dp else 700.dp),14) {
   WorkspaceHeader("ATRIL / ESCENARIO DE REVISIÓN");Query(p);HorizontalDivider(color=line)
   if(stage=="inicio")Box(Modifier.weight(1f)){Preview(p)}
   else {
    if(p.w>600)Row(Modifier.weight(1f)) {
     Box(Modifier.weight(1f).background(Color(0xFF1C232B))){Preview(p)}
     VerticalDivider(color=line)
     Column(Modifier.width(208.dp).padding(10.dp)){Copy("HERRAMIENTAS",10,true,color=muted,modifier=Modifier.padding(12.dp));ActionArea(p)}
    }else Box(Modifier.weight(1f)){Preview(p,stage=="acciones" || stage=="contexto")}
    if(p.w<600 && stage!="acciones" && stage!="contexto")TextButton(onClick={stage="acciones"},modifier=Modifier.fillMaxWidth()){Copy("Acciones del destino seleccionado",12,color=accent)}
    HorizontalDivider(color=line);ResultDock()
   }
  }
 }
 @Composable private fun Folio(p:Profile) {
  Panel(Modifier.fillMaxWidth().height(if(stage=="inicio")300.dp else if(stage=="contexto")if(p.w>600)660.dp else 720.dp else if(p.w>600)520.dp else 590.dp),10) {
   WorkspaceHeader("FOLIO / ÍNDICE EXPANDIBLE")
   if(stage=="inicio")Box(Modifier.weight(1f)){InitialRows(p)}
   else Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
    Row(Modifier.fillMaxWidth().height(36.dp).padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){Copy("#",10,color=muted,modifier=Modifier.width(28.dp));Copy("RESULTADO",10,true,color=muted,modifier=Modifier.weight(1f));if(p.w>600)Copy("TIPO / ORIGEN",10,color=muted)}
    for(i in results.indices) {
     Row(Modifier.fillMaxWidth().height(56.dp).background(if(selected==i)accent.copy(alpha=.14f)else Color.Transparent).clickable{selected=i;stage="seleccion"}.testTag("lab-result:$i").padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
      Copy("0${i+1}",11,color=muted);Glyph(i,25);Column(Modifier.weight(1f)){Copy(results[i].title,15,selected==i,max=1);if(p.w<600)Copy(results[i].category,10,color=muted)};if(p.w>600)Copy(results[i].category+" · fixture",11,color=muted)
     }
     if(i==selected && stage in listOf("seleccion","acciones","contexto"))Column(Modifier.fillMaxWidth().background(Color(0xFF1D242D)).padding(14.dp)){
      if(stage=="contexto"){Document(p);ActionButton("Abrir archivo explícitamente",true)}else if(stage=="acciones")ActionList(p,false)else TextButton(onClick={stage="acciones"}){Copy("Revisar acciones de la selección ↓",13,color=accent)}
     }
     HorizontalDivider(color=line)
    }
   }
   Row(Modifier.fillMaxWidth().height(38.dp).padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){Copy("Orden de búsqueda conservado",10,color=muted,modifier=Modifier.weight(1f));TextButton(onClick={stage="acciones"}){Copy("Acciones",12,color=accent)}}
   HorizontalDivider(color=line);Query(p)
  }
 }
 @Composable private fun Puente(p:Profile) {
  if(stage=="inicio")Panel(Modifier.fillMaxWidth(),18){Query(p);Copy("Una consulta conecta lista y destino.",15,color=muted,modifier=Modifier.padding(20.dp))}
  else if(p.w>600)Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(28.dp)) {
   Panel(Modifier.width(340.dp).padding(top=36.dp),14){WorkspaceHeader("BUSCAR / ELEGIR");Query(p);CompactRows();Copy("La selección se revisa en la otra superficie →",11,color=muted,modifier=Modifier.padding(16.dp),max=3)}
   Panel(Modifier.weight(1f).height(650.dp),18){WorkspaceHeader("DESTINO / CONTEXTO");Box(Modifier.weight(1f)){Preview(p,stage=="acciones" || stage=="contexto")};if(stage!="acciones" && stage!="contexto")TextButton(onClick={stage="acciones"},modifier=Modifier.fillMaxWidth()){Copy("Revisar acciones →",13,color=accent)}}
  }else Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(16.dp)) {
   Panel(Modifier.fillMaxWidth(),14){Query(p);CompactRows();Copy("Lista retenida · destino debajo",10,color=muted,modifier=Modifier.padding(12.dp))}
   Panel(Modifier.fillMaxWidth().height(370.dp),18){WorkspaceHeader("DESTINO");Box(Modifier.weight(1f)){Preview(p,stage=="acciones" || stage=="contexto")};if(stage!="acciones" && stage!="contexto")TextButton(onClick={stage="acciones"},modifier=Modifier.fillMaxWidth()){Copy("Acciones",12,color=accent)}}
  }
 }
 @Composable private fun ResultTiles(p:Profile) {
  val indices=if(stage=="inicio")listOf(0,1,2)else results.indices.toList()
  Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)){
   for(pair in indices.chunked(2))Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
    for(i in pair)Row(Modifier.weight(1f).height(if(p.w>600)92.dp else 82.dp).background(if(selected==i)accent.copy(alpha=.14f)else Color.White.copy(alpha=.04f),RoundedCornerShape(12.dp)).border(1.dp,if(selected==i)accent else line,RoundedCornerShape(12.dp)).clickable{selected=i;stage="seleccion"}.testTag("lab-result:$i").padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
     Glyph(i,28);Column(Modifier.weight(1f)){Copy(results[i].title,14,selected==i,max=2);Copy(results[i].category,10,color=muted,max=1)}
    }
    if(pair.size==1)Spacer(Modifier.weight(1f))
   }
  }
 }
 @Composable private fun Tablero(p:Profile) {
  Panel(Modifier.fillMaxWidth().height(if(stage=="inicio")if(p.w>600)390.dp else 420.dp else if(p.w>600)660.dp else 740.dp),18){
   WorkspaceHeader("TABLERO / ELEGIR Y REVISAR");Query(p);HorizontalDivider(color=line)
   if(stage=="inicio")Column(Modifier.padding(16.dp)){Copy("Elegí una pista o empezá a escribir",15,color=muted,modifier=Modifier.padding(bottom=16.dp));ResultTiles(p)}
   else if(p.w>600)Row(Modifier.weight(1f)) {
    Column(Modifier.weight(1f).padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){ResultTiles(p);Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF1D242C),RoundedCornerShape(10.dp))){Preview(p)}}
    VerticalDivider(color=line);Column(Modifier.width(228.dp).padding(12.dp)){Copy("ACTUAR",10,true,color=muted,modifier=Modifier.padding(10.dp));ActionArea(p)}
   }else Column(Modifier.weight(1f).padding(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
    ResultTiles(p)
    Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF1D242C),RoundedCornerShape(12.dp))){Preview(p,stage=="acciones" || stage=="contexto")}
    if(stage!="acciones" && stage!="contexto")TextButton(onClick={stage="acciones"},modifier=Modifier.fillMaxWidth()){Copy("Ver acciones del seleccionado",12,color=accent)}
   }
  }
 }
 @Composable private fun Nexo(p:Profile) {
  Panel(Modifier.fillMaxWidth().height(if(stage=="inicio")230.dp else if(p.w>600)680.dp else 730.dp),14){
   WorkspaceHeader("NEXO / REVISAR ARRIBA, ELEGIR ABAJO")
   Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF1D242C))){Preview(p,stage=="acciones" || stage=="contexto")}
   HorizontalDivider(color=accent.copy(alpha=.5f));Query(p);HorizontalDivider(color=line)
   if(stage=="inicio")Copy("Una búsqueda enlaza resultados y contexto",11,color=muted,modifier=Modifier.padding(16.dp))
   else {Copy("RESULTADOS",10,true,color=muted,modifier=Modifier.padding(horizontal=14.dp,vertical=8.dp));CompactRows();Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),horizontalArrangement=Arrangement.End){TextButton(onClick={stage="acciones"}){Copy("Acciones de la selección ↑",11,color=accent)}}}
  }
 }
 @Composable private fun Backdrop(p:Profile) {
  Column(Modifier.fillMaxSize().background(Color(0xFF111820)).padding(if(p.w>600)36.dp else 20.dp)) {
   Copy("FONDO COMÚN DE LABORATORIO",10,true,color=Color(0xFF64717E));Spacer(Modifier.height(22.dp))
   Copy("Un día de trabajo",if(p.w>600)32 else 25,true,color=Color(0xFF53616F))
   for(i in 0..5)Row(Modifier.fillMaxWidth().padding(top=28.dp),horizontalArrangement=Arrangement.spacedBy(20.dp)){
    Box(Modifier.width(if(p.w>600)100.dp else 50.dp).height(5.dp).background(Color(0xFF26323E)))
    Column(Modifier.weight(1f)){Box(Modifier.fillMaxWidth(if(i%2==0).68f else .92f).height(6.dp).background(Color(0xFF293541)));Spacer(Modifier.height(13.dp));Box(Modifier.fillMaxWidth(.80f).height(5.dp).background(Color(0xFF202C37)))}
   }
  }
 }

 @Composable private fun Canvas(p:Profile) {
  SpotlightTheme("dark",AppConfig(focusInterface=true,theme="dark",animations=false)) {
   Box(Modifier.size(p.w.dp,p.h.dp).testTag("lab-canvas")) {
    Backdrop(p)
    val width=if(p.w<600)p.w-24 else if(stage=="inicio")when(design){1->640;2->700;3->520;4->740;else->700}else when(design){1->1080;2->960;3->1140;4->1120;else->980}
    val top=if(p.w<600)if(stage=="inicio")150 else if(design==4)40 else 50 else if(stage=="inicio")180 else if(design==2)100 else 60
    Box(Modifier.align(Alignment.TopCenter).padding(top=top.dp).width(width.dp).testTag("lab-envelope")) {
     when(design){1->Atril(p);2->Folio(p);3->Puente(p);4->Tablero(p);else->Nexo(p)}
    }
    Copy("COMPOSE · M0$design · ${names[design-1]} · fixture",9,color=Color(0xFF8895A3),modifier=Modifier.align(Alignment.TopEnd).padding(8.dp))
   }
  }
 }
 private fun show(p:Profile){ui.setContent{Canvas(p)};ui.waitForIdle()}
 private fun render(p:Profile) {
  show(p)
  for(d in (1..5).filter{System.getenv("SPOTLIGHT_MESA_CONCEPTS")?.split(",")?.contains(it.toString())!=false})for(s in listOf("inicio","resultados","seleccion","acciones","contexto").filter{System.getenv("SPOTLIGHT_MESA_STATES")?.split(",")?.contains(it)!=false}) {
   ui.runOnIdle{design=d;stage=s;query=if(s=="inicio")"" else "proyecto";selected=if(s=="contexto")1 else 0;receipt=""};ui.waitForIdle()
   val b=ui.onNodeWithTag("lab-envelope").getUnclippedBoundsInRoot()
   assertTrue("M$d $s fits ${p.id}: $b",b.left>=0.dp && b.top>=0.dp && b.right<=p.w.dp && b.bottom<=p.h.dp)
   ui.onNodeWithTag("lab-query").assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText,AnnotatedString(query)))
   if(s=="contexto")ui.onNodeWithTag("lab-context").assertExists()
   ui.onNodeWithTag("lab-canvas").captureRoboImage("build/mesa-explorations/${p.id}-M0$d-$s.png",roborazziOptions=RoborazziOptions(taskType=RoborazziTaskType.Record))
   File("build/mesa-explorations/geometry.tsv").apply{parentFile!!.mkdirs();appendText("${p.id}\tM0$d\t$s\t${b.left.value}\t${b.top.value}\t${b.width.value}\t${b.height.value}\n")}
  }
  assertEquals("",receipt);assertNull(Shadows.shadowOf(ui.activity.application as SpotlightApp).nextStartedActivity)
 }
 @Test fun tabletFiveStates()=render(tablet)
 @Test fun phoneFiveStates()=render(phone)
 @Test fun localSelectionAndExplicitActionsDoNotLaunchAnything() {
  show(phone)
  for(d in 1..5) {
   ui.runOnIdle{design=d;stage="resultados";query="proyecto";selected=0;receipt=""}
   ui.onNodeWithTag("lab-query").performClick();ui.onNodeWithTag("lab-query").performKeyInput{pressKey(Key.DirectionDown)}
   ui.runOnIdle{assertEquals(1,selected)}
   ui.onNodeWithTag("lab-query").performKeyInput{pressKey(Key.Tab)};ui.runOnIdle{assertEquals("acciones",stage)}
   ui.onNodeWithTag("lab-query").performClick();ui.onNodeWithTag("lab-query").performKeyInput{pressKey(Key.Escape)}
   ui.runOnIdle{assertEquals("resultados",stage);assertEquals(1,selected);assertEquals("proyecto",query);selected=0;stage="acciones"}
   ui.onNodeWithTag("lab-action:Abrir aplicación").performScrollTo().performClick()
   ui.runOnIdle{assertEquals("Prototipo: Abrir aplicación elegida. Sin ejecución externa.",receipt)}
   assertNull(Shadows.shadowOf(ui.activity.application as SpotlightApp).nextStartedActivity)
  }
 }
}
