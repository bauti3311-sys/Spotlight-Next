package dev.bautista.spotlight.access

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import dev.bautista.spotlight.overlay.OverlayService
import androidx.core.content.ContextCompat
import android.provider.Settings
import android.widget.Toast
import dev.bautista.spotlight.SpotlightApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

/** Optional physical-keyboard shortcut. Does not retrieve windows or store keystrokes. */
class ShortcutService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var shortcut = "ctrl_alt"
    private var consumed = false

    override fun onServiceConnected() {
        scope.launch {
            (application as SpotlightApp).preferences.config.collectLatest {
                shortcut = it.shortcut
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    public override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_SPACE) return false
        if (event.action == KeyEvent.ACTION_UP && consumed) {
            consumed = false
            return true
        }
        val match =
            when (shortcut) {
                "alt" ->
                    event.isAltPressed &&
                        !event.isCtrlPressed &&
                        !event.isMetaPressed &&
                        !event.isShiftPressed
                "ctrl" ->
                    event.isCtrlPressed &&
                        !event.isAltPressed &&
                        !event.isMetaPressed &&
                        !event.isShiftPressed
                else ->
                    event.isCtrlPressed &&
                        event.isAltPressed &&
                        !event.isMetaPressed &&
                        !event.isShiftPressed
            }
        if (event.action != KeyEvent.ACTION_DOWN || !match) return false
        if (event.repeatCount == 0) {
            val launched =
                runCatching {
                        if (!Settings.canDrawOverlays(this)) {
                            Toast.makeText(this,"Permití la superposición desde Ajustes de Spotlight.",Toast.LENGTH_SHORT).show()
                            return@runCatching false
                        }
                        // AccessibilityService is bound by Android. Invoke the service
                        // directly: bringing our task forward would expose MainActivity.
                        ContextCompat.startForegroundService(this, Intent(this, OverlayService::class.java).setAction("toggle").putExtra("invocation","keyboard"))
                        true
                    }
                    .getOrDefault(false)
            consumed = launched
        }
        return consumed
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
