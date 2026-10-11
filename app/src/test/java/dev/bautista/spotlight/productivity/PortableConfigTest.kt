package dev.bautista.spotlight.productivity
import android.app.Application
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.data.ConfigTransfer
import dev.bautista.spotlight.automation.*
import dev.bautista.spotlight.ai.ToolCall
import org.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class) @Config(sdk=[33],application=Application::class)
class PortableConfigTest {
 @Test fun portableSettingsRoundTripAndDeviceAccessStaysLocal(){val original=AppConfig(theme="dark",shortcut="alt",clipboardShortcut="off",snippets=setOf("firma|Bautista"),quickLinks=setOf("docs|https://example.com/?q={q}"),routines=Workflows.encode(listOf(Workflow("x","Diagnóstico",steps=listOf(WorkflowStep("tool",Workflows.encodeTool(ToolCall("device_info",emptyMap()))))))),trees=setOf("content://private"),endpoint="https://private-provider",usageData="private-history");val raw=ConfigTransfer.encode(original);assertFalse(raw.contains("private"));val local=AppConfig(trees=setOf("content://other"),endpoint="https://local",contacts=true);val restored=ConfigTransfer.decode(raw,local);assertEquals(original.snippets,restored.snippets);assertEquals(original.routines,restored.routines);assertEquals("dark",restored.theme);assertEquals(local.trees,restored.trees);assertEquals(local.endpoint,restored.endpoint);assertTrue(restored.contacts)}
 @Test fun invalidImportsAreAtomicAndNeverAcceptCredentialsOrPermissionFlags(){val original=AppConfig();for(value in listOf(JSONObject().put("apiKey","private"),JSONObject().put("opacity",0.1),JSONObject().put("theme","fake"),JSONObject().put("contacts",true),JSONObject().put("routines",JSONArray("""[{"id":"x","name":"Bad","steps":[{"type":"shell"}]}]""")))){val root=JSONObject().put("format","spotlight-next").put("version",1).put("settings",value);assertNotNull(runCatching{ConfigTransfer.decode(root.toString(),original)}.exceptionOrNull())};assertEquals(AppConfig(),original)}
 @Test fun focusPauseResumeAndCompletionCountOnlyOnce(){val context=RuntimeEnvironment.getApplication();context.getSharedPreferences("focus_session",0).edit().clear().commit();val timer=FocusSession(context);timer.statistics(true);timer.start(1,now=1000);timer.pause(31000);assertEquals(30000,timer.snapshot(31000).remaining);timer.resume(100000);assertEquals(10000,timer.snapshot(120000).remaining);assertEquals("finished",timer.snapshot(130000).status);assertEquals(1,timer.snapshot(131000).completed);assertEquals(1,timer.snapshot(132000).completed)}
 @Test fun rebootOrMonotonicResetDiscardsSessionAndStatsAreOptIn(){val context=RuntimeEnvironment.getApplication();context.getSharedPreferences("focus_session",0).edit().clear().commit();val timer=FocusSession(context);timer.start(1,now=10000);assertEquals("idle",timer.snapshot(500).status);timer.start(1,now=1000);assertEquals(0,timer.snapshot(61000).completed)}
}
