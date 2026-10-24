package dev.bautista.spotlight.integrations.spotify

import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import java.net.URI
import java.net.URLDecoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/** Ephemeral verifier/state are never encoded into configuration, logs or source backups. */
class PendingSpotifyAuthorization internal constructor(
    internal val clientId:String,internal val redirect:String,internal val verifier:String,
    internal val state:String,internal val createdAt:Long,internal val scopes:Set<String>,
) {
    internal var consumed=false
    override fun toString()="PendingSpotifyAuthorization(redacted)"
}
object SpotifyPkce {
    private fun random()=ByteArray(32).also{SecureRandom().nextBytes(it)}.let{Base64.getUrlEncoder().withoutPadding().encodeToString(it)}
    fun challenge(verifier:String):String {
        require(verifier.matches(Regex("[A-Za-z0-9._~-]{43,128}")))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII)))
    }
    fun prepare(clientId:String,redirect:String,now:Long=System.currentTimeMillis(),scopes:Set<String> = emptySet()):PendingSpotifyAuthorization {
        require(clientId.matches(Regex("[A-Za-z0-9]{32}"))){"Client ID inválido."}
        val uri=URI(redirect)
        require(uri.rawQuery==null && uri.rawFragment==null && uri.rawUserInfo==null)
        require(uri.scheme=="https" && !uri.host.isNullOrBlank() ||
            uri.scheme=="http" && uri.host=="127.0.0.1" && uri.port in 1..65535 && uri.rawPath=="/spotify/callback"){
            "Registrá un redirect HTTPS o el retorno local exacto de Spotify."}
        require(redirect.length<=1000 && now>=0)
        require(scopes.all{it in setOf("user-read-playback-state","user-modify-playback-state")})
        return PendingSpotifyAuthorization(clientId,redirect,random(),random(),now,scopes.toSet())
    }
    fun authorizationUrl(pending:PendingSpotifyAuthorization):String {
        check(!pending.consumed)
        return "https://accounts.spotify.com/authorize".toHttpUrl().newBuilder()
            .addQueryParameter("client_id",pending.clientId).addQueryParameter("response_type","code")
            .addQueryParameter("redirect_uri",pending.redirect).addQueryParameter("state",pending.state)
            .addQueryParameter("code_challenge_method","S256").addQueryParameter("code_challenge",challenge(pending.verifier))
            .apply{if(pending.scopes.isNotEmpty())addQueryParameter("scope",pending.scopes.sorted().joinToString(" "))}.build().toString()
    }
    /** One valid response consumes this transaction. No client secret and no token in a URL. */
    fun exchangeRequest(pending:PendingSpotifyAuthorization,callback:String,now:Long=System.currentTimeMillis()):Request = synchronized(pending) {
        check(!pending.consumed){"La autorización ya fue utilizada."}
        require(now>=pending.createdAt && now-pending.createdAt<=600000){"La autorización venció."}
        require(callback.length<=8000)
        val uri=URI(callback);val expected=URI(pending.redirect)
        require(uri.scheme==expected.scheme && uri.rawAuthority==expected.rawAuthority && uri.rawPath==expected.rawPath && uri.rawFragment==null){"Redirect no reconocido."}
        val pairs=uri.rawQuery.orEmpty().split('&').filter{it.isNotBlank()}.map { pair ->
            val parts=pair.split('=',limit=2);require(parts.size==2)
            URLDecoder.decode(parts[0],"UTF-8") to URLDecoder.decode(parts[1],"UTF-8")
        }
        require(pairs.size<=5 && pairs.map{it.first}.distinct().size==pairs.size){"Respuesta de autorización ambigua."}
        val values=pairs.toMap()
        require(MessageDigest.isEqual(values["state"].orEmpty().toByteArray(),pending.state.toByteArray())){"Estado de autorización incorrecto."}
        pending.consumed=true
        require("error" !in values){"Spotify no autorizó el acceso."}
        val code=values["code"] ?: error("Falta el código de autorización.")
        require(code.length in 1..4096 && '\u0000' !in code)
        Request.Builder().url("https://accounts.spotify.com/api/token").post(FormBody.Builder()
            .add("grant_type","authorization_code").add("client_id",pending.clientId)
            .add("code",code).add("redirect_uri",pending.redirect).add("code_verifier",pending.verifier).build()).build()
    }
}
