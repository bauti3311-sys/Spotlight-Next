package dev.bautista.spotlight.index

import android.app.Application
import android.content.*
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig
import org.robolectric.shadows.ShadowContentResolver
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Android DocumentsProvider implementation hosted by Robolectric. NOT Android instrumentation. */
class CitationDocumentsProvider:DocumentsProvider() {
    lateinit var file:File
    @Volatile var inaccessible=false
    @Volatile var entered:CountDownLatch?=null
    @Volatile var release:CountDownLatch?=null
    override fun onCreate():Boolean {file=File(context!!.cacheDir,"c5-saf.txt");return true}
    override fun isChildDocument(parentDocumentId:String,documentId:String)=parentDocumentId=="root" && documentId=="doc"
    override fun queryRoots(projection:Array<out String>?):Cursor=MatrixCursor(arrayOf(DocumentsContract.Root.COLUMN_ROOT_ID,DocumentsContract.Root.COLUMN_DOCUMENT_ID)).apply{addRow(arrayOf("root","root"))}
    override fun queryDocument(documentId:String,projection:Array<out String>?):Cursor=MatrixCursor(arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE)).apply{addRow(arrayOf(documentId,"c5-saf.txt","text/plain"))}
    override fun queryChildDocuments(parentDocumentId:String,projection:Array<out String>?,sortOrder:String?):Cursor=queryDocument("doc",projection)
    override fun openDocument(documentId:String,mode:String,signal:CancellationSignal?):ParcelFileDescriptor {
        entered?.countDown()
        release?.let{if(!it.await(3,TimeUnit.SECONDS))throw FileNotFoundException("Fixture provider blocked")}
        if(inaccessible)throw FileNotFoundException("Fixture provider unavailable")
        return ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY)
    }
}

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class CitationSafStateTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private val tree=DocumentsContract.buildTreeDocumentUri("c5-fixture","root")
    private val uri=DocumentsContract.buildDocumentUriUsingTree(tree,"doc")
    private lateinit var provider:CitationDocumentsProvider
    private lateinit var config:MutableStateFlow<Config>
    private lateinit var index:FileIndex
    private lateinit var parent:CoroutineScope
    private lateinit var state:DocumentCitationState
    @Before fun setup()=runBlocking {
        context.deleteDatabase("search-index.db")
        val app=context
        provider=CitationDocumentsProvider().apply{attachInfo(app,ProviderInfo().apply{
            authority="c5-fixture";exported=true;grantUriPermissions=true
            readPermission="android.permission.MANAGE_DOCUMENTS";writePermission=readPermission
        })}
        ShadowContentResolver.registerProviderInternal("c5-fixture",provider)
        provider.file.writeText("El convenio local tiene presupuesto de 2400 pesos y revisión del martes.")
        context.contentResolver.takePersistableUriPermission(tree,Intent.FLAG_GRANT_READ_URI_PERMISSION)
        config=MutableStateFlow(Config(files=true,contentIndex=true,online=false,trees=setOf(tree.toString())))
        index=FileIndex(context,liveConfig={config.value})
        // Existing index import insertion followed by its actual SAF ownership; no forged extraction metadata.
        index.storeTranscript(uri.toString(),"c5-saf.txt","text/plain",provider.file.readText())
        context.openOrCreateDatabase("search-index.db",0,null).use{it.execSQL("UPDATE files SET tree=? WHERE uri=?",arrayOf(tree.toString(),uri.toString()))}
        assertTrue(index.cacheExtraction(uri.toString(),DocumentText.extractDetailed(context,uri,"text/plain"),config.value))
        parent=CoroutineScope(SupervisorJob()+Dispatchers.Unconfined)
        state=DocumentCitationState(index,config,parent)
    }
    @After fun cleanup(){if(::provider.isInitialized){provider.release?.countDown();provider.file.delete()};if(::state.isInitialized)state.close();if(::parent.isInitialized)parent.cancel()}
    private suspend fun show():CitationRef {
        state.query("presupuesto revisión").join()
        assertEquals(1,state.visible.value.size)
        return CitationRef(state.visible.value.single().sourceId)
    }
    private suspend fun assertCannotUse(ref:CitationRef) {
        var uses=0;assertFalse(state.withValidated(ref){uses++});assertEquals(0,uses);assertTrue(state.visible.value.isEmpty())
    }
    @Test fun providerValidGrantCanDisplayCopyAndOpenOnlyAfterFreshValidation()=runBlocking {
        val ref=show();val uses=mutableListOf<String>()
        for(action in listOf("display","copy","open"))assertTrue(state.withValidated(ref){assertEquals(provider.file.readText(),it.quote);uses+=action})
        assertEquals(listOf("display","copy","open"),uses)
    }
    @Test fun revocationWithoutConfigChangeFailsGateAndHostSignalClearsSynchronously()=runBlocking {
        val ref=show()
        context.contentResolver.releasePersistableUriPermission(tree,Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // No notification is promised by Android: the action gate still checks the real persisted grants.
        assertCannotUse(ref)
        context.contentResolver.takePersistableUriPermission(tree,Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val next=show()
        context.contentResolver.releasePersistableUriPermission(tree,Intent.FLAG_GRANT_READ_URI_PERMISSION)
        state.invalidate() // returning from permissions settings or a known grant-removal event
        assertTrue(state.visible.value.isEmpty());assertCannotUse(next)
    }
    @Test fun providerDeletionReplacementAndInaccessibilityCannotReuseVisibleText()=runBlocking {
        val ref=show();provider.file.delete();assertCannotUse(ref)
        provider.file.writeText("El convenio local tiene presupuesto de 2400 pesos y revisión del martes.")
        val next=show();provider.file.writeText(provider.file.readText().replace("2400","9999"));assertCannotUse(next)
        provider.file.writeText("El convenio local tiene presupuesto de 2400 pesos y revisión del martes.")
        val last=show();provider.inaccessible=true;assertCannotUse(last)
    }
    @Test fun configAndIndexRevisionClearVisibleStateWithoutAnotherUserAction()=runBlocking {
        show();config.value=config.value.copy(contentIndex=false);assertTrue(state.visible.value.isEmpty())
        config.value=config.value.copy(contentIndex=true);show()
        config.value=config.value.copy(privateMode=true);assertTrue(state.visible.value.isEmpty())
        config.value=config.value.copy(privateMode=false);show()
        config.value=config.value.copy(files=false);assertTrue(state.visible.value.isEmpty())
        config.value=config.value.copy(files=true);show();index.clear();assertTrue(state.visible.value.isEmpty())
    }
    @Test fun queryCancelCloseParentAndNewOwnerCannotRestorePlaintext()=runBlocking {
        val old=show();val task=state.query("sincoincidenciasxyz")
        assertTrue(state.visible.value.isEmpty());task.join();assertCannotUse(old)
        show();state.invalidate();assertTrue(state.visible.value.isEmpty())
        show();state.close();assertTrue(state.visible.value.isEmpty())
        state=DocumentCitationState(index,config,parent);assertTrue(state.visible.value.isEmpty())
        show();parent.cancel();assertTrue(state.visible.value.isEmpty())
    }
    @Test fun invalidationDoesNotWaitForBlockedProviderAndLateResultNeverRepublishes()=runBlocking {
        val ref=show();provider.entered=CountDownLatch(1);provider.release=CountDownLatch(1)
        val action=async(Dispatchers.Default){state.withValidated(ref){fail("Must not use revoked content")}}
        assertTrue(provider.entered!!.await(2,TimeUnit.SECONDS))
        context.contentResolver.releasePersistableUriPermission(tree,Intent.FLAG_GRANT_READ_URI_PERMISSION)
        state.invalidate() // clears while openDocument is still blocked
        assertEquals(1L,provider.release!!.count);assertTrue(state.visible.value.isEmpty())
        provider.release!!.countDown()
        try{action.await()}catch(_:CancellationException){}
        assertTrue(state.visible.value.isEmpty())
    }
    @Test fun cancellingActionRemovesPlaintextWhileProviderIsStillBlocked()=runBlocking {
        val ref=show();provider.entered=CountDownLatch(1);provider.release=CountDownLatch(1)
        val action=async(Dispatchers.Default){state.withValidated(ref){fail("Cancelled action")}}
        assertTrue(provider.entered!!.await(2,TimeUnit.SECONDS))
        try {
            action.cancel()
            withTimeout(1000){state.visible.first{it.isEmpty()}}
            assertEquals(1L,provider.release!!.count)
        } finally {provider.release!!.countDown();action.cancelAndJoin()}
        assertTrue(state.visible.value.isEmpty())
    }
    @Test fun revocationDuringProviderOpenCannotReturnEvidenceWithoutAnEvent()=runBlocking {
        provider.entered=CountDownLatch(1);provider.release=CountDownLatch(1)
        DocumentCitationSession(index){config.value}.use { session ->
            val pending=async(Dispatchers.Default){session.retrieve("presupuesto revisión")}
            assertTrue(provider.entered!!.await(2,TimeUnit.SECONDS))
            context.contentResolver.releasePersistableUriPermission(tree,Intent.FLAG_GRANT_READ_URI_PERMISSION)
            provider.release!!.countDown()
            assertTrue(pending.await().isEmpty())
        }
    }
    @Test fun providerEventClearsChangedOrRemovedDocumentBeforeAnyAction()=runBlocking {
        show();provider.file.writeText("Documento reemplazado")
        state.invalidate() // host's provider notification adapter, not a promised platform broadcast
        assertTrue(state.visible.value.isEmpty())
        provider.file.writeText("El convenio local tiene presupuesto de 2400 pesos y revisión del martes.")
        show();provider.file.delete();state.invalidate();assertTrue(state.visible.value.isEmpty())
    }
    @Test fun cancellationDuringBlockedQueryAndReplacementQueryNeverPublishOldResult()=runBlocking {
        show();provider.entered=CountDownLatch(1);provider.release=CountDownLatch(1)
        val job=state.query("presupuesto revisión")
        assertTrue(state.visible.value.isEmpty());assertTrue(provider.entered!!.await(2,TimeUnit.SECONDS))
        job.cancel();assertTrue(state.visible.value.isEmpty())
        val next=state.query("sincoincidenciasxyz");provider.release!!.countDown();job.join();next.join()
        assertTrue(state.visible.value.isEmpty())
    }
}
