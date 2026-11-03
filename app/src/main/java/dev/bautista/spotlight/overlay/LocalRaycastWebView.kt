package dev.bautista.spotlight.overlay

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import android.util.Base64
import android.webkit.*
import dev.bautista.spotlight.SpotlightApp
import dev.bautista.spotlight.control.SearchScope
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.Action
import dev.bautista.spotlight.search.Result
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.UUID

/** Local-only renderer. No Java interface, remote pages, arbitrary intents or package names. */
internal class LocalRaycastWebView(
    context: Context,
    private val app: SpotlightApp,
    private val scope: CoroutineScope,
    private val config: () -> Config,
    private val closeOverlay: () -> Unit,
    private val recoverCompose: () -> Unit,
) : WebView(context) {
    private val origin = Uri.parse("https://spotlight.local")
    private var port: WebMessagePort? = null
    private var queryJob: Job? = null
    private val targets = mutableMapOf<String, Result>()
    private val icons = android.util.LruCache<String, String>(100)
    private var selectedToken: String? = null
    private var activeSearch: RaycastBridgeProtocol.Request? = null
    private var searchGeneration = 0
    private val protocol = RaycastBridgeProtocol { packet ->
        if (!disposed) runCatching { port?.postMessage(WebMessage(packet)) }
    }
    private var disposed = false
    private val createdAt = SystemClock.elapsedRealtime()

    init {
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        settings.javaScriptEnabled = true
        settings.allowFileAccess = false
        settings.allowContentAccess = false
        settings.domStorageEnabled = false
        settings.databaseEnabled = false
        settings.blockNetworkLoads = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        settings.setSupportMultipleWindows(false)
        isFocusableInTouchMode = true
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                val uri = request.url
                val path = uri.path.orEmpty().removePrefix("/").ifEmpty { "index.html" }
                if (request.method != "GET" || uri.scheme != "https" || uri.host != origin.host ||
                    uri.port != -1 || uri.userInfo != null || path.contains("..") || path.contains('\\')) return denied()
                return try {
                    val mime = when (path.substringAfterLast('.')) {
                        "html" -> "text/html"; "js", "mjs" -> "application/javascript"
                        "css" -> "text/css"; "json" -> "application/json"
                        "svg" -> "image/svg+xml"; "png" -> "image/png"
                        "woff2" -> "font/woff2"; else -> "application/octet-stream"
                    }
                    WebResourceResponse(mime, "UTF-8", 200, "OK", mapOf(
                        "Content-Security-Policy" to "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'none'; frame-src 'none'; object-src 'none'; base-uri 'none'",
                        "X-Content-Type-Options" to "nosniff", "Cache-Control" to "no-store"
                    ), context.assets.open("raycast-web/$path"))
                } catch (_: Exception) { denied() }
            }
            override fun onPageFinished(view: WebView, url: String) {
                if (disposed || url != "https://spotlight.local/") return
                cancelSearch()
                targets.clear(); selectedToken = null
                protocol.reset()
                port?.close()
                val session = protocol.sessionId
                val channel = createWebMessageChannel()
                port = channel[0]
                channel[0].setWebMessageCallback(object : WebMessagePort.WebMessageCallback() {
                    override fun onMessage(port: WebMessagePort, message: WebMessage) {
                        if (!disposed && session == protocol.sessionId && port === this@LocalRaycastWebView.port) accept(message.data.orEmpty())
                    }
                })
                postWebMessage(WebMessage(protocol.hello(), arrayOf(channel[1])), origin)
                android.util.Log.i("SpotlightWebView", "localPageReadyMs=${SystemClock.elapsedRealtime() - createdAt}")
            }
            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                post { recoverCompose() }
                return true
            }
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) post { recoverCompose() }
            }
        }
        loadUrl("https://spotlight.local/")
    }

    private fun denied() = WebResourceResponse("text/plain", "UTF-8", 403, "Blocked", emptyMap(), ByteArrayInputStream(ByteArray(0)))

    private fun cancelSearch() {
        activeSearch?.let { protocol.error(it, "CANCELLED") }
        activeSearch = null
        searchGeneration++
        queryJob?.cancel(); queryJob = null
        targets.clear(); selectedToken = null
    }

    private fun accept(raw: String) {
        if (disposed) return
        val request = protocol.parse(raw) ?: return
        val payload = request.payload
        fun fields(vararg names: String) = payload.keys().asSequence().toSet() == names.toSet()
        fun invalid() = protocol.error(request, "INVALID_INPUT")
        when (request.type) {
            "search.query" -> {
                val query = payload.opt("text") as? String
                val limit = payload.opt("limit") as? Int
                if (!fields("text", "limit") || query == null || query.length > 256 || limit == null || limit !in 1..100) { invalid(); return }
                cancelSearch()
                val generation = searchGeneration
                activeSearch = request
                queryJob = scope.launch {
                    try {
                        val rows = withTimeout(5000) {
                            app.search.search(query, config(), SearchScope("", "apps")).first().take(limit)
                        }
                        val prepared = withContext(Dispatchers.Default) {
                            rows.filter { row ->
                                val intent = (row.action as? Action.Launch)?.intent
                                intent?.component != null && intent.action == Intent.ACTION_MAIN &&
                                    intent.hasCategory(Intent.CATEGORY_LAUNCHER)
                            }.map { row ->
                                val token = UUID.randomUUID().toString()
                                Triple(token, row, JSONObject().put("id", token).put("label", row.title.take(256))
                                    .put("suffix", "").put("type", "Application").put("icon", icon(row)))
                            }
                        }
                        ensureActive()
                        if (disposed || generation != searchGeneration || request.sessionId != protocol.sessionId) return@launch
                        targets.clear()
                        prepared.forEach { targets[it.first] = it.second }
                        protocol.success(request, JSONObject().put("items", JSONArray(prepared.map { it.third })))
                    } catch (_: TimeoutCancellationException) { protocol.error(request, "TIMEOUT") }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { protocol.error(request, "INTERNAL") }
                    finally { if (activeSearch === request) activeSearch = null }
                }
            }
            "search.cancel" -> {
                if (!fields()) { invalid(); return }
                cancelSearch(); protocol.success(request)
            }
            "result.inspect" -> {
                val token = payload.opt("target") as? String
                if (!fields("target") || token == null || token.length > 64) { invalid(); return }
                if (!targets.containsKey(token)) { protocol.error(request, "STALE_RESULT"); return }
                selectedToken = token
                protocol.success(request, JSONObject().put("selected", token))
            }
            "action.execute" -> {
                val token = payload.opt("target") as? String
                val action = payload.opt("action") as? String
                if (!fields("target", "action") || token == null || token.length > 64 || action !in setOf("open", "info")) { invalid(); return }
                if (token != selectedToken) { protocol.error(request, "STALE_RESULT"); return }
                // Consume before requesting an effect, including ambiguous/failed outcomes.
                val row = targets.remove(token)
                selectedToken = null
                val launch = row?.action as? Action.Launch
                val component = launch?.intent?.component
                if (launch == null || component == null) { protocol.error(request, "STALE_RESULT"); return }
                val intent = if (action == "open") Intent(launch.intent) else
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${component.packageName}"))
                try {
                    context.packageManager.getPackageInfo(component.packageName, 0)
                    val activity = context.packageManager.getActivityInfo(component, 0)
                    if (!activity.enabled || !activity.exported || !activity.applicationInfo.enabled ||
                        launch.intent.action != Intent.ACTION_MAIN || !launch.intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
                        protocol.error(request, "UNAVAILABLE"); return
                    }
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    protocol.success(request, JSONObject().put("state", "requested"))
                    closeOverlay()
                } catch (_: SecurityException) { protocol.error(request, "PERMISSION_REQUIRED") }
                catch (_: Exception) { protocol.error(request, "UNAVAILABLE") }
            }
            "overlay.recover", "overlay.close" -> {
                if (!fields()) { invalid(); return }
                protocol.success(request)
                if (request.type == "overlay.close") closeOverlay() else recoverCompose()
            }
            else -> protocol.error(request, "UNSUPPORTED")
        }
    }

    private fun icon(row: Result): String {
        icons.get(row.id)?.let { return it }
        val source = row.icon ?: return ""
        val drawable = source.constantState?.newDrawable()?.mutate() ?: source
        val previousBounds = android.graphics.Rect(drawable.bounds)
        val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
        try {
            drawable.setBounds(0, 0, 48, 48)
            drawable.draw(Canvas(bitmap))
            val bytes = ByteArrayOutputStream().use { stream -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); stream.toByteArray() }
            return ("data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)).also { icons.put(row.id, it) }
        } finally { drawable.bounds = previousBounds; bitmap.recycle() }
    }

    fun dispose() {
        if (disposed) return
        disposed = true
        queryJob?.cancel()
        protocol.dispose()
        activeSearch = null; selectedToken = null
        targets.clear()
        port?.close(); port = null
        stopLoading()
        destroy()
    }
}
