package dev.bautista.spotlight.data

import org.json.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotMutableState

/** Typed Compose saved values only. Never Parcel, Java deserialization, Intent, grant or bitmap. */
internal object OverlaySnapshotCodec {
    const val MAX_BYTES=384*1024
    fun encode(state:Map<String,List<Any?>>):JSONObject {
        var nodes=0
        fun value(v:Any?,depth:Int):Any {
            require(depth<=24 && ++nodes<=24000){"La sesión supera el límite de recuperación."}
            fun tagged(type:String,data:Any)=JSONObject().put("t",type).put("v",data)
            return when(v){
                null -> JSONObject.NULL
                is MutableIntState -> tagged("si",v.intValue)
                is MutableLongState -> tagged("sl",v.longValue.toString())
                is MutableFloatState -> {require(v.floatValue.isFinite());tagged("sf",v.floatValue.toDouble())}
                is MutableDoubleState -> {require(v.doubleValue.isFinite());tagged("sd",v.doubleValue)}
                is SnapshotMutableState<*> -> {
                    val policy=when(v.policy){structuralEqualityPolicy<Any?>()->"s";referentialEqualityPolicy<Any?>()->"r";neverEqualPolicy<Any?>()->"n";else->error("Política de estado no compatible.")}
                    tagged("state",JSONObject().put("policy",policy).put("value",value(v.value,depth+1)))
                }
                is String -> {require(v.length<=96000);tagged("s",v)}
                is Boolean -> tagged("b",v)
                is Int -> tagged("i",v)
                is Long -> tagged("l",v.toString())
                is Float -> {require(v.isFinite());tagged("f",v.toDouble())}
                is Double -> {require(v.isFinite());tagged("d",v)}
                is List<*> -> tagged("a",JSONArray(v.map{value(it,depth+1)}))
                is Map<*,*> -> tagged("m",JSONArray(v.entries.map{entry->require(entry.key is String || entry.key is Int);JSONArray().put(value(entry.key,depth+1)).put(value(entry.value,depth+1))}))
                else -> error("El estado contiene un recurso que debe reconstruirse, no serializarse.")
            }
        }
        val result=JSONObject().put("version",1).put("state",value(state,0))
        require(result.toString().toByteArray(Charsets.UTF_8).size<=MAX_BYTES){"La sesión supera 384 KB. Conservá la conversación antes de continuar."}
        return result
    }
    fun decode(root:JSONObject):Map<String,List<Any?>> {
        require(root.toString().toByteArray(Charsets.UTF_8).size<=MAX_BYTES && root.getInt("version")==1)
        var nodes=0
        fun value(raw:Any,depth:Int):Any? {
            require(depth<=24 && ++nodes<=24000)
            if(raw===JSONObject.NULL)return null
            val j=raw as JSONObject;require(j.keys().asSequence().toSet()==setOf("t","v"))
            val v=j.get("v")
            return when(j.getString("t")){
                "si" -> mutableIntStateOf((v as Number).toInt())
                "sl" -> mutableLongStateOf((v as String).toLong())
                "sf" -> mutableFloatStateOf((v as Number).toFloat().also{require(it.isFinite())})
                "sd" -> mutableDoubleStateOf((v as Number).toDouble().also{require(it.isFinite())})
                "state" -> (v as JSONObject).let { state ->
                    val policy=when(state.getString("policy")){"s"->structuralEqualityPolicy<Any?>();"r"->referentialEqualityPolicy<Any?>();"n"->neverEqualPolicy<Any?>();else->error("Política no compatible.")}
                    mutableStateOf(value(state.get("value"),depth+1),policy)
                }
                "s" -> (v as String).also{require(it.length<=96000)}
                "b" -> v as Boolean
                "i" -> (v as Number).toLong().also{require(it in Int.MIN_VALUE..Int.MAX_VALUE && v.toDouble()==it.toDouble())}.toInt()
                "l" -> (v as String).toLong()
                "f" -> (v as Number).toFloat().also{require(it.isFinite())}
                "d" -> (v as Number).toDouble().also{require(it.isFinite())}
                "a" -> (v as JSONArray).let{a->ArrayList((0 until a.length()).map{value(a.get(it),depth+1)})}
                "m" -> (v as JSONArray).let{a->linkedMapOf<Any,Any?>().apply{for(i in 0 until a.length()){val e=a.getJSONArray(i);require(e.length()==2);val key=value(e.get(0),depth+1);require(key is String || key is Int);require(!containsKey(key));put(key!!,value(e.get(1),depth+1))}}}
                else -> error("Versión de valor no compatible.")
            }
        }
        val decoded=value(root.get("state"),0) as Map<*,*>
        return decoded.entries.associate { (k,v)->require(k is String && v is List<*>);k to v.toList() }
    }
    fun canSave(value:Any):Boolean {
        fun supported(v:Any?,depth:Int):Boolean=depth<=32 && when(v){
            null,is String,is Boolean,is Int,is Long -> true
            is SnapshotMutableState<*> -> supported(v.value,depth+1) && v.policy in listOf(structuralEqualityPolicy<Any?>(),referentialEqualityPolicy<Any?>(),neverEqualPolicy<Any?>())
            is MutableIntState,is MutableLongState,is MutableFloatState,is MutableDoubleState -> true
            is Float -> v.isFinite()
            is Double -> v.isFinite()
            is List<*> -> v.all{supported(it,depth+1)}
            is Map<*,*> -> v.all{(k,item)->(k is String || k is Int) && supported(item,depth+1)}
            else -> false
        }
        return supported(value,0)
    }
}
