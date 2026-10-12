package dev.bautista.spotlight.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject

data class Note(val id: String, val text: String, val created: Long)

/** Text clipboard is kept only in process memory; notes are explicitly saved locally. */
class LocalStore(private val preferences: Preferences, private val scope: CoroutineScope) {
    companion object { fun clipId(text:String)="clip:"+java.security.MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8)).take(12).joinToString(""){"%02x".format(it)} }
    private val mutableClipFavorites=MutableStateFlow<Set<String>>(emptySet())
    val clipFavorites=mutableClipFavorites.asStateFlow()
    @Synchronized fun toggleClipFavorite(text:String){if(text in mutableClips.value)mutableClipFavorites.value=if(text in mutableClipFavorites.value)mutableClipFavorites.value-text else mutableClipFavorites.value+text}
    @Synchronized fun editClip(original:String,replacement:String){require(replacement.isNotBlank() && replacement.length<=8000){"Usá entre 1 y 8000 caracteres."};val favorite=original in mutableClipFavorites.value;deleteClip(original);capture(replacement);if(favorite)mutableClipFavorites.value=mutableClipFavorites.value+replacement}
    val notes =
        preferences.noteData
            .map { raw ->
                runCatching {
                        val a = JSONArray(raw)
                        (0 until a.length()).map { i ->
                            val n = a.getJSONObject(i)
                            Note(n.getString("id"), n.getString("text"), n.getLong("created"))
                        }
                    }
                    .getOrDefault(emptyList())
            }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())
    private val mutableClips = MutableStateFlow<List<String>>(emptyList())
    val clips = mutableClips.asStateFlow()
    private val expiry=mutableMapOf<String,Long>()
    private var limit=20;private var retention=15;private var expiryJob:Job?=null
    init {scope.launch {preferences.config.collect{c->limit=c.clipLimit.coerceIn(5,50);retention=c.clipRetention.coerceIn(1,60);prune()}}}
    @Synchronized private fun prune(){val now=System.currentTimeMillis();mutableClips.value=mutableClips.value.filter{(expiry[it] ?: 0)>now}.take(limit);expiry.keys.retainAll(mutableClips.value.toSet());mutableClipFavorites.value=mutableClipFavorites.value.intersect(mutableClips.value.toSet())}
    @Synchronized fun deleteClip(text:String){expiry.remove(text);mutableClipFavorites.value=mutableClipFavorites.value-text;mutableClips.value=mutableClips.value-text}

    @Synchronized fun capture(text: String) {
        if (preferences.isPrivate || text.isBlank() || text.length > 8000) return
        expiry[text]=System.currentTimeMillis()+retention*60000L
        mutableClips.value = (listOf(text) + mutableClips.value.filter { it != text }).take(limit)
        prune()
        if(expiryJob?.isActive!=true)expiryJob=scope.launch{while(mutableClips.value.isNotEmpty()){delay(60000);prune()}}
    }

    @Synchronized fun clearClips() {
        expiryJob?.cancel();expiry.clear();mutableClipFavorites.value=emptySet()
        mutableClips.value = emptyList()
    }

    suspend fun saveNote(text: String, id: String? = null) {
        if (text.isBlank()) return
        preferences.editNotes { raw ->
            val a = JSONArray(raw)
            val out = JSONArray()
            if(id != null) {
                for(i in 0 until a.length()) {
                    val note=a.getJSONObject(i)
                    if(note.getString("id")==id) note.put("text",text.take(8000)).put("modified",System.currentTimeMillis())
                    out.put(note)
                }
                return@editNotes out.toString()
            }
            out.put(
                JSONObject()
                    .put("id", java.util.UUID.randomUUID().toString())
                    .put("text", text.take(8000))
                    .put("created", System.currentTimeMillis())
            )
            for (i in 0 until minOf(a.length(), 99)) out.put(a.getJSONObject(i))
            out.toString()
        }
    }

    suspend fun deleteNote(id: String) {
        preferences.editNotes { raw ->
            val a = JSONArray(raw)
            val out = JSONArray()
            for (i in 0 until a.length()) {
                val n = a.getJSONObject(i)
                if (n.getString("id") != id) out.put(n)
            }
            out.toString()
        }
    }
}
