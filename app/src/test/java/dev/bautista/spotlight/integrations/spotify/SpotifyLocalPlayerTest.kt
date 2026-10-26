package dev.bautista.spotlight.integrations.spotify
import kotlinx.coroutines.*
import org.junit.Test
import org.junit.Assert.*

class SpotifyLocalPlayerTest {
 private val clientId = "a".repeat(32)
 private val item = SpotifySearchItem(SpotifyItemType.TRACK, "a".repeat(22), "Fixture", "Artist")

 private class Link : SpotifyRemoteLink {
  val calls = mutableListOf<String>()
  val closedSignal = CompletableDeferred<Unit>()
  var allowed = true
  var fail = false
  var connected = true
  var observation = SpotifyPlaybackObservation(true, null, null, 0)
  override val isConnected: Boolean get() = connected
  override suspend fun canPlayOnDemand(): Boolean {
   calls += "capability"
   return allowed
  }
  override suspend fun play(uri: String) {
   calls += uri
   if (fail) error("fixture failure")
  }
  override suspend fun observe(): SpotifyPlaybackObservation {
   calls += "observe"
   if (fail) error("fixture observe failure")
   return observation
  }
  override fun close() {
   connected = false
   calls += "close"
   closedSignal.complete(Unit)
  }
 }

 @Test fun authorizationDoesNotPlayAndSelectedPlayDoesNotShowAuthorization() = runBlocking {
  val link = Link()
  val authorization = mutableListOf<Boolean>()
  val player = SpotifyLocalPlayer { _, auth ->
   authorization += auth
   link
  }
  player.authorize(clientId)
  assertEquals(listOf("close"), link.calls)
  link.calls.clear()
  link.connected = true
  assertTrue(player.play(clientId, item).contains("Solicitud aceptada"))
  assertEquals(listOf(true, false), authorization)
  assertEquals(listOf("capability", item.uri, "close"), link.calls)
 }

 @Test fun selectedPlayWithOwnerScopeRetainsSessionForObservationUntilDisconnect() = runBlocking {
  val authLink = Link()
  val playLink = Link().apply {
   observation = SpotifyPlaybackObservation(true, item.uri, null, 10L, "Fixture", "Artist")
  }
  val authorization = mutableListOf<Boolean>()
  val player = SpotifyLocalPlayer(
   connector = { _, auth ->
    authorization += auth
    if (auth) authLink else playLink
   },
   ownerScope = this,
   idleTimeoutMs = 60_000L,
   sleeper = { awaitCancellation() },
  )
  player.authorize(clientId)
  assertEquals(listOf("close"), authLink.calls)

  assertTrue(player.play(clientId, item).contains("Solicitud aceptada"))
  assertEquals(listOf("capability", item.uri), playLink.calls)

  val observed = player.observe(clientId)
  assertTrue(observed.matches(item))
  assertEquals(listOf(true, false), authorization)
  assertEquals(listOf("capability", item.uri, "observe"), playLink.calls)

  player.disconnect()
  assertEquals(listOf("capability", item.uri, "observe", "close"), playLink.calls)
 }

 @Test fun idleTimeoutClosesRetainedSessionDeterministicallyWithoutLeaking() = runBlocking {
  val link = Link()
  val releaseIdle = CompletableDeferred<Unit>()
  val player = SpotifyLocalPlayer(
   connector = { _, _ -> link },
   ownerScope = this,
   idleTimeoutMs = 8_000L,
   sleeper = { releaseIdle.await() },
  )
  player.play(clientId, item)
  assertEquals(listOf("capability", item.uri), link.calls)

  releaseIdle.complete(Unit)
  withTimeout(5_000L) {
   link.closedSignal.await()
  }
  assertEquals(listOf("capability", item.uri, "close"), link.calls)
 }

 @Test fun cancellingOwnerScopeClosesRetainedSessionWithoutLeaking() = runBlocking {
  val link = Link()
  val childJob = SupervisorJob(coroutineContext[Job])
  val scoped = CoroutineScope(coroutineContext + childJob)
  val player = SpotifyLocalPlayer(
   connector = { _, _ -> link },
   ownerScope = scoped,
   idleTimeoutMs = 60_000L,
   sleeper = { awaitCancellation() },
  )
  player.play(clientId, item)
  assertEquals(listOf("capability", item.uri), link.calls)

  childJob.cancelAndJoin()
  withTimeout(5_000L) {
   link.closedSignal.await()
  }
  assertEquals(listOf("capability", item.uri, "close"), link.calls)
 }

