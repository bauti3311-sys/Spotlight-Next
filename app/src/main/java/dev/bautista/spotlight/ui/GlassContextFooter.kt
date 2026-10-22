package dev.bautista.spotlight.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

@Composable internal fun GlassKeycap(key:String) {
 val colors=MaterialTheme.colorScheme
 Text(key,Modifier.background(colors.onSurface.copy(alpha=.065f),RoundedCornerShape(5.dp))
  .border(1.dp,colors.onSurface.copy(alpha=.09f),RoundedCornerShape(5.dp)).padding(horizontal=7.dp,vertical=4.dp),
  fontSize=10.sp,lineHeight=14.sp,color=colors.onSurfaceVariant,maxLines=1)
}

/** Shared keycaps; hints describe actual Android contracts and retain 48dp touch targets. */
@Composable fun GlassContextFooter(suggestion:Boolean,enabled:Boolean,primaryLabel:String,onExecute:()->Unit,onActions:()->Unit,
 autocomplete:Boolean=false,expandLabel:String="Ver más resultados",onExpand:(()->Unit)?=null,actionsOpen:Boolean=false,actionsKeyLabel:String="Ctrl+K") {
 val colors=MaterialTheme.colorScheme
 BoxWithConstraints(Modifier.fillMaxWidth()) {
  val wide=maxWidth>=560.dp
  Row(Modifier.fillMaxWidth().heightIn(min=48.dp).padding(horizontal=10.dp),verticalAlignment=Alignment.CenterVertically) {
   if(enabled)TextButton(onClick=onExecute,enabled=!actionsOpen,contentPadding=PaddingValues(horizontal=6.dp),modifier=Modifier.weight(1f)) {
    GlassKeycap("Enter");Spacer(Modifier.width(8.dp));Text(if(actionsOpen)"Ejecutar" else primaryLabel,fontSize=11.sp,maxLines=1,overflow=TextOverflow.Ellipsis,color=colors.onSurface)
   } else Spacer(Modifier.weight(1f))
   if(onExpand!=null)TextButton(onClick=onExpand,modifier=Modifier.testTag("results-expand"),contentPadding=PaddingValues(horizontal=6.dp)) {
    Text(if(wide)expandLabel else if(expandLabel.startsWith("Ver menos"))"Menos" else "Más",fontSize=11.sp,color=colors.onSurfaceVariant)
   }
   if(enabled)TextButton(onClick=onActions,contentPadding=PaddingValues(horizontal=6.dp),modifier=Modifier.heightIn(min=48.dp)) {
    GlassKeycap(if(actionsOpen)"Escape" else actionsKeyLabel);Spacer(Modifier.width(8.dp));Text(if(actionsOpen)"Volver" else "Acciones",fontSize=11.sp,color=colors.onSurfaceVariant)
   }
   if(autocomplete && !actionsOpen && wide)Row(Modifier.padding(start=10.dp),verticalAlignment=Alignment.CenterVertically){GlassKeycap("Tab");Spacer(Modifier.width(8.dp));Text("Autocompletar",fontSize=11.sp,color=colors.onSurfaceVariant)}
  }
 }
}
