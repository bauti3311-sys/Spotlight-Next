package dev.bautista.spotlight.overlay

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=android.app.Application::class)
class RaycastBridgeProtocolTest {
    private val sent=mutableListOf<JSONObject>()
    private val bridge=RaycastBridgeProtocol { sent+=JSONObject(it) }
    private fun packet(id:String="1",type:String="action.execute",payload:JSONObject=JSONObject())=JSONObject()
        .put("v",2).put("sessionId",bridge.sessionId).put("requestId",id).put("type",type).put("payload",payload)
    private fun code()=sent.last().getJSONObject("error").getString("code")
    @Test fun validReplyIsCorrelatedAndDuplicateNeverDispatchesAgain() {
        val raw=packet().toString();val r=bridge.parse(raw)!!
        bridge.success(r,JSONObject().put("state","requested"))
        assertNull(bridge.parse(raw));assertEquals(2,sent.size)
        assertEquals(sent[0].toString(),sent[1].toString());assertEquals("1",sent[0].getString("requestId"))
        assertEquals(bridge.sessionId,sent[0].getString("sessionId"))
    }
    @Test fun inFlightRepeatAndConflictingReuseCannotDispatch() {
        val r=bridge.parse(packet().toString())!!
        assertNull(bridge.parse(packet().toString()));assertTrue(sent.isEmpty())
        assertNull(bridge.parse(packet(type="overlay.close").toString()));assertEquals("INVALID_INPUT",code())
        bridge.error(r,"UNAVAILABLE");assertEquals("UNAVAILABLE",code())
    }
    @Test fun replayFenceSurvivesBoundedCacheEviction() {
        for(i in 1..150) {val r=bridge.parse(packet(i.toString()).toString())!!;bridge.success(r)}
        assertNull(bridge.parse(packet().toString()));assertEquals("STALE_RESULT",code())
    }
    @Test fun reconnectRejectsOldPacketsAndOldCompletionEvenWhenIdIsReused() {
        val raw=packet().toString();val old=bridge.parse(raw)!!;bridge.reset()
        assertNull(bridge.parse(raw));assertEquals("UNAVAILABLE",code())
        val current=bridge.parse(packet().toString())!!;val count=sent.size
        bridge.success(old);assertEquals(count,sent.size)
        bridge.success(current);assertEquals(count+1,sent.size)
    }
    @Test fun malformedOversizedUnexpectedAndCoercedFieldsAreRejected() {
        listOf("not-json","x".repeat(8193),packet().put("extra",true).toString(),
            packet().put("requestId",1).toString(),packet("01").toString(),packet("2147483648").toString(),
            packet().put("v",2.5).toString(),packet().put("payload",true).toString(),
            packet().put("payload",JSONObject().put("text","😀".repeat(3000))).toString()).forEach {
            assertNull(it.take(80),bridge.parse(it))
        }
        assertTrue(sent.all { it.getString("status")=="error" })
    }
    @Test fun disposalSilencesRequestsAndLateCompletions() {
        val r=bridge.parse(packet().toString())!!;bridge.dispose()
        assertNull(bridge.parse(packet("2").toString()));bridge.success(r);assertTrue(sent.isEmpty())
    }
    @Test fun repeatedSearchDoesNotCacheLargeResultPacketsOrRestartWork() {
        val raw=packet(type="search.query").toString();val r=bridge.parse(raw)!!
        bridge.success(r,JSONObject().put("items",org.json.JSONArray()))
        assertNull(bridge.parse(raw));assertEquals("STALE_RESULT",code())
    }
}
