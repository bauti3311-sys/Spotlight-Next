package dev.bautista.spotlight.data

import android.content.*
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*

/** Window-lifetime URI selection. It never copies or uploads files. */
class FileBuffer {
    private val selected=mutableStateListOf<Result>()
    val items:List<Result> get()=selected.toList()
    fun add(result:Result):Boolean {
        val uri=(result.action as? Action.Launch)?.intent?.data ?: return false
        if(result.category!="Archivos" || result.mime==android.provider.DocumentsContract.Document.MIME_TYPE_DIR || uri.scheme !in setOf("content","file"))return false
        if(selected.any{(it.action as? Action.Launch)?.intent?.data==uri})return true
        if(selected.size>=12)return false
        selected+=result;return true
    }
    fun remove(id:String){selected.removeAll{it.id==id}}
    fun clear(){selected.clear()}
    fun share():Intent {
        check(selected.isNotEmpty())
        val uris=ArrayList(selected.map{(it.action as Action.Launch).intent.data!!})
        val mime=selected.mapNotNull{it.mime}.distinct().singleOrNull() ?: "*/*"
        val clip=ClipData.newRawUri("Selección de Spotlight",uris.first()).apply{uris.drop(1).forEach{addItem(ClipData.Item(it))}}
        return Intent(Intent.ACTION_SEND_MULTIPLE).setType(mime).putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).also{it.clipData=clip}
    }
    suspend fun text(context:Context):String=withContext(Dispatchers.IO){
        val snapshot=items;check(snapshot.isNotEmpty())
        val perFile=(15000/snapshot.size).coerceAtMost(8000)
        val texts=mutableListOf<String>()
        for(item in snapshot){
            ensureActive();require((item.sizeBytes ?: 0)<=20L*1024*1024){"Archivo demasiado grande: ${item.title}"}
            val uri=(item.action as Action.Launch).intent.data!!
            texts+="Documento: ${item.title}\n"+dev.bautista.spotlight.index.DocumentText.extract(context,uri,dev.bautista.spotlight.index.OfficeText.mime(item.title,item.mime)).take(perFile)
        }
        texts.joinToString("\n\n").take(16000).ifBlank{error("No se obtuvo texto compatible de los documentos seleccionados")}
    }
}
