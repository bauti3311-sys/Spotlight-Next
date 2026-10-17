package dev.bautista.spotlight.index

import android.graphics.Bitmap
import android.os.SystemClock
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import org.json.JSONArray
import org.json.JSONObject
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.*

data class PdfTextLimits(
    val textPages:Int=40, val ocrPages:Int=3, val characters:Int=24000,
    val longEdge:Int=1600, val pixels:Int=1800000,
    val totalMillis:Long=45000, val pageMillis:Long=12000,
) {
    init {require(textPages in 1..40 && ocrPages in 0..6 && characters in 1000..100000)
        require(longEdge in 128..1800 && pixels in 16384..2000000 && totalMillis in 1..60000 && pageMillis in 1..15000)}
    fun dimensions(width:Int,height:Int):Pair<Int,Int> {
        require(width>0 && height>0){"Dimensiones PDF inválidas."}
        val scale=minOf(2.0,longEdge.toDouble()/max(width,height),sqrt(pixels.toDouble()/width/height))
        return max(1,floor(width*scale).toInt()) to max(1,floor(height*scale).toInt())
    }
}

data class TextFragment(val page:Int,val method:String,val start:Int,val end:Int,val width:Int=0,val height:Int=0)
data class ExtractionIssue(val code:String,val page:Int=0)
data class ExtractedDocument(
    val text:String,val fragments:List<TextFragment>,val issues:List<ExtractionIssue>,
    val pages:Int,val examined:Int,val ocrAttempts:Int,val sourceSha256:String,
    val author:String="",val created:Long=0,val extractedAt:Long=System.currentTimeMillis(),
) {
    val partial get()=issues.isNotEmpty()
    val coverage get()=if(pages==0)"Extracción local acotada a ${text.length} caracteres; no se presupone contenido fuera del extracto." else "Extracción local ${if(partial)"parcial" else "acotada"}: $examined de $pages páginas examinadas; ${fragments.count{it.method=="ocr_local"}} páginas con texto OCR. El OCR puede contener errores; no se presupone contenido fuera de este extracto."
    fun metadata():String=JSONObject().put("version",1).put("sha256",sourceSha256)
        .put("pages",pages).put("examined",examined).put("ocrAttempts",ocrAttempts).put("partial",partial).put("extractedAt",extractedAt)
        .put("fragments",JSONArray().apply{fragments.forEach{f->put(JSONObject().put("page",f.page).put("method",f.method).put("start",f.start).put("end",f.end).put("width",f.width).put("height",f.height))}})
        .put("issues",JSONArray().apply{issues.forEach{put(JSONObject().put("code",it.code).put("page",it.page))}}).toString()
    companion object {
        /** Generated page/limit labels are provenance, not words contained in the document. */
        fun searchableText(metadata:String,body:String):String {
            if(metadata.isBlank() || metadata.length>30000)return body
            return runCatching {
                val root=JSONObject(metadata);if(root.optInt("pages")==0)return@runCatching body
                val entries=root.getJSONArray("fragments");require(entries.length()<=40)
                (0 until entries.length()).joinToString("\n"){i->val f=entries.getJSONObject(i);val a=f.getInt("start");val b=f.getInt("end");require(a>=0 && b in a..body.length);body.substring(a,b)}
            }.getOrDefault(body)
        }
        fun provenance(metadata:String,body:String,query:String):String {
            if(metadata.isBlank() || metadata.length>30000)return ""
            return runCatching {
                val root=JSONObject(metadata);if(root.optInt("pages")==0)return@runCatching "";val entries=root.getJSONArray("fragments")
                val words=dev.bautista.spotlight.search.normalize(query).split(Regex("\\s+")).filter{it.isNotBlank()}
                val parts=(0 until minOf(entries.length(),40)).map{entries.getJSONObject(it)}
                val matched=parts.firstOrNull{f->val a=f.getInt("start");val b=f.getInt("end");a>=0 && b in a..body.length && words.isNotEmpty() && words.all{dev.bautista.spotlight.search.normalize(body.substring(a,b)).contains(it)}}
                val detail=matched?.let{(if(it.getString("method")=="ocr_local")"OCR local (revisar)" else "Texto PDF")+" · página ${it.getInt("page")}"} ?: "Extracción local · ${root.getInt("examined")} de ${root.getInt("pages")} páginas examinadas"
                (if(root.optBoolean("partial"))"Extracción parcial · " else "")+detail+"\n"
            }.getOrDefault("")
        }
    }
}

/** A canceled ML task still owns its image. Keep admission closed until it actually finishes. */
internal class OcrWorkLease(private val release:()->Unit):Closeable {
    private var pending=0;private var closing=false;private var released=false
    @Synchronized fun retain():()->Unit {
        check(!closing);pending++
        val once=AtomicBoolean()
        return {if(once.compareAndSet(false,true))synchronized(this){pending--;finish()}}
    }
    @Synchronized override fun close(){closing=true;finish()}
    private fun finish(){if(closing && pending==0 && !released){released=true;release()}}
}

