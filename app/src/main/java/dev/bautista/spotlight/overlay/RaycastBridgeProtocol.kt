package dev.bautista.spotlight.overlay

import org.json.JSONObject
import java.util.UUID

/** One bounded protocol for the local MessagePort. Never executes an Android effect. */
internal class RaycastBridgeProtocol(private val emit: (String) -> Unit) {
    data class Request(val id: String, val type: String, val payload: JSONObject, val sessionId: String)
    private data class Entry(val fingerprint: String, var response: String? = null)
    var sessionId: String = UUID.randomUUID().toString()
        private set
    private var highestId = 0L
    private val entries = linkedMapOf<String, Entry>()
    private var disposed = false

    fun reset() { sessionId=UUID.randomUUID().toString();highestId=0;entries.clear() }
    fun dispose() { disposed=true;entries.clear() }
    fun hello() = JSONObject().put("v",2).put("type","bridge.ready").put("sessionId",sessionId).toString()
    fun parse(raw: String): Request? {
        if(disposed)return null
        if(raw.length>8192 || raw.toByteArray(Charsets.UTF_8).size>8192) { reject("","INVALID_INPUT");return null }
        val json=runCatching { JSONObject(raw) }.getOrNull() ?: run { reject("","INVALID_INPUT");return null }
        val id=(json.opt("requestId") as? String).orEmpty()
        val number=id.takeIf { it.matches(Regex("[1-9][0-9]{0,9}")) }?.toLongOrNull()?.takeIf { it<=Int.MAX_VALUE }
        val safeId=if(number!=null)id else ""
        if(json.opt("v")!=2) { reject(safeId,"UNSUPPORTED");return null }
        if(json.opt("sessionId")!=sessionId) { reject(safeId,"UNAVAILABLE");return null }
        val type=json.opt("type") as? String
        val payload=json.opt("payload") as? JSONObject
        if(number==null || type==null || type.length>40 || payload==null ||
            json.keys().asSequence().toSet()!=setOf("v","sessionId","requestId","type","payload")) {
            reject(safeId,"INVALID_INPUT");return null
        }
        val fingerprint=type+":"+payload.toString()
        entries[id]?.let { entry ->
            if(entry.fingerprint!=fingerprint)reject(id,"INVALID_INPUT")
            else entry.response?.let(emit)
            return null
        }
        // No replay after cache eviction: monotonic IDs form a session-wide fence.
        if(number<=highestId) { reject(id,"STALE_RESULT");return null }
        highestId=number
        entries[id]=Entry(fingerprint)
        while(entries.size>128)entries.remove(entries.keys.first())
        return Request(id,type,payload,sessionId)
    }
    fun success(request: Request, data: JSONObject=JSONObject()) = complete(request,
        envelope(request.id,request.type).put("status","ok").put("data",data).toString())
    fun error(request: Request, code: String) = complete(request, failure(request.id,request.type,code))
    private fun complete(request: Request, message: String) {
        if(disposed || request.sessionId!=sessionId || !entries.containsKey(request.id))return
        // Never retain large icon/result packets in the deduplication cache.
        entries[request.id]?.response=if(request.type=="search.query")failure(request.id,request.type,"STALE_RESULT") else message
        emit(message)
    }
    private fun reject(id: String, code: String) { if(!disposed)emit(failure(id,"bridge.error",code)) }
    private fun envelope(id: String,type: String)=JSONObject().put("v",2).put("sessionId",sessionId)
        .put("requestId",id).put("type",type)
    private fun failure(id: String,type: String,code: String)=envelope(id,type).put("status","error")
        .put("error",JSONObject().put("code",code).put("message",when(code) {
            "INVALID_INPUT"->"Invalid request";"UNSUPPORTED"->"Operation or version not supported"
            "STALE_RESULT"->"Search again to refresh this result";"CANCELLED"->"Search cancelled"
            "TIMEOUT"->"Search timed out";"PERMISSION_REQUIRED"->"Android permission required"
            "UNAVAILABLE"->"Android connection or application unavailable";else->"Could not complete the request"
        })).toString()
}
