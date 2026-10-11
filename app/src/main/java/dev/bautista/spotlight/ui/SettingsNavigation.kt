package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val LocalSettingsGroup=staticCompositionLocalOf{"General"}
object SettingsGroups {
    val names=listOf("General","Apariencia","Búsqueda","Aplicaciones","Integraciones","Comandos","WhatsApp y notificaciones","Inteligencia artificial","Automatizaciones","Clima y deportes","Privacidad","Atajos","Avanzado")
    fun of(title:String)=when{
        title.contains("Centro de comandos") || title.contains("Tasker")->"Automatizaciones"
        title.contains("Clima y agenda")->"Clima y deportes"
        title.contains("Extensiones")->"Integraciones"
        title.contains("Shizuku")->"Integraciones"
        title.contains("Apariencia")->"Apariencia"
        title.contains("Índice")->"Búsqueda"
        title.contains("Integraciones automáticas")->"Aplicaciones"
        title.contains("Biblioteca")||title.contains("Herramientas")||title.contains("Texto e imágenes")->"Comandos"
        title.contains("WhatsApp")||title.contains("Integraciones listas")->"WhatsApp y notificaciones"
        title.contains("Inteligencia")||title.contains("Audio")->"Inteligencia artificial"
        title.contains("Rutinas")->"Automatizaciones"
        title.contains("privacidad")->"Privacidad"
        title.contains("Teclado")->"Atajos"
        title.contains("Tarjetas")->"Avanzado"
        else->"General"
    }
    fun icon(group:String)=when(group){"General"->CommandIcons.Settings;"Apariencia"->CommandIcons.Display;"Búsqueda"->CommandIcons.Search;"Aplicaciones"->CommandIcons.Apps;"Integraciones"->CommandIcons.Layers;"Comandos"->CommandIcons.Bolt;"WhatsApp y notificaciones"->CommandIcons.Forum;"Inteligencia artificial"->CommandIcons.AutoAwesome;"Automatizaciones"->CommandIcons.Layers;"Clima y deportes"->CommandIcons.Cloud;"Privacidad"->CommandIcons.Shield;"Atajos"->CommandIcons.Keyboard;else->CommandIcons.Sliders}
}
@Composable fun SettingsTabs(selected:String,onSelect:(String)->Unit){
    Text("Preferencias",style=MaterialTheme.typography.titleMedium)
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){
        SettingsGroups.names.forEach{group->FilterChip(selected==group,{onSelect(group)},label={Text(group)},leadingIcon={Icon(SettingsGroups.icon(group),null,Modifier.size(16.dp))})}
    }
}

@Composable fun SettingsLayout(selected:String,onSelect:(String)->Unit,content:@Composable ()->Unit){
    BoxWithConstraints(Modifier.fillMaxWidth()){
        if(maxWidth>=760.dp)Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(24.dp)){
            Surface(shape=androidx.compose.foundation.shape.RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surface){Column(Modifier.width(210.dp).padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
                Text("Preferencias",Modifier.padding(8.dp),style=MaterialTheme.typography.titleMedium)
                SettingsGroups.names.forEach{group->Surface(shape=androidx.compose.foundation.shape.RoundedCornerShape(10.dp),color=if(selected==group)MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent){TextButton(onClick={onSelect(group)},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Icon(SettingsGroups.icon(group),null,Modifier.size(18.dp));Spacer(Modifier.width(10.dp));Text(group,Modifier.weight(1f),style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurface)}}}
            }}
            Column(Modifier.weight(1f)){Text(selected,style=MaterialTheme.typography.headlineSmall);content()}
        }else Column(Modifier.fillMaxWidth()){
            var menu by remember{mutableStateOf(false)}
            Text("Preferencias",style=MaterialTheme.typography.titleMedium)
            Box{OutlinedButton(onClick={menu=true},modifier=Modifier.fillMaxWidth().padding(vertical=8.dp)){Icon(SettingsGroups.icon(selected),null,Modifier.size(18.dp));Spacer(Modifier.width(10.dp));Text(selected,Modifier.weight(1f));Text("Cambiar")};DropdownMenu(expanded=menu,onDismissRequest={menu=false},modifier=Modifier.heightIn(max=440.dp)){SettingsGroups.names.forEach{group->DropdownMenuItem(text={Text(group)},leadingIcon={Icon(SettingsGroups.icon(group),null,Modifier.size(18.dp))},onClick={onSelect(group);menu=false})}}}
            content()
        }
    }
}
