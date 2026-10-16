package dev.bautista.spotlight.automation

import android.app.Application
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Preferences
import dev.bautista.spotlight.search.*
import dev.bautista.spotlight.ui.ProductVisibility
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class NativeCommandsTest {
 private lateinit var original:dev.bautista.spotlight.data.Config
 @Before fun preservePreferences(){original=runBlocking{Preferences(RuntimeEnvironment.getApplication()).config.first()}}
 @After fun restorePreferences(){runBlocking{Preferences(RuntimeEnvironment.getApplication()).update(original)}}
 private fun native(vararg calls:ToolCall)=Workflow("native","Mi comando","miplan",calls.map{WorkflowStep("tool_action",Workflows.encodeTool(it))},"music",NativeCommands.KIND)
 private val volume=ToolCall("set_volume",mapOf("percent" to "40"))
 @Test fun onlyMarkedNativePlansBecomeVisibleCommands(){
  val old=Workflow("legacy","Antiguo",steps=listOf(WorkflowStep("volume","40")))
  val command=native(volume);val parsed=Workflows.parse(Workflows.encode(listOf(old,command)))
  assertEquals(listOf(old,command),parsed)
  assertEquals("",parsed.first().kind)
  assertFalse(ProductVisibility.allowed(Workflows.results("Antiguo",Workflows.encode(parsed),emptySet()).single()))
  val row=Workflows.results("miplan",Workflows.encode(parsed),emptySet()).single()
  assertEquals("command:native",row.id);assertTrue(ProductVisibility.allowed(row))
  assertEquals(Action.SystemPanel("saved-command","native"),row.action)
 }
 @Test fun markerCannotPromoteLegacyOrRemoteActions(){
  assertNotNull(Workflows.validate(native(volume).copy(steps=listOf(WorkflowStep("ai_text","private")))))
  assertNotNull(Workflows.validate(native(ToolCall("create_note",mapOf("text" to "private")))))
  assertNotNull(Workflows.validate(native(volume).copy(steps=listOf(WorkflowStep("tool_action",Workflows.encodeTool(volume),"continue")))))
  assertNotNull(Workflows.validate(native(*Array(7){volume})))
  assertNotNull(Workflows.validate(native(volume).copy(kind="untrusted-future")))
 }
 @Test fun reviewPreservesPackageIdentityEvenWhenLabelsAreIdentical(){
  val a=ToolCall("open_app",mapOf("package" to "fixture.first"));val b=ToolCall("open_app",mapOf("package" to "fixture.second"))
  val reviewed=NativeCommands.review(native(a,b)){"Duplicada"}
  assertEquals(listOf(a,b),reviewed.calls)
  assertEquals(listOf(PlanStep.Open("Duplicada"),PlanStep.Open("Duplicada")),reviewed.plan.steps)
 }
 @Test fun savedBrightnessRoundTripsExactNativeValues(){
  for(percent in 8..100){val call=ToolCall("write_display_setting",mapOf("key" to "screen_brightness","value" to (percent*255/100).toString()));assertEquals(PlanStep.Brightness(percent),NativeCommands.review(native(call)){it}.plan.steps.single())}
  assertFalse(NativeCommands.accepts(ToolCall("write_display_setting",mapOf("key" to "screen_brightness","value" to "92"))))
  assertFalse(NativeCommands.accepts(ToolCall("write_display_setting",mapOf("key" to "screen_off_timeout","value" to "30000"))))
 }
 @Test fun savingAppendsWithoutOverwritingLegacyUnknownDataOrConcurrentPreferences()=runBlocking {
  val p=Preferences(RuntimeEnvironment.getApplication());val base=p.config.first()
  val legacy=JSONObject().put("id","old").put("name","Old").put("steps",JSONArray()).put("unknown","preserved")
  p.update(base.copy(routines=JSONArray().put(legacy).toString(),privateMode=true,trees=setOf("content://kept"),windowScale=.8f))
  p.saveNativeCommand(native(volume))
  val after=Preferences(RuntimeEnvironment.getApplication()).config.first();val data=JSONArray(after.routines)
  assertEquals("preserved",data.getJSONObject(0).getString("unknown"));assertEquals(NativeCommands.KIND,data.getJSONObject(1).getString("kind"))
  assertTrue(after.privateMode);assertEquals(setOf("content://kept"),after.trees);assertEquals(.8f,after.windowScale)
  assertEquals(1,NativeCommands.results(after.routines).size)
 }
 @Test fun fullStorageNeverEvictsPreviousData()=runBlocking {
  val p=Preferences(RuntimeEnvironment.getApplication());val raw=JSONArray().apply{repeat(30){put(JSONObject().put("id","old:$it").put("unknown",it))}}.toString()
  p.update(p.config.first().copy(routines=raw))
  assertTrue(runCatching{p.saveNativeCommand(native(volume))}.isFailure)
  assertEquals(raw,p.config.first().routines)
 }
 @Test fun previousMediaStepRemainsEditable(){assertEquals(PlanStep.Media("previous"),LocalPlan.step("tema anterior"))}
 @Test fun deletingNativeCommandPreservesLegacyRecordWithSameIdAndUnknownFields()=runBlocking {
  val p=Preferences(RuntimeEnvironment.getApplication())
  val data=JSONArray(Workflows.encode(listOf(native(volume))))
  data.put(JSONObject().put("id","native").put("name","Old").put("unknown","retained"))
  p.update(p.config.first().copy(routines=data.toString(),privateMode=true))
  p.deleteNativeCommand("native")
  val actual=p.config.first();val remaining=JSONArray(actual.routines)
  assertTrue(actual.privateMode);assertEquals(1,remaining.length());assertEquals("retained",remaining.getJSONObject(0).getString("unknown"))
 }
}
