package dev.bautista.spotlight.search

import android.content.*
import android.content.pm.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.CommandCenter
import dev.bautista.spotlight.data.Config as AppConfig
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=SpotlightApp::class)
class SmartSearchAcceptanceTest {
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private val config=AppConfig(files=false,contacts=false,clipboard=false,learning=false)
    private fun install(pkg:String,title:String) {
        val a=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=title}
        val activity=ActivityInfo().apply{packageName=pkg;name="Main";nonLocalizedLabel=title;applicationInfo=a;exported=true}
        Shadows.shadowOf(app.packageManager).installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=a;activities=arrayOf(activity)})
        Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=activity})
        // Real PackageManager also resolves the package-scoped launcher used by capability checks.
        Shadows.shadowOf(app.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(pkg),ResolveInfo().apply{activityInfo=activity})
        app.search.invalidateApps()
    }
    private fun rows(q:String,c:AppConfig=config)=runBlocking{app.search.search(q,c).last()}
    private fun row(id:String,title:String,category:String="Aplicaciones",score:Int=100)=Result(id,title,"",category,score,
        if(category=="Aplicaciones")Action.Launch(Intent(Intent.ACTION_MAIN).setComponent(ComponentName(id,"Main"))) else Action.Device("volume_down"))
    @Test fun partialInstalledAppsAreFirstWithoutRemoteAi() {
        install("com.spotify.music","Spotify");install("com.whatsapp","WhatsApp");install("webos.fixture","webOS Dev Manager")
        for((q,title) in listOf("spot" to "Spotify","whats" to "WhatsApp","webOS Dev Manager" to "webOS Dev Manager")) {
            val all=rows(q);assertEquals(q,title,CompactResults.select(q,all,config).initial.first().title)
            assertTrue(all.none{it.action is Action.Ask && it.id.startsWith("universal:")})
        }
    }
    @Test fun exactCommandsBeatApplicationPrefixesEvenWithExtremeHistory() {
        val command=row("cmd:volume","Volumen","Comandos",1)
        val app=row("volume.fixture","Volumen Player",score=9000)
        assertEquals(command.id,ResultRanker.rank("volumen",listOf(app,command),config).first().id)
        assertEquals(command.id,CompactResults.select("volumen",ResultRanker.rank("volumen",listOf(app,command),config),config).initial.first().id)
    }
    @Test fun relevantPartialApplicationBeatsGenericContextActionsInCompactView() {
        val partial=row("media.fixture","Mi Spotify")
        val generic=row("device:volume_down","Bajar volumen","Comandos",9000)
        val ranked=ResultRanker.rank("spotify",listOf(generic,partial),config)
        assertEquals(partial.id,CompactResults.select("spotify",ranked,config).initial.first().id)
    }
    @Test fun typoStillOffersTheMatchingInstalledApplication() {
        install("com.spotify.music","Spotify")
        assertEquals("Spotify",CompactResults.select("spotfy",rows("spotfy"),config).initial.first().title)
    }
    @Test fun aliasesKeepTheTargetApplicationEligibleAndAccentsAreNormalized() {
        install("com.spotify.music","Spotify");install("anatomia.fixture","Anatomía")
        val c=config.copy(aliases=setOf("mus|Spotify"))
        assertEquals("Spotify",CompactResults.select("mus",rows("mus",c),c).initial.first().title)
        assertEquals("Anatomía",CompactResults.select("ANATOMIA",rows("ANATOMIA"),config).initial.first().title)
    }
    @Test fun completionRequiresAnUnambiguousLiveCandidate() {
        assertNull(CommandCenter.complete("vol",emptyList()))
        assertNull(CommandCenter.complete("spot",listOf(row("a","Spotify"),row("b","Spotlight"))))
        assertEquals("spotify",CommandCenter.complete("spot",listOf(row("a","Spotify"))))
        assertNull(CommandCenter.complete("Spotify",listOf(row("a","Spotify"))))
    }
    @Test fun blankHomeDoesNotInventApplicationsOrWhatsAppContent() {
        install("com.spotify.music","Spotify");install("com.whatsapp","WhatsApp")
        val clean=rows("");assertTrue(clean.none{it.id=="suggest:replies"});assertTrue(clean.none{it.action is Action.Launch})
        val c=config.copy(favorites=setOf("com.spotify.music/Main","com.whatsapp/Main"))
        assertTrue(rows("",c).count{it.action is Action.Launch}<=1)
    }
    @Test fun selectedOrderSurvivesAsyncPeersButRemovedResultsDisappear() {
        val a=row("a","Spotify");val b=row("b","Spotlight");val c=row("c","Spotcast")
        assertEquals(listOf("a","b","c"),StableResults.merge(listOf(a,b),listOf(c,b,a),true).map{it.id})
        assertEquals(listOf("b","c"),StableResults.merge(listOf(a,b),listOf(c,b),true).map{it.id})
    }
    @Test fun absentAppsAndEmptyQueriesDoNotInventExecutions() {
        val missing=rows("Spotify")
        assertTrue(missing.none{it.category=="Aplicaciones"})
        val unknown=rows("zqvxyz")
        assertTrue(CompactResults.select("zqvxyz",unknown,config).initial.isEmpty())
        assertTrue(unknown.none{it.action is Action.Device || it.action is Action.Ask})
    }
    @Test fun volumePrefixesDoNotDisplaceAnInstalledApplicationPrefix() {
        install("volcano.fixture","Volcano")
        val all=rows("vol")
        assertEquals("Volcano",CompactResults.select("vol",all,config).initial.first().title)
        assertTrue(all.any{it.action is Action.Device && it.action.command in setOf("volume_up","volume_down")})
    }
    @Test fun explicitOpenSpotifyResolvesLocallyAndNeverAsAnAiProposal() {
        install("com.spotify.music","Spotify")
        val initial=CompactResults.select("abrir Spotify",rows("abrir Spotify"),config).initial
        val primary=initial.first().action as Action.Launch
        assertEquals("com.spotify.music",primary.intent.component?.packageName)
    }
    @Test fun deniedContactsNeverBecomeAuthorizedResults() {
        Shadows.shadowOf(app).denyPermissions(android.Manifest.permission.READ_CONTACTS)
        assertTrue(rows("Juan",config.copy(contacts=true)).none{it.category=="Contactos" && it.action !is Action.Access})
    }
    @Test fun configurationWarningsNeverOutrankALiveApplicationPrefix() {
        val application=row("com.whatsapp","WhatsApp")
        val warning=row("warning","WhatsApp no está instalado","Comandos",9000).copy(action=Action.SystemPanel("extensions"))
        val ranked=ResultRanker.rank("whats",listOf(warning,application),config)
        assertEquals(application.id,ranked.first().id)
        assertEquals(application.id,CompactResults.select("whats",ranked,config).initial.first().id)
    }
}
