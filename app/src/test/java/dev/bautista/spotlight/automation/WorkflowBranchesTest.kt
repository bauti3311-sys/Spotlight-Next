package dev.bautista.spotlight.automation
import android.app.Application
import dev.bautista.spotlight.ai.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class) @Config(sdk=[33],application=Application::class)
class WorkflowBranchesTest {
 private class Host:WorkflowHost{val effects=mutableListOf<String>();override suspend fun copy(text:String){effects+=text};override suspend fun note(text:String){effects+=text};override suspend fun volume(level:Int){};override suspend fun timer(minutes:Int){};override suspend fun tool(call:ToolCall):String{if(call.name=="device_info")return "Tablet";error("Disconnected")}}
 @Test fun variablesAndBranchesReuseResults():Unit=runBlocking{val host=Host();val w=Workflow("1","Branch",steps=listOf(WorkflowStep("setvar","nombre=Juan"),WorkflowStep("if_empty"),WorkflowStep("text","Hola {variable:nombre}"),WorkflowStep("else"),WorkflowStep("text","Otra rama"),WorkflowStep("end"),WorkflowStep("copy")));assertFalse(Workflows.run(w,host).last().failed);assertEquals(listOf("Hola Juan"),host.effects);host.effects.clear();Workflows.run(w,host,"entrada").collect{};assertEquals(listOf("Otra rama"),host.effects)}
 @Test fun nestedInactiveBranchesCannotProduceEffects():Unit=runBlocking{val h=Host();val w=Workflow("1","Nested",steps=listOf(WorkflowStep("if_contains","no coincide"),WorkflowStep("if_empty"),WorkflowStep("note","BAD"),WorkflowStep("else"),WorkflowStep("copy","BAD"),WorkflowStep("end"),WorkflowStep("end"),WorkflowStep("copy","OK")));Workflows.run(w,h,"input").collect{};assertEquals(listOf("OK"),h.effects)}
 @Test fun invalidBranchesAndUnknownToolsFailBeforeExecution(){for(steps in listOf(listOf(WorkflowStep("else")),listOf(WorkflowStep("if_empty")),listOf(WorkflowStep("tool",Workflows.encodeTool(ToolCall("shell",emptyMap())))),listOf(WorkflowStep("tool_action",Workflows.encodeTool(ToolCall("run_workflow",mapOf("id" to "x"))))))){assertNotNull(Workflows.validate(Workflow("1","Bad",steps=steps)))}}
 @Test fun recoverableReadFailureKeepsPreviousOutputAndContinues():Unit=runBlocking{val h=Host();val w=Workflow("1","Recover",steps=listOf(WorkflowStep("text","original"),WorkflowStep("tool",Workflows.encodeTool(ToolCall("shizuku_diagnostics",emptyMap())),"continue"),WorkflowStep("copy")));val events=Workflows.run(w,h).toList();assertTrue(events.any{it.warning.isNotBlank()});assertFalse(events.last().failed);assertEquals(listOf("original"),h.effects)}
 @Test fun sensitiveFailuresNeverContinueAndExpandedArgumentsAreValidated():Unit=runBlocking{assertNotNull(Workflows.validate(Workflow("1","Bad",steps=listOf(WorkflowStep("tool_action",Workflows.encodeTool(ToolCall("set_volume",mapOf("percent" to "20"))),"continue")))));val h=Host();val w=Workflow("1","Unsafe",steps=listOf(WorkflowStep("setvar","pkg=com.app;id"),WorkflowStep("tool_action",Workflows.encodeTool(ToolCall("open_app",mapOf("package" to "{variable:pkg}")))),WorkflowStep("copy","BAD")));assertTrue(Workflows.run(w,h).last().failed);assertTrue(h.effects.isEmpty())}
 @Test fun workflowSerializationPreservesPoliciesAndOldSteps(){val w=Workflow("x","Versioned",steps=listOf(WorkflowStep("tool",Workflows.encodeTool(ToolCall("device_info",emptyMap())),"continue"),WorkflowStep("copy")));assertEquals(w,Workflows.parse(Workflows.encode(listOf(w))).single());assertEquals("stop",Workflows.parse("""[{"id":"old","name":"Old","steps":[{"type":"copy"}]}]""").single().steps.single().onError)}
}
