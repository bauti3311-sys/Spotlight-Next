package dev.bautista.spotlight.data

import android.content.*
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*
import dev.bautista.spotlight.index.*

/** Window-lifetime URI selection. It never copies or uploads files. */
class FileBuffer(private val extract:suspend (Context,Uri,String?,PdfTextLimits)->ExtractedDocument={c,u,m,l->DocumentText.extractDetailed(c,u,m,l)}) {
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
    fun share(context:Context?=null,packageName:String?=null):Intent {
        check(selected.isNotEmpty())
        require(packageName==null || packageName.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")))
        val uris=ArrayList(selected.map{val original=(it.action as Action.Launch).intent.data!!;if(original.scheme=="file"){
            require(context!=null);androidx.core.content.FileProvider.getUriForFile(context,context.packageName+".files",java.io.File(original.path!!))
        } else original})
        val types=selected.map{dev.bautista.spotlight.sharing.FileShareContract.mime(it.title,it.mime)}.distinct()
        val families=types.map{it.substringBefore('/')}.distinct()
        val mime=types.singleOrNull() ?: families.singleOrNull()?.let{"$it/*"} ?: "*/*"
        return Intent(if(uris.size==1)Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE).setType(mime).setPackage(packageName)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).also{intent->
                if(uris.size==1)intent.putExtra(Intent.EXTRA_STREAM,uris.single()) else intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris)
                intent.clipData=dev.bautista.spotlight.sharing.FileShareContract.canonicalClip(uris,types)
            }
    }
    suspend fun text(context:Context):String {
        val snapshot=items;check(snapshot.isNotEmpty())
        return withContext(Dispatchers.IO){withTimeoutOrNull(60000){
            val perFile=(15800/snapshot.size).coerceAtMost(8000)
            var remainingOcr=6
            val texts=mutableListOf<String>()
            for((position,item) in snapshot.withIndex()) {
                ensureActive();require((item.sizeBytes ?: 0)<=8_000_000){"El documento supera 8 MB: ${item.title.take(120)}"}
                val uri=(item.action as Action.Launch).intent.data!!
                val mime=OfficeText.mime(item.title,item.mime)
                val image=mime?.startsWith("image/")==true
                check(!image || remainingOcr>0){"Se agotó el presupuesto OCR de la selección. Prepará menos imágenes."}
                val limits=PdfTextLimits(ocrPages=minOf(3,remainingOcr),characters=perFile)
                val document=try{extract(context,uri,mime,limits)}catch(e:TimeoutCancellationException){
                    // Preserve cancellation of the batch/user; a nested extractor timeout is an error.
                    ensureActive();throw IllegalStateException("La extracción local agotó su tiempo: ${item.title.take(120)}",e)
                }
                ensureActive()
                remainingOcr=(remainingOcr-if(image)1 else document.ocrAttempts).coerceAtLeast(0)
                texts+=DocumentSelectionText.excerpt("D${position+1}",item.title,document,perFile)
            }
            texts.joinToString("\n\n").also{check(it.length<=16000)}
        } ?: error("La preparación local agotó 60 segundos. Probá con menos documentos.")}
    }
}