 @Test fun clientIdChangeClosesPreviousSessionBeforeOpeningNewOne() = runBlocking {
  val firstLink = Link()
  val secondLink = Link()
  val seenClients = mutableListOf<String>()
  val player = SpotifyLocalPlayer(
   connector = { id, _ ->
    seenClients += id
    if (seenClients.size == 1) firstLink else secondLink
   },
   ownerScope = this,
   idleTimeoutMs = 60_000L,
   sleeper = { awaitCancellation() },
  )
  player.play(clientId, item)
  assertEquals(listOf("capability", item.uri), firstLink.calls)

  val secondClientId = "b".repeat(32)
  player.play(secondClientId, item)
  assertEquals(listOf("capability", item.uri, "close"), firstLink.calls)
  assertEquals(listOf("capability", item.uri), secondLink.calls)

  player.disconnect()
  assertEquals(listOf("capability", item.uri, "close"), secondLink.calls)
 }

 @Test fun unsupportedOnDemandPlaybackDoesNotSendPlay() = runBlocking {
  val link = Link().apply { allowed = false }
  val player = SpotifyLocalPlayer(
   connector = { _, _ -> link },
   ownerScope = this,
   idleTimeoutMs = 60_000L,
   sleeper = { awaitCancellation() },
  )
  assertTrue(runCatching { player.play(clientId, item) }.isFailure)
  assertEquals(listOf("capability", "close"), link.calls)
 }

 @Test fun failedPlaybackIsNotRetriedOrFollowedByARecoveryCommand() = runBlocking {
  val link = Link().apply { fail = true }
  val player = SpotifyLocalPlayer(
   connector = { _, _ -> link },
   ownerScope = this,
   idleTimeoutMs = 60_000L,
   sleeper = { awaitCancellation() },
  )
  assertTrue(runCatching { player.play(clientId, item) }.isFailure)
  assertEquals(listOf("capability", item.uri, "close"), link.calls)
 }

 @Test fun cancellationDuringConnectionNeverSendsPlaybackAndClosesIfConnectedLate() = runBlocking {
  var opened = 0
  val player = SpotifyLocalPlayer { _, _ ->
   opened++
   awaitCancellation()
  }
  val task = launch(start = CoroutineStart.UNDISPATCHED) { player.play(clientId, item) }
  task.cancelAndJoin()
  assertEquals(1, opened)
  assertTrue(task.isCancelled)

  val lateLink = Link()
  var lateTaskJob: Job? = null
  val latePlayer = SpotifyLocalPlayer(
   connector = { _, _ ->
    lateTaskJob?.cancel()
    lateLink
   },
   ownerScope = this,
   idleTimeoutMs = 60_000L,
   sleeper = { awaitCancellation() },
  )
  val lateTask = launch(start = CoroutineStart.UNDISPATCHED) {
   lateTaskJob = coroutineContext[Job]
   latePlayer.play(clientId, item)
  }
  lateTask.join()
  assertTrue(lateTask.isCancelled)
  assertEquals(listOf("close"), lateLink.calls)
 }

 @Test fun playbackObservationDistinguishesConfirmedPausedUnknownContextAndDifferentTrack() {
  val confirmed = SpotifyPlaybackObservation(true, item.uri, null, 1L, "Fixture", "Artist")
  assertEquals("Spotify confirmó reproducción del contenido seleccionado.", confirmed.diagnose(item))

  val pausedOnTarget = SpotifyPlaybackObservation(false, item.uri, null, 2L, "Fixture", "Artist")
  assertEquals(
   "Spotify cargó el contenido seleccionado, pero el reproductor está en pausa.",
   pausedOnTarget.diagnose(item),
  )

  val playingOther = SpotifyPlaybackObservation(true, "spotify:track:other", null, 3L, "Other", "Artist")
  assertEquals(
   "Spotify sigue reproduciendo otro contenido; aún no confirmó el cambio a la selección.",
   playingOther.diagnose(item),
  )

  val pausedOther = SpotifyPlaybackObservation(false, "spotify:track:other", null, 4L)
  assertEquals(
   "Spotify no confirmó la reproducción del contenido seleccionado.",
   pausedOther.diagnose(item),
  )

  val albumItem = SpotifySearchItem(SpotifyItemType.ALBUM, "c".repeat(22), "Album", "Artist")
  val playingAlbumWithoutContext = SpotifyPlaybackObservation(true, item.uri, null, 5L, "Song", "Artist")
  assertEquals(
   "Spotify está reproduciendo en este momento, pero el cliente local no informó el contexto para verificar si corresponde a la selección.",
   playingAlbumWithoutContext.diagnose(albumItem),
  )

  val pausedAlbumWithoutContext = SpotifyPlaybackObservation(false, item.uri, null, 6L, "Song", "Artist")
  assertEquals(
   "Spotify está en pausa y el cliente local no informó el contexto para verificar si corresponde a la selección.",
   pausedAlbumWithoutContext.diagnose(albumItem),
  )

  val playingAlbumWithMatchingContext = SpotifyPlaybackObservation(true, item.uri, albumItem.uri, 7L, "Song", "Artist")
  assertEquals(
   "Spotify confirmó reproducción del contenido seleccionado.",
   playingAlbumWithMatchingContext.diagnose(albumItem),
  )
 }
}
