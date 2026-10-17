package dev.bautista.spotlight.index

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.content.Intent
import com.google.android.gms.tasks.TaskCompletionSource
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.IOException

/** Production extraction/index coordination with recognizer fixtures, not a native OCR claim. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class PdfOcrPipelineTest {
 private val context get()=RuntimeEnvironment.getApplication()
 private class Pages(val values:List<String>):PdfPageSource {
  override val pages get()=values.size
  var rendered=0;val images=mutableListOf<Bitmap>()
  override fun text(page:Int)=values[page]
  override fun render(page:Int,limits:PdfTextLimits)=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888).also{rendered++;images+=it}
  override fun close(){}
 }
 private suspend fun extract(source:Pages,limits:PdfTextLimits=PdfTextLimits(),clock:()->Long={0L},recognize:suspend()->String={"Rehabilitación vestibular y equilibrio reconocido"}):ExtractedDocument {
  val lease=OcrWorkLease{}
  return try{PdfTextPipeline.extract(source,limits,"a".repeat(64),lease,start=0,clock=clock){image,done->try{recognize()}finally{image.recycle();done()}}}finally{lease.close()}
 }
 @Before fun clear(){context.deleteDatabase("search-index.db")}
 @Test fun mixedPagesProduceTraceableSearchableContentAndKeepNativeText()=runBlocking {
  val source=Pages(listOf("Contenido nativo largo y legible que no necesita ningún reconocimiento visual.",""))
  val doc=extract(source)
  assertEquals(listOf("pdf_text","ocr_local"),doc.fragments.map{it.method});assertEquals(1,source.rendered)
  assertTrue(source.images.all{it.isRecycled});assertFalse(doc.partial)
  assertEquals("a".repeat(64),org.json.JSONObject(doc.metadata()).getString("sha256"))
  doc.fragments.forEach{assertTrue(doc.text.substring(it.start,it.end).isNotBlank())}
  val index=FileIndex(context);val uri="content://selected/document/ocr"
  index.storeTranscript(uri,"escaneado.pdf","application/pdf","")
  assertTrue(index.cacheExtraction(uri,doc,AppConfig(contentIndex=true)))
  val row=FileIndex(context).search("kind:pdf vestibular",AppConfig(contentIndex=true)).single()
  assertTrue(row.preview!!.contains("OCR local (revisar) · página 2"));assertTrue(row.preview!!.contains("equilibrio"))
  assertTrue(index.search("vestibular",AppConfig(contentIndex=false)).isEmpty())
  assertTrue("Generated OCR labels must not create content matches",index.search("ocr",AppConfig(contentIndex=true)).isEmpty())
 }
 @Test fun pageAndCharacterLimitsNeverPretendTheWholeDocumentWasRead()=runBlocking {
  val source=Pages(List(5){""});val doc=extract(source,PdfTextLimits(textPages=4,ocrPages=2))
  assertEquals(2,source.rendered);assertTrue(doc.partial)
  assertTrue(doc.issues.any{it.code=="ocr_page_limit"});assertTrue(doc.issues.any{it.code=="page_limit"})
  assertEquals(listOf(1,2),doc.fragments.map{it.page})
  assertTrue(doc.coverage.contains("parcial"));assertTrue(doc.coverage.contains("2 páginas con texto OCR"))
  val long=extract(Pages(listOf("")),PdfTextLimits(characters=1000)){"Texto largo ".repeat(500)}
  assertTrue(long.text.length<=1000);assertTrue(long.issues.any{it.code=="character_limit"});assertTrue(long.text.contains("Extracción parcial"))
 }
 @Test fun deadlinesStopSchedulingAndOcrFailuresDoNotInventText()=runBlocking {
  var now=0L
  val doc=extract(Pages(listOf("","")),PdfTextLimits(totalMillis=10),clock={now}){now=11;"Texto real del fixture"}
  assertEquals(1,doc.ocrAttempts);assertTrue(doc.issues.any{it.code=="time_limit"})
  val failed=extract(Pages(listOf(""))){throw IOException("Fixture OCR error")}
  assertEquals("",failed.text);assertTrue(failed.fragments.isEmpty());assertEquals("ocr_error",failed.issues.single().code)
 }
 @Test fun timedOutTaskOwnsItsImageAndAdmissionUntilActualCompletion()=runBlocking {
  val task=TaskCompletionSource<String>();val source=Pages(listOf(""));var released=0
  val lease=OcrWorkLease{released++}
  val doc=PdfTextPipeline.extract(source,PdfTextLimits(pageMillis=10),"fixture",lease,start=0,clock={0}){image,done->awaitOwnedTask({task.task}){image.recycle();done()}}
  lease.close();assertTrue(doc.partial);assertEquals(0,released);assertFalse(source.images.single().isRecycled)
  task.setResult("late text must never be inserted")
  assertTrue(source.images.single().isRecycled);assertEquals(1,released);assertTrue(doc.text.isBlank())
 }
 @Test fun callerCancellationPropagatesAndDoesNotReleaseAnInFlightImageEarly()=runBlocking {
  val task=TaskCompletionSource<String>();val submitted=CompletableDeferred<Unit>();var released=0
  val source=Pages(listOf(""));val lease=OcrWorkLease{released++}
  val job=launch{try{PdfTextPipeline.extract(source,PdfTextLimits(),"fixture",lease){image,done->submitted.complete(Unit);awaitOwnedTask({task.task}){image.recycle();done()}}}finally{lease.close()}}
  submitted.await();job.cancelAndJoin();assertTrue(job.isCancelled);assertEquals(0,released)
  assertFalse(source.images.single().isRecycled);task.setException(IOException("late failure"))
  assertEquals(1,released);assertTrue(source.images.single().isRecycled)
 }
 @Test fun startFailureAndCanceledTaskCleanUpExactlyOnce()=runBlocking {
  var released=0
  try{awaitOwnedTask<String>({throw IOException("start")}){released++};fail()}catch(_:IOException){}
  assertEquals(1,released)
  val canceled=com.google.android.gms.tasks.Tasks.forCanceled<String>()
  try{awaitOwnedTask({canceled}){released++};fail()}catch(_:CancellationException){}
  assertEquals(2,released)
 }
 @Test fun dimensionsAreBoundedWithoutIntegerOverflow() {
  val limits=PdfTextLimits()
  for((w,h) in listOf(595 to 842,Int.MAX_VALUE to Int.MAX_VALUE,1 to 2000000,2000000 to 1)) {
   val (x,y)=limits.dimensions(w,h);assertTrue(x in 1..1600 && y in 1..1600);assertTrue(x.toLong()*y<=1800000)
  }
 }
 @Test fun cacheNeverCreatesAnImportOrBypassesRevocationOrPrivateMode()=runBlocking {
  val index=FileIndex(context);val doc=extract(Pages(listOf("")))
  assertFalse(index.cacheExtraction("content://missing/1",doc,AppConfig(contentIndex=true)))
  val uri="content://revoked/doc/1";val tree="content://revoked/tree/root"
  index.storeTranscript(uri,"revoked.pdf","application/pdf","")
  context.openOrCreateDatabase("search-index.db",0,null).use{db->db.execSQL("UPDATE files SET tree=? WHERE uri=?",arrayOf(tree,uri))}
  val config=AppConfig(contentIndex=true,trees=setOf(tree))
  assertFalse(index.cacheExtraction(uri,doc,config))
  context.contentResolver.takePersistableUriPermission(Uri.parse(tree),Intent.FLAG_GRANT_READ_URI_PERMISSION)
  assertTrue(index.cacheExtraction(uri,doc,config))
  assertFalse(index.cacheExtraction(uri,doc,config.copy(privateMode=true)))
  context.contentResolver.releasePersistableUriPermission(Uri.parse(tree),Intent.FLAG_GRANT_READ_URI_PERMISSION)
  assertTrue(index.search("vestibular",config).isEmpty());assertFalse(index.cacheExtraction(uri,doc,config))
 }
 @Test fun versionOneIndexMigratesWithoutDeletingExistingContent()=runBlocking {
  context.openOrCreateDatabase("search-index.db",0,null).use{db->
   db.execSQL("CREATE TABLE files(uri TEXT PRIMARY KEY,tree TEXT,parent TEXT,name TEXT,mime TEXT,modified INTEGER,size INTEGER,author TEXT,created INTEGER,body TEXT,mode TEXT)")
   db.execSQL("INSERT INTO files(uri,tree,name,mime,modified,body) VALUES('content://legacy/1','__imports__','conservado.pdf','application/pdf',0,'contenido anterior conservado')");db.version=1
  }
  val row=FileIndex(context).search("conservado",AppConfig(contentIndex=true)).single();assertEquals("conservado.pdf",row.title)
  context.openOrCreateDatabase("search-index.db",0,null).use{db->
   assertEquals(3,db.version)
   db.rawQuery("SELECT extraction,body FROM files",null).use{assertTrue(it.moveToFirst());assertEquals("",it.getString(0));assertEquals("contenido anterior conservado",it.getString(1));assertFalse(it.moveToNext())}
   db.rawQuery("SELECT COUNT(*) FROM semantic_vectors",null).use{assertTrue(it.moveToFirst());assertEquals(0,it.getInt(0))}
  }
 }
}
