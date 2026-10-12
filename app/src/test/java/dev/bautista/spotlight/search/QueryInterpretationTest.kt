package dev.bautista.spotlight.search

import java.time.*
import org.junit.Test
import org.junit.Assert.*

class QueryInterpretationTest {
    private val day=LocalDate.of(2026,10,12)
    private val zone=ZoneId.of("America/Argentina/Buenos_Aires")
    @Test fun yesterdayPdfUsesHalfOpenLocalDatesNotName(){
        val i=QueryInterpreter.interpret("Buscar PDF de ayer",today=day,zone=zone)
        assertEquals(QueryIntent.FILES,i.intent);assertEquals("",i.files!!.terms)
        val start=Instant.parse("2026-10-11T03:00:00Z").toEpochMilli()
        assertEquals(start,i.files!!.start)
        val q=SearchQuery.parse(i.text)
        val d=SearchQuery.Document("anatomía.pdf","document","application/pdf",modified=start)
        assertTrue(q.matches(d));assertFalse(q.matches(d.copy(modified=start-1)))
        assertFalse(q.matches(d.copy(modified=i.files!!.end!!)));assertFalse(q.matches(d.copy(name="ayer.pdf",modified=0)))
    }
    @Test fun lastWeekPhotoIsCalendarWeekAndUnknownMetadataExcluded(){
        val i=QueryInterpreter.interpret("Buscar fotos de la semana pasada",today=day,zone=zone)
        assertEquals(Instant.parse("2026-10-05T03:00:00Z").toEpochMilli(),i.files!!.start)
        assertEquals(Instant.parse("2026-10-12T03:00:00Z").toEpochMilli(),i.files!!.end)
        assertEquals("created",i.files!!.dateField)
        assertFalse(SearchQuery.parse(i.text).matches(SearchQuery.Document("semana pasada.jpg","image","image/jpeg",created=0)))
    }
    @Test fun timezoneDaySurvivesDstAndDoesNotAssume24Hours(){
        val f=QueryInterpreter.files("PDF de hoy",LocalDate.of(2026,3,8),ZoneId.of("America/New_York"))!!
        assertEquals(23*60*60*1000L,f.end!!-f.start!!)
    }
    @Test fun literalOperatorsAndEntityNumbersArePreserved(){
        assertEquals("\"PDF de ayer\"",QueryInterpreter.interpret("\"PDF de ayer\"").text)
        assertEquals("Factura uno",QueryInterpreter.interpret("Factura uno").text)
        assertEquals("kind:pdf factura NOT ayer",QueryInterpreter.interpret("kind:pdf factura NOT ayer").text)
    }
    @Test fun sqlPreFilterIsParameterizedAndNeverPrunesOrNotBranches(){
        val f=QueryInterpreter.files("PDF de ayer",day,zone)!!
        val sql=FileSqlFilter.from(f.query)
        assertTrue(sql.selection.contains("modified >= ?"));assertTrue(sql.selection.contains("mime = ?"))
        assertTrue(sql.args.contains("application/pdf"));assertFalse(sql.selection.contains("2026"))
        assertEquals("",FileSqlFilter.from("kind:pdf OR kind:image").selection)
        assertEquals("",FileSqlFilter.from("kind:pdf NOT modified-after:123").selection)
    }
    @Test fun knowledgeAndCalendarResolveWithoutRemoteCalls(){
        assertEquals(QueryIntent.KNOWLEDGE,QueryInterpreter.interpret("Explicame el VOR").intent)
        assertEquals("tomorrow",QueryInterpreter.interpret("¿Qué tengo mañana?").calendarPeriod)
        assertNull(QueryInterpreter.interpret("Spotify").question)
    }
}
