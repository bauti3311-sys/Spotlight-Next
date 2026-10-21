package dev.bautista.spotlight.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

/** One quiet footer. Visual density is compact; native actions retain 48dp touch areas. */
@Composable fun GlassContextFooter(suggestion:Boolean,enabled:Boolean,primaryLabel:String,onExecute:()->Unit,onActions:()->Unit,
    autocomplete:Boolean=false,expandLabel:String="Ver más resultados",onExpand:(()->Unit)?=null,actionsOpen:Boolean=false) {
    val colors=MaterialTheme.colorScheme
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width=maxWidth
        Row(Modifier.fillMaxWidth().heightIn(min=48.dp).padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {
            if(onExpand!=null) TextButton(onClick=onExpand,modifier=Modifier.testTag("results-expand"),contentPadding=PaddingValues(horizontal=4.dp)) {
                Text(if(width>=520.dp)expandLabel else if(expandLabel.startsWith("Ver menos"))"Menos" else "Más",fontSize=11.sp,color=colors.onSurfaceVariant)
            } else if(width>=560.dp) Text("Spotlight",fontSize=11.sp,color=colors.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            if(actionsOpen) {
                Text("Enter · Ejecutar",fontSize=11.sp,color=colors.onSurfaceVariant)
                TextButton(onClick=onActions,contentPadding=PaddingValues(horizontal=8.dp)){Text("Escape · Volver",fontSize=11.sp,color=colors.onSurface)}
            } else if(enabled) {
                TextButton(onClick=onExecute,enabled=!actionsOpen,contentPadding=PaddingValues(horizontal=8.dp)) {
                    Text(primaryLabel,fontSize=11.sp,maxLines=1,overflow=TextOverflow.Ellipsis,color=colors.onSurface)
                    Text("  Enter",fontSize=10.sp,color=colors.onSurfaceVariant)
                }
                TextButton(onClick=onActions,enabled=!actionsOpen,contentPadding=PaddingValues(horizontal=8.dp)) {
                    Text(if(actionsOpen)"Escape · Volver" else if(autocomplete)"Ctrl+K · Acciones" else "Tab · Acciones",fontSize=11.sp,color=colors.onSurfaceVariant)
                }
            }
        }
    }
}
