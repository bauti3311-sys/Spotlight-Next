package dev.bautista.spotlight.ai

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/** Only invoked after the user confirms sending the chosen audio to their private backend. */
class AudioClient {
    private val client = OkHttpClient.Builder().callTimeout(180, TimeUnit.SECONDS).followRedirects(false).followSslRedirects(false).build()

    suspend fun transcribe(context:Context,uri:Uri,c:dev.bautista.spotlight.data.Config,name:String):String {
        if(c.aiProvider=="backend")return transcribe(context,uri,c.endpoint,name)
        val request=withContext(Dispatchers.IO){
            val key=CredentialVault(context).read(c.aiProvider).orEmpty()
            val bytes=context.contentResolver.openInputStream(uri)?.use{input->val out=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192);while(true){ensureActive();val n=input.read(buffer);if(n<0)break;require(out.size()+n<=20_000_000){"El audio supera 20 MB"};out.write(buffer,0,n)};out.toByteArray()} ?: error("No se pudo abrir el audio")
            AudioProtocol.request(c,key,bytes,context.contentResolver.getType(uri) ?: "audio/mpeg",name)
        }
        return suspendCancellableCoroutine {cont->
            val call=client.newCall(request);cont.invokeOnCancellation{call.cancel()}
            call.enqueue(object:Callback {
                override fun onFailure(call:Call,e:java.io.IOException){if(cont.isActive)cont.resumeWithException(java.io.IOException("No se pudo conectar al proveedor de transcripción."))}
                override fun onResponse(call:Call,response:Response){response.use{r->try{if(!r.isSuccessful)error(AiProtocol.error(r.code));val raw=r.body?.string() ?: error("Respuesta vacía");require(raw.length<150000);val text=AudioProtocol.transcript(c.aiProvider,raw);if(cont.isActive)cont.resume(text)}catch(e:Exception){if(cont.isActive)cont.resumeWithException(e)}}}
            })
        }
    }

    suspend fun transcribe(context: Context, uri: Uri, endpoint: String, name: String): String {
        val url = endpoint.toHttpUrlOrNull() ?: error("Configurá un backend HTTPS")
        require(url.isHttps && url.username.isEmpty() && url.password.isEmpty())
        val bytes =
            withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val out = java.io.ByteArrayOutputStream()
                    val buf = ByteArray(8192)
                    while (true) {
                        ensureActive()
                        val n = input.read(buf)
                        if (n < 0) break
                        require(out.size() + n <= 20_000_000) { "El audio supera 20 MB" }
                        out.write(buf, 0, n)
                    }
                    out.toByteArray()
                } ?: error("No se pudo abrir el audio")
            }
        val body =
            JSONObject()
                .put("name", name)
                .put("audio", Base64.encodeToString(bytes, Base64.NO_WRAP))
                .toString()
        val target = url.newBuilder().encodedPath("/transcribe").query(null).build()
        return suspendCancellableCoroutine { c ->
            val call =
                client.newCall(
                    Request.Builder()
                        .url(target)
                        .post(body.toRequestBody("application/json".toMediaType()))
                        .build()
                )
            c.invokeOnCancellation { call.cancel() }
            call.enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        if (c.isActive) c.resumeWithException(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use { r ->
                            try {
                                require(r.isSuccessful) {
                                    "Transcripción respondió ${r.code}; revisá el backend"
                                }
                                val raw = r.body?.string() ?: error("Respuesta vacía")
                                require(raw.length <= 150000)
                                val text = JSONObject(raw).getString("text")
                                if (c.isActive) c.resume(text)
                            } catch (e: Exception) {
                                if (c.isActive) c.resumeWithException(e)
                            }
                        }
                    }
                }
            )
        }
    }
}
