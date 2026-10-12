package dev.bautista.spotlight.overlay

import android.content.Context
import android.os.SystemClock
import kotlinx.coroutines.flow.*
import org.json.JSONObject

data class OpeningSample(val visibleMs:Long,val interactiveMs:Long,val processCold:Boolean)
class Diagnostics(private val context:Context) {
    private var begun=0L;private var first=true;private var visible=0L
    private val samples=MutableStateFlow<List<OpeningSample>>(emptyList())
    val openings=samples.asStateFlow()
    private val searches=ArrayDeque<Long>()
    fun beginOpening(){begun=SystemClock.elapsedRealtime();visible=0L}
    fun markVisible(){if(begun>0 && visible==0L)visible=(SystemClock.elapsedRealtime()-begun).coerceAtLeast(1)}
    fun markInteractive(){if(begun>0){samples.value=(samples.value+OpeningSample(visible,(SystemClock.elapsedRealtime()-begun).coerceAtLeast(0),first)).takeLast(50);first=false;begun=0}}
    @Synchronized fun search(duration:Long){searches.addLast(duration.coerceAtLeast(0));while(searches.size>100)searches.removeFirst()}
    @Synchronized fun report():String {
        fun p95(list:List<Long>)=list.sorted().let{if(it.isEmpty())JSONObject.NULL else it[((it.size-1)*.95).toInt()]}
        return JSONObject().put("version",1).put("androidApi",android.os.Build.VERSION.SDK_INT)
            .put("openingSamples",samples.value.size).put("coldFirstDrawMs",samples.value.firstOrNull{it.processCold}?.visibleMs ?: JSONObject.NULL)
            .put("warmFirstDrawP95Ms",p95(samples.value.filterNot{it.processCold}.map{it.visibleMs}))
            .put("interactiveP95Ms",p95(samples.value.map{it.interactiveMs})).put("searchSamples",searches.size)
            .put("searchP95Ms",p95(searches.toList())).put("measurement","Local observations in this process; no queries, names, keys or file paths").toString(2)
    }
    fun clear(){samples.value=emptyList();synchronized(this){searches.clear()}}
}
