package dev.bautista.spotlight.semantic

import kotlinx.coroutines.*

/** Default implementation runs the pinned local model. Tests can inject a labelled fixture. */
interface DocumentEmbedder {
    suspend fun available():Boolean
    suspend fun fragments(text:String):WordPiece.Fragments
    suspend fun query(text:String):FloatArray?
    suspend fun embed(ids:LongArray):FloatArray?
    fun invalidate()
}
class LocalDocumentEmbedder(private val store:SemanticModelStore):DocumentEmbedder {
    @Volatile private var tokenizer:WordPiece?=null
    private val worker=SemanticWorker{OnnxSentenceEncoder(store.directory)}
    override suspend fun available()=store.ready()
    private fun tokenizer():WordPiece=tokenizer ?: synchronized(this) {
        tokenizer ?: WordPiece(java.io.File(store.directory,"vocab.txt").readLines(Charsets.UTF_8)).also{tokenizer=it}
    }
    override suspend fun fragments(text:String)=withContext(Dispatchers.IO){tokenizer().fragments(text)}
    override suspend fun query(text:String)=withContext(Dispatchers.IO){embed(tokenizer().encode(text))}
    override suspend fun embed(ids:LongArray)=withTimeoutOrNull(1500){worker.encode(ids)}
    override fun invalidate(){tokenizer=null;worker.invalidate()}
}
