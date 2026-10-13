package dev.bautista.spotlight.data

import android.app.Application
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class OverlayWidthPersistenceTest {
    @Test fun manualMemoryAndHorizontalOverrideSurviveSavingAutomaticAndReopening():Unit=runBlocking {
        val context=RuntimeEnvironment.getApplication()
        val prefs=Preferences(context)
        val saved=prefs.config.first().copy(width=920,landscapeWidth=460,landscapePosition=.25f,pins=setOf("pdf de ayer"))
        prefs.update(saved)
        val before=prefs.config.first()
        assertEquals(920,before.manualWidth)
        prefs.update(OverlayWidthPolicy.automatic(before,true))
        val reopened=Preferences(context).config.first()
        assertEquals(0,reopened.width)
        assertEquals(920,reopened.manualWidth)
        assertEquals(460,reopened.landscapeWidth)
        assertEquals(.25f,reopened.landscapePosition)
        assertEquals(saved.pins,reopened.pins)
        prefs.update(OverlayWidthPolicy.automatic(reopened,false))
        assertEquals(920,prefs.config.first().width)
        prefs.update(OverlayWidthPolicy.horizontal(prefs.config.first(),false))
        assertEquals(0,prefs.config.first().landscapeWidth)
        assertEquals(.25f,prefs.config.first().landscapePosition)
    }
}
