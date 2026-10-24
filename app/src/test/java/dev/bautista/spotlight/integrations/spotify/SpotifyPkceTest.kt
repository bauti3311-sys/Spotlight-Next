package dev.bautista.spotlight.integrations.spotify

import java.net.URLEncoder
import org.junit.Assert.*
import org.junit.Test

class SpotifyPkceTest {
    private fun pending()=SpotifyPkce.prepare("a".repeat(32),"dev.bautista.spotlight.next.raycastpreview.spotify://oauth",1000)
    private fun callback(p:PendingSpotifyAuthorization,state:String=p.state)="${p.redirect}?code=fixture-code&state=${URLEncoder.encode(state,"UTF-8")}"
    @Test fun s256MatchesRfc7636Vector() {
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",SpotifyPkce.challenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"))
    }
    @Test fun authorizationUsesRandomStateAndS256WithoutVerifierOrClientSecret() {
        val p=pending();val second=pending();val url=SpotifyPkce.authorizationUrl(p)
        assertNotEquals(p.state,second.state);assertNotEquals(p.verifier,second.verifier)
        assertTrue(url.contains("code_challenge_method=S256"));assertFalse(url.contains(p.verifier));assertFalse(url.contains("client_secret"))
        assertFalse(p.toString().contains(p.state));assertFalse(p.toString().contains(p.verifier))
    }
    @Test fun wrongStateRedirectDuplicatesAndExpiredResponseAreRejected() {
        for(build in listOf<(PendingSpotifyAuthorization)->String>(
            {callback(it,"wrong")},{callback(it).replace("://oauth","://evil")},{callback(it)+"&state=${it.state}"})) {
            val p=pending();assertTrue(runCatching{SpotifyPkce.exchangeRequest(p,build(p),2000)}.isFailure)
        }
        val p=pending();assertTrue(runCatching{SpotifyPkce.exchangeRequest(p,callback(p),601001)}.isFailure)
    }
    @Test fun validCallbackIsConsumedOnceAndCodeOnlyGoesInPostBody() {
        val p=pending();val request=SpotifyPkce.exchangeRequest(p,callback(p),2000)
        assertEquals("POST",request.method);assertEquals("accounts.spotify.com",request.url.host)
        assertNull(request.url.query);assertFalse(request.toString().contains("fixture-code"))
        assertTrue(runCatching{SpotifyPkce.exchangeRequest(p,callback(p),2001)}.isFailure)
    }
    @Test fun cancellationErrorConsumesAuthorizationWithoutRequest() {
        val p=pending();assertTrue(runCatching{SpotifyPkce.exchangeRequest(p,"${p.redirect}?error=access_denied&state=${p.state}",2000)}.isFailure)
        assertTrue(runCatching{SpotifyPkce.exchangeRequest(p,callback(p),2001)}.isFailure)
    }
    @Test fun insecureOrQueryBearingRedirectsAreRejected() {
        for(url in listOf("http://example.com/callback","https://example.com/callback?state=owned","https://user@example.com/callback","other.app://oauth"))
            assertTrue(runCatching{SpotifyPkce.prepare("a".repeat(32),url)}.isFailure)
    }
}
