package dev.bautista.spotlight.data
import android.app.Application
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig
@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class PortablePriorityTest {
 @Test fun rankingPreferencesRoundTripWhileLegacyAndInvalidBackupsPreserveData()=runBlocking {
  val p=Preferences(RuntimeEnvironment.getApplication());p.update(Config(sourcePriority="""{"Archivos":70,"IA":-40}"""))
  val before=p.config.first();val raw=ConfigTransfer.encode(before)
  p.importPortable(raw);assertEquals(before.sourcePriority,p.config.first().sourcePriority)
  p.importPortable("""{"format":"spotlight-next","version":1,"settings":{}}""");assertEquals(before,p.config.first())
  val bad=JSONObject(raw);bad.getJSONObject("settings").getJSONObject("sourcePriority").put("Archivos",151)
  assertTrue(runCatching{p.importPortable(bad.toString())}.isFailure);assertEquals(before,p.config.first())
 }
}
