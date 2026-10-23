package dev.bautista.spotlight.ai

import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class StructuredToolAdapterTest {
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    @Test fun actualLocalCalculationAndTextAdaptersProduceExplicitTypedOutputs()=runBlocking {
        val executor=ToolExecutor(app)
        val numeric=executor.execute(ToolCall("calculate",mapOf("expression" to "40+10")),AppConfig())
        assertEquals(ExecutionState.VERIFIED,numeric.state)
        assertEquals(ToolValue(ToolValueType.NUMBER,"50"),numeric.outputs["value"])
        val text=executor.execute(ToolCall("transform_text",mapOf("operation" to "mayusculas","text" to "dato")),AppConfig())
        assertEquals(ExecutionState.VERIFIED,text.state)
        assertEquals(ToolValue(ToolValueType.TEXT,"DATO"),text.outputs["text"])
    }
    @Test fun actualVolumeAdapterRequiresConfirmationAndReturnsObservedValue()=runBlocking {
        val executor=ToolExecutor(app);val call=ToolCall("set_volume",mapOf("percent" to "40"))
        assertTrue(runCatching{executor.execute(call,AppConfig(),false)}.isFailure)
        val result=executor.execute(call,AppConfig(),true)
        assertEquals(ExecutionState.VERIFIED,result.state)
        val audio=app.getSystemService(android.media.AudioManager::class.java)
        val min=audio.getStreamMinVolume(android.media.AudioManager.STREAM_MUSIC);val max=audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        assertEquals(((audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)-min)*100/(max-min)).toString(),result.outputs["percent"]!!.value)
    }
    @Test fun actualNoteAdapterVerifiesNewPersistedRecordOnlyAfterConfirmation()=runBlocking {
        val executor=ToolExecutor(app);val call=ToolCall("create_note",mapOf("text" to "Nota de prueba confirmada"))
        val before=app.preferences.noteData.first()
        assertTrue(runCatching{executor.execute(call,AppConfig(),false)}.isFailure)
        assertEquals(before,app.preferences.noteData.first())
        val result=executor.execute(call,AppConfig(),true)
        assertEquals(ExecutionState.VERIFIED,result.state)
        val latest=org.json.JSONArray(app.preferences.noteData.first()).getJSONObject(0)
        assertEquals("Nota de prueba confirmada",latest.getString("text"))
    }
}
