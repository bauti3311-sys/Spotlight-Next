package dev.bautista.spotlight.next

import android.content.Context
import org.json.*

data class SportsEntity(val id:String,val name:String,val kind:String,val sport:String,val country:String="")
/** New entity storage does not reinterpret historical favorite event IDs as team IDs. */
class SportsFavorites(context:Context) {
    private val prefs=context.getSharedPreferences("sports_native",Context.MODE_PRIVATE)
    fun list():List<SportsEntity> = runCatching {val a=JSONArray(prefs.getString("entities_v2","[]"));(0 until minOf(a.length(),8)).map {val j=a.getJSONObject(it);SportsEntity(j.getString("id"),j.getString("name"),j.getString("kind"),j.getString("sport"),j.optString("country"))}.filter {valid(it)}}.getOrDefault(emptyList())
    fun save(items:List<SportsEntity>){require(items.size<=8 && items.all(::valid));prefs.edit().putString("entities_v2",JSONArray().apply{items.distinctBy{it.kind+it.id}.forEach{put(JSONObject().put("id",it.id).put("name",it.name).put("kind",it.kind).put("sport",it.sport).put("country",it.country))}}.toString()).apply()}
    companion object {
        fun valid(e:SportsEntity)=e.id.matches(Regex("[0-9]{1,12}")) && e.name.length in 1..100 && e.kind in setOf("team","league") && e.sport in setOf("Soccer","Basketball","Motorsport","Tennis","Baseball")
        fun matches(e:SportsEntity,event:JSONObject)=if(e.kind=="league")event.optString("idLeague")==e.id else event.optString("idHomeTeam")==e.id || event.optString("idAwayTeam")==e.id
        fun events(e:SportsEntity,j:JSONObject):List<JSONObject>{val a=j.optJSONArray("events") ?: j.optJSONArray("results") ?: JSONArray();return (0 until a.length()).mapNotNull{a.optJSONObject(it)}.filter{matches(e,it)}}
    }
}
