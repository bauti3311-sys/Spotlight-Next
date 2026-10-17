package dev.bautista.spotlight.index

import android.app.Application
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.*
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class DocumentTextSafetyTest {
 private val context get()=RuntimeEnvironment.getApplication()
 @Test fun nativePdfUsesActualBoundedSnapshotWithoutStartingOcr()=runBlocking {
  PDFBoxResourceLoader.init(context)
  val file=File(context.cacheDir,"native-extraction.pdf")
  try {
   PDDocument().use{doc->repeat(2){val page=PDPage();doc.addPage(page);PDPageContentStream(doc,page).use{stream->stream.beginText();stream.setFont(PDType1Font.HELVETICA,14f);stream.newLineAtOffset(20f,700f);stream.showText("Rehabilitacion vestibular y equilibrio con ejercicios locales autorizados");stream.endText()}};doc.save(file)}
   val result=DocumentText.extractDetailed(context,Uri.fromFile(file),"application/pdf")
   assertEquals(2,result.pages);assertEquals(0,result.ocrAttempts);assertFalse(result.partial)
   assertEquals(listOf("pdf_text","pdf_text"),result.fragments.map{it.method})
   val sha=MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString(""){"%02x".format(it)}
   assertEquals(sha,result.sourceSha256);assertTrue(result.text.contains("vestibular"))
   assertTrue(PdfTextPipeline.admission.tryAcquire());PdfTextPipeline.admission.release()
  }finally{file.delete()}
 }
 @Test fun oversizedAndCorruptPdfFailWithoutKeepingAdmissionOrPrivateSnapshots()=runBlocking {
  val file=File(context.cacheDir,"bad-extraction.pdf")
  try {
   file.outputStream().use{out->repeat(8){out.write(ByteArray(1000000))};out.write(1)}
   try{DocumentText.extract(context,Uri.fromFile(file),"application/pdf");fail()}catch(_:IllegalArgumentException){}
   assertTrue(PdfTextPipeline.admission.tryAcquire());PdfTextPipeline.admission.release()
   file.writeText("not a PDF")
   try{DocumentText.extract(context,Uri.fromFile(file),"application/pdf");fail()}catch(_:Exception){}
   assertTrue(PdfTextPipeline.admission.tryAcquire());PdfTextPipeline.admission.release()
  }finally{file.delete()}
 }
 @Test fun abandonedSnapshotCleanupTouchesOnlyOwnedOldSessionFiles() {
  val directory=File(context.cacheDir,"pdf-ocr-snapshots").apply{mkdirs()}
  val old=File(directory,"scan-old-session-test.pdf").apply{writeText("private fixture")}
  val other=File(directory,"keep.txt").apply{writeText("keep")}
  val current=PdfSnapshotFiles.create(context)
  try{PdfSnapshotFiles.removeAbandoned(context);assertFalse(old.exists());assertTrue(other.exists());assertTrue(current.exists())}
  finally{other.delete();current.delete();old.delete()}
 }
}
