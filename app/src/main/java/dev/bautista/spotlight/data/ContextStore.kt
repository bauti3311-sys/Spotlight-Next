package dev.bautista.spotlight.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.AtomicFile
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.*
import java.io.File

data class SharedContext(val id:String,val title:String,val kind:String,val text:String="",val uri:String="",val mime:String="",val created:Long=System.currentTimeMillis(),val pinned:Boolean=false,val ownedFile:String="")
data class SavedConversation(val id:String,val title:String,val text:String,val created:Long,val turns:List<Pair<String,String>> = emptyList())

/** Explicit Android inputs only. Temporary context expires; saved items never enter diagnostics. */
class ContextStore(private val context:Context,private val scope:CoroutineScope,private val preferences:Preferences) {
    private val file=AtomicFile(File(context.noBackupFilesDir,"spotlight-context-v1.json"))
    private val lock=Mutex()
    private var started=false
    private var loadJob:Job?=null
    private var original=JSONObject()
    private val conversationRecords=mutableMapOf<String,JSONObject>()
    private val contextRecords=mutableMapOf<String,JSONObject>()
    @Volatile private var pinningFile=""
    private var expiryJob:Job?=null
    // Process-local deduplication: never persist clipboard text just to remember an event.
    private var lastAutomaticFingerprint:String?=null
    private var automaticId:String?=null
    private fun scheduleExpiry(){expiryJob?.cancel();val item=mutableActive.value ?: return;if(!item.pinned)expiryJob=scope.launch{delay((item.created+600000L-System.currentTimeMillis()).coerceAtLeast(1));expire()}}
    private val mutableActive=MutableStateFlow<SharedContext?>(null)
    val active=mutableActive.asStateFlow()
    private val mutablePinned=MutableStateFlow<List<SharedContext>>(emptyList())
    val pinned=mutablePinned.asStateFlow()
    private val mutableConversations=MutableStateFlow<List<SavedConversation>>(emptyList())
    val conversations=mutableConversations.asStateFlow()
    fun start(){if(started)return;started=true;loadJob=scope.launch {lock.withLock{
        val j=runCatching{JSONObject(file.readFully().toString(Charsets.UTF_8))}.getOrNull() ?: return@withLock
        original=j
        val a=j.optJSONArray("contexts") ?: JSONArray()
        mutablePinned.value=(0 until minOf(a.length(),30)).mapNotNull {i->runCatching{val x=a.getJSONObject(i);SharedContext(x.getString("id"),x.getString("title"),x.getString("kind"),x.optString("text").take(16000),x.optString("uri"),x.optString("mime"),x.getLong("created"),true,x.optString("ownedFile")).also{contextRecords[it.id]=x}}.getOrNull()}
        val b=j.optJSONArray("conversations") ?: JSONArray()
        mutableConversations.value=(0 until minOf(b.length(),30)).mapNotNull{i->runCatching{val x=b.getJSONObject(i);val text=x.getString("text").take(48000);val id=x.getString("id");conversationRecords[id]=x;SavedConversation(id,x.getString("title"),text,x.getLong("created"),ConversationHistory.decode(x.optJSONArray("turns"),text))}.getOrNull()}
    }}}
    fun expire(now:Long=System.currentTimeMillis()){mutableActive.value?.let{if(!it.pinned && now-it.created>=10*60000L)clearActive()}}
    private fun release(item:SharedContext){if(item.ownedFile.isBlank() || item.ownedFile==pinningFile || mutablePinned.value.any{it.ownedFile==item.ownedFile})return;val f=File(item.ownedFile);if(f.parentFile?.canonicalFile==File(context.noBackupFilesDir,"context-imports").canonicalFile)scope.launch{withContext(Dispatchers.IO){f.delete()}}}
    fun clearActive(){expiryJob?.cancel();val item=mutableActive.value;mutableActive.value=null;if(item!=null)release(item)}
    fun select(id:String){
        val previous=mutableActive.value
        val next=mutablePinned.value.firstOrNull{it.id==id}
        mutableActive.value=next
        if(previous!=null && previous.id!=next?.id)release(previous)
        scheduleExpiry()
    }
    fun setText(text:String,title:String="Texto seleccionado"):SharedContext {
        require(text.isNotBlank() && text.length<=16000)
        return SharedContext(java.util.UUID.randomUUID().toString(),title.take(100),if(text.startsWith("https://"))"link" else "text",text=text).also{mutableActive.value?.let{old->if(old.id!=it.id)release(old)};mutableActive.value=it;automaticId=null;scheduleExpiry()}
    }
    fun offerAutomatic(text:String) {
        if(!preferences.ready || preferences.isPrivate || text.isBlank() || text.length>8000) return
        val fingerprint=java.security.MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8)).joinToString(""){"%02x".format(it)}
        if(fingerprint==lastAutomaticFingerprint) return
        val current=mutableActive.value
        // Explicit selection (including documents) always has priority over an automatic offer.
        if(current!=null && (current.pinned || current.id!=automaticId)) return
        lastAutomaticFingerprint=fingerprint
        automaticId=setText(text,"Portapapeles").id
    }
    suspend fun setDocument(uri:Uri,ownedFile:String=""):SharedContext=withContext(Dispatchers.IO) {
        require(uri.scheme=="content")
        val suppliedMime=context.contentResolver.getType(uri).orEmpty()
        val title=runCatching{context.contentResolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use{if(it.moveToFirst())it.getString(0) else null}}.getOrNull() ?: "Documento autorizado"
        val mime=dev.bautista.spotlight.index.OfficeText.mime(title,suppliedMime).orEmpty()
        SharedContext(java.util.UUID.randomUUID().toString(),title.take(100),if(mime.startsWith("image/"))"image" else "document",uri=uri.toString(),mime=mime,ownedFile=ownedFile).also{mutableActive.value?.let{old->if(old.id!=it.id)release(old)};mutableActive.value=it;automaticId=null;scheduleExpiry()}
    }
    suspend fun text(item:SharedContext):String=withContext(Dispatchers.IO) {
        if(item.text.isNotBlank())item.text else {
            require(item.uri.startsWith("content://")){"Elegí nuevamente el documento."}
            dev.bautista.spotlight.index.DocumentText.extract(context,Uri.parse(item.uri),dev.bautista.spotlight.index.OfficeText.mime(item.title,item.mime)).take(24000).ifBlank{error("No se pudo obtener texto compatible de este archivo.")}
        }
    }
    suspend fun pinActive() {
        val requestedId=mutableActive.value?.id
        start();loadJob?.join()
        lock.withLock {
            val item=mutableActive.value?.takeIf{it.id==requestedId} ?: return@withLock
            val previous=mutablePinned.value
            check(previous.any{it.id==item.id} || previous.size<30){"Ya hay 30 contextos fijados. Eliminá uno antes de fijar otro."}
            val pinnedItem=item.copy(pinned=true)
            val next=listOf(pinnedItem)+previous.filterNot{it.id==item.id}
            currentCoroutineContext().ensureActive()
            // Once the atomic disk commit begins, finish disk + in-memory publication together.
            // A changing active context must not release a private imported file being pinned.
            withContext(NonCancellable) {
                pinningFile=item.ownedFile
                try {
                    save(next)
                    mutablePinned.value=next
                    if(mutableActive.value?.id==item.id){mutableActive.value=pinnedItem;scheduleExpiry()}
                } finally {
                    pinningFile=""
                    if(mutableActive.value?.ownedFile!=item.ownedFile)release(item)
                }
            }
        }
    }
    suspend fun removeContext(id:String) {
        start();loadJob?.join()
        lock.withLock {
            val previous=mutablePinned.value
            val removed=previous.firstOrNull{it.id==id} ?: return@withLock
            val next=previous.filterNot{it.id==id}
            currentCoroutineContext().ensureActive()
            withContext(NonCancellable) {
                // Keep old pins/resources visible until persistence succeeds. No rollback delete.
                save(next)
                mutablePinned.value=next
                contextRecords.remove(id)
                if(mutableActive.value?.id==id)clearActive() else release(removed)
            }
        }
    }
    suspend fun saveConversation(prompt:String,answer:String)=saveConversation(listOf("user" to prompt,"assistant" to answer))
    suspend fun saveConversation(turns:List<Pair<String,String>>) {
        val frozen=turns.map{it.first to it.second}
        require(ConversationHistory.valid(frozen)){"Guardá entre uno y seis intercambios completos, con hasta 48.000 caracteres."}
        start();loadJob?.join()
        lock.withLock {
            check(mutableConversations.value.size<30){"Ya hay 30 conversaciones guardadas. Eliminá una antes de guardar otra."}
            val item=SavedConversation(java.util.UUID.randomUUID().toString(),frozen.first().second.take(100),ConversationHistory.render(frozen),System.currentTimeMillis(),frozen)
            val previous=mutableConversations.value
            mutableConversations.value=listOf(item)+previous
            try{save()}catch(e:Exception){mutableConversations.value=previous;throw e}
        }
    }
    suspend fun deleteConversation(id:String) {
        start();loadJob?.join()
        lock.withLock {
            val previous=mutableConversations.value
            mutableConversations.value=previous.filterNot{it.id==id}
            try{save();conversationRecords.remove(id)}catch(e:Exception){mutableConversations.value=previous;throw e}
        }
    }
    private suspend fun save(contexts:List<SharedContext> = mutablePinned.value)=withContext(Dispatchers.IO) {
        val root=JSONObject(original.toString()).put("version",1)
        root.put("contexts",JSONArray().apply{contexts.forEach{put(JSONObject(contextRecords[it.id]?.toString() ?: "{}").put("id",it.id).put("title",it.title).put("kind",it.kind).put("text",it.text).put("uri",it.uri).put("mime",it.mime).put("created",it.created).put("ownedFile",it.ownedFile))}})
        root.put("conversations",JSONArray().apply{mutableConversations.value.forEach{item->put(JSONObject(conversationRecords[item.id]?.toString() ?: "{}").put("id",item.id).put("title",item.title).put("text",item.text).put("created",item.created).apply{if(item.turns.isNotEmpty())put("turns",ConversationHistory.encode(item.turns))})}})
        val out=file.startWrite();try{out.write(root.toString().toByteArray());file.finishWrite(out)}catch(e:Exception){file.failWrite(out);throw e}
    }
    fun results(query:String):List<Result> {
        val contexts=mutablePinned.value.mapNotNull{item->val score=relevance(query,item.title);if(score==0)null else Result("context:${item.id}",item.title,"Contexto fijado · activar y revisar","Contexto",score+450,Action.SystemPanel("context",item.id))}
        val chats=mutableConversations.value.mapNotNull{item->val score=relevance(query,item.title);if(score==0 && !normalize(query).contains("conversacion"))null else Result("conversation:${item.id}",item.title,"Conversación guardada manualmente · abrir","IA",score+500,Action.SystemPanel("conversation",item.id))}
        return contexts+chats
    }
}
