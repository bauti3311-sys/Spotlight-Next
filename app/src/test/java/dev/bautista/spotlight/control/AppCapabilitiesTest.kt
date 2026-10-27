package dev.bautista.spotlight.control

import android.app.Application
import android.content.Intent
import android.content.pm.*
import android.net.Uri
import dev.bautista.spotlight.search.Action
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class AppCapabilitiesTest {
    private val context get()=RuntimeEnvironment.getApplication()
    private fun expose(intent:Intent,pkg:String,exported:Boolean=true,permission:String?=null):ActivityInfo {
        val activity=ActivityInfo().apply{packageName=pkg;name="$pkg.Public";this.exported=exported;this.permission=permission;enabled=true;applicationInfo=ApplicationInfo().apply{packageName=pkg;enabled=true}}
        Shadows.shadowOf(context.packageManager).addResolveInfoForIntent(intent,ResolveInfo().apply{activityInfo=activity})
        return activity
    }
    @Test fun absentCapabilitiesDoNotProduceActionButtons() {assertTrue(AppCapabilities(context).available("missing.app").isEmpty())}
    @Test fun documentedSearchRequiresExportedResolvableExactPackage() {
        val pkg="com.spotify.music";val intent=Intent(Intent.ACTION_VIEW,Uri.parse("https://open.spotify.com/search/consulta")).setPackage(pkg)
        val activity=expose(intent,pkg,false)
        assertTrue(AppCapabilities(context).available(pkg).isEmpty())
        activity.exported=true
        val c=AppCapabilities(context).available(pkg).single()
        assertEquals(AppCapabilityMechanism.DOCUMENTED_LINK,c.mechanism)
        assertEquals(Action.ComposeSearch("spotify",pkg),c.action)
        assertTrue(c.confirmationRequired)
        activity.permission="fixture.RESTRICTED"
        assertTrue(AppCapabilities(context).available(pkg).isEmpty())
    }
    @Test fun sharePreparationKeepsTheSelectedAppAndDoesNotSend() {
        val pkg="com.google.android.gm"
        expose(Intent(Intent.ACTION_SEND).setType("text/plain").setPackage(pkg),pkg)
        val c=AppCapabilities(context).available(pkg).single()
        val action=AppCapabilities(context).prepare(pkg,c.id) as Action.ComposeMessage
        assertEquals("share:$pkg",action.channel)
        assertTrue(c.expectedResult.contains("envío final"))
        assertNull(Shadows.shadowOf(context).nextStartedActivity)
    }
    @Test fun genericSearchIsOnlyExposedByItsPublicHandler() {
        val pkg="fixture.search"
        expose(Intent(Intent.ACTION_SEARCH).setPackage(pkg).putExtra(android.app.SearchManager.QUERY,"consulta"),pkg)
        assertEquals(AppCapabilityMechanism.PUBLIC_SEARCH,AppCapabilities(context).available(pkg).single().mechanism)
    }
    @Test fun revokedCapabilityIsRecheckedBeforeOpeningItsForm() {
        val pkg="fixture.share";val activity=expose(Intent(Intent.ACTION_SEND).setType("text/plain").setPackage(pkg),pkg)
        assertEquals(1,AppCapabilities(context).available(pkg).size)
        activity.exported=false
        assertTrue(runCatching{AppCapabilities(context).prepare(pkg,"share-text")}.isFailure)
        assertNull(Shadows.shadowOf(context).nextStartedActivity)
    }
    @Test fun browserSearchRetainsChromeOrVivaldiInsteadOfUsingDefaultBrowser() {
        for((topic,pkg) in listOf("chrome" to "com.android.chrome","vivaldi" to "com.vivaldi.browser")) {
            val action=ActionCatalog.searchAction(topic,pkg,"Cerati") as Action.InApp
            assertEquals(pkg,action.intent.`package`)
            assertEquals("Cerati",action.intent.data!!.getQueryParameter("q"))
        }
    }

    @Test fun disabledOrSuspendedAppsDoNotPublishShareActions() {
        val pkg="fixture.share"
        val activity=expose(Intent(Intent.ACTION_SEND).setType("text/plain").setPackage(pkg),pkg)
        val registry=AppCapabilities(context)
        assertEquals(1,registry.available(pkg).size)
        activity.enabled=false
        assertTrue(registry.available(pkg).isEmpty())
        activity.enabled=true
        activity.applicationInfo.enabled=false
        assertTrue(registry.available(pkg).isEmpty())
        activity.applicationInfo.enabled=true
        activity.applicationInfo.flags=activity.applicationInfo.flags or ApplicationInfo.FLAG_SUSPENDED
        assertTrue(registry.available(pkg).isEmpty())
        assertTrue(runCatching{registry.prepare(pkg,"share-text")}.isFailure)
        assertNull(Shadows.shadowOf(context).nextStartedActivity)
    }
    @Test fun anotherPackageCannotSupplyTheSelectedAppsShareCapability() {
        val pkg="fixture.selected"
        expose(Intent(Intent.ACTION_SEND).setType("text/plain").setPackage(pkg),"fixture.other")
        assertTrue(AppCapabilities(context).available(pkg).isEmpty())
        assertTrue(runCatching{AppCapabilities(context).prepare(pkg,"share-text")}.isFailure)
        assertNull(Shadows.shadowOf(context).nextStartedActivity)
    }
    @Test fun mapsAndSofascoreDocumentedSearchCapabilitiesMatchIntegrationRegistry() {
        val mapsPkg="com.google.android.apps.maps"
        val mapsIntent=Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/maps/search/?api=1&query=consulta")).setPackage(mapsPkg)
        expose(mapsIntent,mapsPkg)
        val mapsCap=AppCapabilities(context).available(mapsPkg).single()
        assertEquals(AppCapabilityMechanism.DOCUMENTED_LINK,mapsCap.mechanism)
        assertEquals(Action.ComposeSearch("mapas",mapsPkg),mapsCap.action)
        assertTrue(IntegrationRegistry(context).capabilities(mapsPkg).any{it.title=="Búsqueda documentada" && it.state==CapabilityState.AVAILABLE})

        val sofaPkg="com.sofascore.results"
        val sofaIntent=Intent(Intent.ACTION_VIEW,Uri.parse("https://www.sofascore.com/")).setPackage(sofaPkg)
        expose(sofaIntent,sofaPkg)
        val sofaCap=AppCapabilities(context).available(sofaPkg).single()
        assertEquals(AppCapabilityMechanism.DOCUMENTED_LINK,sofaCap.mechanism)
        assertEquals(Action.ComposeSearch("sofascore",sofaPkg),sofaCap.action)
        assertTrue(IntegrationRegistry(context).capabilities(sofaPkg).any{it.title=="Búsqueda documentada" && it.state==CapabilityState.AVAILABLE})
    }
}
