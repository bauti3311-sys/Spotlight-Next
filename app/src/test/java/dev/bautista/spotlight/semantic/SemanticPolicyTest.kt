package dev.bautista.spotlight.semantic

import dev.bautista.spotlight.control.DeviceResourceState
import dev.bautista.spotlight.search.SearchQuery
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.json.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=android.app.Application::class)
class SemanticPolicyTest {
    private val healthy=DeviceResourceState(80,false,false,2L*1024*1024*1024,128L*1024*1024,false,0)
    @Test fun devicePressureAndUnknownStateNeverAdmitEmbedding() {
        assertNull(SemanticPolicy.deferred(healthy))
        for(s in listOf(healthy.copy(lowMemory=true),healthy.copy(availableMemory=128L*1024*1024),healthy.copy(batteryPercent=10),healthy.copy(powerSave=true),healthy.copy(thermalStatus=2),healthy.copy(availableMemory=null),healthy.copy(thermalStatus=null)))assertNotNull(SemanticPolicy.deferred(s))
        assertNull(SemanticPolicy.deferred(healthy.copy(batteryPercent=10,charging=true)))
        assertNull(SemanticPolicy.deferred(healthy.copy(thermalSupported=false,thermalStatus=null)))
    }
    @Test fun semanticNeverRelaxesLiteralBooleanOrUnknownFilters() {
        for(q in listOf("kind:pdf \"mirada estable\"","marcha NOT dolor","marcha OR silla","marcha AND equilibrio","(marcha)","unknown:secret equilibrio","kind:pdf"))assertNull(q,SemanticPolicy.query(q))
        val q=requireNotNull(SemanticPolicy.query("kind:pdf equilibrio durante la marcha"))
        assertEquals("equilibrio durante la marcha",q.text)
        assertTrue(q.filter.matches(SearchQuery.Document("a.pdf","document","application/pdf")))
        assertFalse(q.filter.matches(SearchQuery.Document("a.txt","document","text/plain")))
    }
    @Test fun corruptVectorsAreRefusedAndSerializationIsNormalized() {
        val v=FloatArray(512){if(it==0)2f else 0f};val normalized=requireNotNull(SemanticPolicy.vector(SemanticPolicy.bytes(v)))
        assertEquals(1f,normalized[0],0f);assertEquals(1f,SemanticPolicy.dot(normalized,normalized),1e-6f)
        assertNull(SemanticPolicy.vector(ByteArray(1)));assertNull(SemanticPolicy.vector(ByteArray(2048)))
        try{SemanticPolicy.bytes(FloatArray(512){Float.NaN});fail("Non-finite vector accepted")}catch(_:IllegalArgumentException){}
    }
    private fun fixture():WordPiece {
        val raw=javaClass.getResource("/semantic/wordpiece-vocab.json")!!.readText();val j=JSONObject(raw)
        val size=j.keys().asSequence().map{it.toInt()}.max()+1
        val vocabulary=MutableList(size){"<unused-fixture-$it>"};j.keys().forEach{k->vocabulary[k.toInt()]=j.getString(k)}
        return WordPiece(vocabulary)
    }
    @Test fun casedWordPieceMatchesRealHuggingFaceTokensForSpanishCorpus() {
        val tokenizer=fixture();val reference=JSONArray(javaClass.getResource("/semantic/tokenizer-reference.json")!!.readText())
        for(i in 0 until reference.length()) {
            val row=reference.getJSONObject(i);val ids=row.getJSONArray("ids")
            assertArrayEquals(row.getString("text"),LongArray(ids.length()){ids.getLong(it)},tokenizer.encode(row.getString("text")))
        }
    }
    @Test fun fragmentOffsetsStayInOriginalTextAndCoverageIsExplicitlyLimited() {
        val t=fixture();val text="Los ejercicios de equilibrio ayudan a reducir el vértigo. ".repeat(100)
        val fragments=t.fragments(text,2)
        assertEquals(2,fragments.items.size);assertTrue(fragments.truncated)
        for(f in fragments.items){assertEquals(text.substring(f.start,f.end),f.text);assertArrayEquals(t.encode(f.text),f.ids);assertTrue(f.ids.size<=128)}
        val short=t.fragments("ejercicios de equilibrio")
        assertFalse(short.truncated);assertEquals("ejercicios de equilibrio",short.items.single().text)
    }
    @Test fun controlsAndSurrogatesCannotProduceInvalidOffsets() {
        val t=fixture();val text="equilibrio\u0000\u0007 🧠[CLS] marcha"
        val tokens=t.tokenize(text)
        assertTrue(tokens.pieces.all{it.start>=0 && it.end<=text.length && it.start<it.end})
        assertTrue(tokens.pieces.any{ text.substring(it.start,it.end)=="[CLS]" })
    }
}
