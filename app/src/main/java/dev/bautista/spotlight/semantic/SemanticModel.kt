package dev.bautista.spotlight.semantic

import ai.onnxruntime.*
import java.io.Closeable
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.LongBuffer
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject

object SemanticModel {
    const val REVISION="bfe45d0732ca50787611c0fe107ba278c7f3f889"
    const val BASE="https://huggingface.co/sentence-transformers/distiluse-base-multilingual-cased-v2/resolve/$REVISION/"
    data class Asset(val remote:String,val local:String,val size:Long,val sha256:String)
    val assets=listOf(
        Asset("onnx/model_qint8_arm64.onnx","model.onnx",135336307,"c78c64bb0446ceb7086d2de24c2f4297e7a909eb457b4f68e5fdb6ab19f285c0"),
        Asset("vocab.txt","vocab.txt",995526,"fe0fda7c425b48c516fc8f160d594c8022a0808447475c1a7c6d6479763f310c"),
        Asset("2_Dense/model.safetensors","dense.safetensors",1575104,"0a21b1ce908e772ebf09f93c20ca09524c32706e9918d9c0169a3f0663b191ed"),
    )
    const val TOTAL=137906937L
}

/** Cancellation owns a single native run. It never closes resources still used by JNI. */
class InferenceCancellation {
    @Volatile var cancelled=false
        private set
    private var terminate:(()->Unit)?=null
    @Synchronized fun attach(block:()->Unit){terminate=block;if(cancelled)runCatching{block()}}
    @Synchronized fun detach(){terminate=null}
    @Synchronized fun cancel(){cancelled=true;runCatching{terminate?.invoke()}}
}
interface SentenceEncoder:Closeable {fun embed(ids:LongArray,cancellation:InferenceCancellation):FloatArray}

/** Same mean pooling + learned 768→512 Dense/Tanh as the pinned model card. */
class OnnxSentenceEncoder(directory:File):SentenceEncoder {
    private val environment=OrtEnvironment.getEnvironment()
    private val session=OrtSession.SessionOptions().use { options ->
        options.setIntraOpNumThreads(2);options.setInterOpNumThreads(1);options.setCPUArenaAllocator(false)
        environment.createSession(File(directory,"model.onnx").path,options)
    }
    private val weights:FloatArray
    private val bias:FloatArray
    init {
        try {
            val data=File(directory,"dense.safetensors").readBytes()
            val buffer=ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);val headerSize=buffer.long
            require(headerSize in 1..8192 && headerSize+8<data.size)
            val j=JSONObject(String(data,8,headerSize.toInt(),Charsets.UTF_8));val base=8+headerSize.toInt()
            fun tensor(name:String,shape:List<Int>):FloatArray {
                val t=j.getJSONObject(name);require(t.getString("dtype")=="F32")
                val dimensions=t.getJSONArray("shape");require(dimensions.length()==shape.size && shape.indices.all{dimensions.getInt(it)==shape[it]})
                val offsets=t.getJSONArray("data_offsets");val count=shape.fold(1){a,b->a*b};val start=offsets.getLong(0);val end=offsets.getLong(1)
                require(start>=0 && end-start==count*4L && base+end<=data.size)
                val floats=ByteBuffer.wrap(data,base+start.toInt(),count*4).order(ByteOrder.LITTLE_ENDIAN)
                return FloatArray(count){floats.float.also{require(it.isFinite())}}
            }
            weights=tensor("linear.weight",listOf(512,768));bias=tensor("linear.bias",listOf(512))
            require(session.inputNames==setOf("input_ids","attention_mask"))
        }catch(t:Throwable){session.close();throw t}
    }
    override fun embed(ids:LongArray,cancellation:InferenceCancellation):FloatArray {
        require(ids.size in 2..128)
        val shape=longArrayOf(1,ids.size.toLong())
        OnnxTensor.createTensor(environment,LongBuffer.wrap(ids),shape).use { input ->
            OnnxTensor.createTensor(environment,LongBuffer.wrap(LongArray(ids.size){1}),shape).use { mask ->
                OrtSession.RunOptions().use { run ->
                    cancellation.attach{run.setTerminate(true)}
                    try {
                        session.run(mapOf("input_ids" to input,"attention_mask" to mask),run).use { output ->
                            @Suppress("UNCHECKED_CAST")
                            val hidden=output[0].value as Array<Array<FloatArray>>
                            require(hidden.size==1 && hidden[0].size==ids.size && hidden[0].all{it.size==768})
                            val pooled=DoubleArray(768)
                            hidden[0].forEach{row->for(i in row.indices)pooled[i]+=row[i]/ids.size.toDouble()}
                            val vector=FloatArray(512){i->
                                var sum=bias[i].toDouble();for(k in 0 until 768)sum+=weights[i*768+k]*pooled[k]
                                kotlin.math.tanh(sum).toFloat()
                            }
                            return SemanticPolicy.normalized(vector)
                        }
                    }finally{cancellation.detach()}
                }
            }
        }
    }
    override fun close(){session.close()}
}

