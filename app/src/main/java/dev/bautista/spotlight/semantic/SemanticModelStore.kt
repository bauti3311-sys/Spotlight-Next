package dev.bautista.spotlight.semantic

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.*
import java.io.*
import java.security.MessageDigest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SemanticModelStore(context:Context,private val client:()->OkHttpClient={OkHttpClient.Builder().connectTimeout(15,java.util.concurrent.TimeUnit.SECONDS).readTimeout(30,java.util.concurrent.TimeUnit.SECONDS).callTimeout(300,java.util.concurrent.TimeUnit.SECONDS).build()}) {
    data class State(val installed:Boolean=false,val busy:Boolean=false,val bytes:Long=0,val message:String="Modelo local no instalado")
    val directory=File(context.noBackupFilesDir,"semantic/${SemanticPolicy.MODEL}")
    private val mutable=MutableStateFlow(State())
    val state=mutable.asStateFlow()
    private val lock=Mutex()
    @Volatile private var verified=false
    @Volatile private var generation=0L
    suspend fun ready():Boolean=withContext(Dispatchers.IO) {
        if(verified)return@withContext true
        val owner=generation
        val valid=SemanticModel.assets.all { a ->valid(File(directory,a.local),a) }
        if(owner!=generation)return@withContext false
        verified=valid
        if(!mutable.value.busy)mutable.value=State(valid,message=if(valid)"Modelo local verificado" else "Modelo local no instalado o incompleto")
        valid
    }
    private fun valid(file:File,a:SemanticModel.Asset):Boolean {
        if(!file.isFile || file.length()!=a.size)return false
        val digest=MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use{stream->val buffer=ByteArray(65536);while(true){val n=stream.read(buffer);if(n<0)break;digest.update(buffer,0,n)}}
        return digest.digest().joinToString(""){"%02x".format(it)}==a.sha256
    }
    suspend fun install()=withContext(Dispatchers.IO) {lock.withLock {
        if(ready())return@withLock
        val owner=generation
        val staging=File(directory.parentFile,".staging-${java.util.UUID.randomUUID()}")
        check(directory.parentFile?.let{it.mkdirs() || it.isDirectory}==true)
        check(android.os.StatFs(directory.parentFile!!.path).availableBytes>=SemanticModel.TOTAL+32L*1024*1024){"Se necesitan al menos 170 MB libres para el modelo."}
        check(staging.mkdirs());mutable.value=State(busy=true,message="Descargando solo el modelo; no se envían consultas ni documentos")
        try {
            var completed=0L
            for(asset in SemanticModel.assets) {
                currentCoroutineContext().ensureActive();check(owner==generation)
                download(asset,File(staging,asset.local)){n->mutable.value=mutable.value.copy(bytes=completed+n)}
                completed+=asset.size
            }
            currentCoroutineContext().ensureActive();check(owner==generation)
            // A verified existing installation is never replaced by partial files.
            if(directory.exists())check(directory.deleteRecursively())
            check(staging.renameTo(directory)){"No se pudo guardar el modelo local."}
            verified=true;mutable.value=State(true,bytes=SemanticModel.TOTAL,message="Modelo local verificado")
        }catch(e:CancellationException){mutable.value=State(message="Descarga cancelada; no se activó un modelo parcial");throw e}
        catch(e:Exception){mutable.value=State(message=e.message ?: "No se pudo instalar el modelo local.");throw e}
        finally{staging.deleteRecursively()}
    }}
    private suspend fun download(a:SemanticModel.Asset,target:File,progress:(Long)->Unit)=suspendCancellableCoroutine<Unit> { continuation ->
        val call=client().newCall(Request.Builder().url(SemanticModel.BASE+a.remote+"?download=true").get().build())
        continuation.invokeOnCancellation{call.cancel()}
        call.enqueue(object:Callback {
            override fun onFailure(call:Call,e:IOException){if(continuation.isActive)continuation.resumeWithException(IOException("No se pudo descargar el modelo. Reintentá con una conexión estable."))}
            override fun onResponse(call:Call,response:Response){response.use{r->
                try {
                    check(r.isSuccessful){"No se pudo descargar el modelo (HTTP ${r.code})."}
                    val body=requireNotNull(r.body);check(body.contentLength()<=a.size)
                    val digest=MessageDigest.getInstance("SHA-256");var size=0L
                    body.byteStream().use{input->target.outputStream().buffered().use{out->
                        val buffer=ByteArray(65536)
                        while(true){if(!continuation.isActive)throw CancellationException();val n=input.read(buffer);if(n<0)break;size+=n;check(size<=a.size){"Modelo de tamaño inesperado."};digest.update(buffer,0,n);out.write(buffer,0,n);progress(size)}
                    }}
                    check(size==a.size && digest.digest().joinToString(""){"%02x".format(it)}==a.sha256){"El modelo no coincide con la versión verificada. No se instaló."}
                    if(continuation.isActive)continuation.resume(Unit)
                }catch(t:Exception){if(continuation.isActive)continuation.resumeWithException(t)}
            }}
        })
    }
    suspend fun remove()=withContext(Dispatchers.IO){lock.withLock{generation++;verified=false;check(!directory.exists() || directory.deleteRecursively());mutable.value=State(message="Modelo local eliminado")}}
}
