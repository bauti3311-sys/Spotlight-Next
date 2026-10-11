package dev.bautista.spotlight.search

import org.junit.Assert.*
import org.junit.Test

class LocalPlanTest {
    @Test fun parsesTypedOrderedStepsAndAccents() {
        assertEquals(listOf(PlanStep.Brightness(35),PlanStep.Open("spotify")),LocalPlan.parse("bajar el brillo al 35% y abrir Spotify")!!.steps)
        assertEquals(listOf(PlanStep.Volume(60),PlanStep.Open("spotify")),LocalPlan.parse("ajustar el volumen al 60% después abrir Spotify")!!.steps)
    }
    @Test fun rejectsUnsupportedOrUnsafeStepsRatherThanExecutingPartialInterpretation() {
        listOf("brillo 101% y abrir Spotify","brillo -1% y abrir Spotify","abrir Spotify y bajar brillo mucho","abrir Spotify","buscar información sobre Juan y María","x".repeat(4001)).forEach{assertNull(it,LocalPlan.parse(it))}
        assertNull(LocalPlan.parse((1..7).joinToString(" y "){"abrir Spotify"}))
    }
    @Test fun supportsIndependentVolumeAndBrightnessBoundaries() {
        assertEquals(listOf(PlanStep.Volume(0),PlanStep.Brightness(100)),LocalPlan.parse("volumen al 0% y brillo al 100%")!!.steps)
    }
}
