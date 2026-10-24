package dev.bautista.spotlight.integrations.spotify

import org.json.JSONObject

/** Deliberately not a data class: diagnostics must never print bearer credentials. */
class SpotifyTokens internal constructor(
    internal val accessToken: String,
    internal val refreshToken: String,
    internal val expiresAt: Long,
    internal val scopes: Set<String>,
) {
    override fun toString() = "SpotifyTokens(redacted)"
    internal fun encode(): String = JSONObject().put("access", accessToken).put("refresh", refreshToken)
        .put("expires", expiresAt).put("scope", scopes.sorted().joinToString(" ")).toString()

    companion object {
        private fun token(value: String): String {
            require(value.length in 1..4096 && value.matches(Regex("[A-Za-z0-9._~+/-]+=*"))) { "Respuesta de autorización inválida." }
            return value
        }
        internal fun response(raw: String, now: Long, previous: SpotifyTokens? = null): SpotifyTokens {
            require(raw.length <= 65536 && now >= 0)
            val json = JSONObject(raw)
            require(json.getString("token_type").equals("Bearer", ignoreCase = true))
            val seconds = json.getLong("expires_in")
            require(seconds in 1..86400 && now <= Long.MAX_VALUE - seconds * 1000)
            val refresh = if (json.has("refresh_token")) token(json.getString("refresh_token"))
                else previous?.refreshToken ?: error("Spotify no devolvió la autorización renovable.")
            val scopes = if (json.has("scope")) json.getString("scope").split(' ').filter { it.isNotBlank() }.toSet()
                else previous?.scopes.orEmpty()
            return SpotifyTokens(token(json.getString("access_token")), refresh, now + seconds * 1000, scopes)
        }
        internal fun restore(raw: String): SpotifyTokens {
            require(raw.length <= 16384)
            val json = JSONObject(raw)
            val expires = json.getLong("expires"); require(expires >= 0)
            return SpotifyTokens(token(json.getString("access")), token(json.getString("refresh")), expires,
                json.getString("scope").split(' ').filter { it.isNotBlank() }.toSet())
        }
    }
}

internal interface SpotifyTokenStore {
    fun read(): SpotifyTokens?
    fun save(tokens: SpotifyTokens)
    fun clear()
}
