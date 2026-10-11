package dev.bautista.spotlight

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class PermissionActivity:ComponentActivity() {
    private val ask=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        val app=application as SpotlightApp
        lifecycleScope.launch {
            val c=app.preferences.config.first()
            app.preferences.update(c.copy(contacts=grants[Manifest.permission.READ_CONTACTS] ?: c.contacts))
            if(intent.getBooleanExtra("reopen",true) && Settings.canDrawOverlays(this@PermissionActivity)) ContextCompat.startForegroundService(this@PermissionActivity,Intent(this@PermissionActivity,dev.bautista.spotlight.overlay.OverlayService::class.java).putExtra("query",intent.getStringExtra("query").orEmpty()))
            finish()
        }
    }
    override fun onCreate(state:Bundle?) {
        super.onCreate(state)
        if(state!=null)return
        val permissions=when(intent.getStringExtra("access")){"calendar"->listOf(Manifest.permission.READ_CALENDAR);"contacts"->listOf(Manifest.permission.READ_CONTACTS);else->listOf(Manifest.permission.READ_CONTACTS,Manifest.permission.READ_CALENDAR)+if(Build.VERSION.SDK_INT>=33)listOf(Manifest.permission.POST_NOTIFICATIONS) else emptyList()}
        ask.launch(permissions.toTypedArray())
    }
}
