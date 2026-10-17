package dev.bautista.spotlight.control

import android.app.Application
import android.content.Intent
import android.os.BatteryManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class DeviceResourcesTest {
    @Suppress("DEPRECATION")
    @Test fun sharedReaderUsesReportedBatteryScaleAndPlugState() {
        val context=RuntimeEnvironment.getApplication()
        context.sendStickyBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).putExtra(BatteryManager.EXTRA_LEVEL,1)
            .putExtra(BatteryManager.EXTRA_SCALE,4).putExtra(BatteryManager.EXTRA_PLUGGED,0)
            .putExtra(BatteryManager.EXTRA_STATUS,BatteryManager.BATTERY_STATUS_FULL))
        val state=DeviceResources.read(context)
        assertEquals(25,state.batteryPercent);assertEquals(false,state.charging);assertTrue(state.thermalSupported)
    }
    @Suppress("DEPRECATION")
    @Config(sdk=[26])
    @Test fun invalidCapacityIsUnknownAndOlderApiDoesNotFabricateThermalData() {
        val context=RuntimeEnvironment.getApplication()
        context.sendStickyBroadcast(Intent(Intent.ACTION_BATTERY_CHANGED).putExtra(BatteryManager.EXTRA_LEVEL,500)
            .putExtra(BatteryManager.EXTRA_SCALE,100))
        val state=DeviceResources.read(context)
        assertNull(state.batteryPercent);assertNull(state.thermalStatus);assertFalse(state.thermalSupported)
    }
}
