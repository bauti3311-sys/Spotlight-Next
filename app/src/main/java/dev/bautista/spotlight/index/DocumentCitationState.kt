package dev.bautista.spotlight.index

import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicBoolean

/** Ephemeral owner, deliberately not wired to the bar and never saved/restored.
 * The host must call invalidate() synchronously on access/provider/lifecycle events,
 * including returning from Android permission settings. SAF has no universal revocation callback.
 * Index revision/config are additional invalidation signals, not proof of current permission.
 */
class DocumentCitationState(
    private val index:FileIndex,
    private val config:StateFlow<Config>,
    parent:CoroutineScope,
):AutoCloseable {
    private val guard=Any()
    private val scope=CoroutineScope(parent.coroutineContext+SupervisorJob(parent.coroutineContext[Job]))
    private val content=MutableStateFlow<List<DocumentCitation>>(emptyList())
    val visible:StateFlow<List<DocumentCitation>> = content.asStateFlow()
    private var generation=0L
    private var closed=false
    private var session:DocumentCitationSession?=null
    private var pending:Job?=null

    init {
        scope.launch(start=CoroutineStart.UNDISPATCHED){config.drop(1).collect{invalidate()}}
        scope.launch(start=CoroutineStart.UNDISPATCHED){index.revision.drop(1).collect{invalidate()}}
        scope.launch(start=CoroutineStart.UNDISPATCHED){try{awaitCancellation()}finally{invalidate()}}
    }

    /** Clears plaintext before cancellation/IO. Also used by explicit cancel and host backgrounding. */
    fun invalidate()=synchronized(guard){reset()}
    private fun reset() {
        generation++
        content.value=emptyList()
        val old=pending;pending=null
        val previous=session;session=null
        previous?.close();old?.cancel()
    }
    override fun close() {
        synchronized(guard){if(closed)return;closed=true;reset()}
        scope.cancel()
    }

    /** Starts a new lifetime using the existing retrieval engine; no text is carried to the next query. */
    fun query(text:String):Job {
        val job=synchronized(guard) {
            check(!closed && scope.isActive){"La sesión de citas está cerrada"}
            reset()
            val epoch=generation;val preferences=config.value;val revision=index.revision.value
            val current=DocumentCitationSession(index){config.value};session=current
            scope.launch(start=CoroutineStart.LAZY) {
                try {
                    val citations=current.retrieve(text).mapNotNull{current.resolve(it)}
                    synchronized(guard) {
                        if(valid(epoch,preferences,revision))content.value=citations
                    }
                } catch(e:CancellationException){invalidateIfCurrent(epoch);throw e}
                  catch(_:Exception){invalidateIfCurrent(epoch)}
            }.also { task ->
                pending=task
                task.invokeOnCompletion{cause->if(cause!=null)invalidateIfCurrent(epoch)}
            }
        }
        job.start();return job
    }

    /** Explicit display/copy/open gate. Callback must be synchronous, short and must not cache text.
     * No Quick Look, clipboard or Intent is invoked by this owner itself. */
    suspend fun withValidated(ref:CitationRef,action:(DocumentCitation)->Unit):Boolean=coroutineScope {
        data class Snapshot(val epoch:Long,val preferences:Config,val revision:Long,val current:DocumentCitationSession)
        val snapshot=synchronized(guard) {
            if(closed || content.value.none{it.sourceId==ref.sourceId})return@coroutineScope false
            Snapshot(generation,config.value,index.revision.value,session ?: return@coroutineScope false)
        }
        // This sibling can clear state on caller cancellation even while resolve is stuck in native IO.
        val finished=AtomicBoolean(false)
        val cancellation=launch(start=CoroutineStart.UNDISPATCHED) {
            try{awaitCancellation()}finally{if(!finished.get())invalidateIfCurrent(snapshot.epoch)}
        }
        try {
            val citation=try{snapshot.current.resolve(ref)}
                catch(e:CancellationException){invalidateIfCurrent(snapshot.epoch);throw e}
                catch(_:Exception){null}
            synchronized(guard) {
                if(!valid(snapshot.epoch,snapshot.preferences,snapshot.revision)) {
                    invalidateIfCurrent(snapshot.epoch);false
                } else if(citation==null){reset();false}
                else {action(citation);true}
            }
        } finally {finished.set(true);cancellation.cancel()}
    }
    private fun valid(epoch:Long,preferences:Config,revision:Long)=
        !closed && scope.isActive && generation==epoch && config.value==preferences && index.revision.value==revision
    private fun invalidateIfCurrent(epoch:Long)=synchronized(guard){if(generation==epoch)reset()}
}
