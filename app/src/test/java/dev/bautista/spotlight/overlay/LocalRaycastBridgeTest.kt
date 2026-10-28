package dev.bautista.spotlight.overlay

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config as AppConfig
import dev.bautista.spotlight.search.Result as SearchResult
import kotlinx.coroutines.*
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

/** Native integration fixtures only: no fake applications are packaged in production. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = SpotlightApp::class)
class LocalRaycastBridgeTest {
    private val app get() = RuntimeEnvironment.getApplication() as SpotlightApp
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private var view: LocalRaycastWebView? = null
    private var closes = 0
    private var recoveries = 0
    private val component = android.content.ComponentName("fixture.installed", "fixture.installed.Main")

    @After fun cleanup() { view?.dispose(); scope.cancel(); app.scope.cancel() }
    private fun palette() = LocalRaycastWebView(app, app, scope, { AppConfig(files = false) },
        { closes++ }, { recoveries++ }).also { view = it }

    private fun installApp() {
        val info = ApplicationInfo().apply {
            packageName = component.packageName; nonLocalizedLabel = "Installed Fixture"; enabled = true
        }
        val activity = ActivityInfo().apply {
            packageName = info.packageName; name = component.className; applicationInfo = info
            nonLocalizedLabel = "Installed Fixture"; exported = true; enabled = true
        }
        val pm = Shadows.shadowOf(app.packageManager)
        pm.installPackage(PackageInfo().apply {
            packageName = info.packageName; applicationInfo = info; activities = arrayOf(activity)
        })
        pm.addResolveInfoForIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
            ResolveInfo().apply { activityInfo = activity })
        app.search.invalidateApps()
    }
    private fun send(palette: LocalRaycastWebView, message: String) {
        LocalRaycastWebView::class.java.getDeclaredMethod("accept", String::class.java).apply {
            isAccessible = true
        }.invoke(palette, message)
    }
    @Suppress("UNCHECKED_CAST")
    private fun targets(palette: LocalRaycastWebView): Map<String, SearchResult> =
        (LocalRaycastWebView::class.java.getDeclaredField("targets").apply { isAccessible = true }
            .get(palette) as Map<String, SearchResult>).toMap()

    private suspend fun search(palette: LocalRaycastWebView, text: String, id: Int = 1) {
        send(palette, JSONObject().put("v", 1).put("id", id).put("op", "search").put("query", text).toString())
        val job = LocalRaycastWebView::class.java.getDeclaredField("queryJob").apply { isAccessible = true }.get(palette) as? Job
        withTimeout(15_000) { job?.join() }
    }
    private fun execute(palette: LocalRaycastWebView, token: String, action: String) =
        send(palette, JSONObject().put("v", 1).put("id", 2).put("op", "execute")
            .put("target", token).put("action", action).toString())

    @Test fun installedPackageFlowsThroughExistingEngineToExplicitLaunch() = runBlocking {
        installApp()
        val palette = palette()
        search(palette, "Installed Fixture")
        val row = targets(palette).entries.single { it.value.title == "Installed Fixture" }
        assertNotEquals(component.flattenToString(), row.key)
        assertNotEquals(component.packageName, row.key)
        execute(palette, row.key, "open")
        val launched = Shadows.shadowOf(app).nextStartedActivity
        assertNotNull(launched)
        assertEquals(component, launched.component)
        assertEquals(Intent.ACTION_MAIN, launched.action)
        assertTrue(launched.hasCategory(Intent.CATEGORY_LAUNCHER))
        assertEquals(1, closes)
    }

    @Test fun infoOnlyUsesTheSelectedInstalledPackageAndStaleTokensStopWorking() = runBlocking {
        installApp()
        val palette = palette()
        search(palette, "Installed Fixture")
        val token = targets(palette).entries.single { it.value.title == "Installed Fixture" }.key
        execute(palette, token, "info")
        val info = Shadows.shadowOf(app).nextStartedActivity
        assertEquals(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, info.action)
        assertEquals("package:${component.packageName}", info.data.toString())
        search(palette, "zzzzzzzzzzzzzzzzzzzzzzzz", 3)
        assertTrue(targets(palette).isEmpty())
        execute(palette, token, "open")
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
        assertEquals(1, closes)
    }

    @Test fun arbitraryTargetsUnknownOperationsAndInvalidEnvelopesCannotLaunchOrClose() {
        val palette = palette()
        listOf("not-json", "{\"v\":2,\"id\":1,\"op\":\"close\"}",
            "{\"v\":1,\"id\":0,\"op\":\"recover\"}",
            "{\"v\":1,\"id\":1,\"op\":\"delete\"}").forEach { send(palette, it) }
        execute(palette, component.flattenToString(), "open")
        execute(palette, "unknown", "clear_data")
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
        assertEquals(0, closes); assertEquals(0, recoveries)
    }

    @Test fun closeAndComposeRecoveryMessagesInvokeOnlyTheirCallbacks() {
        val palette = palette()
        send(palette, "{\"v\":1,\"id\":1,\"op\":\"close\"}")
        assertEquals(1, closes); assertEquals(0, recoveries)
        send(palette, "{\"v\":1,\"id\":2,\"op\":\"recover\"}")
        assertEquals(1, closes); assertEquals(1, recoveries)
        assertNull(Shadows.shadowOf(app).nextStartedActivity)
    }

    @Test fun rendererFailureRecoversComposeButSubresourceFailureDoesNot() {
        val palette = palette()
        val host = org.robolectric.Robolectric.buildActivity(android.app.Activity::class.java).setup()
        host.get().setContentView(palette)
        fun request(main: Boolean) = object : WebResourceRequest {
            override fun getUrl() = Uri.parse("https://spotlight.local/")
            override fun isForMainFrame() = main
            override fun isRedirect() = false
            override fun hasGesture() = false
            override fun getMethod() = "GET"
            override fun getRequestHeaders() = emptyMap<String, String>()
        }
        val error = android.webkit.LocalRaycastTestWebError()
        palette.webViewClient.onReceivedError(palette, request(false), error)
        ShadowLooper.idleMainLooper()
        assertEquals(0, recoveries)
        palette.webViewClient.onReceivedError(palette, request(true), error)
        ShadowLooper.idleMainLooper()
        assertEquals(1, recoveries)
        assertTrue(palette.webViewClient.onRenderProcessGone(palette, object : RenderProcessGoneDetail() {
            override fun didCrash() = true
            override fun rendererPriorityAtExit() = 0
        }))
        ShadowLooper.idleMainLooper()
        assertEquals(2, recoveries)
        assertEquals(0, closes)
        host.pause().stop().destroy()
    }
}
