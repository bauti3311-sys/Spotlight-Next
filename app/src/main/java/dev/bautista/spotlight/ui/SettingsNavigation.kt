package dev.bautista.spotlight.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.search.normalize

val LocalSettingsGroup=staticCompositionLocalOf{"General"}
val LocalSettingsQuery=staticCompositionLocalOf{""}

/** Presentation catalog only. Legacy keys and stored user content are never removed. */
object SettingsGroups {
 val names=listOf("General","Apariencia","Integraciones","Inteligencia artificial","Búsqueda y archivos","Atajos","Permisos y accesos","Avanzado")
 private val retired=listOf("Tasker", "Rutinas", "Centro de comandos", "Biblioteca personal")
 fun visible(title:String)=retired.none{title.startsWith(it)}
 fun canonical(group:String?):String=when(group){
  "Invocación"->"Atajos";"Inteligencia"->"Inteligencia artificial";"Búsqueda"->"Búsqueda y archivos";"Privacidad y sistema"->"Permisos y accesos";"Permisos y privacidad"->"Permisos y accesos"
  else->group?.takeIf{it in names} ?: "General"
 }
 fun of(title:String)=when {
  title=="Apariencia"->"Apariencia"
  title.startsWith("Inteligencia") || title.startsWith("Audio") || title.startsWith("Contexto")->"Inteligencia artificial"
  title.startsWith("Privacidad") || title.startsWith("Portapapeles") || title.startsWith("Permisos") || title.startsWith("Contactos autorizados") || title.startsWith("Shizuku")->"Permisos y accesos"
  title.startsWith("Invocación") || title.startsWith("Teclado") || title=="Acceso rápido"->"Atajos"
  title.startsWith("Diagnóstico") || title.startsWith("Transferir") || title.startsWith("Alias") || title=="Búsqueda · prioridades"->"Avanzado"
  title.startsWith("Herramientas") || title.startsWith("Extensiones") || title.startsWith("Integraciones") || title.startsWith("WhatsApp") || title.startsWith("Contactos") || title.startsWith("Clima") || title.startsWith("Tarjetas")->"Integraciones"
  title.startsWith("Aprendizaje") || title.startsWith("Búsqueda") || title.startsWith("Índice") || title.startsWith("Texto e imágenes")->"Búsqueda y archivos"
  else->"General"
 }
 fun label(group:String)=when(canonical(group)){"Búsqueda y archivos"->"Búsqueda";"Permisos y accesos"->"Permisos";"Inteligencia artificial"->"IA";else->canonical(group)}
 fun icon(group:String)=when(canonical(group)){"Apariencia"->CommandIcons.Display;"Búsqueda y archivos"->CommandIcons.Search;"Inteligencia artificial"->CommandIcons.AutoAwesome;"Integraciones"->CommandIcons.Apps;"Atajos"->CommandIcons.Keyboard;"General"->HugeIcons.Settings;"Avanzado"->CommandIcons.Sliders;else->CommandIcons.Shield}
 fun description(group:String)=when(canonical(group)){
  "General"->"Inicio, sugerencias y formas de abrir Spotlight."
  "Apariencia"->"Solid Graphite, Dark Glass y opacidad por superficie."
  "Inteligencia artificial"->"Proveedor, credenciales y contexto bajo tu control."
  "Búsqueda y archivos"->"Fuentes autorizadas, índice y reconocimiento local."
  "Integraciones"->"Aplicaciones, conexiones y herramientas disponibles."
  "Atajos"->"Atajos globales, teclado físico e invocación de la barra."
  "Permisos y accesos"->"Todos los permisos Android y servicios especiales en un único lugar."
  else->"Alias, prioridades, respaldo y diagnóstico."
 }
 fun summary(title:String)=when {
  title=="Apariencia"->"Solid Graphite y variantes de contraste"
  title.startsWith("Inteligencia")->"Configurar proveedor y clave; comprobar conexión de forma explícita"
  title.startsWith("Shizuku")->"Consultar conexión y autorización del servicio real"
  title.startsWith("Índice")->"Consultar estado, actualizar o cancelar el análisis local"
  title.startsWith("Transferir")->"Exportar e importar preferencias sin claves ni permisos"
  title.startsWith("WhatsApp")->"Mensajes de notificaciones compatibles, con autorización"
  title.startsWith("Privacidad")->"Modo privado, contexto voluntario e historial local"
  else->description(of(title))
 }
 fun matches(title:String,query:String):Boolean {
  if(!visible(title))return false
  val aliases=when {
   title=="Apariencia"->"tema glass graphite macos claro oscuro ancho color acento animaciones movimiento densidad opacidad transparencia superficie"
   title.startsWith("Inteligencia")->"gemini openai ia modelo clave api proveedor conexion"
   title.startsWith("Shizuku")->"shizuku adb brillo permisos servicio"
   title=="Permisos y accesos"->"superposición ventana flotante overlay contactos llamadas notificaciones whatsapp archivos carpetas almacenamiento calendario ubicación accesibilidad control aplicaciones phone use atajo teclado shizuku adb autorizacion servicio avisos uso actividad"
   title.startsWith("Búsqueda por significado")->"semantica embeddings documentos modelo local significado fragmentos"
   title.startsWith("Índice")->"ocr pdf contenido reconocimiento indexacion cancelar"
   title.startsWith("Invocación") || title.startsWith("Teclado")->"atajo teclado foco posicion gesto"
   title.startsWith("Portapapeles")->"clipboard texto retencion limite caducidad"
   else->summary(title)
  }
  val haystack=normalize("$title $aliases")
  return normalize(query.trim()).split(Regex("\\s+")).filter{it.isNotBlank()}.all{it in haystack}
 }
}

