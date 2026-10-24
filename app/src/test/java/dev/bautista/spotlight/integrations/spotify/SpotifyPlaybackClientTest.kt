package dev.bautista.spotlight.integrations.spotify

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class SpotifyPlaybackClientTest {
    @Test fun typedTrackAndContextRequestsNeverExecuteArbitraryText() {
        val client = SpotifyPlaybackClient()
        for (type in SpotifyItemType.entries) {
            val item = SpotifySearchItem(type, "a".repeat(22), "Fixture", "Artist")
            val request = client.request("play", "fixture-token", item)
            val buffer = okio.Buffer(); request.body!!.writeTo(buffer); val json = JSONObject(buffer.readUtf8())
            assertEquals("PUT", request.method); assertFalse(request.url.toString().contains("fixture-token"))
            if (type == SpotifyItemType.TRACK) { assertEquals(item.uri, json.getJSONArray("uris").getString(0)); assertFalse(json.has("context_uri")) }
            else { assertEquals(item.uri, json.getString("context_uri")); assertFalse(json.has("uris")) }
        }
        assertTrue(runCatching { client.request("DELETE", "fixture-token") }.isFailure)
    }
    @Test fun acceptedRequestAndObservedPlaybackRemainDifferentStates() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            val client = SpotifyPlaybackClient(OkHttpClient(), server.url("/"))
            server.enqueue(MockResponse().setResponseCode(204))
            assertTrue(client.command("pause", "fixture-token").contains("Solicitud aceptada"))
            server.enqueue(MockResponse().setBody("""{"is_playing":true,"item":{"uri":"spotify:track:aaaaaaaaaaaaaaaaaaaaaa"}}"""))
            val state = client.observe("fixture-token")!!
            assertTrue(state.playing); assertEquals("spotify:track:aaaaaaaaaaaaaaaaaaaaaa", state.trackUri)
            server.enqueue(MockResponse().setResponseCode(204)); assertNull(client.observe("fixture-token"))
        } finally { server.shutdown() }
    }
    @Test fun externalSideEffectsAreNeverAutomaticallyRetried() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            val client = SpotifyPlaybackClient(OkHttpClient(), server.url("/"))
            for (status in listOf(403, 404, 429, 500)) {
                server.enqueue(MockResponse().setResponseCode(status).setBody("private-account-details"))
                val error = runCatching { client.command("next", "fixture-token") }.exceptionOrNull() as SpotifyApiException
                assertEquals(status, error.status); assertFalse(error.toString().contains("private-account-details"))
            }
            assertEquals(4, server.requestCount)
        } finally { server.shutdown() }
    }
}
