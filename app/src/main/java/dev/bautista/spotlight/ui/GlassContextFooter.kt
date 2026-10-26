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

/** A single command dock; every label retains its existing executor and 48dp target. */
@Composable fun GlassContextFooter(suggestion:Boolean,enabled:Boolean,primaryLabel:String,onExecute:()->Unit,onActions:()->Unit,
 autocomplete:Boolean=false,expandLabel:String="Ver más resultados",onExpand:(()->Unit)?=null,actionsOpen:Boolean=false,actionsKeyLabel:String="Ctrl+K") {
 GlassContextFooter(suggestion,enabled,primaryLabel,onExecute,onActions,autocomplete,expandLabel,onExpand,actionsOpen,actionsKeyLabel,onOptions=null)
}

@Composable fun GlassContextFooter(suggestion:Boolean,enabled:Boolean,primaryLabel:String,onExecute:()->Unit,onActions:()->Unit,
 autocomplete:Boolean=false,expandLabel:String="Ver más resultados",onExpand:(()->Unit)?=null,actionsOpen:Boolean=false,actionsKeyLabel:String="Ctrl+K",
 onOptions:(()->Unit)?,scopeLabel:String="Spotlight") {
 val colors=MaterialTheme.colorScheme
 BoxWithConstraints(Modifier.fillMaxWidth().testTag("context-footer")) {
  val wide=maxWidth>=700.dp
  Row(Modifier.fillMaxWidth().heightIn(min=56.dp).padding(horizontal=10.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically) {
   onOptions?.let { options ->
    IconButton(onClick=options,modifier=Modifier.size(48.dp).testTag("command-dock-menu")) {
     IosCommandIcon(CommandIcons.MoreHoriz,"Opciones de Spotlight",Modifier.size(20.dp),tint=colors.onSurfaceVariant)
    }
   }
   if(wide)Text(scopeLabel,Modifier.widthIn(max=160.dp),fontSize=11.sp,color=colors.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
   if(wide || !enabled)Spacer(Modifier.weight(1f))
   if(onExpand!=null)TextButton(onClick=onExpand,modifier=Modifier.testTag("results-expand"),contentPadding=PaddingValues(horizontal=6.dp)) {
    Text(if(wide)expandLabel else if(expandLabel.startsWith("Ver menos"))"Menos" else "Más",fontSize=11.sp,color=colors.onSurfaceVariant)
   }
   if(autocomplete && !actionsOpen && wide)Row(Modifier.padding(horizontal=10.dp),verticalAlignment=Alignment.CenterVertically){Text("Autocompletar",fontSize=11.sp,color=colors.onSurfaceVariant);Spacer(Modifier.width(8.dp));GlassKeycap("Tab")}
   if(enabled)Row(Modifier.then(if(wide)Modifier else Modifier.weight(1f)).testTag("command-dock-actions").background(colors.onSurface.copy(alpha=.045f),RoundedCornerShape(24.dp))
    .border(1.dp,colors.onSurface.copy(alpha=.10f),RoundedCornerShape(24.dp)),verticalAlignment=Alignment.CenterVertically) {
    TextButton(onClick=onExecute,enabled=!actionsOpen,contentPadding=PaddingValues(horizontal=12.dp),modifier=Modifier.then(if(wide)Modifier.widthIn(max=220.dp) else Modifier.weight(1f)).heightIn(min=48.dp)) {
     Text(if(actionsOpen)"Ejecutar" else primaryLabel,fontSize=12.sp,maxLines=1,overflow=TextOverflow.Ellipsis,color=colors.onSurface)
     if(wide){Spacer(Modifier.width(8.dp));GlassKeycap("Enter")}
    }
    Box(Modifier.width(1.dp).height(18.dp).background(colors.onSurface.copy(alpha=.12f)))
    TextButton(onClick=onActions,contentPadding=PaddingValues(horizontal=12.dp),modifier=Modifier.heightIn(min=48.dp)) {
     Text(if(actionsOpen)"Volver" else "Acciones",fontSize=12.sp,color=colors.onSurfaceVariant)
     if(wide){Spacer(Modifier.width(8.dp));GlassKeycap(if(actionsOpen)"Escape" else actionsKeyLabel)}
    }
   }
  }
 }
}
