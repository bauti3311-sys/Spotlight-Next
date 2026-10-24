package dev.bautista.spotlight.next

import org.junit.Assert.*
import org.junit.Test

class SourceFreshnessTest {
    @Test fun cachedAndStaleResultsDoNotClaimFreshOrLiveUpdates() {
        assertEquals("Consulta recibida",SourceFreshness("Fuente",100,false,false).status)
        assertEquals("Copia guardada",SourceFreshness("Fuente",100,true,false).status)
        assertEquals("Sin actualizar · copia guardada",SourceFreshness("Fuente",100,true,true).status)
    }
    @Test fun futureAndMissingTimesAreUnknownRatherThanFabricated() {
        assertNull(SourceFreshness("Fuente",101,false,false).knownTimestamp(100))
        assertNull(SourceFreshness("Fuente",0,false,false).knownTimestamp(100))
        assertEquals(90L,SourceFreshness("Fuente",90,true,true).knownTimestamp(100))
    }
}
