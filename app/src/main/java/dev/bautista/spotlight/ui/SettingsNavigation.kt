package dev.bautista.spotlight.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
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
  title.startsWith("Privacidad") || title.startsWith("Portapapeles") || title.startsWith("Permisos") || title.startsWith("Contactos autorizados")->"Permisos y accesos"
  title.startsWith("Invocación") || title.startsWith("Teclado") || title=="Acceso rápido"->"Atajos"
  title.startsWith("Diagnóstico") || title.startsWith("Transferir") || title.startsWith("Alias") || title=="Búsqueda · prioridades"->"Avanzado"
  title.startsWith("Extensiones") || title.startsWith("Integraciones") || title.startsWith("WhatsApp") || title.startsWith("Contactos") || title.startsWith("Clima") || title.startsWith("Shizuku") || title.startsWith("Tarjetas")->"Integraciones"
  title.startsWith("Búsqueda") || title.startsWith("Índice") || title.startsWith("Texto e imágenes")->"Búsqueda y archivos"
  else->"General"
 }
 fun icon(group:String)=when(canonical(group)){"Apariencia"->CommandIcons.Display;"Búsqueda y archivos"->CommandIcons.Folder;"Inteligencia artificial"->CommandIcons.AutoAwesome;"Integraciones"->CommandIcons.Layers;"Atajos"->CommandIcons.Keyboard;"General"->CommandIcons.Settings;"Avanzado"->CommandIcons.Info;else->CommandIcons.Shield}
 fun description(group:String)=when(canonical(group)){
  "General"->"Inicio, sugerencias y formas de abrir Spotlight."
  "Apariencia"->"Materiales, acento y un único control de ancho."
  "Inteligencia artificial"->"Proveedor, credenciales y contexto bajo tu control."
  "Búsqueda y archivos"->"Fuentes autorizadas, índice y reconocimiento local."
  "Integraciones"->"Aplicaciones, conexiones y herramientas disponibles."
  "Atajos"->"Atajos globales, teclado físico e invocación de la barra."
  "Permisos y accesos"->"Todos los permisos Android y servicios especiales en un único lugar."
  else->"Alias, prioridades, respaldo y diagnóstico."
 }
 fun summary(title:String)=when {
  title=="Apariencia"->"Obsidian Glass, Graphite y macOS Light"
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
   title=="Apariencia"->"tema glass graphite macos claro oscuro ancho color acento animaciones movimiento densidad"
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

/** Bounded preference workspace: the category rail stays still while details scroll. */
@Composable fun SettingsLayout(selected:String,onSelect:(String)->Unit,modifier:Modifier=Modifier,overview:(@Composable (()->Unit)->Unit)?=null,content:@Composable ()->Unit) {
 var query by rememberSaveable{mutableStateOf("")};var chooser by rememberSaveable{mutableStateOf(false)}
 var generalDetails by rememberSaveable{mutableStateOf(false)}
 var showAdvanced by rememberSaveable{mutableStateOf(false)}
 val visibleGroups=SettingsGroups.names.filter{it!="Avanzado" || showAdvanced || selected=="Avanzado"}
 val holder=rememberSaveableStateHolder()
 fun back():Boolean=when{chooser->{chooser=false;true};query.isNotEmpty()->{query="";true};generalDetails->{generalDetails=false;true};else->false}
 BackHandler(chooser || query.isNotEmpty() || generalDetails){back()}
 CompositionLocalProvider(LocalSettingsQuery provides query) {
  Column(modifier.fillMaxWidth().onPreviewKeyEvent{it.type==KeyEventType.KeyDown && it.key==Key.Escape && back()},verticalArrangement=Arrangement.spacedBy(16.dp)) {
   OutlinedTextField(query,{query=it.take(100)},modifier=Modifier.fillMaxWidth().testTag("settings-search"),singleLine=true,label={Text("Buscar en todos los ajustes",maxLines=1,overflow=TextOverflow.Ellipsis)},leadingIcon={Icon(CommandIcons.Search,null,Modifier.size(18.dp))},trailingIcon={if(query.isNotEmpty())IconButton(onClick={query=""}){Icon(CommandIcons.Close,"Limpiar búsqueda",Modifier.size(18.dp))}},shape=RoundedCornerShape(12.dp))
   BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
    val category:@Composable (String)->Unit={group->
     Row(Modifier.fillMaxWidth().selectable(selected==group,onClick={query="";onSelect(group);chooser=false;generalDetails=false},role=Role.Tab)
      .background(if(selected==group)MaterialTheme.colorScheme.primary.copy(alpha=.11f) else androidx.compose.ui.graphics.Color.Transparent,RoundedCornerShape(9.dp))
      .heightIn(min=48.dp).padding(horizontal=12.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically){
       Icon(SettingsGroups.icon(group),null,Modifier.size(18.dp),tint=if(selected==group)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
       Spacer(Modifier.width(12.dp));Text(group,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium,
           fontWeight=if(selected==group)FontWeight.SemiBold else FontWeight.Normal,maxLines=2,overflow=TextOverflow.Ellipsis)
     }
    }
    val detail:@Composable ()->Unit={
     holder.SaveableStateProvider(if(query.isBlank())selected else "search") {
      Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("settings-details"),verticalArrangement=Arrangement.spacedBy(8.dp)) {
       Text(if(query.isNotBlank())"Resultados de ajustes" else if(selected=="General" && overview!=null && !generalDetails)"Inicio" else SettingsGroups.canonical(selected),style=MaterialTheme.typography.titleLarge)
       Text(if(query.isNotBlank())"Se muestran controles de todas las áreas que coinciden con tu búsqueda." else if(selected=="General" && overview!=null && !generalDetails)"Configurá Spotlight para vos." else SettingsGroups.description(selected),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
       if(selected=="General" && query.isBlank() && overview!=null && !generalDetails)overview{generalDetails=true} else content();Spacer(Modifier.height(24.dp))
      }
     }
    }
    if(maxWidth>=760.dp)Row(Modifier.fillMaxSize(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
     Surface(shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.onSurface.copy(alpha=.035f)) {
      Column(Modifier.width(216.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(10.dp),
          verticalArrangement=Arrangement.spacedBy(4.dp)) {
       Text("SPOTLIGHT NEXT",Modifier.padding(horizontal=12.dp,vertical=14.dp),
           style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
       visibleGroups.forEach{category(it)}
       TextButton(onClick={showAdvanced=!showAdvanced}){Text(if(showAdvanced)"Menos opciones" else "Más opciones")}
      }
     }
     Surface(modifier=Modifier.weight(1f).fillMaxHeight(),shape=RoundedCornerShape(16.dp),
         color=MaterialTheme.colorScheme.onSurface.copy(alpha=.022f)) {
      Box(Modifier.fillMaxSize().padding(18.dp)){detail()}
     }
    } else Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(12.dp)) {
     Box {
      OutlinedButton(onClick={chooser=true},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(10.dp)) {Icon(SettingsGroups.icon(selected),null,Modifier.size(18.dp));Spacer(Modifier.width(12.dp));Text(SettingsGroups.canonical(selected),Modifier.weight(1f));Text("Cambiar")}
      DropdownMenu(chooser,{chooser=false},modifier=Modifier.heightIn(max=400.dp)){visibleGroups.forEach{group->DropdownMenuItem(text={Text(group)},leadingIcon={Icon(SettingsGroups.icon(group),null,Modifier.size(18.dp))},onClick={query="";onSelect(group);chooser=false;generalDetails=false})};DropdownMenuItem(text={Text(if(showAdvanced)"Menos opciones" else "Más opciones")},onClick={showAdvanced=!showAdvanced})}
     }
     Box(Modifier.weight(1f).fillMaxWidth()){detail()}
    }
   }
  }
 }
}

@Composable fun SettingsPreferenceSection(title:String,content:@Composable ColumnScope.()->Unit) {
 val query=LocalSettingsQuery.current
 if(!SettingsGroups.visible(title))return
 if(query.isBlank() && SettingsGroups.of(title)!=LocalSettingsGroup.current)return
 if(query.isNotBlank() && !SettingsGroups.matches(title,query))return
 if(title=="Apariencia") { Column(Modifier.fillMaxWidth().padding(top=12.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content);return }
 var expanded by rememberSaveable(title){mutableStateOf(title in setOf("Apariencia","General · inicio","Inteligencia artificial · opcional","Permisos y accesos"))}
 val open=expanded || query.isNotBlank()
 Column(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
  Row(Modifier.fillMaxWidth().heightIn(min=56.dp).clickable(role=Role.Button,onClickLabel=if(open)"Contraer sección" else "Expandir sección"){expanded=!expanded}
   .semantics{stateDescription=if(open)"Expandida" else "Contraída"}.padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
   Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleSmall);Text(SettingsGroups.summary(title),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
   Text(if(open)"−" else "+",Modifier.padding(start=12.dp),style=MaterialTheme.typography.titleMedium)
  }
  if(open)Column(Modifier.fillMaxWidth().padding(bottom=12.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
 }
}
