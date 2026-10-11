package dev.bautista.spotlight.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config as RoboConfig

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=android.app.Application::class)
class PersonalStorageTest {
    @Test fun editingPreservesIdentityWithoutDuplicatingNotes()=runBlocking {
        val preferences=Preferences(RuntimeEnvironment.getApplication())
        val scope=CoroutineScope(SupervisorJob()+Dispatchers.Default)
        try {
            preferences.editNotes { "[]" } // Each test starts from a controlled store, including repeated Gradle runs.
            val store=LocalStore(preferences,scope)
            store.saveNote("Texto original")
            val before=JSONArray(preferences.noteData.first()).getJSONObject(0)
            store.saveNote("Texto corregido",before.getString("id"))
            val after=JSONArray(preferences.noteData.first())
            assertEquals(1,after.length())
            assertEquals(before.getString("id"),after.getJSONObject(0).getString("id"))
            assertEquals(before.getLong("created"),after.getJSONObject(0).getLong("created"))
            assertEquals("Texto corregido",after.getJSONObject(0).getString("text"))
            store.saveNote("No insertar","missing-id")
            assertEquals(1,JSONArray(preferences.noteData.first()).length())
        } finally {scope.cancel()}
    }
    @Test fun personalSettingsRoundTripPreservesOlderPreferences()=runBlocking {
        val preferences=Preferences(RuntimeEnvironment.getApplication())
        val expected=Config(theme="dark",trees=setOf("content://folder"),aliases=setOf("musica|youtube música"),snippets=setOf("firma|Bautista"),pins=setOf("kind:pdf"))
        preferences.update(expected);assertEquals(expected,preferences.config.first())
        preferences.togglePin("kind:pdf")
        val after=preferences.config.first()
        assertTrue(after.pins.isEmpty());assertEquals(expected.aliases,after.aliases);assertEquals(expected.trees,after.trees)
    }
}
