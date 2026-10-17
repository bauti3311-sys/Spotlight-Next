package dev.bautista.spotlight.semantic
import android.app.Application
import dev.bautista.spotlight.data.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig
@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class SemanticPreferenceTest {
 @Test fun existingPreferencesDoNotOptInAndPartialUpdatesPreserveConsent()=runBlocking {
  val c=RuntimeEnvironment.getApplication();val p=Preferences(c)
  assertFalse(Config().semanticSearch)
  p.update(p.config.first().copy(semanticSearch=false,trees=setOf("content://keep"),privateMode=true))
  val stale=p.config.first();p.updateSemantic(true)
  p.updateAppearance(stale.copy(theme="light",windowScale=.6f))
  val actual=Preferences(c).config.first()
  assertTrue(actual.semanticSearch);assertTrue(actual.privateMode)
  assertEquals(setOf("content://keep"),actual.trees);assertEquals("light",actual.theme)
  p.updateSemantic(false);assertFalse(Preferences(c).config.first().semanticSearch)
 }
 @Test fun absentOrPartialModelDoesNotDownloadOrBecomeReady()=runBlocking {
  val store=SemanticModelStore(RuntimeEnvironment.getApplication()){error("No network allowed during readiness")}
  store.remove();assertFalse(store.ready());store.directory.mkdirs()
  java.io.File(store.directory,"model.onnx").writeText("incomplete")
  assertFalse(store.ready());assertFalse(store.state.value.installed)
  store.remove();assertFalse(store.directory.exists())
 }
}
