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

/** Real production adapters and eligibility; package manager is a declared Android fixture. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class CompactProductionTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Default)
    private val config=AppConfig(files=false)
    @After fun cleanup(){scope.cancel()}
    private fun engine():SearchEngine {val p=Preferences(context);return SearchEngine(context,p,LocalStore(p,scope),FileIndex(context))}
    private fun install(pkg:String,label:String){
        val app=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=label}
        val activity=ActivityInfo().apply{packageName=pkg;name="Main";applicationInfo=app;nonLocalizedLabel=label;exported=true}
        Shadows.shadowOf(context.packageManager).installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=app;activities=arrayOf(activity)})
        val info=ResolveInfo().apply{activityInfo=activity}
        Shadows.shadowOf(context.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),info)
        Shadows.shadowOf(context.packageManager).addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(pkg),info)
    }
    @Test fun everyWhatsAppSnapshotKeepsExactAppWithoutAutomaticDefinitions()=runBlocking {
        install("com.whatsapp","WhatsApp")
        engine().search("WhatsApp",config).toList().forEach{rows->
            val view=CompactResults.select("WhatsApp",rows,config)
            assertEquals("WhatsApp",view.initial.first().title)
            assertTrue(view.initial.size<=3)
            assertTrue(view.initial.none{it.id=="concept-definition" || it.category=="Web"})
            assertEquals(StableResults.unique(rows).size,view.all.size)
        }
    }
    @Test fun naturalActionsAndExplicitAiComeFromProductionWithoutExecuting()=runBlocking {
        install("com.whatsapp","WhatsApp");install("com.spotify.music","Spotify")
        val engine=engine()
        val cases=listOf("brillo 40%","música de Cerati","explicame el reflejo vestibuloocular","mandar WhatsApp a Juan")
        cases.forEach{query->
            val rows=engine.search(query,config).toList().last()
            val initial=CompactResults.select(query,rows,config).initial
            assertTrue("Production must provide a relevant action for $query: $rows",initial.isNotEmpty())
            assertTrue(initial.size<=3)
            when(query){
                cases[0]->assertTrue(initial.any{it.action is Action.SystemPanel || it.action is Action.Device})
                cases[1]->assertTrue(initial.any{it.action is Action.ComposeSearch})
                cases[2]->assertTrue(initial.first().action is Action.Ask)
                cases[3]->assertTrue(initial.any{it.action is Action.ComposeMessage})
            }
        }
        assertNull(Shadows.shadowOf(context).nextStartedActivity)
    }
    @Test fun pdfPermissionRequirementRemainsVisibleWithNoStorageGrant()=runBlocking {
        val query="PDF anatomía"
        val rows=engine().search(query,config).toList().last()
        val initial=CompactResults.select(query,rows,config).initial
        assertTrue(initial.any{it.action is Action.Access})
    }
}
