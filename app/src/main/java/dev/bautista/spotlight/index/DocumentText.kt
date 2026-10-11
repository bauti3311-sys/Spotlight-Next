package dev.bautista.spotlight.index

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.*

object DocumentText {
    suspend fun extract(context:Context,uri:Uri,mime:String?):String=withContext(Dispatchers.IO) {
        val type=mime ?: context.contentResolver.getType(uri).orEmpty()
        require(type=="application/pdf" || type.startsWith("text/") || type.startsWith("image/") || OfficeText.supports(type)){"La extracción admite PDF, Office, texto e imágenes."}
        val bytes=context.contentResolver.openInputStream(uri)?.use{input->val out=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192);while(true){ensureActive();val n=input.read(buffer);if(n<0)break;require(out.size()+n<=8_000_000){"El documento supera 8 MB para extracción rápida."};out.write(buffer,0,n)};out.toByteArray()} ?: error("No se pudo leer el archivo. Revisá el permiso.")
        val text=when {
            OfficeText.supports(type)->OfficeText.extract(bytes).text
            type=="application/pdf"->{PDFBoxResourceLoader.init(context);PDDocument.load(bytes).use{doc->PDFTextStripper().apply{startPage=1;endPage=minOf(doc.numberOfPages,40)}.getText(doc).take(24000)}}
            type.startsWith("image/")->MediaAnalysis(context).use{it.image(uri,false)}.take(24000)
            else->String(bytes,Charsets.UTF_8).take(24000)
        }
        if(text.isBlank())error("No se encontró texto extraíble. El PDF puede contener páginas escaneadas.")
        text
    }
}
