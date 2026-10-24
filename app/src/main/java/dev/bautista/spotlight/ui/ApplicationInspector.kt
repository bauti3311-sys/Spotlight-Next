package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import dev.bautista.spotlight.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bautista.spotlight.search.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Presentation only. Existing public intents/favorites/MediaSession remain authoritative. */
@Composable internal fun ApplicationInspector(
    item: Result,
    packageName: String?,
    favorite: Boolean,
    playing: Boolean,
    mediaCommands: List<String>,
    onOpen: () -> Unit,
    onFavorite: () -> Unit,
    onInfo: () -> Unit,
    onActions: () -> Unit,
    onMedia: (String) -> Unit,
    mediaContent: (@Composable () -> Unit)? = null,
    capabilitiesContent: (@Composable () -> Unit)? = null,
) {
    val colors=MaterialTheme.colorScheme
    val context=LocalContext.current
    var version by remember(packageName) { mutableStateOf<String?>(null) }
    var spotifySearch by remember(packageName) { mutableStateOf(false) }
    LaunchedEffect(packageName) {
        version=packageName?.let { pkg -> withContext(Dispatchers.IO) {
            runCatching { context.packageManager.getPackageInfo(pkg,0).versionName }.getOrNull()
        } }
    }
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min=52.dp),verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(14.dp)) {
            ResultIcon(item,Modifier.size(46.dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {
                Text(item.title,Modifier.testTag("inspector-title"),fontSize=17.sp,
                    fontWeight=FontWeight.SemiBold,maxLines=1,overflow=TextOverflow.Ellipsis)
                Text(if(playing)"En reproducción" else "Aplicación instalada",fontSize=12.sp,
                    color=colors.onSurfaceVariant)
            }
            if(packageName=="com.spotify.music")IconButton(onClick={spotifySearch=!spotifySearch}) {
                IosCommandIcon(CommandIcons.Search,"Buscar dentro de Spotify",Modifier.size(20.dp))
            }
        }
        mediaContent?.invoke()
        if(packageName=="com.spotify.music" && spotifySearch) {
            (context.applicationContext as? dev.bautista.spotlight.SpotlightApp)?.let { SpotifySearchPanel(it) }
            TextButton(onClick={spotifySearch=false}) { Text("Cerrar búsqueda de Spotify") }
        }
        Button(onClick=onOpen,shape=RoundedCornerShape(10.dp),
            colors=ButtonDefaults.buttonColors(containerColor=colors.primary,contentColor=colors.onPrimary),
            contentPadding=PaddingValues(horizontal=12.dp,vertical=0.dp),
            modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("inspector-primary-action")) {
            Icon(painterResource(R.drawable.inspector_ios_open),null,Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("Abrir aplicación",Modifier.weight(1f),fontSize=14.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
            InspectorKeyHint("Enter")
        }
        Column(Modifier.fillMaxWidth()) {
            InspectorActionRow("Ver información de la app",R.drawable.inspector_ios_info,onClick=onInfo)
            InspectorActionRow(if(favorite)"Quitar favorita" else "Añadir favorita",
                R.drawable.inspector_ios_star,onClick=onFavorite)
            mediaCommands.forEach { command ->
                InspectorActionRow(when(command){"pause"->"Pausar reproducción";"play"->"Reproducir";"next"->"Siguiente canción";else->"Canción anterior"},
                    when(command){"pause"->R.drawable.inspector_ios_pause;"play"->R.drawable.inspector_ios_play;"next"->R.drawable.inspector_ios_next;else->R.drawable.inspector_ios_previous},
                    onClick={onMedia(command)})
            }
            InspectorActionRow("Ver acciones contextuales",R.drawable.inspector_ios_actions,"Ctrl+K",onActions)
        }
        capabilitiesContent?.invoke()
        if(packageName!=null) {
            HorizontalDivider(color=colors.onSurface.copy(alpha=.10f))
            Column(Modifier.fillMaxWidth().testTag("inspector-metadata"),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                version?.let { InspectorProperty("Versión",it) }
                InspectorProperty("Paquete",packageName)
            }
        }
    }
}

@Composable private fun InspectorActionRow(label:String,icon:Int,hint:String?=null,onClick:()->Unit) {
    val colors=MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().heightIn(min=48.dp).clickable(onClick=onClick).padding(horizontal=2.dp),
        verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
        Icon(painterResource(icon),null,Modifier.size(18.dp),tint=colors.onSurfaceVariant)
        Text(label,Modifier.weight(1f),fontSize=12.sp,maxLines=2,overflow=TextOverflow.Ellipsis)
        if(hint!=null)InspectorKeyHint(hint)
    }
}

@Composable private fun InspectorKeyHint(label:String) {
    val colors=MaterialTheme.colorScheme
    Text(label,Modifier.background(colors.onSurface.copy(alpha=.07f),RoundedCornerShape(5.dp))
        .border(.5.dp,colors.onSurface.copy(alpha=.10f),RoundedCornerShape(5.dp)).padding(horizontal=6.dp,vertical=3.dp),
        fontSize=10.sp,color=LocalContentColor.current.copy(alpha=.8f))
}

@Composable private fun InspectorProperty(label:String,value:String) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.Top,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
        Text(label,fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        SelectionContainer(Modifier.weight(2f)) {
            Text(value,Modifier.fillMaxWidth(),fontSize=11.sp,maxLines=2,overflow=TextOverflow.Ellipsis,
                textAlign=androidx.compose.ui.text.style.TextAlign.End,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
