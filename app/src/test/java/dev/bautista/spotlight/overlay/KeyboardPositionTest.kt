package dev.bautista.spotlight.overlay

import dev.bautista.spotlight.data.Config
import org.junit.Assert.*
import org.junit.Test

class KeyboardPositionTest {
    @Test fun tabletPositionUsesTheUnoccludedAreaAndRestoresWithoutEditingPreferences() {
        val config=Config(position=.2f,landscapePosition=.2f)
        val closed=AdaptiveLayout.bounds(1280,728,1f,0,config)
        val keyboard=AdaptiveLayout.bounds(1280,728,1f,310,config)
        assertEquals((418*.2f).toInt(),keyboard.top)
        assertTrue(keyboard.top<closed.top)
        assertTrue(keyboard.top+keyboard.heightDp+24<=418)
        assertEquals(closed,AdaptiveLayout.bounds(1280,728,1f,0,config))
        assertEquals(.2f,config.position,0f)
    }
    @Test fun noKeyboardAndHardwareKeyboardKeepSavedPlacementAcrossDensities() {
        for(density in listOf(1f,1.5f,2f)) {
            val config=Config(position=.16f)
            val closed=AdaptiveLayout.bounds(1920,1080,density,0,config)
            assertEquals((1080*.16f).toInt(),closed.top)
            assertEquals(closed,AdaptiveLayout.bounds(1920,1080,density,0,config))
        }
    }
}
