package dev.bautista.spotlight.control

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.ai.*
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.Action
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class AndroidCapabilityTest {
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private val number="+5491112345678"
    private fun installDialer() {
        val info=ResolveInfo().apply{activityInfo=ActivityInfo().apply{packageName="fixture.phone";name="DialActivity";exported=true}}
        for(raw in listOf("123",number))shadowOf(app.packageManager).addResolveInfoForIntent(People.dialIntent(raw),info)
    }
    @Test fun dialArgumentsRejectCodesUrisAndInjectedParameters() {
        for(raw in listOf("*123#","tel:123","123;456","123,456","++123","12","1\n23","１２３")) {
            assertTrue(raw,runCatching{ToolRegistry.decode(ToolCall("prepare_dial",mapOf("number" to raw)))}.isFailure)
        }
        val intent=People.dialIntent("+54 (9) 11-1234-5678")
        assertEquals(Intent.ACTION_DIAL,intent.action);assertEquals("tel:$number",intent.data.toString())
    }
    @Test fun unavailableDialerIsNotAdvertisedAndCannotExecute()=runBlocking {
        val executor=ToolExecutor(app)
        assertEquals(CapabilityStatus.UNSUPPORTED,executor.capabilities(AppConfig()).single{it.definition.id=="prepare_dial"}.status)
        assertTrue(runCatching{executor.execute(ToolCall("prepare_dial",mapOf("number" to number)),AppConfig(),true)}.isFailure)
        assertNull(shadowOf(app).nextStartedActivity)
    }
    @Test fun confirmedToolOnlyPreparesDialerAndRequiresUserCall()=runBlocking {
        installDialer();val executor=ToolExecutor(app);val call=ToolCall("prepare_dial",mapOf("number" to number))
        assertTrue(runCatching{executor.execute(call,AppConfig(),false)}.isFailure)
        val result=executor.execute(call,AppConfig(),true)
        assertEquals(ExecutionState.PREPARED,result.state)
        assertEquals(Intent.ACTION_DIAL,(result.action as Action.Launch).intent.action)
        assertNull(shadowOf(app).nextStartedActivity)
    }
    @Test fun explicitNumberUsesLocalSearchWithoutContactsPermission()=runBlocking {
        installDialer();assertFalse(People.allowed(app))
        val row=NaturalIntents.calls(app,number).single()
        assertEquals("tel:$number",(row.action as Action.Launch).intent.data.toString())
        assertNull(shadowOf(app).nextStartedActivity)
    }
    @Test fun missingDialerOffersCopyRatherThanFalseLaunch()=runBlocking {
        assertTrue(NaturalIntents.calls(app,number).single().action is Action.Copy)
    }
    @Test fun appFunctionsPrerequisitesNeverAdvertiseImplementedExecution() {
        assertTrue(AppFunctionsSupport.describe(35,true,true).contains("requiere Android 16"))
        assertTrue(AppFunctionsSupport.describe(36,false,true).contains("no expone"))
        assertTrue(AppFunctionsSupport.describe(36,true,false).contains("autorización del sistema"))
        assertTrue(AppFunctionsSupport.describe(36,true,true).contains("todavía no descubre ni ejecuta"))
    }
    @Test fun diagnosticRunsLocallyWithoutExternalAction()=runBlocking {
        val result=ToolExecutor(app).execute(ToolCall("appfunctions_status",emptyMap()),AppConfig())
        assertTrue(result.text.contains("requiere Android 16"));assertNull(result.action)
    }
}
