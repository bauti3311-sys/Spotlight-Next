package dev.bautista.spotlight.overlay

import org.junit.Assert.*
import org.junit.Test

class OverlayLayoutPolicyTest {
    @Test fun narrowOverlayNeverUsesDeviceWidthToCreateTwoPanes() {
        assertEquals(OverlayLayoutPolicy.Mode.Compact, OverlayLayoutPolicy.resolve(360f, 800f).mode)
        assertEquals(OverlayLayoutPolicy.Mode.Intermediate, OverlayLayoutPolicy.resolve(608f, 800f).mode)
        assertFalse(OverlayLayoutPolicy.resolve(691f, 800f).split)
    }
    @Test fun everyDividerPositionPreservesBothMinimums() {
        for (width in listOf(692f, 728f, 840f, 960f)) {
            val layout = OverlayLayoutPolicy.resolve(width, 640f)
            assertTrue(layout.split)
            for (requested in listOf(.1f, .35f, .62f, .9f)) {
                val available = width - OverlayLayoutPolicy.dividerDp
                val fraction = layout.fraction(requested)
                assertTrue(available * fraction >= 399.99f)
                assertTrue(available * (1f - fraction) >= 279.99f)
            }
        }
    }
    @Test fun keyboardAndLargeTextChooseOnePaneWithoutChangingPreference() {
        assertFalse(OverlayLayoutPolicy.resolve(840f, 220f).split)
        assertFalse(OverlayLayoutPolicy.resolve(728f, 640f, 1.3f).split)
        assertTrue(OverlayLayoutPolicy.resolve(960f, 640f, 1.3f).split)
        assertEquals(.62f, OverlayLayoutPolicy.resolve(960f, 640f).fraction(), .001f)
    }
}
