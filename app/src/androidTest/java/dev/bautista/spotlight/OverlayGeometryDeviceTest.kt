package dev.bautista.spotlight

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
    private fun windowWidth(): Int {
        var width = 0
        instrumentation.runOnMainSync { width = host()?.width ?: 0 }
        return width
    }
    private fun assertAndCapture(context: Context, stage: String, expectIme: Boolean = true) {
        var previous: Rect? = null
        var stableSince = SystemClock.uptimeMillis()
        var sampled = false
        await("Unexpected or unsettled IME visibility during $stage") {
            val current = keyboardBounds()
            val now = SystemClock.uptimeMillis()
            if (!sampled || current != previous || (current != null) != expectIme) {
                sampled = true
                previous = current
                stableSince = now
            }
            (current != null) == expectIme && now - stableSince >= 100
        }
        await("Query editor lost focus during $stage") {
            automation.windows.any { it.root?.packageName?.toString() == context.packageName && focusedQuery(it.root) }
        }
        val keyboard = keyboardBounds()
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
        if (expectIme) {
            assertNotNull("IME disappeared before capture ($stage)", keyboard)
            assertTrue("Overlay overlaps IME: root=$root ime=$keyboard ($stage)", root.bottom <= requireNotNull(keyboard).top)
        } else assertNull("IME reappeared before capture ($stage)", keyboard)
        val directory = requireNotNull(context.getExternalFilesDir("f13-device"))
        directory.mkdirs()
        val bitmap = requireNotNull(automation.takeScreenshot())
        try { File(directory, "$stage.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
        finally { bitmap.recycle() }
        File(directory, "$stage.txt").writeText("API=${Build.VERSION.SDK_INT}\nroot=$root\nvisible=$visible\nime=$keyboard\nqueryFocused=true\n")
        // Shared Downloads survives AGP uninstall; Android 11 blocks shell reads of
        // Android/data. Publish only these synthetic emulator fixtures via scoped storage.
        for (file in listOf(File(directory, "$stage.png"), File(directory, "$stage.txt"))) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, if (file.extension == "png") "image/png" else "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/spotlight-geometry/")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = requireNotNull(resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values))
            try {
                requireNotNull(resolver.openOutputStream(uri)).use { output ->
                    file.inputStream().use { it.copyTo(output) }
                }
                resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
                requireNotNull(resolver.openInputStream(uri)).use { input ->
                    assertArrayEquals("Capture publication corrupted ${file.name}", file.readBytes(), input.readBytes())
                }
            } catch (failure: Throwable) {
                resolver.delete(uri, null, null)
                throw failure
            }
        }
        val target = "/sdcard/${Environment.DIRECTORY_DOWNLOADS}/spotlight-geometry/$stage.png"
        assertEquals("Capture export failed before test cleanup", target, shell("ls $target"))
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
                landscapePosition = .35f, adaptive = true, autoKeyboard = "always", files = false, animations = false, clipboard = false, captureContext = false)
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
            val manualPixels = (380 * context.resources.displayMetrics.density).toInt()
            // Portrait phones may cap the saved manual width to their safe viewport.
            val portraitPixels = minOf(manualPixels, windowWidth())
            runBlocking { app.preferences.update(OverlayWidthPolicy.manual(fixture, 380)) }
            assertEquals(380, runBlocking { app.preferences.config.first() }.width)
            await("Portrait manual width not applied") { windowWidth() == portraitPixels }
            instrumentation.waitForIdleSync()
            assertAndCapture(context, "portrait-manual")
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            await("Landscape not applied") { context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE }
            instrumentation.waitForIdleSync()
            await("Rotation lost manual width") { windowWidth() == manualPixels }
            assertAndCapture(context, "landscape-manual")
            runBlocking { app.preferences.update(fixture) }
            instrumentation.waitForIdleSync()
            await("Automatic width did not restore") { windowWidth() > manualPixels }
            assertAndCapture(context, "landscape-auto")
            val autoWidth = windowWidth()
            instrumentation.runOnMainSync {
                val view = requireNotNull(editor(requireNotNull(host())))
                context.getSystemService(InputMethodManager::class.java).hideSoftInputFromWindow(view.windowToken, 0)
            }
            assertAndCapture(context, "landscape-ime-hidden", expectIme = false)
            assertEquals("Hiding IME changed automatic width", autoWidth, windowWidth())
            // The IME window can disappear before its focus/input-connection handoff
            // completes. Semantics focus alone does not mean IMM can accept a show.
            await("Overlay input connection did not recover after hiding IME") {
                var ready = false
                instrumentation.runOnMainSync {
                    val root = host()
                    val input = root?.let { editor(it) }
                    ready = root?.hasWindowFocus() == true && input != null &&
                        context.getSystemService(InputMethodManager::class.java).isActive(input)
                }
                ready
            }
            instrumentation.runOnMainSync {
                val view = requireNotNull(editor(requireNotNull(host())))
                context.getSystemService(InputMethodManager::class.java).showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
            assertAndCapture(context, "landscape-ime-restored")
            assertEquals("Showing IME changed automatic width", autoWidth, windowWidth())
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