@Composable fun SettingsTabs(selected:String,onSelect:(String)->Unit) {
 Row(Modifier.horizontalScroll(rememberScrollState())){SettingsGroups.names.forEach{group->FilterChip(selected==group,{onSelect(group)},label={Text(group)})}}
}

/** One glass workspace; existing routes and every preference body remain in place. */
@Composable fun SettingsLayout(selected:String,onSelect:(String)->Unit,modifier:Modifier=Modifier,
    overview:(@Composable (()->Unit)->Unit)?=null,onInvoke:(()->Unit)?=null,content:@Composable ()->Unit) {
 var query by rememberSaveable{mutableStateOf("")};var chooser by rememberSaveable{mutableStateOf(false)}
 var generalDetails by rememberSaveable{mutableStateOf(true)}
 val holder=rememberSaveableStateHolder()
 fun back():Boolean=when{chooser->{chooser=false;true};query.isNotEmpty()->{query="";true};!generalDetails->{generalDetails=true;true};else->false}
 BackHandler(chooser || query.isNotEmpty() || !generalDetails){back()}
 val colors=MaterialTheme.colorScheme
 val groups=listOf("General","Apariencia","Búsqueda y archivos","Integraciones","Permisos y accesos","Inteligencia artificial","Atajos","Avanzado")
 val brand:@Composable ()->Unit={
  Row(Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   Box(Modifier.size(40.dp).background(colors.primary.copy(alpha=.12f),RoundedCornerShape(10.dp)),contentAlignment=Alignment.Center){IosCommandIcon(CommandIcons.Search,null,Modifier.size(26.dp),tint=colors.primary)}
   Column {Text("Spotlight Next",fontSize=15.sp,fontWeight=FontWeight.SemiBold);Text("Búsqueda en todas partes",fontSize=10.sp,color=colors.onSurfaceVariant)}
  }
 }
 val category:@Composable (String)->Unit={group->
  Row(Modifier.fillMaxWidth().testTag("settings-area:$group").selectable(selected==group,onClick={query="";onSelect(group);chooser=false;generalDetails=true},role=Role.Tab)
   .background(if(selected==group)colors.primary.copy(alpha=.12f) else Color.Transparent,RoundedCornerShape(9.dp))
   .heightIn(min=48.dp).padding(horizontal=12.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
   IosCommandIcon(SettingsGroups.icon(group),null,Modifier.size(20.dp),tint=colors.primary)
   Text(SettingsGroups.label(group),Modifier.weight(1f),fontSize=13.sp,fontWeight=if(selected==group)FontWeight.Medium else FontWeight.Normal,maxLines=2,overflow=TextOverflow.Ellipsis)
  }
 }
 CompositionLocalProvider(LocalSettingsQuery provides query) {
  BoxWithConstraints(modifier.fillMaxWidth().onPreviewKeyEvent{it.type==KeyEventType.KeyDown && it.key==Key.Escape && back()}) {
   val wide=maxWidth>=760.dp
   val detail:@Composable ()->Unit={
     Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(10.dp)) {
      Text(if(query.isNotBlank())"Resultados de ajustes" else if(!generalDetails && selected=="General")"Configuración guiada" else SettingsGroups.label(selected),fontSize=20.sp,fontWeight=FontWeight.SemiBold)
      Text(if(query.isNotBlank())"Controles disponibles en todas las secciones." else SettingsGroups.description(selected),fontSize=12.sp,color=colors.onSurfaceVariant)
      OutlinedTextField(query,{query=it.take(100)},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("settings-search"),singleLine=true,
       textStyle=MaterialTheme.typography.bodySmall,placeholder={Text("Buscar en todos los ajustes",fontSize=12.sp)},leadingIcon={IosCommandIcon(CommandIcons.Search,null,Modifier.size(17.dp))},
       trailingIcon={if(query.isNotEmpty())IconButton(onClick={query=""}){IosCommandIcon(CommandIcons.Close,"Limpiar búsqueda",Modifier.size(18.dp))}},shape=RoundedCornerShape(10.dp),
       colors=OutlinedTextFieldDefaults.colors(unfocusedBorderColor=colors.onSurface.copy(alpha=.10f),unfocusedContainerColor=colors.onSurface.copy(alpha=.025f)))
      HorizontalDivider(Modifier.padding(vertical=4.dp),color=colors.onSurface.copy(alpha=.10f))
      holder.SaveableStateProvider(if(query.isBlank())selected else "search") {
       Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).testTag("settings-details"),verticalArrangement=Arrangement.spacedBy(10.dp)) {
      if(selected=="General" && query.isBlank() && overview!=null) {
       if(generalDetails)TextButton(onClick={generalDetails=false},contentPadding=PaddingValues(0.dp)){Text("Configuración guiada",fontSize=12.sp)}
       else overview{generalDetails=true}
      }
      if(generalDetails || selected!="General" || query.isNotBlank())content()
      Spacer(Modifier.height(20.dp))
       }
      }
     }
   }
   if(wide)Row(Modifier.fillMaxSize()) {
    Column(Modifier.width(224.dp).fillMaxHeight().background(colors.onSurface.copy(alpha=.015f)).padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
     brand();Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(4.dp)){groups.forEach{category(it)}}
     onInvoke?.let{TextButton(onClick=it,modifier=Modifier.fillMaxWidth()){Text("Abrir Spotlight",fontSize=12.sp)}}
    }
    VerticalDivider(color=colors.onSurface.copy(alpha=.10f))
    Box(Modifier.weight(1f).fillMaxHeight().padding(horizontal=28.dp,vertical=24.dp)){detail()}
   } else Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.weight(1f)){brand()};onInvoke?.let{IconButton(onClick=it){IosCommandIcon(CommandIcons.Search,"Abrir Spotlight",Modifier.size(20.dp))}}}
    Box {
     OutlinedButton(onClick={chooser=true},modifier=Modifier.fillMaxWidth().testTag("settings-category-picker"),shape=RoundedCornerShape(10.dp)) {IosCommandIcon(SettingsGroups.icon(selected),null,Modifier.size(18.dp));Spacer(Modifier.width(12.dp));Text(SettingsGroups.label(selected),Modifier.weight(1f));Text("Cambiar")}
     DropdownMenu(chooser,{chooser=false},modifier=Modifier.heightIn(max=480.dp)){groups.forEach{group->DropdownMenuItem(text={Text(group)},leadingIcon={IosCommandIcon(SettingsGroups.icon(group),null,Modifier.size(18.dp))},onClick={query="";onSelect(group);chooser=false;generalDetails=true})}}
    }
    Box(Modifier.weight(1f).fillMaxWidth()){detail()}
   }
  }
 }
}

