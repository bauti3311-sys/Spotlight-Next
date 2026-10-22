package dev.bautista.spotlight.ui

/** Overlay-local completed task snapshots. No disk, provider calls, credentials or automatic resume. */
internal data class InspectorAiSnapshot(val text:String,val turns:List<Pair<String,String>>,val messageTarget:String)
internal class InspectorAiSessions {
    private val entries=linkedMapOf<String,InspectorAiSnapshot>()
    fun get(key:String)=entries[key]
    fun remove(key:String){entries.remove(key)}
    fun put(key:String,value:InspectorAiSnapshot) {
        if(value.text.isBlank() || value.turns.lastOrNull()!=("assistant" to value.text))return
        if(key.length+value.text.length+value.turns.sumOf{it.second.length}>64000)return
        entries.remove(key);entries[key]=value.copy(turns=value.turns.toList())
        while(entries.size>4)entries.remove(entries.keys.first())
    }
}
