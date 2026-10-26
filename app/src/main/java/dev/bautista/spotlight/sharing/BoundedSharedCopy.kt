package dev.bautista.spotlight.sharing

import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.yield

/** Caller owns streams and deletes an unpublished destination on failure/cancellation.
 * Provider reads can block; cancellation is checked again before any subsequent write. */
suspend fun copySharedContent(input:InputStream,output:OutputStream,limit:Long,onProgress:(Long)->Unit):Long {
    require(limit>0)
    val buffer=ByteArray(8192);var total=0L;var reported=0L;var stalled=0
    while(true) {
        currentCoroutineContext().ensureActive()
        val count=input.read(buffer)
        currentCoroutineContext().ensureActive()
        if(count<0)break
        if(count==0){check(++stalled<=8){"El proveedor no entrega datos."};yield();continue}
        stalled=0
        require(total<=limit-count){"El archivo supera el límite permitido."}
        output.write(buffer,0,count);total+=count
        if(total-reported>=65536){onProgress(total);reported=total}
    }
    currentCoroutineContext().ensureActive();onProgress(total);return total
}
