package dev.bautista.spotlight.ui

import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.automation.*
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.*

@Composable fun SavedCommandPanel(app:SpotlightApp,c:Config,id:String,onAction:(Action)->Unit) {
 val template=remember(c.routines,id){CommandTemplates.parse(c.routines).singleOrNull{it.id==id}}
 if(template!=null){SavedTemplatePanel(app,c,template,onAction);return}
 val command=remember(c.routines,id){Workflows.parse(c.routines).singleOrNull{it.id==id && NativeCommands.isNative(it)}}
 var review by remember(command){mutableStateOf<NativeCommands.Review?>(null)}
 val scope=rememberCoroutineScope()
 var error by remember(id){mutableStateOf("")}
 LaunchedEffect(command) {
  review=command?.let { w -> withContext(Dispatchers.IO) { NativeCommands.review(w){pkg->
   runCatching{app.packageManager.getApplicationLabel(app.packageManager.getApplicationInfo(pkg,0)).toString()}.getOrDefault(pkg)
  }}}
 }
 when {
  command==null->Text("Este comando ya no está disponible. Los datos anteriores se conservan.")
  review==null->Text("Comprobando comando guardado…")
  else->Column(Modifier.fillMaxWidth()) {
   Row(Modifier.fillMaxWidth()) {
    Text(command.name,Modifier.weight(1f),maxLines=1,overflow=TextOverflow.Ellipsis)
    ConfirmedSettingsButton("Eliminar comando","Elimina este comando guardado. Las aplicaciones, preferencias y datos anteriores se conservan.") {
     scope.launch{try{app.preferences.deleteNativeCommand(id);onAction(Action.RestoreQuery(""))}catch(e:CancellationException){throw e}catch(e:Exception){error="No se pudo eliminar el comando."}}
    }
   }
   if(error.isNotBlank())Text(error)
   EditablePlanPanel(app,c,"saved:$id",review!!.plan,review!!.calls,onAction)
  }
 }
}
