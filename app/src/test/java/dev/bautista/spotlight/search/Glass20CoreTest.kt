package dev.bautista.spotlight.search

import android.app.Application
import dev.bautista.spotlight.data.*
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.next.*
import dev.bautista.spotlight.ui.SettingsGroups
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[26,35],application=Application::class)
class Glass20CoreTest {
 private val context get()=RuntimeEnvironment.getApplication()
 @Test fun spanishWordsAndRelativeActionsStayTyped(){assertEquals("bajar brillo al 35%",LocalLanguage.canonical("Bajá la pantalla al treinta y cinco por ciento"));assertEquals(PlanStep.RelativeVolume(-20),LocalPlan.step("bajá el volumen en veinte%"));assertEquals(PlanStep.Media("pause"),LocalPlan.step("pausá música"));assertEquals(3,LocalPlan.parse("brillo al 40%, volumen al 20% y abrir Spotify")!!.steps.size)}
 @Test fun malformedPlansCannotExecute(){assertNull(LocalPlan.parse("brillo al 200% y abrir Spotify"));assertNull(LocalPlan.step("volumen -10%"));assertNull(LocalPlan.parse("abrir Spotify y borrar todos los datos"))}
 @Test fun originalTextAndAccentsSurviveSearch(){assertEquals("José Pérez",LocalLanguage.forSearch("José Pérez"));val rows=RootActions.results(context,"Mandale a Juan que Llegaré a las 18:30",AppConfig(messageApp="sms"));val a=rows.first{it.action is Action.ComposeMessage}.action as Action.ComposeMessage;assertEquals("Llegaré a las 18:30",a.text)}
 @Test fun emptyHomeHasNoFiller(){assertTrue(HomeSuggestions.results(context,AppConfig(files=false),false,false).isEmpty());assertEquals(6,SettingsGroups.names.size)}
 @Test fun preciseResultWinsOverHabitAndLargeFileBatch(){val exact=Result("app","Spotify","","Aplicaciones",1000,Action.Copy("app"));val files=(0..80).map{Result("f$it","Spotify audio $it","","Archivos",150,Action.Copy("$it"))};val ranked=ResultRanker.rank("Spotify",files+exact,AppConfig(usageData=JSONObject().put("f0",JSONObject().put("count",999999)).toString()));assertEquals("app",ranked.first().id);assertTrue(ResultSections.unified(ranked).count{it.category=="Archivos"}<=2)}
 @Test fun lateResultsPreserveUserNavigation(){val a=Result("a","A","","Acciones",1,Action.Copy("a"));val b=a.copy(id="b",title="B",score=500);assertEquals(listOf("a","b"),StableResults.merge(listOf(a),listOf(b,a),true).map{it.id})}
 @Test fun visualExportExcludesUserDataAndRejectsSecrets(){val c=AppConfig(endpoint="PRIVATE-ENDPOINT",trees=setOf("content://private"),privateMode=true);val raw=ThemeTransfer.encode(c);assertFalse(raw.contains("PRIVATE-ENDPOINT"));assertFalse(raw.contains("private"));assertTrue(ThemeTransfer.decode(raw,c).privateMode);val invalid=JSONObject(raw);invalid.getJSONObject("visual").put("apiKey","secret");assertTrue(runCatching{ThemeTransfer.decode(invalid.toString(),c)}.isFailure)}
 @Test fun privateModeStopsLearningWithoutDestroyingSavedState()=runBlocking {val p=Preferences(context);p.update(AppConfig(privateMode=true,favorites=setOf("keep"),routines="[]",actionRecent=listOf("old")));p.recordAction("new");p.record("new/app");val c=p.config.first();assertTrue(c.privateMode);assertEquals(listOf("old"),c.actionRecent);assertTrue(c.recent.isEmpty());p.clearHistory();val next=p.config.first();assertTrue(next.privateMode);assertEquals(setOf("keep"),next.favorites)}
 @Test fun temporaryContextExpiresWhilePinnedTextPersists()=runBlocking {val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO);try{val store=ContextStore(context,scope,Preferences(context));val temporary=store.setText("temporary");store.expire(temporary.created+600000);assertNull(store.active.value);store.setText("saved");store.pinActive();store.expire(System.currentTimeMillis()+600001);assertEquals("saved",store.active.value!!.text);store.removeContext(store.active.value!!.id);assertTrue(store.pinned.value.isEmpty())}finally{scope.cancel()}}
 @Test fun sportsNeverIncludesAnotherTeamsEvents(){val e=SportsEntity("133602","Team","team","Soccer");assertFalse(SportsFavorites.matches(e,JSONObject().put("idHomeTeam","999")));assertTrue(SportsFavorites.matches(e,JSONObject().put("idAwayTeam","133602")));assertEquals("Marcador no informado",ProviderValues.score(JSONObject()));assertFalse(SportsFavorites.valid(e.copy(id="not-id")))}
}
