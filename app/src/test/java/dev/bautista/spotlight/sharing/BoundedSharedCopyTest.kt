package dev.bautista.spotlight.sharing

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class BoundedSharedCopyTest {
    @Test fun boundedCopyReportsMonotonicBytesAndFinalSize()=runBlocking {
        val bytes=ByteArray(150000){(it%251).toByte()};val out=ByteArrayOutputStream();val progress=mutableListOf<Long>()
        assertEquals(bytes.size.toLong(),copySharedContent(ByteArrayInputStream(bytes),out,200000){progress+=it})
        assertArrayEquals(bytes,out.toByteArray());assertEquals(150000L,progress.last())
        assertEquals(progress.sorted(),progress);assertTrue(progress.size<=4)
    }
    @Test fun oversizedInputNeverWritesBeyondTheLimit()=runBlocking {
        val out=ByteArrayOutputStream()
        assertTrue(runCatching{copySharedContent(ByteArrayInputStream(ByteArray(20000)),out,10000){}}.isFailure)
        assertTrue(out.size()<=10000)
    }
    @Test fun cancellationDuringProviderReadPreventsWriteAndPublication()=runBlocking {
        val out=ByteArrayOutputStream();var published=false
        val job=launch {
            val self=currentCoroutineContext().job
            val input=object:InputStream(){override fun read()=1;override fun read(b:ByteArray,off:Int,len:Int):Int{b[off]=1;self.cancel();return 1}}
            copySharedContent(input,out,1000){}
            published=true
        }
        job.join();assertTrue(job.isCancelled);assertFalse(published);assertEquals(0,out.size())
    }
    @Test fun stalledProviderFailsInsteadOfSpinningForever()=runBlocking {
        val input=object:InputStream(){override fun read()=0;override fun read(b:ByteArray,off:Int,len:Int)=0}
        assertTrue(runCatching{copySharedContent(input,ByteArrayOutputStream(),1000){}}.isFailure)
    }
}
