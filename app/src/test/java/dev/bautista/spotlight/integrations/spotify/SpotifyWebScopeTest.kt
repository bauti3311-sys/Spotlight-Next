package dev.bautista.spotlight.integrations.spotify

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SpotifyWebScopeTest {
    @Test fun missingModifyScopeRejectsAnOtherwiseValidToken() = runBlocking {
        val store = object : SpotifyTokenStore {
            override fun read() = SpotifyTokens("access", "refresh", Long.MAX_VALUE, setOf("user-read-playback-state"))
            override fun save(tokens: SpotifyTokens) { error("Unexpected renewal") }
            override fun clear() { error("Must preserve authorization") }
        }
        val session = SpotifySession(store)
        assertEquals("access", session.token("a".repeat(32), setOf("user-read-playback-state")))
        val failure = runCatching { session.token("a".repeat(32), setOf("user-read-playback-state", "user-modify-playback-state")) }.exceptionOrNull()
        assertTrue(failure is IllegalStateException)
        assertTrue(failure!!.message!!.contains("permisos Web API"))
    }
    @Test fun doublePressIsRejectedWithoutQueueAndCancellationCanReleaseGate() {
        val gate = SpotifyRequestGate()
        assertTrue(gate.enter())
        assertFalse(gate.enter())
        try { assertFalse(gate.enter()) } finally { gate.leave() }
        assertTrue(gate.enter())
        gate.leave()
    }
    @Test fun webObservationDoesNotClaimLocalEvidenceOrAnUnobservedContext() {
        val item = SpotifySearchItem(SpotifyItemType.ALBUM, "a".repeat(22), "Album", "Artist")
        val observation = SpotifyPlaybackObservation(true, "spotify:track:" + "b".repeat(22), null, 1)
        val message = observation.diagnose(item, "Web API")
        assertTrue(message.contains("Web API no informó el contexto"))
        assertFalse(message.contains("cliente local"))
        assertFalse(message.contains("confirmó reproducción"))
    }

}