/** One native owner, at most one queued request. Cancellation discards late results. */
class SemanticWorker(private val factory:()->SentenceEncoder) {
    private val executor=ThreadPoolExecutor(1,1,30,TimeUnit.SECONDS,ArrayBlockingQueue(1),{r->Thread(r,"spotlight-semantic").apply{isDaemon=true}},ThreadPoolExecutor.AbortPolicy()).apply{allowCoreThreadTimeOut(true)}
    private val timer by lazy{Executors.newSingleThreadScheduledExecutor{r->Thread(r,"spotlight-semantic-idle").apply{isDaemon=true}}}
    private val generation=AtomicLong()
    private val tickets=ConcurrentHashMap.newKeySet<InferenceCancellation>()
    private var engine:SentenceEncoder?=null // owned exclusively by executor
    private var idle:ScheduledFuture<*>?=null
    suspend fun encode(ids:LongArray):FloatArray?=suspendCancellableCoroutine { continuation ->
        val owner=generation.get();val cancellation=InferenceCancellation();tickets+=cancellation
        val future=AtomicReference<Future<*>?>()
        continuation.invokeOnCancellation{cancellation.cancel();future.get()?.let{it.cancel(false);executor.remove(it as Runnable)};tickets-=cancellation}
        try {
            val submitted=executor.submit {
                try {
                    if(!continuation.isActive || cancellation.cancelled || generation.get()!=owner){if(continuation.isActive)continuation.resume(null);return@submit}
                    idle?.cancel(false)
                    if(engine==null)engine=factory()
                    if(cancellation.cancelled || generation.get()!=owner){if(continuation.isActive)continuation.resume(null);return@submit}
                    val vector=engine!!.embed(ids,cancellation)
                    if(continuation.isActive)continuation.resume(if(generation.get()==owner && !cancellation.cancelled)vector else null)
                }catch(t:Throwable){
                    runCatching{engine?.close()};engine=null
                    if(continuation.isActive)continuation.resumeWithException(t)
                }finally {
                    tickets-=cancellation
                    if(generation.get()!=owner){runCatching{engine?.close()};engine=null}
                    else idle=timer.schedule({try{executor.execute{if(generation.get()==owner && tickets.isEmpty()){runCatching{engine?.close()};engine=null}}}catch(_:RejectedExecutionException){}},30,TimeUnit.SECONDS)
                }
            }
            future.set(submitted)
            if(!continuation.isActive){submitted.cancel(false);executor.remove(submitted as Runnable)}
        }catch(_:RejectedExecutionException){tickets-=cancellation;if(continuation.isActive)continuation.resume(null)}
    }
    fun invalidate() {
        generation.incrementAndGet();tickets.forEach{it.cancel()}
        try{executor.execute{runCatching{engine?.close()};engine=null}}catch(_:RejectedExecutionException){} // active owner also closes in finally
    }
}
