package dev.bautista.spotlight.search

import dev.bautista.spotlight.overlay.AdaptiveLayout
import dev.bautista.spotlight.data.Config
import org.junit.Test
import org.junit.Assert.*

class SpaceRequestsTest {
    @Test fun sportsQueriesPreserveEntityAndUnavailableMmaIsExplicit(){
        assertEquals("Boca",SportsRequest.parse("Boca próximo partido")!!.entity)
        assertTrue(SportsRequest.parse("Deportes en vivo")!!.live)
        assertEquals("MMA",SportsRequest.parse("Próxima UFC")!!.unsupported)
        assertNull(SportsRequest.parse("\"Boca próximo partido\""))
    }
    @Test fun weatherQuestionsUseForecastAndAutomaticWidthPreservesManualSettings(){
        assertTrue(WeatherRequests.matches("¿Necesito paraguas mañana?"))
        assertTrue(WeatherRequests.matches("¿A qué hora deja de llover?"))
        assertEquals(1024,AdaptiveLayout.bounds(1280,800,1f,0,Config(width=0)).width)
        assertEquals(1100,AdaptiveLayout.bounds(1280,800,1f,0,Config(width=0),expanded=true).width)
        assertEquals(640,AdaptiveLayout.bounds(1280,800,1f,0,Config(width=640),expanded=true).width)
        assertEquals(336,AdaptiveLayout.bounds(360,800,1f,300,Config(width=0)).width)
    }
}
