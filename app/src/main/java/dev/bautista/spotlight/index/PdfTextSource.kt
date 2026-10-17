package dev.bautista.spotlight.index

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.Writer

/** Native text and rendering share the same bounded, hashed source snapshot. */
internal class PdfTextSource private constructor(private val context:Context,private val bytes:ByteArray,private val pdf:PDDocument):PdfPageSource {
    private var renderer:PdfRenderer?=null
    private var snapshot:File?=null
    override val pages get()=pdf.numberOfPages
    override val author get()=pdf.documentInformation.author.orEmpty().take(300)
    override val created get()=runCatching{pdf.documentInformation.creationDate?.timeInMillis ?: 0L}.getOrDefault(0L)
    override fun text(page:Int):String {
        val out=object:Writer(){val content=StringBuilder()
            override fun write(buffer:CharArray,offset:Int,length:Int){val count=minOf(length,100001-content.length);if(count>0)content.append(buffer,offset,count)}
            override fun flush(){};override fun close(){}
        }
        PDFTextStripper().apply{startPage=page+1;endPage=page+1}.writeText(pdf,out)
        return out.content.toString()
    }
    override fun render(page:Int,limits:PdfTextLimits):Bitmap {
        val active=renderer ?: run {
            PdfSnapshotFiles.removeAbandoned(context)
            val file=PdfSnapshotFiles.create(context);snapshot=file;file.writeBytes(bytes)
            val fd=ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY)
            try{PdfRenderer(fd).also{renderer=it}}catch(e:Throwable){fd.close();throw e}
        }
        return active.openPage(page).use { p ->
            val (width,height)=limits.dimensions(p.width,p.height)
            val bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
            try{bitmap.eraseColor(Color.WHITE);p.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);bitmap}
            catch(e:Throwable){bitmap.recycle();throw e}
        }
    }
    override fun close(){try{renderer?.close()}finally{try{pdf.close()}finally{snapshot?.delete()}}}
    companion object {
        fun open(context:Context,bytes:ByteArray):PdfTextSource {PDFBoxResourceLoader.init(context);return PdfTextSource(context,bytes,PDDocument.load(bytes))}
    }
}

internal object PdfSnapshotFiles {
    private val prefix="scan-${java.util.UUID.randomUUID()}-"
    private fun directory(context:Context)=File(context.cacheDir,"pdf-ocr-snapshots").apply{mkdirs()}
    fun create(context:Context):File=File.createTempFile(prefix,".pdf",directory(context))
    fun removeAbandoned(context:Context){directory(context).listFiles()?.filter{it.name.startsWith("scan-") && it.name.endsWith(".pdf") && !it.name.startsWith(prefix)}?.forEach{it.delete()}}
}
