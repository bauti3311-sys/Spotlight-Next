package dev.bautista.spotlight.ui

import androidx.compose.ui.graphics.vector.ImageVector
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class FocusFoundationTest {
 @Test fun everyBuiltInUtilityUsesAttributedIosArt() {
  var count=0
  for(owner in listOf(CommandIcons,HugeIcons,PhosphorIcons))for(method in owner.javaClass.declaredMethods.filter{it.name.startsWith("get") && java.lang.reflect.Modifier.isPublic(it.modifiers) && it.parameterCount==0 && it.returnType==ImageVector::class.java}) {
   val icon=method.invoke(owner) as ImageVector
   assertNotNull("Missing Icons8 mapping: ${icon.name}",iosAssetFor(icon));count++
  }
  assertTrue(count>=70)
 }
 @Test fun focusAppearancePersistsAndLegacyCanBeRestoredWithoutDataLoss()=runBlocking {
  val app=RuntimeEnvironment.getApplication() as SpotlightApp
  val original=app.preferences.config.first().copy(favorites=setOf("fixture/Main"),contacts=false,privateMode=true,aiModel="retained-model")
  app.preferences.update(original)
  app.preferences.updateAppearance(original.copy(focusInterface=true,focusTypeface="geist"))
  val focus=app.preferences.config.first()
  assertTrue(focus.focusInterface);assertEquals("geist",focus.focusTypeface)
  assertEquals(original.favorites,focus.favorites);assertEquals(original.aiModel,focus.aiModel);assertTrue(focus.privateMode)
  app.preferences.updateAppearance(focus.copy(focusInterface=false))
  val legacy=app.preferences.config.first();assertFalse(legacy.focusInterface);assertEquals(focus.favorites,legacy.favorites)
 }
 @Test fun portableThemesAndPreferencesRoundTripFocusWithoutAuthorizingAnything() {
  val config=dev.bautista.spotlight.data.Config(focusInterface=true,focusTypeface="geist",contacts=false)
  val theme=ThemeTransfer.decode(ThemeTransfer.encode(config),dev.bautista.spotlight.data.Config())
  assertTrue(theme.focusInterface);assertEquals("geist",theme.focusTypeface)
  val portable=ConfigTransfer.decode(ConfigTransfer.encode(config),dev.bautista.spotlight.data.Config())
  assertTrue(portable.focusInterface);assertEquals("geist",portable.focusTypeface);assertFalse(portable.aiRootConsent)
  val old=org.json.JSONObject(ThemeTransfer.encode(config));old.getJSONObject("visual").remove("focusInterface");old.getJSONObject("visual").remove("focusTypeface")
  assertTrue(ThemeTransfer.decode(old.toString(),config).focusInterface)
 }
}
