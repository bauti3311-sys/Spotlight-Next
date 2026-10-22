package dev.bautista.spotlight.ui

/** Overlay-local completed task snapshots. Bounded saved values; no provider calls, credentials or automatic resume. */
internal data class InspectorAiSnapshot(val text:String,val turns:List<Pair<String,String>>,val messageTarget:String,val interrupted:Boolean=false)
internal class InspectorAiSessions {
    fun saved():String=org.json.JSONArray().apply{entries.forEach{(key,item)->put(org.json.JSONObject().put("key",key).put("text",item.text).put("target",item.messageTarget).put("interrupted",item.interrupted).put("turns",org.json.JSONArray(item.turns.map{listOf(it.first,it.second)})))}}.toString()
    companion object {
        fun restore(raw:String)=InspectorAiSessions().apply {
            require(raw.length<=200000);val a=org.json.JSONArray(raw);require(a.length()<=4)
            for(i in 0 until a.length()) {val j=a.getJSONObject(i);val t=j.getJSONArray("turns");require(t.length()<=12 && t.length()%2==0)
                val turns=(0 until t.length()).map{n->val p=t.getJSONArray(n);p.getString(0) to p.getString(1)}
                require(turns.withIndex().all{(n,p)->p.first==if(n%2==0)"user" else "assistant"})
                put(j.getString("key"),InspectorAiSnapshot(j.getString("text"),turns,j.getString("target"),j.optBoolean("interrupted"))))
            }
        }
    }
    private val entries=linkedMapOf<String,InspectorAiSnapshot>()
    fun get(key:String)=entries[key]
    fun remove(key:String){entries.remove(key)}
    fun put(key:String,value:InspectorAiSnapshot) {
        if(value.text.isBlank() || value.turns.lastOrNull()!=("assistant" to value.text))return
        if(key.length+value.text.length+value.turns.sumOf{it.second.length}>64000)return
        entries.remove(key);entries[key]=value.copy(turns=value.turns.toList())
        while(entries.size>4 || entries.entries.sumOf{it.key.length+it.value.text.length+it.value.turns.sumOf{turn->turn.second.length}}>96000)entries.remove(entries.keys.first())
    }
}

internal val InspectorAiSessionsSaver=androidx.compose.runtime.saveable.Saver<InspectorAiSessions,String>(save={it.saved()},restore={runCatching{InspectorAiSessions.restore(it)}.getOrDefault(InspectorAiSessions())})
internal fun taskFingerprint(value:String)=java.security.MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8)).joinToString(""){"%02x".format(it)}
