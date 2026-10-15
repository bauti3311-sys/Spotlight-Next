package dev.bautista.spotlight.search

import android.app.Application
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config as RoboConfig

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class GeminiModelsTest {
    private val config=Config(aiProvider="gemini")
    private val credentials=object:AiCredentials {override fun read(provider:String)="fixture-not-a-real-key";override fun contains(provider:String)=true}
    @Test fun metadataIsExplicitPaginatedAndNeverContainsPromptOrKeyInUrl()=runBlocking {
        val requests=mutableListOf<Request>()
        val client=OkHttpClient.Builder().addInterceptor{chain->
            val request=chain.request();requests+=request
            val body=if(requests.size==1)"""{"models":[{"name":"models/gemini-fixture-a","supportedGenerationMethods":["generateContent"]},{"name":"models/embedding-fixture","supportedGenerationMethods":["embedContent"]}],"nextPageToken":"page / two"}"""
                else """{"models":[{"name":"models/gemini-fixture-b","supportedGenerationMethods":["generateContent"]}]}"""
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("fixture").body(body.toResponseBody("application/json".toMediaType())).build()
        }.build()
        var transports=0
        val ai=DirectAi(credentials,clientFactory={transports++;client})
        val models=ai.availableGeminiModels(config)
        assertEquals(0,transports);assertTrue(requests.isEmpty())
        assertEquals(listOf("gemini-fixture-a","gemini-fixture-b"),models.single())
        assertEquals(1,transports);assertEquals(2,requests.size)
        assertEquals("page / two",requests.last().url.queryParameter("pageToken"))
        requests.forEach { request->
            assertEquals("GET",request.method);assertNull(request.body)
            assertEquals("generativelanguage.googleapis.com",request.url.host)
            assertEquals("/v1beta/models",request.url.encodedPath)
            assertEquals("fixture-not-a-real-key",request.header("x-goog-api-key"))
            assertFalse(request.url.toString().contains("fixture-not-a-real-key"))
        }
    }
    @Test fun deniedMetadataDoesNotEchoProviderBodyOrSwitchProvider()=runBlocking {
        var requests=0
        val client=OkHttpClient.Builder().addInterceptor{chain->requests++;Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(403).message("fixture").body("fixture-not-a-real-key".toResponseBody()).build()}.build()
        val failure=runCatching{DirectAi(credentials,client).availableGeminiModels(config).single()}.exceptionOrNull()!!
        assertEquals(1,requests);assertTrue(failure.message!!.contains("Clave"));assertFalse(failure.message!!.contains("fixture-not-a-real-key"))
    }
    @Test fun repeatedPaginationFailsInsteadOfLoopingOrPresentingPartialAvailability()=runBlocking {
        var requests=0
        val client=OkHttpClient.Builder().addInterceptor{chain->requests++;Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("fixture").body("""{"models":[],"nextPageToken":"same"}""".toResponseBody()).build()}.build()
        assertNotNull(runCatching{DirectAi(credentials,client).availableGeminiModels(config).single()}.exceptionOrNull())
        assertEquals(2,requests)
    }
}
