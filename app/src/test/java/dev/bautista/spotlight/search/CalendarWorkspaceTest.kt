package dev.bautista.spotlight.search

import dev.bautista.spotlight.control.*
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class CalendarWorkspaceTest {
    @Test fun touchingIntervalsAreFreeAndOverlappingIntervalsConflict(){
        val b=listOf(TimeBlock(100,200),TimeBlock(250,350))
        assertTrue(Availability.conflicts(200,250,b).isEmpty())
        assertEquals(2,Availability.conflicts(150,300,b).size)
    }
    @Test fun alternativesSkipConflictsKeepDurationAndRespectWindow(){
        val b=listOf(TimeBlock(0,60000),TimeBlock(120000,180000))
        val slots=Availability.alternatives(0,60000,b,360000)
        assertEquals(3,slots.size);slots.forEach{assertEquals(60000,it.end-it.start);assertTrue(Availability.conflicts(it.start,it.end,b).isEmpty());assertTrue(it.end<=360000)}
    }
    @Test fun naturalDraftPreservesTitleResolvesWeekdayAndRejectsInvalidClock(){
        val draft=CalendarLanguage.draft("Agendá dentista el jueves a las 16",LocalDate.of(2026,10,12))!!
        assertEquals("dentista",draft.title);assertEquals(LocalDate.of(2026,10,15),draft.day);assertEquals(16,draft.hour)
        assertNull(CalendarLanguage.draft("Agendá dentista mañana a las 30"))
        assertNull(CalendarLanguage.draft("Agendá dentista mañana a las 16:90"))
    }
}
