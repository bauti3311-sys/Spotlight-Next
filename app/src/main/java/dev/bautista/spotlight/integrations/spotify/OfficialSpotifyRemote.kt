package dev.bautista.spotlight.integrations.spotify

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.AuthenticationFailedException
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.android.appremote.api.error.OfflineModeException
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.android.appremote.api.error.UnsupportedFeatureVersionException
import com.spotify.android.appremote.api.error.UserNotAuthorizedException
import com.spotify.protocol.client.CallResult
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.*

/** Official Spotify IPC SDK; no playback HTTP fallback, launch intent, polling or debug logging. */
internal class OfficialSpotifyRemote(context:Context):SpotifyRemoteConnector {
 private val app=context.applicationContext
 override suspend fun open(clientId:String,authorize:Boolean):SpotifyRemoteLink = withContext(Dispatchers.Main.immediate) {
  check(SpotifyAppRemote.isSpotifyInstalled(app)){"Instalá Spotify e iniciá sesión para usar la reproducción local."}
  suspendCancellableCoroutine { pending ->
   val params=ConnectionParams.Builder(clientId)
    .setRedirectUri("http://127.0.0.1/spotify/callback").showAuthView(authorize).build()
   try { SpotifyAppRemote.connect(app,params,object:Connector.ConnectionListener {
    override fun onConnected(remote:SpotifyAppRemote) {
     val link=OfficialLink(remote)
     if(pending.isActive)pending.resume(link,onCancellation={link.close()}) else link.close()
    }
    override fun onFailure(error:Throwable) {
     if(pending.isActive)pending.resumeWithException(classifyConnectionError(error))
    }
   }) } catch (_:Exception) {
    if(pending.isActive)pending.resumeWithException(IOException("No se pudo iniciar la conexión local de Spotify."))
   }
  }
 }
 private class OfficialLink(private val remote:SpotifyAppRemote):SpotifyRemoteLink {
  private val closed=AtomicBoolean(false)
  override val isConnected:Boolean get()=!closed.get() && remote.isConnected
  override suspend fun canPlayOnDemand()=spotifySdkCall { remote.userApi.capabilities.awaitResult().canPlayOnDemand }
  override suspend fun play(uri:String) { spotifySdkCall { remote.playerApi.play(uri).awaitResult() } }
  override suspend fun observe():SpotifyPlaybackObservation = spotifySdkCall {
   val state=remote.playerApi.playerState.awaitResult()
   val track=state.track
   SpotifyPlaybackObservation(
    playing=!state.isPaused,
    trackUri=track?.uri,
    contextUri=null,
    observedAt=System.currentTimeMillis(),
    trackName=track?.name,
    artistName=track?.artist?.name,
   )
  }
  override fun close() {
   if(!closed.compareAndSet(false,true))return
   if(Looper.myLooper()==Looper.getMainLooper())SpotifyAppRemote.disconnect(remote)
   else Handler(Looper.getMainLooper()).post{SpotifyAppRemote.disconnect(remote)}
  }
 }
}
private fun classifyConnectionError(error:Throwable):IOException = when(error) {
 is CouldNotFindSpotifyApp -> IOException("Instalá Spotify para usar la reproducción local.")
 is NotLoggedInException -> IOException("Iniciá sesión en la app de Spotify antes de reproducir.")
 is UserNotAuthorizedException, is AuthenticationFailedException -> IOException(
  "Spotify rechazó la autorización local. Revisá el Client ID, el paquete y el SHA-1 en Spotify Dashboard."
 )
 is OfflineModeException -> IOException("Spotify está en modo sin conexión.")
 is UnsupportedFeatureVersionException -> IOException("Actualizá la app de Spotify para usar el control local.")
 is SpotifyDisconnectedException -> IOException("La sesión local con Spotify se desconectó.")
 else -> IOException(
  "No se pudo conectar con el cliente Spotify. Revisá la autorización local, el paquete y la huella en Spotify Dashboard."
 )
}
private suspend fun <T> CallResult<T>.awaitResult():T=suspendCancellableCoroutine { pending ->
 pending.invokeOnCancellation{cancel()}
 setResultCallback{value->if(pending.isActive)pending.resume(value)}
 setErrorCallback{if(pending.isActive)pending.resumeWithException(IOException(
  "El cliente Spotify no confirmó la operación. No se reintentó automáticamente."
 ))}
}

private suspend fun <T> spotifySdkCall(block:suspend()->T):T=try { block() }
catch(e:CancellationException){throw e}
catch(e:IOException){throw e}
catch(_:Exception){throw IOException("Spotify no confirmó la operación local. No se reintentó automáticamente.")}
