package dev.bautista.spotlight.integrations.cast

import org.junit.Assert.*
import org.junit.Test

class LocalMediaRequestPolicyTest {
    private fun lease()=LocalMediaRequestPolicy("content://fixture/document/granted",1000,"video/mp4","192.168.1.50",1000,10000)
    @Test fun rangesSupportBoundedOpenAndSuffixForms() {
        assertEquals(ByteRange(0,99),ByteRange.parse("bytes=0-99",1000))
        assertEquals(ByteRange(900,999),ByteRange.parse("bytes=900-",1000))
        assertEquals(ByteRange(900,999),ByteRange.parse("bytes=-100",1000))
        assertEquals(ByteRange(0,999),ByteRange.parse("bytes=-2000",1000))
        assertEquals(ByteRange(999,999),ByteRange.parse("bytes=999-999999",1000))
    }
    @Test fun publicRangeLengthCannotOverflowToNegative() {
        assertTrue(runCatching{ByteRange(0,Long.MAX_VALUE).length}.isFailure)
    }
    @Test fun invalidMultipleAndOverflowRangesDoNotFallBackToFullContent() {
        for(header in listOf("bytes=0-9,20-30","bytes=1000-","bytes=-0","bytes=9-1","bytes=-","bytes=999999999999999999999999-","items=0-1"))
            assertTrue(header,runCatching{ByteRange.parse(header,1000)}.isFailure)
    }
    @Test fun rangeGrantIncludesOnlyRequestedBytesAndSafeHeaders() {
        val l=lease();val grant=l.authorize("GET",l.path,"192.168.1.50","bytes=100-199",2000)
        assertEquals(206,grant.status);assertEquals(100L,grant.offset);assertEquals(100L,grant.length)
        assertEquals("bytes 100-199/1000",grant.headers["Content-Range"]);assertEquals("no-store",grant.headers["Cache-Control"])
        assertTrue(grant.includeBody)
    }
    @Test fun headDoesNotOpenOrReturnBody() {
        val l=lease();val grant=l.authorize("HEAD",l.path,"192.168.1.50",null,2000)
        assertEquals(200,grant.status);assertFalse(grant.includeBody);assertEquals(1000L,grant.length)
    }
    @Test fun peerPathTraversalMethodExpirationAndCloseAreEnforced() {
        val l=lease()
        for((method,path,peer) in listOf(Triple("POST",l.path,"192.168.1.50"),Triple("GET","/media/../document/granted","192.168.1.50"),Triple("GET",l.path,"192.168.1.51")))
            assertTrue(runCatching{l.authorize(method,path,peer,null,2000)}.isFailure)
        assertTrue(runCatching{l.authorize("GET",l.path,"192.168.1.50",null,11000)}.isFailure)
        l.close();assertTrue(runCatching{l.authorize("GET",l.path,"192.168.1.50",null,2000)}.isFailure)
    }
    @Test fun leasesAreUniqueAndDoNotExposeGrantInLogs() {
        val a=lease();val b=lease();assertNotEquals(a.path,b.path)
        assertFalse(a.toString().contains(a.path));assertFalse(a.toString().contains(a.contentUri))
    }
    @Test fun malformedPrivateUrlDoesNotEnterErrorMessages() {
        val secretUrl="https://media.example/bad%private-secret"
        val error=runCatching{CastMediaPolicy.directUrl(secretUrl,"video/mp4",true)}.exceptionOrNull()
        assertNotNull(error);assertFalse(error.toString().contains(secretUrl))
    }
    @Test fun onlyExplicitlyAuthorizedUnprotectedDirectMediaIsEligible() {
        assertEquals("https://media.example/video.mp4",CastMediaPolicy.directUrl("https://media.example/video.mp4","video/mp4",true).toString())
        for(url in listOf("http://media.example/video.mp4","https://user:pass@media.example/video.mp4","https://media.example/video.mp4?token=private","file:///private/video.mp4"))
            assertTrue(runCatching{CastMediaPolicy.directUrl(url,"video/mp4",true)}.isFailure)
        assertTrue(runCatching{CastMediaPolicy.directUrl("https://media.example/video.mp4","video/mp4",true,true)}.isFailure)
        assertTrue(runCatching{CastMediaPolicy.directUrl("https://media.example/video.mp4","application/pdf",true)}.isFailure)
        assertTrue(runCatching{CastMediaPolicy.directUrl("https://media.example/video.mp4","video/mp4",false)}.isFailure)
    }
}
