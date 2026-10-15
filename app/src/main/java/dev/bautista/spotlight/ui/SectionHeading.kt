package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun sectionIcon(name: String): ImageVector = when(name) {
    "Aplicaciones", "Apps" -> CommandIcons.Apps
    "Acciones y comandos", "Acciones", "Comandos" -> CommandIcons.Bolt
    "Contactos y comunicación", "Contactos" -> CommandIcons.Person
    "Archivos y carpetas", "Archivos" -> CommandIcons.Folder
    "Sistema", "Ajustes", "Ajustes del dispositivo" -> CommandIcons.Settings
    "Herramientas y conversiones", "Calculadora", "Conversión" -> CommandIcons.Calculate
    "Búsqueda web", "Web" -> CommandIcons.Language
    "IA" -> CommandIcons.AutoAwesome
    "Clima" -> CommandIcons.Cloud
    "Deportes en vivo" -> CommandIcons.Sport
    "Portapapeles" -> CommandIcons.ContentCopy
    "Notas" -> CommandIcons.EditNote
    "Agenda" -> CommandIcons.Event
    "Textos frecuentes" -> CommandIcons.Snippet
    "Fijados", "Consultas" -> CommandIcons.Pin
    "Recientes" -> CommandIcons.History
    "Sugerencias" -> CommandIcons.Star
    "Rutinas", "Automatizaciones" -> CommandIcons.Layers
    else -> CommandIcons.TravelExplore
}

@Composable fun SectionHeading(name: String,first:Boolean=false) {
    Row(Modifier.fillMaxWidth().padding(start=10.dp,end=8.dp,top=if(first)7.dp else 14.dp,bottom=6.dp),verticalAlignment=Alignment.CenterVertically) {
        Text(name,fontSize=11.sp,lineHeight=15.sp,letterSpacing=0.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
