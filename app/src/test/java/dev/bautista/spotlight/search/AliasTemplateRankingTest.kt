package dev.bautista.spotlight.search
import android.content.*
import android.content.pm.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.personal.PersonalCommands
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.last
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class AliasTemplateRankingTest {
 @Test fun templateArgumentsAreLiteralAndMissingArgumentsNeverExecute() {
  val aliases=setOf("musica|buscar {args} en spotify")
  assertEquals("buscar Cerati acústico en spotify",PersonalCommands.expand("música Cerati acústico",aliases))
  assertEquals("musica",PersonalCommands.expand("musica",aliases))
  assertEquals("buscar a; borrar todo en spotify",PersonalCommands.expand("musica a; borrar todo",aliases))
  val result=PersonalCommands.results("musica",AppConfig(aliases=aliases)).single()
  assertTrue(result.action is Action.RestoreQuery)
 }
 @Test fun exactInstalledApplicationCannotBeReplacedByAnAlias()=runBlocking {
  val app=RuntimeEnvironment.getApplication() as SpotlightApp
  val info=ApplicationInfo().apply{packageName="com.spotify.music";nonLocalizedLabel="Spotify"}
  val activity=ActivityInfo().apply{packageName=info.packageName;name="Main";nonLocalizedLabel="Spotify";applicationInfo=info;exported=true}
  val pm=Shadows.shadowOf(app.packageManager)
  pm.installPackage(PackageInfo().apply{packageName=info.packageName;applicationInfo=info;activities=arrayOf(activity)})
  pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=activity})
  app.search.invalidateApps()
  val c=AppConfig(files=false,contacts=false,learning=false,aliases=setOf("spotify|volumen"))
  val rows=app.search.search("Spotify",c).last()
  assertEquals("Spotify",CompactResults.select("Spotify",rows,c).initial.first().title)
 }
}
