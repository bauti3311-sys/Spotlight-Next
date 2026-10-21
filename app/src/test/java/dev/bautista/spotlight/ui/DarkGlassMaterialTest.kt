package dev.bautista.spotlight.ui

import android.app.Application
import androidx.compose.ui.graphics.*
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.data.Preferences
import dev.bautista.spotlight.data.ThemeTransfer
import dev.bautista.spotlight.data.ConfigTransfer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class DarkGlassMaterialTest {
 @Test fun everyPresetUsesRealSurfaceAlphaAndActionPanelStaysLegible(){
  for(preset in listOf("glass","raycast","graphite","macos","pure","hybrid","ios"))for(value in listOf(.70f,.80f,.96f,1f)) {
   val c=AppConfig(preset=preset,opacity=value)
   for(dark in listOf(true,false)) {
    GlassTokens.windowColors(c,dark).forEach{assertEquals(value,it.alpha,.005f)}
    assertEquals((value+.08f).coerceAtMost(1f),GlassTokens.actionColor(c,dark).alpha,.005f)
   }
  }
  assertEquals(.80f,GlassTokens.opacity(AppConfig(opacity=Float.NaN)),0f)
  assertEquals(.70f,GlassTokens.opacity(AppConfig(opacity=0f)),0f)
 }
 @Test fun secondaryTextMaintainsAAContrastOnLightAndDarkBackdrops(){
  fun contrast(a:Color,b:Color):Float {val x=a.luminance();val y=b.luminance();return (maxOf(x,y)+.05f)/(minOf(x,y)+.05f)}
  for(dark in listOf(true,false))for(alpha in listOf(.70f,.75f,.79f,.80f,.85f,.96f,1f))for(backdrop in listOf(Color.White,Color.Black)) {
   val c=AppConfig(opacity=alpha)
   for(surface in GlassTokens.windowColors(c,dark)) {
    val bg=surface.compositeOver(backdrop)
    val ratio=contrast(GlassTokens.secondary(c,dark),bg)
    assertTrue("dark=$dark opacity=$alpha backdrop=$backdrop contrast=$ratio",ratio>=4.5f)
   }
  }
 }
 @Test fun opacityPersistsWithoutOverwritingConcurrentPrivateData()=runBlocking {
  val p=Preferences(RuntimeEnvironment.getApplication())
  val before=p.config.first().copy(opacity=.96f,aiModel="keep-model",favorites=setOf("keep"),aliases=setOf("keep|alias"))
  p.update(before)
  p.update(before.copy(privateMode=true,trees=setOf("content://authorized"),autoKeyboard="never"))
  p.updateAppearance(before.copy(opacity=.73f))
  val actual=Preferences(RuntimeEnvironment.getApplication()).config.first()
  assertEquals(.73f,actual.opacity,.0001f)
  assertTrue(actual.privateMode);assertEquals(setOf("content://authorized"),actual.trees)
  assertEquals("never",actual.autoKeyboard);assertEquals("keep-model",actual.aiModel)
  assertEquals(before.favorites,actual.favorites);assertEquals(before.aliases,actual.aliases)
 }
 @Test fun visualAndConfigurationBackupsRoundTripNewOpacityRange(){
  for(alpha in listOf(.70f,.80f,1f)) {
   val c=AppConfig(opacity=alpha)
   assertEquals(alpha,ThemeTransfer.decode(ThemeTransfer.encode(c),AppConfig()).opacity,.0001f)
   assertEquals(alpha,ConfigTransfer.decode(ConfigTransfer.encode(c),AppConfig()).opacity,.0001f)
  }
 }
}