/** Shared grouped rows restyle every existing page without replacing any callbacks. */
@Composable fun SettingsPreferenceSection(title:String,content:@Composable ColumnScope.()->Unit) {
 val query=LocalSettingsQuery.current
 if(!SettingsGroups.visible(title))return
 if(query.isBlank() && SettingsGroups.of(title)!=LocalSettingsGroup.current)return
 if(query.isNotBlank() && !SettingsGroups.matches(title,query))return
 if(title=="Apariencia") {Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(16.dp),content=content);return}
 var expanded by rememberSaveable(title){mutableStateOf(title !in setOf("Diagnóstico de Spotlight","Transferir configuración","Bienvenido a Spotlight Next","Extensiones e integraciones","Integraciones automáticas","Tarjetas de información","Herramientas incluidas"))}
 val open=expanded || query.isNotBlank()
 Column(Modifier.fillMaxWidth().padding(vertical=4.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
  Row(Modifier.fillMaxWidth().heightIn(min=48.dp).clickable(role=Role.Button,onClickLabel=if(open)"Contraer sección" else "Expandir sección"){expanded=!expanded}
   .semantics{stateDescription=if(open)"Expandida" else "Contraída"},verticalAlignment=Alignment.CenterVertically){
   Text(title,Modifier.weight(1f),fontSize=13.sp,fontWeight=FontWeight.SemiBold)
   IosCommandIcon(CommandIcons.Chevron,null,Modifier.size(14.dp).rotate(if(open)90f else 0f),tint=MaterialTheme.colorScheme.onSurfaceVariant)
  }
  if(open)SettingsGroupSurface{content()}
 }
}

@Composable fun SettingsGroupSurface(content:@Composable ColumnScope.()->Unit) {
 Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp),color=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.30f),
  border=BorderStroke(1.dp,MaterialTheme.colorScheme.onSurface.copy(alpha=.075f)),tonalElevation=0.dp) {
  Column(Modifier.padding(horizontal=14.dp,vertical=10.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)
 }
}
