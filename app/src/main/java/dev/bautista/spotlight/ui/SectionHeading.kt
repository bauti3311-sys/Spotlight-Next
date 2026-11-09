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
    val appearance=LocalAppearance.current
    val d=GlassTokens.densityScale(appearance)
    Row(Modifier.fillMaxWidth().padding(start=GlassTokens.rowHorizontalPadding(appearance),end=8.dp,top=((if(first)4f else 10f)*d).dp,bottom=(4f*d).dp),verticalAlignment=Alignment.CenterVertically) {
        Text(name,fontSize=GlassTokens.scaledSp(11.sp,appearance),lineHeight=GlassTokens.scaledSp(14.sp,appearance,minSp=12f),letterSpacing=if(LocalPuenteUi.current).5.sp else 0.sp,fontWeight=if(LocalPuenteUi.current)androidx.compose.ui.text.font.FontWeight.Medium else null,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
