package dev.bautista.spotlight.integrations.spotify

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class SpotifySearchClientTest {
    private val type=setOf(SpotifyItemType.TRACK)
    private val id="a".repeat(22)
    private fun body()="""{"tracks":{"items":[null,{"id":"$id","uri":"spotify:track:$id","type":"track","name":"Fixture","artists":[{"name":"Artista"}]},{"id":"$id","uri":"spotify:track:$id","type":"track","name":"Duplicado"}]}}"""
    @Test fun requestUsesBoundedCurrentSearchAndAuthorizationHeader()=runBlocking {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(MockResponse().setBody(body()))
            val result=SpotifySearchClient(OkHttpClient(),server.url("/")).search("Cerati",type,"fixture-token")
            val request=server.takeRequest();assertEquals("10",request.requestUrl!!.queryParameter("limit"))
            assertEquals("Bearer fixture-token",request.getHeader("Authorization"));assertFalse(request.path!!.contains("fixture-token"))
            assertEquals(1,result.items.size);assertEquals("Artista",result.items.single().subtitle);assertTrue(result.observedAt>0)
        }finally{server.shutdown()}
    }
    @Test fun forbiddenRateLimitAndRedirectDoNotRetryOrLeakServerBody()=runBlocking {
        val server=MockWebServer();server.start()
        try {
            val client=SpotifySearchClient(OkHttpClient(),server.url("/"))
            for(status in listOf(401,403,429,302)) {
                server.enqueue(MockResponse().setResponseCode(status).setHeader("Retry-After","20").setHeader("Location",server.url("/leak")).setBody("sensitive-provider-body"))
                val error=runCatching{client.search("Cerati",type,"fixture-token")}.exceptionOrNull() as SpotifyApiException
                assertEquals(status,error.status);assertFalse(error.message!!.contains("sensitive-provider-body"))
                assertEquals(20,error.retryAfterSeconds)
                server.takeRequest();assertNull(server.takeRequest(50,TimeUnit.MILLISECONDS))
            }
        }finally{server.shutdown()}
    }
    @Test fun cancellationStopsTheExplicitQuery()=runBlocking {
        val server=MockWebServer();server.start()
        try {
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val client=SpotifySearchClient(OkHttpClient(),server.url("/"))
            val job=launch{client.search("Cerati",type,"fixture-token")}
            withContext(Dispatchers.IO){assertNotNull(server.takeRequest(3,TimeUnit.SECONDS))}
            job.cancelAndJoin();assertTrue(job.isCancelled)
        }finally{server.shutdown()}
    }
    @Test fun malformedItemsUntrustedUrisAndDuplicatesAreNotExecutableResults() {
        assertEquals(1,SpotifySearchClient.parse(body(),type).size)
        val raw=body().replace("spotify:track:$id","https://evil.example/track")
        assertTrue(SpotifySearchClient.parse(raw,type).isEmpty())
        assertTrue(runCatching{SpotifySearchClient.parse("x".repeat(1048577),type)}.isFailure)
    }
    @Test fun invalidQueriesAndHeadersAreRejectedBeforeNetwork() {
        val client=SpotifySearchClient()
        assertTrue(runCatching{client.request("",type,"fixture-token",0)}.isFailure)
        assertTrue(runCatching{client.request("Cerati",type,"bad\r\nheader",0)}.isFailure)
        assertTrue(runCatching{client.request("Cerati",type,"fixture-token",1001)}.isFailure)
    }
}
