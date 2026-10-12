package dev.bautista.spotlight.control
import android.content.*
import android.media.session.*

/** Only an active session exposed to the authorized notification listener can be controlled. */
object MediaControl {
    private fun controller(context:Context,command:String):MediaController? {
        val required=when(command){"pause"->PlaybackState.ACTION_PAUSE;"play"->PlaybackState.ACTION_PLAY;"next"->PlaybackState.ACTION_SKIP_TO_NEXT;"previous"->PlaybackState.ACTION_SKIP_TO_PREVIOUS;else->return null}
        return runCatching{context.getSystemService(MediaSessionManager::class.java).getActiveSessions(ComponentName(context,ReplyService::class.java)).firstOrNull{(it.playbackState?.actions ?: 0L) and required !=0L}}.getOrNull()
    }
    fun available(context:Context,command:String)=controller(context,command)!=null
    fun execute(context:Context,command:String):String {
        val session=controller(context,command) ?: error("No hay una sesión compatible autorizada. Activá acceso a notificaciones y abrí una app de música.")
        when(command){"pause"->session.transportControls.pause();"play"->session.transportControls.play();"next"->session.transportControls.skipToNext();"previous"->session.transportControls.skipToPrevious();else->error("Control no compatible.")}
        return "Solicitud enviada a ${session.packageName}; el reproductor controla la reproducción."
    }
}
