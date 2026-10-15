package dev.bautista.spotlight.search

import android.app.Application
import dev.bautista.spotlight.data.Config as SearchConfig
import dev.bautista.spotlight.data.Preferences
import dev.bautista.spotlight.data.LocalStore
import dev.bautista.spotlight.index.FileIndex
import dev.bautista.spotlight.next.IntentRouter
import kotlinx.coroutines.*
import org.junit.After
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.toList
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class UniversalSearchTest {
    private val localScope=CoroutineScope(SupervisorJob()+Dispatchers.Default)
    @After fun cleanup() { localScope.cancel() }
    private fun engine(): SearchEngine {
        val context=RuntimeEnvironment.getApplication()
        return SearchEngine(context,Preferences(context),LocalStore(Preferences(context),localScope),FileIndex(context))
    }
    private fun row(id:String,title:String,category:String,score:Int)=Result(id,title,"",category,score,Action.Copy(title))

    @Test fun understandsExamplesAndPreservesInformationalText() {
        assertEquals(UniversalIntent.OpenApp("Spotify"),UniversalIntent.recognize("abrir Spotify"))
        assertEquals(UniversalIntent.Volume(60),UniversalIntent.recognize("volumen al 60%"))
        assertEquals(UniversalIntent.Photos("kind:image vacaciones"),UniversalIntent.recognize("buscar fotos de vacaciones"))
        assertEquals(UniversalIntent.Information("rehabilitación vestibular"),UniversalIntent.recognize("buscar información sobre rehabilitación vestibular"))
        assertEquals(Action.SystemPanel("display-review","screen_brightness:102"),IntentRouter.route("brillo al 40%"))
        assertEquals(Action.ComposeMessage("whatsapp","Juan"),IntentRouter.route("mandar WhatsApp a Juan"))
    }
    @Test fun acceptsSpacingAccentsAndPercentageBoundaries() {
        assertEquals(UniversalIntent.Volume(0),UniversalIntent.recognize("  AJUSTAR   el volumen a 0 %  "))
        assertEquals(UniversalIntent.Volume(100),UniversalIntent.recognize("poner volumen 100"))
        assertEquals(UniversalIntent.InvalidLevel("volumen"),UniversalIntent.recognize("volumen al 101%"))
        assertEquals(UniversalIntent.InvalidLevel("brillo"),UniversalIntent.recognize("brillo -1%"))
        assertNull(UniversalIntent.recognize("volumen al"))
        assertNull(UniversalIntent.recognize("Spotify"))
        assertNull(UniversalIntent.recognize("x".repeat(4001)))
    }
    @Test fun photoDatesUseExistingParser() {
        assertEquals(UniversalIntent.Photos(SearchQuery.natural("fotos de ayer")),UniversalIntent.recognize("buscar fotos de ayer"))
    }
    @Test fun engineProducesExecutableVolumeAndBrightnessActions()=runBlocking {
        assertEquals(Action.Device("volume:60"),engine().search("volumen al 60%",SearchConfig()).toList().last().single().action)
        val brightness=engine().search("brillo al 40%",SearchConfig()).toList().last().single()
        assertTrue(brightness.subtitle.contains("Shizuku"))
        assertEquals(Action.SystemPanel("display-review","screen_brightness:102"),brightness.action)
    }
    @Test fun systemSettingsRemainExecutableRatherThanBecomingAppSearch()=runBlocking {
        val rows=engine().search("Abrir ajustes de sonido",SearchConfig(files=false)).toList().last()
        assertEquals(android.provider.Settings.ACTION_SOUND_SETTINGS,(rows.single().action as Action.Launch).intent.action)
    }
    @Test fun disabledPhotoProviderExplainsAccessInsteadOfInventingMatches()=runBlocking {
        val row=engine().search("buscar fotos de vacaciones",SearchConfig(files=false)).toList().last().single()
        assertEquals("files",(row.action as Action.Access).access)
        assertTrue(row.subtitle.contains("acceso"))
    }
    @Test fun informationCombinesLocalWebAndExplicitAiWithoutRemoteCalls()=runBlocking {
        val rows=engine().search("buscar información sobre rehabilitación vestibular",SearchConfig(files=false,contacts=false)).toList().last()
        val ai=rows.single { it.action is Action.Ask }
        assertEquals("rehabilitación vestibular",(ai.action as Action.Ask).prompt)
        assertTrue(rows.any { it.id=="smart:web" && it.action is Action.Launch })
        assertTrue(ai.subtitle.contains("Solo al seleccionar"))
        assertTrue((ai.action as Action.Ask).sensitive)
    }
    @Test fun installedAppLaunchesFromUniversalBarAndKeepsStableIdentity()=runBlocking {
        val context=RuntimeEnvironment.getApplication()
        val launcher=context.packageManager.getLaunchIntentForPackage(context.packageName)!!
        val label=context.applicationInfo.loadLabel(context.packageManager).toString()
        val rows=engine().search("abrir $label",SearchConfig(files=false)).toList().last()
        val first=rows.first()
        assertEquals("${launcher.component!!.packageName}/${launcher.component!!.className}",first.id)
        assertEquals(launcher.component,(first.action as Action.Launch).intent.component)
        assertTrue(rows.any { it.category=="Web" })
    }
    @Test fun sameIdentityKeepsHighestRelevanceRegardlessOfProviderArrival() {
        val exact=row("app","Spotify","Aplicaciones",1000)
        val menu=row("app","Abrir Spotify","Aplicaciones",2400)
        assertEquals("Spotify",ResultRanker.rank("Spotify",listOf(menu,exact),SearchConfig()).single().title)
        assertEquals("Spotify",ResultRanker.rank("Spotify",listOf(exact,menu),SearchConfig()).single().title)
    }
    @Test fun missingAppReturnsWebFallbackRatherThanFakeLaunch()=runBlocking {
        val rows=engine().search("abrir ImpossibleSpotlightTestApp",SearchConfig(files=false)).toList().last()
        assertEquals("Web",rows.single().category)
    }
    @Test fun exactResultsBeatFuzzyFavoritesAndProviderBatchSize() {
        val app=row("app","Spotify","Aplicaciones",1000)
        val files=(1..80).map { row("file:$it","Spotify archivo $it","Archivos",800) }
        val fuzzy=row("fuzzy","Spotif","Aplicaciones",250)
        val ranked=ResultRanker.rank("Spotify",files+fuzzy+app+app,SearchConfig(favorites=setOf("fuzzy")))
        assertEquals("app",ranked.first().id)
        assertEquals(82,ranked.size)
        assertEquals("app",ResultSections.grouped(ranked,query="Spotify").first().id)
    }
    @Test fun relevantActionSectionPrecedesUnrelatedAppAndTiesAreStable() {
        val rows=listOf(row("app","Otra app","Aplicaciones",800),row("action","Volumen al 60%","Acciones",2600))
        assertEquals("action",ResultSections.grouped(rows,query="volumen al 60%").first().id)
        assertEquals(listOf("a","b"),ResultRanker.rank("",listOf(row("b","B","Notas",500),row("a","A","Notas",500)),SearchConfig()).map{it.id})
    }
    @Test fun exactAndPrefixInstalledAppBeatGenericCommandsRegardlessOfLearning() {
        val exact=Result("org.webos/Main","webOS Dev Manager","","Aplicaciones",1000,Action.Launch(android.content.Intent().setComponent(android.content.ComponentName("org.webos","Main"))))
        val prefix=exact.copy(id="org.webos/tools",title="webOS Dev Manager Tools",score=800)
        val wifi=row("wifi","Ajustes Wi-Fi","Comandos",4000)
        val whatsapp=exact.copy(id="com.whatsapp/Main",title="WhatsApp",score=2500)
        val c=SearchConfig(favorites=setOf("wifi","com.whatsapp/Main"),usageData="{\"wifi\":{\"count\":99999}}")
        val ranked=ResultRanker.rank("webOS Dev Manager",listOf(wifi,whatsapp,prefix,exact),c)
        assertEquals(listOf(exact.id,prefix.id),ranked.take(2).map{it.id})
    }

    @Test fun calculatedValueRemainsPrimaryAheadOfWebFallback()=runBlocking {
        val rows=engine().search("2+2",SearchConfig(files=false,contacts=false)).toList().last()
        assertEquals(Action.Copy("4"),rows.first().action)
        assertTrue(rows.any{it.category=="Web"})
    }

    @Test fun naturalQuestionExamplesOfferOnlyExplicitAiAndContextReview()=runBlocking {
        for(query in listOf("Explicame el reflejo vestibuloocular","Qué significa esta palabra","Ayudame a redactar un mensaje","Compará estas dos opciones")) {
            val rows=engine().search(query,SearchConfig(files=false,contacts=false)).toList().last()
            val action=rows.map{it.action}.filterIsInstance<Action.Ask>().first()
            assertEquals(query,action.prompt);assertTrue(action.sensitive)
        }
        val document=engine().search("Resumí este documento",SearchConfig(files=false,contacts=false)).toList().last()
        assertTrue(document.any{it.action is Action.SystemPanel && (it.action as Action.SystemPanel).page in setOf("context-ai","interpret")})
        assertTrue(document.none{it.action is Action.Ask})
        assertFalse(LocalLanguage.isQuestion("Comparador de precios"))
    }

    @Test fun multiwordAppQueriesRequireEveryTokenRatherThanAnUnrelatedSharedWord() {
        assertEquals(620,relevance("web dev", "webOS Dev Manager"))
        assertEquals(1000,relevance("webOS Dev Manager", "webOS Dev Manager"))
        assertEquals(0,relevance("web dev", "WhatsApp"))
        assertEquals(0,relevance("web dev", "Web navegador"))
    }

}
