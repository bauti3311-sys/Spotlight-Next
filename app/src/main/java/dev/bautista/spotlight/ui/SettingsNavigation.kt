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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
 private val retired=listOf("Tasker", "Rutinas", "Centro de comandos", "Biblioteca personal", "Bienvenido a Spotlight Next", "Herramientas incluidas", "Clima y agenda deportiva")
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
  title=="Acerca de Spotlight" || title=="Integraciones automáticas" || title=="Extensiones e integraciones" || title=="Tarjetas de información" || title.startsWith("Diagnóstico") || title.startsWith("Transferir") || title.startsWith("Alias") || title=="Búsqueda · prioridades"->"Avanzado"
  title.startsWith("Herramientas") || title.startsWith("Extensiones") || title.startsWith("Integraciones") || title.startsWith("WhatsApp") || title.startsWith("Contactos") || title.startsWith("Clima") || title.startsWith("Tarjetas")->"Integraciones"
  title.startsWith("Aprendizaje") || title.startsWith("Búsqueda") || title.startsWith("Índice") || title.startsWith("Texto e imágenes")->"Búsqueda y archivos"
  else->"General"
 }
 fun heading(group:String)=when(canonical(group)){"General"->"Inicio";"Apariencia"->"Apariencia y ventana";"Integraciones"->"Aplicaciones";"Inteligencia artificial"->"Inteligencia artificial";"Búsqueda y archivos"->"Búsqueda y archivos";"Permisos y accesos"->"Privacidad y permisos";else->canonical(group)}
 fun sectionLabel(title:String)=when(title){"Integraciones automáticas"->"Aplicación predeterminada para mensajes";"Extensiones e integraciones"->"Conectores heredados";"Tarjetas de información"->"Consultas externas y proveedores";"General · inicio"->"Al abrir Spotlight";"Privacidad · Spotlight Glass"->"Privacidad local";else->title}
 fun label(group:String)=when(canonical(group)){"Búsqueda y archivos"->"Búsqueda";"Permisos y accesos"->"Permisos";"Inteligencia artificial"->"IA";else->canonical(group)}
 fun icon(group:String)=when(canonical(group)){"Apariencia"->CommandIcons.Display;"Búsqueda y archivos"->CommandIcons.Search;"Inteligencia artificial"->CommandIcons.AutoAwesome;"Integraciones"->CommandIcons.Apps;"Atajos"->CommandIcons.Keyboard;"General"->HugeIcons.Settings;"Avanzado"->CommandIcons.Sliders;else->CommandIcons.Shield}
 fun description(group:String)=when(canonical(group)){
  "General"->"Accesos útiles y preferencias cotidianas."
  "Apariencia"->"Tamaño del buscador, tema y legibilidad."
  "Inteligencia artificial"->"Proveedor, credenciales y contexto bajo tu control."
  "Búsqueda y archivos"->"Fuentes autorizadas, índice y reconocimiento local."
  "Integraciones"->"Disponibilidad real de las aplicaciones y sus acciones."
  "Atajos"->"Atajos globales, teclado físico e invocación de la barra."
  "Permisos y accesos"->"Todos los permisos Android y servicios especiales en un único lugar."
  else->"Preferencias técnicas, proveedores, respaldo y diagnóstico."
 }
 fun summary(title:String)=when {
  title=="Apariencia"->"Temas, tipografía y contraste"
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
   title=="Apariencia"->"tema glass graphite macos claro oscuro ancho color acento animaciones movimiento densidad opacidad transparencia superficie texto tipografia fuente letra espaciado puente"
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

/** Native settings workspace: searchable sections on phone, persistent navigation on tablet. */
@OptIn(ExperimentalLayoutApi::class)
@Composable fun SettingsLayout(selected:String,onSelect:(String)->Unit,modifier:Modifier=Modifier,
    overview:(@Composable (()->Unit)->Unit)?=null,onInvoke:(()->Unit)?=null,content:@Composable ()->Unit) {
 var query by rememberSaveable{mutableStateOf("")}
 var compactOverview by rememberSaveable{mutableStateOf(selected=="General")}
 val holder=rememberSaveableStateHolder()
 val focusRequester=remember{FocusRequester()}
 var previousSelected by rememberSaveable{mutableStateOf(selected)}
 LaunchedEffect(selected,compactOverview){
  // An explicit Settings deep link must enter its section even if the phone
  // currently shows the overview. Restoring that overview is not a new link.
  if(previousSelected!=selected){previousSelected=selected;compactOverview=false}
  focusRequester.requestFocus()
 }
 val colors=MaterialTheme.colorScheme
 val groups=listOf("General","Apariencia","Búsqueda y archivos","Integraciones","Permisos y accesos","Inteligencia artificial","Atajos","Avanzado")
 fun navigate(group:String){query="";onSelect(group);compactOverview=false}
 CompositionLocalProvider(LocalSettingsQuery provides query) {
  BoxWithConstraints(modifier.fillMaxWidth()) {
   val wide=maxWidth>=840.dp && androidx.compose.ui.platform.LocalDensity.current.fontScale<1.8f
   fun back():Boolean=when{query.isNotEmpty()->{query="";true};!wide && !compactOverview->{compactOverview=true;true};else->false}
   BackHandler(query.isNotEmpty() || (!wide && !compactOverview)){back()}
   val search:@Composable ()->Unit={
    OutlinedTextField(query,{query=it.take(100)},modifier=Modifier.fillMaxWidth().testTag("settings-search"),singleLine=true,
     placeholder={Text("Buscar un ajuste…",style=MaterialTheme.typography.bodyMedium,maxLines=1,overflow=TextOverflow.Ellipsis)},leadingIcon={IosCommandIcon(CommandIcons.Search,null,Modifier.size(20.dp))},
     trailingIcon={if(query.isNotEmpty())IconButton(onClick={query=""}){IosCommandIcon(CommandIcons.Close,"Limpiar búsqueda",Modifier.size(20.dp))}},
     shape=RoundedCornerShape(14.dp),colors=OutlinedTextFieldDefaults.colors(unfocusedContainerColor=colors.surfaceContainerLow,unfocusedBorderColor=colors.outlineVariant.copy(alpha=.5f)))
   }
   val sectionBody:@Composable ()->Unit={
    val section=if(query.isBlank())selected else "search"
    key(section) { holder.SaveableStateProvider(section) {
     Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("settings-details"),verticalArrangement=Arrangement.spacedBy(20.dp)) {
      if(query.isNotBlank())Text("Resultados de ajustes",style=MaterialTheme.typography.headlineSmall)
      else {
       Text(SettingsGroups.heading(selected),style=MaterialTheme.typography.headlineMedium)
       Text(SettingsGroups.description(selected),style=MaterialTheme.typography.bodyMedium,color=colors.onSurfaceVariant)
      }
      if(selected=="General" && query.isBlank())overview?.invoke{navigate("Atajos")}
      content()
      Spacer(Modifier.height(24.dp))
     }
    }
    }
   }
   val category:@Composable (String,Boolean)->Unit={group,tile->
    Surface(onClick={navigate(group)},modifier=Modifier.fillMaxWidth().testTag("settings-area:$group").semantics{this.selected=selected==group;role=Role.Tab},
     shape=RoundedCornerShape(12.dp),color=if(!tile && selected==group)colors.primary.copy(alpha=.15f) else if(tile)colors.surfaceContainerLow else Color.Transparent) {
     Row(Modifier.heightIn(min=if(tile)76.dp else 50.dp).padding(horizontal=14.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
      IosCommandIcon(SettingsGroups.icon(group),null,Modifier.size(21.dp),tint=if(selected==group)colors.primary else colors.onSurfaceVariant)
      Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {
       Text(SettingsGroups.heading(group),style=MaterialTheme.typography.titleSmall,color=if(!tile && selected==group)colors.primary else colors.onSurface)
       if(tile)Text(SettingsGroups.description(group),style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
      }
      if(tile)IosCommandIcon(CommandIcons.Chevron,null,Modifier.size(14.dp),tint=colors.onSurfaceVariant)
     }
    }
   }
   Row(Modifier.fillMaxSize().onPreviewKeyEvent{it.type==KeyEventType.KeyDown && it.key==Key.Escape && back()}.focusRequester(focusRequester).focusable()) {
    if(wide)Column(Modifier.width(240.dp).fillMaxHeight().background(colors.surfaceContainerLow).padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
     Text("Spotlight",style=MaterialTheme.typography.headlineSmall,modifier=Modifier.padding(vertical=12.dp))
     Text("AJUSTES",style=MaterialTheme.typography.labelSmall,color=colors.onSurfaceVariant,modifier=Modifier.padding(start=14.dp,bottom=16.dp))
     Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(4.dp)) {
      groups.forEachIndexed{i,group->if(i in setOf(3,7))HorizontalDivider(Modifier.padding(vertical=10.dp),color=colors.outlineVariant.copy(alpha=.45f));category(group,false)}
     }
     onInvoke?.let{OutlinedButton(onClick=it,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp)){Text("Abrir Spotlight")}}
    }
    Column(Modifier.weight(1f).fillMaxHeight().padding(if(wide)30.dp else 18.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
     Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
      if(!wide && !compactOverview && query.isBlank())TextButton(onClick={compactOverview=true},modifier=Modifier.weight(1f),contentPadding=PaddingValues(0.dp)){Text("‹ Secciones")}
      else Text("Ajustes",Modifier.weight(1f),style=MaterialTheme.typography.titleLarge)
      if(!wide)onInvoke?.let{IconButton(onClick=it){IosCommandIcon(CommandIcons.Search,"Abrir Spotlight",Modifier.size(22.dp))}}
     }
     search()
     if(!wide && compactOverview && query.isBlank())Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).testTag("settings-overview"),verticalArrangement=Arrangement.spacedBy(12.dp)) {
      Text("A tu manera",style=MaterialTheme.typography.headlineMedium)
      Text("Lo importante primero. Los detalles técnicos, en Avanzado.",style=MaterialTheme.typography.bodyMedium,color=colors.onSurfaceVariant)
      groups.forEach{category(it,true)}
     } else Box(Modifier.weight(1f).fillMaxWidth()){sectionBody()}
    }
   }
  }
 }
}

