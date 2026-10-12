package dev.bautista.spotlight.ui

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.*
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.normalize
import kotlinx.coroutines.*
import java.time.*
import java.time.format.DateTimeFormatter

@Composable fun CalendarPanel(app:SpotlightApp,initial:String,onAction:(Action)->Unit) {
    val repo=remember{CalendarWorkspace(app)};val scope=rememberCoroutineScope();val zone=ZoneId.systemDefault();val fmt=DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val draft=remember(initial){CalendarLanguage.draft(initial)}
    val free=remember(initial){CalendarLanguage.free(initial)}
    var availability by remember(initial){mutableStateOf(free!=null)}
    var day by remember(initial){mutableStateOf(draft?.day ?: free?.day ?: if(normalize(initial).contains("manana"))LocalDate.now().plusDays(1) else LocalDate.now())}
    var period by remember{mutableStateOf("day")};var filter by remember{mutableStateOf("")};var revision by remember{mutableIntStateOf(0)}
    var events by remember{mutableStateOf<List<CalendarEntry>>(emptyList())};var targets by remember{mutableStateOf<List<CalendarTarget>>(emptyList())}
    var selected by remember{mutableStateOf<CalendarEntry?>(null)};var editor by remember(initial){mutableStateOf(draft!=null || availability)};var title by remember(initial){mutableStateOf(draft?.title.orEmpty())}
    var date by remember(initial){mutableStateOf(day.format(fmt))};var time by remember(initial){mutableStateOf((free?.start ?: LocalTime.of(draft?.hour ?: 16,draft?.minute ?: 0)).toString())};var duration by remember{mutableStateOf(free?.let{Duration.between(it.start,it.end).toMinutes().toString()} ?: "60")};var calendar by remember{mutableStateOf<Long?>(null)}
    var review by remember{mutableStateOf(false)};var deleting by remember{mutableStateOf(false)};var busy by remember{mutableStateOf(false)};var error by remember{mutableStateOf("")}
    LaunchedEffect(day,period,revision){if(!repo.readable())return@LaunchedEffect;busy=true;error="";events=emptyList();try{val start=if(period=="month")day.withDayOfMonth(1) else day;val end=when(period){"month"->start.plusMonths(1);"week"->start.plusDays(7);else->start.plusDays(1)};events=repo.events(start.atStartOfDay(zone).toInstant().toEpochMilli(),end.atStartOfDay(zone).toInstant().toEpochMilli());targets=repo.targets();if(calendar==null)calendar=targets.firstOrNull()?.id}catch(e:CancellationException){throw e}catch(e:Exception){error="No se pudo consultar el calendario. Revisá permisos y cuenta sincronizada."}finally{busy=false}}
    fun stamp(value:Long)=Instant.ofEpochMilli(value).atZone(zone).format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
    val start=runCatching{LocalDate.parse(date,fmt).atTime(LocalTime.parse(time)).atZone(zone).toInstant().toEpochMilli()}.getOrNull()
    val minutes=duration.toLongOrNull()?.takeIf{it in 1..1440};val end=if(start!=null && minutes!=null)start+minutes*60000 else null
    Column(Modifier.fillMaxWidth().heightIn(max=440.dp).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Calendario",style=MaterialTheme.typography.titleLarge)
        if(!repo.readable()){Text("Consultá tus calendarios sincronizados mediante el permiso opcional de Android.");TextButton(onClick={onAction(Action.Access("calendar","calendario"))}){Text("Permitir calendario")};return@Column}
        Row(Modifier.horizontalScroll(rememberScrollState())){listOf("day" to "Día","week" to "Semana","month" to "Mes").forEach{(key,label)->FilterChip(period==key,{period=key},label={Text(label)})}}
        Row{TextButton(onClick={day=if(period=="month")day.minusMonths(1) else day.minusDays(if(period=="week")7 else 1)}){Text("Anterior")};Text(day.format(fmt));TextButton(onClick={day=if(period=="month")day.plusMonths(1) else day.plusDays(if(period=="week")7 else 1)}){Text("Siguiente")}}
        if(period!="day" && !editor)CalendarGrid(day,period,events){day=it;period="day"}
        TextButton(enabled=!busy,onClick={revision++}){Text("Actualizar calendario")}
        TextButton(onClick={availability=false;selected=null;editor=true;review=false;title="";date=day.format(fmt);time="16:00";duration="60"}){Text("Crear evento")}
        if(!editor){
            OutlinedTextField(filter,{filter=it.take(100)},label={Text("Buscar eventos")},singleLine=true,modifier=Modifier.fillMaxWidth())
            events.filter{filter.isBlank()||normalize(it.title).contains(normalize(filter))}.forEach{entry->TextButton(onClick={selected=entry},modifier=Modifier.fillMaxWidth()){Column(Modifier.fillMaxWidth()){Text(entry.title);Text(if(entry.allDay)"Todo el día" else stamp(entry.start)+" — "+stamp(entry.end),style=MaterialTheme.typography.bodySmall)}}}
            if(events.isEmpty() && !busy && error.isBlank())Text("No hay eventos en este período. Los horarios libres solo reflejan tus calendarios autorizados.")
            selected?.let{entry->HorizontalDivider();Text(entry.title,style=MaterialTheme.typography.titleMedium);Text(stamp(entry.start)+" — "+stamp(entry.end));Row{TextButton(onClick={editor=true;review=false;title=entry.title;date=Instant.ofEpochMilli(entry.start).atZone(zone).toLocalDate().format(fmt);time=Instant.ofEpochMilli(entry.start).atZone(zone).toLocalTime().withSecond(0).withNano(0).toString();duration=((entry.end-entry.start)/60000).toString();calendar=entry.calendar}){Text("Editar")};TextButton(onClick={deleting=true}){Text("Eliminar")};TextButton(onClick={onAction(Action.Launch(repo.externalEdit(entry)))}){Text("Editor del sistema")}}}
        } else {
            if(!availability)OutlinedTextField(title,{title=it.take(200);review=false},label={Text("Título")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(date,{date=it.take(10);review=false;runCatching{LocalDate.parse(date,fmt)}.getOrNull()?.let{day=it}},label={Text("Fecha · dd/mm/aaaa")},singleLine=true,modifier=Modifier.fillMaxWidth())
            Row{OutlinedTextField(time,{time=it.take(5);review=false},label={Text("Hora · 24 h")},singleLine=true,modifier=Modifier.weight(1f));OutlinedTextField(duration,{duration=it.filter(Char::isDigit).take(4);review=false},label={Text("Minutos")},singleLine=true,modifier=Modifier.weight(1f))}
            if(!availability)Row(Modifier.horizontalScroll(rememberScrollState())){targets.forEach{target->FilterChip(calendar==target.id,{calendar=target.id;review=false},label={Text(target.title)})}}
            if(!availability && !repo.writable())TextButton(onClick={onAction(Action.Access("calendar_write","calendario"))}){Text("Permitir cambios en calendario")}
            if(!availability)TextButton(onClick={onAction(Action.Launch(repo.externalEdit(selected).putExtra(android.provider.CalendarContract.Events.TITLE,title).also{if(start!=null)it.putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME,start);if(end!=null)it.putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME,end)}))}){Text("Usar editor del sistema")}
            Button(enabled=start!=null && end!=null && (availability || title.isNotBlank() && calendar!=null) && !busy && error.isBlank(),onClick={review=true}){Text("Revisar horarios")}
            if(review && start!=null && end!=null){
                val conflicts=events.filter{it.id!=selected?.id}.filter{it.start<end && it.end>start}
                Text("${if(availability)"Disponibilidad local" else title.trim()} · ${stamp(start)} — ${stamp(end)}")
                if(availability && conflicts.isEmpty())Text("Sin eventos superpuestos en tus calendarios autorizados. No indica disponibilidad de otras personas.")
                if(conflicts.isNotEmpty()){Text("Coincide con: "+conflicts.joinToString{it.title});val limit=LocalDate.parse(date,fmt).atTime(23,59).atZone(zone).toInstant().toEpochMilli();Availability.alternatives(start,end,events.filter{it.id!=selected?.id}.map{TimeBlock(it.start,it.end)},limit).forEach{slot->TextButton(onClick={time=Instant.ofEpochMilli(slot.start).atZone(zone).toLocalTime().withSecond(0).toString();review=false}){Text("Alternativa · ${stamp(slot.start)}")}}}
                if(!availability)Button(enabled=repo.writable() && !busy && selected?.recurring!=true && selected?.allDay!=true,onClick={busy=true;scope.launch{try{val latest=repo.events(start,end).filter{it.id!=selected?.id};if(latest.map{Triple(it.id,it.start,it.end)}.toSet()!=conflicts.map{Triple(it.id,it.start,it.end)}.toSet()){error="La agenda cambió. Volvé a revisar el horario.";review=false}else{repo.save(selected,calendar!!,title.trim(),start,end);editor=false;review=false;selected=null;revision++}}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message ?: "No se pudo guardar el evento"}finally{busy=false}}}){Text(if(conflicts.isEmpty())"Confirmar y guardar" else "Confirmar el horario original")}
            }
            TextButton(onClick={editor=false;review=false}){Text("Cancelar edición")}
        }
        if(deleting && selected!=null){Text("¿Eliminar ${selected!!.title} de tu calendario?");Row{TextButton(enabled=repo.writable() && !busy && !selected!!.recurring,onClick={busy=true;scope.launch{try{repo.delete(selected!!);selected=null;deleting=false;revision++}catch(e:CancellationException){throw e}catch(e:Exception){error=e.message.orEmpty()}finally{busy=false}}}){Text("Confirmar eliminación")};TextButton(onClick={deleting=false}){Text("Cancelar")}}}
        if(busy)LinearProgressIndicator(Modifier.fillMaxWidth());if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        Text("Calendarios del dispositivo · disponibilidad local. Series recurrentes y eventos de todo el día se editan en su app.",style=MaterialTheme.typography.labelSmall)
    }
}
