package dev.bautista.spotlight

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.view.inspector.WindowInspector
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import android.view.inputmethod.InputMethodManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import dev.bautista.spotlight.data.OverlayWidthPolicy
import dev.bautista.spotlight.overlay.OverlayService
import dev.bautista.spotlight.overlay.WindowFocusHost
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream

/** Isolated-emulator test: never change preferences or permissions on a physical device. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 29)
class OverlayGeometryDeviceTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val automation get() = instrumentation.uiAutomation
    private fun shell(command: String): String = automation.executeShellCommand(command).use {
        FileInputStream(it.fileDescriptor).use { stream -> stream.readBytes().toString(Charsets.UTF_8).trim() }
    }
    private fun await(message: String, predicate: () -> Boolean) {
        val end = SystemClock.uptimeMillis() + 15000
        while (!predicate()) {
            if (SystemClock.uptimeMillis() >= end) throw AssertionError(message)
            SystemClock.sleep(20)
        }
    }
    private fun host(): WindowFocusHost? {
        var result: WindowFocusHost? = null
        val find = {
            result = WindowInspector.getGlobalWindowViews().filterIsInstance<WindowFocusHost>()
                .firstOrNull { it.isAttachedToWindow && it.width > 0 && it.height > 0 }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) find() else instrumentation.runOnMainSync { find() }
        return result
    }
    private fun editor(view: View): View? {
        if (view.onCheckIsTextEditor()) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) editor(view.getChildAt(i))?.let { return it }
        return null
    }
    private fun keyboardBounds(): Rect? = automation.windows
        .firstOrNull { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }
        ?.let { window -> Rect().also { window.getBoundsInScreen(it) } }?.takeIf { !it.isEmpty }
    private fun focusedQuery(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        if (node.isEditable && node.isFocused && node.text?.toString() == "2+2") return true
        return (0 until node.childCount).any { focusedQuery(node.getChild(it)) }
    }
    private fun assertAndCapture(context: Context, stage: String) {
        await("IME disappeared during $stage") { keyboardBounds() != null }
        await("Query editor lost focus during $stage") {
            automation.windows.any { it.root?.packageName?.toString() == context.packageName && focusedQuery(it.root) }
        }
        val keyboard = requireNotNull(keyboardBounds())
        val root = Rect()
        val visible = Rect()
        instrumentation.runOnMainSync {
            val view = requireNotNull(host())
            val xy = IntArray(2)
            view.getLocationOnScreen(xy)
            root.set(xy[0], xy[1], xy[0] + view.width, xy[1] + view.height)
            view.getWindowVisibleDisplayFrame(visible)
        }
        assertTrue("Unsafe root=$root visible=$visible ($stage)", visible.contains(root))
        assertTrue("Overlay overlaps IME: root=$root ime=$keyboard ($stage)", root.bottom <= keyboard.top)
        val directory = requireNotNull(context.getExternalFilesDir("f13-device"))
        directory.mkdirs()
        val bitmap = requireNotNull(automation.takeScreenshot())
        try { File(directory, "$stage.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
        finally { bitmap.recycle() }
        File(directory, "$stage.txt").writeText("API=${Build.VERSION.SDK_INT}\nroot=$root\nvisible=$visible\nime=$keyboard\nqueryFocused=true\n")
        // AGP may uninstall the target APK after the run, removing app-specific files.
        val source = directory.absolutePath.replace("'", "'\"'\"'")
        val exported = shell("mkdir -p /data/local/tmp/spotlight-geometry && cp -R '$source'/. /data/local/tmp/spotlight-geometry/ && echo exported")
        assertEquals("Capture export failed before test cleanup", "exported", exported)
    }

    @Test fun softwareImeWidthChangeAndRotationKeepOverlaySafeAndQueryFocused() {
        assertTrue("Requires an isolated emulator before modifying any data", Build.HARDWARE in setOf("ranchu", "goldfish"))
        val context = instrumentation.targetContext
        val app = context.applicationContext as SpotlightApp
        await("Preferences not ready") { app.preferences.ready }
        val original = runBlocking { app.preferences.config.first() }
        val hardKeyboard = shell("settings get secure show_ime_with_hard_keyboard")
        val previousFlags = automation.serviceInfo.flags
        automation.serviceInfo = automation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            context.stopService(Intent(context, OverlayService::class.java))
            instrumentation.waitForIdleSync()
            await("Previous overlay still attached") { host() == null }
            shell("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow")
            shell("settings put secure show_ime_with_hard_keyboard 1")
            val fixture = original.copy(width = 0, landscapeWidth = 0, position = .35f,
                landscapePosition = .35f, files = false, animations = false, clipboard = false, captureContext = false)
            runBlocking { app.preferences.update(fixture) }
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
            context.startForegroundService(Intent(context, OverlayService::class.java)
                .putExtra("query", "2+2").putExtra("invocation", "touch"))
            await("Overlay absent") { host() != null }
            await("Overlay text editor not ready") {
                var ready = false
                instrumentation.runOnMainSync { ready = host()?.let { editor(it) != null } == true }
                ready
            }
            instrumentation.runOnMainSync {
                val view = requireNotNull(editor(requireNotNull(host())))
                context.getSystemService(InputMethodManager::class.java).showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
            assertAndCapture(context, "portrait-auto")
            runBlocking { app.preferences.update(OverlayWidthPolicy.manual(fixture, 320)) }
            instrumentation.waitForIdleSync()
            assertAndCapture(context, "portrait-manual")
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            await("Landscape not applied") { context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE }
            instrumentation.waitForIdleSync()
            assertAndCapture(context, "landscape-manual")
            runBlocking { app.preferences.update(fixture) }
            instrumentation.waitForIdleSync()
            assertAndCapture(context, "landscape-auto")
            assertEquals(original.density, runBlocking { app.preferences.config.first() }.density)
        } finally {
            context.stopService(Intent(context, OverlayService::class.java))
            instrumentation.waitForIdleSync()
            runBlocking { app.preferences.update(original) }
            if (hardKeyboard == "null") shell("settings delete secure show_ime_with_hard_keyboard")
            else shell("settings put secure show_ime_with_hard_keyboard $hardKeyboard")
            automation.serviceInfo = automation.serviceInfo.apply { flags = previousFlags }
            scenario.close()
        }
    }
}
