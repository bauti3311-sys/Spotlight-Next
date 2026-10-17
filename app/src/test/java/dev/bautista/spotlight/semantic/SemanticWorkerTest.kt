package dev.bautista.spotlight.semantic

import kotlinx.coroutines.*
import org.junit.Test
import org.junit.Assert.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*

class SemanticWorkerTest {
    @Test fun cancelledNativeOwnerRetainsResourcesUntilItFinishesAndCannotPublishLate()=runBlocking {
        val entered=CountDownLatch(1);val release=CountDownLatch(1);val second=CountDownLatch(1);val closed=CountDownLatch(1)
        val calls=AtomicInteger();val active=AtomicInteger();val peak=AtomicInteger();val sawCancel=AtomicBoolean()
        val worker=SemanticWorker{object:SentenceEncoder {
            override fun embed(ids:LongArray,cancellation:InferenceCancellation):FloatArray {
                val n=active.incrementAndGet();peak.updateAndGet{maxOf(it,n)}
                try {
                    if(calls.incrementAndGet()==1){entered.countDown();check(release.await(5,TimeUnit.SECONDS));sawCancel.set(cancellation.cancelled)}else second.countDown()
                    return FloatArray(512){if(it==0)1f else 0f}
                }finally{active.decrementAndGet()}
            }
            override fun close(){assertEquals(0,active.get());closed.countDown()}
        }}
        try {
            val first=async{worker.encode(longArrayOf(101,102))}
            assertTrue(withContext(Dispatchers.IO){entered.await(5,TimeUnit.SECONDS)})
            first.cancelAndJoin();assertEquals(1,active.get());assertEquals(1L,closed.count)
            val next=async{worker.encode(longArrayOf(101,102))}
            delay(30);assertEquals(1L,second.count)
            release.countDown();assertNotNull(withTimeout(5000){next.await()})
            assertTrue(sawCancel.get());assertEquals(1,peak.get())
        }finally{release.countDown();worker.invalidate()}
        assertTrue(withContext(Dispatchers.IO){closed.await(5,TimeUnit.SECONDS)})
    }
    @Test fun cancellingQueuedRequestDoesNotCancelTheDifferentActiveOwner()=runBlocking {
        val entered=CountDownLatch(1);val release=CountDownLatch(1);val activeCancellation=AtomicReference<InferenceCancellation>()
        val worker=SemanticWorker{object:SentenceEncoder {
            override fun embed(ids:LongArray,cancellation:InferenceCancellation):FloatArray {activeCancellation.set(cancellation);entered.countDown();check(release.await(5,TimeUnit.SECONDS));return FloatArray(512){if(it==0)1f else 0f}}
            override fun close(){}
        }}
        try {
            val first=async{worker.encode(longArrayOf(1,2))}
            assertTrue(withContext(Dispatchers.IO){entered.await(5,TimeUnit.SECONDS)})
            val queued=async(start=CoroutineStart.UNDISPATCHED){worker.encode(longArrayOf(3,4))}
            queued.cancelAndJoin();assertFalse(activeCancellation.get().cancelled)
            release.countDown();assertNotNull(withTimeout(5000){first.await()})
        }finally{release.countDown();worker.invalidate()}
    }
    @Test fun nativeCancellationBindingNeverSurvivesDetach() {
        val cancellation=InferenceCancellation();var calls=0
        cancellation.attach{calls++};cancellation.detach();cancellation.cancel();assertEquals(0,calls)
        cancellation.attach{calls++};assertEquals(1,calls);cancellation.detach()
    }
}
