package dev.bautista.spotlight.data
import android.app.Application
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig
import org.json.JSONObject
@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class PortableImportTransactionTest {
 @Test fun fullGlassAndKeyboardRoundTripPreservesLocalConsentAndGrants()=runBlocking {
  val p=Preferences(RuntimeEnvironment.getApplication())
  p.update(Config(privateMode=true,trees=setOf("content://keep"),contacts=true,aiRootConsent=true,captureContext=true,autoKeyboard="never"))
  val visual=Config(glassIntensity=.7f,surfaceTone="cool",borderWidth=.5f,shadowSize=8f,typeface="mono",selectionStyle="solid",movement="precise",autoKeyboard="never",opacity=.78f,actionOpacity=.9f,settingsOpacity=.96f)
  p.importPortable(ConfigTransfer.encode(visual))
  val c=p.config.first()
  assertEquals(visual.glassIntensity,c.glassIntensity);assertEquals(visual.surfaceTone,c.surfaceTone);assertEquals(visual.typeface,c.typeface);assertEquals(visual.borderWidth,c.borderWidth);assertEquals(visual.movement,c.movement)
  assertTrue(c.privateMode);assertTrue(c.aiRootConsent);assertTrue(c.captureContext);assertTrue(c.contacts);assertEquals(setOf("content://keep"),c.trees)
 }
 @Test fun invalidImportIsAtomicAndLegacyPartialBackupKeepsNewFields()=runBlocking {
  val p=Preferences(RuntimeEnvironment.getApplication());p.update(Config(surfaceTone="warm",typeface="mono",privateMode=true))
  val old=p.config.first()
  val bad=JSONObject(ConfigTransfer.encode(Config(theme="light")));bad.getJSONObject("settings").put("opacity",2)
  assertTrue(runCatching{p.importPortable(bad.toString())}.isFailure);assertEquals(old,p.config.first())
  val legacy="""{"format":"spotlight-next","version":1,"settings":{"theme":"dark"}}"""
  p.importPortable(legacy);assertEquals(old.copy(theme="dark"),p.config.first())
 }
 @Test fun backupDoesNotContainAuthorizationOrPrivateHistory() {
  val j=JSONObject(ConfigTransfer.encode(Config(aiRootConsent=true,captureContext=true,trees=setOf("content://private"),usageData="SECRET_HISTORY",endpoint="SECRET_ENDPOINT"))).getJSONObject("settings")
  for(key in listOf("trees","contacts","files","aiRootConsent","captureContext","endpoint","usageData","privateMode"))assertFalse(j.has(key))
 }
}
