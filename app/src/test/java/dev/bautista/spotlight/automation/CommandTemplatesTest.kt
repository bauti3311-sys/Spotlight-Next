package dev.bautista.spotlight.automation

import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Preferences
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class CommandTemplatesTest {
    private val volume=ToolCall("set_volume",mapOf("percent" to "40"))
    private fun template()=CommandTemplate("typed-command","Volumen de estudio","estudio",ToolPlan("Revisar",listOf(volume)),
        listOf(CommandParameter("nivel",CommandParameterType.NUMBER,"40")),listOf(CommandParameterTarget(0,"percent","nivel")))
    private fun rejects(block:()->Unit){assertTrue(runCatching(block).isFailure)}
    @Test fun typedValuesResolveWithoutMutatingOriginalPlan() {
        val t=template();assertEquals("65",CommandTemplates.instantiate(t,mapOf("nivel" to "65")).calls.single().arguments["percent"])
        assertEquals("40",t.plan.calls.single().arguments["percent"])
    }
    @Test fun invalidMissingExtraAndOutOfRangeValuesAreRejected() {
        val t=template()
        for(values in listOf(emptyMap(),mapOf("other" to "50"),mapOf("nivel" to "40;open_app"),mapOf("nivel" to "101"),mapOf("nivel" to "-1"),mapOf("nivel" to "40.5")))rejects{CommandTemplates.instantiate(t,values)}
    }
    @Test fun literalTextIsNotRecursivelyExpandedOrTreatedAsAnExecutableResult() {
        val t=CommandTemplate("text","Texto","",ToolPlan("",listOf(ToolCall("create_note",mapOf("text" to "original")))),
            listOf(CommandParameter("texto",CommandParameterType.TEXT,"original")),listOf(CommandParameterTarget(0,"text","texto")))
        val literal="{nivel} {variable:delete} open_app(package)"
        assertEquals(literal,CommandTemplates.instantiate(t,mapOf("texto" to literal)).calls.single().arguments["text"])
    }
    @Test fun choicesAndPackagesUseDestinationValidation() {
        val t=CommandTemplate("media","Música","",ToolPlan("",listOf(ToolCall("control_app_media",mapOf("package" to "com.spotify.music","command" to "pause")))),
            listOf(CommandParameter("app",CommandParameterType.PACKAGE,"com.spotify.music"),CommandParameter("accion",CommandParameterType.CHOICE,"pause")),
            listOf(CommandParameterTarget(0,"package","app"),CommandParameterTarget(0,"command","accion")))
        assertEquals("play",CommandTemplates.instantiate(t,mapOf("app" to "fixture.player","accion" to "play")).calls.single().arguments["command"])
        rejects{CommandTemplates.instantiate(t,mapOf("app" to "bad/package","accion" to "play"))}
        rejects{CommandTemplates.instantiate(t,mapOf("app" to "fixture.player","accion" to "send"))}
    }
    @Test fun targetsCannotBindToolNamesMissingStepsOrWrongTypes() {
        val t=template()
        rejects{CommandTemplates.validate(t.copy(targets=listOf(CommandParameterTarget(1,"percent","nivel"))))}
        rejects{CommandTemplates.validate(t.copy(targets=listOf(CommandParameterTarget(0,"name","nivel"))))}
        rejects{CommandTemplates.validate(t.copy(parameters=listOf(CommandParameter("nivel",CommandParameterType.TEXT,"40"))))}
        rejects{CommandTemplates.validate(t.copy(targets=t.targets+t.targets))}
        rejects{CommandTemplates.validate(t.copy(parameters=t.parameters+t.parameters))}
    }
    @Test fun dependencyAndParameterCannotOwnSameArgumentAndGraphStaysTyped() {
        val t=template().copy(plan=ToolPlan("",listOf(ToolCall("calculate",mapOf("expression" to "40+10")),volume)),
            targets=listOf(CommandParameterTarget(1,"percent","nivel")),bindings=mapOf(1 to mapOf("percent" to ToolResultRef(0,"value"))))
        rejects{CommandTemplates.validate(t)}
        val bound=t.copy(parameters=emptyList(),targets=emptyList())
        assertEquals(bound,CommandTemplates.decode(CommandTemplates.encode(bound)))
        rejects{CommandTemplates.validate(bound.copy(bindings=mapOf(1 to mapOf("percent" to ToolResultRef(1,"percent")))))}
    }
    @Test fun codecRoundTripPreservesParametersDependenciesAndRevision() {
        val t=template().copy(revision=3);assertEquals(t,CommandTemplates.decode(CommandTemplates.encode(t)))
        val unknown=CommandTemplates.encode(t).put("future","keep")
        rejects{CommandTemplates.decode(unknown)}
    }
    @Test fun upsertPreservesUnknownRecordsAndRejectsDuplicateNamesAliasesAndIds() {
        val unknown=JSONObject().put("id","future").put("name","Anterior").put("metadata",JSONObject().put("retained",true))
        val raw=JSONArray().put(unknown).toString();val saved=CommandTemplates.upsert(raw,template(),null,emptySet())
        assertEquals(unknown.toString(),JSONArray(saved).getJSONObject(0).toString())
        rejects{CommandTemplates.upsert(saved,template(),null,emptySet())}
        rejects{CommandTemplates.upsert(saved,template().copy(id="second",name="Otro",alias="estudio"),null,emptySet())}
        rejects{CommandTemplates.upsert(saved,template().copy(id="second",name="VOLUMEN DE ESTUDIO",alias="otro"),null,emptySet())}
        rejects{CommandTemplates.upsert(raw,template(),null,setOf("estudio|some query"))}
    }
    @Test fun staleRevisionAndUnknownMetadataCannotBeOverwritten() {
        val saved=CommandTemplates.upsert("[]",template(),null,emptySet())
        val updated=CommandTemplates.upsert(saved,template().copy(name="Nuevo"),0,emptySet())
        assertEquals(1,CommandTemplates.parse(updated).single().revision)
        rejects{CommandTemplates.upsert(updated,template(),0,emptySet())}
        rejects{CommandTemplates.delete(updated,template().id,0)}
        val unknown=JSONArray().put(CommandTemplates.encode(template()).put("future","keep")).toString()
        rejects{CommandTemplates.upsert(unknown,template(),0,emptySet())}
    }
    @Test fun mixedNativeAndTemplateCommandsRemainVisibleAndOnlyOpenReview() {
        val native=Workflow("native","Comando previo",steps=listOf(WorkflowStep("tool_action",Workflows.encodeTool(volume))),kind=NativeCommands.KIND)
        val raw=JSONArray(Workflows.encode(listOf(native))).put(CommandTemplates.encode(template())).toString()
        assertEquals(1,Workflows.parse(raw).size);assertEquals(2,NativeCommands.results(raw).size)
        val result=Workflows.results("estudio",raw,emptySet()).single()
        assertEquals(Action.SystemPanel("saved-command",template().id),result.action)
        assertEquals(1,CommandTemplates.results("",raw,setOf("command:${template().id}")).size)
    }
    @Test fun storageEditRestoreAndDeletePreserveExistingData()=runBlocking {
        val p=Preferences(RuntimeEnvironment.getApplication());val old=p.config.first()
        val unknown=JSONObject().put("id","future").put("metadata","preserved")
        try {
            p.update(old.copy(routines=JSONArray().put(unknown).toString()))
            p.saveCommandTemplate(template())
            val restored=CommandTemplates.parse(p.config.first().routines).single();assertEquals(template(),restored)
            p.saveCommandTemplate(restored.copy(name="Editado"),restored.revision)
            assertTrue(runCatching{p.saveCommandTemplate(restored,restored.revision)}.isFailure)
            p.deleteCommandTemplate(restored.id,1)
            assertEquals(unknown.toString(),JSONArray(p.config.first().routines).getJSONObject(0).toString())
            assertEquals(old.trees,p.config.first().trees)
        } finally { p.update(p.config.first().copy(routines=old.routines)) }
    }
    @Test fun restoredTemplateUsesD27ConfirmationBindingsAndReceiptsWithoutReplay()=runBlocking {
        val t=template().copy(plan=ToolPlan("",listOf(ToolCall("calculate",mapOf("expression" to "40+10")),volume)),
            parameters=emptyList(),targets=emptyList(),bindings=mapOf(1 to mapOf("percent" to ToolResultRef(0,"value"))))
        val restored=CommandTemplates.decode(CommandTemplates.encode(t));val plan=CommandTemplates.instantiate(restored)
        val executed=mutableListOf<ToolCall>();val receipts=mutableMapOf<Int,ToolReceipt>()
        val sequence=ToolSequenceExecution{call->executed+=call;ToolExecution("Fixture observado",state=ExecutionState.VERIFIED,
            outputs=if(call.name=="calculate")mapOf("value" to ToolValue(ToolValueType.NUMBER,"50")) else emptyMap())}
        suspend fun advance(confirmed:Int?=null)=sequence.advance(plan.calls,emptySet(),restored.bindings,receipts.toMap(),confirmed,
            resolve={i,current->StructuredToolResults.resolve(i,plan.calls,restored.bindings,emptySet(),current)},started={},
            completed={i,result->receipts[i]=ToolReceipt(result.state,result.outputs,result.action!=null)})
        assertEquals(SequenceDisposition.AWAIT_CONFIRMATION,advance().disposition)
        assertEquals(1,executed.size)
        assertEquals(SequenceDisposition.FINISHED,advance(1).disposition)
        assertEquals("50",executed[1].arguments["percent"])
        assertEquals(SequenceDisposition.FINISHED,advance().disposition)
        assertEquals(2,executed.size)
    }
    @Test fun dynamicFormReportsSpecificMissingInvalidAndUnknownParameters() {
        val t=template()
        assertTrue(CommandTemplates.parameterErrors(t,mapOf("nivel" to "50")).isEmpty())
        assertTrue("nivel" in CommandTemplates.parameterErrors(t,emptyMap()))
        assertTrue("nivel" in CommandTemplates.parameterErrors(t,mapOf("nivel" to "101")))
        assertTrue("extra" in CommandTemplates.parameterErrors(t,mapOf("nivel" to "50","extra" to "x")))
    }
    @Test fun dynamicChoicesComeFromActualToolSchema() {
        val t=CommandTemplate("media","Reproductor","",ToolPlan("",listOf(ToolCall("control_media",mapOf("command" to "pause")))),
            listOf(CommandParameter("accion",CommandParameterType.CHOICE,"pause")),listOf(CommandParameterTarget(0,"command","accion")))
        assertEquals(setOf("play","pause","next","previous"),CommandTemplates.choices(t,"accion").toSet())
        assertTrue("accion" in CommandTemplates.parameterErrors(t,mapOf("accion" to "send")))
    }
    @Test fun relatedFieldsAreValidatedAsOneResolvedRequest() {
        val t=CommandTemplate("display","Pantalla","",ToolPlan("",listOf(ToolCall("write_display_setting",mapOf("key" to "screen_brightness","value" to "100")))),
            listOf(CommandParameter("ajuste",CommandParameterType.CHOICE,"screen_brightness"),CommandParameter("valor",CommandParameterType.NUMBER,"100")),
            listOf(CommandParameterTarget(0,"key","ajuste"),CommandParameterTarget(0,"value","valor")))
        val values=mapOf("ajuste" to "screen_off_timeout","valor" to "30000")
        assertTrue(CommandTemplates.parameterErrors(t,values).isEmpty())
        assertEquals("30000",CommandTemplates.instantiate(t,values).calls.single().arguments["value"])
    }
    @Test fun fullStoreRejectsNewTemplateWithoutDeletingOldEntries() {
        val raw=JSONArray().apply{repeat(30){put(JSONObject().put("id","old-$it"))}}.toString()
        rejects{CommandTemplates.upsert(raw,template(),null,emptySet())}
        assertEquals(30,JSONArray(raw).length())
    }
}
