package dev.bautista.spotlight.integrations.spotify

import kotlinx.coroutines.*
import java.io.IOException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal interface SpotifyRemoteLink {
 suspend fun canPlayOnDemand(): Boolean
 suspend fun play(uri: String)
 suspend fun observe(): SpotifyPlaybackObservation
 fun close()
}
internal fun interface SpotifyRemoteConnector {
 suspend fun open(clientId: String, authorize: Boolean): SpotifyRemoteLink
}

/** Explicit operations only; never retries or falls back after a possible playback effect. */
internal class SpotifyLocalPlayer(private val connector: SpotifyRemoteConnector) {
 private val mutex=Mutex()
 @Volatile private var operation: Job?=null
 private suspend fun <T:Any> use(clientId:String,authorize:Boolean,block:suspend(SpotifyRemoteLink)->T):T =
  mutex.withLock {
   require(clientId.matches(Regex("[A-Za-z0-9]{32}"))){"Guardá primero el Client ID de Spotify."}
   withTimeoutOrNull(20000) {
    val job=currentCoroutineContext().job
    operation=job
    try {
     val link=connector.open(clientId,authorize)
     try { currentCoroutineContext().ensureActive(); block(link) } finally { link.close() }
    } finally { if(operation===job)operation=null }
   } ?: throw IOException("Spotify tardó demasiado en responder. No se reintentó la reproducción.")
  }
 suspend fun authorize(clientId:String) = use(clientId,true) { Unit }
 suspend fun play(clientId:String,item:SpotifySearchItem):String = use(clientId,false) { link ->
  if(item.type==SpotifyItemType.TRACK)check(link.canPlayOnDemand()){
   "Spotify no habilitó reproducción de canciones a demanda para esta cuenta."
  }
  currentCoroutineContext().ensureActive()
  link.play(item.uri)
  "Solicitud aceptada por el cliente Spotify; comprobá la reproducción."
 }
 suspend fun observe(clientId:String):SpotifyPlaybackObservation = use(clientId,false){it.observe()}
 suspend fun disconnect() {
  operation?.takeIf{it!==currentCoroutineContext().job}?.cancelAndJoin()
 }
}
