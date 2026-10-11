package dev.bautista.spotlight.ui

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun AudioPreview(item: Result) {
    val context = LocalContext.current
    val uri = (item.action as? Action.Launch)?.intent?.data ?: return
    var player by remember(item.id) { mutableStateOf<MediaPlayer?>(null) }
    var ready by remember(item.id) { mutableStateOf(false) }
    var failed by remember(item.id) { mutableStateOf(false) }
    var duration by remember(item.id) { mutableIntStateOf(0) }
    var position by remember(item.id) { mutableFloatStateOf(0f) }
    DisposableEffect(uri) {
        val media = MediaPlayer()
        player = media
        runCatching {
                media.setDataSource(context, uri)
                media.setOnPreparedListener {
                    duration = it.duration
                    ready = true
                }
                media.setOnErrorListener { _, _, _ ->
                    failed = true
                    true
                }
                media.prepareAsync()
            }
            .onFailure { failed = true }
        onDispose {
            ready = false
            media.release()
            player = null
        }
    }
    LaunchedEffect(ready) {
        while (isActive && ready) {
            delay(500)
            position = runCatching { player?.currentPosition?.toFloat() ?: 0f }.getOrDefault(0f)
        }
    }
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        if (failed) Text("No se pudo previsualizar el audio; abrilo en una app compatible.")
        else if (!ready) LinearProgressIndicator(Modifier.fillMaxWidth())
        else {
            Row {
                TextButton(onClick = { player?.start() }) { Text("Reproducir") }
                TextButton(onClick = { player?.pause() }) { Text("Pausar") }
                Text(
                    "${position.toInt()/60000}:${((position.toInt()/1000)%60).toString().padStart(2, '0')}",
                    Modifier.padding(top = 14.dp),
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
}
