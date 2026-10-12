package dev.bautista.spotlight.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable fun WeatherChart(title:String,values:List<Float?>,unit:String,fixedMax:Float?=null) {
    val valid=values.filterNotNull().filter{it.isFinite()};if(valid.isEmpty())return
    val min=if(fixedMax!=null)0f else valid.min();val max=fixedMax ?: valid.max().coerceAtLeast(min+1f);val color=MaterialTheme.colorScheme.primary
    Text("$title · ${valid.min()}$unit — ${valid.max()}$unit",style=MaterialTheme.typography.labelMedium)
    Canvas(Modifier.fillMaxWidth().height(68.dp)) {
        val path=Path();var previous=false
        values.forEachIndexed{index,v->
            if(v==null || !v.isFinite()){previous=false;return@forEachIndexed}
            val x=size.width*index/(values.size-1).coerceAtLeast(1);val y=size.height*(1-(v-min)/(max-min).coerceAtLeast(1f))
            if(previous)path.lineTo(x,y)else path.moveTo(x,y);previous=true
            drawCircle(color,2.dp.toPx(),androidx.compose.ui.geometry.Offset(x,y))
        }
        drawPath(path,color,style=Stroke(2.dp.toPx()))
    }
}
