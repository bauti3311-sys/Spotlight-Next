package dev.bautista.spotlight

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.bautista.spotlight.control.DeviceResources
import dev.bautista.spotlight.index.OcrBudget
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceResourcesDeviceTest {
    @Test fun publicResourceReadingsAndOcrDecisionStayWithinBounds() {
        val state=DeviceResources.read(InstrumentationRegistry.getInstrumentation().targetContext)
        assertTrue(state.batteryPercent==null || state.batteryPercent in 0..100)
        assertTrue(state.availableMemory==null || state.availableMemory>=0)
        val decision=OcrBudget.evaluate(state)
        assertTrue(decision.pages in 0..3);assertTrue(decision.pixels<=1800000)
        if(decision.deferred!=null)assertEquals(0,decision.pages)
    }
}
