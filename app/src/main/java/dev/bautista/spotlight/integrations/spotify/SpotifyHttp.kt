package dev.bautista.spotlight.integrations.spotify

import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*

/** Credentials only travel to fixed official endpoints; fixture origins must be loopback. */
internal class SpotifyHttp(client: OkHttpClient = OkHttpClient()) {
    private val http = client.newBuilder().followRedirects(false).followSslRedirects(false)
        .retryOnConnectionFailure(false).callTimeout(15, TimeUnit.SECONDS).build()

    suspend fun read(request: Request, maximum: Int = 65536): String = suspendCancellableCoroutine { result ->
        val call = http.newCall(request)
        result.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (result.isActive) result.resumeWithException(IOException("No se pudo conectar con Spotify."))
            }
            override fun onResponse(call: Call, response: Response) {
                try {
                    val text = response.use { r ->
                        if (!r.isSuccessful) throw SpotifyApiException(r.code, r.header("Retry-After")?.toIntOrNull()?.coerceIn(0, 3600))
                        val body = r.body ?: throw IOException("Spotify no devolvió contenido.")
                        require(body.contentLength() <= maximum) { "Respuesta Spotify demasiado grande." }
                        body.source().let { source ->
                            require(!source.request(maximum.toLong() + 1)) { "Respuesta Spotify demasiado grande." }
                            source.readUtf8()
                        }
                    }
                    if (result.isActive) result.resume(text)
                } catch (e: Exception) {
                    if (result.isActive) result.resumeWithException(e)
                }
            }
        })
    }
}
