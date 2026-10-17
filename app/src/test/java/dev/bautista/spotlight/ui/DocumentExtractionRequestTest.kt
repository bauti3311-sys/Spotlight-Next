package dev.bautista.spotlight.ui

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class DocumentExtractionRequestTest {
    @Test fun replacementDropsOldCompletionAndKeepsNewRequestBusy() = runBlocking {
        val request = DocumentExtractionRequest()
        val old = CompletableDeferred<String>()
        val next = CompletableDeferred<String>()
        val outputs = mutableListOf<String>()
        request.start(this, { true }, { withContext(NonCancellable) { old.await() } }, outputs::add) { throw it }
        yield()
        request.start(this, { true }, { next.await() }, outputs::add) { throw it }
        yield()
        old.complete("obsolete PDF")
        yield(); yield()
        assertTrue(request.running)
        assertTrue(outputs.isEmpty())
        next.complete("current PDF")
        yield(); yield()
        assertEquals(listOf("current PDF"), outputs)
        assertFalse(request.running)
    }

    @Test fun changedSelectionBeforeComposeDisposalDropsBothTextAndError() = runBlocking {
        val request = DocumentExtractionRequest()
        var current = true
        val ready = CompletableDeferred<String>()
        val outputs = mutableListOf<String>()
        val errors = mutableListOf<Exception>()
        request.start(this, { current }, { ready.await() }, outputs::add, errors::add)
        yield(); current = false; ready.complete("old selection")
        yield(); yield()
        assertTrue(outputs.isEmpty()); assertTrue(errors.isEmpty())
        request.start<String>(this, { current }, { error("stale read failure") }, outputs::add, errors::add)
        yield(); yield()
        assertTrue(errors.isEmpty()); assertFalse(request.running)
    }

    @Test fun explicitCancelDropsDelayedNativeFailureAndClearsBusy() = runBlocking {
        val request = DocumentExtractionRequest()
        val finished = CompletableDeferred<Unit>()
        val errors = mutableListOf<Exception>()
        request.start<String>(this, { true }, {
            withContext(NonCancellable) { finished.await(); error("native failure after cancel") }
        }, { fail("Cancelled extraction must not publish") }, errors::add)
        yield(); assertTrue(request.running)
        request.cancel(); request.cancel()
        assertFalse(request.running)
        finished.complete(Unit); yield(); yield()
        assertTrue(errors.isEmpty())
    }

    @Test fun currentFailureIsVisibleAndNextExtractionCanSucceed() = runBlocking {
        val request = DocumentExtractionRequest()
        var message: String? = null
        request.start<String>(this, { true }, { error("PDF protegido") }, { fail() }, { message = it.message })
        yield(); yield()
        assertEquals("PDF protegido", message)
        assertFalse(request.running)
        var text: String? = null
        request.start(this, { true }, { "texto local" }, { text = it }) { throw it }
        yield(); yield()
        assertEquals("texto local", text)
        assertFalse(request.running)
    }

    @Test fun disposedParentCannotPublishNativeErrorEvenBeforeOwnerDisposal() = runBlocking {
        val parent = SupervisorJob(coroutineContext[Job])
        val scope = CoroutineScope(coroutineContext + parent)
        val request = DocumentExtractionRequest()
        val finished = CompletableDeferred<Unit>()
        val errors = mutableListOf<Exception>()
        request.start<String>(scope, { true }, {
            withContext(NonCancellable) { finished.await(); error("late native error") }
        }, { fail() }, errors::add)
        yield(); parent.cancel(); finished.complete(Unit); parent.join()
        assertTrue(errors.isEmpty())
        assertFalse(request.running)
    }

    @Test fun alreadyDisposedScopeDoesNotLeaveExtractionBusy() = runBlocking {
        val parent = SupervisorJob(coroutineContext[Job]).apply { cancel() }
        val request = DocumentExtractionRequest()
        request.start(CoroutineScope(coroutineContext + parent), { true }, { "never read" }, { fail() }) { throw it }
        parent.join()
        assertFalse(request.running)
    }
}
