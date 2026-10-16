package dev.bautista.spotlight.index

import android.app.Application
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.QueryInterpreter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class DocumentRetrievalTest {
 @Test fun naturalDocumentRequestFindsAuthorizedDeepTextAndRelevantExcerpt()=runBlocking {
  val context=RuntimeEnvironment.getApplication();context.deleteDatabase("search-index.db")
  val index=FileIndex(context)
  val body="Introducción. ".repeat(900)+"Rehabilitación vestibular y equilibrio. "+"Final. ".repeat(900)
  index.storeTranscript("content://fixture/document/1","capitulo.pdf","application/pdf",body)
  val query=QueryInterpreter.files("Buscá documentos PDF que hablan sobre rehabilitación vestibular")!!.query
  assertEquals("kind:pdf rehabilitacion vestibular",query)
  val row=index.search(query,AppConfig(contentIndex=true)).single()
  assertEquals("capitulo.pdf",row.title)
  assertTrue(row.preview!!.contains("Rehabilitación vestibular"))
  assertFalse(index.search(query,AppConfig(contentIndex=false)).isNotEmpty())
 }
 @Test fun excerptsRemainBoundedAndHandleDecomposedAccents() {
  val text="a\u0301".repeat(6000)+" evidencia vestibular "+"final ".repeat(1000)
  val excerpt=DocumentExcerpt.around(text,"vestibular",600)
  assertTrue(excerpt.contains("vestibular"));assertTrue(excerpt.length<=602)
  assertTrue(excerpt.startsWith("…"))
 }
 @Test fun literalQueriesKeepTheirGrammar() {
  assertNull(QueryInterpreter.files("kind:pdf \"que hablan sobre anatomía\""))
  assertNull(QueryInterpreter.files("PDF anatomía AND vestibular"))
 }
}
