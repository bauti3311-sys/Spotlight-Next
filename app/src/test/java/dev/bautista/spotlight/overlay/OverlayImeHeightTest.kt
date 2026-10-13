package dev.bautista.spotlight.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28, 33], application = android.app.Application::class)
class OverlayImeHeightTest {
    @Test fun geometryRefreshRetainsKeyboardUntilInsetsReportItHidden() = verifyInsetRetention()

    @Test @Config(qualifiers = "w800dp-h1280dp-port")
    fun tabletPortraitRetainsKeyboardOnRefresh() = verifyInsetRetention()

    @Test @Config(qualifiers = "w1280dp-h800dp-land")
    fun tabletLandscapeRetainsKeyboardOnRefresh() = verifyInsetRetention()

    private fun verifyInsetRetention() {
        val controller = Robolectric.buildService(OverlayService::class.java).create()
        val service = controller.get()
        val height = OverlayService::class.java.getDeclaredMethod("getAvailableHeightDp").apply { isAccessible = true }
        fun available() = height.invoke(service) as Float
        try {
            service.updateAvailableHeight(0)
            val withoutKeyboard = available()
            service.updateAvailableHeight(180)
            val withKeyboard = available()
            assertTrue(withKeyboard < withoutKeyboard)
            // Width/preferences and configuration callbacks refresh without a new inset.
            service.updateAvailableHeight()
            assertEquals(withKeyboard, available(), 0f)
            service.onConfigurationChanged(service.resources.configuration)
            assertEquals(withKeyboard, available(), 0f)
            service.updateAvailableHeight(0)
            assertEquals(withoutKeyboard, available(), 0f)
        } finally { controller.destroy() }
    }
}
