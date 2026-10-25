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
import java.util.concurrent.TimeUnit

/** Controlled API failures, never an authenticated account or actual playback. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class SpotifyPlaybackFailureTest {
    @Test fun explicitMissingDeviceReasonSurvivesWithoutPrivateResponseText() = runBlocking {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(404).setBody("""{"error":{"status":404,"reason":"NO_ACTIVE_DEVICE","message":"PRIVATE_RESPONSE_SENTINEL"}}"""))
            val client=SpotifyPlaybackClient(OkHttpClient(),server.url("/"))
            val failure=runCatching{client.command("play","fixture-token")}.exceptionOrNull() as SpotifyApiException
            assertEquals(404,failure.status)
            assertTrue(failure.message!!.contains("NO_ACTIVE_DEVICE"))
            assertFalse(failure.toString().contains("PRIVATE_RESPONSE_SENTINEL"))
        } finally {server.shutdown()}
    }
    @Test fun unknownMissingResourceDoesNotInventAnInactiveDevice() = runBlocking {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(404).setBody("""{"error":{"reason":"PRIVATE_REASON_SENTINEL","message":"PRIVATE_RESPONSE_SENTINEL"}}"""))
            val failure=runCatching{SpotifyPlaybackClient(OkHttpClient(),server.url("/")).command("play","fixture-token")}.exceptionOrNull() as SpotifyApiException
            assertTrue(failure.message!!.contains("No se pudo determinar el motivo"))
            assertFalse(failure.toString().contains("PRIVATE_"))
            assertFalse(failure.message!!.contains("NO_ACTIVE_DEVICE"))
        } finally {server.shutdown()}
    }
    @Test fun rejectedTrackSendsOnePlayAndNoPauseOrAutomaticRecovery() = runBlocking {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(404).setBody("""{"error":{"reason":"NO_ACTIVE_DEVICE"}}"""))
            val item=SpotifySearchItem(SpotifyItemType.TRACK,"a".repeat(22),"Fixture","Artist")
            val failure=runCatching{SpotifyPlaybackClient(OkHttpClient(),server.url("/")).command("play","fixture-token",item)}.exceptionOrNull()
            assertTrue(failure is SpotifyApiException)
            val request=server.takeRequest(1,TimeUnit.SECONDS)!!
            assertEquals("PUT",request.method)
            assertEquals("/v1/me/player/play",request.path)
            val body=JSONObject(request.body.readUtf8())
            assertEquals(item.uri,body.getJSONArray("uris").getString(0))
            assertFalse(body.has("context_uri"))
            assertEquals(1,server.requestCount)
            assertNull(server.takeRequest(200,TimeUnit.MILLISECONDS))
        } finally {server.shutdown()}
    }
    @Test fun authorizationStatusWinsOverAnUnrelatedPlayerReason() = runBlocking {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":{"reason":"NO_ACTIVE_DEVICE","message":"PRIVATE_RESPONSE_SENTINEL"}}"""))
            val failure=runCatching{SpotifyPlaybackClient(OkHttpClient(),server.url("/")).command("play","fixture-token")}.exceptionOrNull() as SpotifyApiException
            assertEquals(401,failure.status)
            assertTrue(failure.message!!.contains("autorizar"))
            assertFalse(failure.toString().contains("NO_ACTIVE_DEVICE"))
            assertFalse(failure.toString().contains("PRIVATE_RESPONSE_SENTINEL"))
        } finally {server.shutdown()}
    }
    @Test fun malformedAndOversizedFailureBodiesRemainBoundedAndPrivate() = runBlocking {
        val server=MockWebServer();server.start()
        try {
            for(body in listOf("{PRIVATE_RESPONSE_SENTINEL", """{"error":{"reason":"NO_ACTIVE_DEVICE","message":"PRIVATE_RESPONSE_SENTINEL"}}"""+" ".repeat(5000))) {
                server.enqueue(MockResponse().setResponseCode(404).setBody(body))
                val failure=runCatching{SpotifyPlaybackClient(OkHttpClient(),server.url("/")).command("play","fixture-token")}.exceptionOrNull() as SpotifyApiException
                assertEquals(404,failure.status)
                assertTrue(failure.message!!.contains("No se pudo determinar el motivo"))
                assertFalse(failure.toString().contains("PRIVATE_RESPONSE_SENTINEL"))
                assertFalse(failure.toString().contains("NO_ACTIVE_DEVICE"))
            }
            assertEquals(2,server.requestCount)
        } finally {server.shutdown()}
    }
}
