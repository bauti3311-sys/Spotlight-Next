package dev.bautista.spotlight.data

import android.app.Application
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.json.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config as RoboConfig

@RunWith(RobolectricTestRunner::class)
@RoboConfig(sdk=[33],application=Application::class)
class PortableThemeCollectionTest {
 @Test fun themesAndPersonalCommandsRoundTripWithoutRestoringGrants()=runBlocking {
  val source=ThemeTransfer.save(Config(aliases=setOf("musica|abrir Spotify"),quickLinks=setOf("sitio|https://example.com"),opacity=.81f,typeface="inter"),"Mi Glass")
  val p=Preferences(RuntimeEnvironment.getApplication());p.update(Config(trees=setOf("content://local-only"),aiRootConsent=true))
  p.importPortable(ConfigTransfer.encode(source))
  val restored=p.config.first()
  assertEquals(source.aliases,restored.aliases);assertEquals(source.quickLinks,restored.quickLinks)
  val list=JSONArray(restored.customThemes);assertEquals(1,list.length())
  val theme=ThemeTransfer.decode(list.getJSONObject(0).getJSONObject("theme").toString(),Config())
  assertEquals(.81f,theme.opacity);assertTrue(restored.aiRootConsent);assertEquals(setOf("content://local-only"),restored.trees)
 }
 @Test fun malformedThemeRollsBackEntireTransaction()=runBlocking {
  val p=Preferences(RuntimeEnvironment.getApplication());val current=ThemeTransfer.save(Config(theme="dark"),"Existente");p.update(current)
  val before=p.config.first();val root=JSONObject(ConfigTransfer.encode(ThemeTransfer.save(Config(theme="light"),"Importado")))
  root.getJSONObject("settings").getJSONArray("customThemes").getJSONObject(0).getJSONObject("theme").getJSONObject("visual").put("trees",JSONArray().put("content://forbidden"))
  assertTrue(runCatching{p.importPortable(root.toString())}.isFailure);assertEquals(before,p.config.first())
 }
 @Test fun legacyBackupPreservesExistingThemes()=runBlocking {
  val p=Preferences(RuntimeEnvironment.getApplication());p.update(ThemeTransfer.save(Config(),"Existente"));val before=p.config.first().customThemes
  p.importPortable("""{"format":"spotlight-next","version":1,"settings":{"theme":"dark"}}""")
  assertEquals(before,p.config.first().customThemes)
 }
 @Test fun duplicateIdsWrongTypesAndExcessCollectionsAreRejected() {
  val valid=JSONArray(ThemeTransfer.save(Config(),"Glass").customThemes)
  val duplicate=JSONArray().put(valid.getJSONObject(0)).put(valid.getJSONObject(0))
  assertTrue(runCatching{ThemeTransfer.portableThemes(duplicate.toString())}.isFailure)
  val wrong=JSONObject(ThemeTransfer.encode(Config()));wrong.getJSONObject("visual").put("blur","false")
  assertTrue(runCatching{ThemeTransfer.decode(wrong.toString(),Config())}.isFailure)
  assertTrue(runCatching{ThemeTransfer.portableThemes(JSONArray((0..20).map{valid.getJSONObject(0)}).toString())}.isFailure)
 }
}
