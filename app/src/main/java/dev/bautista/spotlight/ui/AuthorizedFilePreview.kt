package dev.bautista.spotlight.ui

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import kotlinx.coroutines.*

/** Reopen with current OS authorization; a saved URI is never treated as a saved grant. */
internal suspend fun canReadPreview(context:Context,item:Result):Boolean=withContext(Dispatchers.IO) {
    val uri=(item.action as? Action.Launch)?.intent?.data ?: return@withContext false
    try { context.contentResolver.openFileDescriptor(uri,"r")?.use{true} ?: false }
    catch(e:CancellationException){throw e}
    catch(e:Exception){false}
}

@Composable internal fun AuthorizedFilePreview(context:Context,item:Result,expanded:Boolean,page:Int,onPage:(Int)->Unit) {
    val owner=LocalLifecycleOwner.current
    var active by remember(owner){mutableStateOf(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))}
    var revision by remember(owner){mutableIntStateOf(0)}
    DisposableEffect(owner) {
        val observer=LifecycleEventObserver{_,event -> when(event){
            Lifecycle.Event.ON_PAUSE,Lifecycle.Event.ON_STOP,Lifecycle.Event.ON_DESTROY -> active=false
            Lifecycle.Event.ON_RESUME -> {revision++;active=true}
            else -> Unit
        }}
        owner.lifecycle.addObserver(observer)
        onDispose{owner.lifecycle.removeObserver(observer)}
    }
    // Remove the renderer/media child while inactive, including its callbacks and descriptors.
    if(!active)return
    key(item.id,(item.action as? Action.Launch)?.intent?.data,item.modifiedAt,revision) {
        var readable by remember{mutableStateOf<Boolean?>(null)}
        LaunchedEffect(Unit){readable=canReadPreview(context,item)}
        when(readable){
            null -> LinearProgressIndicator()
            false -> Text("El documento ya no está disponible o perdió su autorización. Volvé a seleccionarlo desde Archivos.",color=MaterialTheme.colorScheme.error)
            true -> FilePreview(context,item,expanded,page,onPage)
        }
    }
}
