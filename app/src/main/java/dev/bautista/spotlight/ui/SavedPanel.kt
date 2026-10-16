package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.personal.PersonalCommands
import dev.bautista.spotlight.search.*

/** One presentation over existing stores: no destructive migration or duplicate database. */
@Composable fun SavedPanel(app:SpotlightApp,c:Config,onAction:(Action)->Unit) {
    val notes by app.local.notes.collectAsState();val contexts by app.contexts.pinned.collectAsState();val chats by app.contexts.conversations.collectAsState()
    var filter by remember{mutableStateOf("")}
    val rows=remember(notes,contexts,chats,c.snippets,c.pins,c.quickLinks,c.routines,filter){
        val base=notes.map{Result("note:${it.id}",it.text.lineSequence().first().take(100),"Nota · editar","Guardados",0,Action.EditNote(it.id,it.text))}+
            dev.bautista.spotlight.automation.NativeCommands.results(c.routines)+
            c.pins.map{Result("pin:$it",it,"Consulta fijada","Guardados",0,Action.RestoreQuery(it))}+
            contexts.map{Result("context:${it.id}",it.title,"Documento, enlace o texto fijado","Guardados",0,Action.SystemPanel("context",it.id))}+
            chats.map{Result("conversation:${it.id}",it.title,"Conversación guardada","Guardados",0,Action.SystemPanel("conversation",it.id))}+
            c.quickLinks.mapNotNull{entry->val parts=entry.split('|',limit=2);if(parts.size!=2 || !parts[1].startsWith("https://"))null else Result("link:$entry",parts[0],"Enlace guardado","Guardados",0,Action.Launch(android.content.Intent(android.content.Intent.ACTION_VIEW,android.net.Uri.parse(parts[1].replace("{q}",""))))) }
        base.filter{filter.isBlank() || normalize(it.title).contains(normalize(filter))}
    }
    Column(Modifier.fillMaxWidth().heightIn(max=400.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Guardados",style=MaterialTheme.typography.titleLarge)
        Row{TextButton(onClick={onAction(Action.NewNote())}){Text("Nueva nota")};TextButton(onClick={onAction(Action.SystemPanel("context"))}){Text("Fijar texto o documento")}}
        OutlinedTextField(filter,{filter=it.take(120)},label={Text("Buscar en Guardados")},singleLine=true,modifier=Modifier.fillMaxWidth())
        rows.forEach{item->TextButton(onClick={onAction(item.action)},modifier=Modifier.fillMaxWidth()){Column(Modifier.fillMaxWidth()){Text(item.title);Text(item.subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
        if(rows.isEmpty())Text("No hay elementos guardados con esta búsqueda. Podés crear una nota o fijar contenido autorizado.")
    }
}
