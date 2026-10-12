package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config

@Composable fun GlassQuickMenu(c:Config,update:(Config)->Unit,onSettings:()->Unit,onFilter:(String)->Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal=12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Icon(CommandIcons.Display,null,Modifier.size(18.dp));TextButton(onClick={update(c.copy(theme=when(c.theme){"auto"->"light";"light"->"dark";else->"auto"}))}){Text("Tema · "+when(c.theme){"light"->"Claro";"dark"->"Oscuro";else->"Automático"})}}
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Icon(CommandIcons.Shield,null,Modifier.size(18.dp));Spacer(Modifier.width(12.dp));Text("Modo privado",Modifier.weight(1f));Switch(c.privateMode,{update(c.copy(privateMode=it))},Modifier.semantics{contentDescription="Modo privado"})}
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Animaciones breves",Modifier.weight(1f));Switch(c.animations,{update(c.copy(animations=it))})}
        TextButton(onClick=onSettings,modifier=Modifier.fillMaxWidth()){Icon(CommandIcons.Settings,null,Modifier.size(18.dp));Spacer(Modifier.width(12.dp));Text("Configuración completa",Modifier.weight(1f))}
        HorizontalDivider()
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){
            listOf("Todo" to CommandIcons.Search,"Apps" to CommandIcons.Apps,"Archivos" to CommandIcons.Folder,"Acciones" to CommandIcons.Bolt,"IA" to CommandIcons.AutoAwesome).forEachIndexed{i,(mode,icon)->
                val label=when(mode){"Apps"->"Aplicaciones";"Acciones"->"Comandos";else->mode}
                IconButton(onClick={onFilter(mode)},modifier=Modifier.size(48.dp)){Icon(icon,if(i==0)"Búsqueda universal" else "$label · Ctrl/⌘+$i",Modifier.size(19.dp))}
            }
        }
    }
}
