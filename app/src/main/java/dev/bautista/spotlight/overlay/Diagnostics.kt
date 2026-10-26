package dev.bautista.spotlight.overlay

import android.content.Context
import android.os.SystemClock
import kotlinx.coroutines.flow.*
import org.json.JSONObject

data class OpeningSample(val visibleMs:Long,val interactiveMs:Long,val firstInProcess:Boolean)
class Diagnostics(private val context:Context,private val clock:()->Long={SystemClock.elapsedRealtime()}) {
    private var begun:Long?=null;private var visible:Long?=null;private var interactive:Long?=null
    private var firstInvocation=true;private var currentIsFirst=false
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
    @Synchronized fun beginOpening(){begun=clock();visible=null;interactive=null;currentIsFirst=firstInvocation;firstInvocation=false}
    private fun elapsed():Long? {val start=begun ?: return null;val now=clock();return (now-start).takeIf{now>=start}}
    @Synchronized fun markVisible(){if(begun!=null && visible==null){visible=elapsed() ?: return;complete()}}
    @Synchronized fun markInteractive(){if(begun!=null && interactive==null){interactive=elapsed() ?: return;complete()}}
    private fun complete(){val v=visible;val i=interactive;if(v!=null && i!=null){samples.value=(samples.value+OpeningSample(v,i,currentIsFirst)).takeLast(50);begun=null}}
    @Synchronized fun search(duration:Long){if(duration<0)return;searches.addLast(duration);while(searches.size>100)searches.removeFirst()}
    @Synchronized fun report():String {
        fun p95(list:List<Long>)=list.sorted().let{if(it.isEmpty())JSONObject.NULL else it[(kotlin.math.ceil(it.size*.95).toInt()-1).coerceAtLeast(0)]}
        return JSONObject().put("version",2).put("androidApi",android.os.Build.VERSION.SDK_INT)
            .put("openingSamples",samples.value.size).put("coldFirstDrawMs",JSONObject.NULL)
            .put("firstOverlayDrawMs",samples.value.firstOrNull{it.firstInProcess}?.visibleMs ?: JSONObject.NULL)
            .put("warmFirstDrawP95Ms",p95(samples.value.filterNot{it.firstInProcess}.map{it.visibleMs}))
            .put("interactiveP95Ms",p95(samples.value.map{it.interactiveMs})).put("searchSamples",searches.size)
            .put("searchP95Ms",p95(searches.toList())).put("coldStartupMeasurement","Not instrumented: first overlay invocation is not a cold process startup")
            .put("javaHeapUsedBytes",Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())
            .put("javaHeapLimitBytes",Runtime.getRuntime().maxMemory())
            .put("memoryMeasurement","Current Java heap only; not total process RAM, PSS or a physical benchmark")
            .put("measurement","Local observations in this process; no queries, names, keys or file paths").toString(2)
    }
    @Synchronized fun clear(){samples.value=emptyList();searches.clear();begun=null;visible=null;interactive=null}
}
