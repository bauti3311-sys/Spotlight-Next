package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

data class SharedImportProgress(val document:Int=0,val total:Int=0,val copiedBytes:Long?=null)
/** Temporary foreground feedback only; no URI, filename, content or background service. */
@Composable internal fun SharedImportPanel(progress:SharedImportProgress,onCancel:()->Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp),contentAlignment=Alignment.Center) {
        Surface(Modifier.widthIn(max=440.dp).fillMaxWidth().testTag("shared-import"),shape=MaterialTheme.shapes.large) {
            Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text("Preparando contenido compartido",style=MaterialTheme.typography.titleMedium)
                Text(if(progress.total>0)"Documento ${progress.document.coerceAtLeast(1)} de ${progress.total}" else "Comprobando acceso autorizado…")
                progress.copiedBytes?.let{Text("${it/1024} KB copiados localmente",style=MaterialTheme.typography.bodySmall)}
                LinearProgressIndicator(Modifier.fillMaxWidth())
                TextButton(onClick=onCancel,modifier=Modifier.testTag("shared-import-cancel")){Text("Cancelar importación")}
            }
        }
    }
}
