package dev.bautista.spotlight.index

import android.content.*
import android.net.Uri
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig
import java.io.File
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=android.app.Application::class)
class DocumentCitationsTest {
 private val context get()=RuntimeEnvironment.getApplication()
 private var c=Config(files=true,contentIndex=true,online=false)
 private lateinit var index:FileIndex
 private val files=mutableListOf<File>()
 @Before fun setup(){context.deleteDatabase("search-index.db");index=FileIndex(context,liveConfig={c})}
 @After fun cleanup(){files.forEach{it.delete()}}
 private suspend fun seed(name:String,text:String,page:Int?=null,ocr:Boolean=false):String {
  val f=File(context.cacheDir,name).apply{writeText(text)};files+=f;val uri=Uri.fromFile(f).toString()
  index.storeTranscript(uri,name,if(page==null)"text/plain" else "application/pdf",text)
  val sha=MessageDigest.getInstance("SHA-256").digest(f.readBytes()).joinToString(""){"%02x".format(it)}
  val doc=ExtractedDocument(text,if(page==null)emptyList() else listOf(TextFragment(page,if(ocr)"ocr_local" else "pdf_text",0,text.length)),emptyList(),page ?: 0,page ?: 0,if(ocr)1 else 0,sha)
  assertTrue(index.cacheExtraction(uri,doc,c));return uri
 }
 @Test fun threeOfflineNaturalQueriesReturnLiteralAttributableSources()=runBlocking {
  seed("plan.txt","El presupuesto aprobado del proyecto es de 2400 pesos.")
  seed("agenda.txt","La reunión será el martes a las 15 horas.")
  seed("escaneo.pdf","La dosis aparente es 1O mg; revisar la imagen original.",page=2,ocr=true)
  DocumentCitationSession(index,{c}).use { session ->
   for((query,expected,page) in listOf(Triple("buscar archivos de presupuesto aprobado","plan.txt",null),Triple("buscar archivos de reunión martes","agenda.txt",null),Triple("buscar archivos de dosis aparente","escaneo.pdf",2))) {
    val refs=session.retrieve(query);assertEquals(query,1,refs.size)
    val hit=session.resolve(refs.single())!!;assertEquals(expected,hit.documentTitle);assertEquals(page,hit.page)
    assertEquals(files.first{it.name==expected}.readText().substring(hit.start,hit.end),hit.quote)
    assertEquals(expected=="escaneo.pdf",hit.uncertain);assertEquals(refs.single().sourceId,hit.sourceId)
    println("CORPUS query=$query source=${hit.documentTitle} page=${hit.page} method=${hit.method} uncertain=${hit.uncertain} quote=${hit.quote}")
   }
  }
 }
 @Test fun revokedSafGrantInvalidatesAnAlreadyReturnedHandle()=runBlocking {
  val uri=seed("secreto.txt","Contrato local con importe acordado.")
  val tree=Uri.parse("content://fixture/tree/root");c=c.copy(trees=setOf(tree.toString()))
  context.openOrCreateDatabase("search-index.db",0,null).use{db->db.execSQL("UPDATE files SET tree=? WHERE uri=?",arrayOf(tree.toString(),uri))}
  context.contentResolver.takePersistableUriPermission(tree,Intent.FLAG_GRANT_READ_URI_PERMISSION)
  val s=DocumentCitationSession(index,{c});val ref=s.retrieve("importe acordado").single();assertNotNull(s.resolve(ref))
  context.contentResolver.releasePersistableUriPermission(tree,Intent.FLAG_GRANT_READ_URI_PERMISSION)
  assertNull(s.resolve(ref));assertTrue(s.retrieve("importe acordado").isEmpty());s.close()
 }
 @Test fun deletingSourceInvalidatesHandleDespiteCachedIndexText()=runBlocking {
  seed("deleted.txt","Proyecto lunar con calendario confirmado.");val s=DocumentCitationSession(index,{c});val ref=s.retrieve("calendario confirmado").single()
  files.single().delete();assertNull(s.resolve(ref));s.close()
 }
 @Test fun sameLengthSourceReplacementCannotReuseCachedQuote()=runBlocking {
  seed("changed.txt","Documento inicial con plazo vigente.");val s=DocumentCitationSession(index,{c});val ref=s.retrieve("plazo vigente").single()
  val f=files.single();f.writeText(f.readText().replace("inicial","alterad"));assertNull(s.resolve(ref));s.close()
 }
 @Test fun cancellationInvalidatesOldHandlesAndPublishesNothing()=runBlocking {
  seed("cancel.txt","Revisión del convenio anual.");val s=DocumentCitationSession(index,{c});val ref=s.retrieve("convenio anual").single()
  val pending=launch(start=CoroutineStart.UNDISPATCHED){s.retrieve("convenio anual")};pending.cancelAndJoin()
  assertNull(s.resolve(ref));s.close()
 }
 @Test fun closingSessionAndNewQueriesInvalidateOldReferences()=runBlocking {
  seed("close.txt","Revisión del convenio anual.");val s=DocumentCitationSession(index,{c});val old=s.retrieve("convenio anual").single()
  assertTrue(s.retrieve("noexistexyz").isEmpty());assertNull(s.resolve(old))
  val ref=s.retrieve("convenio anual").single();s.close();assertNull(s.resolve(ref))
 }
 @Test fun privacyChangeAndIndexDeletionInvalidateReferences()=runBlocking {
  seed("private.txt","Informe fiscal anual.");val s=DocumentCitationSession(index,{c});val ref=s.retrieve("fiscal anual").single()
  c=c.copy(privateMode=true);assertNull(s.resolve(ref));c=c.copy(privateMode=false)
  val next=s.retrieve("fiscal anual").single();index.clear();assertNull(s.resolve(next));s.close()
 }
 @Test fun invalidPageProvenanceIsNeverFabricated()=runBlocking {
  val uri=seed("bad.txt","Archivo local con contenido verificable.")
  context.openOrCreateDatabase("search-index.db",0,null).use{db->db.execSQL("UPDATE files SET extraction=replace(extraction,'\"pages\":0','\"pages\":3') WHERE uri=?",arrayOf(uri))}
  DocumentCitationSession(index,{c}).use{assertTrue(it.retrieve("contenido verificable").isEmpty())}
 }
 @Test fun legacyRowsWithoutByteFingerprintCannotProduceCitations()=runBlocking {
  index.storeTranscript("content://legacy/unknown","old.txt","text/plain","Contenido sin procedencia verificable")
  DocumentCitationSession(index,{c}).use{assertTrue(it.retrieve("procedencia verificable").isEmpty())}
 }
 @Test fun nonPdfCannotAdvertisePagesAndForeignSessionCannotResolveHandle()=runBlocking {
  val uri=seed("ordinary.txt","Texto local con frase exclusiva.",page=1)
  context.openOrCreateDatabase("search-index.db",0,null).use{it.execSQL("UPDATE files SET mime='text/plain' WHERE uri=?",arrayOf(uri))}
  DocumentCitationSession(index,{c}).use{assertTrue(it.retrieve("frase exclusiva").isEmpty())}
  seed("second.txt","Otra frase sobre calendario escolar.")
  DocumentCitationSession(index,{c}).use{a->DocumentCitationSession(index,{c}).use{b->val ref=a.retrieve("calendario escolar").single();assertNull(b.resolve(ref));assertNotNull(a.resolve(ref))}}
 }

}
