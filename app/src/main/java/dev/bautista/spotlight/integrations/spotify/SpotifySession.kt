package dev.bautista.spotlight.integrations.spotify

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request

/** Serializes renewal and disconnect. No automatic replay of external side effects. */
internal class SpotifySession(
    private val store: SpotifyTokenStore,
    private val http: SpotifyHttp = SpotifyHttp(),
    private val tokenUrl: HttpUrl = "https://accounts.spotify.com/api/token".toHttpUrl(),
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val mutex = Mutex()
    init {
        require(tokenUrl.toString() == "https://accounts.spotify.com/api/token" ||
            tokenUrl.scheme == "http" && tokenUrl.host in setOf("127.0.0.1", "localhost", "::1"))
    }
    suspend fun authorized() = mutex.withLock { store.read() != null }
    suspend fun install(tokens: SpotifyTokens) = mutex.withLock { store.save(tokens) }
    suspend fun disconnect() = mutex.withLock { store.clear() }
    suspend fun token(clientId: String): String = mutex.withLock {
        require(clientId.matches(Regex("[A-Za-z0-9]{32}")))
        val saved = try { store.read() } catch (_: Exception) { error("No se pudo leer la autorización protegida. Volvé a conectar Spotify.") }
            ?: error("Conectá tu cuenta de Spotify en Ajustes.")
        if (saved.expiresAt - clock() > 60000) return@withLock saved.accessToken
        val request = Request.Builder().url(tokenUrl).post(FormBody.Builder()
            .add("grant_type", "refresh_token").add("client_id", clientId)
            .add("refresh_token", saved.refreshToken).build()).build()
        val renewed = try { SpotifyTokens.response(http.read(request), clock(), saved) }
            catch (e: SpotifyApiException) { if (e.status in setOf(400, 401)) store.clear(); throw e }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Exception) { error("Spotify devolvió una autorización inválida; no se reemplazó la sesión guardada.") }
        store.save(renewed)
        renewed.accessToken
    }
}
