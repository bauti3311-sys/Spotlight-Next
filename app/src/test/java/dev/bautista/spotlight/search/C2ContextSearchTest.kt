package dev.bautista.spotlight.search

import android.content.*
import android.content.pm.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.*
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
class C2ContextSearchTest {
    private val app get()=RuntimeEnvironment.getApplication() as SpotlightApp
    private val config=AppConfig(files=false,contacts=false,online=false,learning=false)
    private fun install(pkg:String="org.wikipedia",title:String="Wikipedia",search:Boolean=true,web:Boolean=false) {
        val info=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=title;enabled=true}
        val main=ActivityInfo().apply{packageName=pkg;name="Main";nonLocalizedLabel=title;applicationInfo=info;enabled=true;exported=true}
        val handler=ActivityInfo().apply{packageName=pkg;name="$pkg.Search";applicationInfo=info;enabled=true;exported=true}
        val pm=Shadows.shadowOf(app.packageManager)
        pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=info;activities=arrayOf(main,handler)})
        pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=main})
        pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(pkg),ResolveInfo().apply{activityInfo=main})
        if(search)pm.addResolveInfoForIntent(Intent(Intent.ACTION_SEARCH).setPackage(pkg),ResolveInfo().apply{activityInfo=handler})
        if(web)pm.addResolveInfoForIntent(Intent(Intent.ACTION_WEB_SEARCH).setPackage(pkg),ResolveInfo().apply{activityInfo=handler})
        app.search.invalidateApps()
    }
    private fun rows(q:String)=runBlocking{app.search.search(q,config).last()}
    @Test fun appAndTermsPrepareTheExistingPublicSearchWithoutLaunching() {
        install()
        val result=rows("Wikipedia luna azul").single{it.id=="context:app-search:org.wikipedia/Main"}
        val action=result.action as Action.ComposeSearch
        assertEquals("org.wikipedia",action.packageName);assertEquals("luna azul",action.query)
        assertEquals("app",action.topic)
        assertTrue(CompactResults.select("Wikipedia luna azul",rows("Wikipedia luna azul"),config).initial.any{it.id==result.id})
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun exactInstalledApplicationWinsSameNamedHighScoreAction() {
        val app=Result("app","Atlas","","Aplicaciones",1,Action.Launch(Intent(Intent.ACTION_MAIN).setComponent(ComponentName("fixture.atlas","Main"))))
        val action=Result("action","Atlas","","Acciones",1000000,Action.Device("volume_down"))
        val ranked=ResultRanker.rank("Atlas",listOf(action,app),config)
        assertEquals("app",ranked.first().id)
        assertEquals("app",CompactResults.select("Atlas",ranked,config).initial.first().id)
    }
    @Test fun absentSearchDoesNotInventContextualCapability() {
        install(search=false)
        assertTrue(rows("Wikipedia luna").none{it.id.startsWith("context:app-search:")})
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }    @Test fun longestApplicationNameWinsAndAccentsInTermsArePreserved() {
        install();install("fixture.wikipedia.lite","Wikipedia Lite")
        val rows=rows("Wikipedia Lite café de Córdoba").filter{it.id.startsWith("context:app-search:")}
        assertEquals(1,rows.size)
        val draft=rows.single().action as Action.ComposeSearch
        assertEquals("fixture.wikipedia.lite",draft.packageName)
        assertEquals("café de Córdoba",draft.query)
    }
    @Test fun homonymousPackagesRequireSelectionAndExactQueryStaysApplicationFirst() {
        install();install("fixture.otherwiki","Wikipedia")
        assertTrue(rows("Wikipedia luna").none{it.id.startsWith("context:app-search:")})
        assertEquals("Aplicaciones",rows("Wikipedia").first().category)
    }
    @Test fun disabledModulesCannotBeBypassedByContextualRoute() {
        install()
        val store=dev.bautista.spotlight.extensions.ExtensionStore
        try {
            store.setEnabled(app,"apps",false)
            assertTrue(rows("Wikipedia luna").none{it.id.startsWith("context:app-search:")})
            store.setEnabled(app,"apps",true);store.setEnabled(app,"web",false)
            assertTrue(rows("Wikipedia luna").none{it.id.startsWith("context:app-search:")})
        }finally{store.setEnabled(app,"apps",true);store.setEnabled(app,"web",true)}
    }

    @Test fun explicitPrefixAndSuffixPreserveTermsWithoutWebFallbackResults() {
        install()
        for(q in listOf("buscar en Wikipedia: café en Córdoba","buscar café en Córdoba en Wikipedia")) {
            val results=rows(q)
            val row=results.single{it.id.startsWith("context:app-search:")}
            assertEquals("café en Córdoba",(row.action as Action.ComposeSearch).query)
            assertTrue(CompactResults.select(q,results,config).initial.any{it.id==row.id})
        }
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }
    @Test fun genericWebSearchParticipatesWithoutInventingInternalSearch() {
        install("fixture.webatlas","Web Atlas",search=false,web=true)
        val draft=rows("Web Atlas órbita").single{it.id.startsWith("context:app-search:")}.action as Action.ComposeSearch
        assertEquals("app-web-search",draft.topic)
        assertEquals("órbita",draft.query)
    }
    @Test fun exactInstalledLabelCannotBeInterpretedAsACommand() {
        install();install("fixture.literal","buscar en Wikipedia")
        assertEquals("fixture.literal/Main",rows("buscar en Wikipedia").first().id)
        assertTrue(rows("buscar en Wikipedia").none{it.id.startsWith("context:app-search:")})
    }
    @Test fun explicitFormsStillRequireUnambiguousAvailableAppAndTerms() {
        install(search=false)
        assertTrue(rows("buscar en Wikipedia: luna").none{it.id.startsWith("context:app-search:")})
        install();install("fixture.homonym","Wikipedia")
        assertTrue(rows("buscar luna en Wikipedia").none{it.id.startsWith("context:app-search:")})
        assertTrue(rows("buscar en Wikipedia:").none{it.id.startsWith("context:app-search:")})
        assertTrue(rows("buscar en Wikip: luna").none{it.id.startsWith("context:app-search:")})
    }

    @Test fun shareQueryRequiresPublicTextHandlerAndNeverCopiesOrLaunches() {
        install()
        assertTrue(rows("compartir en Wikipedia").none{it.id.startsWith("context:app-share:")})
        val pm=Shadows.shadowOf(app.packageManager)
        val handler=ActivityInfo().apply{packageName="org.wikipedia";name="org.wikipedia.Share";exported=true;enabled=true;applicationInfo=app.packageManager.getApplicationInfo("org.wikipedia",0)}
        pm.addResolveInfoForIntent(Intent(Intent.ACTION_SEND).setType("text/plain").setPackage("org.wikipedia"),ResolveInfo().apply{activityInfo=handler})
        for(query in listOf("compartir en Wikipedia","compartir enlace en Wikipedia")) {
            val results=rows(query)
            val row=results.single{it.id.startsWith("context:app-share:")}
            val draft=row.action as Action.ComposeMessage
            assertEquals("share:org.wikipedia",draft.channel)
            assertEquals("",draft.text)
            assertTrue(CompactResults.select(query,results,config).initial.any{it.id==row.id})
        }
        handler.exported=false
        assertTrue(rows("compartir en Wikipedia").none{it.id.startsWith("context:app-share:")})
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }

}
