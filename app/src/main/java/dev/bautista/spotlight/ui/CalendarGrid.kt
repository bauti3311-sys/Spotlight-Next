package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.control.CalendarEntry
import java.time.*

@Composable fun CalendarGrid(day:LocalDate,period:String,events:List<CalendarEntry>,onDay:(LocalDate)->Unit) {
    val start=if(period=="month")day.withDayOfMonth(1) else day
    val days=if(period=="month")day.lengthOfMonth()else 7
    val offset=if(period=="month")start.dayOfWeek.value-1 else 0
    val cells=(List<LocalDate?>(offset){null}+(0 until days).map{start.plusDays(it.toLong())}).let{it+List<LocalDate?>((7-it.size%7)%7){null}}
    Column(Modifier.fillMaxWidth()){
        if(period=="month")Row{listOf("L","M","M","J","V","S","D").forEach{Text(it,Modifier.weight(1f),style=MaterialTheme.typography.labelSmall)}}
        cells.chunked(7).forEach{row->Row(Modifier.fillMaxWidth()){row.forEach{date->TextButton(enabled=date!=null,onClick={date?.let(onDay)},modifier=Modifier.weight(1f).heightIn(min=48.dp)){
            val count=if(date==null)0 else events.count{Instant.ofEpochMilli(it.start).atZone(ZoneId.systemDefault()).toLocalDate()==date}
            Text(if(date==null)""else "${date.dayOfMonth}"+if(count>0)" · $count"else "",style=MaterialTheme.typography.labelSmall)
        }}}}
    }
}
