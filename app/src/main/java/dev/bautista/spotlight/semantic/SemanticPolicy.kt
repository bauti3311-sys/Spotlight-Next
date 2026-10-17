package dev.bautista.spotlight.semantic

import dev.bautista.spotlight.control.DeviceResourceState
import dev.bautista.spotlight.search.SearchQuery
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import kotlin.math.sqrt

object SemanticPolicy {
    const val MODEL="distiluse-v2-arm64-int8-bfe45d0-v1"
    const val DIMENSIONS=512
    const val MAX_DOCUMENTS=200
    const val MAX_FRAGMENTS=1000
    const val MAX_PER_FILE=8
    const val MIN_SCORE=.28f
    fun deferred(s:DeviceResourceState):String?=when {
        s.lowMemory==true || (s.availableMemory ?: Long.MAX_VALUE)<maxOf(768L*1024*1024,(s.memoryThreshold ?: 0)*2)->"memoria disponible insuficiente"
        s.thermalSupported && s.thermalStatus in 2..6->"temperatura del dispositivo"
        s.powerSave==true->"ahorro de energía"
        s.charging!=true && (s.batteryPercent ?: 100)<=20->"batería baja"
        s.availableMemory==null || s.memoryThreshold==null || s.lowMemory==null || s.powerSave==null || s.batteryPercent==null || s.charging==null || s.thermalSupported && s.thermalStatus==null->"estado de recursos desconocido"
        else->null
    }
    data class Query(val text:String,val filter:SearchQuery)
    fun query(input:String):Query? {
        if(input.any{it=='"' || it=='(' || it==')'} || Regex("""(?:^|\s)(?:AND|OR|NOT)(?:\s|$)""").containsMatchIn(input))return null
        val natural=SearchQuery.natural(input)
        if(natural.length !in 4..512 || Regex("[\"()]|(?:^|\\s)(?:AND|OR|NOT)(?:\\s|$)").containsMatchIn(natural))return null
        val tokens=natural.split(Regex("\\s+")).filter{it.isNotBlank()}
        val filters=tokens.filter{':' in it}
        val keys=setOf("kind","tipo","author","autor","name","nombre","modified","modificado","date","fecha","created","creado","after","despues","before","antes","modified-after","modified-before","created-after","created-before")
        if(filters.any{it.substringBefore(':').lowercase() !in keys})return null
        val text=tokens.filterNot{':' in it}.joinToString(" ")
        if(text.length<4 || text.none{it.isLetter()})return null
        val filter=SearchQuery.parse(filters.joinToString(" "))
        return if(filter.problem==null)Query(text,filter) else null
    }
    fun hash(text:String)=MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8)).joinToString(""){"%02x".format(it)}
    fun normalized(vector:FloatArray):FloatArray {
        require(vector.size==DIMENSIONS && vector.all{it.isFinite()})
        val norm=sqrt(vector.sumOf{it.toDouble()*it})
        require(norm.isFinite() && norm>1e-12)
        return FloatArray(vector.size){(vector[it]/norm).toFloat()}
    }
    fun bytes(vector:FloatArray)=ByteBuffer.allocate(DIMENSIONS*4).order(ByteOrder.LITTLE_ENDIAN).apply{normalized(vector).forEach{putFloat(it)}}.array()
    fun vector(bytes:ByteArray):FloatArray? {
        if(bytes.size!=DIMENSIONS*4)return null
        val b=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        return runCatching{normalized(FloatArray(DIMENSIONS){b.float})}.getOrNull()
    }
    fun dot(a:FloatArray,b:FloatArray):Float {require(a.size==DIMENSIONS && b.size==DIMENSIONS);return a.indices.sumOf{a[it].toDouble()*b[it]}.toFloat().coerceIn(-1f,1f)}
}
