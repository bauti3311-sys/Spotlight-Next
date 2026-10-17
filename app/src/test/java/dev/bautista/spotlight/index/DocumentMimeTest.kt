package dev.bautista.spotlight.index

import android.app.Application
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class DocumentMimeTest {
 @Test fun genericKnownDocumentsUseTheCorrectExtractorWithoutGuessingOtherFormats() {
  assertEquals("application/pdf",OfficeText.mime("Anatomia.PDF","application/octet-stream"))
  assertEquals("text/plain",OfficeText.mime("notas.MD",null))
  assertEquals("text/plain",OfficeText.mime("texto.txt",""))
  assertEquals("image/png",OfficeText.mime("imagen.pdf","image/png"))
  assertEquals("application/octet-stream",OfficeText.mime("programa.apk","application/octet-stream"))
  assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",OfficeText.mime("archivo.docx","application/zip"))
 }
 @Test fun oldGenericPdfRowsReachQuickLookWithCanonicalMimeWithoutReindexing()=runBlocking {
  val context=RuntimeEnvironment.getApplication();context.deleteDatabase("search-index.db")
  val index=FileIndex(context);index.storeTranscript("content://authorized/pdf/1","anatomia.PDF","application/octet-stream","Texto de anatomía")
  val row=index.search("kind:pdf anatomia",AppConfig(contentIndex=true)).single()
  assertEquals("application/pdf",row.mime)
  assertEquals("application/pdf",(row.action as Action.Launch).intent.type)
 }
}
