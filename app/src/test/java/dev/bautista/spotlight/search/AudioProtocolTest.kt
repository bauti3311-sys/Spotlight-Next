package dev.bautista.spotlight.search

import android.app.Application
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config as RoboConfig

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class AudioProtocolTest {
    @Test fun directAudioKeysUseHeaders(){val r=AudioProtocol.request(Config(),"test-key",byteArrayOf(1,2),"audio/mpeg","audio.mp3");assertEquals("/v1/audio/transcriptions",r.url.encodedPath);assertEquals("Bearer test-key",r.header("Authorization"));assertFalse(r.url.toString().contains("test-key"));val g=AudioProtocol.request(Config(aiProvider="gemini",aiModel="gemini-3.5-flash"),"test-key",byteArrayOf(1),"audio/mpeg","audio.mp3");assertEquals("test-key",g.header("x-goog-api-key"));assertFalse(g.url.toString().contains("test-key"))}
    @Test fun transcriptionErrorsAndLimitsAreExplicit(){assertEquals("Texto",AudioProtocol.transcript("openai","{\"text\":\"Texto\"}"));assertThrows(IllegalArgumentException::class.java){AudioProtocol.request(Config(aiProvider="gemini"),"key",ByteArray(12_000_001),"audio/mpeg","test")};assertThrows(IllegalStateException::class.java){AudioProtocol.transcript("gemini","{\"candidates\":[{\"finishReason\":\"MAX_TOKENS\"}]}")}}
}
