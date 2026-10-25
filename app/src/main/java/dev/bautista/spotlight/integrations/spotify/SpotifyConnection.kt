package dev.bautista.spotlight.integrations.spotify

import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.bautista.spotlight.ai.CredentialVault
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex

enum class SpotifyConnectionState { DISCONNECTED, CONNECTING, AUTHORIZED, ERROR }

/** Only called by explicit UI actions. No on-start connection, refresh, polling or analytics. */
class SpotifyConnection(context: Context) {
    private val app = context.applicationContext
    private val settings = app.getSharedPreferences("spotify-public", Context.MODE_PRIVATE)
    private val vault = CredentialVault(app)
    private val http = SpotifyHttp()
    private val session = SpotifySession(object : SpotifyTokenStore {
        override fun read() = vault.read("spotify")?.let(SpotifyTokens::restore)
        override fun save(tokens: SpotifyTokens) = vault.save("spotify", tokens.encode())
        override fun clear() = vault.delete("spotify")
    })
    private val localPlayer=SpotifyLocalPlayer(OfficialSpotifyRemote(app))
    suspend fun authorizeLocalPlayback() = localPlayer.authorize(clientId)
    private val connecting = Mutex()
    @Volatile private var authorizationJob: Job? = null
    private val mutableState = MutableStateFlow(SpotifyConnectionState.DISCONNECTED)
    val state = mutableState.asStateFlow()
    val clientId: String get() = settings.getString("client-id", "").orEmpty()

    suspend fun inspect() = withContext(Dispatchers.IO) {
        if (mutableState.value != SpotifyConnectionState.CONNECTING)
            mutableState.value = if (runCatching { session.authorized() }.getOrDefault(false))
                SpotifyConnectionState.AUTHORIZED else SpotifyConnectionState.DISCONNECTED
    }
    suspend fun configure(id: String) = withContext(Dispatchers.IO) {
        val clean = id.trim()
        require(clean.matches(Regex("[A-Za-z0-9]{32}"))) { "El Client ID debe tener 32 caracteres." }
        disconnect()
        check(settings.edit().putString("client-id", clean).commit()) { "No se pudo guardar el Client ID." }
    }
    suspend fun connect() = withContext(Dispatchers.IO) {
        check(connecting.tryLock()) { "Ya hay una autorización de Spotify en curso." }
        val job = currentCoroutineContext().job
        authorizationJob = job
        try {
            val id = clientId
            require(id.matches(Regex("[A-Za-z0-9]{32}"))) { "Guardá primero tu Client ID de Spotify." }
            mutableState.value = SpotifyConnectionState.CONNECTING
            SpotifyLoopback().use { listener ->
                val pending = SpotifyPkce.prepare(id, listener.redirect, scopes = setOf("user-read-playback-state", "user-modify-playback-state"))
                withContext(Dispatchers.Main) {
                    app.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SpotifyPkce.authorizationUrl(pending)))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                val request = listener.awaitExchange(pending)
                val tokens = SpotifyTokens.response(http.read(request), System.currentTimeMillis())
                currentCoroutineContext().ensureActive()
                session.install(tokens)
                mutableState.value = SpotifyConnectionState.AUTHORIZED
            }
        } catch (e: CancellationException) {
            mutableState.value = SpotifyConnectionState.DISCONNECTED
            throw e
        } catch (_: Exception) {
            mutableState.value = SpotifyConnectionState.ERROR
            error("No se pudo autorizar Spotify. Revisá Client ID, redirect y acceso de la cuenta; podés volver a conectar.")
        } finally {
            if (authorizationJob == job) authorizationJob = null
            connecting.unlock()
        }
    }
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        authorizationJob?.takeIf { it != currentCoroutineContext().job }?.cancelAndJoin()
        localPlayer.disconnect()
        session.disconnect()
        mutableState.value = SpotifyConnectionState.DISCONNECTED
    }
    suspend fun search(query: String, types: Set<SpotifyItemType> = SpotifyItemType.entries.toSet()): SpotifySearchPage =
        withContext(Dispatchers.IO) {
            try { SpotifySearchClient().search(query, types, session.token(clientId)) }
            catch (e: SpotifyApiException) {
                if (e.status == 401) { session.disconnect(); mutableState.value = SpotifyConnectionState.DISCONNECTED }
                throw e
            }
        }
    suspend fun playback(command: String, item: SpotifySearchItem? = null): String = withContext(Dispatchers.IO) {
        val player = SpotifyPlaybackClient()
        if (command == "play" && item != null) localPlayer.play(clientId, item)
        else player.command(command, session.token(clientId), item)
    }
    suspend fun observe(): SpotifyPlaybackObservation? = withContext(Dispatchers.IO) {
        localPlayer.observe(clientId)
    }
}
