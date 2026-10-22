package dev.bautista.spotlight.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.personal.PersonalCommands
import dev.bautista.spotlight.search.normalize
import kotlinx.coroutines.*

@Composable fun AliasSettings(config:Config,update:(Config)->Unit){
    val context=LocalContext.current
    val choices by produceState<List<String>>(emptyList()){value=withContext(Dispatchers.IO){(context.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0).map{it.loadLabel(context.packageManager).toString()}+listOf("crear nota","preguntar a IA","temporizador","herramientas de texto","sonido","bluetooth","portapapeles")).distinct().sorted()}}
    var alias by remember{mutableStateOf("")};var target by remember{mutableStateOf("")};var open by remember{mutableStateOf(false)};var error by remember{mutableStateOf("")}
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text("Alias opcionales",style=MaterialTheme.typography.titleMedium)
        Text("Usá {args} para insertar los argumentos. Una aplicación con nombre exacto conserva prioridad.",style=MaterialTheme.typography.bodySmall)
        Text("Las integraciones funcionan sin alias. Elegí una app o un comando para asignarle un nombre corto; no necesitás enlaces.",style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(alias,{alias=it.take(24)},label={Text("Nombre corto")},singleLine=true,modifier=Modifier.fillMaxWidth())
        Box{OutlinedButton(onClick={open=true}){Text(target.ifBlank{"Elegir aplicación o comando"})};DropdownMenu(open,{open=false}){choices.forEach{choice->DropdownMenuItem(text={Text(choice)},onClick={target=choice;open=false})}}}
        OutlinedTextField(target,{target=it.take(950)},label={Text("Destino y argumentos")},minLines=1,maxLines=3,modifier=Modifier.fillMaxWidth())
        TextButton(enabled=target.isNotBlank(),onClick={if(choices.any{normalize(it.trim())==normalize(alias.trim())})error="Ese nombre ya pertenece a una aplicación o comando. Elegí otro alias." else if(!PersonalCommands.validAlias(alias))error="Usá letras, números o guiones, sin espacios." else {val items=config.aliases.filterNot{normalize(it.substringBefore('|'))==normalize(alias)}.toSet()+"${alias.trim()}|$target";if(PersonalCommands.expand("$alias ejemplo",items)=="$alias ejemplo")error="El alias forma un ciclo." else {update(config.copy(aliases=items));alias="";target="";error=""}}}){Text("Guardar alias")}
        config.aliases.sorted().forEach{entry->Row{Text(entry.substringBefore('|')+" → "+entry.substringAfter('|'),Modifier.weight(1f));TextButton(onClick={update(config.copy(aliases=config.aliases-entry))}){Text("Eliminar")}}}
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
    }
}
