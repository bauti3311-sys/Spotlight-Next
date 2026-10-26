package dev.bautista.spotlight.data
import android.app.Application
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig
@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class AiDestinationConsentTest {
 @Test fun destinationEditsInvalidateInheritedConsentWhileExplicitNewReviewWorks()=runBlocking {
  val p=Preferences(RuntimeEnvironment.getApplication())
  p.update(Config(aiProvider="gemini",aiRootConsent=true))
  val old=p.config.first();p.updateAi(old,old.copy(aiModel="another-model"))
  assertTrue(p.config.first().aiRootConsent)
  val model=p.config.first();p.updateAi(model,model.copy(aiProvider="openai"))
  assertFalse(p.config.first().aiRootConsent)
  val changed=p.config.first();p.updateAi(changed,changed.copy(aiRootConsent=true))
  assertTrue(p.config.first().aiRootConsent)
  p.update(p.config.first().copy(endpoint="https://another.invalid"))
  assertFalse(p.config.first().aiRootConsent)
 }
 @Test fun portableProviderChangeCannotInheritLocalConsentAndSameProviderPreservesIt()=runBlocking {
  val p=Preferences(RuntimeEnvironment.getApplication());p.update(Config(aiProvider="gemini",aiRootConsent=true))
  val old=p.config.first();p.importPortable(ConfigTransfer.encode(old))
  assertTrue(p.config.first().aiRootConsent)
  p.importPortable(ConfigTransfer.encode(old.copy(aiProvider="openai")))
  assertFalse(p.config.first().aiRootConsent)
 }
}
