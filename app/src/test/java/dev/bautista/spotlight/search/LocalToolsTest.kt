package dev.bautista.spotlight.search

import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class LocalToolsTest {
    @Test
    fun convertsCompatibleUnitsAndRejectsDifferentFamilies() {
        assertEquals("6.21371192 mi", LocalTools.convert("10 km a mi")?.title)
        assertEquals("1536 kib", LocalTools.convert("1,5 mib a kib")?.title)
        assertNull(LocalTools.convert("10 kg a km"))
        assertNull(LocalTools.convert("abrir gmail"))
    }

    @Test
    fun naturalConversionsUseOnlyKnownCompatibleUnits() {
        assertEquals("2000 metros", LocalTools.convert("¿Cuántos metros son 2 km?")?.title)
        assertEquals("1536 kib", LocalTools.convert("convertí 1,5 mib a kib")?.title)
        assertEquals("0 c", LocalTools.convert("¿Cuánto es 32 f en c?")?.title)
        assertNull(LocalTools.convert("¿Cuántos dólares son 100 pesos?"))
        assertNull(LocalTools.convert("¿Cuántos kg son 10 metros?"))
        assertNull(LocalTools.convert("convertí " + "1".repeat(241) + " km a m"))
    }

    @Test
    fun temperaturesRespectAbsoluteZero() {
        assertEquals("0 c", LocalTools.convert("32 f a c")?.title)
        assertEquals("273.15 k", LocalTools.convert("0 c a k")?.title)
        assertNull(LocalTools.convert("-274 c a f"))
    }

    @Test
    fun clockUsesRealTimeZonesAndDateRollover() {
        val instant = Instant.parse("2026-10-09T23:30:00Z")
        assertEquals("08:30", LocalTools.clock("hora en Tokio", instant)?.title)
        assertTrue(LocalTools.clock("hora en Tokio", instant)!!.subtitle.contains("2026-10-10"))
        assertNull(LocalTools.clock("hora en lugar inexistente", instant))
    }

    @Test
    fun datesRejectInvalidCalendarInput() {
        assertEquals(
            "8 días",
            LocalTools.dateDifference("días entre 2026-10-01 y 2026-10-09")?.title,
        )
        assertNull(LocalTools.dateDifference("dias entre 2026-02-30 y 2026-03-01"))
    }

    @Test
    fun quickLinksRejectCredentialsAndExecutableSchemes() {
        assertTrue(
            LocalTools.validQuickLink("wiki", "https://es.wikipedia.org/w/index.php?search={q}")
        )
        assertFalse(LocalTools.validQuickLink("x", "javascript:alert(1)"))
        assertFalse(LocalTools.validQuickLink("x", "https://user:pass@example.com/"))
        assertFalse(LocalTools.validQuickLink("alias con espacios", "https://example.com"))
    }
}
