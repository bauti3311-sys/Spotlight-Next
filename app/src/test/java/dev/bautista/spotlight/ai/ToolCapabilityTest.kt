package dev.bautista.spotlight.ai

import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class ToolCapabilityTest {
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 @Test fun staleFolderGrantDoesNotAdvertiseAvailableFileSearch()=runBlocking {
  app.index.clear()
  val executor=ToolExecutor(app)
  val capabilities=executor.capabilities(AppConfig(files=true,trees=setOf("content://revoked/tree/source")))
  assertEquals(CapabilityStatus.PERMISSION_REQUIRED,capabilities.single{it.definition.id=="search_files"}.status)
  assertFalse(capabilities.any{it.definition.extension in setOf("tasker","workflows","snippets","focus")})
 }
 @Test fun permissionsAreRecheckedBeforeExecution()=runBlocking {
  val executor=ToolExecutor(app);val config=AppConfig(contacts=true)
  val before=executor.capabilities(config).single{it.definition.id=="search_contacts"}
  assertEquals(CapabilityStatus.PERMISSION_REQUIRED,before.status)
  assertNotNull(runCatching{executor.execute(ToolCall("search_contacts",mapOf("query" to "fixture")),config)}.exceptionOrNull())
 }
 @Test fun volumeReportsTheObservedPlatformLevel()=runBlocking {
  val audio=app.getSystemService(android.media.AudioManager::class.java)
  val r=ToolExecutor(app).execute(ToolCall("set_volume",mapOf("percent" to "40")),AppConfig(),true)
  assertEquals(ExecutionState.VERIFIED,r.state)
  assertTrue(r.text.contains("${audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)}/${audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)}"))
 }
}
