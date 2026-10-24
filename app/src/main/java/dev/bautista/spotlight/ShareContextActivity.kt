package dev.bautista.spotlight

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import dev.bautista.spotlight.ui.SharedImportPanel
import dev.bautista.spotlight.ui.SharedImportProgress
import dev.bautista.spotlight.ui.SpotlightTheme
import dev.bautista.spotlight.sharing.copySharedContent
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.io.File

/** Explicit Share sheet input; transient grants are copied with a strict size bound. */
class ShareContextActivity:ComponentActivity() {
 private val progress=MutableStateFlow(SharedImportProgress())
 private var importJob:Job?=null
 internal fun cancelImport(){importJob?.cancel();finish()}
 override fun onCreate(state:Bundle?){super.onCreate(state);if(state!=null){finish();return}
 setContent { val current by progress.collectAsState();SpotlightTheme(theme="dark"){SharedImportPanel(current,::cancelImport)} }
 importJob=lifecycleScope.launch {
  try {
   val app=application as SpotlightApp
   val input=dev.bautista.spotlight.sharing.SharedInput.parse(intent)
   val text=input.text
   val uri=input.documents.singleOrNull()
   val multiple=input.documents.takeIf{it.size>1}.orEmpty()
   if(multiple.isNotEmpty()) {
    require(multiple.size<=12 && multiple.all{it.scheme=="content"})
    val joined=withContext(Dispatchers.IO){multiple.mapIndexed{index,input->
     ensureActive()
     progress.value=SharedImportProgress(index+1,multiple.size)
     val size=contentResolver.query(input,arrayOf(OpenableColumns.SIZE),null,null,null)?.use{if(it.moveToFirst() && !it.isNull(0))it.getLong(0) else 0L} ?: 0L
     require(size<=20L*1024*1024)
     val mime=contentResolver.getType(input).orEmpty()
     "Documento ${index+1}: "+dev.bautista.spotlight.index.DocumentText.extract(this@ShareContextActivity,input,mime).take(15000/multiple.size)
    }.joinToString("\n\n").take(16000)}
    app.contexts.setText(joined,"${multiple.size} documentos compartidos")
   }
   else if(uri==null){require(text.isNotBlank());app.contexts.setText(text.take(16000),"Texto compartido")}
   else {
    progress.value=SharedImportProgress(1,1)
    require(uri.scheme=="content")
    val hadGrant=runCatching{contentResolver.persistedUriPermissions.any{it.uri==uri && it.isReadPermission}}.getOrDefault(true)
    val persisted=runCatching{contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);true}.getOrDefault(false)
    if(persisted)try{app.contexts.setDocument(uri)}catch(e:Exception){
     if(!hadGrant && app.contexts.active.value?.uri!=uri.toString())runCatching{contentResolver.releasePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)}
     throw e
    }
    else withContext(Dispatchers.IO){
     val directory=File(noBackupFilesDir,"context-imports").apply{mkdirs()}
     require(directory.listFiles().orEmpty().sumOf{it.length()}<60L*1024*1024){"Quitá contextos guardados antes de importar más archivos."}
     val name=runCatching{contentResolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use{if(it.moveToFirst())it.getString(0) else null}}.getOrNull() ?: "Documento compartido"
     val target=File(directory,java.util.UUID.randomUUID().toString()+"-"+name.replace(Regex("[^A-Za-z0-9._-]"),"_").take(80))
     try{contentResolver.openInputStream(uri)?.use{input->target.outputStream().use{out->copySharedContent(input,out,20L*1024*1024){bytes->progress.value=SharedImportProgress(1,1,bytes)}}} ?: error("No se pudo leer el archivo.");val local=FileProvider.getUriForFile(this@ShareContextActivity,"$packageName.files",target);app.contexts.setDocument(local,target.absolutePath)}catch(e:Exception){if(app.contexts.active.value?.ownedFile!=target.absolutePath)target.delete();throw e}
    }
   }
   ensureActive()
   startActivity(Intent(this@ShareContextActivity,InvokeActivity::class.java).putExtra("query","contexto"))
  }catch(e:CancellationException){throw e}catch(e:Exception){android.widget.Toast.makeText(this@ShareContextActivity,"No se pudo importar el contenido. Probá un documento de hasta 20 MB.",android.widget.Toast.LENGTH_LONG).show()}
  finally{finish()}
 }}
}
