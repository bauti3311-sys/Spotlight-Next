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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
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
    embedded: Boolean = false,
) {
    val colors=MaterialTheme.colorScheme
    val appearance=LocalAppearance.current
    val d=GlassTokens.densityScale(appearance)
    val premium=LocalPremiumReview.current
    val context=LocalContext.current
    var version by remember(packageName) { mutableStateOf<String?>(null) }
    var spotifySearch by remember(packageName) { mutableStateOf(false) }
    LaunchedEffect(packageName) {
        version=packageName?.let { pkg -> withContext(Dispatchers.IO) {
            runCatching { context.packageManager.getPackageInfo(pkg,0).versionName }.getOrNull()
        } }
    }
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(GlassTokens.inspectorGap(appearance))) {
        if(LocalFocusUi.current)Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            ResultIcon(item,Modifier.size(40.dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {
                Text(item.title,Modifier.testTag("inspector-title").semantics{heading()},style=MaterialTheme.typography.titleLarge,maxLines=2,overflow=TextOverflow.Ellipsis)
                Text(if(playing)"En reproducción" else "Aplicación instalada",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
            }
        }
        else if(LocalLiquidScene.current!=null && packageName=="com.vivaldi.browser")LiquidApplicationIdentity(item)
        else if(premium)PremiumInspectorHeader(item,
            when(packageName){"com.spotify.music"->"Música";"com.whatsapp","com.whatsapp.w4b"->"Mensajería";"com.vivaldi.browser","com.android.chrome"->"Navegador";else->"Aplicación"},
            if(playing)"En reproducción" else "Aplicación instalada",compact=LocalInspectorReviewBudget.current<420f || packageName=="com.spotify.music" && mediaContent!=null,
            onSearch=if(packageName=="com.spotify.music")({spotifySearch=!spotifySearch}) else null)
        else Row(Modifier.fillMaxWidth().heightIn(min=(46f*d).coerceAtLeast(40f).dp),verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy((12f*d).dp)) {
            ResultIcon(item,Modifier.size((42f*d).coerceIn(36f,48f).dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)) {
                Text(item.title,Modifier.testTag("inspector-title").semantics{heading()},fontSize=GlassTokens.scaledSp(17.sp,appearance),
                    lineHeight=GlassTokens.scaledSp(21.sp,appearance),fontWeight=FontWeight.SemiBold,maxLines=2,overflow=TextOverflow.Ellipsis)
                Text(if(playing)"En reproducción" else "Aplicación instalada",fontSize=GlassTokens.scaledSp(12.sp,appearance),
                    color=colors.onSurfaceVariant)
            }
            if(packageName=="com.spotify.music")IconButton(onClick={spotifySearch=!spotifySearch}) {
                IosCommandIcon(CommandIcons.Search,"Buscar dentro de Spotify",Modifier.size(20.dp))
            }
        }
        mediaContent?.invoke()
        if(premium && packageName in setOf("com.whatsapp","com.whatsapp.w4b"))
            Text("La conversación y el envío se confirman en WhatsApp.",style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant)
        if(packageName=="com.spotify.music" && spotifySearch) {
            (context.applicationContext as? dev.bautista.spotlight.SpotlightApp)?.let { SpotifySearchPanel(it) }
            TextButton(onClick={spotifySearch=false}) { Text("Cerrar búsqueda de Spotify") }
        }
        if(!embedded)Button(onClick=onOpen,shape=RoundedCornerShape(10.dp),
            colors=ButtonDefaults.buttonColors(containerColor=colors.onSurface.copy(alpha=.07f),contentColor=colors.onSurface),
            contentPadding=PaddingValues(horizontal=12.dp,vertical=0.dp),
            modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("inspector-primary-action")) {
            Icon(painterResource(R.drawable.inspector_ios_open),null,Modifier.size(18.dp),tint=colors.primary)
            Spacer(Modifier.width(10.dp))
            Text("Abrir aplicación",Modifier.weight(1f),fontSize=GlassTokens.scaledSp(14.sp,appearance),maxLines=1,overflow=TextOverflow.Ellipsis)
            InspectorKeyHint("Enter")
        }
        if(premium && embedded && LocalLiquidScene.current==null)Column(Modifier.fillMaxWidth().testTag("inspector-utility-actions"),verticalArrangement=Arrangement.spacedBy(2.dp)) {
            PremiumSectionLabel("Accesos")
            InspectorActionRow("Ver información de la app",R.drawable.inspector_ios_info,onClick=onInfo)
            InspectorActionRow(if(favorite)"Quitar favorita" else "Añadir favorita",R.drawable.inspector_ios_star,onClick=onFavorite)
        }
        else if(embedded)Row(Modifier.fillMaxWidth().testTag("inspector-utility-actions"),horizontalArrangement=Arrangement.spacedBy((8f*d).dp)) {
            InspectorActionRow("Ver información de la app",R.drawable.inspector_ios_info,onClick=onInfo,modifier=Modifier.weight(1f))
            InspectorActionRow(if(favorite)"Quitar favorita" else "Añadir favorita",
                R.drawable.inspector_ios_star,onClick=onFavorite,modifier=Modifier.weight(1f))
        }
        Column(Modifier.fillMaxWidth()) {
            if(!embedded) {
            InspectorActionRow("Ver información de la app",R.drawable.inspector_ios_info,onClick=onInfo)
            InspectorActionRow(if(favorite)"Quitar favorita" else "Añadir favorita",
                R.drawable.inspector_ios_star,onClick=onFavorite)
            }
            mediaCommands.forEach { command ->
                InspectorActionRow(when(command){"pause"->"Pausar reproducción";"play"->"Reproducir";"next"->"Siguiente canción";else->"Canción anterior"},
                    when(command){"pause"->R.drawable.inspector_ios_pause;"play"->R.drawable.inspector_ios_play;"next"->R.drawable.inspector_ios_next;else->R.drawable.inspector_ios_previous},
                    onClick={onMedia(command)})
            }
            if(!embedded)InspectorActionRow("Ver acciones contextuales",R.drawable.inspector_ios_actions,"Ctrl+K",onActions)
        }
        capabilitiesContent?.invoke()
        if(packageName!=null) {
            HorizontalDivider(color=colors.onSurface.copy(alpha=.10f))
            Column(Modifier.fillMaxWidth().testTag("inspector-metadata"),verticalArrangement=Arrangement.spacedBy((6f*d).dp)) {
                version?.let { InspectorProperty("Versión",it) }
                InspectorProperty("Paquete",packageName)
            }
        }
    }
}

@Composable private fun InspectorActionRow(label:String,icon:Int,hint:String?=null,onClick:()->Unit,modifier:Modifier=Modifier) {
    val colors=MaterialTheme.colorScheme
    val appearance=LocalAppearance.current
    val d=GlassTokens.densityScale(appearance)
    Row(modifier.fillMaxWidth().heightIn(min=48.dp).clickable(role=Role.Button,onClick=onClick).padding(horizontal=2.dp),
        verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy((8f*d).dp)) {
        Icon(painterResource(icon),null,Modifier.size((18f*d).coerceIn(16f,20f).dp),tint=colors.onSurfaceVariant)
        Text(label,Modifier.weight(1f),fontSize=GlassTokens.scaledSp(12.sp,appearance),maxLines=2,overflow=TextOverflow.Ellipsis)
        if(hint!=null)InspectorKeyHint(hint)
    }
}

@Composable private fun InspectorKeyHint(label:String) {
    val colors=MaterialTheme.colorScheme
    val appearance=LocalAppearance.current
    Text(label,Modifier.background(colors.onSurface.copy(alpha=.07f),RoundedCornerShape(5.dp))
        .border(.5.dp,colors.onSurface.copy(alpha=.10f),RoundedCornerShape(5.dp)).padding(horizontal=6.dp,vertical=2.dp),
        fontSize=GlassTokens.scaledSp(10.sp,appearance),color=LocalContentColor.current.copy(alpha=.8f))
}

@Composable private fun InspectorProperty(label:String,value:String) {
    val appearance=LocalAppearance.current
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.Top,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(label,fontSize=GlassTokens.scaledSp(if(LocalFocusUi.current)12.sp else 11.sp,appearance),color=MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        SelectionContainer(Modifier.weight(2f)) {
            Text(value,Modifier.fillMaxWidth(),fontSize=GlassTokens.scaledSp(if(LocalFocusUi.current)12.sp else 11.sp,appearance),maxLines=2,overflow=TextOverflow.Ellipsis,
                textAlign=androidx.compose.ui.text.style.TextAlign.End,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

