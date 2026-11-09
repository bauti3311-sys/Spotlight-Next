package dev.bautista.spotlight.control

import android.app.Application
import android.app.SearchManager
import android.content.Intent
import android.content.pm.*
import dev.bautista.spotlight.search.Action
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class C2WebSearchCapabilityTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private val pkg="fixture.websearch"
    private fun expose(action:String):ActivityInfo {
        val activity=ActivityInfo().apply{packageName=pkg;name="$pkg.Search";exported=true;enabled=true;applicationInfo=ApplicationInfo().apply{packageName=pkg;enabled=true}}
        Shadows.shadowOf(context.packageManager).addResolveInfoForIntent(Intent(action).setPackage(pkg),ResolveInfo().apply{activityInfo=activity})
        return activity
    }
    @Test fun webSearchOnlyAppPublishesOneExactPreparation() {
        expose(Intent.ACTION_WEB_SEARCH)
        val capabilities=AppCapabilities(context).available(pkg)
        assertEquals(1,capabilities.size)
        assertEquals(Action.ComposeSearch("app-web-search",pkg),capabilities.single().action)
        val action=ActionCatalog.searchAction("app-web-search",pkg,"café lunar") as Action.InApp
        assertEquals(Intent.ACTION_WEB_SEARCH,action.intent.action)
        assertEquals(pkg,action.intent.`package`)
        assertEquals("café lunar",action.intent.getStringExtra(SearchManager.QUERY))
        assertNull(Shadows.shadowOf(context).nextStartedActivity)
    }
    @Test fun publicSearchWinsWithoutDuplicateCapability() {
        expose(Intent.ACTION_SEARCH);expose(Intent.ACTION_WEB_SEARCH)
        assertEquals(Action.ComposeSearch("app",pkg),AppCapabilities(context).available(pkg).single().action)
    }
    @Test fun revokedWebSearchCannotBePrepared() {
        val activity=expose(Intent.ACTION_WEB_SEARCH)
        assertEquals(1,AppCapabilities(context).available(pkg).size)
        activity.exported=false
        assertTrue(AppCapabilities(context).available(pkg).isEmpty())
        assertTrue(runCatching{AppCapabilities(context).prepare(pkg,"search")}.isFailure)
        assertNull(Shadows.shadowOf(context).nextStartedActivity)
    }
}
