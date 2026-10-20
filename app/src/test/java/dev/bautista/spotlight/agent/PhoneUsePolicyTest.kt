package dev.bautista.spotlight.agent

import org.junit.Assert.*
import org.junit.Test

class PhoneUsePolicyTest {
    @Test fun validatesRealAppAndBlocksPrivilegedWindows() {
        val host = "dev.bautista.spotlight.next"
        assertTrue(PhoneUsePolicy.allowPackage("com.whatsapp", "com.whatsapp", host))
        assertFalse(PhoneUsePolicy.allowPackage("com.whatsapp", "com.spotify.music", host))
        assertFalse(PhoneUsePolicy.allowPackage(host, host, host))
        assertFalse(PhoneUsePolicy.allowPackage("com.android.systemui", "com.android.systemui", host))
        assertFalse(PhoneUsePolicy.allowPackage("com.android.permissioncontroller", "com.android.permissioncontroller", host))
        assertFalse(PhoneUsePolicy.allowPackage("android", "android", host))
        assertFalse(PhoneUsePolicy.allowPackage("invalid package", "invalid package", host))
    }

    @Test fun onlyExactTypedSelectorsAreAccepted() {
        assertEquals(PhoneUsePolicy.Selector.Id("com.whatsapp:id/send"), PhoneUsePolicy.parseSelector("id:com.whatsapp:id/send"))
        assertEquals(PhoneUsePolicy.Selector.Label("Enviar"), PhoneUsePolicy.parseSelector("text:Enviar"))
        assertEquals(PhoneUsePolicy.Selector.Description("Enviar"), PhoneUsePolicy.parseSelector("desc:Enviar"))
        listOf("Enviar", "tap:12,30", "id:malicious;rm", "text:", "desc:", "text:Hello\nWorld").forEach { raw ->
            assertThrows(IllegalArgumentException::class.java) { PhoneUsePolicy.parseSelector(raw) }
        }
    }

    @Test fun overlayRedrawDoesNotInvalidateExternalObservation() {
        val host = "dev.bautista.spotlight.next"
        val observed = "com.whatsapp"
        assertFalse(PhoneUsePolicy.invalidateObservation(host, host, observed))
        assertTrue(PhoneUsePolicy.invalidateObservation(observed, host, observed))
        assertTrue(PhoneUsePolicy.invalidateObservation("com.android.systemui", host, observed))
        assertTrue(PhoneUsePolicy.invalidateObservation(null, host, observed))
        assertFalse(PhoneUsePolicy.invalidateObservation(observed, host, ""))
    }

    @Test fun textLengthAndLabelsAreBounded() {
        assertTrue(PhoneUsePolicy.allowText("Llego a las 9"))
        assertFalse(PhoneUsePolicy.allowText(""))
        assertFalse(PhoneUsePolicy.allowText("a".repeat(1601)))
        assertFalse(PhoneUsePolicy.allowText("a\u0000b"))
        assertEquals("Enviar ahora", PhoneUsePolicy.compactLabel("  Enviar\nahora  "))
        assertEquals(65, PhoneUsePolicy.compactLabel("x".repeat(200))?.length)
    }
}
