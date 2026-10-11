package dev.bautista.spotlight.overlay

import android.graphics.PixelFormat
import android.view.WindowManager
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class OverlayStartupTest {
    @Test
    fun windowRootAndComposeChildHaveServiceOwners() {
        val controller = Robolectric.buildService(OverlayService::class.java).create()
        val service = controller.get()
        try {
            val panel = ComposeView(service)
            val host = service.createHost(panel)
            assertSame(service, host.findViewTreeLifecycleOwner())
            assertSame(service, host.findViewTreeSavedStateRegistryOwner())
            assertSame(service, host.findViewTreeViewModelStoreOwner())
            assertSame(service, panel.findViewTreeLifecycleOwner())
            assertSame(service, panel.findViewTreeSavedStateRegistryOwner())
        } finally {
            controller.destroy()
        }
    }

    @Test
    fun composeCanAttachToAnActualOverlayWindow() {
        val controller = Robolectric.buildService(OverlayService::class.java).create()
        val service = controller.get()
        val wm = service.getSystemService(WindowManager::class.java)
        val panel = ComposeView(service).apply { setContent { Text("Spotlight") } }
        val host = service.createHost(panel)
        var added = false
        try {
            wm.addView(
                host,
                WindowManager.LayoutParams(
                    640,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    0,
                    PixelFormat.TRANSLUCENT,
                ),
            )
            added = true
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            panel.createComposition()
            assertTrue(panel.isAttachedToWindow)
        } finally {
            panel.disposeComposition()
            if (added) wm.removeViewImmediate(host)
            controller.destroy()
        }
    }
}
