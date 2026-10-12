package dev.bautista.spotlight

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.io.File

/** Explicit Share sheet input; transient grants are copied with a strict size bound. */
class ShareContextActivity:ComponentActivity() {
 override fun onCreate(state:Bundle?){super.onCreate(state);if(state!=null){finish();return};lifecycleScope.launch {
  try {
   val app=application as SpotlightApp
   val text=intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString().orEmpty()
   @Suppress("DEPRECATION") val uri=intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
   @Suppress("DEPRECATION") val multiple=if(intent.action==Intent.ACTION_SEND_MULTIPLE)intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM).orEmpty() else emptyList()
   if(multiple.isNotEmpty()) {
    require(multiple.size<=12 && multiple.all{it.scheme=="content"})
    val joined=withContext(Dispatchers.IO){multiple.mapIndexed{index,input->
     ensureActive()
     val size=contentResolver.query(input,arrayOf(OpenableColumns.SIZE),null,null,null)?.use{if(it.moveToFirst() && !it.isNull(0))it.getLong(0) else 0L} ?: 0L
     require(size<=20L*1024*1024)
     val mime=contentResolver.getType(input).orEmpty()
     "Documento ${index+1}: "+dev.bautista.spotlight.index.DocumentText.extract(this@ShareContextActivity,input,mime).take(15000/multiple.size)
    }.joinToString("\n\n").take(16000)}
    app.contexts.setText(joined,"${multiple.size} documentos compartidos")
   }
   else if(uri==null){require(text.isNotBlank());app.contexts.setText(text.take(16000),"Texto compartido")}
   else {
    require(uri.scheme=="content")
    val persisted=runCatching{contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);true}.getOrDefault(false)
    if(persisted)app.contexts.setDocument(uri)
    else withContext(Dispatchers.IO){
     val directory=File(noBackupFilesDir,"context-imports").apply{mkdirs()}
     require(directory.listFiles().orEmpty().sumOf{it.length()}<60L*1024*1024){"Quitá contextos guardados antes de importar más archivos."}
     val name=runCatching{contentResolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use{if(it.moveToFirst())it.getString(0) else null}}.getOrNull() ?: "Documento compartido"
     val target=File(directory,java.util.UUID.randomUUID().toString()+"-"+name.replace(Regex("[^A-Za-z0-9._-]"),"_").take(80))
     try{contentResolver.openInputStream(uri)?.use{input->target.outputStream().use{out->val bytes=ByteArray(8192);var total=0L;while(true){ensureActive();val n=input.read(bytes);if(n<0)break;total+=n;require(total<=20L*1024*1024){"El archivo supera 20 MB."};out.write(bytes,0,n)}}} ?: error("No se pudo leer el archivo.");val local=FileProvider.getUriForFile(this@ShareContextActivity,"$packageName.files",target);app.contexts.setDocument(local,target.absolutePath)}catch(e:Exception){target.delete();throw e}
    }
   }
   startActivity(Intent(this@ShareContextActivity,InvokeActivity::class.java).putExtra("query","contexto"))
  }catch(e:CancellationException){throw e}catch(e:Exception){android.widget.Toast.makeText(this@ShareContextActivity,"No se pudo importar el contenido. Probá un documento de hasta 20 MB.",android.widget.Toast.LENGTH_LONG).show()}
  finally{finish()}
 }}
}
