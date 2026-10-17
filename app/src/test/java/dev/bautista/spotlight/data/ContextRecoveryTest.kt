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
class ContextRecoveryTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private val file get()=File(context.noBackupFilesDir,"spotlight-context-v1.json")
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
    private fun store()=ContextStore(context,scope,Preferences(context))
    @Before fun clean(){file.delete();File(file.path+".bak").delete();File(file.path+".new").deleteRecursively()}
    @After fun close(){scope.cancel();clean()}
    private suspend fun refusesWithoutReplacing(body:String,operation:suspend (ContextStore)->Unit) {
        file.writeText(body);val current=store();current.setText("Contexto nuevo explícito")
        try{operation(current);fail("Unreadable or unsupported storage must not be overwritten")}catch(_:IllegalStateException){}
        assertEquals(body,file.readText())
    }
    @Test fun malformedStorageCannotBeReplacedByPinning()=runBlocking {
        refusesWithoutReplacing("{\"version\":1,\"contexts\":[",{it.pinActive()})
    }
    @Test fun newerStorageVersionCannotBeDowngradedBySavingAConversation()=runBlocking {
        refusesWithoutReplacing(JSONObject().put("version",2).put("futureData","preserve").toString(),{it.saveConversation("Pregunta","Respuesta")})
    }
    @Test fun invalidPersistedRecordCannotBeSilentlyRemovedByAnotherWrite()=runBlocking {
        refusesWithoutReplacing(JSONObject().put("version",1).put("contexts",JSONArray().put(JSONObject().put("id","recoverable-record").put("futureValue","keep"))).toString(),{it.pinActive()})
    }
    @Test fun malformedStorageCannotBeReplacedByDeletingAnUnknownConversation()=runBlocking {
        refusesWithoutReplacing("{\"version\":1,\"conversations\":[",{it.deleteConversation("missing")})
    }
    private fun validStorage():String=JSONObject().put("version",1).put("unknownRoot","retained")
        .put("contexts",JSONArray().put(JSONObject().put("id","old").put("title","Guardado").put("kind","text").put("text","Texto anterior").put("created",1).put("futurePin","preserve")))
        .put("conversations",JSONArray().put(JSONObject().put("id","chat").put("title","Conversación").put("text","Historial anterior").put("created",1))).toString()
    @Test fun explicitRetryLoadsRecoveredDataBeforeAllowingANewSave()=runBlocking {
        file.writeText("{not-readable");val current=store();current.setText("Contexto explícito actual")
        try{current.pinActive();fail("Must refuse unreadable storage")}catch(_:IllegalStateException){}
        assertNotNull(current.storageIssue.value)
        file.writeText(validStorage());current.retryStorage()
        assertNull(current.storageIssue.value);assertEquals("old",current.pinned.value.single().id)
        assertEquals("chat",current.conversations.value.single().id)
        assertFalse(current.active.value!!.pinned)
        current.pinActive()
        val saved=JSONObject(file.readText());assertEquals("retained",saved.getString("unknownRoot"))
        assertEquals("preserve",saved.getJSONArray("contexts").getJSONObject(1).getString("futurePin"))
        val restarted=store();restarted.start()
        withTimeout(5000){restarted.conversations.first{it.isNotEmpty()}}
        assertEquals(2,restarted.pinned.value.size);assertEquals("chat",restarted.conversations.value.single().id)
    }
    @Test fun repeatedFailedRetryPreservesBytesAndTemporarySelection()=runBlocking {
        val original="{still-not-readable";file.writeText(original);val current=store();current.setText("Temporal privado")
        current.retryStorage();current.retryStorage()
        assertNotNull(current.storageIssue.value);assertEquals(original,file.readText())
        assertTrue(current.pinned.value.isEmpty());assertEquals("Temporal privado",current.active.value!!.text)
        assertFalse(current.active.value!!.pinned)
    }
    @Test fun cancelledInitialLoadDoesNotGrantPermissionToOverwriteStorage()=runBlocking {
        val before=validStorage();file.writeText(before)
        val tasks=java.util.concurrent.LinkedBlockingQueue<Runnable>()
        val dispatcher=object:CoroutineDispatcher(){override fun dispatch(context:kotlin.coroutines.CoroutineContext,block:Runnable){tasks.add(block)}}
        val pausedScope=CoroutineScope(SupervisorJob()+dispatcher)
        val current=ContextStore(context,pausedScope,Preferences(context));current.setText("No reemplazar sin lectura")
        current.start();pausedScope.cancel()
        while(true){val task=tasks.poll() ?: break;task.run()}
        try{current.pinActive();fail("Cancelled load must not allow persistence")}catch(_:IllegalStateException){}
        assertEquals(before,file.readText());assertFalse(current.active.value!!.pinned)
    }
    @Test fun unsupportedListsAndDuplicateIdsArePreservedWithoutPartialPublication()=runBlocking {
        val good=JSONObject(validStorage());val row=good.getJSONArray("contexts").getJSONObject(0)
        for(body in listOf(
            JSONObject(validStorage()).put("contexts",JSONObject()).toString(),
            JSONObject(validStorage()).put("contexts",JSONArray().put(row).put(row)).toString(),
            JSONObject(validStorage()).put("contexts",JSONArray().apply{repeat(31){put(JSONObject(row.toString()).put("id","old-$it"))}}).toString()
        ))refusesWithoutReplacing(body,{it.pinActive()})
    }
    @Test fun atomicBackupIsRecoveredWithoutTreatingStorageAsNew()=runBlocking {
        val before=validStorage();File(file.path+".bak").writeText(before)
        val current=store();current.retryStorage()
        assertNull(current.storageIssue.value);assertEquals("old",current.pinned.value.single().id)
        assertEquals("chat",current.conversations.value.single().id)
        assertEquals(before,file.readText())
    }
}
