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

data class SharedContext(val id:String,val title:String,val kind:String,val text:String="",val uri:String="",val mime:String="",val created:Long=System.currentTimeMillis(),val pinned:Boolean=false)
data class SavedConversation(val id:String,val title:String,val text:String,val created:Long)

/** Explicit Android inputs only. Temporary context expires; saved items never enter diagnostics. */
class ContextStore(private val context:Context,private val scope:CoroutineScope,private val preferences:Preferences) {
    private val file=AtomicFile(File(context.noBackupFilesDir,"spotlight-context-v1.json"))
    private val lock=Mutex()
    private var started=false
    private val mutableActive=MutableStateFlow<SharedContext?>(null)
    val active=mutableActive.asStateFlow()
    private val mutablePinned=MutableStateFlow<List<SharedContext>>(emptyList())
    val pinned=mutablePinned.asStateFlow()
    private val mutableConversations=MutableStateFlow<List<SavedConversation>>(emptyList())
    val conversations=mutableConversations.asStateFlow()
    fun start(){if(started)return;started=true;scope.launch {lock.withLock{
        val j=runCatching{JSONObject(file.readFully().toString(Charsets.UTF_8))}.getOrNull() ?: return@withLock
        val a=j.optJSONArray("contexts") ?: JSONArray()
        mutablePinned.value=(0 until minOf(a.length(),30)).mapNotNull {i->runCatching{val x=a.getJSONObject(i);SharedContext(x.getString("id"),x.getString("title"),x.getString("kind"),x.optString("text").take(16000),x.optString("uri"),x.optString("mime"),x.getLong("created"),true)}.getOrNull()}
        val b=j.optJSONArray("conversations") ?: JSONArray()
        mutableConversations.value=(0 until minOf(b.length(),30)).mapNotNull{i->runCatching{val x=b.getJSONObject(i);SavedConversation(x.getString("id"),x.getString("title"),x.getString("text").take(48000),x.getLong("created"))}.getOrNull()}
    }};scope.launch{while(isActive){delay(60000);expire()}}}
    fun expire(now:Long=System.currentTimeMillis()){mutableActive.value?.let{if(!it.pinned && now-it.created>10*60000L)mutableActive.value=null}}
    fun clearActive(){mutableActive.value=null}
    fun select(id:String){mutableActive.value=mutablePinned.value.firstOrNull{it.id==id}}
    fun setText(text:String,title:String="Texto seleccionado"):SharedContext {
        require(text.isNotBlank() && text.length<=16000)
        return SharedContext(java.util.UUID.randomUUID().toString(),title.take(100),if(text.startsWith("https://"))"link" else "text",text=text).also{mutableActive.value=it}
    }
    fun offerAutomatic(text:String){if(!preferences.isPrivate && mutableActive.value?.pinned!=true && text.isNotBlank() && text.length<=8000)setText(text,"Portapapeles autorizado")}
    suspend fun setDocument(uri:Uri):SharedContext=withContext(Dispatchers.IO) {
        require(uri.scheme=="content")
        val mime=context.contentResolver.getType(uri).orEmpty()
        val title=runCatching{context.contentResolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use{if(it.moveToFirst())it.getString(0) else null}}.getOrNull() ?: "Documento autorizado"
        SharedContext(java.util.UUID.randomUUID().toString(),title.take(100),if(mime.startsWith("image/"))"image" else "document",uri=uri.toString(),mime=mime).also{mutableActive.value=it}
    }
    suspend fun text(item:SharedContext):String=withContext(Dispatchers.IO) {
        if(item.text.isNotBlank())item.text else {
            require(item.uri.startsWith("content://")){"Elegí nuevamente el documento."}
            dev.bautista.spotlight.index.DocumentText.extract(context,Uri.parse(item.uri),item.mime).take(24000).ifBlank{error("No se pudo obtener texto compatible de este archivo.")}
        }
    }
    suspend fun pinActive()=lock.withLock {
        val item=mutableActive.value ?: return@withLock
        val pinnedItem=item.copy(pinned=true)
        mutablePinned.value=(listOf(pinnedItem)+mutablePinned.value.filterNot{it.id==item.id}).take(30)
        mutableActive.value=pinnedItem;save()
    }
    suspend fun removeContext(id:String)=lock.withLock {mutablePinned.value=mutablePinned.value.filterNot{it.id==id};if(mutableActive.value?.id==id)clearActive();save()}
    suspend fun saveConversation(prompt:String,answer:String)=lock.withLock {
        require(answer.isNotBlank())
        val item=SavedConversation(java.util.UUID.randomUUID().toString(),prompt.take(100).ifBlank{"Conversación guardada"},("Usuario: $prompt\n\nSpotlight: $answer").take(48000),System.currentTimeMillis())
        mutableConversations.value=(listOf(item)+mutableConversations.value).take(30);save()
    }
    suspend fun deleteConversation(id:String)=lock.withLock {mutableConversations.value=mutableConversations.value.filterNot{it.id==id};save()}
    private suspend fun save()=withContext(Dispatchers.IO) {
        val root=JSONObject().put("version",1)
        root.put("contexts",JSONArray().apply{mutablePinned.value.forEach{put(JSONObject().put("id",it.id).put("title",it.title).put("kind",it.kind).put("text",it.text).put("uri",it.uri).put("mime",it.mime).put("created",it.created))}})
        root.put("conversations",JSONArray().apply{mutableConversations.value.forEach{put(JSONObject().put("id",it.id).put("title",it.title).put("text",it.text).put("created",it.created))}})
        val out=file.startWrite();try{out.write(root.toString().toByteArray());file.finishWrite(out)}catch(e:Exception){file.failWrite(out);throw e}
    }
    fun results(query:String):List<Result> {
        val contexts=mutablePinned.value.mapNotNull{item->val score=relevance(query,item.title);if(score==0)null else Result("context:${item.id}",item.title,"Contexto fijado · activar y revisar","Contexto",score+450,Action.SystemPanel("context",item.id))}
        val chats=mutableConversations.value.mapNotNull{item->val score=relevance(query,item.title);if(score==0 && !normalize(query).contains("conversacion"))null else Result("conversation:${item.id}",item.title,"Conversación guardada manualmente · abrir","IA",score+500,Action.SystemPanel("conversation",item.id))}
        return contexts+chats
    }
}
