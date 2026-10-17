package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.FileBuffer

@Composable fun FileBufferTray(buffer:FileBuffer,busy:Boolean,onShare:()->Unit,onAi:(String)->Unit,onCancel:()->Unit={}) {
    Column(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=6.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Selección · ${buffer.items.size}",style=MaterialTheme.typography.titleSmall);TextButton(enabled=!busy,onClick={buffer.clear()}){Text("Vaciar")}}
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            buffer.items.forEach{item->InputChip(selected=true,onClick={buffer.remove(item.id)},label={Text(item.title.take(30))},trailingIcon={Icon(CommandIcons.Close,"Quitar ${item.title}",Modifier.size(16.dp))})}
        }
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            TextButton(enabled=!busy,onClick=onShare){Text("Compartir")}
            TextButton(onClick={if(busy)onCancel() else onAi("Resumí los documentos seleccionados por separado y luego sintetizá sus puntos comunes.")}){Text(if(busy)"Cancelar preparación" else "Preparar resumen")}
            TextButton(enabled=!busy && buffer.items.size>1,onClick={onAi("Compará el contenido de los documentos seleccionados, indicando acuerdos, diferencias y referencias al documento de origen.")}){Text("Comparar con IA")}
        }
    }
}
