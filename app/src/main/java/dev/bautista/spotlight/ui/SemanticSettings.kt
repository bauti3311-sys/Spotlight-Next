package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.*

@Composable fun SemanticSettings(app:SpotlightApp,c:Config) {
    val scope=rememberCoroutineScope()
    val model by app.index.semanticModel.state.collectAsState()
    val status by app.index.semanticStatus.collectAsState()
    var download by remember{mutableStateOf<Job?>(null)}
    var confirm by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf("")}
    LaunchedEffect(Unit){app.index.semanticModel.ready();app.index.refreshSemanticStatus()}
    Text("Busca conceptos en los fragmentos de texto ya autorizados e indexados. El modelo es local y no envía consultas, archivos ni embeddings a un proveedor.",style=MaterialTheme.typography.bodySmall)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Búsqueda por significado",Modifier.weight(1f))
        Switch(c.semanticSearch,onCheckedChange={enabled->
            if(!enabled)app.index.cancelSemanticWork()
            scope.launch{app.preferences.updateSemantic(enabled)}
        },enabled=c.files && c.contentIndex && !c.privateMode && model.installed && !model.busy)
    }
    if(!c.files || !c.contentIndex)Text("Activá la búsqueda de archivos y su contenido de texto para preparar esta búsqueda.",style=MaterialTheme.typography.bodySmall)
    Text(model.message,style=MaterialTheme.typography.bodySmall)
    if(model.busy){LinearProgressIndicator(Modifier.fillMaxWidth());Text("${model.bytes/1_000_000} / 138 MB",style=MaterialTheme.typography.bodySmall)}
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        if(!model.installed && !model.busy)TextButton(onClick={confirm=true},enabled=!c.privateMode){Text("Instalar modelo local · 138 MB")}
        if(model.busy)TextButton(onClick={download?.cancel()}){Text("Cancelar descarga")}
        if(model.installed)ConfirmedSettingsButton("Eliminar modelo e índice","Elimina el modelo descargado y sus vectores. Conserva archivos, texto indexado y preferencias restantes.",enabled=!model.busy && !status.busy){
            scope.launch{try{app.preferences.updateSemantic(false);app.index.clearSemantic();app.index.semanticModel.remove()}catch(e:CancellationException){throw e}catch(_:Exception){error="No se pudo eliminar el modelo local."}}
        }
    }
    Text(status.message,style=MaterialTheme.typography.bodySmall)
    if(status.busy)LinearProgressIndicator(Modifier.fillMaxWidth())
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        TextButton(onClick={scope.launch{app.index.buildSemantic(c)}},enabled=c.semanticSearch && c.contentIndex && c.files && !c.privateMode && model.installed && !status.busy){Text("Actualizar significado")}
        TextButton(onClick={app.index.cancelSemanticWork()},enabled=status.busy){Text("Cancelar preparación")}
    }
    Text("DistilUSE multilingüe · Apache 2.0. Hasta 200 archivos, 1000 fragmentos y 8 por archivo en cada preparación. La cobertura puede ser parcial. Se aplaza con pocos recursos; podés reanudar sin perder fragmentos completados. La búsqueda habitual aparece primero.",style=MaterialTheme.typography.bodySmall)
    if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
    if(confirm)AlertDialog(onDismissRequest={confirm=false},title={Text("Instalar modelo local")},text={Text("Descargará 138 MB desde el repositorio público de Sentence Transformers en Hugging Face. Usá una conexión adecuada a tu plan de datos. Se necesitan unos 170 MB libres. Solo se descarga el modelo; no se envían tus documentos o consultas ni se activa la búsqueda automáticamente.")},confirmButton={TextButton(onClick={
        confirm=false;error="";download=scope.launch{try{app.index.semanticModel.install()}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo instalar el modelo."}}
    }){Text("Descargar modelo")}},dismissButton={TextButton(onClick={confirm=false}){Text("Cancelar")}})
}
