package dev.bautista.spotlight.data

import android.app.Application
import dev.bautista.spotlight.ui.AppearanceEdits
import dev.bautista.spotlight.ui.SettingsGroups
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config as RoboConfig

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class SettingsPersistenceTest {
 @Test fun appearanceWritePreservesConcurrentPrivacySourcesAndLegacyData()=runBlocking {
  val p=Preferences(RuntimeEnvironment.getApplication())
  p.update(p.config.first().copy(width=550,landscapeWidth=900,windowScale=-1f,aiProvider="gemini",aiModel="fixture-model",
   snippets=setOf("old|preserved"),routines="[]",favorites=setOf("app:favorite"),trees=setOf("content://old"),aliases=setOf("work|notes")))
  val draft=p.config.first()
  p.editNotes { "[{\"id\":\"fixture\",\"text\":\"local note\",\"created\":1}]" }
  p.update(draft.copy(privateMode=true,autoKeyboard="never",trees=setOf("content://new")))
  p.updateAppearance(draft.copy(theme="light",preset="glass",accent="rose",windowScale=.4f,density="compact",animations=false))
  val actual=Preferences(RuntimeEnvironment.getApplication()).config.first()
  assertEquals("light",actual.theme);assertEquals(.4f,actual.windowScale)
  assertTrue(actual.privateMode);assertEquals("never",actual.autoKeyboard);assertEquals(setOf("content://new"),actual.trees)
  assertEquals(draft.aiProvider,actual.aiProvider);assertEquals(draft.aiModel,actual.aiModel)
  assertEquals(draft.snippets,actual.snippets);assertEquals(draft.routines,actual.routines);assertEquals(draft.favorites,actual.favorites)
  assertEquals(draft.aliases,actual.aliases);assertEquals(550,actual.width);assertEquals(900,actual.landscapeWidth)
  assertTrue(p.noteData.first().contains("local note"))
  p.editNotes{"[]"}
 }
 @Test fun visibleAppearanceMergeDoesNotResetUnrelatedPreferences() {
  val current=Config(privateMode=true,trees=setOf("content://source"),width=700,landscapeWidth=1000,
   snippets=setOf("legacy|keep"),aiModel="private-model",captureContext=true,clipboard=true)
  val next=AppearanceEdits.merge(current,Config(theme="light",windowScale=.8f))
  assertEquals(current.copy(theme="light",windowScale=.8f),next)
 }
 @Test fun staleAiDraftDoesNotOverwritePrivacyAppearanceOrConcurrentEndpoint()=runBlocking {
  val p=Preferences(RuntimeEnvironment.getApplication());val draft=p.config.first()
  val current=draft.copy(privateMode=true,trees=setOf("content://still-authorized"),endpoint="https://fixture.invalid",windowScale=.8f,favorites=setOf("keep"))
  p.update(current)
  p.updateAi(draft,draft.copy(aiProvider="gemini",aiModel="fixture-reviewed-model",aiRootConsent=!draft.aiRootConsent))
  val actual=Preferences(RuntimeEnvironment.getApplication()).config.first()
  assertEquals(current.copy(aiProvider="gemini",aiModel="fixture-reviewed-model",aiRootConsent=!draft.aiRootConsent),actual)
 }
 @Test fun legacyNavigationMapsToSevenRealAreasWithoutRetiredEntries() {
  assertEquals(8,SettingsGroups.names.distinct().size)
  assertEquals("Atajos",SettingsGroups.canonical("Invocación"))
  assertEquals("Permisos y accesos",SettingsGroups.canonical("Privacidad y sistema"))
  assertEquals("Inteligencia artificial",SettingsGroups.canonical("Inteligencia"))
  assertEquals("Búsqueda y archivos",SettingsGroups.canonical("Búsqueda"))
  assertFalse(SettingsGroups.visible("Tasker · automatización externa"))
  assertFalse(SettingsGroups.matches("Rutinas y automatizaciones","rutinas"))
  assertTrue(SettingsGroups.matches("Inteligencia artificial · opcional","gemini clave"))
  assertTrue(SettingsGroups.matches("Apariencia","ancho"))
 }
}