internal interface PdfPageSource:Closeable {
    val pages:Int
    val author:String get()=""
    val created:Long get()=0
    fun text(page:Int):String
    fun render(page:Int,limits:PdfTextLimits):Bitmap
}

internal object PdfTextPipeline {
    val admission=Semaphore(1)
    suspend fun extract(source:PdfPageSource,limits:PdfTextLimits,sha:String,lease:OcrWorkLease,
        start:Long=SystemClock.elapsedRealtime(),clock:()->Long={SystemClock.elapsedRealtime()},
        recognizeOwned:suspend(Bitmap,()->Unit)->String):ExtractedDocument {
        val body=StringBuilder();val fragments=mutableListOf<TextFragment>();val issues=mutableListOf<ExtractionIssue>()
        var examined=0;var attempts=0
        fun add(page:Int,method:String,text:String,w:Int=0,h:Int=0) {
            if(text.isBlank())return
            val header="[Página $page · ${if(method=="ocr_local")"OCR local (revisar)" else "texto PDF"}]\n"
            val remaining=limits.characters-500-body.length-header.length-2
            if(remaining<=0){issues+=ExtractionIssue("character_limit",page);return}
            body.append(header);val from=body.length;body.append(text.take(remaining));val end=body.length;body.append("\n\n")
            fragments+=TextFragment(page,method,from,end,w,h)
            if(text.length>remaining)issues+=ExtractionIssue("character_limit",page)
        }
        for(index in 0 until minOf(source.pages,limits.textPages)) {
            currentCoroutineContext().ensureActive()
            if(clock()-start>=limits.totalMillis){issues+=ExtractionIssue("time_limit",index+1);break}
            val native=try{source.text(index).trim()}catch(e:CancellationException){throw e}catch(_:Exception){issues+=ExtractionIssue("text_error",index+1);""}
            examined++
            if(clock()-start>=limits.totalMillis){add(index+1,"pdf_text",native);issues+=ExtractionIssue("time_limit",index+1);break}
            if(native.count{it.isLetterOrDigit()}>=40) add(index+1,"pdf_text",native)
            else if(attempts<limits.ocrPages) {
                attempts++
                var transferred=false;var bitmap:Bitmap?=null
                try {
                    currentCoroutineContext().ensureActive()
                    val image=source.render(index,limits);bitmap=image
                    require(image.width<=limits.longEdge && image.height<=limits.longEdge && image.width.toLong()*image.height<=limits.pixels && image.config==Bitmap.Config.ARGB_8888){"Imagen OCR fuera del límite."}
                    val remaining=limits.totalMillis-(clock()-start)
                    if(remaining<=0){issues+=ExtractionIssue("time_limit",index+1);add(index+1,"pdf_text",native);break}
                    val w=image.width;val h=image.height
                    val recognized=withTimeoutOrNull(minOf(limits.pageMillis,remaining)) {
                        val finished=lease.retain();transferred=true
                        recognizeOwned(image,finished)
                    }
                    if(recognized==null){issues+=ExtractionIssue("time_limit",index+1);add(index+1,"pdf_text",native);break}
                    if(recognized.isNotBlank())add(index+1,"ocr_local",recognized.trim(),w,h)
                    else {issues+=ExtractionIssue("ocr_empty",index+1);add(index+1,"pdf_text",native)}
                } catch(e:CancellationException){throw e}
                catch(_:OutOfMemoryError){issues+=ExtractionIssue("memory_limit",index+1);add(index+1,"pdf_text",native);break}
                catch(_:Exception){issues+=ExtractionIssue("ocr_error",index+1);add(index+1,"pdf_text",native)}
                finally{if(!transferred)bitmap?.recycle()}
            } else {issues+=ExtractionIssue("ocr_page_limit",index+1);add(index+1,"pdf_text",native)}
            if(issues.any{it.code=="character_limit"})break
        }
        if(source.pages>limits.textPages)issues+=ExtractionIssue("page_limit",limits.textPages+1)
        val labels=mapOf("character_limit" to "límite de texto", "time_limit" to "tiempo agotado", "text_error" to "texto PDF no legible", "memory_limit" to "límite de memoria", "ocr_error" to "OCR no disponible en alguna página", "ocr_empty" to "página sin texto reconocido", "ocr_page_limit" to "límite de páginas OCR", "page_limit" to "límite de páginas")
        val notice=if(issues.isNotEmpty())"[Extracción parcial: ${issues.map{labels[it.code] ?: "página no procesada"}.distinct().joinToString(", ")}. Solo se incluye el contenido obtenido localmente.]\n".take(499) else ""
        return ExtractedDocument(if(body.isEmpty())"" else notice+body,fragments.map{it.copy(start=it.start+notice.length,end=it.end+notice.length)},issues,source.pages,examined,attempts,sha,source.author,source.created)
    }
}
