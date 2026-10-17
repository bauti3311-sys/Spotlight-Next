package dev.bautista.spotlight.index

import dev.bautista.spotlight.control.DeviceResourceState
import org.junit.Assert.*
import org.junit.Test

class OcrBudgetTest {
    private val healthy=DeviceResourceState(80,false,false,1024L*1024*1024,64L*1024*1024,false,0)
    @Test fun lowBatteryAndPowerSavingDeferWithoutInventingCapacity() {
        assertEquals(OcrDeferral.BATTERY,OcrBudget.evaluate(healthy.copy(batteryPercent=15)).deferred)
        assertEquals(OcrDeferral.POWER_SAVE,OcrBudget.evaluate(healthy.copy(powerSave=true)).deferred)
        assertNull(OcrBudget.evaluate(healthy.copy(batteryPercent=10,charging=true)).deferred)
        assertEquals(OcrDeferral.UNKNOWN,OcrBudget.evaluate(healthy.copy(batteryPercent=null)).deferred)
    }
    @Test fun memoryAndThermalSafetyTakePriorityEvenWhileCharging() {
        assertEquals(OcrDeferral.MEMORY,OcrBudget.evaluate(healthy.copy(lowMemory=true,charging=true)).deferred)
        assertEquals(OcrDeferral.MEMORY,OcrBudget.evaluate(healthy.copy(availableMemory=63L*1024*1024)).deferred)
        for(status in 3..6)assertEquals(OcrDeferral.THERMAL,OcrBudget.evaluate(healthy.copy(thermalStatus=status)).deferred)
    }
    @Test fun moderatePressureReducesPageImageAndTimeBudgets() {
        for(state in listOf(healthy.copy(batteryPercent=30),healthy.copy(thermalStatus=2),healthy.copy(availableMemory=100L*1024*1024))) {
            val budget=OcrBudget.evaluate(state)
            assertNull(budget.deferred);assertEquals(1,budget.pages);assertEquals(1024,budget.longEdge)
            assertEquals(786432,budget.pixels);assertEquals(8000L,budget.pageMillis)
        }
    }
    @Test fun recoveredStateRestoresNormalBoundedBudget() {
        OcrBudget.evaluate(healthy.copy(powerSave=true))
        val budget=OcrBudget.evaluate(healthy)
        assertNull(budget.deferred);assertEquals(3,budget.pages);assertEquals(1600,budget.longEdge)
        assertEquals(1800000,budget.pixels);assertEquals(12000L,budget.pageMillis)
    }
    @Test fun unavailableSignalsDeferButOlderApiWithoutThermalSupportIsHonest() {
        assertEquals(OcrDeferral.UNKNOWN,OcrBudget.evaluate(DeviceResourceState()).deferred)
        assertEquals(OcrDeferral.UNKNOWN,OcrBudget.evaluate(healthy.copy(thermalStatus=null)).deferred)
        assertEquals(OcrDeferral.UNKNOWN,OcrBudget.evaluate(healthy.copy(thermalStatus=-1)).deferred)
        assertEquals(OcrDeferral.UNKNOWN,OcrBudget.evaluate(healthy.copy(batteryPercent=101)).deferred)
        assertNull(OcrBudget.evaluate(healthy.copy(thermalSupported=false,thermalStatus=null)).deferred)
    }
}
