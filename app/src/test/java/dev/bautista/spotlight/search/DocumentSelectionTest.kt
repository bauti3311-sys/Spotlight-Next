package dev.bautista.spotlight.search

import android.app.Application
import android.content.Intent
import android.net.Uri
import dev.bautista.spotlight.data.FileBuffer
import dev.bautista.spotlight.index.*
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class DocumentSelectionTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private fun row(n:Int,mime:String="text/plain",uri:Uri=Uri.parse("content://selected/$n"))=Result("selected:$n","documento$n","","Archivos",1,Action.Launch(Intent(Intent.ACTION_VIEW,uri)),mime=mime)
    private fun plain(text:String)=ExtractedDocument(text,emptyList(),emptyList(),0,0,0,"a".repeat(64))
    @Test fun actualTextFilesKeepBothContentsAndExactSourceHashes()=runBlocking {
        val files=(1..2).map{File.createTempFile("selection-",".txt",context.cacheDir).apply{writeText("Contenido autorizado documento $it")}}
        try {
            val buffer=FileBuffer();files.forEachIndexed{i,file->buffer.add(row(i,uri=Uri.fromFile(file)))}
            val text=buffer.text(context)
            files.forEachIndexed{i,file->
                assertTrue(text.contains("[D${i+1}]"));assertTrue(text.contains(file.readText()))
                val sha=MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString(""){"%02x".format(it)}
                assertTrue(text.contains(sha))
            }
            assertFalse(text.contains("content://"));assertFalse(text.contains(context.cacheDir.path))
        } finally{files.forEach{it.delete()}}
    }
    @Test fun twelveLargeDocumentsKeepEveryHeaderAndFooterWithinOneBudget()=runBlocking {
        val buffer=FileBuffer{_,_,_,_->plain("texto real ".repeat(3000))}
        (1..12).forEach{buffer.add(row(it).copy(title="nombre\n"+"largo".repeat(200)))}
        val text=buffer.text(context)
        assertTrue(text.length<=16000)
        (1..12).forEach{assertTrue(text.contains("[D$it]"));assertTrue(text.contains("[Fin D$it ·"))}
        assertTrue(text.contains("Recorte adicional"));assertFalse(text.contains("nombre\n"))
    }
    @Test fun pdfExcerptPreservesPageMethodAndPartialCoverageWithoutGeneratedText()=runBlocking {
        val body="AVISO GENERADO\n"+"fragmento clínico ".repeat(1000)
        val document=ExtractedDocument(body,listOf(TextFragment(4,"ocr_local",15,body.length)),listOf(ExtractionIssue("page_limit")),60,4,1,"b".repeat(64))
        val buffer=FileBuffer{_,_,_,_->document};buffer.add(row(1,"application/pdf"))
        val text=buffer.text(context)
        assertTrue(text.contains("página 4 · OCR local (revisar)"));assertTrue(text.contains("4 de 60 páginas"))
        assertTrue(text.contains("parcial"));assertTrue(text.contains("Recorte adicional"));assertFalse(text.contains("AVISO GENERADO"))
        assertTrue(text.length<=8000)
    }
    @Test fun invalidProvenanceAndPermissionFailureNeverProducePartialDrafts()=runBlocking {
        val invalid=FileBuffer{_,_,_,_->ExtractedDocument("texto",listOf(TextFragment(1,"ocr_local",0,999)),emptyList(),1,1,1,"c".repeat(64))}
        invalid.add(row(1,"application/pdf"))
        try{invalid.text(context);fail("Invalid provenance must fail")}catch(e:IllegalArgumentException){assertTrue(e.message!!.contains("Procedencia"))}
        var calls=0
        val denied=FileBuffer{_,_,_,_->if(++calls==2)throw SecurityException("Permiso revocado") else plain("primera fuente")}
        denied.add(row(1));denied.add(row(2))
        try{denied.text(context);fail("Revoked source must fail the draft")}catch(e:SecurityException){assertEquals(2,calls)}
    }
    @Test fun nestedExtractorTimeoutIsAnExplicitFailureRatherThanSilentCancellation()=runBlocking {
        val buffer=FileBuffer{_,_,_,_->withTimeout(1){delay(10000);plain("late")}}
        buffer.add(row(1))
        try{buffer.text(context);fail("Nested timeout must report its document")}catch(e:IllegalStateException){assertTrue(e.message!!.contains("agotó su tiempo: documento1"));assertTrue(coroutineContext.isActive)}
    }
    @Test fun cancellingPreparationDoesNotExtractTheNextDocument()=runBlocking {
        val started=CompletableDeferred<Unit>();var calls=0
        val buffer=FileBuffer{_,_,_,_->calls++;started.complete(Unit);awaitCancellation()}
        buffer.add(row(1));buffer.add(row(2))
        val job=async{buffer.text(context)};withTimeout(5000){started.await()};job.cancelAndJoin()
        assertTrue(job.isCancelled);assertEquals(1,calls)
    }
    @Test fun selectionOcrAdmissionHasSixPageBudgetAndReportsUnhashedImages()=runBlocking {
        val caps=mutableListOf<Int>()
        val buffer=FileBuffer{_,_,_,limits->caps+=limits.ocrPages;plain("documento").copy(ocrAttempts=limits.ocrPages)}
        (1..3).forEach{buffer.add(row(it,"application/pdf"))}
        buffer.text(context);assertEquals(listOf(3,3,0),caps)
        var calls=0
        val images=FileBuffer{_,_,_,_->calls++;plain("texto imagen").copy(sourceSha256="")}
        (1..7).forEach{images.add(row(it,"image/png"))}
        try{images.text(context);fail("Image OCR must be bounded")}catch(e:IllegalStateException){assertTrue(e.message!!.contains("presupuesto OCR"));assertEquals(6,calls)}
        images.remove("selected:7");assertTrue(images.text(context).contains("no disponible; revisar fuente"))
    }
}
