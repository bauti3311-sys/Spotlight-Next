package dev.bautista.spotlight.integrations.spotify

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.CallResult
import java.io.IOException
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
   SpotifyAppRemote.connect(app,params,object:Connector.ConnectionListener {
    override fun onConnected(remote:SpotifyAppRemote) {
     val link=OfficialLink(remote)
     if(pending.isActive)pending.resume(link,onCancellation={link.close()}) else link.close()
    }
    override fun onFailure(error:Throwable) {
     if(pending.isActive)pending.resumeWithException(IOException(
      "No se pudo conectar con el cliente Spotify. Revisá la autorización local, el paquete y la huella en Spotify Dashboard."
     ))
    }
   })
  }
 }
 private class OfficialLink(private val remote:SpotifyAppRemote):SpotifyRemoteLink {
  override suspend fun canPlayOnDemand()=remote.userApi.capabilities.awaitResult().canPlayOnDemand
  override suspend fun play(uri:String) { remote.playerApi.play(uri).awaitResult() }
  override suspend fun observe():SpotifyPlaybackObservation {
   val state=remote.playerApi.playerState.awaitResult()
   return SpotifyPlaybackObservation(!state.isPaused,state.track?.uri,null,System.currentTimeMillis())
  }
  override fun close() {
   if(Looper.myLooper()==Looper.getMainLooper())SpotifyAppRemote.disconnect(remote)
   else Handler(Looper.getMainLooper()).post{SpotifyAppRemote.disconnect(remote)}
  }
 }
}
private suspend fun <T> CallResult<T>.awaitResult():T=suspendCancellableCoroutine { pending ->
 pending.invokeOnCancellation{cancel()}
 setResultCallback{value->if(pending.isActive)pending.resume(value)}
 setErrorCallback{if(pending.isActive)pending.resumeWithException(IOException(
  "El cliente Spotify no confirmó la operación. No se reintentó automáticamente."
 ))}
}
