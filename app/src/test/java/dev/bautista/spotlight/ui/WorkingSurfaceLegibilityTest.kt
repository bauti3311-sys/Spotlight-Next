package dev.bautista.spotlight.ui

import dev.bautista.spotlight.data.Config
import org.junit.Assert.*
import org.junit.Test

class WorkingSurfaceLegibilityTest {
    @Test fun solidGraphiteWorkingWindowIsOpaqueWithoutOverwritingPreference() {
        val config=Config(preset="raycast",opacity=.70f)
        assertTrue(GlassTokens.windowColors(config,true,legibleBackdrop=true).all{it.alpha==1f})
        assertEquals(.70f,config.opacity,0f)
        assertTrue(GlassTokens.windowColors(config,true).all{it.alpha<.71f})
    }
    @Test fun glassWorkingWindowKeepsMaterialButProtectsTextOverOtherWindows() {
        val config=Config(preset="glass",opacity=.70f)
        assertTrue(GlassTokens.windowColors(config,true,legibleBackdrop=true).all{it.alpha>=.959f})
        assertTrue(GlassTokens.windowColors(config,false,legibleBackdrop=true).all{it.alpha>=.959f})
        assertEquals(.70f,config.opacity,0f)
    }
}
