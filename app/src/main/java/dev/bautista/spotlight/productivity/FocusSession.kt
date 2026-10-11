package dev.bautista.spotlight.productivity
import android.content.Context
import android.os.SystemClock
import android.provider.Settings

data class FocusState(val status:String="idle",val phase:String="work",val remaining:Long=0,val completed:Int=0,val stats:Boolean=false)
class FocusSession(context:Context) {
 private val prefs=context.getSharedPreferences("focus_session",Context.MODE_PRIVATE)
 private val resolver=context.contentResolver
 private fun boot()=runCatching{Settings.Global.getInt(resolver,Settings.Global.BOOT_COUNT,-1)}.getOrDefault(-1)
 @Synchronized fun snapshot(now:Long=SystemClock.elapsedRealtime()):FocusState {
  var status=prefs.getString("status","idle").orEmpty();val phase=prefs.getString("phase","work").orEmpty();val stats=prefs.getBoolean("stats",false)
  var completed=prefs.getInt("completed",0);val remaining=if(status=="running"){
   if(prefs.getInt("boot",-1)!=boot() || now<prefs.getLong("started",0)){status="idle";prefs.edit().putString("status",status).apply();0L}else (prefs.getLong("deadline",now)-now).coerceAtLeast(0)
  }else prefs.getLong("remaining",0)
  if(status=="running" && remaining==0L){status="finished";if(stats && phase=="work")completed++;prefs.edit().putString("status",status).putLong("remaining",0).putInt("completed",completed).apply()}
  return FocusState(status,phase,remaining,completed,stats)
 }
 @Synchronized fun start(minutes:Int,phase:String="work",now:Long=SystemClock.elapsedRealtime()){require(minutes in 1..180 && phase in setOf("work","break"));prefs.edit().putString("status","running").putString("phase",phase).putLong("started",now).putLong("deadline",now+minutes*60000L).putInt("boot",boot()).apply()}
 @Synchronized fun pause(now:Long=SystemClock.elapsedRealtime()){val s=snapshot(now);if(s.status=="running")prefs.edit().putString("status","paused").putLong("remaining",s.remaining).apply()}
 @Synchronized fun resume(now:Long=SystemClock.elapsedRealtime()){val s=snapshot(now);if(s.status=="paused" && s.remaining>0)prefs.edit().putString("status","running").putLong("started",now).putLong("deadline",now+s.remaining).putInt("boot",boot()).apply()}
 fun stop(){prefs.edit().putString("status","idle").putLong("remaining",0).apply()}
 fun statistics(enabled:Boolean){prefs.edit().putBoolean("stats",enabled).apply()}
 fun clearStatistics(){prefs.edit().putInt("completed",0).apply()}
}
