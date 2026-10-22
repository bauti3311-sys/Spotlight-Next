package dev.bautista.spotlight.control
import android.content.*
import android.media.session.*
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.os.SystemClock
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Immutable observation; progress is extrapolated only from a timestamp supplied by Android. */
data class MediaSnapshot(
    val token:MediaSession.Token, val packageName:String, val title:String, val artist:String,
    val album:String, val artwork:Bitmap?, val duration:Long?, val state:PlaybackState?,
    val observedAt:Long=SystemClock.elapsedRealtime()
) {
    val playing get()=state?.state==PlaybackState.STATE_PLAYING
    fun supports(action:Long)=(state?.actions ?: 0L) and action != 0L
    fun positionAt(now:Long=SystemClock.elapsedRealtime()):Long? {
        val s=state ?: return null
        if(s.position<0)return null
        val elapsed=if(playing && s.lastPositionUpdateTime>0 && s.playbackSpeed.isFinite())
            ((now-s.lastPositionUpdateTime).coerceAtLeast(0)*s.playbackSpeed).toLong() else 0L
        val value=(s.position+elapsed).coerceAtLeast(0)
        return duration?.let{value.coerceAtMost(it)} ?: value
    }
}

/** Only an active session exposed to the authorized notification listener can be controlled. */
object MediaControl {
    private fun packageController(context:Context,packageName:String):MediaController? = runCatching {
        context.applicationContext.getSystemService(MediaSessionManager::class.java)
            .getActiveSessions(ComponentName(context,ReplyService::class.java))
            .filter{it.packageName==packageName}.singleOrNull()
    }.getOrNull()

