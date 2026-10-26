package dev.bautista.spotlight.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
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
    val appPackage=if(PreviewPresentation.isApplication(item))
        (item.action as? Action.Launch)?.intent?.component?.packageName
            ?: item.id.removePrefix("suggest:app:").substringBefore('/')
        else null
    var appVersion by remember(item.id){mutableStateOf<String?>(null)}
    LaunchedEffect(item.id,appPackage) {
        appVersion=if(appPackage!=null)withContext(Dispatchers.IO) {
            runCatching { context.packageManager.getPackageInfo(appPackage,0).versionName }.getOrNull()
        } else null
    }
    Column(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
        if(item.category=="Archivos"){
            Text("Metadatos",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            val rows=buildList {
                add("Nombre" to item.title)
                item.parentUri?.let{raw->
                    val label=Uri.parse(raw).lastPathSegment?.substringAfter(':')?.takeIf{it.isNotBlank()}
                    if(label!=null)add("Ubicación" to label)
                }
                item.mime?.takeIf{it.isNotBlank()}?.let{add("Tipo" to it)}
                item.sizeBytes?.let{add("Tamaño" to android.text.format.Formatter.formatShortFileSize(context,it))}
                item.modifiedAt?.let{add("Modificado" to DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(Date(it)))}
            }
            SelectionContainer { Column(Modifier.fillMaxWidth()) {
            rows.forEachIndexed { index,(label,value)->
                Row(Modifier.fillMaxWidth().testTag("file-metadata:$label")
                    .background(if(index%2==0)MaterialTheme.colorScheme.onSurface.copy(alpha=.035f) else androidx.compose.ui.graphics.Color.Transparent,RoundedCornerShape(6.dp))
                    .padding(horizontal=8.dp,vertical=8.dp),verticalAlignment=Alignment.Top,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    Text(label,Modifier.weight(1f),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value,Modifier.weight(2f),fontSize=11.sp,textAlign=TextAlign.End)
                }
            }
            } }
        }
        if(appPackage!=null && appVersion!=null)Text("Versión · $appVersion",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        if(destination?.scheme in setOf("https","http")){SelectionContainer{Text(destination.toString(),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.primary)}}
    }
}

