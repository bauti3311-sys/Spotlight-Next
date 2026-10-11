package dev.bautista.spotlight.ai

import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.*

interface AiProvider {
    fun answer(endpoint: String, messages: List<Pair<String, String>>): Flow<String>
}

/** HTTPS intermediary contract. No API key, contact or local search data is sent by the client. */
class AiClient : AiProvider {
    private val client =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build()

    override fun answer(endpoint: String, messages: List<Pair<String, String>>): Flow<String> =
        callbackFlow {
                val url = endpoint.toHttpUrlOrNull()
                require(
                    url != null && url.isHttps && url.username.isEmpty() && url.password.isEmpty()
                ) {
                    "Configurá un backend HTTPS válido en Ajustes."
                }
                val body =
                    JSONObject()
                        .put(
                            "messages",
                            JSONArray().apply {
                                messages.takeLast(12).forEach { (role, text) ->
                                    put(JSONObject().put("role", role).put("content", text))
                                }
                            },
                        )
                val call =
                    client.newCall(
                        Request.Builder()
                            .url(url)
                            .post(body.toString().toRequestBody("application/json".toMediaType()))
                            .build()
                    )
                call.enqueue(
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            close(e)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            response.use { r ->
                                try {
                                    if (!r.isSuccessful)
                                        error(
                                            "El backend respondió ${r.code}. Revisá su configuración."
                                        )
                                    val source = r.body?.source() ?: error("Respuesta vacía")
                                    while (!source.exhausted()) {
                                        val line = source.readUtf8Line() ?: break
                                        if (!line.startsWith("data:")) continue
                                        val data = line.removePrefix("data:").trim()
                                        if (data == "[DONE]") break
                                        val json = JSONObject(data)
                                        if (json.has("error"))
                                            error("El proveedor de IA informó un error")
                                        val delta = json.optString("delta")
                                        if (delta.isNotEmpty()) trySend(delta).getOrThrow()
                                    }
                                    close()
                                } catch (e: Exception) {
                                    close(e)
                                }
                            }
                        }
                    }
                )
                awaitClose { call.cancel() }
            }
            .buffer(128)
}
