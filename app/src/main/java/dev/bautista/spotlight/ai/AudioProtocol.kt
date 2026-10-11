package dev.bautista.spotlight.ai

import android.util.Base64
import dev.bautista.spotlight.data.Config
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.*

object AudioProtocol {
    fun request(c:Config,key:String,bytes:ByteArray,mime:String,name:String):Request {
        require(key.isNotBlank()){"Guardá una API key antes de transcribir."}
        require(bytes.size<=if(c.aiProvider=="gemini")12_000_000 else 20_000_000){"El audio supera el límite de este proveedor (Gemini: 12 MB; OpenAI: 20 MB)."}
        if(c.aiProvider=="gemini") {
            require(c.aiModel.matches(Regex("[A-Za-z0-9._-]{1,100}")))
            val body=JSONObject().put("contents",JSONArray().put(JSONObject().put("parts",JSONArray().put(JSONObject().put("text","Transcribí literalmente el audio. Devolvé solamente la transcripción.")).put(JSONObject().put("inlineData",JSONObject().put("mimeType",mime).put("data",Base64.encodeToString(bytes,Base64.NO_WRAP))))))).put("generationConfig",JSONObject().put("maxOutputTokens",8192))
            return Request.Builder().url("https://generativelanguage.googleapis.com/v1beta/models/${c.aiModel}:generateContent").header("x-goog-api-key",key).post(body.toString().toRequestBody("application/json".toMediaType())).build()
        }
        val body=MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("model","gpt-4o-mini-transcribe").addFormDataPart("response_format","json").addFormDataPart("file",name.replace(Regex("[\\r\\n\"]"),"_").take(120),bytes.toRequestBody(mime.toMediaType())).build()
        return Request.Builder().url("https://api.openai.com/v1/audio/transcriptions").header("Authorization","Bearer $key").post(body).build()
    }
    fun transcript(provider:String,raw:String):String {
        val text=if(provider=="gemini")AiProtocol.delta(provider,raw) else JSONObject(raw).optString("text")
        require(text.isNotBlank() && text.length<=100000){"La transcripción está vacía o es demasiado extensa."};return text
    }
}