    private fun snapshot(controller:MediaController):MediaSnapshot {
        val m=controller.metadata
        fun text(key:String)=m?.getText(key)?.toString().orEmpty()
        val art=m?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
            ?: m?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) ?: m?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        return MediaSnapshot(controller.sessionToken,controller.packageName,
            text(MediaMetadata.METADATA_KEY_DISPLAY_TITLE).ifBlank{text(MediaMetadata.METADATA_KEY_TITLE)},
            text(MediaMetadata.METADATA_KEY_ARTIST),text(MediaMetadata.METADATA_KEY_ALBUM),art,
            m?.getLong(MediaMetadata.METADATA_KEY_DURATION)?.takeIf{it>0},controller.playbackState)
    }
    fun snapshotForPackage(context:Context,packageName:String):MediaSnapshot? =
        packageController(context,packageName)?.let{runCatching{snapshot(it)}.getOrNull()}

    /** Collected only while the contextual inspector exists. Callbacks plus a visible-only tick
     * revalidate authorization/session identity; no background service, URI fetch or artwork cache. */
    fun inspect(context:Context,packageName:String):Flow<MediaSnapshot?> = callbackFlow {
        val manager=context.applicationContext.getSystemService(MediaSessionManager::class.java)
        val handler=Handler(Looper.getMainLooper())
        var bound:MediaController?=null
        lateinit var refresh:()->Unit
        val callback=object:MediaController.Callback() {
            override fun onMetadataChanged(metadata:MediaMetadata?){refresh()}
            override fun onPlaybackStateChanged(state:PlaybackState?){refresh()}
            override fun onSessionDestroyed(){refresh()}
        }
        refresh={
            val live=packageController(context,packageName)
            if(live?.sessionToken!=bound?.sessionToken){
                bound?.let{runCatching{it.unregisterCallback(callback)}}
                bound=live
                live?.let{runCatching{it.registerCallback(callback,handler)}}
            }
            trySend(live?.let{runCatching{snapshot(it)}.getOrNull()})
        }
        val listener=MediaSessionManager.OnActiveSessionsChangedListener{refresh()}
        runCatching{manager.addOnActiveSessionsChangedListener(listener,ComponentName(context,ReplyService::class.java),handler)}
        refresh()
        val tick=launch{while(true){delay(1000);refresh()}}
        awaitClose {
            tick.cancel()
            runCatching{manager.removeOnActiveSessionsChangedListener(listener)}
            bound?.let{runCatching{it.unregisterCallback(callback)}}
            bound=null
        }
    }
    fun seekForPackage(context:Context,packageName:String,position:Long,expectedToken:MediaSession.Token):String {
        val live=packageController(context,packageName) ?: error("La sesión multimedia ya no está disponible.")
        check(live.sessionToken==expectedToken){"La sesión cambió; revisá la pista actual."}
        val current=snapshot(live)
        check(current.supports(PlaybackState.ACTION_SEEK_TO)){"Este reproductor no permite cambiar la posición."}
        val duration=current.duration ?: error("La duración no está disponible.")
        live.transportControls.seekTo(position.coerceIn(0,duration))
        return "Posición solicitada al reproductor; pendiente de confirmación."
    }
    /** Observe only authorized sessions, while the window is composed. No installed-app guesses. */
    fun playing(context:Context):Flow<Set<String>> = callbackFlow {
        val manager=context.applicationContext.getSystemService(MediaSessionManager::class.java)
        val handler=Handler(Looper.getMainLooper())
        val callbacks=mutableMapOf<MediaController,MediaController.Callback>()
        fun publish() {trySend(callbacks.keys.filter{isPlaying(it.playbackState)}.map{it.packageName}.toSet())}
        fun bind(sessions:List<MediaController>) {
            callbacks.forEach{(controller,callback)->controller.unregisterCallback(callback)}
            callbacks.clear()
            sessions.forEach { controller ->
                val callback=object:MediaController.Callback() {
                    override fun onPlaybackStateChanged(state:PlaybackState?) {publish()}
                    override fun onSessionDestroyed() {callbacks.remove(controller)?.let{controller.unregisterCallback(it)};publish()}
                }
                callbacks[controller]=callback
                controller.registerCallback(callback,handler)
            }
            publish()
        }
        val listener=MediaSessionManager.OnActiveSessionsChangedListener{sessions->bind(sessions.orEmpty())}
        try {
            manager.addOnActiveSessionsChangedListener(listener,ComponentName(context,ReplyService::class.java),handler)
            bind(manager.getActiveSessions(ComponentName(context,ReplyService::class.java)))
        } catch(_:SecurityException) {bind(emptyList())}
        awaitClose {
            manager.removeOnActiveSessionsChangedListener(listener)
            callbacks.forEach{(controller,callback)->controller.unregisterCallback(callback)}
            callbacks.clear()
        }
    }
    internal fun isPlaying(state:PlaybackState?)=state?.state==PlaybackState.STATE_PLAYING

    private fun controller(context:Context,command:String):MediaController? {
        val required=when(command){"pause"->PlaybackState.ACTION_PAUSE;"play"->PlaybackState.ACTION_PLAY;"next"->PlaybackState.ACTION_SKIP_TO_NEXT;"previous"->PlaybackState.ACTION_SKIP_TO_PREVIOUS;else->return null}
        return runCatching {
            // Re-read authorized sessions for every action; never retain a previously available target.
            val compatible=context.getSystemService(MediaSessionManager::class.java)
                .getActiveSessions(ComponentName(context,ReplyService::class.java))
                .filter{(it.playbackState?.actions ?: 0L) and required !=0L}
            val playing=compatible.filter{isPlaying(it.playbackState)}
            if(playing.isNotEmpty())playing.singleOrNull() else compatible.singleOrNull()
        }.getOrNull()
    }
    /**
     * Context inspector must never dispatch a Spotify command to a different
     * currently active player. A single authorized session of the exact app is
     * mandatory; ambiguous or unsupported sessions fail closed.
     */
    private fun controllerForPackage(context:Context,packageName:String,command:String):MediaController? {
        val required=when(command){
            "pause"->PlaybackState.ACTION_PAUSE
            "play"->PlaybackState.ACTION_PLAY
            "next"->PlaybackState.ACTION_SKIP_TO_NEXT
            "previous"->PlaybackState.ACTION_SKIP_TO_PREVIOUS
            else->return null
        }
        return packageController(context,packageName)?.takeIf {
            (it.playbackState?.actions ?: 0L) and required != 0L
        }
    }
    fun availableForPackage(context:Context,packageName:String,command:String)=
        controllerForPackage(context,packageName,command)!=null

    fun executeForPackage(context:Context,packageName:String,command:String):String {
        val session=controllerForPackage(context,packageName,command)
            ?: error("No hay una sesión multimedia única y autorizada de $packageName para esta acción.")
        when(command){
            "pause"->session.transportControls.pause()
            "play"->session.transportControls.play()
            "next"->session.transportControls.skipToNext()
            "previous"->session.transportControls.skipToPrevious()
            else->error("Control no compatible.")
        }
        return "Solicitud enviada a ${session.packageName}; Android no garantiza su ejecución final."
    }
    fun available(context:Context,command:String)=controller(context,command)!=null
    fun execute(context:Context,command:String):String {
        val session=controller(context,command) ?: error("No hay un único reproductor compatible y autorizado. Revisá el acceso a notificaciones y dejá activo el reproductor que querés controlar.")
        when(command){"pause"->session.transportControls.pause();"play"->session.transportControls.play();"next"->session.transportControls.skipToNext();"previous"->session.transportControls.skipToPrevious();else->error("Control no compatible.")}
        return "Solicitud enviada a ${session.packageName}; el reproductor controla la reproducción."
    }
}
