package dev.bautista.spotlight.integrations.spotify

import java.io.Closeable
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.Request

/** No LAN binding, forwarded port, service or persisted verifier. Exists only during user login. */
internal class SpotifyLoopback : Closeable {
    private val server = ServerSocket(0, 1, InetAddress.getByName("127.0.0.1")).apply { soTimeout = 1000 }
    val redirect = "http://127.0.0.1:${server.localPort}/spotify/callback"
    override fun close() = server.close()

    suspend fun awaitExchange(pending: PendingSpotifyAuthorization): Request = withContext(Dispatchers.IO) {
        val deadline = System.nanoTime() + 300_000_000_000L
        while (System.nanoTime() < deadline) {
            currentCoroutineContext().ensureActive()
            val socket = try { server.accept() } catch (_: SocketTimeoutException) { continue }
            socket.use { peer ->
                peer.soTimeout = 2000
                val request = try {
                    val bytes = java.io.ByteArrayOutputStream()
                    val input = peer.getInputStream()
                    while (bytes.size() < 8192) {
                        val next = input.read(); require(next >= 0)
                        bytes.write(next)
                        val b = bytes.toByteArray()
                        if (b.size >= 4 && b.takeLast(4) == listOf<Byte>(13, 10, 13, 10)) break
                    }
                    val raw = bytes.toString("US-ASCII"); require(raw.endsWith("\r\n\r\n"))
                    val lines = raw.split("\r\n")
                    val first = lines.first().split(' ')
                    require(first.size == 3 && first[0] == "GET" && first[2] == "HTTP/1.1")
                    require(first[1].startsWith("/spotify/callback?") && first[1].length <= 7000)
                    val hosts = lines.drop(1).filter { it.startsWith("Host:", ignoreCase = true) }
                    require(hosts.size == 1 && hosts.single().substringAfter(':').trim() == "127.0.0.1:${server.localPort}")
                    SpotifyPkce.exchangeRequest(pending, "http://127.0.0.1:${server.localPort}${first[1]}")
                } catch (e: Exception) {
                    if (pending.consumed) throw e
                    null
                }
                val text = if (request != null) "Respuesta recibida. Volvé a Spotlight para comprobar la conexión." else "Respuesta no válida."
                val body = text.toByteArray(Charsets.UTF_8)
                runCatching {
                    peer.getOutputStream().apply {
                        write(("HTTP/1.1 ${if(request!=null)"200 OK" else "400 Bad Request"}\r\nContent-Type: text/plain; charset=utf-8\r\n" +
                            "Cache-Control: no-store\r\nContent-Security-Policy: default-src 'none'\r\nConnection: close\r\nContent-Length: ${body.size}\r\n\r\n").toByteArray(Charsets.US_ASCII))
                        write(body); flush()
                    }
                }
                if (request != null) return@withContext request
            }
        }
        error("La autorización de Spotify venció. Podés volver a conectar.")
    }
}
