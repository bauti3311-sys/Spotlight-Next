package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.search.normalize

val LocalSettingsGroup=staticCompositionLocalOf{"Apariencia"}
val LocalSettingsQuery=staticCompositionLocalOf{""}
object SettingsGroups {
 val names=listOf("Apariencia","Búsqueda","Inteligencia","Integraciones","Invocación","Privacidad y sistema")
 fun of(title:String)=when {
  title.contains("Apariencia")->"Apariencia"
  title.contains("Inteligencia") || title.contains("Audio") || title.contains("Contexto") || title.contains("Texto e imágenes")->"Inteligencia"
  title.contains("Teclado") || title.contains("Acceso rápido") || title.contains("Invocación")->"Invocación"
  title.contains("privacidad",true) || title.contains("Transferir") || title.contains("Shizuku") || title.contains("Diagnóstico")->"Privacidad y sistema"
  title.contains("Tasker") || title.contains("Rutinas") || title.contains("Centro de comandos") || title.contains("Extensiones") || title.contains("Integraciones") || title.contains("WhatsApp") || title.contains("Clima") || title.contains("Tarjetas")->"Integraciones"
  else->"Búsqueda"
 }
 fun icon(group:String)=when(group){"Apariencia"->CommandIcons.Display;"Búsqueda"->CommandIcons.Search;"Inteligencia"->CommandIcons.AutoAwesome;"Integraciones"->CommandIcons.Layers;"Invocación"->CommandIcons.Keyboard;else->CommandIcons.Shield}
 fun matches(title:String,query:String):Boolean {
  val aliases=when(of(title)){"Apariencia"->"tema glass opacidad transparencia fuente color animaciones";"Búsqueda"->"ranking archivos pdf indice notas sugerencias favoritos";"Inteligencia"->"ia modelo clave contexto conversaciones traduccion audio";"Integraciones"->"whatsapp spotify youtube tasker clima deportes";"Invocación"->"atajos teclado gesto posicion ancho orientacion";else->"permisos privado datos privacidad diagnostico shizuku brillo volumen"}
  return normalize(title+" "+aliases).contains(normalize(query.trim()))
 }
}
@Composable fun SettingsTabs(selected:String,onSelect:(String)->Unit) {Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())){SettingsGroups.names.forEach{group->FilterChip(selected==group,{onSelect(group)},label={Text(group)})}}}
@Composable fun SettingsLayout(selected:String,onSelect:(String)->Unit,content:@Composable ()->Unit) {
 var query by remember{mutableStateOf("")};var chooser by remember{mutableStateOf(false)}
 CompositionLocalProvider(LocalSettingsQuery provides query) {
  Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(12.dp)) {
   OutlinedTextField(query,{query=it.take(100)},modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text("Buscar en todos los ajustes")},leadingIcon={Icon(CommandIcons.Search,null,Modifier.size(18.dp))})
   BoxWithConstraints(Modifier.fillMaxWidth()) {
    val sidebar:@Composable ()->Unit={Column(Modifier.padding(8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){SettingsGroups.names.forEach{group->Surface(shape=RoundedCornerShape(10.dp),color=if(selected==group)MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent){TextButton(onClick={query="";onSelect(group);chooser=false},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Icon(SettingsGroups.icon(group),null,Modifier.size(18.dp));Spacer(Modifier.width(10.dp));Text(group,Modifier.weight(1f))}}}}}
    if(maxWidth>=760.dp)Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(24.dp)) {
     Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceContainerLow,modifier=Modifier.width(210.dp)){sidebar()}
     Column(Modifier.weight(1f)){Text(if(query.isBlank())selected else "Resultados de ajustes",style=MaterialTheme.typography.headlineSmall);content()}
    } else Column(Modifier.fillMaxWidth()) {
     OutlinedButton(onClick={chooser=!chooser},modifier=Modifier.fillMaxWidth()){Icon(SettingsGroups.icon(selected),null,Modifier.size(18.dp));Spacer(Modifier.width(12.dp));Text(selected,Modifier.weight(1f));Text(if(chooser)"Cerrar" else "Cambiar")}
     if(chooser)sidebar()
     content()
    }
   }
  }
 }
}
