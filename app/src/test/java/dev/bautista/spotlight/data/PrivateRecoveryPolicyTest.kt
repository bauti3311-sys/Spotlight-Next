package dev.bautista.spotlight.data
import dev.bautista.spotlight.SpotlightApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.io.File
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig
@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=SpotlightApp::class)
class PrivateRecoveryPolicyTest {
 @Test fun privateModeClearsSuspendedOverlayWithoutDeletingExplicitSavedConversations()=runBlocking {
  val app=RuntimeEnvironment.getApplication() as SpotlightApp
  app.preferences.update(Config(privateMode=false));app.preferences.config.first{!it.privateMode}
  app.contexts.saveConversation("Conservar conversación explícita","Guardada antes del modo privado")
  app.contexts.saveOverlaySnapshot(mapOf("query" to listOf("texto efímero")))
  val file=File(app.noBackupFilesDir,"spotlight-context-v1.json")
  assertTrue(JSONObject(file.readText()).has("overlayRecovery"))
  app.preferences.update(app.preferences.config.first().copy(privateMode=true))
  withTimeout(5000){while(JSONObject(file.readText()).has("overlayRecovery"))delay(10)}
  assertFalse(JSONObject(file.readText()).has("overlayRecovery"))
  assertTrue(app.contexts.conversations.value.any{it.title=="Conservar conversación explícita"})
  app.preferences.update(app.preferences.config.first().copy(privateMode=false))
  assertNull(ContextStore(app,app.scope,app.preferences).overlaySnapshot())
 }
}
