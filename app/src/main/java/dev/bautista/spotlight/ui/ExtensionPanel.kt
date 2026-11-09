package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.extensions.*
import dev.bautista.spotlight.search.*

fun extensionIcon(icon:ExtensionIcon)=when(icon){ExtensionIcon.APPS->CommandIcons.Apps;ExtensionIcon.FILES->CommandIcons.Folder;ExtensionIcon.PEOPLE->CommandIcons.Person;ExtensionIcon.CHAT->CommandIcons.Forum;ExtensionIcon.CALENDAR->CommandIcons.Event;ExtensionIcon.MAPS->CommandIcons.Location;ExtensionIcon.MUSIC->CommandIcons.MusicNote;ExtensionIcon.WEB->CommandIcons.Language;ExtensionIcon.WEATHER->CommandIcons.Cloud;ExtensionIcon.SPORTS->CommandIcons.Sports;ExtensionIcon.AI->CommandIcons.AutoAwesome;ExtensionIcon.NOTE->CommandIcons.EditNote;ExtensionIcon.SNIPPET->CommandIcons.Snippet;ExtensionIcon.CLIPBOARD->CommandIcons.ContentCopy;ExtensionIcon.WORKFLOW->CommandIcons.Layers;ExtensionIcon.TOOLS->CommandIcons.Calculate;ExtensionIcon.SETTINGS->CommandIcons.Settings;ExtensionIcon.SYSTEM->CommandIcons.Display;ExtensionIcon.SHIELD->CommandIcons.Shield}
@Composable fun ExtensionPanel(app:SpotlightApp,config:Config,onExecute:((Action)->Unit)?=null) {
    var items by remember{mutableStateOf<List<ExtensionSnapshot>>(emptyList())};var selected by remember{mutableStateOf<String?>(null)};var filter by remember{mutableStateOf("")};var revision by remember{mutableIntStateOf(0)}
    LaunchedEffect(config,revision){items=ExtensionRegistry.snapshot(app,config).filter{ProductVisibility.allowedExtension(it.definition.id)}}
    Column(Modifier.fillMaxWidth().heightIn(max=340.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        Text("Extensiones e integraciones",style=MaterialTheme.typography.titleMedium)
        Text("Spotlight Extensions · activá solamente los módulos que querés usar.",style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(filter,{filter=it.take(100)},singleLine=true,label={Text("Filtrar extensiones")},modifier=Modifier.fillMaxWidth())
        items.filter{normalize(it.definition.name).contains(normalize(filter))}.forEach{item->val e=item.definition
            Surface(shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surfaceVariant){Column(Modifier.fillMaxWidth().padding(10.dp)){
                TextButton(onClick={selected=if(selected==e.id)null else e.id},modifier=Modifier.fillMaxWidth()){IosCommandIcon(extensionIcon(e.icon),null,Modifier.size(20.dp));Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(e.name,modifier=Modifier.fillMaxWidth());Text(item.availability.label,style=MaterialTheme.typography.bodySmall,modifier=Modifier.fillMaxWidth())};IosCommandIcon(CommandIcons.Chevron,null,Modifier.size(16.dp))}
                if(selected==e.id){Row{Switch(ExtensionStore.enabled(app,e.id),{ExtensionStore.setEnabled(app,e.id,it);revision++});Text("Activada",Modifier.padding(12.dp))};Text(e.explanation,style=MaterialTheme.typography.bodySmall);e.commands.forEach{command->if(onExecute!=null && item.availability==ExtensionAvailability.AVAILABLE)TextButton(onClick={onExecute(command.action)}){Text(command.title)} else Text(command.title,Modifier.padding(vertical=6.dp),style=MaterialTheme.typography.bodySmall)};if(e.registeredTools.isNotEmpty())Text("Herramientas IA: "+e.registeredTools.joinToString{it.title},style=MaterialTheme.typography.bodySmall);if(e.requirements.permissions.isNotEmpty())Text("Permisos: "+e.requirements.permissions.joinToString{it.substringAfterLast('.')},style=MaterialTheme.typography.labelSmall);e.advancedAdapter?.let{Text("Ampliación opcional con Shizuku; cada operación comprueba su autorización.",style=MaterialTheme.typography.labelSmall)}}
            }}
        }
        if(items.isNotEmpty() && items.none{normalize(it.definition.name).contains(normalize(filter))})Text("No hay extensiones con ese nombre.")
    }
}
