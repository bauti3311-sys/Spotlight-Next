package dev.bautista.spotlight.data

import dev.bautista.spotlight.overlay.AdaptiveLayout
import org.junit.Assert.*
import org.junit.Test

class OverlayWidthPolicyTest {
    @Test fun automaticGeneralDoesNotHideOrEraseExistingHorizontalOverride() {
        val saved=Config(width=820,landscapeWidth=460,landscapePosition=.25f,pins=setOf("pdf de ayer"))
        val automatic=OverlayWidthPolicy.automatic(saved,true)
        assertEquals(0,automatic.width)
        assertEquals(820,automatic.manualWidth)
        assertEquals(460,AdaptiveLayout.bounds(1280,800,1f,0,automatic).width)
        assertEquals(640,AdaptiveLayout.bounds(800,1280,1f,0,automatic).width)
        assertEquals(saved,OverlayWidthPolicy.automatic(automatic,false))
    }
    @Test fun restoringHorizontalInheritanceChangesNeitherGeneralWidthNorPosition() {
        val saved=Config(width=820,landscapeWidth=460,landscapePosition=.25f,density="spacious")
        val inherited=OverlayWidthPolicy.horizontal(saved,false)
        assertEquals(saved.copy(landscapeWidth=0),inherited)
        assertEquals(820,AdaptiveLayout.bounds(1280,800,1f,0,inherited).width)
        val automatic=inherited.copy(width=0)
        assertEquals(1024,AdaptiveLayout.bounds(1280,800,1f,0,automatic).width)
    }
    @Test fun explicitHorizontalWidthStartsFromSavedManualAndGeneralRemainsAutomatic() {
        val automatic=Config(width=0,manualWidth=900)
        val horizontal=OverlayWidthPolicy.horizontal(automatic,true)
        assertEquals(0,horizontal.width)
        assertEquals(900,horizontal.landscapeWidth)
        assertEquals(900,OverlayWidthPolicy.automatic(horizontal,false).width)
    }
    @Test fun manualWidthSurvivesDensityChangesOrientationAndImeWithoutExceedingScreen() {
        val manual=OverlayWidthPolicy.manual(Config(width=0),820)
        for(density in listOf("compact","balanced","spacious")) {
            val c=manual.copy(density=density)
            val portrait=AdaptiveLayout.bounds(390,844,1f,300,c)
            val landscape=AdaptiveLayout.bounds(844,390,1f,200,c)
            assertEquals(820,c.width)
            assertEquals(366,portrait.width)
            assertTrue(landscape.width<=844-32)
            assertTrue(portrait.heightDp>=100f)
            assertEquals(AdaptiveLayout.bounds(390,844,1f,0,c).width,portrait.width)
        }
    }
}
