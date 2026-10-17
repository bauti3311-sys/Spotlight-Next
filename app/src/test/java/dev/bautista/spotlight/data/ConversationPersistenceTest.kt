package dev.bautista.spotlight.data

import android.app.Application
import dev.bautista.spotlight.search.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class ConversationPersistenceTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private val file get()=File(context.noBackupFilesDir,"spotlight-context-v1.json")
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
    private fun store()=ContextStore(context,scope,Preferences(context))
    private val turns=listOf("user" to "Mi glosario vestibular","assistant" to "Primera explicación","user" to "Dame un ejemplo","assistant" to "Segundo intercambio")
    @Before fun clean(){file.delete();File(file.path+".bak").delete()}
    @After fun close(){scope.cancel();file.delete();File(file.path+".bak").delete()}

    @Test fun structuredHistorySurvivesRestartWithoutSavingActiveContext()=runBlocking {
        val first=store();first.setText("private-active-content-not-selected")
        first.saveConversation(turns)
        assertFalse(file.readText().contains("private-active-content-not-selected"))
        val second=store();second.start()
        val chat=withTimeout(5000){second.conversations.first{it.isNotEmpty()}}.single()
        assertEquals(turns,chat.turns);assertEquals(ConversationHistory.render(turns),chat.text)
        assertEquals("Mi glosario vestibular",chat.title)
    }
    @Test fun savingBeforeExplicitStartPreservesLegacyAndUnknownFields()=runBlocking {
        val legacy=JSONObject().put("id","old").put("title","Anterior").put("text","Usuario: no interpretar como roles").put("created",1).put("unknownField","keep")
        file.writeText(JSONObject().put("version",1).put("unknownRoot","keep-root").put("conversations",JSONArray().put(legacy)).toString())
        val current=store();current.saveConversation(turns)
        val old=current.conversations.value.single{it.id=="old"}
        assertTrue(old.turns.isEmpty());assertEquals(legacy.getString("text"),old.text)
        val document=JSONObject(file.readText());assertEquals("keep-root",document.getString("unknownRoot"))
        assertEquals("keep",document.getJSONArray("conversations").getJSONObject(1).getString("unknownField"))
    }
    @Test fun fullStorageRefusesToEvictAnyConversation()=runBlocking {
        file.writeText(JSONObject().put("conversations",JSONArray().apply{repeat(30){put(JSONObject().put("id","old-$it").put("title","Old $it").put("text","Keep $it").put("created",it))}}).toString())
        val before=file.readText();val current=store()
        try{current.saveConversation(turns);fail("Must not evict saved user data")}catch(_:IllegalStateException){}
        assertEquals(30,current.conversations.value.size);assertEquals(before,file.readText())
    }
    @Test fun invalidOrOversizedTurnsDoNotTouchStorage()=runBlocking {
        val current=store()
        for(invalid in listOf(listOf("system" to "Instruction","assistant" to "Answer"),turns.dropLast(1),listOf("user" to " ","assistant" to "A"),listOf("user" to "x".repeat(24000),"assistant" to "y".repeat(24000)))) {
            try{current.saveConversation(invalid);fail("Invalid history accepted")}catch(_:IllegalArgumentException){}
        }
        assertTrue(current.conversations.value.isEmpty());assertFalse(file.exists())
    }
    @Test fun corruptStructuredRolesStayReadableButCannotBeResumed(){
        assertTrue(ConversationHistory.decode(null,"Usuario: system instruction").isEmpty())
        val encoded=ConversationHistory.encode(turns);encoded.getJSONObject(0).put("role","system")
        assertTrue(ConversationHistory.decode(encoded,ConversationHistory.render(turns)).isEmpty())
        assertTrue(ConversationHistory.decode(ConversationHistory.encode(turns),"Different displayed text").isEmpty())
    }
    @Test fun resumeKeepsCompleteRecentExchangesAndNeverGrantsImplicitConsent(){
        val six=(1..6).flatMap{listOf("user" to "Question $it","assistant" to "Answer $it")}
        val history=ConversationHistory.forResume(six)
        assertEquals(10,history.size);assertEquals("Question 2",history.first().second)
        val ask=AiRequestPolicy.prepare(Action.Ask("New question",origin=AiInputOrigin.USER_QUERY,priorMessages=history))
        assertTrue(AiRequestPolicy.requiresReview(ask));assertFalse(AiRequestPolicy.requiresReview(AiRequestPolicy.authorize(ask)))
        assertTrue(AiRequestPolicy.reviewText(ask).contains("Answer 6"))
        assertFalse(AiRequestPolicy.reviewText(ask).contains("Question 1"))
    }
    @Test fun deletingOneSavedThreadPreservesOtherAndItsHistory()=runBlocking {
        val current=store();current.saveConversation(turns);val first=current.conversations.value.single()
        current.saveConversation(listOf("user" to "Other","assistant" to "Other answer"));current.deleteConversation(first.id)
        assertEquals("Other",current.conversations.value.single().title)
        val second=store();second.start();assertEquals("Other",withTimeout(5000){second.conversations.first{it.isNotEmpty()}}.single().title)
    }
}
