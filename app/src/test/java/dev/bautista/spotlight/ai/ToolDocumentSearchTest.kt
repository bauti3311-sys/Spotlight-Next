package dev.bautista.spotlight.ai

import android.app.Application
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.DeviceResourceState
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.index.FileIndex
import dev.bautista.spotlight.search.SearchEngine
import dev.bautista.spotlight.semantic.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig

/** Actual registry→executor→SearchEngine→SQLite; embedding fixture only, not model accuracy. */
@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=SpotlightApp::class)
class ToolDocumentSearchTest {
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 private var config=Config(files=true,contentIndex=true,semanticSearch=true)
 private val backend=object:DocumentEmbedder {
  override suspend fun available()=true
  override suspend fun fragments(text:String)=WordPiece.Fragments(listOf(WordPiece.Fragment(0,0,text.length,text,longArrayOf(101,102))),false)
  override suspend fun embed(ids:LongArray)=FloatArray(512){if(it==0)1f else 0f}
  override suspend fun query(text:String)=embed(longArrayOf(101,102))
  override fun invalidate(){}
 }
 private val resources=DeviceResourceState(80,false,false,2L*1024*1024*1024,128L*1024*1024,false,0)
 private fun index()=FileIndex(app,resourceState={resources},liveConfig={config},semanticBackend=backend)
 @Before fun reset(){runBlocking{app.index.clear()}}
 @Test fun explicitFileToolUsesUniversalLocalSemanticsAndReturnsNoExcerpt()=runBlocking {
  val index=index();index.storeTranscript("content://authorized/tool/1","protocolo.txt","text/plain","Ejercicios vestibulares. TEXTO_PRIVADO_NO_AUTORIZADO_PARA_ENVIO")
  index.buildSemantic(config)
  assertTrue(index.search("marcha segura",config).isEmpty())
  val executor=ToolExecutor(app,SearchEngine(app,app.preferences,app.local,index))
  val result=executor.execute(ToolCall("search_files",mapOf("query" to "marcha segura")),config)
  assertEquals(ExecutionState.COMPLETED,result.state);assertNull(result.action)
  assertTrue(result.text.contains("protocolo.txt"));assertTrue(result.text.contains("Por significado"))
  assertFalse(result.text.contains("TEXTO_PRIVADO"));assertFalse(result.text.contains("Ejercicios vestibulares"))
 }
 @Test fun defaultProductionWiringRetainsLexicalSearchAndNeverExpandsLiteralQueries()=runBlocking {
  app.index.storeTranscript("content://authorized/tool/2","factura.pdf","application/pdf","Saldo pendiente con contenido reservado")
  val result=ToolExecutor(app).execute(ToolCall("search_files",mapOf("query" to "PDF factura NOT pagado")),config.copy(semanticSearch=false))
  assertTrue(result.text.contains("factura.pdf"));assertFalse(result.text.contains("contenido reservado"))
 }
 @Test fun disabledFilesCannotExecuteEvenIfTheIndexContainsMatches()=runBlocking {
  app.index.storeTranscript("content://authorized/tool/3","factura.pdf","application/pdf","contenido")
  try{ToolExecutor(app).execute(ToolCall("search_files",mapOf("query" to "factura")),config.copy(files=false));fail("disabled tool executed")}
  catch(e:IllegalArgumentException){assertTrue(e.message!!.contains("disponible"))}
 }
 @Test fun unsupportedOpeningDateIsReportedInsteadOfSubstitutingModificationDate()=runBlocking {
  app.index.storeTranscript("content://authorized/tool/4","ayer.pdf","application/pdf","contenido reservado")
  val result=ToolExecutor(app).execute(ToolCall("search_files",mapOf("query" to "archivos abiertos ayer")),config.copy(semanticSearch=false))
  assertTrue(result.text.contains("No hay historial de apertura"))
  assertTrue(result.text.contains("No se filtra por modificación"))
  assertFalse(result.text.contains("ayer.pdf"));assertFalse(result.text.contains("contenido reservado"))
 }

}
