package dev.bautista.spotlight.search

import android.app.Application
import android.content.Intent
import android.content.pm.*
import dev.bautista.spotlight.data.*
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.index.FileIndex
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[26,35],application=Application::class)
class UnifiedRootRegressionTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Default)
    @After fun cleanup(){scope.cancel()}
    private fun engine():SearchEngine {val p=Preferences(context);return SearchEngine(context,p,LocalStore(p,scope),FileIndex(context))}
    @Test fun installedSpotifyIsFirstInEveryRootSnapshot()=runBlocking {
        val app=ApplicationInfo().apply{packageName="com.spotify.music";nonLocalizedLabel="Spotify"}
        val activity=ActivityInfo().apply{packageName=app.packageName;name="Main";applicationInfo=app;nonLocalizedLabel="Spotify";exported=true}
        Shadows.shadowOf(context.packageManager).installPackage(PackageInfo().apply{packageName=app.packageName;applicationInfo=app;activities=arrayOf(activity)})
        val resolve=ResolveInfo().apply{activityInfo=activity}
        Shadows.shadowOf(context.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),resolve)
        Shadows.shadowOf(context.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(app.packageName),resolve)
        val snapshots=engine().search("Spotify",AppConfig(files=false)).toList()
        assertTrue(snapshots.isNotEmpty());snapshots.forEach{assertEquals("Spotify",it.first().title);assertTrue(it.first().action is Action.Launch);assertEquals(it.map{r->r.id}.distinct().size,it.size)}
    }
    @Test fun dateFilesExplainPermissionInsteadOfSearchingForYesterday()=runBlocking {
        val rows=engine().search("Buscar PDF de ayer",AppConfig(files=false)).toList().last()
        assertTrue(rows.single().action is Action.Access);assertTrue(rows.single().subtitle.contains("acceso"))
    }
    @Test fun rootKnowledgeConsentIsExplicitAndPlainSearchHasNoAiFiller()=runBlocking {
        val e=engine()
        val pending=e.search("Explicame el VOR",AppConfig(files=false)).toList().last().first().action as Action.Ask
        assertEquals(AiInputOrigin.USER_QUERY,pending.origin);assertFalse(AiRequestPolicy.requiresReview(pending))
        val approved=e.search("Explicame el VOR",AppConfig(files=false,aiRootConsent=true)).toList().last().first().action as Action.Ask
        assertFalse(approved.sensitive)
        assertTrue(e.search("inexistente palabras largas de prueba sin comando",AppConfig(files=false)).toList().last().none{it.action is Action.Ask})
    }
}
