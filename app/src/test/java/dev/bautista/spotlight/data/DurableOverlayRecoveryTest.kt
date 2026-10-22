package dev.bautista.spotlight.data

import android.app.Application
import android.content.Intent
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig
import org.json.*
import java.io.File

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class DurableOverlayRecoveryTest {
 private val app get()=RuntimeEnvironment.getApplication()
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
 @After fun stop(){scope.cancel()}
 @Test fun newStoreRestoresTypedDraftsAndDoesNotLoseSavedConversations()=runBlocking {
  val prefs=Preferences(app);val first=ContextStore(app,scope,prefs)
  first.saveConversation("Consulta explícita","Respuesta local")
  val state=mapOf("query" to listOf<Any?>("PDF anatomía"),"tasks" to listOf(mapOf("task-A" to mapOf("draft" to listOf("Editado",4,8L,.4f,true,null)))))
  first.saveOverlaySnapshot(state)
  val recreated=ContextStore(app,scope,prefs)
  assertEquals(state,recreated.overlaySnapshot());assertEquals(1,recreated.conversations.value.size)
  recreated.saveOverlaySnapshot(null)
  val again=ContextStore(app,scope,prefs);assertNull(again.overlaySnapshot());assertEquals(1,again.conversations.value.size)
 }
 @Test fun ordinaryBackupDoesNotExportTaskStateAndPrivateModeClearsIt()=runBlocking {
  val prefs=Preferences(app);val store=ContextStore(app,scope,prefs);store.saveOverlaySnapshot(mapOf("draft" to listOf("PRIVATE_TASK")))
  assertFalse(ConfigTransfer.encode(Config()).contains("PRIVATE_TASK"))
  prefs.update(Config(privateMode=true));store.saveOverlaySnapshot(mapOf("draft" to listOf("MUST_NOT_PERSIST")))
  assertNull(ContextStore(app,scope,prefs).overlaySnapshot())
  val raw=File(app.noBackupFilesDir,"spotlight-context-v1.json").readText();assertFalse(raw.contains("PRIVATE_TASK"));assertFalse(raw.contains("MUST_NOT_PERSIST"))
 }
 @Test fun invalidAndOversizedValuesNeverReplaceTheLastGoodSnapshot()=runBlocking {
  val store=ContextStore(app,scope,Preferences(app));val good=mapOf("query" to listOf("conservar"));store.saveOverlaySnapshot(good)
  assertFalse(OverlaySnapshotCodec.canSave(Intent(Intent.ACTION_SEND)))
  assertTrue(runCatching{store.saveOverlaySnapshot(mapOf("bad" to listOf(Intent())))}.isFailure)
  assertTrue(runCatching{store.saveOverlaySnapshot(mapOf("large" to listOf("x".repeat(100000))))}.isFailure)
  assertEquals(good,ContextStore(app,scope,Preferences(app)).overlaySnapshot())
 }
 @Test fun unreadableStateCannotBeOverwrittenByAnEmptySession()=runBlocking {
  val file=File(app.noBackupFilesDir,"spotlight-context-v1.json");file.writeText("{broken")
  val store=ContextStore(app,scope,Preferences(app));assertTrue(runCatching{store.overlaySnapshot()}.isFailure)
  assertTrue(runCatching{store.saveOverlaySnapshot(null)}.isFailure);assertEquals("{broken",file.readText())
 }
}
