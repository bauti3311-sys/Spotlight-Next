package dev.bautista.spotlight.ui

import dev.bautista.spotlight.ai.ToolCall
import org.junit.Assert.*
import org.junit.Test

class PlanReviewProjectionTest {
    private fun volume(value: Int) = ReviewedStep("Volumen", ToolCall("set_volume", mapOf("percent" to "$value")), "")
    private fun relative(delta: Int, baseline: Int = 20) = volume(0).copy(
        relative = RelativePlanValue(RelativePlanSetting.VOLUME, delta, baseline))
    private fun value(row: ReviewedStep) = row.call!!.arguments.getValue("percent").toInt()

    @Test fun omittedPredecessorsDoNotAffectTheReviewedTarget() {
        val rows = listOf(volume(40), relative(10))
        assertEquals(30, value(projectPlanRows(rows, setOf(1))[1]))
        assertEquals(50, value(projectPlanRows(rows, setOf(0, 1))[1]))
    }
    @Test fun reorderedStepsUseTheNewPrecedingTarget() {
        val rows = listOf(relative(10), volume(40), relative(-5))
        assertEquals(listOf(30, 40, 35), projectPlanRows(rows, rows.indices.toSet()).map(::value))
    }
    @Test fun projectionIsIdempotentAcrossRepeatedSelectionChanges() {
        val rows = listOf(volume(40), relative(10), relative(5))
        val first = projectPlanRows(rows, setOf(0, 1, 2))
        assertEquals(listOf(40, 50, 55), first.map(::value))
        assertEquals(first, projectPlanRows(first, setOf(0, 1, 2)))
        assertEquals(35, value(projectPlanRows(first, setOf(1, 2))[2]))
    }
    @Test fun unavailableStepsAreNeverUsedAsDependencies() {
        val rows = listOf(volume(90).copy(call = null), relative(10))
        assertNull(projectPlanRows(rows, setOf(0, 1))[0].call)
        assertEquals(30, value(projectPlanRows(rows, setOf(0, 1))[1]))
    }
    @Test fun relativeVolumeIsClampedAtBothEnds() {
        val rows = listOf(volume(95), relative(10), relative(-100))
        assertEquals(listOf(95, 100, 0), projectPlanRows(rows, rows.indices.toSet()).map(::value))
    }
    @Test fun brightnessTracksItsOwnSelectedStepsAndRetainsRawMinimum() {
        val bright = ReviewedStep("Brillo", ToolCall("write_display_setting", mapOf("key" to "screen_brightness", "value" to "102")), "")
        val relativeBright = bright.copy(relative = RelativePlanValue(RelativePlanSetting.BRIGHTNESS, 10, 20))
        val rows = listOf(bright, volume(90), relativeBright,
            bright.copy(relative = RelativePlanValue(RelativePlanSetting.BRIGHTNESS, -100, 20)))
        val review = projectPlanRows(rows, rows.indices.toSet())
        assertEquals("127", review[2].call!!.arguments["value"])
        assertEquals("20", review[3].call!!.arguments["value"])
        assertTrue(review[2].title.endsWith("50%"))
        assertEquals("76", projectPlanRows(rows, setOf(1, 2))[2].call!!.arguments["value"])
    }
}
