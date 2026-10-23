package dev.bautista.spotlight.ui

import android.media.session.PlaybackState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.control.MediaSnapshot

private fun mediaTime(ms:Long):String {
    val seconds=ms.coerceAtLeast(0)/1000
    return "${seconds/60}:${(seconds%60).toString().padStart(2,'0')}"
}

/** Displays only metadata supplied by the authorized MediaSession. Audio stays in its app. */
@Composable fun MediaInspector(snapshot:MediaSnapshot,onCommand:(String)->Unit,onSeek:(Long)->Unit) {
    val s=snapshot
    val colors=MaterialTheme.colorScheme
    var dragging by remember(s.token,s.title,s.duration){mutableStateOf<Float?>(null)}
    Column(Modifier.fillMaxWidth().testTag("media-inspector").padding(top=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            s.artwork?.let { artwork ->
                Image(artwork.asImageBitmap(),contentDescription="Portada de la sesión",contentScale=ContentScale.Crop,
                    modifier=Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)))
            }
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                Text(s.title.ifBlank{"Título no disponible"},style=MaterialTheme.typography.titleMedium,maxLines=2,overflow=TextOverflow.Ellipsis)
                if(s.artist.isNotBlank())Text(s.artist,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=2,overflow=TextOverflow.Ellipsis)
                if(s.album.isNotBlank())Text(s.album,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
            }
        }
        Text(when(s.state?.state){PlaybackState.STATE_PLAYING->"En reproducción";PlaybackState.STATE_PAUSED->"En pausa";PlaybackState.STATE_BUFFERING->"Cargando audio…";PlaybackState.STATE_STOPPED->"Detenido";PlaybackState.STATE_ERROR->"El reproductor informó un error";else->"Estado no disponible"},style=MaterialTheme.typography.labelSmall)
        val duration=s.duration
        val position=s.positionAt(s.observedAt)
        if(duration!=null && position!=null) {
            val fraction=position.toFloat()/duration.toFloat()
            if(s.supports(PlaybackState.ACTION_SEEK_TO))Slider(dragging ?: fraction,{dragging=it},
                colors=SliderDefaults.colors(thumbColor=colors.primary,activeTrackColor=colors.primary,
                    inactiveTrackColor=colors.onSurface.copy(alpha=.12f)),
                onValueChangeFinished={dragging?.let{onSeek((it*duration).toLong())};dragging=null},
                modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("media-seek"))
            else LinearProgressIndicator(progress={fraction.coerceIn(0f,1f)},color=colors.primary,
                trackColor=colors.onSurface.copy(alpha=.12f),modifier=Modifier.fillMaxWidth().testTag("media-progress"))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                Text(mediaTime(position),style=MaterialTheme.typography.labelSmall)
                Text(mediaTime(duration),style=MaterialTheme.typography.labelSmall)
            }
        } else Text("Progreso no disponible",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            if(s.supports(PlaybackState.ACTION_SKIP_TO_PREVIOUS))IconButton(onClick={onCommand("previous")},modifier=Modifier.size(48.dp)) {Icon(CommandIcons.SkipPrevious,"Pista anterior")}
            val command=if(s.playing)"pause" else "play"
            if(s.supports(if(s.playing)PlaybackState.ACTION_PAUSE else PlaybackState.ACTION_PLAY))
                FilledTonalIconButton(onClick={onCommand(command)},colors=IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor=colors.primary,contentColor=colors.onPrimary),
                    modifier=Modifier.size(48.dp)){Icon(if(s.playing)CommandIcons.Pause else CommandIcons.Play,if(s.playing)"Pausar reproducción" else "Reanudar reproducción")}
            if(s.supports(PlaybackState.ACTION_SKIP_TO_NEXT))IconButton(onClick={onCommand("next")},modifier=Modifier.size(48.dp)){Icon(CommandIcons.SkipNext,"Pista siguiente")}
        }
    }
}
