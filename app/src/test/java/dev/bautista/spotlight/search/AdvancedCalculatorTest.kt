package dev.bautista.spotlight.search

import org.junit.Assert.*
import org.junit.Test

class AdvancedCalculatorTest {
    @Test
    fun trigConstantsAndLogarithms() {
        assertEquals("1", Calculator.evaluate("sin(pi/2)"))
        assertEquals("3", Calculator.evaluate("log(1000)"))
        assertEquals("2", Calculator.evaluate("abs(-2)"))
        assertEquals("1", Calculator.evaluate("cos(0)"))
        assertEquals("1", Calculator.evaluate("sin(π/2)"))
        assertEquals("1", Calculator.evaluate("sind(90)"))
        assertEquals("1", Calculator.evaluate("cosd(0)"))
    }

    @Test
    fun rejectsInvalidDomainsAndTrailingCode() {
        assertNull(Calculator.evaluate("ln(-1)"))
        assertNull(Calculator.evaluate("asin(2)"))
        assertNull(Calculator.evaluate("sin(0);alert(1)"))
        assertNull(Calculator.evaluate("sqrt(-1)"))
    }
}
