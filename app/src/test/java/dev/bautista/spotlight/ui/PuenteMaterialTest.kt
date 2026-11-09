package dev.bautista.spotlight.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class PuenteMaterialTest {
 @Test fun darkAndLightTextRemainReadableOnAllContentLayers() {
  for(dark in listOf(true,false)) {
   val c=PuenteMaterial.palette(if(dark)darkColorScheme() else lightColorScheme(),dark)
   for(surface in listOf(c.surface,c.surfaceContainerLow,c.surfaceContainerHigh)) {
    assertEquals(1f,surface.alpha)
    assertTrue("Primary text contrast $dark",InspectorAccent.contrast(c.onSurface,surface)>=7f)
    assertTrue("Secondary text contrast $dark",InspectorAccent.contrast(c.onSurfaceVariant,surface)>=4.5f)
   }
  }
 }
 @Test fun materialsPreserveCustomAccentAndErrorMeaning() {
  val base=darkColorScheme(primary=Color(0xFFFFBB88),error=Color(0xFFFF6B81))
  val c=PuenteMaterial.palette(base,true)
  assertEquals(base.primary,c.primary);assertEquals(base.onPrimary,c.onPrimary)
  assertEquals(base.error,c.error);assertEquals(base.onError,c.onError)
 }
}
