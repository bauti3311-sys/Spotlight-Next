package dev.bautista.spotlight.index

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import kotlinx.coroutines.*
import java.security.MessageDigest

object DocumentText {
    private const val MAX_BYTES=8_000_000
    private suspend fun read(context:Context,uri:Uri,deadline:Long?=null):ByteArray {
        return context.contentResolver.openInputStream(uri)?.use{input->
            val out=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192)
            while(true){currentCoroutineContext().ensureActive();check(deadline==null || SystemClock.elapsedRealtime()<deadline){"La extracción local agotó su tiempo de lectura."};val n=input.read(buffer);if(n<0)break
                require(out.size()+n<=MAX_BYTES){"El documento supera 8 MB para extracción rápida."};out.write(buffer,0,n)}
            out.toByteArray()
        } ?: error("No se pudo leer el archivo. Revisá el permiso.")
    }
    suspend fun extract(context:Context,uri:Uri,mime:String?):String {
        val result=extractDetailed(context,uri,mime)
        check(result.text.isNotBlank()){"No se encontró texto legible dentro de los límites de extracción local. El PDF puede estar vacío o requerir una revisión por páginas."}
        return result.text
    }
    suspend fun extractDetailed(context:Context,uri:Uri,mime:String?,limits:PdfTextLimits=PdfTextLimits()):ExtractedDocument=withContext(Dispatchers.IO) {
        val type=mime ?: context.contentResolver.getType(uri).orEmpty()
        require(type=="application/pdf" || type.startsWith("text/") || type.startsWith("image/") || OfficeText.supports(type)){"La extracción admite PDF, Office, texto e imágenes."}
        val start=SystemClock.elapsedRealtime()
        var admitted=false;var lease:PdfWorkLease?=null
        try {
            if(type=="application/pdf") {
                admitted=PdfTextPipeline.admission.tryAcquire()
                check(admitted){"La extracción PDF sigue ocupada. Intentá nuevamente cuando termine la tarea local."}
                lease=PdfWorkLease{PdfTextPipeline.admission.release()}
            }
            val bytes=read(context,uri,if(type=="application/pdf")start+limits.totalMillis else null)
            val sha=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
            if(type=="application/pdf") {
                PdfTextSource.open(context,bytes).use{source->MediaAnalysis(context).use{media->
                    PdfTextPipeline.extract(source,limits,sha,requireNotNull(lease),start=start,recognizeOwned=media::pdfPage)
                }}
            } else {
                val text=when {
                    OfficeText.supports(type)->OfficeText.extract(bytes).text
                    type.startsWith("image/")->MediaAnalysis(context).use{it.image(uri,false)}
                    else->String(bytes,Charsets.UTF_8)
                }.take(24000)
                ExtractedDocument(text,emptyList(),emptyList(),0,0,0,if(type.startsWith("image/"))"" else sha)
            }
        } catch(e:OutOfMemoryError){throw IllegalStateException("No hay memoria suficiente para extraer este PDF de forma local.")}
        finally{if(admitted){lease?.close() ?: PdfTextPipeline.admission.release()}}
    }
}
