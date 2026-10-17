package dev.bautista.spotlight.search

import org.junit.Assert.*
import org.junit.Test

class AdvancedDocumentQueryTest {
    private val unpaid=SearchQuery.Document("factura.pdf","document","application/pdf","Pendiente de abono")
    @Test fun naturalPdfPrefixKeepsNegationInsteadOfSearchingTheWordNot() {
        val query=SearchQuery.parse(SearchQuery.natural("PDF factura NOT pagado"))
        assertNull(query.problem);assertTrue(query.matches(unpaid))
        assertFalse(query.matches(unpaid.copy(text="Saldo pagado")))
    }
    @Test fun naturalTypeConstrainsBothBranchesOfOr() {
        val query=SearchQuery.parse(SearchQuery.natural("buscar PDF factura OR recibo"))
        assertNull(query.problem);assertTrue(query.matches(unpaid))
        assertTrue(query.matches(unpaid.copy(name="recibo.pdf")))
        assertFalse(query.matches(unpaid.copy(name="recibo.txt",mime="text/plain")))
    }
    @Test fun naturalPrefixesKeepQuotedOperatorsAndMetadataFilters() {
        val query=SearchQuery.parse(SearchQuery.natural("PDF \"AND OR\" author:\"Juan Pérez\""))
        assertNull(query.problem)
        assertTrue(query.matches(unpaid.copy(text="AND OR",author="Juan Pérez")))
        assertFalse(query.matches(unpaid.copy(text="AND intermedio OR",author="Juan Pérez")))
        assertFalse(query.matches(unpaid.copy(text="AND OR",author="Otra persona")))
    }
    @Test fun explicitGrammarAndMalformedExpressionsAreNeverRepairedSilently() {
        val raw="kind:pdf (factura OR recibo) NOT pagado"
        assertEquals(raw,SearchQuery.natural(raw))
        assertNotNull(SearchQuery.parse(SearchQuery.natural("PDF (factura OR)")).problem)
    }
}
