package dev.bautista.spotlight.index

import dev.bautista.spotlight.control.DeviceResourceState

enum class OcrDeferral(val code:String,val label:String) {
    MEMORY("memory","presión de memoria"), THERMAL("thermal","temperatura elevada"),
    POWER_SAVE("power_save","ahorro de energía"), BATTERY("battery","batería baja"),
    UNKNOWN("unknown","estado del dispositivo no disponible"),
    REDUCED("reduced","presupuesto reducido de OCR"), PERMISSION("permission","permiso revocado"),
}
data class OcrBudgetDecision(
    val deferred:OcrDeferral?=null, val pages:Int=3, val longEdge:Int=1600,
    val pixels:Int=1800000, val pageMillis:Long=12000,
)
object OcrBudget {
    private const val MIB=1024L*1024
    fun evaluate(s:DeviceResourceState):OcrBudgetDecision {
        val available=s.availableMemory;val threshold=s.memoryThreshold
        val memoryFloor=maxOf(64*MIB,threshold ?: 0)
        val reason=when {
            s.lowMemory==true || available!=null && available<memoryFloor -> OcrDeferral.MEMORY
            s.thermalStatus!=null && s.thermalStatus in 3..6 -> OcrDeferral.THERMAL
            s.powerSave==true -> OcrDeferral.POWER_SAVE
            s.batteryPercent!=null && s.batteryPercent !in 0..100 -> OcrDeferral.UNKNOWN
            s.batteryPercent!=null && s.batteryPercent<=15 && s.charging!=true -> OcrDeferral.BATTERY
            available==null || threshold==null || s.lowMemory==null || s.powerSave==null ||
                s.batteryPercent==null && s.charging!=true || s.thermalSupported && (s.thermalStatus?.let{it !in 0..6} ?: true) -> OcrDeferral.UNKNOWN
            else -> null
        }
        if(reason!=null)return OcrBudgetDecision(deferred=reason,pages=0)
        val reduced=s.thermalStatus==2 || s.batteryPercent!=null && s.batteryPercent<=30 && s.charging!=true ||
            available!!<maxOf(128*MIB, memoryFloor.coerceAtMost(Long.MAX_VALUE/2)*2)
        return if(reduced)OcrBudgetDecision(pages=1,longEdge=1024,pixels=786432,pageMillis=8000) else OcrBudgetDecision()
    }
    fun label(code:String)=OcrDeferral.entries.firstOrNull { it.code==code }?.label ?: "presupuesto local de OCR"
}

/** Snapshot of already recognized pages. Reuse is allowed only after hashing the current bytes. */
data class CachedPdfText(val text:String,val metadata:String) {
    internal fun recognized(page:Int,sha:String,pages:Int):Pair<TextFragment,String>? = runCatching {
        require(metadata.length<=30000 && text.length<=100000)
        val root=org.json.JSONObject(metadata)
        require(root.getInt("version")==1)
        require(sha.matches(Regex("[0-9a-f]{64}")) && root.getString("sha256")==sha && root.getInt("pages")==pages)
        val issues=root.optJSONArray("issues")
        if(issues!=null && (0 until issues.length()).any {
            val issue=issues.getJSONObject(it);issue.optString("code")=="character_limit" && issue.optInt("page")==page
        })return@runCatching null
        val parts=root.getJSONArray("fragments");require(parts.length()<=40)
        (0 until parts.length()).map { parts.getJSONObject(it) }.firstOrNull {
            it.getInt("page")==page && it.getString("method")=="ocr_local"
        }?.let {
            val start=it.getInt("start");val end=it.getInt("end")
            require(start>=0 && end>start && end<=text.length)
            TextFragment(page,"ocr_local",start,end,it.optInt("width"),it.optInt("height")) to text.substring(start,end)
        }
    }.getOrNull()
}
