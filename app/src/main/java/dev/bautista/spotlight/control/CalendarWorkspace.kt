package dev.bautista.spotlight.control

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.*
import kotlinx.coroutines.*

data class CalendarEntry(val id:Long,val title:String,val start:Long,val end:Long,val calendar:Long,val allDay:Boolean=false,val recurring:Boolean=false)
data class CalendarTarget(val id:Long,val title:String)
data class TimeBlock(val start:Long,val end:Long)
object Availability {
    fun conflicts(start:Long,end:Long,busy:List<TimeBlock>)=busy.filter{it.start<end && it.end>start}
    fun alternatives(start:Long,end:Long,busy:List<TimeBlock>,limit:Long,max:Int=3):List<TimeBlock> {
        require(end>start);val duration=end-start;var cursor=start;val out=mutableListOf<TimeBlock>()
        while(cursor+duration<=limit && out.size<max){val hit=conflicts(cursor,cursor+duration,busy);if(hit.isEmpty()){out+=TimeBlock(cursor,cursor+duration);cursor+=duration}else cursor=hit.maxOf{it.end}.coerceAtLeast(cursor+60000)}
        return out
    }
}
class CalendarWorkspace(private val context:Context) {
    fun readable()=ContextCompat.checkSelfPermission(context,Manifest.permission.READ_CALENDAR)==PackageManager.PERMISSION_GRANTED
    fun writable()=ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_CALENDAR)==PackageManager.PERMISSION_GRANTED
    suspend fun events(start:Long,end:Long):List<CalendarEntry> = withContext(Dispatchers.IO) {
        check(readable()){"Falta permiso de calendario"}
        val uri=CalendarContract.Instances.CONTENT_URI.buildUpon().also{ContentUris.appendId(it,start);ContentUris.appendId(it,end)}.build()
        val out=mutableListOf<CalendarEntry>()
        context.contentResolver.query(uri,arrayOf("event_id","title","begin","end","calendar_id","allDay","rrule","rdate"),null,null,"begin ASC")?.use { c->
            while(c.moveToNext() && out.size<500){
                ensureActive()
                val allDay=c.getInt(5)!=0
                fun localTime(value:Long)=if(allDay)Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() else value
                val begin=localTime(c.getLong(2));val finish=localTime(c.getLong(3))
                if(begin<end && finish>start)out+=CalendarEntry(c.getLong(0),c.getString(1).orEmpty().ifBlank{"Evento"},begin,finish,c.getLong(4),allDay,!c.getString(6).isNullOrBlank() || !c.getString(7).isNullOrBlank())
            }
        } ?: error("El proveedor de calendario no está disponible")
        check(out.size<500){"Demasiados eventos; elegí un período más corto"}
        out
    }
    suspend fun targets():List<CalendarTarget> = withContext(Dispatchers.IO){
        check(readable());val out=mutableListOf<CalendarTarget>()
        context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI,arrayOf("_id","calendar_displayName"),"calendar_access_level >= ? AND sync_events = 1",arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString()),null)?.use{c->while(c.moveToNext()){ensureActive();out+=CalendarTarget(c.getLong(0),c.getString(1).orEmpty())}}
        out
    }
    suspend fun save(original:CalendarEntry?,calendar:Long,title:String,start:Long,end:Long):Long=withContext(Dispatchers.IO){
        check(writable()){"Falta permiso de escritura"};require(title.isNotBlank() && title.length<=200 && end>start)
        require(original?.recurring!=true && original?.allDay!=true){"Editá eventos recurrentes o de todo el día en su app de calendario"}
        check(targets().any{it.id==calendar}){"El calendario no permite editar eventos"}
        val values=ContentValues().apply{put(CalendarContract.Events.CALENDAR_ID,calendar);put(CalendarContract.Events.TITLE,title);put(CalendarContract.Events.DTSTART,start);put(CalendarContract.Events.DTEND,end);put(CalendarContract.Events.EVENT_TIMEZONE,ZoneId.systemDefault().id);put(CalendarContract.Events.ALL_DAY,0)}
        if(original==null)ContentUris.parseId(context.contentResolver.insert(CalendarContract.Events.CONTENT_URI,values) ?: error("No se pudo guardar"))
        else {check(context.contentResolver.update(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,original.id),values,null,null)==1){"El evento cambió o ya no existe"};original.id}
    }
    suspend fun delete(entry:CalendarEntry)=withContext(Dispatchers.IO){check(writable());require(!entry.recurring){"Gestioná la serie en su app de calendario"};check(context.contentResolver.delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,entry.id),null,null)==1){"No se pudo eliminar el evento"}}
    fun externalEdit(entry:CalendarEntry?)=if(entry==null)Intent(Intent.ACTION_INSERT,CalendarContract.Events.CONTENT_URI) else Intent(Intent.ACTION_EDIT,ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,entry.id))
}

