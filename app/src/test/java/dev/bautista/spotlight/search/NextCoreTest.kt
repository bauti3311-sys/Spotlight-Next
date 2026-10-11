package dev.bautista.spotlight.search

import android.app.Application
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config as RoboConfig
import org.json.JSONObject
import okio.Buffer

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class NextCoreTest {
    @Test fun officialRequestsKeepKeysOutOfUrlsAndLimitHistory() {
        val messages=(1..20).map{"user" to "Pregunta $it"}
        val open=AiProtocol.request(Config(),"test-private",messages)
        assertEquals("api.openai.com",open.url.host);assertEquals("Bearer test-private",open.header("Authorization"));assertFalse(open.url.toString().contains("test-private"))
        val buffer=Buffer();open.body!!.writeTo(buffer);val body=JSONObject(buffer.readUtf8());assertFalse(body.getBoolean("store"));assertEquals(12,body.getJSONArray("input").length())
        val gem=AiProtocol.request(Config(aiProvider="gemini",aiModel=AiProtocol.defaultModel("gemini")),"test-private",messages)
        assertEquals("test-private",gem.header("x-goog-api-key"));assertEquals("sse",gem.url.queryParameter("alt"));assertFalse(gem.url.toString().contains("test-private"))
    }
    @Test fun streamedDeltasAndIncompleteResponses() {
        assertEquals("Hola",AiProtocol.delta("openai","{\"type\":\"response.output_text.delta\",\"delta\":\"Hola\"}"))
        assertEquals("Texto",AiProtocol.delta("gemini","{\"candidates\":[{\"content\":{\"parts\":[{\"thought\":true,\"text\":\"oculto\"},{\"text\":\"Texto\"}]}}]}"))
        assertThrows(IllegalStateException::class.java){AiProtocol.delta("openai","{\"type\":\"response.incomplete\"}")}
        assertThrows(IllegalStateException::class.java){AiProtocol.delta("gemini","{\"candidates\":[{\"finishReason\":\"MAX_TOKENS\"}]}")}
    }
    @Test fun exactMatchSurvivesLearnedFuzzyAndDuplicateSources() {
        val exact=Result("a","Spotify","","Aplicaciones",1000,Action.Copy(""))
        val fuzzy=Result("b","Spotify Lite","","Aplicaciones",800,Action.Copy(""))
        val c=Config(usageData="{\"b\":{\"count\":10000}}")
        val rows=ResultRanker.rank("spotify",listOf(fuzzy,exact,exact),c)
        assertEquals(listOf("a","b"),rows.map{it.id})
        assertEquals(ResultRanker.rank("spotify",listOf(exact,fuzzy),Config()),ResultRanker.rank("spotify",listOf(exact,fuzzy),c.copy(learning=false)))
    }
}
