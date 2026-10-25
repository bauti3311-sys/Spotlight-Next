package dev.bautista.spotlight.integrations.spotify

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class SpotifyPlaybackObservation(val playing: Boolean, val trackUri: String?, val contextUri: String?, val observedAt: Long)

/** Official Connect API. Successful commands are requests, never claims of verified playback. */
class SpotifyPlaybackClient internal constructor(client: OkHttpClient, private val base: HttpUrl) {
    constructor() : this(OkHttpClient(), "https://api.spotify.com/".toHttpUrl())
    private val http = SpotifyHttp(client)
    init { require(base.toString() == "https://api.spotify.com/" || base.scheme == "http" && base.host in setOf("127.0.0.1", "localhost", "::1")) }
    internal fun request(command: String, token: String, item: SpotifySearchItem? = null): Request {
        require(token.length in 1..4096 && token.matches(Regex("[A-Za-z0-9._~+/-]+=*"))) { "Autorización Spotify inválida." }
        val builder = Request.Builder().url(base.newBuilder().addPathSegments("v1/me/player").apply {
            if (command != "state") addPathSegment(when(command) { "play", "pause", "next", "previous" -> command; else -> error("Control no compatible.") })
        }.build()).header("Authorization", "Bearer $token")
        val json = if (item == null) "{}" else JSONObject().apply {
            if (item.type == SpotifyItemType.TRACK) put("uris", JSONArray().put(item.uri)) else put("context_uri", item.uri)
        }.toString()
        return when(command) {
            "state" -> builder.get()
            "play", "pause" -> builder.put(json.toRequestBody("application/json".toMediaType()))
            else -> builder.post(ByteArray(0).toRequestBody(null))
        }.build()
    }
    suspend fun command(command: String, token: String, item: SpotifySearchItem? = null): String {
        require(command != "state")
        http.read(request(command, token, item))
        return "Solicitud aceptada por Spotify; comprobá el estado del reproductor."
    }
    /** Baseline delegation used to reproduce the missing preflight in a controlled test. */
    suspend fun playSelection(token: String, item: SpotifySearchItem): String = command("play", token, item)

    suspend fun observe(token: String): SpotifyPlaybackObservation? {
        val raw = http.read(request("state", token))
        if (raw.isBlank()) return null
        val json = JSONObject(raw)
        return SpotifyPlaybackObservation(json.optBoolean("is_playing"), json.optJSONObject("item")?.optString("uri"),
            json.optJSONObject("context")?.optString("uri"), System.currentTimeMillis())
    }
}