data class NaturalAvailability(val day:LocalDate,val start:LocalTime,val end:LocalTime)
data class NaturalCalendarDraft(val title:String,val day:LocalDate,val hour:Int,val minute:Int=0)
object CalendarLanguage {
    private val weekdays=mapOf("lunes" to DayOfWeek.MONDAY,"martes" to DayOfWeek.TUESDAY,"miercoles" to DayOfWeek.WEDNESDAY,"jueves" to DayOfWeek.THURSDAY,"viernes" to DayOfWeek.FRIDAY,"sabado" to DayOfWeek.SATURDAY,"domingo" to DayOfWeek.SUNDAY)
    fun free(raw:String,today:LocalDate=LocalDate.now()):NaturalAvailability? {
        val q=dev.bautista.spotlight.search.normalize(raw)
        if(!Regex("^(?:¿?tengo(?: tiempo)? libre|(?:busca|buscar|buscame) (?:una )?(?:hora|horario|hueco) libre)\\b").containsMatchIn(q.trim()))return null
        val day=if(q.contains("manana"))today.plusDays(1) else weekdays.entries.firstOrNull{Regex("\\b"+it.key+"\\b").containsMatchIn(q)}?.value?.let{today.with(java.time.temporal.TemporalAdjusters.nextOrSame(it))} ?: today
        val clocks=Regex("(\\d{1,2})(?::(\\d{2}))? +a +(\\d{1,2})(?::(\\d{2}))?").find(q)
        if(clocks==null)return NaturalAvailability(day,LocalTime.of(9,0),LocalTime.of(10,0))
        return runCatching{val start=LocalTime.of(clocks.groupValues[1].toInt(),clocks.groupValues[2].toIntOrNull() ?: 0);val end=LocalTime.of(clocks.groupValues[3].toInt(),clocks.groupValues[4].toIntOrNull() ?: 0);require(end>start);NaturalAvailability(day,start,end)}.getOrNull()
    }
    fun draft(raw:String,today:LocalDate=LocalDate.now()):NaturalCalendarDraft? {
        val match=Regex("^(?:agend[aá]|agendar|crear evento|cre[aá] evento) (.+?) (?:el )?(hoy|ma[nñ]ana|lunes|martes|mi[eé]rcoles|jueves|viernes|s[aá]bado|domingo) a las (\\d{1,2})(?::(\\d{2}))?$",RegexOption.IGNORE_CASE).matchEntire(raw.trim()) ?: return null
        val word=dev.bautista.spotlight.search.normalize(match.groupValues[2]);val hour=match.groupValues[3].toInt();val minute=match.groupValues[4].toIntOrNull() ?: 0
        if(hour !in 0..23 || minute !in 0..59)return null
        val day=when(word){"hoy"->today;"manana"->today.plusDays(1);else->today.with(java.time.temporal.TemporalAdjusters.nextOrSame(weekdays.getValue(word)))}
        return NaturalCalendarDraft(match.groupValues[1],day,hour,minute)
    }
}
