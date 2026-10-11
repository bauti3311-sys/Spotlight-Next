package dev.bautista.spotlight.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

@Composable fun QuickLookMetadata(item:Result){
    val context=LocalContext.current
    val destination=(item.action as? Action.Launch)?.intent?.data
    var appVersion by remember(item.id){mutableStateOf<String?>(null)}
    LaunchedEffect(item.id){if(item.category=="Aplicaciones")appVersion=withContext(Dispatchers.IO){runCatching{context.packageManager.getPackageInfo(item.id.substringBefore('/'),0).versionName}.getOrNull()}}
    Column(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
        if(item.category=="Archivos"){
            Text(item.mime.orEmpty(),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            item.sizeBytes?.let{Text(android.text.format.Formatter.formatShortFileSize(context,it),style=MaterialTheme.typography.bodySmall)}
            item.modifiedAt?.let{Text("Modificado · "+DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(Date(it)),style=MaterialTheme.typography.bodySmall)}
            item.parentUri?.let{raw->val label=Uri.parse(raw).lastPathSegment?.substringAfter(':')?.takeIf{it.isNotBlank()};if(label!=null)SelectionContainer{Text("Ubicación · $label",style=MaterialTheme.typography.bodySmall)}}
        }
        if(item.category=="Aplicaciones" && appVersion!=null)Text("Versión · $appVersion",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        if(destination?.scheme in setOf("https","http")){SelectionContainer{Text(destination.toString(),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.primary)}}
    }
}
