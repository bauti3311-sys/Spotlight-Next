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
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        settings.setSupportMultipleWindows(false)
        isFocusableInTouchMode = true
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                val uri = request.url
                val path = uri.path.orEmpty().removePrefix("/").ifEmpty { "index.html" }
                if (request.method != "GET" || uri.scheme != "https" || uri.host != origin.host ||
                    uri.port != -1 || path.contains("..") || path.contains('\\')) return denied()
                return try {
                    val mime = when (path.substringAfterLast('.')) {
                        "html" -> "text/html"; "js", "mjs" -> "application/javascript"
                        "css" -> "text/css"; "json" -> "application/json"
                        "svg" -> "image/svg+xml"; "png" -> "image/png"
                        "woff2" -> "font/woff2"; else -> "application/octet-stream"
                    }
                    WebResourceResponse(mime, "UTF-8", 200, "OK", mapOf(
                        "Content-Security-Policy" to "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; connect-src 'none'; frame-src 'none'; object-src 'none'; base-uri 'none'",
                        "X-Content-Type-Options" to "nosniff"
                    ), context.assets.open("raycast-web/$path"))
                } catch (_: Exception) { denied() }
            }
            override fun onPageFinished(view: WebView, url: String) {
                if (disposed || url != "https://spotlight.local/") return
                port?.close()
                val channel = createWebMessageChannel()
                port = channel[0]
                channel[0].setWebMessageCallback(object : WebMessagePort.WebMessageCallback() {
                    override fun onMessage(port: WebMessagePort, message: WebMessage) {
                        if (!disposed) accept(message.data.orEmpty())
                    }
                })
                postWebMessage(WebMessage("spotlight-native-v1", arrayOf(channel[1])), origin)
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

    private fun reply(id: Int, rows: JSONArray? = null, error: String? = null) {
        if (disposed) return
        val payload = JSONObject().put("v", 1).put("id", id)
        if (rows != null) payload.put("rows", rows)
        if (error != null) payload.put("error", error)
        port?.postMessage(WebMessage(payload.toString()))
    }

    private fun accept(raw: String) {
        if (raw.length > 8192) return
        val message = runCatching { JSONObject(raw) }.getOrNull() ?: return
        if (message.optInt("v") != 1 || !message.has("id")) return
        val id = message.optInt("id", -1)
        if (id < 1) return
        when (message.optString("op")) {
            "search" -> {
                val query = message.opt("query") as? String ?: return
                if (query.length > 256) { reply(id, error = "Query too long"); return }
                queryJob?.cancel()
                targets.clear()
                queryJob = scope.launch {
                    try {
                        // Reuse the existing app provider/ranking; never run AI or online providers here.
                        val rows = app.search.search(query, config(), SearchScope("", "apps")).first().take(100)
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
                        targets.clear()
                        prepared.forEach { targets[it.first] = it.second }
                        reply(id, JSONArray(prepared.map { it.third }))
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { reply(id, error = "Could not read installed applications") }
                }
            }
            "execute" -> {
                val target = message.opt("target") as? String ?: return
                val action = message.optString("action")
                if (target.length > 64 || action !in setOf("open", "info")) return
                val row = targets[target] ?: return
                val launch = row.action as? Action.Launch ?: return
                val component = launch.intent.component ?: return
                // Only a currently returned, explicit installed launcher activity can execute.
                val intent = if (action == "open") Intent(launch.intent) else
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${component.packageName}"))
                try {
                    context.packageManager.getActivityInfo(component, 0)
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    closeOverlay()
                } catch (_: Exception) { reply(id, error = "Application is unavailable") }
            }
            "recover" -> recoverCompose()
            "close" -> closeOverlay()
            else -> reply(id, error = "Operation not allowed")
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
        disposed = true
        queryJob?.cancel()
        targets.clear()
        port?.close(); port = null
        stopLoading()
        destroy()
    }
}
