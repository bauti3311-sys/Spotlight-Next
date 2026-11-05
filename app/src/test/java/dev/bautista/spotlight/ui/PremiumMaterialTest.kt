package dev.bautista.spotlight.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class PremiumMaterialTest {
    @Test fun bothMaterialsKeepReadableTextOnEveryInspectorSurface() {
        for(dark in listOf(true,false)) {
            val p=PremiumMaterial.palette(if(dark)darkColorScheme() else lightColorScheme(),dark)
            for(surface in listOf(p.surface,p.surfaceContainerLow,p.surfaceContainerHigh)) {
                assertTrue(InspectorAccent.contrast(p.onSurface,surface)>=4.5f)
                assertTrue(InspectorAccent.contrast(p.onSurfaceVariant,surface)>=4.5f)
            }
            assertTrue(InspectorAccent.contrast(p.primary,p.surface)>=4.5f)
        }
    }
    @Test fun atelierIsWarmPaperWithDistinctCoolSupportSurfaces() {
        val p=PremiumMaterial.palette(lightColorScheme(),false)
        assertEquals(Color(0xFFFAF9F6),p.surface)
        assertNotEquals(p.surface,p.surfaceContainerLow)
        assertNotEquals(p.surfaceContainerLow,p.surfaceContainerHigh)
    }
    @Test fun brandAccentsRemainScopedAndReadableWithoutTintingNeutralMaterials() {
        val base=PremiumMaterial.palette(darkColorScheme(),true)
        for(pkg in listOf("com.spotify.music","com.whatsapp","com.google.android.gm")) {
            val p=InspectorAccent.palette(base,InspectorAccent.brand(pkg)!!)
            assertEquals(base.surface,p.surface)
            assertTrue(InspectorAccent.contrast(p.primary,p.surface)>=4.5f)
            assertTrue(InspectorAccent.contrast(p.onPrimary,p.primary)>=4.5f)
        }
    }
}
