package dev.bautista.spotlight.integrations.spotify

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class SpotifySessionTest {
    private class Memory : SpotifyTokenStore {
        var value: SpotifyTokens? = SpotifyTokens("old-access", "old-refresh", 1000, setOf("user-read-playback-state"))
        override fun read() = value
        override fun save(tokens: SpotifyTokens) { value = tokens }
        override fun clear() { value = null }
    }
    private val client = "a".repeat(32)
    private fun response(extra: String = "") = """{"access_token":"new-access","token_type":"Bearer","expires_in":3600$extra}"""
    @Test fun concurrentReadersRenewOnceAndPreserveOmittedRefreshAndScopes() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            server.enqueue(MockResponse().setBody(response()))
            val store = Memory()
            val session = SpotifySession(store, SpotifyHttp(), server.url("/token")) { 2000 }
            val values = coroutineScope { (1..4).map { async { session.token(client) } }.awaitAll() }
            assertEquals(listOf("new-access", "new-access", "new-access", "new-access"), values)
            assertEquals(1, server.requestCount)
            val request = server.takeRequest()
            assertEquals("POST", request.method); assertFalse(request.path!!.contains("old-refresh"))
            assertTrue(request.body.readUtf8().contains("refresh_token=old-refresh"))
            assertNull(request.getHeader("Authorization"))
            assertEquals("old-refresh", store.value!!.refreshToken)
            assertEquals(setOf("user-read-playback-state"), store.value!!.scopes)
            session.disconnect(); assertNull(store.value)
            assertTrue(runCatching { session.token(client) }.isFailure)
            assertEquals(1, server.requestCount)
        } finally { server.shutdown() }
    }
    @Test fun tokenRotationIsDurableAndNeverPrinted() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            server.enqueue(MockResponse().setBody(response(",\"refresh_token\":\"rotated-refresh\"")))
            val store = Memory()
            SpotifySession(store, SpotifyHttp(), server.url("/token")) { 2000 }.token(client)
            val restored = SpotifyTokens.restore(store.value!!.encode())
            assertEquals("rotated-refresh", restored.refreshToken)
            assertEquals("new-access", restored.accessToken)
            assertFalse(restored.toString().contains("access")); assertFalse(restored.toString().contains("refresh"))
        } finally { server.shutdown() }
    }
    @Test fun revokedRefreshClearsStorageAndDoesNotRetry() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(400).setBody("secret-provider-error"))
            val store = Memory()
            val failure = runCatching { SpotifySession(store, SpotifyHttp(), server.url("/token")) { 2000 }.token(client) }.exceptionOrNull()
            assertNotNull(failure); assertFalse(failure.toString().contains("secret-provider-error"))
            assertNull(store.value); assertEquals(1, server.requestCount)
        } finally { server.shutdown() }
    }
    @Test fun redirectAndRateLimitNeverReplayOrDestroyTheOldSession() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            for (status in listOf(302, 429)) {
                server.enqueue(MockResponse().setResponseCode(status).setHeader("Location", server.url("/leak")).setHeader("Retry-After", "30"))
                val store = Memory()
                val failure = runCatching { SpotifySession(store, SpotifyHttp(), server.url("/token")) { 2000 }.token(client) }.exceptionOrNull() as SpotifyApiException
                assertEquals(status, failure.status); assertEquals("old-refresh", store.value!!.refreshToken)
                server.takeRequest(); assertNull(server.takeRequest(50, TimeUnit.MILLISECONDS))
            }
        } finally { server.shutdown() }
    }
    @Test fun cancelledRenewalDoesNotInstallTokensAndDisconnectCanComplete() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val store = Memory(); val session = SpotifySession(store, SpotifyHttp(), server.url("/token")) { 2000 }
            val job = launch { session.token(client) }
            withContext(Dispatchers.IO) { assertNotNull(server.takeRequest(3, TimeUnit.SECONDS)) }
            job.cancelAndJoin(); assertEquals("old-access", store.value!!.accessToken)
            session.disconnect(); assertNull(store.value)
        } finally { server.shutdown() }
    }
    @Test fun malformedTokenResponsesAndUntrustedOriginsFailClosed() {
        for (raw in listOf(response().replace("Bearer", "Basic"), response().replace("3600", "-1"), response().replace("new-access", "private\\r\\nvalue"), "{}")) {
            assertTrue(runCatching { SpotifyTokens.response(raw, 1000) }.isFailure)
        }
        assertTrue(runCatching { SpotifyTokens.restore("{}") }.isFailure)
        assertTrue(runCatching { SpotifySession(Memory(), SpotifyHttp(), okhttp3.HttpUrl.Builder().scheme("https").host("evil.example").build()) }.isFailure)
    }
    @Test fun renewedTokenCannotSilentlyLoseTheRequiredPlaybackScope() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            val required = setOf("user-read-playback-state", "user-modify-playback-state")
            val store = Memory()
            store.value = SpotifyTokens("old-access", "old-refresh", 1000, required)
            server.enqueue(MockResponse().setBody(response(",\"scope\":\"user-read-playback-state\"")))
            val session = SpotifySession(store, SpotifyHttp(), server.url("/token")) { 2000 }
            val failure = runCatching { session.token(client, required) }.exceptionOrNull()
            assertTrue(failure is IllegalStateException)
            assertEquals("old-access", store.value!!.accessToken)
            assertEquals(required, store.value!!.scopes)
            assertEquals(1, server.requestCount)
        } finally { server.shutdown() }
    }

}
