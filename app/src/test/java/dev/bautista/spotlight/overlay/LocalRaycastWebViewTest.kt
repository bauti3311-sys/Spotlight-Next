package dev.bautista.spotlight.overlay

import android.app.Application
import android.net.Uri
import android.webkit.WebResourceRequest
import androidx.test.core.app.ApplicationProvider
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.data.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config as RobolectricConfig

@RunWith(RobolectricTestRunner::class)
@RobolectricConfig(sdk = [33], application = Application::class)
class LocalRaycastWebViewTest {
    private fun request(url: String, method: String = "GET") = object : WebResourceRequest {
        override fun getUrl() = Uri.parse(url)
        override fun isForMainFrame() = true
        override fun isRedirect() = false
        override fun hasGesture() = false
        override fun getMethod() = method
        override fun getRequestHeaders() = emptyMap<String, String>()
    }

    @Test fun localEntryLoadsWithNetworkAndFileAccessClosed() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val view = LocalRaycastWebView(ApplicationProvider.getApplicationContext(), SpotlightApp(), scope, { Config() }, {}, {})
        try {
            assertTrue(view.settings.javaScriptEnabled)
            assertFalse(view.settings.allowFileAccess)
            assertFalse(view.settings.allowContentAccess)
            assertTrue(view.settings.blockNetworkLoads)
            val response = view.webViewClient.shouldInterceptRequest(view, request("https://spotlight.local/index.html"))!!
            assertEquals(200, response.statusCode)
            assertTrue(response.responseHeaders["Content-Security-Policy"]!!.contains("connect-src 'none'"))
            assertTrue(response.data.bufferedReader().use { it.readText() }.contains("__nuxt"))
        } finally { view.dispose(); scope.cancel() }
    }

    @Test fun remoteTraversalAndUnexpectedMethodsNeverReachAssets() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val view = LocalRaycastWebView(ApplicationProvider.getApplicationContext(), SpotlightApp(), scope, { Config() }, {}, {})
        try {
            listOf("https://example.org/index.html", "file:///etc/passwd", "content://private/item",
                "https://spotlight.local:443/index.html", "https://spotlight.local/%2e%2e/index.html").forEach { url ->
                assertEquals(url, 403, view.webViewClient.shouldInterceptRequest(view, request(url))!!.statusCode)
                assertTrue(view.webViewClient.shouldOverrideUrlLoading(view, request(url)))
            }
            assertEquals(403, view.webViewClient.shouldInterceptRequest(view, request("https://spotlight.local/index.html", "POST"))!!.statusCode)
        } finally { view.dispose(); scope.cancel() }
    }
}
