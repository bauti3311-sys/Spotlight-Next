package dev.bautista.spotlight.ui
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import org.junit.Test
import org.junit.Assert.assertTrue
class LiquidMaterialTest {
 private fun contrast(dark:Boolean) {
  val c=liquidPalette(if(dark)darkColorScheme() else lightColorScheme(),dark)
  for(surface in listOf(c.surface,c.surfaceContainerHigh,c.surfaceContainerLow)) {
   assertTrue("primary text",InspectorAccent.contrast(c.onSurface,surface)>=4.5f)
   assertTrue("secondary text",InspectorAccent.contrast(c.onSurfaceVariant,surface)>=4.5f)
   assertTrue("neutral action",InspectorAccent.contrast(c.primary,surface)>=4.5f)
  }
 }
 @Test fun darkTextContrast()=contrast(true)
 @Test fun lightTextContrast()=contrast(false)
}
