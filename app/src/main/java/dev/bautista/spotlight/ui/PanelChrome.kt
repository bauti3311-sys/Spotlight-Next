package dev.bautista.spotlight.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.advanced.AdvancedState
fun panelLabel(page:String)=when(page){"catalog"->"CENTRO DE COMANDOS";"profiles"->"PERFILES";"display-review"->"PANTALLA";"tasker"->"TASKER";"weather-native"->"CLIMA";"sports-native"->"DEPORTES";"agent"->"IA · HERRAMIENTAS";"focus"->"CONCENTRACIÓN";"extensions"->"INTEGRACIONES";"device"->"DISPOSITIVO";"app"->"APLICACIÓN";"controls"->"SISTEMA";"shizuku"->"SHIZUKU";else->"AJUSTES"}
@Composable fun InfoField(title:String,value:String,icon:ImageVector){
 Surface(color=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.65f),shape=MaterialTheme.shapes.medium){Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,Modifier.size(22.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(value,style=MaterialTheme.typography.bodyMedium)}}}
}
@Composable fun ConnectionCard(state:AdvancedState){Surface(shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.primaryContainer){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(CommandIcons.Shield,null,Modifier.size(24.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(12.dp));Column{Text(state.status.label,style=MaterialTheme.typography.titleSmall);Text(if(state.available)"Controles ADB disponibles" else "Las funciones locales siguen disponibles",style=MaterialTheme.typography.bodySmall)}}}}
