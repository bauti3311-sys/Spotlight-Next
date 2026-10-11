package dev.bautista.spotlight.search

import android.app.Application
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.*
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
class AiTransportTest {
    private val creds=object:AiCredentials{override fun read(provider:String)="private-test-value";override fun contains(provider:String)=true}
    private fun client(body:String,status:Int=200)=OkHttpClient.Builder().addInterceptor{chain->Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(status).message("fixture").body(body.toResponseBody("text/event-stream".toMediaType())).build()}.build()
    @Test fun multilineSseAndLastUnterminatedEventAreParsed():Unit=runBlocking {val data="data: {\"type\":\"response.output_text.delta\",\ndata: \"delta\":\"Hola \"}\n\ndata: {\"type\":\"response.output_text.delta\",\"delta\":\"mundo\"}";assertEquals("Hola mundo",DirectAi(creds,client(data)).answer(Config(),listOf("user" to "hola")).toList().joinToString(""))}
    @Test fun authorizationErrorsNeverEchoSecrets():Unit=runBlocking {val error=runCatching{DirectAi(creds,client("private-test-value",401)).answer(Config(),listOf("user" to "hola")).collect{}}.exceptionOrNull()!!;assertTrue(error.message!!.contains("Clave"));assertFalse(error.message!!.contains("private-test-value"))}
    @Test fun emptyAndMalformedStreamsFail():Unit=runBlocking {for(raw in listOf("data: [DONE]\n\n","data: broken\n\n")){assertNotNull(runCatching{DirectAi(creds,client(raw)).answer(Config(),listOf("user" to "hola")).collect{}}.exceptionOrNull())}}
}
