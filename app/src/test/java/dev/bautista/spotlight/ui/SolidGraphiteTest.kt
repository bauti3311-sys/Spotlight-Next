package dev.bautista.spotlight.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.data.Preferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config as RoboConfig

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class SolidGraphiteTest {
 @Test fun defaultIsNeutralGraphiteWithIndependentHighOpacityRoles() {
  val c=Config();assertEquals("raycast",c.preset)
  assertEquals(.98f,GlassTokens.opacity(c));assertEquals(1f,GlassTokens.actionOpacity(c));assertEquals(.99f,GlassTokens.settingsOpacity(c))
  val colors=GlassTokens.windowColors(c,true)
  assertEquals(Color(0xFF1B1B20).copy(alpha=.98f),colors.first())
  assertEquals(Color(0xFF19191D),GlassTokens.actionColor(c,true))
  assertNotEquals(colors.first(),GlassTokens.actionColor(c,true))
 }
 @Test fun darkGlassRemainsAnExplicitHistoricalMaterialAndOtherDataIsUntouched() {
  val original=Config(privateMode=true,aliases=setOf("music|Spotify"),trees=setOf("content://source"))
  val glass=AppearancePresets.apply("Dark Glass",original)
  assertEquals(.84f,GlassTokens.opacity(glass));assertEquals(.92f,GlassTokens.actionOpacity(glass));assertEquals(.94f,GlassTokens.settingsOpacity(glass))
  val solid=AppearancePresets.apply("Solid Graphite",glass)
  assertEquals(original.aliases,solid.aliases);assertEquals(original.trees,solid.trees);assertTrue(solid.privateMode)
  assertEquals(.98f,GlassTokens.opacity(solid));assertFalse(solid.blur)
 }
 @Test fun explicitStoredOpacitySurvivesReopeningWithoutForcedMigration()=runBlocking {
  val prefs=Preferences(RuntimeEnvironment.getApplication())
  prefs.update(prefs.config.first().copy(preset="glass",opacity=.73f,actionOpacity=.81f,settingsOpacity=.89f))
  val restored=Preferences(RuntimeEnvironment.getApplication()).config.first()
  assertEquals("glass",restored.preset);assertEquals(.73f,restored.opacity);assertEquals(.81f,restored.actionOpacity);assertEquals(.89f,restored.settingsOpacity)
 }
 @Test fun contrastOverLightAndDarkBackdropsIsStableWithoutBlur() {
  val foreground=GlassTokens.darkInk;val surface=GlassTokens.windowColors(Config(),true).first()
  for(backdrop in listOf(Color.White,Color.Black)) {
   val bg=surface.compositeOver(backdrop)
   val ratio=(foreground.luminance()+.05f)/(bg.luminance()+.05f)
   assertTrue("Text remains readable without cross-window blur",ratio>=7f)
  }
 }
}
