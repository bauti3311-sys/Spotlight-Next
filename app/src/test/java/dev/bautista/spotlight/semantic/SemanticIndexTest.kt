package dev.bautista.spotlight.semantic

import android.app.Application
import dev.bautista.spotlight.control.DeviceResourceState
import dev.bautista.spotlight.control.SearchScope
import dev.bautista.spotlight.data.*
import dev.bautista.spotlight.index.FileIndex
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig
import java.util.concurrent.atomic.AtomicInteger

/** Dependency fixture tests production DB/authorization/flow, NOT real model accuracy. */
@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class SemanticIndexTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private var config=Config(contentIndex=true,semanticSearch=true)
    private var resources=DeviceResourceState(80,false,false,2L*1024*1024*1024,128L*1024*1024,false,0)
    private val calls=AtomicInteger()
    private var during:(suspend ()->Unit)?=null
    private val backend=object:DocumentEmbedder {
        override suspend fun available()=true
        override suspend fun fragments(text:String)=WordPiece.Fragments(listOf(WordPiece.Fragment(0,0,text.length,text,longArrayOf(101,102))),false)
        override suspend fun query(text:String)=FloatArray(512){if(it==0)1f else 0f}
        override suspend fun embed(ids:LongArray):FloatArray {calls.incrementAndGet();during?.invoke();return FloatArray(512){if(it==0)1f else 0f}}
        override fun invalidate(){}
    }
    private fun index()=FileIndex(context,resourceState={resources},liveConfig={config},semanticBackend=backend)
    @Before fun reset(){context.deleteDatabase("search-index.db")}
    private suspend fun seed(index:FileIndex){index.storeTranscript("content://authorized/doc/1","protocolo.txt","text/plain","Ejercicios para el equilibrio y la estabilidad de la mirada.")}
    @Test fun semanticRetrievesWithoutLiteralOverlapAndKeepsNativeFileAction()=runBlocking {
        val index=index();seed(index);index.buildSemantic(config)
        assertTrue(index.search("marcha segura",config).isEmpty())
        val result=index.semanticSearch("marcha segura",config).single()
        assertTrue(result.semanticMatch);assertTrue(result.action is Action.Launch)
        assertEquals("file:content://authorized/doc/1",result.id)
        assertTrue(result.preview!!.contains("Ejercicios para el equilibrio"))
        assertTrue(result.preview!!.contains("cobertura acotada"))
        assertEquals("text/plain",result.mime)
    }
    @Test fun rebuiltIndexResumesCachedFragmentsAndNeverReturnsStaleText()=runBlocking {
        val index=index();seed(index);index.buildSemantic(config);assertEquals(1,calls.get())
        index.buildSemantic(config);assertEquals(1,calls.get())
        index.storeTranscript("content://authorized/doc/1","protocolo.txt","text/plain","Otro texto actualizado con contenido diferente.")
        assertTrue(index.semanticSearch("marcha segura",config).isEmpty())
        index.buildSemantic(config);assertEquals(2,calls.get())
        assertTrue(index.semanticSearch("marcha segura",config).single().preview!!.contains("Otro texto actualizado"))
    }
    @Test fun privacyAndResourceChangesDuringInferenceDiscardThePendingWrite()=runBlocking {
        val index=index();seed(index)
        during={config=config.copy(privateMode=true)};index.buildSemantic(config)
        assertTrue(index.semanticSearch("marcha segura",config.copy(privateMode=false)).isEmpty())
        config=config.copy(privateMode=false);during=null;resources=resources.copy(powerSave=true)
        index.buildSemantic(config);assertEquals(1,calls.get());assertTrue(index.semanticStatus.value.partial)
        resources=resources.copy(powerSave=false);index.buildSemantic(config)
        assertEquals(2,calls.get());assertEquals(1,index.semanticSearch("marcha segura",config).size)
    }
    @Test fun cancelledPreparationCannotPublishAFragmentAndCanResume()=runBlocking {
        val index=index();seed(index);val entered=CompletableDeferred<Unit>();val finish=CompletableDeferred<Unit>()
        during={entered.complete(Unit);withContext(NonCancellable){finish.await()}}
        val job=launch{index.buildSemantic(config)};withTimeout(5000){entered.await()};job.cancel();finish.complete(Unit);job.join()
        assertTrue(index.semanticSearch("marcha segura",config).isEmpty())
        during=null;index.buildSemantic(config);assertEquals(1,index.semanticSearch("marcha segura",config).size)
    }
    @Test fun literalQueriesAndExplicitTypeFiltersCannotBeRelaxed()=runBlocking {
        val index=index();seed(index);index.buildSemantic(config)
        assertTrue(index.semanticSearch("\"marcha segura\"",config).isEmpty())
        assertTrue(index.semanticSearch("marcha NOT mirada",config).isEmpty())
        assertTrue(index.semanticSearch("kind:pdf marcha segura",config).isEmpty())
        assertEquals(1,index.semanticSearch("kind:document marcha segura",config).size)
    }
    @Test fun searchEnginePublishesLexicalBeforeDelayedSemanticAndRanksExactAppsAboveIt()=runBlocking {
        val index=index();seed(index);index.buildSemantic(config)
        val prefs=Preferences(context);val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
        try {
            val engine=SearchEngine(context,prefs,LocalStore(prefs,scope),index)
            val snapshots=withTimeout(5000){engine.search("marcha segura",config,SearchScope("","files-mode")).toList()}
            assertTrue(snapshots.first().isEmpty());assertTrue(snapshots.last().single().semanticMatch)
            val semantic=snapshots.last().single()
            val app=Result("app","marcha segura","Instalada","Aplicaciones",1,Action.Copy("app"))
            assertEquals("app",ResultRanker.rank("marcha segura",listOf(semantic,app),config).first().id)
        }finally{scope.cancel()}
    }
    @Test fun clearRemovesOnlyDerivedVectorsAndKeepsTheTextualIndex()=runBlocking {
        val index=index();seed(index);index.buildSemantic(config);index.clearSemantic()
        assertTrue(index.semanticSearch("marcha segura",config).isEmpty())
        assertEquals(1,index.search("equilibrio",config).size)
    }
    @Test fun revokedSafGrantDuringInferenceCannotPublishOrReturnAFragment()=runBlocking {
        val index=index();seed(index)
        val tree=android.net.Uri.parse("content://authorized/tree/root")
        context.contentResolver.takePersistableUriPermission(tree,android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        config=config.copy(trees=setOf(tree.toString()))
        context.openOrCreateDatabase("search-index.db",0,null).use{db->db.execSQL("UPDATE files SET tree=?",arrayOf(tree.toString()))}
        during={context.contentResolver.releasePersistableUriPermission(tree,android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)}
        index.buildSemantic(config)
        assertEquals(0,index.semanticStatus.value.fragments)
        assertTrue(index.semanticSearch("marcha segura",config).isEmpty())
        during=null
        context.contentResolver.takePersistableUriPermission(tree,android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        index.buildSemantic(config);assertEquals(1,index.semanticSearch("marcha segura",config).size)
    }
    @Test fun changingSourceWhileInferenceRunsDiscardsTheOldFragmentAndKeepsNewText()=runBlocking {
        val index=index();seed(index)
        during={index.storeTranscript("content://authorized/doc/1","protocolo.txt","text/plain","Contenido nuevo autorizado y conservado.")}
        index.buildSemantic(config)
        assertTrue(index.semanticSearch("marcha segura",config).isEmpty())
        assertEquals(1,index.search("conservado",config).size)
        during=null;index.buildSemantic(config)
        assertTrue(index.semanticSearch("marcha segura",config).single().preview!!.contains("Contenido nuevo"))
    }
    @Test fun versionTwoMigrationAddsVectorsWithoutResettingTheTextIndex()=runBlocking {
        val legacy=context.openOrCreateDatabase("search-index.db",0,null)
        legacy.execSQL("CREATE TABLE files(uri TEXT PRIMARY KEY,tree TEXT,name TEXT,mime TEXT,modified INTEGER,size INTEGER,parent TEXT,body TEXT,author TEXT,created INTEGER,seen INTEGER,extraction TEXT)")
        legacy.execSQL("INSERT INTO files(uri,tree,name,mime,body) VALUES('content://migration/1','__imports__','guardado.txt','text/plain','contenido legado conservado')")
        legacy.version=2;legacy.close()
        val index=index()
        assertEquals(1,index.search("legado",config).size)
        index.buildSemantic(config);assertEquals(1,index.semanticSearch("marcha segura",config).size)
    }

}
