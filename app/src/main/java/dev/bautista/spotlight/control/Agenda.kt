package dev.bautista.spotlight.control

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import dev.bautista.spotlight.search.*
import java.time.*
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.*

object Agenda {
    fun range(period:String,today:LocalDate=LocalDate.now(),zone:ZoneId=ZoneId.systemDefault()):Pair<Long,Long> {
        val start=if(period=="tomorrow")today.plusDays(1) else today
        val end=start.plusDays(if(period=="week")7 else 1)
        return start.atStartOfDay(zone).toInstant().toEpochMilli() to end.atStartOfDay(zone).toInstant().toEpochMilli()
    }
    suspend fun search(context:Context,query:String,period:String):List<Result> = withContext(Dispatchers.IO) {
        if(ContextCompat.checkSelfPermission(context,Manifest.permission.READ_CALENDAR)!=PackageManager.PERMISSION_GRANTED)return@withContext listOf(Result("agenda:permission","Permitir acceso al calendario","Android pide este permiso una vez","Agenda",2000,Action.Access("calendar","agenda")))
        val (begin,end)=range(period)
        val builder=CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder,begin);ContentUris.appendId(builder,end)
        val out=mutableListOf<Result>()
        try { context.contentResolver.query(builder.build(),arrayOf(CalendarContract.Instances.EVENT_ID,CalendarContract.Instances.TITLE,CalendarContract.Instances.BEGIN,CalendarContract.Instances.END,CalendarContract.Instances.ALL_DAY,CalendarContract.Instances.EVENT_LOCATION),null,null,CalendarContract.Instances.BEGIN+" ASC")?.use { c ->
            while(c.moveToNext() && out.size<80) {
                ensureActive()
                val title=c.getString(1) ?: "Evento"
                if(query.isNotBlank()&&!normalize(title).contains(normalize(query)))continue
                val id=c.getLong(0);val time=c.getLong(2)
                val allDay=c.getInt(4)!=0
                val date=Instant.ofEpochMilli(time).atZone(if(allDay)ZoneOffset.UTC else ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(if(allDay)"dd/MM" else "dd/MM · HH:mm"))
                val location=c.getString(5).orEmpty()
                out+=Result("event:$id:$time",title,date+(if(allDay)" · Todo el día" else "")+if(location.isBlank())"" else " · $location","Agenda",1500-out.size,Action.Launch(Intent(Intent.ACTION_VIEW,ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,id))),preview="$title\n$date\n$location")
            }
        }
        } catch(e:CancellationException){throw e}
        catch(e:SecurityException){return@withContext listOf(Result("agenda:permission","Permitir acceso al calendario","El permiso ya no está disponible","Agenda",2000,Action.Access("calendar","agenda")))}
        catch(e:Exception){return@withContext listOf(Result("agenda:unavailable","Calendario no disponible","Abrir la app de calendario para crear un evento","Agenda",1000,Action.RestoreQuery("evento ")))}
        if(out.isEmpty())out+=Result("agenda:empty","No hay eventos en este período","Crear uno en tu calendario","Agenda",1000,Action.RestoreQuery("evento "))
        out
    }
}
