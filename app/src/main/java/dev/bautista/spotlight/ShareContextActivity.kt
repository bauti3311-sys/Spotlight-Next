package dev.bautista.spotlight

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import dev.bautista.spotlight.ui.SharedImportPanel
import dev.bautista.spotlight.ui.SharedImportProgress
import dev.bautista.spotlight.ui.SpotlightTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

/** Explicit Share input: preserve original URIs and transfer read grants, never extract on arrival. */
class ShareContextActivity:ComponentActivity() {
 private val progress=MutableStateFlow(SharedImportProgress())
 private var importJob:Job?=null
 internal fun cancelImport(){importJob?.cancel();finish()}
 override fun onCreate(state:Bundle?){super.onCreate(state);if(state!=null){finish();return}
 setContent { val current by progress.collectAsState()
  val config by (application as SpotlightApp).preferences.config.collectAsState(initial=dev.bautista.spotlight.data.Config())
  SpotlightTheme(theme=config.theme,config=config){SharedImportPanel(current,::cancelImport)} }
 importJob=lifecycleScope.launch {
  try {
   val app=application as SpotlightApp
   val input=dev.bautista.spotlight.sharing.SharedInput.parse(intent)
   val text=input.text
   if(input.documents.isEmpty()) {
    require(text.isNotBlank());app.contexts.setText(text.take(16000),"Texto compartido")
   } else {
    progress.value=SharedImportProgress(1,input.documents.size)
    // Share keeps only the offered temporary read access. Persistent SAF selection
    // belongs to the existing document picker, never to an automatic Share import.
    app.contexts.setDocuments(input.documents,text)
   }
   ensureActive()
   val entry=Intent(this@ShareContextActivity,InvokeActivity::class.java).putExtra("query",if(input.documents.isEmpty())"contexto" else "compartir archivos seleccionados")
   if(input.documents.isNotEmpty()) {
    entry.clipData=dev.bautista.spotlight.sharing.FileShareContract.canonicalClip(input.documents)
    entry.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
   }
   startActivity(entry)
  }catch(e:CancellationException){throw e}catch(e:Exception){android.widget.Toast.makeText(this@ShareContextActivity,"No se pudo autorizar la selección. Elegí nuevamente los archivos con Android.",android.widget.Toast.LENGTH_LONG).show()}
  finally{finish()}
 }}
}
