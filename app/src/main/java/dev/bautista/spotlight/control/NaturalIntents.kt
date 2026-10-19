package dev.bautista.spotlight.control

import android.content.*
import android.net.Uri
import android.provider.CalendarContract
import dev.bautista.spotlight.search.*
import java.time.*

object NaturalIntents {
    fun callName(q:String):String?=Regex("^llamar(?: a)? (.+)$",RegexOption.IGNORE_CASE).matchEntire(q.trim())?.groupValues?.get(1)
    suspend fun calls(context:Context,name:String):List<Result> {
        val number=runCatching{People.dialIntent(name)}.getOrNull()
        if(number!=null) {
            if(number.resolveActivity(context.packageManager)==null)return listOf(Result("call:unavailable","Marcador no disponible","Copiar número: $name","Contactos",2400,Action.Copy(name)))
            return listOf(Result("call:number:${number.data}","Preparar llamada",name+" · revisar en Teléfono","Contactos",2400,Action.Launch(number)))
        }
        if(!People.allowed(context))return listOf(Result("calls:permission","Permitir contactos","Elegir el destinatario de la llamada","Contactos",2400,Action.Access("contacts","llamar a $name")))
        return People.find(context,name).map{person->Result("call:${person.id}:${person.address}","Llamar a ${person.name}",person.address+" · revisar en Teléfono","Contactos",2400,Action.Launch(Intent(Intent.ACTION_DIAL,Uri.fromParts("tel",person.address,null))))}.ifEmpty{listOf(Result("call:empty","No se encontró ese contacto",name,"Contactos",2400,Action.RestoreQuery(name)))}
    }
    fun event(q:String,today:LocalDate=LocalDate.now(),zone:ZoneId=ZoneId.systemDefault()):Intent? {
        val m=Regex("^(?:crear|nuevo) evento (hoy|manana) a las (\\d{1,2})(?::(\\d{2}))?(?: (.+))?$").matchEntire(normalize(q.trim())) ?: return null
        val hour=m.groupValues[2].toInt();val minute=m.groupValues[3].ifBlank{"0"}.toInt();if(hour !in 0..23 || minute !in 0..59)return null
        val day=if(m.groupValues[1]=="manana")today.plusDays(1) else today
        val begin=day.atTime(hour,minute).atZone(zone).toInstant().toEpochMilli()
        return Intent(Intent.ACTION_INSERT,CalendarContract.Events.CONTENT_URI).putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,begin).putExtra(CalendarContract.EXTRA_EVENT_END_TIME,begin+3600000).putExtra(CalendarContract.Events.TITLE,m.groupValues[4].ifBlank{"Nuevo evento"})
    }
}
