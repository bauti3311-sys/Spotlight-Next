package dev.bautista.spotlight.search

import android.app.Application
import android.content.Intent
import android.content.pm.*
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.data.*
import dev.bautista.spotlight.index.FileIndex
import dev.bautista.spotlight.next.IntentRouter
import dev.bautista.spotlight.ui.AppearancePresets
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class HybridSearchTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Default)
    @After fun cleanup(){scope.cancel()}
    private fun engine()=SearchEngine(context,Preferences(context),LocalStore(Preferences(context),scope),FileIndex(context))
    private fun installMusic() {
        val info=ApplicationInfo().apply{packageName="fixture.music";category=ApplicationInfo.CATEGORY_AUDIO;nonLocalizedLabel="Música local"}
        val activity=ActivityInfo().apply{packageName=info.packageName;name="fixture.music.Main";applicationInfo=info;nonLocalizedLabel="Música local";exported=true}
        Shadows.shadowOf(context.packageManager).installPackage(PackageInfo().apply{packageName=info.packageName;applicationInfo=info;activities=arrayOf(activity)})
        Shadows.shadowOf(context.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=activity})
        Shadows.shadowOf(context.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(info.packageName),ResolveInfo().apply{activityInfo=activity})
    }
    @Test fun musicUsesInstalledAudioCategoryAndRealLaunchRatherThanAutoplay()=runBlocking {
        installMusic()
        val rows=engine().search("escuchar musica",AppConfig(files=false)).toList().last()
        val music=rows.single{it.title=="Música local"}
        assertEquals("fixture.music",(music.action as Action.Launch).intent.component!!.packageName)
        assertTrue(rows.any{it.action is Action.AudioPanel})
        assertTrue(rows.none{it.action is Action.ComposeSearch})
    }
    @Test fun noMusicAppOffersSessionControlsWithoutFakeLaunch()=runBlocking {
        val rows=engine().search("quiero escuchar música",AppConfig(files=false)).toList().last()
        assertEquals(1,rows.size)
        assertTrue(rows.single().action is Action.AudioPanel)
    }
    @Test fun compoundAndContextualQueriesOfferReviewWithoutExecuting()=runBlocking {
        val e=engine()
        listOf("bajar el brillo al 35% y abrir Spotify","quiero estudiar sin distracciones","explicame este PDF","traducí este texto").forEach { query->
            val rows=e.search(query,AppConfig(files=false)).toList().last()
            assertTrue(rows.any{it.action==Action.SystemPanel("interpret",query) || it.action==Action.SystemPanel("context-ai",query)})
        }
        assertEquals(Action.ComposeMessage("whatsapp","Juan"),IntentRouter.route("mandarle un WhatsApp a Juan"))
        assertEquals(Action.SystemPanel("display-review","screen_brightness:89"),IntentRouter.route("bajar el brillo al 35%"))
    }
    @Test fun presentationKeepsSecondExactMatchAboveUnrelatedRowsInTopCategory() {
        fun row(id:String,category:String,score:Int)=Result(id,id,"",category,score,Action.Copy(id))
        val ranked=ResultSections.grouped(listOf(row("best","Aplicaciones",5000),row("weak","Aplicaciones",100),row("exact-contact","Contactos",4900)),query="Juan")
        assertEquals(listOf("best","exact-contact","weak"),ranked.map{it.id})
    }
    @Test fun hybridPresetAndPortableConfigKeepUserChoicesAndPrivateState() {
        val c=AppConfig(theme="dark",accent="rose",density="spacious",textScale=1.1f,favorites=setOf("fixture/app"),trees=setOf("content://private"),pins=setOf("query"),learning=false)
        val hybrid=AppearancePresets.apply("Spotlight Hybrid",c)
        assertEquals("auto",hybrid.theme)
        assertEquals(c.accent,hybrid.accent);assertEquals(c.density,hybrid.density);assertEquals(c.textScale,hybrid.textScale)
        assertEquals(c.favorites,hybrid.favorites);assertEquals(c.trees,hybrid.trees);assertFalse(hybrid.learning)
        assertEquals("hybrid",ConfigTransfer.decode(ConfigTransfer.encode(hybrid),hybrid).preset)
        assertEquals("glass",AppConfig().preset)
    }
    @Test fun learningControlsRecentSuggestionsAndHiddenPinnedStillApply() {
        installMusic()
        val recent=AppConfig(files=false,recent=listOf("fixture.music/fixture.music.Main"),actionRecent=listOf("volumen al 60%"))
        val rows=HomeSuggestions.results(context,recent,false,false)
        assertTrue(rows.any{it.id=="suggest:app:fixture.music"})
        assertTrue(rows.any{it.action==Action.Device("volume:60")})
        assertTrue(rows.size<=4)
        val private=HomeSuggestions.results(context,recent.copy(learning=false),false,false)
        assertTrue(private.none{it.subtitle.contains("reciente") || it.subtitle.contains("frecuente") || it.id.startsWith("suggest:action:")})
        assertTrue(private.any{it.id=="suggest:app:fixture.music" && it.subtitle=="Aplicación instalada"})
        val hidden=HomeSuggestions.results(context,recent.copy(hiddenSuggestions=setOf("suggest:app:fixture.music"),pinnedSuggestions=setOf("suggest:commands")),false,false)
        assertTrue(hidden.none{it.id=="suggest:commands"})
        assertTrue(hidden.none{it.id=="suggest:app:fixture.music"})
    }
    @Test fun homePinPrioritySurvivesPresentationAndDuplicateRecentQueriesStaySafe() {
        val config=AppConfig(files=false,actionRecent=listOf("volumen al 60%","VOLUMEN AL 60%"),pinnedSuggestions=setOf("suggest:commands"))
        val rows=HomeSuggestions.results(context,config,false,false)
        assertEquals(rows.size,rows.distinctBy{it.id}.size)
        assertTrue(ResultSections.grouped(rows,expanded=true).any{it.action==Action.Device("volume:60")})
    }

}
