package dev.bautista.spotlight.ui

import org.junit.Assert.*
import org.junit.Test

/** Navigation regressions: legacy deep links remain valid after consolidating access. */
class RaycastSettingsNavigationTest {
    @Test fun permissionsHaveOneCanonicalDestination() {
        assertTrue("Permisos y accesos" in SettingsGroups.names)
        assertFalse("Permisos y privacidad" in SettingsGroups.names)
        assertEquals("Permisos y accesos",SettingsGroups.canonical("Permisos y privacidad"))
        assertEquals("Permisos y accesos",SettingsGroups.canonical("Privacidad y sistema"))
        assertEquals("Permisos y accesos",SettingsGroups.of("Permisos y accesos"))
        assertEquals("Permisos y accesos",SettingsGroups.of("Privacidad · Spotlight Glass"))
        assertEquals("Permisos y accesos",SettingsGroups.of("Contactos autorizados"))
    }

    @Test fun shortcutsAndIntegrationsStayOutOfPermissionList() {
        assertEquals("Atajos",SettingsGroups.canonical("Invocación"))
        assertEquals("Atajos",SettingsGroups.of("Invocación · teclado y posición"))
        assertEquals("Atajos",SettingsGroups.of("Teclado físico · acceso global optativo"))
        assertEquals("Atajos",SettingsGroups.of("Acceso rápido"))
        assertEquals("Integraciones",SettingsGroups.of("Integraciones · capacidades disponibles"))
        assertEquals("Permisos y accesos",SettingsGroups.of("Shizuku · integración avanzada opcional"))
    }

    @Test fun retainsAppAndDocumentPreferences() {
        assertEquals("Apariencia",SettingsGroups.of("Apariencia"))
        assertEquals("Búsqueda y archivos",SettingsGroups.of("Búsqueda y archivos"))
        assertEquals("Inteligencia artificial",SettingsGroups.of("Inteligencia artificial · opcional"))
        assertTrue(SettingsGroups.matches("Permisos y accesos","permisos"))
        listOf("shizuku","notificaciones","accesibilidad","archivos","uso","ubicación","contactos").forEach { q ->
            assertTrue("Expected unified permissions to match $q",
                SettingsGroups.matches("Permisos y accesos",q))
        }
    }
}
