package dev.bautista.spotlight.control

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager

data class DeviceResourceState(
    val batteryPercent:Int?=null, val charging:Boolean?=null, val powerSave:Boolean?=null,
    val availableMemory:Long?=null, val memoryThreshold:Long?=null, val lowMemory:Boolean?=null,
    val thermalStatus:Int?=null, val thermalSupported:Boolean=true,
)

/** Shared public-API readings formerly in AdvancedPanel; unknown values remain unknown. */
object DeviceResources {
    fun read(context:Context):DeviceResourceState {
        val memory=runCatching { ActivityManager.MemoryInfo().also {
            requireNotNull(context.getSystemService(ActivityManager::class.java)).getMemoryInfo(it)
        } }.getOrNull()
        val battery=runCatching { context.registerReceiver(null,IntentFilter(Intent.ACTION_BATTERY_CHANGED)) }.getOrNull()
        val level=battery?.getIntExtra(BatteryManager.EXTRA_LEVEL,-1) ?: -1
        val scale=battery?.getIntExtra(BatteryManager.EXTRA_SCALE,-1) ?: -1
        val status=battery?.getIntExtra(BatteryManager.EXTRA_STATUS,-1) ?: -1
        val plugged=battery?.getIntExtra(BatteryManager.EXTRA_PLUGGED,-1) ?: -1
        val power=runCatching{context.getSystemService(PowerManager::class.java)}.getOrNull()
        return DeviceResourceState(
            batteryPercent=if(level>=0 && scale>0 && level<=scale)(level.toLong()*100/scale).toInt() else null,
            charging=if(plugged>=0)plugged!=0 else if(status==BatteryManager.BATTERY_STATUS_CHARGING)true else null,
            powerSave=runCatching { requireNotNull(power).isPowerSaveMode }.getOrNull(),
            availableMemory=memory?.availMem?.takeIf { it>=0 }, memoryThreshold=memory?.threshold?.takeIf { it>=0 }, lowMemory=memory?.lowMemory,
            thermalStatus=if(Build.VERSION.SDK_INT>=29)runCatching { requireNotNull(power).currentThermalStatus }.getOrNull() else null,
            thermalSupported=Build.VERSION.SDK_INT>=29,
        )
    }
}