/** Clear section hierarchy; descriptions and destructive confirmations stay accessible. */
@Composable fun SettingsPreferenceSection(title:String,content:@Composable ColumnScope.()->Unit) {
 val query=LocalSettingsQuery.current
 if(!SettingsGroups.visible(title))return
 if(query.isBlank() && SettingsGroups.of(title)!=LocalSettingsGroup.current)return
 if(query.isNotBlank() && !SettingsGroups.matches(title,query))return
 if(title=="Apariencia") {Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(20.dp),content=content);return}
 var expanded by rememberSaveable(title){mutableStateOf(title !in setOf("Diagnóstico de Spotlight","Transferir configuración","Integraciones automáticas","Tarjetas de información"))}
 val open=expanded || query.isNotBlank()
 Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)) {
  Row(Modifier.fillMaxWidth().heightIn(min=48.dp).clickable(role=Role.Button,onClickLabel=if(open)"Contraer sección" else "Expandir sección"){expanded=!expanded}
   .semantics{stateDescription=if(open)"Expandida" else "Contraída"},verticalAlignment=Alignment.CenterVertically){
   Text(SettingsGroups.sectionLabel(title),Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
   IosCommandIcon(CommandIcons.Chevron,null,Modifier.size(16.dp).rotate(if(open)90f else 0f),tint=MaterialTheme.colorScheme.onSurfaceVariant)
  }
  if(open)SettingsGroupSurface{content()}
 }
}

@Composable fun SettingsGroupSurface(content:@Composable ColumnScope.()->Unit) {
 Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surfaceContainerLow,tonalElevation=0.dp) {
  Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
 }
}

/** Same Puente palette as the overlay; no separate renderer, font or stored theme. */
@Composable internal fun SettingsTheme(config:dev.bautista.spotlight.data.Config,content:@Composable ()->Unit) {
 SpotlightTheme(config.theme,config){PuenteOverlayTheme(config,content)}
}
