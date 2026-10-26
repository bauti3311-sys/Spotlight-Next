package dev.bautista.spotlight.integrations.spotify

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

class SpotifyLoopbackTest {
    private suspend fun get(uri: String): Int = withContext(Dispatchers.IO) {
        (URL(uri).openConnection() as HttpURLConnection).let { connection ->
            try { connection.connectTimeout = 3000; connection.readTimeout = 3000; connection.responseCode }
            finally { connection.disconnect() }
        }
    }
    @Test fun localCallbackRejectsWrongStateThenAcceptsExactlyOneResponse() = runBlocking {
        SpotifyLoopback().use { receiver ->
            assertTrue(receiver.redirect.startsWith("http://127.0.0.1:"))
            val pending = SpotifyPkce.prepare("a".repeat(32), receiver.redirect)
            val result = async(Dispatchers.IO) { receiver.awaitExchange(pending) }
            assertEquals(400, get("${receiver.redirect}?code=fixture-code&state=wrong"))
            assertFalse(pending.consumed)
            assertEquals(200, get("${receiver.redirect}?code=fixture-code&state=${pending.state}"))
            val request = withTimeout(4000) { result.await() }
            assertEquals("accounts.spotify.com", request.url.host)
            assertTrue(pending.consumed); assertNull(request.url.query)
        }
    }
    @Test fun cancelledAuthorizationClosesItsPortWithoutAnyExchange() = runBlocking {
        val receiver = SpotifyLoopback(); val redirect = receiver.redirect
        val pending = SpotifyPkce.prepare("a".repeat(32), redirect)
        val job = launch(Dispatchers.IO) { receiver.use { it.awaitExchange(pending) } }
        yield(); withTimeout(4000) { job.cancelAndJoin() }
        receiver.close()
        assertFalse(pending.consumed)
        assertTrue(runCatching { get("$redirect?code=fixture&state=${pending.state}") }.isFailure)
    }
    @Test fun requestedScopesAreMinimalAndOtherScopesCannotBeInjected() {
        val pending = SpotifyPkce.prepare("a".repeat(32), "http://127.0.0.1:43210/spotify/callback", scopes = setOf("user-read-playback-state", "user-modify-playback-state"))
        val url = SpotifyPkce.authorizationUrl(pending)
        assertTrue(url.contains("scope=")); assertFalse(url.contains("user-library-read")); assertFalse(url.contains("client_secret"))
        assertTrue(runCatching { SpotifyPkce.prepare("a".repeat(32), pending.redirect, scopes = setOf("playlist-modify-public")) }.isFailure)
    }
}
