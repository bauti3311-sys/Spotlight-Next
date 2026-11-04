package dev.bautista.spotlight.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.data.FileBuffer
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.sharing.FileShareContract
import kotlinx.coroutines.*

/** Uses the existing contextual inspector and launch callback; authorization is never saved. */
@Composable internal fun FileShareEditor(buffer:FileBuffer,packageName:String?=null,onRequest:(Intent)->Unit,onCancel:()->Unit,isCurrent:()->Boolean={true}) {
    val context=LocalContext.current
    val items=buffer.items
    val current=isCurrent()
    val latestCurrent by rememberUpdatedState(isCurrent)
    val identity=items.map{listOf(it.id,(it.action as? Action.Launch)?.intent?.data?.toString(),it.title,it.mime)}
    val latestRequest by rememberUpdatedState(onRequest)
    var reviewed by remember(identity,packageName){mutableStateOf<FileShareContract.Prepared?>(null)}
    var busy by remember(identity,packageName){mutableStateOf(false)}
    var attempted by remember(identity,packageName){mutableStateOf(false)}
    var error by remember(identity,packageName){mutableStateOf("")}
    var job by remember(identity,packageName){mutableStateOf<Job?>(null)}
    val scope=rememberCoroutineScope()
    val cancelFocus=remember{FocusRequester()}
    DisposableEffect(identity,packageName){onDispose{job?.cancel()}}
    LaunchedEffect(identity,packageName){withFrameNanos{};cancelFocus.requestFocus()}
    LaunchedEffect(current){if(!current){job?.cancel();reviewed=null;error="La selección cambió. Volvé a elegir los archivos que querés compartir."}}
    val destination=remember(packageName){packageName?.let{pkg->runCatching{context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(pkg,0)).toString()}.getOrDefault(pkg)} ?: "selector de aplicaciones de Android"}
    fun prepare(confirm:Boolean) {
        if(busy || attempted || !latestCurrent())return
        val approved=reviewed
        if(confirm && approved==null)return
        val owner=identity;val snapshot=items
        busy=true;error=""
        job=scope.launch {
            try {
                val next=FileShareContract.prepare(context,snapshot,packageName)
                ensureActive()
                if(!latestCurrent())return@launch
                if(buffer.items.map{listOf(it.id,(it.action as? Action.Launch)?.intent?.data?.toString(),it.title,it.mime)}!=owner)return@launch
                if(confirm) {
                    if(approved?.documents!=next.documents) {
                        reviewed=null;error="Cambió la selección o sus metadatos. Revisá los archivos nuevamente."
                    } else {
                        // Consume this UI proposal before handing it to NativeLaunchSession.
                        attempted=true
                        latestRequest(next.chooser())
                    }
                } else reviewed=next
            }catch(e:CancellationException){throw e}
            catch(e:FileShareContract.Failure){reviewed=null;error=e.problem.label}
            catch(_:Exception){reviewed=null;error="No se pudo revisar la selección. Elegí los archivos nuevamente con Android."}
            finally{busy=false}
        }
    }
    val documents=reviewed?.documents ?: items.map{item->
        val uri=(item.action as? Action.Launch)?.intent?.data ?: android.net.Uri.EMPTY
        FileShareContract.Document(uri,item.title,item.mime ?: "Tipo por revisar",item.sizeBytes,
            if(uri.scheme=="content")"Origen seleccionado · ${uri.authority}" else "Archivo local seleccionado")
    }
    Column(Modifier.fillMaxWidth().padding(16.dp).testTag("file-share-review"),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Compartir archivos · ${documents.size}",style=MaterialTheme.typography.titleMedium)
        documents.forEachIndexed { index,doc -> key(doc.uri) {
            Column(Modifier.fillMaxWidth().testTag("file-share-document:$index"),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                Text(doc.name,style=MaterialTheme.typography.titleSmall)
                Text("Tipo: ${doc.mime}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text(doc.origin,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                if(doc.name.length>=300)Text("Nombre abreviado para la revisión.",style=MaterialTheme.typography.labelSmall)
            }
            HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
        } }
        Text("Destino: $destination",style=MaterialTheme.typography.bodyMedium)
        Text("Android dará acceso temporal de lectura a la aplicación que elijas. No se crea otra copia.",style=MaterialTheme.typography.bodySmall)
        Text("La entrega sólo puede comprobarse en la aplicación receptora.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())
        if(error.isNotBlank())Text(error,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.error,modifier=Modifier.testTag("file-share-error"))
        Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            if(reviewed!=null)Button(enabled=current && !busy && !attempted,onClick={prepare(true)},colors=inspectorButtonColors(),
                modifier=Modifier.heightIn(min=48.dp).testTag("file-share-confirm-button")){Text("Confirmar y abrir selector")}
            else Button(enabled=current && items.isNotEmpty() && !busy && !attempted,onClick={prepare(false)},colors=inspectorButtonColors(),
                modifier=Modifier.heightIn(min=48.dp).testTag("file-share-review-button")){Text("Revisar envío")}
            TextButton(onClick={job?.cancel();onCancel()},modifier=Modifier.heightIn(min=48.dp).focusProperties{canFocus=true}
                .focusRequester(cancelFocus).testTag("file-share-cancel")){Text("Volver sin compartir")}
        }
    }
}
