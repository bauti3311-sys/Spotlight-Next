package dev.bautista.spotlight.data

import android.app.Application
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class ContextPreservationTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private val file get()=File(context.noBackupFilesDir,"spotlight-context-v1.json")
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
    private fun store()=ContextStore(context,scope,Preferences(context))
    private fun seed(count:Int=1,owned:String="") {
        file.writeText(JSONObject().put("version",1).put("futureRoot","keep-root")
            .put("contexts",JSONArray().apply{repeat(count){put(JSONObject().put("id","old-$it").put("title","Anterior $it").put("kind","text").put("text","Texto conservado $it").put("created",it).put("ownedFile",owned).put("futurePin","keep-$it"))}})
            .put("conversations",JSONArray().put(JSONObject().put("id","chat").put("title","Conversación anterior").put("text","Conversación conservada").put("created",1).put("futureChat","keep-chat"))).toString())
    }
    @Before fun clean(){file.delete();File(file.path+".bak").delete();File(file.path+".new").deleteRecursively()}
    @After fun close(){scope.cancel();file.delete();File(file.path+".bak").delete();File(file.path+".new").deleteRecursively()}
    private suspend fun loaded(s:ContextStore,count:Int=1){s.start();withTimeout(5000){s.pinned.first{it.size==count}}}
    @Test fun pinBeforeStartPreservesOldContextsConversationsAndUnknownFields()=runBlocking {
        seed();val current=store();current.setText("Nuevo contexto explícito");current.pinActive()
        assertEquals(2,current.pinned.value.size);assertEquals(1,current.conversations.value.size)
        val saved=JSONObject(file.readText());assertEquals("keep-root",saved.getString("futureRoot"))
        assertEquals("keep-0",saved.getJSONArray("contexts").getJSONObject(1).getString("futurePin"))
        assertEquals("keep-chat",saved.getJSONArray("conversations").getJSONObject(0).getString("futureChat"))
    }
    @Test fun fullContextStorageRefusesToEvictExistingPins()=runBlocking {
        seed(30);val before=file.readText();val current=store();current.setText("No desplazar otro guardado")
        try{current.pinActive();fail("Must refuse to evict a pinned context")}catch(_:IllegalStateException){}
        assertEquals(30,current.pinned.value.size);assertEquals(before,file.readText());assertFalse(current.active.value!!.pinned)
    }
    @Test fun removeBeforeStartDeletesOnlyChosenContextAndKeepsConversation()=runBlocking {
        seed(2);val current=store();current.removeContext("old-0")
        assertEquals(listOf("old-1"),current.pinned.value.map{it.id});assertEquals("chat",current.conversations.value.single().id)
        val saved=JSONObject(file.readText());assertEquals("keep-1",saved.getJSONArray("contexts").getJSONObject(0).getString("futurePin"))
    }
    @Test fun failedPinWriteKeepsActiveUnpinnedAndExistingStorageIntact()=runBlocking {
        seed();val current=store();loaded(current);current.setText("Nuevo contexto sin guardar");val before=file.readText()
        assertTrue(File(file.path+".new").mkdir())
        try{current.pinActive();fail("Blocked AtomicFile should fail")}catch(_:java.io.IOException){}
        assertEquals(listOf("old-0"),current.pinned.value.map{it.id});assertFalse(current.active.value!!.pinned);assertEquals(before,file.readText())
    }
    @Test fun failedRemovalKeepsActiveAndOwnedFileWithoutDeletingData()=runBlocking {
        val owned=File(context.noBackupFilesDir,"context-imports/preserved-test.txt").apply{parentFile!!.mkdirs();writeText("keep source")}
        try {
            seed(owned=owned.path);val current=store();loaded(current);current.select("old-0");val before=file.readText()
            assertTrue(File(file.path+".new").mkdir())
            try{current.removeContext("old-0");fail("Blocked AtomicFile should fail")}catch(_:java.io.IOException){}
            assertEquals("old-0",current.active.value!!.id);assertEquals(1,current.pinned.value.size)
            assertTrue(owned.exists());assertEquals(before,file.readText())
        }finally{owned.delete()}
    }
    @Test fun delayedLoadCannotPinPrivateContextThatReplacedTheOriginalSelection()=runBlocking {
        seed();val before=file.readText()
        val tasks=java.util.concurrent.LinkedBlockingQueue<Runnable>()
        val dispatcher=object:CoroutineDispatcher(){override fun dispatch(context:kotlin.coroutines.CoroutineContext,block:Runnable){tasks.add(block)}}
        fun drain(){while(true){val task=tasks.poll() ?: return;task.run()}}
        val pausedScope=CoroutineScope(SupervisorJob()+dispatcher)
        try {
            val current=ContextStore(context,pausedScope,Preferences(context))
            current.setText("Primera selección autorizada")
            val pin=async(start=CoroutineStart.UNDISPATCHED){current.pinActive()}
            current.setText("Otra selección privada no autorizada para fijar")
            withTimeout(5000){while(!pin.isCompleted){drain();delay(10)}};pin.await()
            assertEquals(listOf("old-0"),current.pinned.value.map{it.id})
            assertEquals("Otra selección privada no autorizada para fijar",current.active.value!!.text)
            assertFalse(current.active.value!!.pinned);assertEquals(before,file.readText())
        }finally{pausedScope.cancel();drain()}
    }
    @Test fun selectingAnotherContextReleasesSupersededTemporaryImportButKeepsPinnedSource()=runBlocking {
        val directory=File(context.noBackupFilesDir,"context-imports").apply{mkdirs()}
        val temporary=File(directory,"replaced-temporary.txt").apply{writeText("temporary source")}
        val pinned=File(directory,"retained-pinned.txt").apply{writeText("pinned source")}
        try {
            seed(owned=pinned.path);val current=store();loaded(current)
            current.setDocument(android.net.Uri.parse("content://selection-fixture/doc/1"),temporary.path)
            current.select("old-0")
            withTimeout(5000){while(temporary.exists())delay(10)}
            assertEquals("old-0",current.active.value!!.id);assertTrue(pinned.exists())
            current.select("missing");assertTrue(pinned.exists())
        }finally{temporary.delete();pinned.delete()}
    }
    @Test fun removalNeverDeletesAFileOutsideManagedContextImports()=runBlocking {
        val outside=File(context.filesDir,"outside-context-fixture.txt").apply{writeText("not owned by context imports")}
        try{seed(owned=outside.path);val current=store();loaded(current);current.removeContext("old-0");assertTrue(outside.exists())}finally{outside.delete()}
    }
    @Test fun repinningOneOfThirtyExistingContextsPreservesCapacityAndUnknownMetadata()=runBlocking {
        seed(30);val current=store();loaded(current,30);current.select("old-4");current.pinActive()
        assertEquals(30,current.pinned.value.size);assertEquals("old-4",current.pinned.value.first().id)
        val saved=JSONObject(file.readText());assertEquals("keep-4",saved.getJSONArray("contexts").getJSONObject(0).getString("futurePin"))
    }
}
