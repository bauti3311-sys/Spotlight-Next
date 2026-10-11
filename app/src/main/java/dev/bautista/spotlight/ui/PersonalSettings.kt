package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.personal.PersonalCommands
import dev.bautista.spotlight.search.normalize

@Composable fun PersonalSettings(config: Config, update: (Config)->Unit) {
    var name by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AliasSettings(config,update)
        HorizontalDivider()
        Text("Textos reutilizables",style=MaterialTheme.typography.titleMedium)
        Text("Buscá con snippet nombre o snippets. {fecha} y {hora} se completan al buscar; {nombre}, {entrada} u otros campos muestran un formulario. Máximo 8 campos. Copiá el texto y pegalo en la app que uses.",fontSize=12.sp)
        OutlinedTextField(name,{name=it.take(80)},label={Text("Nombre del texto")},singleLine=true,modifier=Modifier.fillMaxWidth())
        OutlinedTextField(text,{text=it.take(8000)},label={Text("Texto o plantilla")},minLines=2,maxLines=6,modifier=Modifier.fillMaxWidth())
        TextButton(onClick={
            if(name.trim().isNotBlank()&&!name.contains('|')&&text.isNotBlank()&&PersonalCommands.fields(text).size<=8) {
                update(config.copy(snippets=config.snippets.filterNot { normalize(it.substringBefore('|'))==normalize(name.trim()) }.toSet()+"${name.trim()}|$text"))
                name="";text="";error=""
            } else error="Completá el nombre y el texto; el nombre no puede contener |. Máximo 8 campos por plantilla."
        }){Text("Guardar texto")}
        config.snippets.sorted().forEach { entry -> Row {
            Text(entry.substringBefore('|'),Modifier.weight(1f))
            TextButton(onClick={name=entry.substringBefore('|');text=entry.substringAfter('|')}){Text("Editar")}
            TextButton(onClick={update(config.copy(snippets=config.snippets-entry))}){Text("Eliminar")}
        }}
        HorizontalDivider()
        Text("Consultas fijadas",style=MaterialTheme.typography.titleMedium)
        Text("Buscalas con fijados o favoritos. Tocarlas carga la consulta para revisar o completar; no ejecuta la acción automáticamente.",fontSize=12.sp)
        OutlinedTextField(pin,{pin=it.take(4000)},label={Text("Consulta o comando")},singleLine=true,modifier=Modifier.fillMaxWidth())
        TextButton(onClick={
            if(pin.isNotBlank() && (pin.trim() in config.pins || config.pins.size<50)) {
                update(config.copy(pins=config.pins+pin.trim()));pin="";error=""
            } else error="Escribí una consulta. Podés fijar hasta 50."
        }){Text("Fijar consulta")}
        config.pins.sorted().forEach { entry -> Row {
            Text(entry,Modifier.weight(1f),fontSize=12.sp)
            TextButton(onClick={update(config.copy(pins=config.pins-entry))}){Text("Eliminar")}
        }}
        if(error.isNotBlank()) Text(error,color=MaterialTheme.colorScheme.error,fontSize=12.sp)
    }
}
