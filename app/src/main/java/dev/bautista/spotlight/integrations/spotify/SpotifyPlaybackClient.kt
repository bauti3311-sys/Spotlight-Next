package dev.bautista.spotlight.integrations.spotify

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.IOException
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class SpotifyPlaybackObservation(
    val playing: Boolean,
    val trackUri: String?,
    val contextUri: String?,
    val observedAt: Long,
    val trackName: String? = null,
    val artistName: String? = null,
) {
    fun matches(item: SpotifySearchItem): Boolean =
        trackUri == item.uri || contextUri == item.uri

    fun diagnose(item: SpotifySearchItem, source: String = "el cliente local"): String = when {
        playing && matches(item) ->
            "Spotify confirmó reproducción del contenido seleccionado."
        !playing && matches(item) ->
            "Spotify cargó el contenido seleccionado, pero el reproductor está en pausa."
        playing && item.type != SpotifyItemType.TRACK && contextUri.isNullOrBlank() && !trackUri.isNullOrBlank() ->
            "Spotify está reproduciendo en este momento, pero $source no informó el contexto para verificar si corresponde a la selección."
        !playing && item.type != SpotifyItemType.TRACK && contextUri.isNullOrBlank() && !trackUri.isNullOrBlank() ->
            "Spotify está en pausa y $source no informó el contexto para verificar si corresponde a la selección."
        playing && (!trackUri.isNullOrBlank() || !contextUri.isNullOrBlank()) ->
            "Spotify sigue reproduciendo otro contenido; aún no confirmó el cambio a la selección."
        else ->
            "Spotify no confirmó la reproducción del contenido seleccionado."
    }
}

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

    /** Check the current account's live target before replacing its selected content.
     * No transfer, pause, fallback, retry or persistence of device identifiers. */
    suspend fun playSelection(token: String, item: SpotifySearchItem): String {
        val raw = http.read(request("state", token))
        val deviceId = try {
            val device = if (raw.isBlank()) null else JSONObject(raw).optJSONObject("device")
            val id = device?.opt("id") as? String
            if (device?.opt("is_active") != true || device?.opt("is_restricted") != false ||
                id.isNullOrBlank() || id.length > 256 || id.any { it.isISOControl() }) null else id
        } catch (_: Exception) { null }
        if (deviceId == null) throw IOException(
            "Spotify no informó un dispositivo activo y controlable para esta cuenta. " +
                "No se solicitó cambiar la música. Revisá el reproductor y la cuenta de Spotify."
        )
        currentCoroutineContext().ensureActive()
        val approved = request("play", token, item)
        val targeted = approved.newBuilder().url(approved.url.newBuilder()
            .addQueryParameter("device_id", deviceId).build()).build()
        http.read(targeted)
        return "Solicitud aceptada por Spotify; comprobá el estado del reproductor."
    }

    suspend fun observe(token: String): SpotifyPlaybackObservation? {
        val raw = http.read(request("state", token))
        if (raw.isBlank()) return null
        val json = JSONObject(raw)
        return SpotifyPlaybackObservation(json.optBoolean("is_playing"), json.optJSONObject("item")?.optString("uri"),
            json.optJSONObject("context")?.optString("uri"), System.currentTimeMillis())
    }
}
