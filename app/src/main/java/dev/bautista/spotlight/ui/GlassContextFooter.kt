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
 val appearance=LocalAppearance.current
 val keyMaterial=if(LocalPremiumReview.current)Modifier else Modifier.background(colors.onSurface.copy(alpha=.065f),RoundedCornerShape(5.dp))
  .border(1.dp,colors.onSurface.copy(alpha=.09f),RoundedCornerShape(5.dp))
 Text(key,keyMaterial.padding(horizontal=6.dp,vertical=3.dp),
  fontSize=GlassTokens.scaledSp(10.sp,appearance),lineHeight=GlassTokens.scaledSp(13.sp,appearance,minSp=11f),color=colors.onSurfaceVariant,maxLines=1)
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
 val appearance=LocalAppearance.current
 val d=GlassTokens.densityScale(appearance)
 val premium=LocalPremiumReview.current
 BoxWithConstraints(Modifier.fillMaxWidth().testTag("context-footer")
  .then(if(premium)Modifier.background(colors.surfaceContainerLow) else Modifier)) {
  val wide=maxWidth>=700.dp
  Row(Modifier.fillMaxWidth().heightIn(min=(48f*d).coerceAtLeast(48f).dp).padding(horizontal=(10f*d).dp,vertical=(2f*d).dp),verticalAlignment=Alignment.CenterVertically) {
   onOptions?.let { options ->
    IconButton(onClick=options,modifier=Modifier.size(48.dp).testTag("command-dock-menu")) {
     IosCommandIcon(CommandIcons.MoreHoriz,"Opciones de Spotlight",Modifier.size(20.dp),tint=colors.onSurfaceVariant)
    }
   }
   if(wide)Text(scopeLabel,Modifier.padding(start=if(onOptions==null)(12f*d).dp else 0.dp).widthIn(max=160.dp),fontSize=GlassTokens.scaledSp(11.sp,appearance),color=colors.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
   if(wide || !enabled)Spacer(Modifier.weight(1f))
   if(onExpand!=null)TextButton(onClick=onExpand,modifier=Modifier.testTag("results-expand").heightIn(min=48.dp),contentPadding=PaddingValues(horizontal=6.dp)) {
    Text(if(wide)expandLabel else if(expandLabel.startsWith("Ver menos"))"Menos" else "Más",fontSize=GlassTokens.scaledSp(11.sp,appearance),color=colors.onSurfaceVariant)
   }
   if(autocomplete && !actionsOpen && wide)Row(Modifier.padding(horizontal=(8f*d).dp),verticalAlignment=Alignment.CenterVertically){Text("Autocompletar",fontSize=GlassTokens.scaledSp(11.sp,appearance),color=colors.onSurfaceVariant);Spacer(Modifier.width(6.dp));GlassKeycap("Tab")}
   // A quiet text dock on touch layouts; preserve callbacks, 48dp targets and total height.
   val dock=if(wide && !premium)Modifier.background(colors.onSurface.copy(alpha=.035f),RoundedCornerShape(12.dp))
    .border(1.dp,colors.onSurface.copy(alpha=.08f),RoundedCornerShape(12.dp)) else Modifier
   if(enabled)Row(Modifier.then(if(wide)Modifier else Modifier.weight(1f)).testTag("command-dock-actions").then(dock),verticalAlignment=Alignment.CenterVertically) {
    TextButton(onClick=onExecute,enabled=!actionsOpen,contentPadding=PaddingValues(horizontal=(11f*d).dp),modifier=Modifier.then(if(wide)Modifier.widthIn(max=220.dp) else Modifier.weight(1f)).heightIn(min=48.dp).testTag("command-primary")) {
     Text(if(actionsOpen)"Ejecutar" else primaryLabel,fontSize=GlassTokens.scaledSp(12.sp,appearance),maxLines=1,overflow=TextOverflow.Ellipsis,color=colors.onSurface.copy(alpha=if(actionsOpen).38f else 1f))
     if(wide && LocalLiquidScene.current==null){Spacer(Modifier.width(6.dp));GlassKeycap("Enter")}
    }
    if(wide)Box(Modifier.width(1.dp).height(18.dp).background(colors.onSurface.copy(alpha=.10f)))
    TextButton(onClick=onActions,contentPadding=PaddingValues(horizontal=(11f*d).dp),modifier=Modifier.heightIn(min=48.dp).testTag("command-actions")) {
     Text(if(actionsOpen)"Volver" else "Acciones",fontSize=GlassTokens.scaledSp(12.sp,appearance),color=colors.onSurfaceVariant)
     if(wide && LocalLiquidScene.current==null){Spacer(Modifier.width(6.dp));GlassKeycap(if(actionsOpen)"Escape" else actionsKeyLabel)}
    }
   }
  }
 }
}
