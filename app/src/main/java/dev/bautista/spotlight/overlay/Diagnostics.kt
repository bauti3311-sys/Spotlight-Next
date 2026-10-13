package dev.bautista.spotlight.overlay

import android.content.Context
import android.os.SystemClock
import kotlinx.coroutines.flow.*
import org.json.JSONObject

data class OpeningSample(val visibleMs:Long,val interactiveMs:Long,val processCold:Boolean)
class Diagnostics(private val context:Context) {
    private var begun=0L;private var visible=0L;private var interactive=0L;private var cold=false
    private val samples=MutableStateFlow<List<OpeningSample>>(emptyList())
    val openings=samples.asStateFlow()
    private val searches=ArrayDeque<Long>()
    // Opt-in, bounded process-local evidence. Never retain query text, topics, URIs or keys.
    @Volatile private var searchTracing=false
    private val searchTrace=ArrayDeque<String>()
    val searchTracingEnabled get()=searchTracing
    @Synchronized fun enableSearchTracing(){searchTrace.clear();searchTracing=true}
    fun traceSearch(event:String,section:String?,generation:Int,rows:Int?=null){
        if(!searchTracing)return
        synchronized(this){
            searchTrace.addLast("${SystemClock.elapsedRealtime()} $event section=${section ?: "root"} generation=$generation rows=${rows ?: "-"}")
            while(searchTrace.size>160)searchTrace.removeFirst()
        }
    }
    @Synchronized fun searchTraceReport()=searchTrace.joinToString("\n")
    fun beginOpening(){begun=SystemClock.elapsedRealtime();visible=0L;interactive=0L;cold=begun-(context.applicationContext as? dev.bautista.spotlight.SpotlightApp)?.processStartedAt.orZero()<1000L}
    fun markVisible(){if(begun>0 && visible==0L){visible=(SystemClock.elapsedRealtime()-begun).coerceAtLeast(1);complete()}}
    fun markInteractive(){if(begun>0 && interactive==0L){interactive=(SystemClock.elapsedRealtime()-begun).coerceAtLeast(1);complete()}}
    private fun complete(){if(visible>0 && interactive>0){samples.value=(samples.value+OpeningSample(visible,interactive,cold)).takeLast(50);begun=0}}
    private fun Long?.orZero()=this ?: 0L
    @Synchronized fun search(duration:Long){searches.addLast(duration.coerceAtLeast(0));while(searches.size>100)searches.removeFirst()}
    @Synchronized fun report():String {
        fun p95(list:List<Long>)=list.sorted().let{if(it.isEmpty())JSONObject.NULL else it[(kotlin.math.ceil(it.size*.95).toInt()-1).coerceAtLeast(0)]}
        return JSONObject().put("version",1).put("androidApi",android.os.Build.VERSION.SDK_INT)
            .put("openingSamples",samples.value.size).put("coldFirstDrawMs",samples.value.firstOrNull{it.processCold}?.visibleMs ?: JSONObject.NULL)
            .put("warmFirstDrawP95Ms",p95(samples.value.filterNot{it.processCold}.map{it.visibleMs}))
            .put("interactiveP95Ms",p95(samples.value.map{it.interactiveMs})).put("searchSamples",searches.size)
            .put("searchP95Ms",p95(searches.toList())).put("measurement","Local observations in this process; no queries, names, keys or file paths").toString(2)
    }
    fun clear(){samples.value=emptyList();synchronized(this){searches.clear()}}
}
