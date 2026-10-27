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
        prefs.updateAppearance(original.copy(compactSearchResults=true,textScale=.85f,uiDensityScale=1.10f,density="spacious"))
        val saved=prefs.config.first()
        assertTrue(saved.compactSearchResults)
        assertEquals(.85f,saved.textScale,.001f)
        assertEquals(1.10f,saved.uiDensityScale,.001f)
        assertEquals("spacious",saved.density)
        val decoded=ConfigTransfer.decode(ConfigTransfer.encode(saved),original)
        assertTrue(decoded.compactSearchResults)
        assertEquals(.85f,decoded.textScale,.001f)
        assertEquals(1.10f,decoded.uiDensityScale,.001f)
        val legacy=JSONObject(ConfigTransfer.encode(saved));legacy.getJSONObject("settings").remove("compactSearchResults");legacy.getJSONObject("settings").remove("uiDensityScale")
        val legacyDecoded=ConfigTransfer.decode(legacy.toString(),original.copy(compactSearchResults=false,uiDensityScale=-1f))
        assertFalse(legacyDecoded.compactSearchResults)
        assertEquals(-1f,legacyDecoded.uiDensityScale,.001f)
        val merged=dev.bautista.spotlight.ui.AppearanceEdits.merge(original,saved)
        assertTrue(merged.compactSearchResults)
        assertEquals(.85f,merged.textScale,.001f)
        assertEquals(1.10f,merged.uiDensityScale,.001f)
        prefs.updateAppearance(saved.copy(compactSearchResults=false,textScale=1f,uiDensityScale=1f,density="balanced"))
        assertFalse(prefs.config.first().compactSearchResults)
        assertEquals(1f,prefs.config.first().textScale,.001f)
        assertEquals(1f,prefs.config.first().uiDensityScale,.001f)
    }

    @Test fun newConfigRoundTripsWithoutSecrets():Unit=runBlocking {
        val prefs=Preferences(RuntimeEnvironment.getApplication());val original=prefs.config.first()
        val next=original.copy(aiProvider="gemini",aiModel="model-test",corner=27,textScale=1.15f,uiDensityScale=.90f,iconSize=33,maxHeight=550,adaptive=false,routines="[]",onboarded=true)
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
