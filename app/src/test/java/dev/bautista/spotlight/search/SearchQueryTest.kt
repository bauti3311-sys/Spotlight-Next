package dev.bautista.spotlight.search

import java.time.*
import org.junit.Assert.*
import org.junit.Test

class SearchQueryTest {
    private val pdf =
        SearchQuery.Document(
            "Factura 2026.pdf",
            "document",
            "application/pdf",
            "informe de gastos anuales\nSaldo pagado",
            "Juan Pérez",
            Instant.parse("2026-10-09T18:00:00Z").toEpochMilli(),
            Instant.parse("2025-02-01T12:00:00Z").toEpochMilli(),
        )

    @Test
    fun booleanPrecedenceAndNegation() {
        assertTrue(SearchQuery.parse("factura NOT 2025").matches(pdf))
        assertFalse(SearchQuery.parse("factura NOT 2026").matches(pdf))
        assertTrue(SearchQuery.parse("inexistente AND no OR factura").matches(pdf))
        assertFalse(SearchQuery.parse("(factura OR recibo) AND 2025").matches(pdf))
    }

    @Test
    fun phrasesAreContiguousAndSupportQuotedOperators() {
        assertTrue(SearchQuery.parse("\"informe de gastos anuales\"").matches(pdf))
        assertFalse(SearchQuery.parse("\"informe anuales\"").matches(pdf))
        assertTrue(
            SearchQuery.parse("\"AND\"").matches(SearchQuery.Document("AND.pdf", "document"))
        )
    }

    @Test
    fun kindAuthorAndDatesUseRealMetadata() {
        assertTrue(
            SearchQuery.parse("kind:pdf author:\"Juan Pérez\" modified:2026-10 created:2025")
                .matches(pdf)
        )
        assertFalse(SearchQuery.parse("kind:image").matches(pdf))
        assertFalse(SearchQuery.parse("created:2025").matches(pdf.copy(created = 0)))
        assertTrue(SearchQuery.parse("after:2026-10-01 before:2026-11-01").matches(pdf))
    }

    @Test
    fun malformedQueriesFailWithoutCrashing() {
        for (q in listOf("factura AND", "OR factura", "\"sin cerrar", "(factura", "NOT")) {
            val parsed = SearchQuery.parse(q)
            assertNotNull(parsed.problem)
            assertFalse(parsed.matches(pdf))
        }
    }

    @Test
    fun naturalImageDescriptionsMapToTypeAndTerms() {
        assertEquals("kind:image perros playa", SearchQuery.natural("fotos de perros en la playa"))
        assertTrue(
            SearchQuery.parse(SearchQuery.natural("fotos de perros en la playa"))
                .matches(
                    SearchQuery.Document(
                        "IMG01.jpg",
                        "image",
                        "image/jpeg",
                        "Dog perro perros Beach playa playas",
                    )
                )
        )
    }
}
