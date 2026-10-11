package dev.bautista.spotlight.search

import org.junit.Assert.*
import org.junit.Test

class CalculatorTest {
    @Test
    fun realQueries() {
        assertEquals("1000", Calculator.evaluate("125 × 8"))
        assertEquals("625", Calculator.evaluate("2500 / 4"))
        assertEquals("2700", Calculator.evaluate("15% de 18000"))
        assertEquals("1024", Calculator.evaluate("2^10"))
        assertEquals("9", Calculator.evaluate("sqrt(81)"))
    }

    @Test
    fun precedence() {
        assertEquals("14", Calculator.evaluate("2+3*4"))
        assertEquals("512", Calculator.evaluate("2^3^2"))
        assertEquals("-4", Calculator.evaluate("-2^2"))
        assertEquals("0.5", Calculator.evaluate("50%"))
    }

    @Test
    fun rejectsInvalid() {
        listOf("hola", "1/0", "sqrt(-1)", "2+", "System.exit(0)", "(2+3", "2+3)", "2^9999")
            .forEach { assertNull(it, Calculator.evaluate(it)) }
    }

    @Test
    fun searchRelevance() {
        assertTrue(relevance("chrome", "Chrome") > relevance("chro", "Chrome"))
        assertTrue(relevance("spotligh", "Spotlight") > 0)
        assertTrue(relevance("camara", "Cámara") > 0)
        assertEquals(0, relevance("xx", "Chrome"))
    }
}
