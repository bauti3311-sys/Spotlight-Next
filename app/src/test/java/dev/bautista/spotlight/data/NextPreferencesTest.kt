package dev.bautista.spotlight.data

import android.app.Application
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class NextPreferencesTest {
    @Test fun compactSearchSurvivesAppearanceSaveAndPortableRoundTrip():Unit=runBlocking {
        val prefs=Preferences(RuntimeEnvironment.getApplication());val original=prefs.config.first()
        prefs.updateAppearance(original.copy(compactSearchResults=true))
        val saved=prefs.config.first()
        assertTrue(saved.compactSearchResults)
        assertTrue(ConfigTransfer.decode(ConfigTransfer.encode(saved),original).compactSearchResults)
        val legacy=JSONObject(ConfigTransfer.encode(saved));legacy.getJSONObject("settings").remove("compactSearchResults")
        assertFalse(ConfigTransfer.decode(legacy.toString(),original.copy(compactSearchResults=false)).compactSearchResults)
        val merged=dev.bautista.spotlight.ui.AppearanceEdits.merge(original,saved)
        assertTrue(merged.compactSearchResults)
        prefs.updateAppearance(saved.copy(compactSearchResults=false));assertFalse(prefs.config.first().compactSearchResults)
    }

    @Test fun newConfigRoundTripsWithoutSecrets():Unit=runBlocking {
        val prefs=Preferences(RuntimeEnvironment.getApplication());val original=prefs.config.first()
        val next=original.copy(aiProvider="gemini",aiModel="model-test",corner=27,textScale=1.15f,iconSize=33,maxHeight=550,adaptive=false,routines="[]",onboarded=true)
        prefs.update(next);assertEquals(next,prefs.config.first())
        assertFalse(prefs.config.first().toString().contains("apiKey"))
    }
    @Test fun learnedUsageIsNotOverwrittenByAppearanceSnapshotAndCanBeCleared():Unit=runBlocking {
        val prefs=Preferences(RuntimeEnvironment.getApplication());prefs.clearHistory();val snapshot=prefs.config.first()
        prefs.update(snapshot.copy(learning=true));prefs.recordSelection("app:test");prefs.recordSelection("app:test")
        prefs.update(snapshot.copy(corner=25));assertEquals(2,JSONObject(prefs.config.first().usageData).getJSONObject("app:test").getInt("count"))
        prefs.clearHistory();prefs.update(prefs.config.first().copy(learning=false));prefs.recordSelection("app:test");assertEquals("{}",prefs.config.first().usageData)
    }
}
