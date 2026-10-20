package dev.bautista.spotlight.control
import android.content.*
import android.media.session.*
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Only an active session exposed to the authorized notification listener can be controlled. */
object MediaControl {
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
        return runCatching {
            context.getSystemService(MediaSessionManager::class.java)
                .getActiveSessions(ComponentName(context,ReplyService::class.java))
                .filter { it.packageName==packageName && (it.playbackState?.actions ?: 0L) and required != 0L }
                .singleOrNull()
        }.getOrNull()
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
