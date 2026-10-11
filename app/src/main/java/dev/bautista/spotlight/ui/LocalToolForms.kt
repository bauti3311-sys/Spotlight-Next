package dev.bautista.spotlight.ui

import android.content.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.provider.AlarmClock
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.personal.PersonalCommands

@Composable fun ClipboardEditor(original:String,onSave:(String)->Unit,onDismiss:()->Unit){
    var text by remember(original){mutableStateOf(original)}
    Column(Modifier.fillMaxWidth().heightIn(max=340.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text("Editar texto del portapapeles",style=MaterialTheme.typography.titleMedium)
        Text("Solo cambia el historial temporal de Spotlight. Guardalo como fragmento para conservarlo.",style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(text,{text=it.take(8000)},minLines=3,maxLines=8,modifier=Modifier.fillMaxWidth())
        Row{Button(enabled=text.isNotBlank(),onClick={onSave(text)}){Text("Guardar cambio")};TextButton(onClick=onDismiss){Text("Cancelar")}}
    }
}
@Composable fun SnippetForm(template:String,onCopy:(String)->Unit,onDismiss:()->Unit){
    val fields=remember(template){PersonalCommands.fields(template)};val values=remember(template){mutableStateMapOf<String,String>()}
    val output=PersonalCommands.fill(template,values)
    Column(Modifier.fillMaxWidth().heightIn(max=340.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text("Completar fragmento",style=MaterialTheme.typography.titleMedium)
        if(fields.size>8)Text("Esta plantilla tiene más de 8 campos. Editala desde Comandos.",color=MaterialTheme.colorScheme.error)
        fields.take(8).forEach{field->OutlinedTextField(values[field].orEmpty(),{values[field]=it.take(1000)},label={Text(field.replaceFirstChar{it.titlecase()})},singleLine=true,modifier=Modifier.fillMaxWidth())}
        SelectionContainer{Text(output.take(4000))}
        Row{Button(enabled=fields.size<=8 && fields.all{!values[it].isNullOrBlank()},onClick={onCopy(output)}){Text("Copiar fragmento")};TextButton(onClick=onDismiss){Text("Cancelar")}}
    }
}
@Composable fun TimerForm(initialMinutes:Int,onDismiss:()->Unit){
    val context=LocalContext.current;var minutes by remember{mutableStateOf(initialMinutes.toString())};var error by remember{mutableStateOf("")}
    Column(Modifier.fillMaxWidth().heightIn(max=340.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text("Temporizador",style=MaterialTheme.typography.titleMedium)
        OutlinedTextField(minutes,{minutes=it.filter(Char::isDigit).take(4)},label={Text("Minutos")},singleLine=true,modifier=Modifier.fillMaxWidth())
        Text("Se inicia con tu app Reloj compatible, que mantiene la cuenta al cerrar Spotlight.",style=MaterialTheme.typography.bodySmall)
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        Row{Button(onClick={val number=minutes.toIntOrNull();if(number==null||number !in 1..1440)error="Elegí entre 1 y 1440 minutos." else {val intent=Intent(AlarmClock.ACTION_SET_TIMER).putExtra(AlarmClock.EXTRA_LENGTH,number*60).putExtra(AlarmClock.EXTRA_SKIP_UI,false).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);if(context.packageManager.resolveActivity(intent,0)==null)error="No hay una app Reloj compatible instalada." else runCatching{context.startActivity(intent);onDismiss()}.onFailure{error="Android no permitió abrir el temporizador."}}}){Text("Iniciar con Reloj")};TextButton(onClick=onDismiss){Text("Cancelar")}}
    }
}
