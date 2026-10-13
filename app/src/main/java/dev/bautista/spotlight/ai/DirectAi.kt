package dev.bautista.spotlight.ai

import android.content.Context
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.*
import java.io.IOException
import java.util.concurrent.TimeUnit

object AiProtocol {
    fun defaultModel(provider:String)=if(provider=="gemini")"gemini-3.5-flash" else "gpt-4.1-mini"
    fun request(c:Config,key:String,messages:List<Pair<String,String>>):Request {
        require(key.isNotBlank() && !key.contains('\n') && !key.contains('\r')) {"Guardá tu API key en Ajustes → Inteligencia artificial."}
        require(c.aiModel.matches(Regex("[A-Za-z0-9._-]{1,100}"))) {"Modelo inválido"}
        val history=messages.takeLast(12).map{(role,text)->(if(role=="assistant")role else "user") to text.take(24000)}
        val body=if(c.aiProvider=="gemini")JSONObject().put("contents",JSONArray().apply {history.forEach{(role,text)->put(JSONObject().put("role",if(role=="assistant")"model" else "user").put("parts",JSONArray().put(JSONObject().put("text",text))))}}).put("generationConfig",JSONObject().put("maxOutputTokens",4096))
            else JSONObject().put("model",c.aiModel).put("stream",true).put("store",false).put("max_output_tokens",4096).put("input",JSONArray().apply{history.forEach{(role,text)->put(JSONObject().put("role",role).put("content",text))}})
        val url=if(c.aiProvider=="gemini")"https://generativelanguage.googleapis.com/v1beta/models/${c.aiModel}:streamGenerateContent?alt=sse" else "https://api.openai.com/v1/responses"
        return Request.Builder().url(url).header(if(c.aiProvider=="gemini")"x-goog-api-key" else "Authorization",if(c.aiProvider=="gemini")key else "Bearer $key").post(body.toString().toRequestBody("application/json".toMediaType())).build()
    }
    fun delta(provider:String,data:String):String {
        if(data=="[DONE]")return ""
        val j=try{JSONObject(data)}catch(e:Exception){error("Respuesta de IA inválida")}
        if(j.has("error") || j.optString("type") in setOf("response.failed","response.incomplete","error"))error("El proveedor no pudo completar la respuesta.")
        if(provider!="gemini")return if(j.optString("type")=="response.output_text.delta")j.optString("delta") else ""
        if(j.optJSONObject("promptFeedback")?.has("blockReason")==true)error("El proveedor bloqueó la consulta.")
        val candidates=j.optJSONArray("candidates") ?: return ""
        return buildString {for(i in 0 until candidates.length()){val candidate=candidates.getJSONObject(i);if(candidate.optString("finishReason") in setOf("MAX_TOKENS","SAFETY","RECITATION"))error("La respuesta quedó incompleta o bloqueada.");val parts=candidate.optJSONObject("content")?.optJSONArray("parts") ?: continue;for(k in 0 until parts.length()){val part=parts.getJSONObject(k);if(!part.optBoolean("thought"))append(part.optString("text"))}}}
    }
    fun error(code:Int)=when(code){401,403->"Clave no autorizada. Revisá tu API key y permisos.";404->"Modelo no disponible. Elegí otro en Ajustes.";429->"Límite de uso alcanzado. Revisá la cuota del proveedor.";else->"No se pudo consultar IA (HTTP $code)."}
}

class DirectAi(private val credentials:AiCredentials,private val client:OkHttpClient=OkHttpClient.Builder().connectTimeout(15,TimeUnit.SECONDS).readTimeout(90,TimeUnit.SECONDS).callTimeout(180,TimeUnit.SECONDS).followRedirects(false).followSslRedirects(false).build()) {
    constructor(context:Context):this(CredentialVault(context))
    fun configured(c:Config)=if(c.aiProvider=="backend")c.endpoint.isNotBlank() else credentials.contains(c.aiProvider)
    fun answer(c:Config,messages:List<Pair<String,String>>):Flow<String> = flow {
        if(c.aiProvider=="backend"){emitAll(AiClient().answer(c.endpoint,messages));return@flow}
        val request=withContext(Dispatchers.IO){ val key=runCatching{credentials.read(c.aiProvider)}.getOrElse{error("No se pudo leer la clave. Guardala nuevamente.")}.orEmpty();AiProtocol.request(c,key,messages) }
        emitAll(callbackFlow {
            val call=client.newCall(request)
            call.enqueue(object:Callback {
                override fun onFailure(call:Call,e:IOException){close(IOException(if(call.isCanceled() && e !is java.io.InterruptedIOException)"Consulta cancelada" else "No se pudo conectar al proveedor de IA."))}
                override fun onResponse(call:Call,response:Response){response.use {r->try{
                    if(!r.isSuccessful)error(AiProtocol.error(r.code))
                    val source=r.body?.source() ?: error("Respuesta vacía");val event=StringBuilder();var received=false
                    fun flush(){if(event.isNotEmpty()){val text=AiProtocol.delta(c.aiProvider,event.toString());event.clear();if(text.isNotEmpty()){received=true;trySend(text).getOrThrow()}}}
                    while(!source.exhausted() && !call.isCanceled()){val line=source.readUtf8Line() ?: break;if(line.isBlank())flush() else if(line.startsWith("data:")){if(event.isNotEmpty())event.append('\n');event.append(line.substringAfter(':').trimStart())}}
                    flush();if(!received)error("El proveedor devolvió una respuesta vacía.");close()
                }catch(e:Exception){close(e)}}}
            });awaitClose{call.cancel()}
        }.buffer(128))
    }
}
