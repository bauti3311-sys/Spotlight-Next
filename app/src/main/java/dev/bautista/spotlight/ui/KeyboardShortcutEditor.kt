package dev.bautista.spotlight.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.input.*

@Composable fun KeyboardShortcutEditor(config:Config,update:(Config)->Unit) {
    var editing by remember{mutableStateOf<String?>(null)}
    var error by remember{mutableStateOf("")}
    val focus=remember{FocusRequester()}
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Atajos de la paleta",style=MaterialTheme.typography.titleSmall)
        Text("Solo actúan cuando Spotlight tiene foco. Flechas, Enter, Tab y Escape conservan su función. Android y otras aplicaciones pueden reservar combinaciones.",style=MaterialTheme.typography.bodySmall)
        KeyboardBindings.titles.forEach{(id,title)->
            Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                Text(title,Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium)
                TextButton(onClick={editing=id;error=""},modifier=Modifier.heightIn(min=48.dp).testTag("shortcut-edit-$id")) {
                    Text(KeyboardBindings.resolve(config,id)?.label ?: "Sin atajo")
                }
            }
        }
        if(editing!=null) {
            Text("Presioná la combinación para ${KeyboardBindings.titles[editing]}. Escape cancela.",style=MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick={},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).focusRequester(focus)
                .onPreviewKeyEvent{e->
                    if(e.type!=KeyEventType.KeyDown)true
                    else if(e.key==Key.Escape){editing=null;true}
                    else if(android.view.KeyEvent.isModifierKey(e.nativeKeyEvent.keyCode))true
                    else {
                        val chord=KeyChord.capture(e.nativeKeyEvent)
                        val id=editing!!
                        val issue=KeyboardBindings.conflict(config,id,chord)
                        if(issue!=null)error=issue else {update(KeyboardBindings.assign(config,id,chord));editing=null;error=""}
                        true
                    }
                }.focusable().testTag("shortcut-capture")) {Text("Esperando combinación…")}
            LaunchedEffect(editing){focus.requestFocus()}
        }
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
        TextButton(onClick={update(config.copy(clipboardShortcut="off",keyBindings=config.keyBindings.filterNot{it.startsWith("clipboard=")}.toSet()))}){Text("Desactivar atajo de portapapeles")}
        TextButton(onClick={update(config.copy(keyBindings=emptySet()));editing=null;error=""}){Text("Restablecer atajos de la paleta")}
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun GlobalKeyboardShortcutOptions(config:Config,update:(Config)->Unit) {
    var warning by remember{mutableStateOf("")}
    Text("Acceso global · servicio de teclado optativo",style=MaterialTheme.typography.titleSmall)
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        listOf("ctrl_alt" to "Ctrl+Alt+Espacio","alt" to "Alt+Espacio","ctrl" to "Ctrl+Espacio").forEach{(key,label)->
            FilterChip(config.shortcut==key,onClick={
                val candidate=config.copy(shortcut=key)
                val conflict=KeyboardBindings.titles.keys.firstOrNull{KeyboardBindings.resolve(candidate,it)==KeyboardBindings.global(candidate)}
                if(conflict!=null)warning="Conflicto con ${KeyboardBindings.titles[conflict]}. Cambiá ese atajo primero."
                else {warning="";update(candidate)}
            },label={Text(label)})
        }
    }
    val legacyConflict=KeyboardBindings.titles.keys.firstOrNull{KeyboardBindings.resolve(config,it)==KeyboardBindings.global(config)}
    if(warning.isNotBlank() || legacyConflict!=null)Text(warning.ifBlank{"El acceso global coincide con ${KeyboardBindings.titles[legacyConflict]}; reasigná uno para evitar que Android lo intercepte."},color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
}
