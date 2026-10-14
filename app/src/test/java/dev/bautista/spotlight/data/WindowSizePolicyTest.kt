package dev.bautista.spotlight.data

import android.app.Application
import dev.bautista.spotlight.overlay.AdaptiveLayout
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config as RoboConfig

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[28,35],application=Application::class)
class WindowSizePolicyTest {
    @Test fun legacyWidthsRemainExactUntilUserChangesSlider() {
        val c=Config(width=820,landscapeWidth=460)
        assertEquals(820,AdaptiveLayout.bounds(1000,1600,1f,0,c,landscape=false).width)
        assertEquals(460,AdaptiveLayout.bounds(1600,1000,1f,0,c,landscape=true).width)
    }
    @Test fun scaleSurvivesImeAndRespectsBothOrientations() {
        val c=Config(width=820,landscapeWidth=460,windowScale=.6f)
        for((width,height) in listOf(800 to 1280,1280 to 800,390 to 844,844 to 390)) {
            val open=AdaptiveLayout.bounds(width,height,1f,0,c)
            val ime=AdaptiveLayout.bounds(width,height,1f,250,c)
            assertEquals(open.width,ime.width)
            assertTrue(open.width<=width-24)
            assertTrue(ime.heightDp<open.heightDp)
            assertEquals(open,AdaptiveLayout.bounds(width,height,1f,0,c))
        }
        assertTrue(AdaptiveLayout.bounds(1280,800,1f,0,c).width>AdaptiveLayout.bounds(800,1280,1f,0,c).width)
    }
    @Test fun persistenceAndPortableBackupsPreserveNewChoiceAndOldData()=runBlocking {
        val p=Preferences(RuntimeEnvironment.getApplication())
        val c=Config(width=820,landscapeWidth=460,windowScale=.7f,snippets=setOf("legacy|keep"),pins=setOf("pdf"))
        p.update(c)
        val saved=p.config.first()
        assertEquals(.7f,saved.windowScale);assertEquals(820,saved.width);assertEquals(460,saved.landscapeWidth);assertEquals(c.snippets,saved.snippets)
        assertEquals(.7f,ConfigTransfer.decode(ConfigTransfer.encode(saved),Config()).windowScale)
        assertEquals(.7f,ThemeTransfer.decode(ThemeTransfer.encode(saved),Config()).windowScale)
    }
    @Test fun oldBackupDoesNotInventNewWidthChoice() {
        val old=org.json.JSONObject(ConfigTransfer.encode(Config(width=820)))
        old.getJSONObject("settings").remove("windowScale")
        val recovered=ConfigTransfer.decode(old.toString(),Config())
        assertEquals(-1f,recovered.windowScale);assertEquals(820,recovered.width)
    }
}
