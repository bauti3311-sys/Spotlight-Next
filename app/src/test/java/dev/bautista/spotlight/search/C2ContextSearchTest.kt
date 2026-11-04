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
    private fun install(pkg:String="org.wikipedia",title:String="Wikipedia",search:Boolean=true) {
        val info=ApplicationInfo().apply{packageName=pkg;nonLocalizedLabel=title;enabled=true}
        val main=ActivityInfo().apply{packageName=pkg;name="Main";nonLocalizedLabel=title;applicationInfo=info;enabled=true;exported=true}
        val handler=ActivityInfo().apply{packageName=pkg;name="$pkg.Search";applicationInfo=info;enabled=true;exported=true}
        val pm=Shadows.shadowOf(app.packageManager)
        pm.installPackage(PackageInfo().apply{packageName=pkg;applicationInfo=info;activities=arrayOf(main,handler)})
        pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),ResolveInfo().apply{activityInfo=main})
        pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(pkg),ResolveInfo().apply{activityInfo=main})
        if(search)pm.addResolveInfoForIntent(Intent(Intent.ACTION_SEARCH).setPackage(pkg),ResolveInfo().apply{activityInfo=handler})
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

}
