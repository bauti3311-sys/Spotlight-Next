package dev.bautista.spotlight.integrations.spotify

import kotlinx.coroutines.*
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

/** Real HTTP transport against loopback fixtures only, never an account or actual audio. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class SpotifyPlaybackGuardTest {
    private val item=SpotifySearchItem(SpotifyItemType.TRACK,"a".repeat(22),"Fixture","Artist")
    private fun active(id:String="fixture-device",playing:Boolean=true)=JSONObject()
        .put("device",JSONObject().put("id",id).put("is_active",true).put("is_restricted",false))
        .put("is_playing",playing).toString()

    @Test fun selectedTrackChecksAndTargetsTheLiveDeviceWithoutPausingOrTransferring() = runBlocking {
        val server=MockWebServer();server.start()
        try {
            val device="fixture-device?other=ignored"
            server.enqueue(MockResponse().setBody(active(device)))
            server.enqueue(MockResponse().setResponseCode(204))
            val result=SpotifyPlaybackClient(OkHttpClient(),server.url("/")).playSelection("fixture-token",item)
            assertTrue(result.contains("Solicitud aceptada"))
            assertFalse(result.contains("confirmó"))
            val probe=server.takeRequest(1,TimeUnit.SECONDS)!!
            assertEquals("GET",probe.method)
            assertEquals("/v1/me/player",probe.path)
            val play=server.takeRequest(1,TimeUnit.SECONDS)!!
            assertEquals("PUT",play.method)
            assertEquals("/v1/me/player/play",play.requestUrl!!.encodedPath)
            assertEquals(device,play.requestUrl!!.queryParameter("device_id"))
            assertEquals(setOf("device_id"),play.requestUrl!!.queryParameterNames)
            assertEquals(item.uri,JSONObject(play.body.readUtf8()).getJSONArray("uris").getString(0))
            assertEquals(2,server.requestCount)
            assertNull(server.takeRequest(200,TimeUnit.MILLISECONDS))
        } finally {server.shutdown()}
    }

    @Test fun activePausedDeviceMayReceiveAnExplicitPlayRequest() = runBlocking {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(MockResponse().setBody(active(playing=false)))
            server.enqueue(MockResponse().setResponseCode(204))
            SpotifyPlaybackClient(OkHttpClient(),server.url("/")).playSelection("fixture-token",item)
            assertEquals(2,server.requestCount)
        } finally {server.shutdown()}
    }

    @Test fun absentInactiveRestrictedOrUnverifiedDevicesNeverReceiveAPlaybackMutation() = runBlocking {
        val server=MockWebServer();server.start()
        try {
            val client=SpotifyPlaybackClient(OkHttpClient(),server.url("/"))
            val bodies=listOf(
                "", "{}", "{PRIVATE_RESPONSE_SENTINEL",
                """{"device":{"id":"fixture","is_active":false,"is_restricted":false}}""",
                """{"device":{"id":"fixture","is_active":true,"is_restricted":true}}""",
                """{"device":{"id":"fixture","is_active":true}}""",
                """{"device":{"id":"fixture","is_active":"true","is_restricted":false}}""",
                """{"device":{"id":null,"is_active":true,"is_restricted":false}}""",
                """{"device":{"id":123,"is_active":true,"is_restricted":false}}""",
                active(""), active("x".repeat(257)), active("bad\nidentifier"),
            )
            for ((index,body) in bodies.withIndex()) {
                server.enqueue(MockResponse().setBody(body))
                val failure=runCatching{client.playSelection("fixture-token",item)}.exceptionOrNull()
                assertNotNull(failure)
                assertTrue(failure!!.message!!.contains("No se solicitó cambiar la música"))
                assertFalse(failure.toString().contains("PRIVATE_RESPONSE_SENTINEL"))
                assertEquals(index+1,server.requestCount)
                assertEquals("GET",server.takeRequest(1,TimeUnit.SECONDS)!!.method)
                assertNull(server.takeRequest(100,TimeUnit.MILLISECONDS))
            }
        } finally {server.shutdown()}
    }

    @Test fun rejectedStateReadDoesNotFallThroughToPlayOrRetry() = runBlocking {
        val server=MockWebServer();server.start()
        try {
            val client=SpotifyPlaybackClient(OkHttpClient(),server.url("/"))
            for ((index,status) in listOf(401,403,404,429,500).withIndex()) {
                server.enqueue(MockResponse().setResponseCode(status).setBody("PRIVATE_RESPONSE_SENTINEL"))
                val failure=runCatching{client.playSelection("fixture-token",item)}.exceptionOrNull() as SpotifyApiException
                assertEquals(status,failure.status)
                assertFalse(failure.toString().contains("PRIVATE_RESPONSE_SENTINEL"))
                assertEquals(index+1,server.requestCount)
                assertEquals("GET",server.takeRequest(1,TimeUnit.SECONDS)!!.method)
                assertNull(server.takeRequest(100,TimeUnit.MILLISECONDS))
            }
        } finally {server.shutdown()}
    }

    @Test fun failedTargetedPlayDoesNotPauseTransferFallbackOrAutomaticallyReplay() = runBlocking {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(MockResponse().setBody(active()))
            server.enqueue(MockResponse().setResponseCode(404).setBody(
                """{"error":{"reason":"NO_ACTIVE_DEVICE","message":"PRIVATE_RESPONSE_SENTINEL"}}"""))
            val failure=runCatching{SpotifyPlaybackClient(OkHttpClient(),server.url("/"))
                .playSelection("fixture-token",item)}.exceptionOrNull() as SpotifyApiException
            assertEquals(SpotifyFailureReason.NO_ACTIVE_DEVICE,failure.reason)
            assertFalse(failure.toString().contains("PRIVATE_RESPONSE_SENTINEL"))
            assertEquals("GET",server.takeRequest(1,TimeUnit.SECONDS)!!.method)
            val play=server.takeRequest(1,TimeUnit.SECONDS)!!
            assertEquals("PUT",play.method)
            assertEquals("/v1/me/player/play?device_id=fixture-device",play.path)
            assertEquals(2,server.requestCount)
            assertNull(server.takeRequest(200,TimeUnit.MILLISECONDS))
        } finally {server.shutdown()}
    }

    @Test fun cancellingTheStateReadNeverSchedulesPlayback() = runBlocking {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(MockResponse().setBody(active()).setBodyDelay(2,TimeUnit.SECONDS))
            val client=SpotifyPlaybackClient(OkHttpClient(),server.url("/"))
            val job=launch(start=CoroutineStart.UNDISPATCHED){client.playSelection("fixture-token",item)}
            assertEquals("GET",server.takeRequest(1,TimeUnit.SECONDS)!!.method)
            job.cancelAndJoin()
            assertTrue(job.isCancelled)
            assertEquals(1,server.requestCount)
            assertNull(server.takeRequest(200,TimeUnit.MILLISECONDS))
        } finally {server.shutdown()}
    }
    @Test fun targetedRejectionsNeverReplayTheConfirmedSelection() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            val client = SpotifyPlaybackClient(OkHttpClient(), server.url("/"))
            for (status in listOf(401, 403, 404, 429)) {
                server.enqueue(MockResponse().setBody(active()))
                server.enqueue(MockResponse().setResponseCode(status).setHeader("Retry-After", "30"))
                val failure = runCatching { client.playSelection("fixture-token", item) }.exceptionOrNull() as SpotifyApiException
                assertEquals(status, failure.status)
                assertEquals("GET", server.takeRequest(1, TimeUnit.SECONDS)!!.method)
                assertEquals("PUT", server.takeRequest(1, TimeUnit.SECONDS)!!.method)
                assertNull(server.takeRequest(100, TimeUnit.MILLISECONDS))
            }
            assertEquals(8, server.requestCount)
        } finally { server.shutdown() }
    }

    @Test fun concurrentConfirmationsProduceOnlyOnePlaybackMutation() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            server.enqueue(MockResponse().setBody(active()).setBodyDelay(200, TimeUnit.MILLISECONDS))
            server.enqueue(MockResponse().setResponseCode(204))
            val client = SpotifyPlaybackClient(OkHttpClient(), server.url("/"))
            val gate = SpotifyRequestGate()
            suspend fun confirm() {
                if (!gate.enter()) return
                try { client.playSelection("fixture-token", item) } finally { gate.leave() }
            }
            coroutineScope {
                val first = launch(start = CoroutineStart.UNDISPATCHED) { confirm() }
                val second = launch(start = CoroutineStart.UNDISPATCHED) { confirm() }
                joinAll(first, second)
            }
            assertEquals("GET", server.takeRequest(1, TimeUnit.SECONDS)!!.method)
            assertEquals("PUT", server.takeRequest(1, TimeUnit.SECONDS)!!.method)
            assertEquals(2, server.requestCount)
            assertNull(server.takeRequest(100, TimeUnit.MILLISECONDS))
        } finally { server.shutdown() }
    }

    @Test fun cancellingAfterThePutDoesNotReplayOrClaimToUndoIt() = runBlocking {
        val server = MockWebServer(); server.start()
        try {
            server.enqueue(MockResponse().setBody(active()))
            server.enqueue(MockResponse().setResponseCode(204).setHeadersDelay(2, TimeUnit.SECONDS))
            val client = SpotifyPlaybackClient(OkHttpClient(), server.url("/"))
            val job = launch(start = CoroutineStart.UNDISPATCHED) { client.playSelection("fixture-token", item) }
            withContext(Dispatchers.IO) {
                assertEquals("GET", server.takeRequest(2, TimeUnit.SECONDS)!!.method)
                assertEquals("PUT", server.takeRequest(2, TimeUnit.SECONDS)!!.method)
            }
            job.cancelAndJoin()
            assertTrue(job.isCancelled)
            assertEquals(2, server.requestCount)
            assertNull(server.takeRequest(100, TimeUnit.MILLISECONDS))
        } finally { server.shutdown() }
    }

}
