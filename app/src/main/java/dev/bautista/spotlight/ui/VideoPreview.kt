package dev.bautista.spotlight.ui

import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*

@Composable
fun VideoPreview(item: Result) {
    val uri = (item.action as? Action.Launch)?.intent?.data ?: return
    var player by remember(item.id) { mutableStateOf<VideoView?>(null) }
    var duration by remember(item.id) { mutableIntStateOf(0) }
    var position by remember(item.id) { mutableFloatStateOf((item.positionMs ?: 0L).toFloat()) }
    var error by remember(item.id) { mutableStateOf(false) }
    LaunchedEffect(player) {
        while (isActive) {
            delay(700)
            player?.let { if (it.isPlaying) position = it.currentPosition.toFloat() }
        }
    }
    Column {
        AndroidView(
            factory = { context ->
                VideoView(context).also { view ->
                    player = view
                    view.setOnPreparedListener {
                        duration = it.duration
                        view.seekTo((item.positionMs ?: 0L).toInt())
                        view.pause()
                    }
                    view.setOnErrorListener { _, _, _ ->
                        error = true
                        true
                    }
                    view.setVideoURI(uri)
                }
            },
            onRelease = {
                it.stopPlayback()
                player = null
            },
            modifier = Modifier.fillMaxWidth().height(180.dp),
        )
        if (error)
            Text(
                "No se pudo previsualizar este video; podés abrirlo en otra app.",
                fontSize = 12.sp,
            )
        else
            Row {
                TextButton(onClick = { player?.start() }) { Text("Reproducir") }
                TextButton(onClick = { player?.pause() }) { Text("Pausar") }
                Text(
                    "${position.toInt()/60000}:${((position.toInt()/1000)%60).toString().padStart(2,'0')}",
                    Modifier.padding(top = 14.dp),
                    fontSize = 12.sp,
                )
            }
        if (duration > 0)
            Slider(
                position.coerceIn(0f, duration.toFloat()),
                {
                    position = it
                    player?.seekTo(it.toInt())
                },
                valueRange = 0f..duration.toFloat(),
            )
    }
}
