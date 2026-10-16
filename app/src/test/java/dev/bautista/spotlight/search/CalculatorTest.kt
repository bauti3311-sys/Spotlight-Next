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
    fun argentineSpanishQuickCalculations() {
        assertEquals("7200", Calculator.evaluate("15% de 48.000"))
        assertEquals("7200", Calculator.evaluate("¿Cuánto es 15% de 48.000?"))
        assertEquals("7200", Calculator.evaluate("48.000 × 0,15"))
        assertEquals("7", Calculator.evaluate("3,5 × 2"))
        assertEquals("12345.67", Calculator.evaluate("12.345,67 + 0"))
        assertEquals("5", Calculator.evaluate("2.5 * 2"))
        assertNull(Calculator.evaluate("¿Cuánto cuesta Spotify?"))
    }

    @Test
    fun fractionalDecimalsMustNotBecomeGroupedThousands() {
        assertEquals("0.25", Calculator.evaluate("0.125 * 2"))
        assertEquals("0.002", Calculator.evaluate("0.001 + 0.001"))
        assertEquals("-0.25", Calculator.evaluate("calculá -0.125 × 2"))
        assertEquals("1000000", Calculator.evaluate("calculá 1.000.000 + 0"))
        assertEquals("7200", Calculator.evaluate("CALCULAR 15% de 48.000"))
        assertEquals("-24000", Calculator.evaluate("calculá -48.000 / 2"))
    }

    @Test
    fun naturalPrefixDoesNotMakeArbitraryTextExecutable() {
        listOf("¿Cuánto es el precio de Spotify?", "calculá abrir Spotify", "12.345.67 + 1",
            "calculá 1/0", "calculá sqrt(-1)", "calculá " + "1+".repeat(121))
            .forEach { assertNull(it, Calculator.evaluate(it)) }
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
