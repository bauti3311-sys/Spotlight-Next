package dev.bautista.spotlight.search

import android.app.Application
import dev.bautista.spotlight.ai.AiCredentials
import dev.bautista.spotlight.ai.DirectAi
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.next.NativeProviders
import dev.bautista.spotlight.next.ProviderValues
import dev.bautista.spotlight.next.SportsEntity
import dev.bautista.spotlight.next.SportsFavorites
import java.net.Proxy
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config as RoboConfig

/** Real HTTP on loopback only: no provider credentials, DNS or external API costs. */
@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class OfflineHttpContractTest {
    private lateinit var server:MockWebServer
    private lateinit var client:OkHttpClient
    private val credentials=object:AiCredentials {
        override fun read(provider:String)="fake-key-for-local-tests"
        override fun contains(provider:String)=true
    }
    @Before fun setup() {
        server=MockWebServer().apply{start()}
        client=OkHttpClient.Builder().proxy(Proxy.NO_PROXY).callTimeout(2,TimeUnit.SECONDS)
            .addInterceptor{chain->chain.proceed(chain.request().newBuilder()
                .url(local(chain.request().url)).build())}.build()
        RuntimeEnvironment.getApplication().getSharedPreferences("native_provider_cache_v1",0).edit().clear().commit()
    }
    @After fun cleanup() {
        client.connectionPool.evictAll()
        client.dispatcher.executorService.shutdownNow()
        server.shutdown()
    }
    private fun fixture(name:String)=javaClass.classLoader!!.getResourceAsStream("offline/$name")!!.bufferedReader().use{it.readText()}
    private fun local(url:HttpUrl)=server.url(url.encodedPath).newBuilder().encodedQuery(url.encodedQuery).build()
    private fun ai(client:OkHttpClient=this.client)=DirectAi(credentials,client)
    private fun providers()=NativeProviders(RuntimeEnvironment.getApplication(),fetcher={url->withContext(Dispatchers.IO){
        client.newCall(Request.Builder().url(local(url)).build()).execute().use{response->
            check(response.isSuccessful);JSONObject(response.body!!.string())
        }
    }},clock={1000L})

    @Test fun aiStreamsOverRealHttpWithoutContactingAProvider():Unit=runBlocking {
        server.enqueue(MockResponse().setHeader("Content-Type","text/event-stream").setBody(fixture("ai-success.sse")))
        assertEquals("Hola mundo",ai().answer(Config(),listOf("user" to "hola")).toList().joinToString(""))
        val sent=server.takeRequest(2,TimeUnit.SECONDS)!!
        assertEquals("/v1/responses",sent.path)
        assertEquals("Bearer fake-key-for-local-tests",sent.getHeader("Authorization"))
        assertTrue(sent.body.readUtf8().contains("\"store\":false"))
        assertEquals(1,server.requestCount)
    }
    @Test fun authorizationAndQuotaErrorsDoNotExposeResponseSecrets():Unit=runBlocking {
        for((status,label) in listOf(401 to "Clave",429 to "Límite")) {
            server.enqueue(MockResponse().setResponseCode(status).setBody(fixture("http-error.json")))
            val failure=runCatching{ai().answer(Config(),listOf("user" to "hola")).toList()}.exceptionOrNull()!!
            assertTrue(failure.message.orEmpty().contains(label))
            assertFalse(failure.message.orEmpty().contains("fake-key-for-local-tests"))
        }
        assertEquals(2,server.requestCount)
    }
    @Test fun emptyAiStreamFailsInsteadOfInventingContent():Unit=runBlocking {
        server.enqueue(MockResponse().setBody(fixture("ai-empty.sse")))
        val failure=runCatching{ai().answer(Config(),listOf("user" to "hola")).toList()}.exceptionOrNull()!!
        assertTrue(failure.message.orEmpty().contains("vacía"))
    }
    @Test fun httpTimeoutTerminatesWithoutProviderCalls():Unit=runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val bounded=client.newBuilder().callTimeout(200,TimeUnit.MILLISECONDS).build()
        try {
            val failure=withTimeout(5000){runCatching{ai(bounded).answer(Config(),listOf("user" to "hola")).toList()}.exceptionOrNull()}
            assertNotNull(failure)
            assertTrue(failure!!.message.orEmpty().contains("conectar"))
            assertNotNull(server.takeRequest(2,TimeUnit.SECONDS))
        } finally {bounded.connectionPool.evictAll()}
    }
    @Test fun forecastFixtureIsParsedAndCachedWithoutRepeatedRequests():Unit=runBlocking {
        server.enqueue(MockResponse().setBody(fixture("weather.json")))
        val repo=providers()
        val first=repo.weather()
        assertEquals(18,first.json.getJSONObject("current").getInt("temperature_2m"))
        assertEquals(1000L,first.fetchedAt)
        assertFalse(first.stale)
        assertEquals("—",ProviderValues.number(first.json.getJSONObject("current"),"wind_speed_10m"))
        assertEquals(first.json.toString(),repo.weather().json.toString())
        assertEquals(1,server.requestCount)
        assertTrue(server.takeRequest(2,TimeUnit.SECONDS)!!.path!!.startsWith("/v1/forecast?"))
    }
    @Test fun sportsMissingScoreAndEmptyFixturesStayHonest():Unit=runBlocking {
        val entity=SportsEntity("133602","Equipo A","team","Soccer")
        server.enqueue(MockResponse().setBody(fixture("sports.json")))
        val event=SportsFavorites.events(entity,providers().sportsEntity(entity,true).json).single()
        assertEquals("Marcador no informado",ProviderValues.score(event))
        assertFalse(event.has("strStatus"))
        assertEquals("/api/v1/json/123/eventsnext.php?id=133602",server.takeRequest(2,TimeUnit.SECONDS)!!.path)
        RuntimeEnvironment.getApplication().getSharedPreferences("native_provider_cache_v1",0).edit().clear().commit()
        server.enqueue(MockResponse().setBody(fixture("sports-empty.json")))
        assertTrue(SportsFavorites.events(entity,providers().sportsEntity(entity,true).json).isEmpty())
        assertEquals(2,server.requestCount)
    }
}
