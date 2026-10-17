package dev.bautista.spotlight

import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.bautista.spotlight.index.*
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

/** Real PdfRenderer + bundled Latin ML Kit. Run on a clean authorized emulator/test install. */
@RunWith(AndroidJUnit4::class)
class PdfOcrDeviceTest {
 private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
 private fun scanned():File {
  val bitmap=Bitmap.createBitmap(1000,1400,Bitmap.Config.ARGB_8888)
  val canvas=Canvas(bitmap);canvas.drawColor(Color.WHITE)
  val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.BLACK;textSize=50f;typeface=Typeface.DEFAULT_BOLD}
  canvas.drawText("SPOTLIGHT OCR LOCAL",60f,200f,paint);canvas.drawText("VESTIBULAR EQUILIBRIO",60f,300f,paint)
  val pdf=PdfDocument();val file=File(context.cacheDir,"ocr-instrumented-scan.pdf")
  try {repeat(2){i->val page=pdf.startPage(PdfDocument.PageInfo.Builder(500,700,i+1).create());page.canvas.drawBitmap(bitmap,null,Rect(0,0,500,700),null);pdf.finishPage(page)};file.outputStream().use{pdf.writeTo(it)}}
  finally{pdf.close();bitmap.recycle()};return file
 }
 @Test fun scannedBitmapPdfIsRecognizedLocallyAndIndexedWithPageProvenance()=runBlocking {
  val file=scanned()
  try {
   PDFBoxResourceLoader.init(context)
   PDDocument.load(file).use{assertTrue("Fixture must contain images, not embedded searchable text",PDFTextStripper().getText(it).isBlank())}
   val result=withTimeout(65000){DocumentText.extractDetailed(context,Uri.fromFile(file),"application/pdf",PdfTextLimits(textPages=1,ocrPages=1,totalMillis=60000,pageMillis=15000))}
   assertTrue(result.text,result.text.contains("VESTIBULAR",ignoreCase=true));assertTrue(result.partial)
   assertEquals(2,result.pages);assertEquals(1,result.fragments.single().page);assertEquals("ocr_local",result.fragments.single().method)
   assertTrue(result.fragments.single().width.toLong()*result.fragments.single().height<=1800000)
   assertEquals(64,result.sourceSha256.length)
   val index=FileIndex(context);val uri="content://instrumented-ocr/authorized-copy"
   index.storeTranscript(uri,"instrumented-ocr.pdf","application/pdf","")
   assertTrue(index.cacheExtraction(uri,result,dev.bautista.spotlight.data.Config(contentIndex=true)))
   assertTrue(index.search("kind:pdf VESTIBULAR",dev.bautista.spotlight.data.Config(contentIndex=true)).any{it.preview?.contains("OCR local (revisar) · página 1")==true})
  }finally{
   file.delete()
   if(context.getDatabasePath("search-index.db").exists())context.openOrCreateDatabase("search-index.db",0,null).use{db->db.delete("files","uri=?",arrayOf("content://instrumented-ocr/authorized-copy"))}
  }
 }
}
