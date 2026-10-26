package dev.bautista.spotlight.integrations.spotify

import kotlinx.coroutines.*
import java.io.IOException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal interface SpotifyRemoteLink {
 val isConnected: Boolean get() = true
 suspend fun canPlayOnDemand(): Boolean
 suspend fun play(uri: String)
 suspend fun observe(): SpotifyPlaybackObservation
 fun close()
}
internal fun interface SpotifyRemoteConnector {
 suspend fun open(clientId: String, authorize: Boolean): SpotifyRemoteLink
}

/**
 * Explicit operations only; never retries or falls back after a possible playback effect.
 * Retains a single active IPC session across play/observe calls within a bounded idle window
 * so Spotify does not receive an immediate disconnect while starting playback.
 */
internal class SpotifyLocalPlayer(
 private val connector: SpotifyRemoteConnector,
 private val ownerScope: CoroutineScope?,
 private val idleTimeoutMs: Long = DEFAULT_IDLE_TIMEOUT_MS,
 private val sleeper: suspend (Long) -> Unit = { delay(it) },
) {
 constructor(connector: SpotifyRemoteConnector) : this(connector, null)

 private val mutex = Mutex()
 @Volatile private var operation: Job? = null
 @Volatile private var activeLink: SpotifyRemoteLink? = null
 @Volatile private var activeClientId: String? = null
 @Volatile private var activeGeneration: Long = 0L
 private var idleJob: Job? = null

 private fun validateClientId(clientId: String) {
  require(clientId.matches(CLIENT_ID_REGEX)) { "Guardá primero el Client ID de Spotify." }
 }

 private fun cancelIdleTimerLocked() {
  activeGeneration++
  val currentJob = idleJob ?: return
  idleJob = null
  currentJob.cancel()
 }

 private fun closeActiveLocked() {
  cancelIdleTimerLocked()
  val link = activeLink
  activeLink = null
  activeClientId = null
  if (link != null) {
   runCatching { link.close() }
  }
 }

 private fun scheduleIdleCloseLocked() {
  val link = activeLink ?: return
  if (idleTimeoutMs <= 0L || ownerScope == null || !ownerScope.isActive) {
   closeActiveLocked()
   return
  }
  cancelIdleTimerLocked()
  val generation = activeGeneration
  val scheduled = ownerScope.launch {
   sleeper(idleTimeoutMs)
   mutex.withLock {
    currentCoroutineContext().ensureActive()
    if (activeGeneration == generation && activeLink === link && operation == null) {
     closeActiveLocked()
    }
   }
  }
  scheduled.invokeOnCompletion { cause ->
   if (cause != null && !ownerScope.isActive && activeGeneration == generation && activeLink === link) {
    activeLink = null
    activeClientId = null
    runCatching { link.close() }
   }
  }
  idleJob = scheduled
  if (!ownerScope.isActive) {
   closeActiveLocked()
  }
 }

 private suspend fun obtainLinkLocked(
  clientId: String,
  opener: suspend () -> SpotifyRemoteLink,
 ): SpotifyRemoteLink {
  val current = activeLink
  if (current != null && activeClientId == clientId && current.isConnected) {
   return current
  }
  closeActiveLocked()
  val opened = opener()
  activeLink = opened
  activeClientId = clientId
  return opened
 }

 private suspend fun <T : Any> withSession(
  clientId: String,
  block: suspend (SpotifyRemoteLink) -> T,
 ): T = mutex.withLock {
  validateClientId(clientId)
  cancelIdleTimerLocked()
  withTimeoutOrNull(OPERATION_TIMEOUT_MS) {
   val job = currentCoroutineContext().job
   operation = job
   try {
    val link = obtainLinkLocked(clientId) {
     connector.open(clientId, false).also {
      activeLink = it
      activeClientId = clientId
     }
    }
    currentCoroutineContext().ensureActive()
    val result = block(link)
    if (activeLink === link && link.isConnected) {
     scheduleIdleCloseLocked()
    } else {
     closeActiveLocked()
    }
    result
   } catch (e: CancellationException) {
    withContext(NonCancellable) {
     closeActiveLocked()
    }
    throw e
   } catch (e: Throwable) {
    closeActiveLocked()
    throw e
   } finally {
    if (operation === job) operation = null
   }
  } ?: throw IOException("Spotify tardó demasiado en responder. No se reintentó la reproducción.")
 }

 suspend fun authorize(clientId: String) = mutex.withLock {
  validateClientId(clientId)
  closeActiveLocked()
  withTimeoutOrNull(OPERATION_TIMEOUT_MS) {
   val job = currentCoroutineContext().job
   operation = job
   var opened: SpotifyRemoteLink? = null
   try {
    val link = connector.open(clientId, true).also { opened = it }
    currentCoroutineContext().ensureActive()
    opened = null
    link.close()
   } finally {
    opened?.let { runCatching { it.close() } }
    if (operation === job) operation = null
   }
  } ?: throw IOException("Spotify tardó demasiado en responder. No se reintentó la reproducción.")
 }

 suspend fun play(clientId: String, item: SpotifySearchItem): String = withSession(clientId) { link ->
  if (item.type == SpotifyItemType.TRACK) check(link.canPlayOnDemand()) {
   "Spotify no habilitó reproducción de canciones a demanda para esta cuenta."
  }
  currentCoroutineContext().ensureActive()
  link.play(item.uri)
  "Solicitud aceptada por el cliente Spotify; comprobá la reproducción."
 }

 suspend fun observe(clientId: String): SpotifyPlaybackObservation = withSession(clientId) { it.observe() }

 suspend fun disconnect() {
  val currentJob = currentCoroutineContext()[Job]
  val running = operation
  if (running != null && running !== currentJob) {
   running.cancelAndJoin()
  }
  withContext(NonCancellable) {
   mutex.withLock {
    closeActiveLocked()
   }
  }
 }

 companion object {
  internal const val DEFAULT_IDLE_TIMEOUT_MS = 8_000L
  private const val OPERATION_TIMEOUT_MS = 20_000L
  private val CLIENT_ID_REGEX = Regex("[A-Za-z0-9]{32}")
 }
}
