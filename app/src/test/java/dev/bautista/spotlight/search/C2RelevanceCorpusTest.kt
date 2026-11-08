package dev.bautista.spotlight.search

import android.Manifest
import android.content.*
import android.content.pm.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.CommandCenter
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

/** Reproducible local fixtures; no private accounts, external execution, models or network. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class C2RelevanceCorpusTest {
 private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
 private val c=AppConfig(files=false,contacts=false,clipboard=false,learning=false)
 private val launcher get()=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
 private fun install(pkg:String,label:String):ResolveInfo {
  val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=label;enabled=true}
  val activity=ActivityInfo().apply{packageName=pkg;name="Main";nonLocalizedLabel=label;applicationInfo=a;enabled=true;exported=true}
  val info=ResolveInfo().apply{activityInfo=activity}
  val pm=Shadows.shadowOf(app.packageManager)
  pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity)})
  pm.addResolveInfoForIntent(launcher,info);pm.addResolveInfoForIntent(launcher.setPackage(pkg),info)
  app.search.invalidateApps();return info
 }
 private fun frames(q:String,config:AppConfig=c)=runBlocking{app.search.search(q,config).toList()}
 private fun initial(q:String,config:AppConfig=c):List<Result> {
  val all=frames(q,config).last();val visible=CompactResults.select(q,all,config).initial
  println("CORPUS query=[$q] all=${all.map{it.id+":"+it.title}} initial=${visible.map{it.id+":"+it.title}}")
  return visible
 }
 private fun application(id:String,title:String,score:Int=1000)=Result(id,title,"","Aplicaciones",score,Action.Launch(Intent(Intent.ACTION_MAIN).setComponent(ComponentName(id,"Main"))))
 @Test fun exactApplicationsAndPrefixesLeadEveryProviderSnapshot() {
  install("fixture.webos","webOS Dev Manager");install("com.spotify.music","Spotify");install("com.whatsapp","WhatsApp");install("fixture.wifi","Wi-Fi")
  for((q,pkg) in listOf("webOS Dev Manager" to "fixture.webos","Spotify" to "com.spotify.music","spot" to "com.spotify.music")) {
   val config=c.copy(learning=true,favorites=setOf("com.whatsapp/Main","fixture.wifi/Main"),usageData=JSONObject().put("com.whatsapp/Main",JSONObject().put("count",999999).put("time",System.currentTimeMillis())).toString())
   val snapshots=frames(q,config);assertTrue(snapshots.isNotEmpty())
   snapshots.forEach{rows->assertEquals(q,pkg,((CompactResults.select(q,rows,config).initial.first().action as Action.Launch).intent.component?.packageName))}
   initial(q,config)
  }
 }
 @Test fun internalWhitespaceCannotLetAnAliasHideTheExactInstalledApp() {
  install("fixture.webos","webOS Dev Manager");install("com.spotify.music","Spotify")
  val config=c.copy(aliases=setOf("webos|Spotify"))
  for(q in listOf("webOS Dev Manager","  WEBOS   DEV\tMANAGER  ")) {
   val first=initial(q,config).first();assertTrue("Exact app must survive alias: $q / ${first.id}",first.action is Action.Launch)
   assertEquals(q,"fixture.webos",(first.action as Action.Launch).intent.component?.packageName)
  }
 }
 @Test fun normalizedEquivalentNamesHaveStableScoresAndOrder() {
  val a=application("fixture.a","Café Manager");val b=application("fixture.b","Cafe   Manager")
  val cases=listOf("café manager","CAFE MANAGER"," cafe   manager ","café\tmanager").map{q->q to ResultRanker.rank(q,listOf(b,a),c)}
  cases.forEach{(q,ranked)->println("NORMALIZED [$q] ${ranked.map{it.id+":"+it.score}}")}
  for((q,ranked) in cases) {
   assertEquals(listOf(a.id,b.id),ranked.map{it.id});assertEquals(ranked[0].score,ranked[1].score)
  }
 }
 @Test fun explicitMusicIsPreservedAndBareQueenDoesNotInventMusicIntent() {
  install("com.spotify.music","Spotify")
  assertTrue(initial("queen").none{it.action is Action.ComposeSearch || it.category=="Aplicaciones"})
  val musical=initial("música de Queen");assertTrue(musical.any{(it.action as? Action.ComposeSearch)?.query?.equals("queen",ignoreCase=true)==true})
  install("fixture.queen","Queen")
  assertEquals("fixture.queen",(initial("queen").first().action as Action.Launch).intent.component?.packageName)
  assertTrue(initial("música de Queen").first().action is Action.ComposeSearch)
 }
 @Test fun relevantFilesAndContactsBeatUnrelatedAppsAndGenericWebWithoutExecution() {
  val doc=Result("fixture.doc","Queen historia.pdf","Coincidencia local autorizada","Archivos",700,Action.Launch(Intent("fixture:document")),mime="application/pdf")
  val contact=Result("fixture.contact","Queen Pérez","Contacto autorizado","Contactos",650,Action.ComposeMessage("sms","+15550000000"))
  val unrelated=application("fixture.unrelated","WhatsApp",9000)
  val web=Result("web","Buscar Queen en la web","Acción exploratoria","Web",9999,Action.Launch(Intent("fixture:web")))
  val ranked=ResultRanker.rank("queen",listOf(web,unrelated,contact,doc),c);val view=CompactResults.select("queen",ranked,c)
  assertEquals(listOf(contact.id,doc.id),view.initial.map{it.id});assertTrue(view.all.any{it.id==web.id})
  assertEquals(contact.action,view.initial.single{it.id==contact.id}.action)
  println("LOCAL CONTENT initial=${view.initial.map{it.id}} expanded=${view.all.map{it.id}}")
 }
 @Test fun personalizationOrdersPeersButCannotDisplaceExactOrPrefixMatches() {
  val exact=application("fixture.exact","Spotify");val prefix=application("fixture.prefix","Spotify Studio");val partial=application("fixture.partial","Mi Spotify",9000)
  val usage=JSONObject().put(partial.id,JSONObject().put("count",999999).put("time",System.currentTimeMillis()+60000)).toString()
  val config=c.copy(learning=true,favorites=setOf(partial.id),usageData=usage,sourcePriority="{\"Aplicaciones\":150}")
  assertEquals(listOf(exact.id,prefix.id,partial.id),ResultRanker.rank("spotify",listOf(partial,prefix,exact),config).map{it.id})
  val peers=listOf(application("fixture.a","Spotify Alpha"),application("fixture.b","Spotify Beta"))
  assertEquals("fixture.b",ResultRanker.rank("spot",peers,c.copy(favorites=setOf("fixture.b"))).first().id)
  val learned=c.copy(learning=true,usageData=JSONObject().put("fixture.b",JSONObject().put("count",999999).put("time",System.currentTimeMillis()+60000)).toString())
  assertEquals("fixture.b",ResultRanker.rank("spot",peers,learned).first().id)
  assertEquals(ResultRanker.rank("spot",peers,c).map{it.id},ResultRanker.rank("spot",peers,learned.copy(privateMode=true)).map{it.id})
 }
 @Test fun disabledLearningAndPrivateModeIgnoreLearnedRecencyButKeepExplicitFavorites() {
  install("fixture.player.a","Player Alpha");install("fixture.player.b","Player Beta")
  val learned=c.copy(learning=true,recent=listOf("fixture.player.b/Main"))
  assertEquals("Player Beta",initial("playe",learned).first().title)
  assertEquals("Player Alpha",initial("playe",learned.copy(learning=false)).first().title)
  assertEquals("Player Alpha",initial("playe",learned.copy(privateMode=true)).first().title)
  assertEquals("Player Beta",initial("playe",learned.copy(privateMode=true,favorites=setOf("fixture.player.b/Main"))).first().title)
 }
 @Test fun aliasesAndExplicitDeviceContentAndCalculatorQueriesKeepTheirContracts() {
  install("com.spotify.music","Spotify");assertEquals("Spotify",initial("mus",c.copy(aliases=setOf("mus|Spotify"))).first().title)
  assertTrue(initial("volumen 40%").any{it.action is Action.Device && (it.action as Action.Device).command=="volume:40"})
  assertEquals(Action.Copy("4"),initial("2+2").first().action)
  val files=initial("pdf anatomía");assertTrue(files.none{it.category=="Aplicaciones"});assertTrue(files.any{it.action is Action.Access})
 }
 @Test fun deniedProvidersNeverSupplyPrivateContentAndDisabledModulesStayExcluded() {
  install("fixture.juan","Juan App");Shadows.shadowOf(app).denyPermissions(Manifest.permission.READ_CONTACTS)
  val rows=frames("Juan",c.copy(contacts=true)).last();assertTrue(rows.none{it.category=="Contactos" && it.action !is Action.Access});assertTrue(rows.none{it.category=="Archivos" && it.action is Action.Launch})
  val denied=initial("/contacts Juan",c.copy(contacts=true));assertTrue(denied.all{it.action is Action.Access})
  dev.bautista.spotlight.extensions.ExtensionStore.setEnabled(app,"apps",false)
  assertTrue(frames("Juan",c).last().none{it.category=="Aplicaciones"})
 }
 @Test fun selectionIdentityAndExpansionSurviveLateResultsWithoutReinterpretingActions() {
  val old=(1..8).map{application("fixture.$it","Spotify $it")};val selected=old[4]
  val late=application("fixture.exact","Spotify");val merged=StableResults.merge(old,listOf(late)+old.reversed(),true)
  val view=CompactResults.select("spot",merged,c,preserveOrder=true)
  assertEquals(old.map{it.id}+late.id,view.all.map{it.id});assertEquals(selected.id,view.initial[4].id)
  assertEquals(selected.action,view.initial[4].action);assertTrue(view.visible(false,old[7].id).any{it.id==old[7].id})
  assertEquals(9,view.visible(true).size);assertEquals("spotify",CommandCenter.complete("spot",listOf(late)))
  assertTrue(StableResults.merge(old,listOf(late)+old.filterNot{it.id==selected.id},true).none{it.id==selected.id})
 }
 @Test fun longLabelsAccentsAndEveryWordAreRequiredForAppRelevance() {
  install("fixture.long","Café Clínica — Gestor de documentos profesionales y referencias locales")
  assertEquals("fixture.long",(initial("cafe clinica gestor").first().action as Action.Launch).intent.component?.packageName)
  assertEquals(0,relevance("web dev","WhatsApp"));assertEquals(0,relevance("web dev","Web navegador"))
 }
 @Test fun emptyHomeHasNoAlphabeticalFillerAndRespectsPrivacyAndLimits() {
  install("fixture.alpha","Alpha");install("fixture.beta","Beta")
  assertTrue(initial("").isEmpty())
  val favorites=c.copy(favorites=setOf("fixture.alpha/Main","fixture.beta/Main"));val rows=initial("",favorites)
  assertTrue(rows.size<=3);assertEquals(1,rows.count{it.action is Action.Launch})
  val rich=HomeSuggestions.results(app,favorites.copy(learning=true,clipboard=true,actionRecent=listOf("volumen 40%")),false,true)
  assertEquals(3,rich.size);assertEquals(1,rich.count{it.action is Action.Launch})
  val stale=AppConfig(files=false,contacts=false,clipboard=false,usageData="{\"fixture.alpha/Main\":{\"count\":50,\"time\":1}}")
  assertTrue(HomeSuggestions.results(app,stale,false,false,30L*86400000).none{it.action is Action.Launch})
  assertTrue(HomeSuggestions.results(app,stale.copy(privateMode=true),false,true).isEmpty())
 }
 @Test fun homonymousFavoriteHomeAppsDoNotDependOnPackageManagerEnumerationOrder() {
  val z=install("fixture.z","Duplicada");val a=install("fixture.a","Duplicada")
  val config=c.copy(favorites=setOf("fixture.z/Main","fixture.a/Main"))
  val first=HomeSuggestions.results(app,config,false,false).map{it.id}
  val pm=Shadows.shadowOf(app.packageManager);pm.removeResolveInfosForIntent(launcher,"fixture.z");pm.removeResolveInfosForIntent(launcher,"fixture.a")
  pm.addResolveInfoForIntent(launcher,a);pm.addResolveInfoForIntent(launcher,z)
  val reversed=HomeSuggestions.results(app,config,false,false).map{it.id}
  println("HOME enumeration before=$first reversed=$reversed")
  assertEquals(listOf("suggest:app:fixture.a"),first);assertEquals(first,reversed)
 }
}
